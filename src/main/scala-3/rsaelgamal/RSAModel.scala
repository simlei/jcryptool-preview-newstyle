package rsaelgamal

import scala.util.Try
// import monocle.syntax.all.*
// import monocle.*
import rsaelgamal.*
import bci.metamodel.*
import bci.metamodel.Graphs.*
import bci.metamodel.{FuncEdge, Node, NodeAny, ProductEdge, StateHandler}
import bci.view.Parsing
import simlei.util.*
import rsaelgamal.Intl.Translatable
import rsaelgamal.RSAModel.ErrOrInt
import zio.stream.*
import zio.*

object RSAModel {

  sealed trait Err {
    def isDependent: Boolean = this == Err.Dependent
    def isStandalone: Boolean = ! isDependent
    def isStandaloneMinusNotYetSet: Boolean = isStandalone && ! isNotYetSet
    def isNotYetSet: Boolean = this == Err.NotYetSet
  }

  object Err {
    case object NotYetSet                                              extends Err
    case object Dependent                                              extends Err // marks that a previous node had an error
    case class  ParseErr              (e: Parsing.ParseErr)            extends Err { export e.* }
    case class  IsNoPrime             (p: BigInt)                      extends Err
    case class  SameValueNotAllowedFor(node1: NodeAny, node2: NodeAny) extends Err
    case class  EMustBeSmallerThanPhi (e: BigInt, phi: BigInt)         extends Err
    case object EMustBeGreaterThanOne                                  extends Err
    case class  EMustBeCoprimeWithPhi (e: BigInt, phi: BigInt)         extends Err
    case class  MustBePositive (value: BigInt)         extends Err
    case class  PlaintextMustBeSmallerThanN(plain: BigInt, edn: EDNData) extends Err
  }
  extension(state: NodeState)
    def getErr(n: NodeAny) = state.getContentAny(n) match
      case Left(e: Err) => e
      case _ => throw RuntimeException("casting node content to Err failed.")
    def isErr(n: NodeAny): Boolean = isErrAnd(n)(_ => true)
    def isErrAnd(n: NodeAny)(predicate: Err => Boolean): Boolean =
      val value = state.getContentAny(n)
      value match
        case Left(e: Err) => predicate(e)
        case _ => false
    def isDependentErr(n: NodeAny): Boolean           = isErrAnd(n){_.isDependent}
    def isStandaloneErr(n: NodeAny): Boolean          = isErrAnd(n){_.isStandalone}
    def isStandaloneAndNotYetSet(n: NodeAny): Boolean = isErrAnd(n){_.isStandaloneMinusNotYetSet}
    def isNotYetSet(n: NodeAny): Boolean              = isErrAnd(n){_.isNotYetSet}


  type ParamGraphT = Either[Err, ?]
  type NodeT       = Node[ParamGraphT]
  type PlanT = GraphTransitionPlan[ParamGraphT]
  type InstrT = GraphInstruction[ParamGraphT]
  type GraphResult = GraphTransitionResult[ParamGraphT]
  type GraphResultEntry = GraphTransitionResultEntry[ParamGraphT]

  type ErrOrInt     = Either[Err, BigInt]
  type ErrOrIntpair = Either[Err, (BigInt, BigInt)]
  type ErrOrDCalc   = Either[Err, DAsInverseData]
  type ErrOrEPhi    = Either[Err, EPhiData]
  type ErrOrPQ      = Either[Err, PQData]
  type ErrOrEDN     = Either[Err, EDNData]

  case class EPhiData      (e: BigInt, phi: BigInt)            // e and phi together
  case class PQData        (p: BigInt, q: BigInt)              // p and q together
  case class EDNData       (e: BigInt, d: BigInt, n: BigInt)   // kind of the result type: modulus n, private (d) and public key (e)
  // case class EncryptedData (edn: EDNData, plaintext: BigInt, ciphertext: BigInt)   // kind of the result type: modulus n, private (d) and public key (e)
  case class DAsInverseData(e: BigInt, phi: BigInt, d: BigInt) // add more fields detailing the calc of the inverse


