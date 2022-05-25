package simlei.graphs.spec
// package simlei.graphs.spec

import simlei.graphs.*
import scala.util.NotGiven
import scala.annotation.implicitNotFound

sealed trait EdgeN[NT]:
  def outNode: NT       = EdgeN.outNode(this).asInstanceOf // TODO: investigate (I1)
  def inNodes: List[NT] = EdgeN.inNodesTuple(this).toList.asInstanceOf // TODO: investigate (somewhat I1)
  def outNodeExact             = EdgeN.outNode(this)
  def inNodesTuple             = EdgeN.inNodesTuple(this)
  val arity = inNodes.size
object EdgeN:
  case class ==>:[NT, +From <: NT, +To <: NT] (from: From, to: To ) extends EdgeN[NT]
  case class &&: [NT, +From <: NT, +ToE <: EdgeN[NT]](from: From, to: ToE) extends EdgeN[NT]
  infix final type InNodesTupleOf[NT, E <: EdgeN[NT]] <: Tuple = E match
    case ==>:[_,fromh,toh] => fromh *: EmptyTuple
    case &&: [_,fromc,toc] => fromc *: InNodesTupleOf[NT,toc]
  infix def inNodesTuple[NT, E <: EdgeN[NT]](edge: E): InNodesTupleOf[NT,E] = edge match // TODO: see if other name for NT would work
    case e: ==>:[NT,fh,th] => (e.from *: EmptyTuple).asInstanceOf
    case e: &&: [NT,fc,tc] => (e.from *: inNodesTuple(e.to)).asInstanceOf

  // functions to extract, from an EdgeN, the output node
  final type OutNode[N, E <: EdgeN[N]] /* <: N */ = E match // TODO: (I1) investigate: why is this type bound not enforcible?
    case &&: [_,from,toE] => OutNode[N,toE]
    case ==>:[_,from ,to] => to
  def outNode[N, E <: EdgeN[N]](edge: E): OutNode[N, E] = edge match // TODO: see if other name for NT would work
    case e: &&: [N,from,toE] => (outNode(e.to)).asInstanceOf // TODO: necessary?? mirroring inNodesTuple hack...
    case e: ==>:[N,from,to] => (e.to).asInstanceOf // TODO: necessary?? mirroring inNodesTuple hack...

end EdgeN

trait DGraphBuilder[NT, INT <: NT, ONT <: NT]:

  extension[From <: NT](from: From)
    infix def ==>: [To  <: NT](to: To)(
      using NotGiven[To <:< INT]    @implicitNotFound("${To} seems to be subtype of the input node type ${INT} and can therefore not be target of an arrow."),
            NotGiven[From <:< ONT]  @implicitNotFound("${From} seems to be subtype of the output node type ${ONT} and can therefore not be source of an arrow.")
    ): ==>:[From,To] = EdgeN.==>:(from, to)
  extension[From  <: NT](from: From)
    infix def &&: [To <: EdgeN[NT]](to: To)(
      using NotGiven[From <:< ONT]  @implicitNotFound("${From} seems to be subtype of the output node type ($ONT) and can therefore not be source of an arrow.")
    ): DGraphBuilder.this.&&:[From,To] = EdgeN.&&:(from, to)

  type InNodesTupleOf[E <: EdgeN[NT]] = EdgeN.InNodesTupleOf[NT, E]
  infix type ==>:[+From <: NT, +To <: NT] = EdgeN.==>:[NT,From,To]
  infix type &&: [+From <: NT, +To <: EdgeN[NT]] = EdgeN.&&:[NT,From,To]
  final type OutNode[E <: EdgeN[NT]] = EdgeN.OutNode[NT,E]
  def outNode[E <: EdgeN[NT]](edge: E): EdgeN.OutNode[NT, E] = EdgeN.outNode(edge)

end DGraphBuilder


// a construct that allows the builder pattern, but will not expose the builder to the API (`DGraph`)
object DGraphBuilder:

  class DGraphBuilderInstance[NT, INT <: NT, ONT <: NT] extends DGraphBuilder[NT,INT,ONT]
  extension [NT, INT <: NT, ONT <: NT](self: DGraphBuilderInstance[NT,INT,ONT])
    def build[ET <: EdgeN[NT]](              nodes: List[NT],
                            inputNodes    : List[INT],
                            outputNodes   : List[ONT],
                            edges         : List[ET]) = DGraphThatExports(self, nodes, inputNodes, outputNodes, edges)
  class DGraphThatExports[NT, INT <: NT, ONT <: NT, ET <: EdgeN[NT],
                          BuilderT <: DGraphBuilderInstance[NT,INT,ONT] // we try to lose this stuff
                         ]( val builder: BuilderT,
                            override val nodes         : List[NT],
                            override val inputNodes    : List[INT],
                            override val outputNodes   : List[ONT],
                            override val edges         : List[ET]  ) extends DGraph[NT, INT, ONT, ET]:
    export builder.InNodesTupleOf
    export builder.==>:
    export builder.&&:
    export builder.OutNode
    export builder.outNode
  end DGraphThatExports
  def ofNodeTypes[NT, INT <: NT, ONT <: NT] = new DGraphBuilderInstance[NT,INT,ONT]
    // def ofNode
    
end DGraphBuilder

trait DGraph  [NT, INT <: NT, ONT <: NT, ET <: EdgeN[NT]]:
  val nodes         : List[NT]
  val inputNodes    : List[INT]
  val outputNodes   : List[ONT]
  val edges         : List[ET]

  final type NodeT        = NT
  final type InputNodeT   = INT
  final type OutputNodeT  = ONT
  final type EdgeT        = ET
end DGraph


// Graveyard:
  // type AllEdgesExtractor[Tp <: Tuple] = Tp match
  //   case EmptyTuple => Nothing
  //   case t *: rest => t | AllEdgesExtractor[rest]
