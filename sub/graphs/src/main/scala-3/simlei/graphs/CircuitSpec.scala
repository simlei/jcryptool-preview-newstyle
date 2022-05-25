package simlei.graphs.spec
import simlei.graphs.*
import scala.util.NotGiven
import scala.annotation.implicitNotFound

extension[NT, INT <: NT, ONT <: NT,
          NS <: DGraphBuilder[NT,INT,ONT], 
          ET <: EdgeN[NT],
          GT      <: DGraph[NT,INT,ONT,ET]]
          (self: GT)
  def specifyCircuit[
    DType   <: [N <: NT] =>> Any,
    VErr    <: [N <: NT] =>> Any,
    VErrFor <: [N <: NT] =>> VErr[N]] = new CircuitShape[NT,INT,ONT,ET,DType,VErr,VErrFor,GT](self){}


trait CircuitShape[
  NT,
  INT     <: NT,
  ONT     <: NT,
  ET      <: EdgeN[NT],
  DType   <: [N <: NT] =>> Any,
  VErr    <: [N <: NT] =>> Any,
  VErrFor <: [N <: NT] =>> VErr[N],      // implemented as match type with upper bound VErr hopefully enforced
  GT      <: DGraph[NT,INT,ONT,ET]]
  (val graph: GT):

  sealed trait Error[N <: NT]
  object Error:
    trait Unset[N <: NT] extends Error[N]; object Unset:
      case class ThisInNode[N <: INT]() extends Unset[N]
      case class Ingoing[N <: NT](val trace: Any) extends Unset[N] // TODO: (bugprone): trace is Any
    case class NotValid[N <: NT](err: VErrFor[N]) extends Error[N]
  end Error

  final type ValidationError   [N <: NT]     = VErr[N]
  final type ValidationErrorFor[N <: NT]     = VErrFor[N]

  final type DomainOf   [N <: NT]             = DType[N]
  final type ValidatedOf[N <: NT]             = Either[VErrFor[N], DType[N]]
  final type ContentOf  [N <: NT]             = Either[Error[N], DType[N]] // TODO: or was it VErrFor?
  // final type ContentOf  [N <: NT]             = Either[VErr   [N], DType[N]] // TODO: or was it VErrFor?

  import EdgeN.*
  // yields the tuple type for all inputs (i.e. also single-input --> Tuple1[that-type])
  type EdgeFunInputTypeTupled[E <: ET] <: Tuple = E match
    case &&: [_,from,toE] => DType[from] *: EdgeFunInputTypeTupled[toE]
    case ==>:[_,from ,to] => DType[from] *: EmptyTuple
  type EdgeFunOutputType[E <: ET] = E match
    case &&: [_,from,toE] => EdgeFunOutputType[toE]
    case ==>:[_,from ,to] => DType[to]

  type EdgeFunTupledT[E <: ET] = Function1[EdgeFunInputTypeTupled[E], EdgeFunOutputType[E]]

  // // generic arity experiment...
  type EdgeFunT[E <: ET] <: simlei.graphs.generic.GenericizableFunTargets = E match
    case ==>:[_,from ,to] => Function1[DType[from],DType[to]]
    case &&: [_,from,toE] => simlei.graphs.generic.GenericArity[DType[from], EdgeFunT[toE]]

end CircuitShape

trait CircuitImpl[
  NT,
  INT     <: NT,
  ONT     <: NT,
  ET      <: EdgeN[NT],
  DType   <: [N <: NT] =>> Any,
  VErr    <: [N <: NT] =>> Any,
  VErrFor <: [N <: NT] =>> VErr[N],
  GSpec   <: DGraph[NT,INT,ONT,ET],
  CSpec   <: CircuitShape[NT,INT,ONT,ET,DType,VErr,VErrFor,GSpec]] // implemented as match type with upper bound VErr
  (val spec: CSpec):
  val graph = spec.graph

  def implementation[E <: ET](edge: E): DomainFun

  object ForDomain:
    class Implemented[+E <: ET] (val edge: ET, val transition: List[Any] => Any)
    def implement[E <: ET](e: E, f: spec.EdgeFunTupledT[E]) = 
      def impl(inputs: List[Any]): Any = f(Tuple.fromArray(inputs.toArray).asInstanceOf)
      Implemented(e, impl)
  end ForDomain

  trait DomainFun:
    val edge: ET
  object DomainFun:
    export Simple.implement as implementSimple


    class Simple private (override val edge: ET, anyFunction: List[Any] => Any) extends DomainFun
    object Simple:
      def implement[E <: ET](edge: E, implementation: spec.EdgeFunTupledT[E]) =
        // this function is passed to "Simple" and loses type checking. However, it cannot 
        // be constructed unless it did at one point typecheck through `implement` (here)
        def thisFunctionChecksNoType(inputs: List[Any]): Any =
          implementation(Tuple.fromArray(inputs.toArray).asInstanceOf)
        Simple(edge, thisFunctionChecksNoType)
    // Iterative: f: I => O | sondern f: (Init, A, B => B)
  end DomainFun

  extension[I,O](f: Function1[I,O])
    def tupled: Function1[Tuple1[I],O] =
      def impl(i: Tuple1[I]): O = f(i._1)
      impl

end CircuitImpl








// DOCS:

// (Doc-1)
//  def testSetterDomain[NT <: RSANode](n: NT, v: circuit.DomainT[NT]) = println(s"success: for $n, got: $v")

// (Doc-2)
// these types are both necessary as long as https://github.com/lampepfl/dotty/issues/15138 stands the way it is (closed at time of writing)
// ErrorOf is the MatchType, Error is it's upper bound which must be a real type down the road (sealed trait etc.).

// (Doc-3)
  // object into_PxQ_1      extends (PxQ.type <~~ P.type ~~~ Q.type) // Doesn't work yet, ValueOf[EdgeN] or alike must be implemented
  // object PxQ_to_N_1      extends (N.type <~~ PxQ.type) // Deactivated: this would work with ValueOf somewhat, but not ~~~
  // infix class <~~ [+H <: NT : ValueOf, +T  <: NT : ValueOf] extends Edge1[H,T](summon[ValueOf[H]].value, summon[ValueOf[T]].value)
  // infix class ~~~ [+H <: NT : ValueOf, +TT <: EdgeN[? <: NT]](to:  H, from:    TT) extends EdgeCons[H,TT]

// (Doc-4)
  // yields the "regular" type for a single input and TupleN with N>=2 for compound edges
  // // Tuple => Domain for SISO as well as MISO edges
  // type EdgeFunTupledT[E <: edges.EdgeT] = Function1[EdgeFunInputTypeTupled[E], EdgeFunOutputType[E]]
  // // Domain => Domain for SISO edges, Tuple => Domain for MISO edges
  // type EdgeFunSemitupledT[E <: edges.EdgeT] = Function1[EdgeFunInputTypeSemitupled[E], EdgeFunOutputType[E]]
  // type EdgeFunInputTypeSemitupled[E <: edges.EdgeT] = E match
  //   case factory.&&: [from,toE] => EdgeFunInputTypeTupled[E]
  //   case factory.==>:[from ,to] => TypeOf[from]

// (Doc-5) -- match type workaround w.r.t. implicit resolution of EdgeFunT
    // def workaround[E <: spec.domain.graph.EdgeT, FT <: Any]
    //   (e: E, f: FT)(using spec.domain.EdgeFunT[E] <:< FT): FT = f