  sealed trait Operation {

    val word: Translatable
    def modPowHow: Operation.ModPowHow

    given ProductEdgeImpl[OpProductEdgeInputEDN.type] = paramsGraph.makeEitherProduct[BigInt, EDNData]
    given FuncEdgeImpl[EdgePlainValid.type] = {
      case Left(_) => Left(Err.Dependent)
      case Right(plain) => plain > 0 match
        case false => Left(Err.MustBePositive(plain))
        case true => Right(plain)
    }

    given edgeEncrImpl: FuncEdgeImpl[EdgeEncr.type] = {
      case Left(_) => Left(Err.Dependent)
      case Right((input: BigInt, edn: EDNData)) => Right(modPowHow.routine(edn, input))
    }

    given plainEDNValidImpl: FuncEdgeImpl[EdgePlainEDNValid.type] = {
      case Left(_) => Left(Err.Dependent)
      case Right((plain: BigInt, edn: EDNData)) => plain < edn.n match
        case true => Right((plain, edn))
        case false => Left(Err.PlaintextMustBeSmallerThanN(plain, edn))
    }

    case object OpInput             extends Node[ErrOrInt] with Operation.OpInputNode(this)
    case object OpInputValid        extends Node[ErrOrInt]
    case object OpInputEDNValid     extends Node[Either[Err, (BigInt, EDNData)]]
    case object OpProductInputEDN   extends Node[Either[Err, (BigInt, EDNData)]]
    case object OpOutput            extends Node[Either[Err, BigInt]]  with Operation.OpOutputNode(this)

    case object OpProductEdgeInputEDN extends ProductEdge(OpInputValid, paramsGraph.EDN, OpProductInputEDN)
    case object EdgePlainValid       extends FuncEdge(OpInput, OpInputValid)
    case object EdgePlainEDNValid    extends FuncEdge(OpProductInputEDN, OpInputEDNValid)
    case object EdgeEncr             extends FuncEdge(OpInputEDNValid, OpOutput)

    def edgetuple(fe: bci.metamodel.FuncEdgeProto) = (fe.from -> fe.to)
    val allNodes = List(OpInput, OpInputValid, OpInputEDNValid, OpProductInputEDN, OpOutput)
    val allEdges = List(
      paramsGraph.EDN -> OpProductInputEDN, 
      edgetuple(EdgePlainValid), 
      edgetuple(EdgePlainEDNValid), 
      edgetuple(EdgeEncr), 
      )
    def nodeBelongs(node: NodeAny) = allNodes.contains(node)
    def edgeBelongs(from: NodeAny, to: NodeAny) = allEdges.contains(from -> to)
      
  }

  object Operation:
    trait OpInputNode(val op: Operation) extends Node[ErrOrInt]  // static marker trait for operation nodes of interest
    trait OpOutputNode(val op: Operation) extends Node[ErrOrInt] // static marker trait for operation nodes of interest

    enum ModPowHow(val routine: (EDNData, BigInt) => BigInt):
      case EIsExponent extends ModPowHow((edn, plain) => plain.modPow(edn.e, edn.n))
      case DIsExponent extends ModPowHow((edn, plain) => plain.modPow(edn.d, edn.n))

    val allOps = List(Encrypt,Decrypt,Sign,Verify)

    case object Encrypt extends Operation{
      val word = Intl.of("Encrypt", "Verschlüsseln")
      def modPowHow = Operation.ModPowHow.EIsExponent
    }
    case object Decrypt extends Operation{
      val word = Intl.of("Decrypt", "Entschlüsseln")
      def modPowHow = Operation.ModPowHow.DIsExponent
    }
    case object Sign extends Operation   {
      val word = Intl.of("Sign"   , "Signieren")
      def modPowHow = Operation.ModPowHow.DIsExponent
    }
    case object Verify extends Operation {
      val word = Intl.of("Verify" , "Verifizieren")
      def modPowHow = Operation.ModPowHow.EIsExponent
    }

