import simlei.graphs.boundedge.*

object nodes:
  type NT = NodeT
  sealed trait NodeT
  object L extends NodeT
  object M extends NodeT
  object N extends NodeT
  object O extends NodeT
  object P extends NodeT

  extension[To <: EdgeN](to: To)
    infix def && [From  <: NT](from: From): EdgeCons[From,To] = EdgeCons(from, to)
  extension[To  <: NT](to: To)
    infix def <-- [From <: NT](from: From): Edge1[From,To] = Edge1(from, to)

  sealed trait EdgeN extends BoundEdgeN[NT]
  case class Edge1   [+From <: NT, +To  <: NT   ](from:  From, to:    To ) extends BoundEdge1[NT, From, To]     with EdgeN
  case class EdgeCons[+From <: NT, +ToE <: EdgeN](from:  From, to:    ToE) extends BoundEdgeCons[NT, From, ToE] with EdgeN
end nodes

import nodes.*

val tp1: (EdgeCons[M.type, Edge1[L.type, O.type]], Edge1[N.type, P.type]) = (
    O <-- L && M,
    P <-- N
  )
val toList = tp1.toList
class EdgesReceiver[ET <: nodes.EdgeN](edges: List[ET]):
  type EdgeT = ET

val edges = EdgesReceiver(toList)

val arg = O <-- L && M
val argWrong = O <-- M && L

summon[arg.type <:< edges.EdgeT]
// summon[argWrong.type <:< toClass.EdgeT] // won't work, wrong order

