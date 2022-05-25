package simlei.graphs
import cats.Show

// a walk has elements start, ..., end
// an "empty" walk is therefore one element long
// a walk with zero elements should not exist
trait NodeTrace[+N]:
  def toList: List[N]
  def toEdges = toList.toTuples.toList
  def isStationary: Boolean

case class RelWalk[+N](steps: List[N]) extends NodeTrace[N]:
  override def toList = steps.toList
  override def isStationary = steps.size == 0
  def reverse = steps.reverse
object RelWalk:
  extension[N](self: RelWalk[N])
    def toWalk(origin: N) = Walk(origin, self)
    def appended(nextNode: N): RelWalk[N] = self.copy(steps = self.steps :+ nextNode)
  given relwalkShow[Inner](using innerShow: Show[Inner]): Show[RelWalk[Inner]] = { relwalk =>
    relwalk.toList.map{innerShow.show(_)}.mkString("-> ", " -> ", "")
  }
  given relwalkShowGen[Inner]: Show[RelWalk[Inner]] = relwalkShow(using Show.fromToString)
  def of[N](nodes: N*) = RelWalk(nodes.toList)
  def ofList[N](nodes: List[N]) = RelWalk(nodes)
  def ofEdges[N](edges: (N,N)*) =
    if (edges.size > 0) {
      val allEdgesFlattened = edges.map{_.toList}.toList.flatten // [a, b, b, c, c, d, d, e...] if formed correctly
      if ( allEdgesFlattened.drop(1).dropRight(1).toTuples.exists{(x,y) => x != y} ) throw new RuntimeException(s"malformed edges $edges for relWalk construction")
    }
    RelWalk(edges.map{(from,to) => to}.toList)

case class Walk[+N](origin: N, steps: RelWalk[N]) extends NodeTrace[N]:
  val destination = toList.last
  def isStationary = toList.size == 1
  override def toList: List[N] = (origin +: steps.toList)
  def toRelWalk = steps
  def reverse =
    val reverted = toList.reverse
    Walk(reverted.head, RelWalk(reverted.tail))
object Walk:
  extension[N](self: Walk[N])
    def appended(nextNode: N): Walk[N] = self.copy(steps = self.steps.appended(nextNode))

  given walkShow[Inner](using innerShow: Show[Inner]): Show[Walk[Inner]] = { walk =>
    val relShow: Show[RelWalk[Inner]] = summon
    s"${innerShow.show(walk.origin)} ${relShow.show(walk.steps)}" // walk.toList.map{innerShow.show(_)}.mkString("", " -> ", "")
  }
  given walkShowGen[Inner]: Show[Walk[Inner]] = walkShow(using Show.fromToString)
  def of[N](origin: N, others: N*) = Walk(origin, RelWalk.ofList(others.toList)) // TODO: caution docs, two elements!
  def ofList[N](nodes: List[N]) = Walk(nodes(0), RelWalk(nodes.drop(1))) // TODO: caution docs, two elements!
  def ofEdgeList[N](allEdges: List[(N,N)]): Walk[N] =
    if(allEdges.size == 0) throw new RuntimeException("walk cannot be constructed with empty edge list; use RelWalk instead.")
    val allEdgesFlattened = allEdges.map{_.toList}.toList.flatten // [a, b, b, c, c, d, d, e...] if formed correctly
    val innerGrouped = allEdgesFlattened.drop(1).dropRight(1).grouped(2).toList // [[b,b],[c,c],[d,d]] if formed correctly
    innerGrouped.foreach{listTuple => // check that the above comment form is satisfied
      println(s"testing $listTuple")
      if ( ! listTuple.forall{_ == listTuple.head} )
        throw new RuntimeException(s"malformed edges $allEdges (innerGrouped: $innerGrouped) for walk construction")
    }
    Walk(allEdges(0)._1, RelWalk(allEdges.map{(from,to) => to}))
  def ofEdges[N](edgeFromOrigin: (N,N), moreEdges: (N,N)*): Walk[N] =
    ofEdgeList((edgeFromOrigin +: moreEdges).toList)

case class WalkToCycle[N](leading: Walk[N], cycle: CycleWalk[N])
object WalkToCycle:
  given wtcShow[Inner](using innerShow: Show[Inner]): Show[WalkToCycle[Inner]] = { wtc =>
    val walkShow: Show[Walk[Inner]] = summon
    val cycleShow: Show[CycleWalk[Inner]] = summon
    s"[${walkShow.show(wtc.leading)}] -> ${cycleShow.show(wtc.cycle)}"
  }
  given wtcShowGen[Inner]: Show[WalkToCycle[Inner]] = wtcShow(using Show.fromToString)

case class CycleWalk[N](walk: Walk[N]): // marker object to prevent calculating cyclic graph connectivity
  if(! CycleWalk.isStrictCycle(walk)) throw new RuntimeException(s"is no cycle: ${walk.toList}")
object CycleWalk:
  given cycleShow[Inner](using innerShow: Show[Inner]): Show[CycleWalk[Inner]] = { cycle =>
    val walkShow: Show[Walk[Inner]] = summon
    s"CycleWalk(${walkShow.show(cycle.walk)})"
  }
  def isStrictCycle[N](walk: Walk[N]) = walk.origin == walk.destination
  given cycleShowGen[Inner]: Show[CycleWalk[Inner]] = cycleShow(using Show.fromToString)
