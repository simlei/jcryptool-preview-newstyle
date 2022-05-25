package simlei.graphs
import cats.Show
// import com.github.arturopala.tree.*
import simlei.util.*

object BiEdge:
  given [I,O]: Show[BiEdge[I,O]] = {e => s"${e.from}->${e.to}"}
  def unapply[From,To](edge: BiEdge[From,To]) = (edge._1, edge._2)

// TODO: (bugprone) test whether equality checking is implemented correctly!
class BiEdge[+From,+To](val from: From, override val to: To) extends NEdge[Tuple1[From],To](Tuple1(from), to) with Product2[From,To]:
  override def canEqual(that: Any): Boolean = that.isInstanceOf[BiEdge[_,_]]
  override def _1: From = from
  override def _2: To = to

class NEdge[+Inputs <: Tuple, +To](val inputs: Inputs, val to: To):
  ()

type EdgeOf[+N] = BiEdge[N,N]

// base implementation of Directed Acyclic Graphs with type-designated input nodes
trait IODAG[Node,INode <: Node,EdgeT <: EdgeOf[Node]] {
  type NodeT = Node
  type InputNodeT = INode
  def connectivity: Connectivity[Node]
  val inputs: List[INode] // CONTRACT: implementors have to check, sadly, whether connectivity inputs and this implementation match

  val nodes: List[Node] = connectivity.nodes
  val edges: List[EdgeT]

  val walks           : List[Walk[Node]]  = connectivity.acyclicWalks
  def reverseWalks    : List[Walk[Node]]  = walks.map{_.reverse}
  def edgewalks       : List[List[EdgeT]] = walks.map{_.toEdges}.map{_.map {case (i,o) => findEdge(i,o).get}}
  def reverseEdgewalks: List[List[EdgeT]] = reverseWalks.map{_.toEdges}.map{_.map {case (i,o) => findEdge(o,i).get}}

  def findEdge(in: Node, out: Node) = edges.find{ case BiEdge(i,o) => in == i && out == out }

  val outputs: List[Node] = connectivity.outputnodes
  val isEmpty: Boolean = walks.isEmpty

  val linearRelationsAtoB: Map[(Node,Node), List[Relation.Linear.RouteAtoB[Node]]] =
    val nodePairsAsym = List.fill(2)(nodes).flatten.combinations(2).map{ combo => (combo(0), combo(1)) }.toList
    val linearRelations = MultiMap.makeEmpty[(Node,Node), Relation.Linear.RouteAtoB[Node]]
    for {
      walkAsList <- walks.map{_.toList}
      nonIdPairs = walkAsList.combinations(2).toList
      pair <- nonIdPairs
    } {
      val pairTuple = (pair(0) -> pair(1))
      val (idxStart, idxEnd) = (walkAsList.indexOf(pairTuple._1), walkAsList.indexOf(pairTuple._2))
      val relWalk = RelWalk.ofList(walkAsList.slice(idxStart+1, idxEnd+1))
      val relation = Relation.Linear.RouteAtoB(relWalk)
      linearRelations.addMulti(pairTuple, relation)
    }
    Map.from(linearRelations.mapValues(_.toList))

  def getLinearRelations(a: Node, b: Node): List[Relation.Linear[Node]] =
    if(a == b) return List(Relation.Linear.SameNode)
    linearRelationsAtoB.get(a -> b) match
      case Some(rels) => rels
      case None => List()
  def getLinearRelationsWithBackward(a: Node, b: Node): List[Relation.Linear[Node]] =
    getLinearRelations(a, b) match
      case Nil => getLinearRelations(b, a) match
        case Nil => List()
        case rels => rels.map{ atob => Relation.Linear.RouteBtoA(atob.relFwdWalk) }
      case rels => rels

  // def sequentialize(frontier: List[List[EdgeT]]): List
  def linearize(paths: List[List[EdgeT]]): List[EdgeT] =
    var frontiers = paths
    var result = List[EdgeT]()
    while (! frontiers.forall(_.isEmpty)) {
      val candidates = frontiers.map{_.headOption}.flatten.toList.sortBy(_.toString).sortBy( candidate =>
        frontiers.map(_.headOption).flatten.contains(candidate)
      )
      val candidateTargetCountsInTails = for {
        candidate <- candidates
        frontierTails = frontiers.map { _ match
          case head :: tail => tail
          case Nil => Nil
        }
        count = frontierTails.map{ _.count {_.to == candidate.to} }.sum
      } yield (candidate,count)
      assert (! candidateTargetCountsInTails.forall {_._2 > 0})
      val candidate = candidateTargetCountsInTails.toList.sortBy{_._2}.head._1
      frontiers = frontiers.map {
        case head :: tail if head == candidate => tail
        case untouched@_ => untouched
      }
      result = result :+ candidate
    }
    result
    
  // TODO: is this correctly placed somewhere else now?
  // println(s"asserting for inputs... $inputs") ---> null (postcondition for initialization does not work correctly, here or almost everywhere)
  // println(s"asserting for inputnodes... ${connectivity.inputnodes}")
  // assert(inputs == connectivity.inputnodes)
}

