package simlei.graphs.tests
import scala.util.NotGiven

object rsaAssertions {
  val subj = testWorldRSA.circuitShape
  // import subj.graph.*
  import testWorldRSA.names.nodes.*
  import testWorldRSA.names.edges.*
  import testWorldRSA.names.graph.{
    NodeT, InputNodeT, OutputNodeT, EdgeT, 
    InNodesTupleOf, // TODO: where is it's friend?
    ==>:, &&:, 
    outNode, OutNode}
  import testWorldRSA.names.circuitShape
  import testWorldRSA.names.circuitShape.{
    EdgeFunInputTypeTupled, 
    EdgeFunOutputType, 
    EdgeFunT,
    ContentOf, ValidatedOf, DomainOf}
  import testWorldRSA.names.{error, data}
  import testWorldRSA.circuitShape.Error as cerror

  // CHECKS: instruct the compiler to prove or disprove that an edge is part of the graph.
  // also: two ways of expressing this type

  // val existingEdge1 = P &&: Q ==>: N // TODO: make that work with val-types
  // summon[existingEdge1.type <:< EdgeT]
  // summon[(P.type &&: Q.type ==>: N.type) <:< EdgeT]

  // CHECKS: instruct the compiler to prove or disprove that nodes are input or output 
  summon[P.type <:< InputNodeT]
  summon[NxDE.type <:< OutputNodeT]
  summon[NotGiven[DE.type <:< InputNodeT]]
  summon[NotGiven[DE.type <:< OutputNodeT]]

  // CHECKS: transformation of edges to input node tuples
  summon[InNodesTupleOf[P.type ==>: N.type] =:= Tuple1[P.type]]
  summon[InNodesTupleOf[P.type &&: Q.type ==>: N.type] =:= (P.type, Q.type)]
  // OutNode
  summon[OutNode[Q.type &&: P.type ==>: N.type] =:= N.type]
  summon[NotGiven[OutNode[Q.type &&: P.type ==>: N.type] =:= DE.type]]
  // subtype (remove later)
  summon[OutNode[Q.type &&: P.type ==>: N.type] <:< NodeT]

  // CHECKS: transformation of edges to input node (domain) type tuples
  summon[EdgeFunInputTypeTupled[          P.type ==>: N.type]            =:= Tuple1[BigInt]]
  summon[EdgeFunInputTypeTupled[          P.type &&: Q.type ==>: N.type] =:= (BigInt, BigInt)]
  summon[NotGiven[ EdgeFunInputTypeTupled[P.type &&: Q.type ==>: N.type] =:= (String *: BigInt *: EmptyTuple) ]]

  // correct function output type
  summon[EdgeFunOutputType[P.type &&: Q.type ==>: N.type] =:= BigInt]

  // example "user-defined" functions that should get accepted by the compiler as fitting implementors of edges
  def myFunctionTupled(input: (BigInt, BigInt)): BigInt = input._1 * input._2
  def myFunctionRegular(p: BigInt, q:BigInt) = data.PQ(p,q)
  def myFunctionRegular3(p: BigInt, q:BigInt, boom: BigInt): BigInt = p * q
  def myFunctionNxDE(n: BigInt, de: data.DAsInverseOfE) = data.RSAKeys(n, de)

  // tupled (EdgeFunT) kind of implementation type
  summon[EdgeFunT[N.type &&: DE.type ==>: NxDE.type] =:= Function2[BigInt,data.DAsInverseOfE,data.RSAKeys]]
  summon[NotGiven[ EdgeFunT[P.type &&: Q.type ==>: N.type] =:= Function3[BigInt,BigInt,BigInt,Int]]]

  val test2: EdgeFunT[P.type &&: Q.type ==>: PxQ.type] = myFunctionRegular
  val test3: EdgeFunT[N.type &&: DE.type ==>: NxDE.type] = myFunctionNxDE
  // val test4: EdgeFunT[P.type &&: Q.type ==>: N.type] = myFunctionRegular3 // does not work, but should give the most beautiful error such an abstraction could give :)

  // { // CHECKS: semitupled and the like, but not used right now.
  //   // correct function input type -- no tuple for singular ..
  //   summon[EdgeFunInputTypeSemitupled[P.type ==>: N.type] =:= BigInt]
  //   summon[NotGiven[ EdgeFunInputTypeSemitupled[P.type ==>: N.type] =:= String ]]
  //   // correct function input type -- tupled for hyper..
  //   summon[EdgeFunInputTypeSemitupled[P.type &&: Q.type ==>: N.type] =:= (BigInt, BigInt)]
  //   summon[NotGiven[ EdgeFunInputTypeSemitupled[P.type &&: Q.type ==>: N.type] =:= String ]]
  //   // correct function type :star:
  //   summon[EdgeFunTupledT[P.type &&: Q.type ==>: N.type] <:< Function1[(BigInt,BigInt), BigInt]]
  //   val test1: EdgeFunSemitupledT[P.type &&: Q.type ==>: N.type] = myFunctionTupled
  // }

  // -------- setter assertions (older than the above)

  val rsa = circuitShape

  def testSetterContent[N    <: NodeT](n: N, v: ContentOf[N])    = println(s"success: for $n, got: $v")
  def testSetterValidation[N <: NodeT](n: N, v: ValidatedOf[N]) = println(s"success: for $n, got: $v")
  def testSetterDomain[N     <: NodeT](n: N, v: DomainOf[N])     = println(s"success: for $n, got: $v")

  def run =
    // import rsa.*
    // import rsa.nodesSpec.*

    // absolutely MUST compile!
    testSetterDomain    (P , BigInt(1))
    testSetterDomain    (N , BigInt(10))
    testSetterValidation(P , Right(BigInt(1)))
    testSetterValidation(PxQ , Left(error.PEqualsQ))
    testSetterValidation(N , Right(BigInt(1)))
    testSetterContent   (P , Right(BigInt(1)))
    // 1. Found:    testWorldRSA.names.error.NotAPrime.type
    //    Required: testWorldRSA.ErrorOf[N]
    summon[error.NotAPrime.type <:< testWorldRSA.ErrorOf[P.type]]
    testSetterContent   (P , Left(cerror.NotValid(error.NotAPrime)))
    testSetterContent   (P , Left(cerror.Unset.ThisInNode()))

    // --- would be nice if compiles
    testSetterDomain(P, 11)           // auto cast to BigInt works!
    testSetterValidation(P, Right(11)) // auto cast to BigInt works!
    // testSetterDomain(N, (1,2))      // auto cast to tuple of BigInt doesn't work (yet)...

    // // --- stuff that should NOT compile!
    // // N can't have an error, "required: Nothing"
    // testSetterValidation(N , Left(error.PEqualsQ))

    // // wrong type
    // testSetterDomain    (P , "wrong-type")

    // // wrong inner error type
    // testSetterValidation(DE, Left(error.NotAPrime(P)))

    // // wrong inner type; should show that BigInt is required
    // testSetterValidation(N , Right(BigInt(1),"wrong-type"))

    // // wrong error type (required: Nothing)
    // testSetterValidation(N , Left(error.NotAPrime))

    // // wrong error type
    // testSetterContent   (N , Left(cerror.NotValid(error.NotAPrime)))

    // // (N is no InputNode, should say something to that end... (either P,Q or E)... with P as arg 1 it should work)
    // testSetterContent   (N , Left(cerror.Unset.ThisInNode()))

}
