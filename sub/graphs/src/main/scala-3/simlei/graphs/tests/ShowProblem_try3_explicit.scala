package rsamodel_written

// solution of second problem: ----- props to @ragnar in scala-users discord

// --- model:

trait GraphSpec:
  type NodeT
end GraphSpec

trait NodeTypeMap[GS <: GraphSpec]:
  val baseGraph: GS
  type TypeOf[NT <: baseGraph.NodeT]
end NodeTypeMap

trait DomainSpec[GS <: GraphSpec] extends NodeTypeMap[GS]:
  val graph: GS
  override val baseGraph: graph.type = graph
  override type TypeOf[NT <: baseGraph.NodeT]
end DomainSpec

trait ValidationSpec[GS <: GraphSpec, DS <: DomainSpec[GS]] extends NodeTypeMap[GS]:
  val domain: DS
  override val baseGraph: domain.graph.type = domain.graph
  override type TypeOf[NT <: baseGraph.NodeT] <: Either[String, domain.TypeOf[NT]]
end ValidationSpec

// --- instances:

sealed trait Knot
case object SoleNode1 extends Knot
case object SoleNode2 extends Knot

object testGraph extends GraphSpec:
  override type NodeT = Knot
end testGraph

object testDomain extends DomainSpec[testGraph.type]:
  override val graph: testGraph.type = testGraph
  override type TypeOf[NT <: baseGraph.NodeT] = NT match
    case SoleNode1.type   => BigInt
    case SoleNode2.type   => Boolean
end testDomain

object testValidation extends ValidationSpec[testGraph.type, testDomain.type]:
  override val domain: testDomain.type = testDomain
  override type TypeOf[NT <: testDomain.baseGraph.NodeT] <: Either[String, testDomain.TypeOf[NT]] = NT match
    case SoleNode1.type => Right[String, testDomain.TypeOf[NT]]
    case SoleNode2.type => Either[String, testDomain.TypeOf[NT]]
end testValidation

object program:
  def testSetter[NT <: testGraph.NodeT](n: NT, v: testValidation.TypeOf[NT]) =
    println(s"success: for $n, got: $v")
  def testSetter2[NT <: testGraph.NodeT](n: NT, v: testDomain.TypeOf[NT]) =
    println(s"success: for $n, got: $v")
  def main(args: Array[String]) = 
    // testSetter2[SoleNode1.type](SoleNode1, "should-fail") // Should Fail
    testSetter2[SoleNode1.type](SoleNode1, BigInt(1)) // Should succeed
    // testSetter[SoleNode1.type](SoleNode1, Right("test-should-fail")) // should fail, complaining that expected: BigInt, found: String
    testSetter[SoleNode1.type](SoleNode1, Right(BigInt(1))) // should work
    // testSetter[SoleNode1.type](SoleNode1, Left("error!")) // should NOT work!
    testSetter[SoleNode2.type](SoleNode2, Right(true)) // should work
    testSetter[SoleNode2.type](SoleNode2, Left("error!")) // should work!
    // val y = testSetter[SoleNode1.type](SoleNode1, Left("error!")) // should NOT work!


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

