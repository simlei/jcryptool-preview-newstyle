package showproblem

// solution of second problem: ----- props to @ragnar in scala-users discord

trait GraphSpec:
  type NodeT
  def nodes: List[NodeT]

trait NodeTypeMap[GS <: GraphSpec]:
  val baseGraph: GS
  type NODE = baseGraph.NodeT
end NodeTypeMap

class DomainSpec[GS <: GraphSpec](val graph: GS) extends NodeTypeMap[GS]:
  override val baseGraph: graph.type = graph
  type TypeOf[NT <: NODE]
end DomainSpec

class ValidationSpec[GS <: GraphSpec, DS <: DomainSpec[GS]](val domain: DS) extends NodeTypeMap[GS]:
  override val baseGraph: domain.graph.type = domain.graph
  type ValiErr
  type TypeOf[NT <: NODE] <: Either[ValiErr, domain.TypeOf[NT]]
end ValidationSpec

// instances:

object SoleNode // one is the loneliest and best-debuggable number wrt match types :)

object testGraph extends GraphSpec:
  override def nodes = List(SoleNode)
  override type NodeT = SoleNode.type

object testDomain extends DomainSpec(testGraph):
  override type TypeOf[NT <: NODE] = NT match
    case SoleNode.type   => BigInt

object testValidation extends ValidationSpec(testDomain):
  override type ValiErr = String
  override type TypeOf[NT <: testDomain.graph.NodeT] = NT match
    case SoleNode.type => Right[Nothing, testDomain.TypeOf[SoleNode.type]]
  def testSetter[NT <: testDomain.graph.NodeT](n: NT, v: TypeOf[NT]) =
    println(s"success: for $n, got: $v")

object program:
  def main(args: Array[String]) = 
    // val x = testValidation.testSetter[SoleNode.type](SoleNode, Right("test-should fail"))
    val x = testValidation.testSetter[SoleNode.type](SoleNode, Right(BigInt(1))) // works

// ------------------

// demo of second problem:


// trait GraphSpec:
//   type NodeT
//   def nodes: List[NodeT]

// trait NodeTypeMap[GS <: GraphSpec](val baseGraph: GS):
//   type NODE = baseGraph.NodeT
//   type TypeOf[NT <: NODE]
// end NodeTypeMap

// class DomainSpec[GS <: GraphSpec](val graph: GS) extends NodeTypeMap[graph.type](graph):
//   type NODE = graph.NodeT
//   override type TypeOf[NT <: NODE]
// end DomainSpec

// class ValidationSpec[GS <: GraphSpec, DS <: DomainSpec[GS]](val domain: DS) extends NodeTypeMap[domain.graph.type](domain.graph):
//   type ValiErr = Any
//   override type TypeOf[NT <: NODE] <: Either[ValiErr, domain.TypeOf[NT]]
// end ValidationSpec

// // instances:

// object SoleNode // one is the loneliest and best-debuggable number wrt match types :)

// object testGraph extends GraphSpec:
//   override def nodes = List(SoleNode)
//   override type NodeT = SoleNode.type

// object testDomain extends DomainSpec[testGraph.type](testGraph):
//   override type TypeOf[NT <: NODE] = NT match
//     case SoleNode.type   => BigInt

// object testValidation extends ValidationSpec[testGraph.type, testDomain.type](testDomain):
//   override type TypeOf[NT <: NODE] = NT match
//     case SoleNode.type => Right[Nothing, testDomain.TypeOf[SoleNode.type]]



// // --- Ragnar 2nd answer:
// Your minimization seems to work if you also use singleton types AND are a bit more explicit about what the NODE type in the TypeOf of DomainSpec is

// trait GraphSpec:
//   type NodeT
//   def nodes: List[NodeT]

// trait NodeTypeMap[GS <: GraphSpec](val baseGraph: GS):
//   type NODE = baseGraph.NodeT
//   type TypeOf[NT <: NODE]
// end NodeTypeMap

// class DomainSpec[GS <: GraphSpec](val graph: GS) extends NodeTypeMap[graph.type](graph):
//   // type NODE = graph.NodeT // commented out and replaced alias in line below
//   override type TypeOf[NT <: graph.NodeT]
// end DomainSpec

// class ValidationSpec[GS <: GraphSpec, DS <: DomainSpec[GS]](val domain: DS) extends NodeTypeMap[domain.graph.type](domain.graph):
//   type ValiErr = Any
//   override type TypeOf[NT <: NODE] <: Either[ValiErr, domain.TypeOf[NT]]
// end ValidationSpec



// // --- Ragnar made it it work:
// trait X:
//   type A
// class Foo[Y <: X](val x: Y):
//   type P[XA <: x.A]
// class Bar(val y: X) extends Foo[y.type](y):
//   override type P[XA <: y.A]

// For the minimized example, its really just that x.A and y.A are different types I mean, look at them, the paths are different!
// Now if you assert that x: y.type then things become different and you can have the minimal example work,

// // -----------------------------------------


// // --- Aly minimized: https://scastie.scala-lang.org/m0bPHkfFTXmeAyMPXkiXdg
// trait X:
//   type A
// class Foo(val x: X):
//   type P[XA <: x.A]
// class Bar(val y: X) extends Foo(y):
//   override type P[XA <: y.A]

// I think it's simply that Scala doesn't have the ability to know what fields map to what. if you change XA <: y.A into XA <: x.a in that example, it compiles, but then you'll have trouble still at the callsite.


// // --------- original post:
// package simleigraphs
// trait GraphSpec:
//   type NodeT
//   def nodes: List[NodeT]

// trait NodeTypeMap[GS <: GraphSpec](val baseGraph: GS):
//   type NODE = baseGraph.NodeT
//   type TypeOf[NT <: NODE]
// end NodeTypeMap

// class DomainSpec[GS <: GraphSpec](val graph: GS) extends NodeTypeMap[GS](graph):
//   override type TypeOf[NT <: NODE]
// end DomainSpec

// class ValidationSpec[GS <: GraphSpec, DS <: DomainSpec[GS]](val domain: DS) extends NodeTypeMap[GS](domain.graph):
//   type ValiErr = Any
//   override type TypeOf[NT <: domain.NODE] <: Either[ValiErr, domain.TypeOf[NT]]
// end ValidationSpec

