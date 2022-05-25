package simlei.graphs.tests
import simlei.graphs.spec.*
import simlei.graphs.spec.EdgeN.*

object testWorldRSA:

  // We build a program that configures RSA parameters, validates its parameters, and performs the cryptographic functions.
  // We use the graph concept: The program is taught the structure of the computation, and is implemented only where necessary.

  // the most important and most-used bit: which nodes are there? More on that later, but "nodes are like variables".
  // For example, "P" and "Q" will be prime numbers, and combine into the product "N".

  sealed trait RSANode
  object RSANode:
    case object P         extends RSANode
    case object Q         extends RSANode
    case object PxQ       extends RSANode
    case object N         extends RSANode
    case object Phi       extends RSANode
    case object E         extends RSANode
    case object DE        extends RSANode
    case object NxDE      extends RSANode
  end RSANode
  import RSANode.*

  // The following enables nice syntax (optional):
  // `graphBuilder` provides the "arrow syntax" for graphs, used to make edges ( `&&:` and `==>:` ) (see Doc(1))
  // Also, we specify as type arguments what is the general-, the input- and the output-node-type
  val graphBuilder = DGraphBuilder.ofNodeTypes[RSANode, P.type|Q.type|E.type, NxDE.type]
  import graphBuilder.{&&:, ==>:}

  // Let's connect those nodes as to how the data flows... 
  object RSAEdge:
    val edgeToPQ      = P   &&: Q  ==>: PxQ
    val edgeToN       = PxQ        ==>: N
    val edgeToPhi     = PxQ        ==>: Phi
    val edgeToDE      = Phi &&: E  ==>: DE
    val edgeToNxDE    = N   &&: DE ==>: NxDE
    val allEdgesTuple = ( edgeToN, edgeToPQ, edgeToPhi, edgeToDE, edgeToNxDE )
  end RSAEdge

  // Let's build that graph from the nodes and edges!
  val graph = graphBuilder.build(
      nodes       = List(P, Q, PxQ, N, Phi, E, DE, NxDE),
      inputNodes  = List(P, Q, E),
      outputNodes = List(NxDE),
      edges       = RSAEdge.allEdgesTuple.toList
    )


  // Now that we have that graph, let's elaborate on it what makes it useful:
  // A graph is the structural description of an algorithm ("what (nodes) goes where (edges to nodes)")
  // How to make a program controller — a circuit — out of that graph?
  // For that, we need "meaning" for that structure. In programming terms, data and functions.
  //   Think of nodes like variables, that need types. Edges are functions and are explained further below...
  //   .. A variable in typical programming languages can have exactly one type. A node is a more abstract concept:
  //   .. here, it can have a type for...
  //      - Domain : what you can put into it ("you": the programmer, or the arrows of the graph).
  //                     .. e.g. "String", "BigInt", arbitrarily complex data (any class, interface, ...).
  //      - Error  : what results when you validate it ("must be a prime number, must be positive, ...").
  //                     .. either that, or again, values that belong to the Domain (when validation succeeds).
  //      - Content: what results in the context of the whole graph — the program — "what can there be at any point in time?":
  //            .. - "is not yet set" (e.g. non-input nodes at the very start of the program; input nodes get a default always.)
  //            .. - "it is set but not valid (because..? — which `Error` value?)", 
  //            .. - "it's set and is valid (and what is it? — which value of the "Domain" type?)"
  //   .. To conclude, a node represents these layers which a program has to handle, and which are connected with each other.
  //   .. All you need to do is specify these types for each node (now), and provide the critical part of the implementation (later).

  // These are the data structures the program will use...
  object RSAData:
    case class DAsInverseOfE(e: BigInt, d    : BigInt)
    case class RSAKeys      (n: BigInt, dAndE: DAsInverseOfE)
    case class PQ           (p: BigInt, q    : BigInt) // combines P and Q for joint validation
  import RSAData.*

  // Let's specify what data is stored in the nodes when there are no errors:
  type DomainType[N <: graph.NodeT] = N match
    case P.type     => BigInt
    case Q.type     => BigInt
    case PxQ.type   => PQ
    case N.type     => BigInt
    case Phi.type   => BigInt
    case E.type     => BigInt
    case DE.type    => DAsInverseOfE
    case NxDE.type  => RSAKeys

  // Let's specify what errors can occur when the algorithm propagates that data
  sealed trait RSAError; object RSAError:
    case object NotAPrime             extends RSAError
    case object PEqualsQ              extends RSAError
    case object EMustBeGreaterThanOne extends RSAError
    case object EMustBeSmallerThanPhi extends RSAError
    case object EMustBeCoprimeWithPhi extends RSAError

  // Upper bound for the validation error type `ErrorOf` (necessary for reasons, just copy & paste)
  final type Error[N <: RSANode] = RSAError

  // Nodes and which errors their validation can produce.
  import RSAError.*
  final type ErrorOf[N <: RSANode] <: Error[N] = N match
    case P.type     => NotAPrime.type
    case Q.type     => NotAPrime.type
    case PxQ.type   => PEqualsQ.type
    case N.type     => Nothing
    case Phi.type   => Nothing
    case E.type     => EMustBeGreaterThanOne.type
    case DE.type    => EMustBeSmallerThanPhi.type | EMustBeCoprimeWithPhi.type
    case NxDE.type  => Nothing

  val circuitShape = graph.specifyCircuit[DomainType, Error, ErrorOf]

  // solving one of the hard problem of CS...
  object names:
    // usage: see circuitImpl for "normal API user" pattern
    // usage: see testWorldRSA_assertions for "power user" pattern (test suite etc)
    export testWorldRSA.RSANode  as nodes
    export testWorldRSA.RSAEdge  as edges
    export testWorldRSA.graph    as graph
    export testWorldRSA.RSAError as error
    export testWorldRSA.RSAData  as data
    export testWorldRSA.circuitShape as circuitShape

