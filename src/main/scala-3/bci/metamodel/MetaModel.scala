package bci.metamodel

import scala.util.Try

import Graphs.*
  
// the most important type: Nodes! connected by edges (see below) into directed graphs without loops.
trait NodeAny { type Content }
trait Node[C] extends NodeAny { type Content = C }

// Products: a Product is a node, as well as two incoming edges.
// The product node contains a tuple of both ingoing values.
// It is the only node type that has multiple inputs!

sealed trait ProductEdgeProto {
  type NodeIn1
  type NodeIn2
  type NodeOut
  val from1: NodeIn1
  val from2: NodeIn2
  val to: NodeOut
}

object ProductEdge:
  def unapply[
  CIn1, CIn2, OutC, 
  InT1 <: Node[CIn1],
  InT2 <: Node[CIn2],
  OutT <: Node[OutC]
  ](pn: ProductEdge[CIn1,CIn2,OutC,InT1,InT2,OutT]): Product3[InT1,InT2,OutT] = (pn.from1, pn.from2, pn.to)
class ProductEdge[
  CIn1, CIn2, OutC, 
  InT1 <: Node[CIn1],
  InT2 <: Node[CIn2],
  OutT <: Node[OutC]
  ](val from1: InT1, val from2: InT2, val to: OutT) extends ProductEdgeProto {
    override type NodeIn1 = InT1
    override type NodeIn2 = InT2
    override type NodeOut = OutT
    // def mkProduct(val1: CIn1, val2: CIn2): OutC
  }



// class ProductEitherEdge[
//   ErrT, CIn1, CIn2,
//   InT1 <: Node[Either[ErrT, CIn1]],
//   InT2 <: Node[Either[ErrT, CIn2]]
// ](val from1: InT1, val from2: InT2, val to: Node[Either[ErrT, (CIn1,CIn2)]]) extends ProductEdge[
//   Either[ErrT, CIn1], Either[ErrT, CIn2], Either[ErrT, (CIn1, CIn2)],
//   InT1, InT2, Node[Either[ErrT, (CIn1,CIn2)]]
// ](from1, from2, to) {
//   override def mkProduct(val1: Either[ErrT, CIn1], val2: Either[ErrT, CIn2]): Either[ErrT, (CIn1,CIn2)] =
//     val1 match
//       case Left(l1) => Left(l1)
//       case Right(r1) => val2 match
//         case Left(l2) => Left(l2)
//         case Right(r2) => Right((r1,r2))

// }


// Edges: connect and implement node-to-node functions (graph edges)

sealed trait FuncEdgeProto {
  type NodeIn
  type NodeOut
  val from: NodeAny
  val to: NodeAny
}

object FuncEdge:
  def unapply[ CIn, COut, InT <: Node[CIn], OutT <: Node[COut] ](fe: FuncEdge[CIn,COut,InT,OutT]): Product2[InT,OutT] = (fe.from, fe.to)
class FuncEdge[
  CIn, COut,
  InT <: Node[CIn],
  OutT <: Node[COut]
  ](override val from: InT, override val to: OutT) extends FuncEdgeProto {

  override type NodeIn = InT
  override type NodeOut = OutT
}

// type ProductEdgeImpl[EdgeT <: ProductEdge[?,?,?,?,?,?]] = EdgeT match
//   case ProductEdge[cin1,cin2,cout,inT1,inT2,outT] => ProductEdgeImplementation[EdgeT,cin1,cin2,cout]

// type ProductEdgeImpl2[EdgeT <: ProductEdge[?,?,?,?,?,?]] = EdgeT match
//   case ProductEdge[cin1,cin2,cout,inT1,inT2,outT] => ProductEdgeImplementation[EdgeT,cin1,cin2,cout]

type ProductEdgeImpl[EdgeT <: ProductEdge[?,?,?,?,?,?]] = EdgeT match
  case ProductEdge[cin1,cin2,cout,inT1,inT2,outT] => ProductEdgeImplementation[EdgeT,cin1,cin2,cout]

@FunctionalInterface trait ProductEdgeImplementation[EdgeT,-In1,-In2,+Out] {
  def apply(in1: In1, in2: In2): Out
}

type FuncEdgeImpl[EdgeT <: FuncEdge[?,?,?,?]] = EdgeT match
  case FuncEdge[cin,cout,inT,outT] => FuncEdgeImplementation[EdgeT,cin,cout]

@FunctionalInterface trait FuncEdgeImplementation[EdgeT,-In,+Out] {
  def apply(in: In): Out
}

// // a lens type that can map a node to a part of e.g. a case class, or any other class
// // (google "match type scala 3" for how this is specified)
// type NodeLens[Data, NodeT <: Node[?]] = NodeT match
//   case Node[t] => Lens[Data, t]

// extract the content type out of a node type (google match type scala 3)
type NodeContent[NodeT <: Node[?]] = NodeT match
  case Node[t] => t