  object paramsGraph:
    object nodes:
      type Inputs    = P.type | Q.type   | E.type | Operation.OpInputNode
      type ToDisplay = N.type | Phi.type | D.type | Operation.OpOutputNode
      type All       = NodeAny // TODO
      val allInputs = List(P,Q,E,Operation.Encrypt.OpInput,Operation.Decrypt.OpInput,Operation.Sign.OpInput,Operation.Verify.OpInput)
      val allToDisplay= List(N,Phi,D,Operation.Encrypt.OpOutput,Operation.Decrypt.OpOutput,Operation.Sign.OpOutput,Operation.Verify.OpOutput)

      object attach:
        import rsaelgamal.attach.*
        trait RSAAttachObj:
        end RSAAttachObj
        trait RSAAttachments[NT <: All] extends NodeAttachments[NT]:
        end RSAAttachments
        trait RSAInputsAttachments[NT <: Inputs](override val node: NT) extends RSAAttachments[NT]:
        end RSAInputsAttachments
        trait RSAToDisplayAttachments[NT <: ToDisplay](override val node: NT) extends RSAAttachments[NT]:
        end RSAToDisplayAttachments
      end attach

    end nodes

    def makeEitherProduct[C1,C2](c1: Either[Err,C1], c2: Either[Err,C2]): Either[Err, (C1,C2)] =
      c1 match
        case Left(l1) => Left(Err.Dependent)
        case Right(r1) => c2 match
          case Left(l2) => Left(Err.Dependent)
          case Right(r2) => Right((r1,r2))


    //// Nodes
    case object P           extends Node[ErrOrInt]
    case object PValid      extends Node[ErrOrInt]
    case object Q           extends Node[ErrOrInt]
    case object QValid      extends Node[ErrOrInt]
    case object PQValid     extends Node[ErrOrPQ]
    case object Phi         extends Node[ErrOrInt]
    case object N           extends Node[ErrOrInt]
    case object E           extends Node[ErrOrInt]
    case object EValid      extends Node[ErrOrInt]
    case object EPhiValid   extends Node[ErrOrEPhi]
    case object D           extends Node[ErrOrDCalc]
    case object EDN         extends Node[ErrOrEDN]
    case object ProductPQ   extends Node[Either[Err, (BigInt, BigInt)]]
    case object ProductEPhi extends Node[Either[Err, (BigInt, BigInt)]]
    case object ProductDN   extends Node[Either[Err, (DAsInverseData, BigInt)]]


    //// Edges
    // Validation
    case object EdgePValid    extends FuncEdge(P          , PValid)
    case object EdgeQValid    extends FuncEdge(Q          , QValid)
    case object EdgePQValid   extends FuncEdge(ProductPQ  , PQValid)
    case object EdgeEValid    extends FuncEdge(E          , EValid)
    case object EdgeEPhiValid extends FuncEdge(ProductEPhi, EPhiValid)
    // Calculation
    case object EdgeNCalc     extends FuncEdge(PQValid    , N)
    case object EdgePhiCalc   extends FuncEdge(PQValid    , Phi)
    case object EdgeDCalc     extends FuncEdge(EPhiValid  , D)
    case object EdgeEDNTf     extends FuncEdge(ProductDN  , EDN)

    case object ProductEdgePQ   extends ProductEdge(PValid, QValid, ProductPQ)
    case object ProductEdgeEPhi extends ProductEdge(EValid, Phi, ProductEPhi)
    case object ProductEdgeDN   extends ProductEdge(D, N, ProductDN)

    import RSAImplementation.params.given

    given pqProductImpl:   ProductEdgeImpl[ProductEdgePQ.type]   = makeEitherProduct[BigInt,BigInt]
    given ePhiProductImpl: ProductEdgeImpl[ProductEdgeEPhi.type] = makeEitherProduct[BigInt,BigInt]
    given dnProductImpl:   ProductEdgeImpl[ProductEdgeDN.type]   = makeEitherProduct[DAsInverseData,BigInt]