end testWorldRSA


object circuitImpl extends CircuitImpl(testWorldRSA.circuitShape):
  import testWorldRSA.names.nodes.*
  import testWorldRSA.names.edges.*
  import testWorldRSA.names.graph.{NodeT, InputNodeT, OutputNodeT, EdgeT}
  import testWorldRSA.names.{error, data}
  import testWorldRSA.Error as cerror


  object functions:
    def pq     (p: BigInt, q: BigInt)              = data.PQ(p,q)
    def n      (pq: data.PQ)                       = pq.p * pq.q
    def phi    (pq: data.PQ)                       = (pq.p - 1) * (pq.q - 1)
    def d      (phi: BigInt, e: BigInt)            = data.DAsInverseOfE(e, e.modInverse(phi))
    def nAndDE (n: BigInt, de: data.DAsInverseOfE) = data.RSAKeys(n, de)
    def wrong  (p: BigInt, q: BigInt, e: BigInt)   = p*q*e

    def isPrime(number: BigInt): Either[error.NotAPrime.type, BigInt] =
      number.isProbablePrime(9999) match
        case true =>  Right(number)
        case false => Left(error.NotAPrime)

    def pqConstraint(pq: data.PQ): Either[error.PEqualsQ.type, data.PQ] =
      pq.p == pq.q match
        case true =>  Left(error.PEqualsQ)
        case false => Right(pq)

  end functions

  override def implementation[E <: EdgeT](edge: E): DomainFun.Simple =
    // summon[PQ <:< ((BigInt, BigInt) => simlei.graphs.tests.circuitImpl.spec.domain.EdgeFunTupledT[edgeToPQ.type])]
    edge match
      case e : edgeToPQ.type    => DomainFun.implementSimple(e, functions.pq    .tupled)
      case e : edgeToN.type     => DomainFun.implementSimple(e, functions.n     .tupled)
      case e : edgeToPhi.type   => DomainFun.implementSimple(e, functions.phi   .tupled)
      case e : edgeToDE.type    => DomainFun.implementSimple(e, functions.d     .tupled)
      case e : edgeToNxDE.type  => DomainFun.implementSimple(e, functions.nAndDE.tupled)

  // def validations[N <: spec.nodes.NodeT](node: N): ForValidation.Implemented[N] =
  //   node match
  //     case n : P.type   => ForValidation.implement(n, functions.isPrime)
  //     case n : Q.type   => ForValidation.implement(n, functions.isPrime)
  //     case n : PxQ.type => ForValidation.implement(n, functions.pqConstraint)
end circuitImpl



// Doc(1)
  // The graphBuilder provides the nice edge syntax by knowing the types of all-, input- and output-nodes.
  // Implementing DGraph directly would work, too — this is a convenience method. Without it however, 
  // .. the exports that are made in `DGraphThatExports` would have to be made there, too.

