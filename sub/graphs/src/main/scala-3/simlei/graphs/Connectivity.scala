package simlei.graphs
import simlei.util.*
import cats.Show

extension[N](list: List[N])
  def toTuples = list.sliding(2).filter(_.size == 2).map{case List(a,b) => (a,b); case _ => throw new RuntimeException("impossible")}

sealed trait Relation[+N]
object Relation: // Unconnected, Linear.SameNode, Linear.Route,     trait Linear[N](val a: N, val b: N, val walk: Walk[N]) extends Relation[N]:
  case object Unconnected extends Relation[Nothing]
  sealed trait Linear[+N](val relFwdWalk: RelWalk[N]) extends Relation[N]: // the destination is in the walk, but not the origin
    def isAtoB: Boolean
    def isBtoA: Boolean = ! isAtoB
  object Linear:
    case object SameNode extends Linear[Nothing](RelWalk(List())):
      override def isAtoB = true
    case class RouteAtoB[N](val toB: RelWalk[N]) extends Linear[N](toB):
      override def isAtoB = true
    case class RouteBtoA[N](val toA: RelWalk[N]) extends Linear[N](toA):
      override def isAtoB = false
  sealed trait Indirect[+N](val commonNode: N, val relFwdWalkA: RelWalk[N], val relFwdWalkB: RelWalk[N]) extends Relation[N]
  object Indirect:
    case class SameOrigin[+N](commonOrigin: N, toA: RelWalk[N], toB: RelWalk[N]) extends Indirect[N](commonOrigin, toA, toB)
    case class SameDestination[+N](commonDestination: N, fromA: RelWalk[N], fromB: RelWalk[N]) extends Indirect[N](commonDestination, fromA, fromB)

case class Connectivity[N] private (
  nodes: List[N],
  edges: List[(N,N)],
  cyclicWalks: List[WalkToCycle[N]],
  acyclicWalks: List[Walk[N]],
  inputnodes: List[N],
  outputnodes: List[N],
  isCyclic: Boolean // may be true even if cyclicWalks is empty (no entry point found e.g.)
):
  def isEmpty = nodes.isEmpty

object Connectivity:
  import scala.collection.mutable.{Map => MutableMap}
  // TODO: edges, nodes are assumed to be distinct

  def findIngoingEdgesOf[N] (nodes: List[N], edges: List[(N,N)])(node: N) = edges.filter{(from,to) => to == node}
  def findOutgoingEdgesOf[N](nodes: List[N], edges: List[(N,N)])(node: N) = edges.filter{(from,to) => from == node}
  def findChildrenOf[N]     (nodes: List[N], edges: List[(N,N)])(node: N) = findOutgoingEdgesOf(nodes,edges)(node).map{(from,to) => to}
  def findParentsOf[N]      (nodes: List[N], edges: List[(N,N)])(node: N) = findIngoingEdgesOf(nodes,edges)(node).map{(from,to) => to}

  def findCycle[N](nodes: List[N], edges: List[(N,N)])(walk: Walk[N]): Option[WalkToCycle[N]] =
    val nodeIndices = MutableMap[N, List[Int]]() // maps nodes to index occurrences in walk.
    for {(n, idx) <- walk.toList.zipWithIndex} {
      nodeIndices.put(n, nodeIndices.getOrElse(n, List()) :+ idx)
    }
    val cycleNode = nodeIndices.toList.filter{(n, idcs) => idcs.size > 1}.headOption
    cycleNode.map{(n, idcs) => 
      WalkToCycle(
        leading = Walk.ofList(walk.toList.slice(0, idcs(0))), 
        cycle = CycleWalk(Walk.ofList(walk.toList.slice(idcs(0), idcs(1)+1))))
    }
  def differentiateCycle[N](nodes: List[N], edges: List[(N,N)])(walk: Walk[N]): Either[WalkToCycle[N], Walk[N]] =
    findCycle(nodes,edges)(walk) match
      case Some(walkToCycle) => Left(walkToCycle)
      case None              => Right(walk)

  def findWalks[N](nodes: List[N], edges: List[(N,N)])(walkFrontier: List[Walk[N]]): (List[WalkToCycle[N]], List[Walk[N]]) =
    if(walkFrontier.isEmpty) return (List(), List())
    val frontierWithChildren = walkFrontier.map{fWalk => (fWalk, findOutgoingEdgesOf(nodes,edges)(fWalk.destination))}
    val (finishedFrontier, unfinishedFrontier) = frontierWithChildren.partition{(walk,children) => children.isEmpty}
    // every frontier walk now may fork into zero, one, multiple new walks to from the new frontier, as we attach the children.
    val appendedWithCycles = for { (fWalk: Walk[N], outEdges: List[(N, N)]) <- unfinishedFrontier } yield {
      val newWalks = outEdges.map{(from,to) => fWalk.appended(to)}.distinct
      val withCycles = newWalks.map(differentiateCycle(nodes,edges)(_)) // TODO: exclude walks that yield a cycle
      val justCycles = withCycles.map{_.left.toOption}.flatten
      val justWalks = withCycles.map{_.right.toOption}.flatten
      (justCycles, justWalks)
    }
    val walksFinished = finishedFrontier.map{(walk,_) => walk}
    val newFrontier = appendedWithCycles.map{(cyclic,acyclic) => acyclic}.flatten
    val deadEnds = appendedWithCycles.map{(cyclic,acyclic) => cyclic}.flatten
    val (recursedDeadEnds, recursedWalks) = findWalks(nodes,edges)(newFrontier)
    (recursedDeadEnds ++ deadEnds, walksFinished ++ recursedWalks)

  def of[N](nodes: N*)(edges: (N,N)*) = ofLists(nodes.toList, edges.toList)
  def ofEdges[N](edges: (N,N)*) = ofEdgeList(edges.toList)
  def ofEdgeList[N](edges: List[(N,N)]) = 
    val nodes = edges.map(edge => edge.toList).flatten.distinct
    ofLists(nodes,edges)

  def ofLists[N](nodesSpec: List[N], edgesSpec: List[(N,N)]): Connectivity[N] =
    val nodesInEdges = edgesSpec.map(edge => edge.toList).flatten.distinct
    val allNodes = (nodesSpec ++ nodesInEdges).distinct
    if(allNodes.size == 0) return Connectivity(List(),List(),List(),List(),List(),List(), false)
    val allEdges = edgesSpec.distinct
    val nodesWithNoIngoing = nodesSpec.filter{node => ! edgesSpec.exists{(from,to) => to == node}}
    val nodesWithNoOutgoing = nodesSpec.filter{node => ! edgesSpec.exists{(from,to) => from == node}}
    val hasNoInputs = nodesWithNoIngoing.size == 0
    val hasNoOutputs = nodesWithNoOutgoing.size == 0
    // TODO: calculate cycles even if no inputs, or outputs
    if(hasNoInputs) return Connectivity(allNodes, allEdges, List(), List(), nodesWithNoIngoing, nodesWithNoOutgoing, true)
    val walksAndCycles = Connectivity.findWalks(allNodes,allEdges)(nodesWithNoIngoing.map{Walk.of(_)})
    return Connectivity(allNodes, allEdges, walksAndCycles._1, walksAndCycles._2, nodesWithNoIngoing, nodesWithNoOutgoing, walksAndCycles._1.size > 0)