    // TODO: rename "history" to "asResultList"
    case class AttentionPlanHistory( graph: ParamsGraph.type, initialState: NodeState, history: List[AttentionPlanResult] = List(), limit: Int = 10):
      def run(att_plan: AttentionPlan): AttentionPlanHistory =
        val transitionResult = att_plan.plan.applyForResult(
          this.history.headOption.map{_.result.newState}.getOrElse(initialState)
        )
        val attResult = AttentionPlanResult(att_plan, transitionResult)
        this.copy(history = attResult :: this.history.take(limit-1))
    object AttentionPlanHistory:
      def ofEmpty[CT](graph: ParamsGraph.type, limit: Int = 10) = 
        val initialInstructions: List[InstrT] = graph._initialTransitions
        val initialTransitionPlan: PlanT = graph.planMulti(initialInstructions)
        AttentionPlanHistory(graph, initialState = initialTransitionPlan.applyOnState(NodeState(Map())))

    case class AttentionPlanResult(att_plan: AttentionPlan, result: GraphResult)
    case class AttentionPlan(attention: Attention, unfilteredPlan: PlanT):
      def plan = attention.filterPlan(unfilteredPlan)

    // currentTransition is empty if the last transition is over TODO
    case class AttentionExec(plan_att: AttentionPlan, state: NodeState, currentTransition: InstrT)


    sealed abstract class Attention(target: List[NodeAny]):
      import Attention.*
      def requiredInputs: List[Node[ErrOrInt]]
      def filterPlan(plan: PlanT): PlanT

    object Attention:
      def edgeBelongsToParams(from: NodeAny, to: NodeAny): Boolean =
        val belongsToAnyOperation = Operation.allOps
          .map{_.edgeBelongs(from, to)}
          .exists(b => b == true)
        return ! belongsToAnyOperation

      abstract class ParamAttention(target: List[NodeAny]) extends Attention(target):
        def filterPlan(plan: PlanT) = 
          val transitions = plan.propagatedSeq.filter{
            case GraphInstruction.Propagate(arrow)  => Attention.edgeBelongsToParams(arrow.from, arrow.to)
            case GraphInstruction.Put(node, _) => ! Operation.allOps.map{_.nodeBelongs(node)}.exists(identity)
            case GraphInstruction.Refresh(node) => ! Operation.allOps.map{_.nodeBelongs(node)}.exists(identity)
          }
          return GraphTransitionPlan(plan.seedInstructions, transitions)

      case class OpAttn(op: Operation) extends Attention(List(PValid, QValid, N, Phi, EValid, D, EDN) ++ List(op.OpInputValid, op.OpOutput)):
        def requiredInputs = List(P,Q,E,op.OpInput)
        def filterPlan(plan: PlanT) =
          val transitions = plan.propagatedSeq.filter{
            case GraphInstruction.Propagate(arrow)  => op.edgeBelongs(arrow.from, arrow.to) || Attention.edgeBelongsToParams(arrow.from, arrow.to)
            case GraphInstruction.Put(node, _) => true
            case GraphInstruction.Refresh(node) => true
          }
          return GraphTransitionPlan(plan.seedInstructions, transitions)

      case object PAttn extends ParamAttention(List(PValid)):
        def requiredInputs = List(P)
      case object NPhiAttn extends ParamAttention(List(PValid, QValid, N, Phi)):
        def requiredInputs = List(P,Q)
      case object EDNAttn extends ParamAttention(List(PValid, QValid, N, Phi, EValid, D, EDN)):
        def requiredInputs = List(P,Q,E)
      case object EmptyAttention extends ParamAttention(List()):
        def requiredInputs = List()

    object ParamsGraph extends PGraph[ParamGraphT] {
      addFuncArrow    (EdgePValid)
      addFuncArrow    (EdgeQValid)

      addProductArrows(ProductEdgePQ)
      addFuncArrow    (EdgePQValid)
      addFuncArrow    (EdgeNCalc)
      addFuncArrow    (EdgePhiCalc)