object IODAG:
  // validates the connectivity that a DAG is based on: no loops etc.
  def validateDeduplicateEdges[N,E<:EdgeOf[N]](connectivity: Connectivity[N])(edges: List[E]): Either[String, List[E]] =
    val dedup = edges.distinct
    val asSet1 = Set.from(dedup.map{case BiEdge(a,b) => (a,b)}); val asSet2 = Set.from(connectivity.edges)
    asSet1 == asSet2 match
      case true => Right(dedup)
      case false => Left(s"provided edges not equal to the ones automatically determined. Provided: $asSet1, versus: $asSet2")
  def validateDeduplicateInputnodes[N,IN<:N](connectivity: Connectivity[N])(ins: List[IN]): Either[String, List[IN]] =
    val dedup = ins.distinct
    val asSet1 = Set.from(ins); val asSet2 = Set.from(connectivity.inputnodes)
    asSet1 == asSet2 match
      case true => Right(dedup)
      case false => Left(s"provided inputnodes not equal to the ones automatically determined. Provided: $asSet1, versus: $asSet2")

  def validateConnectivity[N](connectivity: Connectivity[N]): Either[String, Connectivity[N]] =
    // TODO: check if empty graphs are a problem
    def either_assert(condition: Boolean, reason: String): Option[String] =
      condition match
        case false => Some(reason)
        case true => None
    def makeCyclicMessage =
      val uniqueCycles = connectivity.cyclicWalks.map{_.cycle}.distinct
      if(uniqueCycles.size > 0)
        s"The graph is cyclic; it has no inputs"
      else
        s"The graph is cyclic; the first cycle is: ${uniqueCycles(0)}"
    if(connectivity.isCyclic)
      return Left(makeCyclicMessage)
    return Right(connectivity)

// this implementation tracks the type of inputnodes and edges
case class IDAG[Node,INode <: Node,EdgeT <: BiEdge[Node,Node]] private (val connectivity: Connectivity[Node], inputnodes: List[INode], edges: List[EdgeT]) extends IODAG[Node,INode,EdgeT]:
  override val inputs: List[INode] = inputnodes
  assert(inputnodes == connectivity.inputnodes, s"the input nodes provided for class IDAG do not match the ones that result from the graph structure: provided: ${inputnodes}, from_structure: ${connectivity.inputnodes}")
object IDAG:
  // together with ofValidating, main constructor; may fail at runtime:
  //  - if connectivity has loops (see validateConnectivity)
  //  - if the provided input nodes do not match the ones in connectivity
  def ofConnectivity[N,IN<:N,E<:EdgeOf[N]](connectivity: Connectivity[N], inputnodes: List[IN], edges: List[E]): IDAG[N,IN,E] =
    val conn = IODAG.validateConnectivity(connectivity).right.get
    val e = IODAG.validateDeduplicateEdges(conn)(edges).right.get
    val i = IODAG.validateDeduplicateInputnodes(conn)(inputnodes).right.get
    IDAG(conn, i, e)
  def ofLists[N,IN<:N,E<:EdgeOf[N]](nodes: List[N], edges: List[E], inputnodes: List[IN]): IDAG[N,IN,E] =
    val allNodes = (nodes ++ inputnodes).distinct
    val e0 = edges(0)
    ofConnectivity(Connectivity.ofLists(allNodes, edges.map{e => (e._1,e._2)}), inputnodes, edges)
  def of[N,IN<:N,E<:EdgeOf[N]](nodes: N*)(edges: E*)(inputnodes: IN*): IDAG[N,IN,E] = ofLists(nodes.toList, edges.toList, inputnodes.toList)
  def ofEdgeList[N,IN<:N,E<:EdgeOf[N]](edges: List[E], inputnodes: List[IN]): IDAG[N,IN,E] = 
    val nodes = edges.map(e => List(e._1, e._2)).flatten.distinct
    ofLists(nodes,edges,inputnodes)
  def ofEdges[N,IN<:N,E<:EdgeOf[N]](edges: E*)(inputnodes: List[IN]): IDAG[N,IN,E] = ofEdgeList(edges.toList, inputnodes)

end IDAG




// --- old notes:

// -- (in IDAG:)
  // import scala.deriving.*
  // import scala.compiletime.{erasedValue,summonInline}
  // import scala.{compiletime => ct}
  // import scala.{deriving => d}
  // inline def inferred[TInputNode](using mInput: Mirror.Of[TInputNode]) =
  //   // println(s"HELLOOOOO : : : $mInput")
  //   ct.summonAll[mInput.MirroredElemTypes]
  // inline def summonInstance[T]


// hint: 
// given [T, U](using CanEqual[T, U]): CanEqual[Box[T], Box[U]] =
// CanEqual.derived

// guaranteed to have no loops

// a symmetric Product2 -- used as constraints for _general_ edges (where the specific input and output is considered of the same class)
// (connect two elements of type Node). equivalent to Product2[Node, Node]
// (NO! fails!) matching: case (from,to): EdgeOf[spec.defs.RSAParamNode] => println("case 1")
// TODO: why is matching without EdgeOf[...] having Any in second place?
// type EdgeOf[+Node] = Product2[Node,Node] // for shortening certain definitions

// a possibly asymmetric Product2 -- used as constraints for _specific_ edges (where the specific input and output is considered of the same class)
// matching: case (from,to): EdgeBetween[spec.defs.RSAParamNode, _] => println("case 1")
// type EdgeBetween[+N1,+N2] = Product2[N1,N2]

// usage: case object X_to_Y extends EdgeOf(nodeIn, nodeOut) with CustomSealedTraitForExhaustiveMatching
// matching: (NO! fails! ) case EdgeOf(from,to) => println("case 1")
// object EdgeOf:
//   def unapply[N](edge: EdgeOf[N]) = (edge._1, edge._2)
// class EdgeOf[+N](val from: N, val to: N) extends Product2[N,N]:
//   def canEqual(that: Any): Boolean = that.isInstanceOf[EdgeOf[_]]
//   def _1: N = from
//   def _2: N = to