      addFuncArrow    (EdgeEValid)
      addProductArrows(ProductEdgeEPhi)
      addFuncArrow    (EdgeEPhiValid)
      addFuncArrow    (EdgeDCalc)

      addProductArrows(ProductEdgeDN)
      addFuncArrow    (EdgeEDNTf)

      addProductArrows(Operation.Encrypt.OpProductEdgeInputEDN)
      addFuncArrow    (Operation.Encrypt.EdgePlainEDNValid)
      addFuncArrow    (Operation.Encrypt.EdgePlainValid)
      addFuncArrow    (Operation.Encrypt.EdgeEncr)

      addProductArrows(Operation.Decrypt.OpProductEdgeInputEDN)
      addFuncArrow    (Operation.Decrypt.EdgePlainEDNValid)
      addFuncArrow    (Operation.Decrypt.EdgePlainValid)
      addFuncArrow    (Operation.Decrypt.EdgeEncr)

      addProductArrows(Operation.Sign.OpProductEdgeInputEDN)
      addFuncArrow    (Operation.Sign.EdgePlainEDNValid)
      addFuncArrow    (Operation.Sign.EdgePlainValid)
      addFuncArrow    (Operation.Sign.EdgeEncr)

      addProductArrows(Operation.Verify.OpProductEdgeInputEDN)
      addFuncArrow    (Operation.Verify.EdgePlainEDNValid)
      addFuncArrow    (Operation.Verify.EdgePlainValid)
      addFuncArrow    (Operation.Verify.EdgeEncr)

      withInitialValue(paramsGraph.P, Left(Err.NotYetSet))
      withInitialValue(paramsGraph.Q, Left(Err.NotYetSet))
      withInitialValue(paramsGraph.E, Left(Err.NotYetSet))
      withInitialValue(Operation.Encrypt.OpInput, Left(Err.NotYetSet))
      withInitialValue(Operation.Decrypt.OpInput, Left(Err.NotYetSet))
      withInitialValue(Operation.Sign.OpInput, Left(Err.NotYetSet))
      withInitialValue(Operation.Verify.OpInput, Left(Err.NotYetSet))

      calculateInternals()
    }

    // TODO: good example for variance and abstraction/generalization
    def pqePresetGraphInstruction(p: BigInt, q: BigInt, e: BigInt): List[InstrT] =
      val pPut     = GraphInstruction.Put(paramsGraph.P, Right(p))
      val qPut     = GraphInstruction.Put(paramsGraph.Q, Right(q))
      val ePut     = GraphInstruction.Put(paramsGraph.E, Right(e))
      val all = List(pPut, qPut, ePut)
      val allNodes = all.map{_.node}
      val allNodesButThis = ParamsGraph.inputnodes.filterNot{allNodes.contains(_)}.toList
      val otherNodesPut   = allNodesButThis.map{ inode => GraphInstruction.Refresh(inode) }
      val result = pPut +: qPut +: ePut +: otherNodesPut
      result

  end paramsGraph

}






// Code shelf:

    // first try #genericise:
    // given errEitherProductImpl[ErrT, CIn1, CIn2,
    //   InT1 <: Node[Either[ErrT, CIn1]],
    //   InT2 <: Node[Either[ErrT, CIn2]],
    //   OutT <: Node[Either[ErrT, (CIn1,CIn2)]]
    // ]: ProductEdgeImpl[ProductEdge[
    //     Either[ErrT, CIn1], Either[ErrT, CIn2], Either[ErrT, (CIn1, CIn2)],
    //     Node[Either[ErrT, CIn1]], Node[Either[ErrT, CIn2]], Node[Either[ErrT, (CIn1,CIn2)]]
    //     ]] = { (val1,val2) => //Either[ErrT, (CIn1,CIn2)] =
    //   val1 match
    //     case Left(l1) => Left(l1)
    //     case Right(r1) => val2 match
    //       case Left(l2) => Left(l2)
    //       case Right(r2) => Right((r1,r2))
    // }
    //

