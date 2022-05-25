package bci.metamodel

import bci.util
import bci.util.Tabulator
import Graphs.GraphTransitionPlan
import Graphs.GraphTransitionResult

object Graphs:

  abstract class GraphArrowProto(val from: NodeAny, val to: NodeAny) {
    type InContent
    type OutContent
    type InNode <: Node[InContent]
    type OutNode <: Node[OutContent]
    def transition(g: NodeState): NodeState
  }

  abstract class GraphArrow[
    InC, OutC, 
    InT <: Node[InC], OutT <: Node[OutC]
    ](from: InT, to: OutT) extends GraphArrowProto(from,to):
    override type InContent=InC
    override type OutContent=OutC
    override type InNode = InT
    override type OutNode = OutT

  case class FunGraphArrow[
    InC, OutC, 
    InT <: Node[InC], 
    OutT <: Node[OutC]
    ](
      funcedge: FuncEdge[InC,OutC,InT,OutT],
      impl: InC=>OutC
    ) extends GraphArrow[InC, OutC, InT, OutT](funcedge.from, funcedge.to):
    override def toString() = s"${funcedge.from}->${funcedge.to}"
    override def transition(g: NodeState): NodeState = 
      val inNode = funcedge.from
      val currentInput = g.getContent(inNode)
      val calced = impl(currentInput)
      val outNode = funcedge.to
      val changedContent = g.content + (outNode -> calced)
      // val changedMarking = g.markings + (outNode -> (g.getMarking(outNode) + 1)) + (inNode -> 0)
      // NodeState(changedContent, changedMarking)
      NodeState(changedContent)

  case class ProductGraphArrow1[
    InC1, InC2, OutC,
    InT1 <: Node[InC1],
    InT2 <: Node[InC2],
    OutT <: Node[OutC]
    ]( pedge: ProductEdge[InC1,InC2,OutC,InT1,InT2,OutT], impl: (InC1,InC2)=>OutC) extends GraphArrow[InC1, OutC, InT1, OutT](pedge.from1, pedge.to):
    override def toString() = s"${pedge.from1}-p1>${pedge.to}"
    override def transition(g: NodeState): NodeState = 
      val inNode1: InT1 = pedge.from1
      val inNode2: InT2 = pedge.from2
      val outNode = pedge.to
      val currentInput1: InC1 = g.getContent(inNode1)
      val currentInput2: InC2 = g.getContent(inNode2)


      // TODO: work: the current inputs should be set actually! is the instruction plan wrong;
      // // in the next line, we take into account that the graph is initializing. In this case,
      // // product nodes are at one point uninitialized, but one part is set before the other.
      // // this has to remain a case that the user can never reach or observe!
      // val currentOutputOption: Option[outNode.Content] = g.content.get(outNode).asInstanceOf[Option[outNode.Content]]
      // val calced = currentOutputOption.map { fullyInitializedContent => 
      //   pnode.lens2.set(currentInput)(fullyInitializedContent)
      // }.getOrElse((null, currentInput))
      
      val output = impl(currentInput1, currentInput2)
      val changedContent = g.content + (outNode -> output)
      NodeState(changedContent)

  case class ProductGraphArrow2[
    InC1, InC2, OutC,
    InT1 <: Node[InC1],
    InT2 <: Node[InC2],
    OutT <: Node[OutC]
    ]( pedge: ProductEdge[InC1,InC2,OutC,InT1,InT2,OutT], impl: (InC1,InC2)=>OutC) extends GraphArrow[InC2, OutC, InT2, OutT](pedge.from2, pedge.to):
    override def toString() = s"${pedge.from2}-p2>${pedge.to}"
    override def transition(g: NodeState): NodeState = 
      val inNode1: InT1 = pedge.from1
      val inNode2: InT2 = pedge.from2
      val outNode = pedge.to
      val currentInput1: InC1 = g.getContent(inNode1)
      val currentInput2: InC2 = g.getContent(inNode2)
      val output = impl(currentInput1, currentInput2)
      val changedContent = g.content + (outNode -> output)
      NodeState(changedContent)

  case class NodeState(
    val content: Map[NodeAny,Any]
  ) {
    def getNodesRegistered = content.keySet
    def getContent[CT, NodeT <: Node[CT]](node: NodeT): CT = content(node).asInstanceOf[node.Content]
    def getContentAny(node: NodeAny): Any = content(node)
    def getContentAnyAs[CastT](node: NodeAny): CastT = content(node).asInstanceOf[CastT]
    def withValueFor[CT, NodeT <: Node[CT]](node: NodeT, value: CT): NodeState = this.copy(
        content = content + (node -> value)
      )
    // def getMarking(node: NodeAny): Int = markings(node)
  }



  object PGraph:

    val defaultPropagationSpec = GraphPropagationSpec.fullPropagation
  abstract class PGraph[CT]:
    protected var _arrows: Set[GraphArrowProto] = Set()
    protected var _nodesSeen: Set[Node[CT]] = Set()
    // TODO: guard this and other pseudobuilders against not finalizing and then calling methods
    // or... do it The Right Way ™
    def getNodesConnected: Set[Node[CT]] = _nodesSeen
    def arrows = _arrows


    def inputnodes: Set[Node[CT]] = getNodesConnected.filter{incomingArrows(_).size == 0}

    def calculateInternals() =
      this._nodesSeen = for {
        arrow <- _arrows
        nodes <- arrow match
          case ProductGraphArrow1(ProductEdge(from1,from2,to), _) => Set(from1,from2,to)
          case ProductGraphArrow2(ProductEdge(from1,from2,to), _) => Set(from1,from2,to)
          case FunGraphArrow(FuncEdge(from,to), _) => Set(from, to)
      } yield nodes.asInstanceOf[Node[CT]] // TODO: here be malevolent dragons. never expose stuff like this to the user is the plan
      // TODO: check that every node has an initial input

    // TODO: rename transitions into instructions everywhere
    var _initialTransitions: List[GraphInstruction[CT]] = List()
    protected def withInitialValue[NodeC <: CT, NodeT <: Node[NodeC]](node: NodeT, content: NodeC) =
      _initialTransitions = _initialTransitions :+ GraphInstruction.Put(node, content)

    protected def addProductArrows[
      CIn1 <: CT, CIn2 <: CT, COut <: CT, 
      InT1 <: Node[CIn1], 
      InT2 <: Node[CIn2], 
      OutT <: Node[COut], 
      EdgeT <: ProductEdge[CIn1,CIn2,COut,InT1,InT2,OutT]
      ](edge: EdgeT)
      (using impl: ProductEdgeImpl[EdgeT])=
        val implResolved: ProductEdgeImplementation[EdgeT,CIn1,CIn2,COut] = impl.asInstanceOf[ProductEdgeImplementation[EdgeT,CIn1,CIn2,COut]]
        val edge1 = ProductGraphArrow1(edge, implResolved.apply) // TODO: why is .apply necessary?
        val edge2 = ProductGraphArrow2(edge, implResolved.apply)
        this._arrows = _arrows ++ List(edge1,edge2)

    protected def addFuncArrow[
    I <: CT, O <: CT, 
    IN<:Node[I],ON<:Node[O],
    EdgeT <: FuncEdge[I,O,IN,ON]
    ](edge: EdgeT)(using impl: FuncEdgeImpl[EdgeT]) =
      val implResolved: FuncEdgeImplementation[EdgeT,I,O] = impl.asInstanceOf[FuncEdgeImplementation[EdgeT,I,O]]
      val graphEdge = FunGraphArrow(edge, implResolved.apply) // TODO: remove apply? some comp.failure...
      this._arrows = _arrows + graphEdge

    def outgoingArrows(node: NodeAny): Set[GraphArrowProto] = _arrows.filter{ _.from == node }
    def incomingArrows(node: NodeAny): Set[GraphArrowProto] = _arrows.filter{ _.to ==   node }

    def transitionFor(action: GraphInstruction[CT]): NodeState => NodeState = action match
      case GraphInstruction.Put(node, value) => s => s.withValueFor(node, value)
      case _ => throw new RuntimeException("can't happen")


    def findAllPathsFrom(node: NodeAny): Set[List[GraphArrowProto]] =
      // TODO: susceptible to loops!
      for {
        arrow <- outgoingArrows(node)
        nestedResult <-
          val nextPaths = findAllPathsFrom(arrow.to)
          nextPaths.isEmpty match
            case true => Set(List(arrow))
            case false => nextPaths.map(rPath => arrow +: rPath )
      } yield nestedResult

    def propagationSequenceFor(spec: GraphPropagationSpec, putNodes: Set[NodeAny]) =
      val pathSeq: Set[List[GraphArrowProto]] = for {
        node <- putNodes
        paths <- findAllPathsFrom(node)
      } yield paths
      val filteredPathSeq = pathSeq.map{ _.takeWhile(spec.follow_arrow) }
      sequentialize(filteredPathSeq)

    def sequentialize(paths: Set[List[GraphArrowProto]]): List[GraphArrowProto] =
      // println(s"paths: ${paths.mkString("- ", "\n- ", "")}")
      var frontiers = paths
      var result = List[GraphArrowProto]()
      while (! frontiers.forall(_.isEmpty)) {
        val candidates = frontiers.map{_.headOption}.flatten.toList.sortBy(_.toString).sortBy( candidate =>
          frontiers.map(_.headOption).flatten.contains(candidate)
        )
        // val candidateCountsInTails = for {
        val candidateTargetCountsInTails = for {
          candidate <- candidates
          frontierTails = frontiers.map { _ match
            case head :: tail => tail
            case Nil => Nil
          }
          count = frontierTails.map{ _.count {_.to == candidate.to} }.sum
        } yield (candidate,count)
        if (candidateTargetCountsInTails.forall {_._2 > 0})
          throw new RuntimeException(s"could not sequentialize $paths; candidate counts: $candidateTargetCountsInTails were all >0 for frontiers: $frontiers")
        val candidate = candidateTargetCountsInTails.toList.sortBy{_._2}.head._1
        // println(s"candidates with counts: ${candidateTargetCountsInTails.mkString("- ", "\n- ", "")}")
        // println(s"### frontiers: \n${frontiers.mkString("- ", "\n- ", "")}")
        // println(s"candidate chosen: $candidate")
        // println(frontiers.size)
        frontiers = frontiers.map {
          case head :: tail if head == candidate => tail
          case untouched@_ => untouched
        }
        result = result :+ candidate
      }
      result


    def propagation(spec: GraphPropagationSpec, transitions: List[GraphInstruction[CT]]): List[GraphInstruction[CT]] =
      val arrowSeq = propagationSequenceFor(spec, transitions.map{_.targetNodes}.flatten.toSet)
      transitions ++ arrowSeq.map(GraphInstruction.Propagate(_))

    def planMulti(instructions: List[GraphInstruction[CT]], spec: GraphPropagationSpec = PGraph.defaultPropagationSpec): GraphTransitionPlan[CT] =
      val propagatedSeq: List[GraphInstruction[CT]] = propagation(spec, instructions)
      GraphTransitionPlan(instructions, propagatedSeq)

  end PGraph

  // abstract class EitherGraph[ET, CT] extends PGraph[Either[ET,CT]]:
  // end EitherGraph

  sealed trait GraphInstruction[+CT] {
    def targetNodes: Set[NodeAny]
    def targetNode: NodeAny = targetNodes.head // simplified; TODO: or could it stay this atomic?
    val transition: NodeState => NodeState
  }

  object GraphInstruction:
    // TODO: see if we can get this covariant!!
    case class Refresh[CT](node: Node[CT]) extends GraphInstruction[CT] {
      override def targetNodes = Set(node)
      override val transition = state => state
    }
    case class Put[CT, +NodeT <: Node[CT]](node: NodeT, value: CT) extends GraphInstruction[CT] {
      override def targetNodes = Set(node)
      override val transition = state => state.withValueFor(node, value)
    }
    // TODO: Put vs Initialize?
    case class Propagate[CT](arrow: GraphArrowProto) extends GraphInstruction[CT] {
      override def targetNodes = Set(arrow.to)
      override val transition = arrow.transition
    }
  end GraphInstruction

  case class GraphPropagationSpec(
    follow_arrow: GraphArrowProto => Boolean = (_ => true)
  ) {
  }
  object GraphPropagationSpec {
    def noPropagation = GraphPropagationSpec(follow_arrow = (_=>false))
    def fullPropagation = GraphPropagationSpec(follow_arrow = (_=>true))
  }

  object GraphTransitionPlan:
    type PlanT[C] = GraphTransitionPlan[C]
    extension[C](self: PlanT[C])
      def mergeAll(plans: Iterable[PlanT[C]]) =
        val zero = GraphTransitionPlan[C](List(), List())
        plans.foldLeft(zero){(acc, el) => acc.merge(el)}
      def merge(plan2: PlanT[C]) =
        type Instr = GraphInstruction[C]
        val seeds = (self.seedInstructions ++ plan2.seedInstructions).distinct.foldLeft(List[Instr]()){ case (instrs, instr) => instr match 
          case GraphInstruction.Put(node, value) => instrs.filter{_ match // filter existing put instructions
            case GraphInstruction.Put(`node`, _) => false
            case _ => true
          } :+ instr
          case _ => instrs :+ instr
        }
        val propagations = (self.propagatedSeq ++ plan2.propagatedSeq).distinct
        GraphTransitionPlan(seeds, propagations)
  case class GraphTransitionPlan[CT](
      seedInstructions: List[GraphInstruction[CT]],
      propagatedSeq: List[GraphInstruction[CT]]
    ):

    def applyOnState(state: NodeState): NodeState =
      var result = state
      for {planElement <- propagatedSeq} {
        // println(s"- Plan element: $planElement")
        result = planElement.transition(result)
      }
      result

    def applyForResult(state: NodeState): GraphTransitionResult[CT] =
      var currentState = state
      var newState = state
      var entries: List[GraphTransitionResultEntry[CT]] = List()
      for {transition <- propagatedSeq}
        newState = transition.transition(currentState)
        entries = entries :+ GraphTransitionResultEntry(currentState, newState, transition)
        currentState = newState
      GraphTransitionResult(this, entries, state)

  case class GraphTransitionResultEntry[CT](oldState: NodeState, newState: NodeState, transition: GraphInstruction[CT])

  case class InputNodeErrPath[ErrT](inputNode: NodeAny, path: List[GraphArrowProto], errNode: NodeAny, err: ErrT):
    def isJustTheInputNode = path.size == 0
  object GraphTransitionResult:

    extension[LT,RT,CT <: Either[LT,RT]](gtr: GraphTransitionResult[CT]) // TODO: this extension is a bit rushed to get stuff done. it does not check the content types, casts, ...
      def entriesWithNondependentErrs =
        val ndErrEntries: List[GraphTransitionResultEntry[CT]] = gtr.entries.filter{ entry => entry.newState.getContentAny(entry.transition.targetNode) match
          case Left(rsaelgamal.RSAModel.Err.Dependent) => false
          case Left(x) if x.isInstanceOf[rsaelgamal.RSAModel.Err] => true
          case _ => false
        }
        ndErrEntries

      // TODO: is following breadcrums even necessary... I think not. just find the first errs...?
      // def nodePathsCausingErrs(nodes: List[Node[Either[LT,RT]]], gc: PGraph[Either[LT,RT]]) = // old signature before deciding that the exact type is in the way of getting stuff done
      def nodePathsCausingErrs(nodes: List[NodeAny], gc: PGraph[CT], isBreadcrumbErr: LT => Boolean = ((x:LT) => false)) =
        // returns a Map[NodeAny] (inputNodes->error)
        val mapped: List[List[Option[InputNodeErrPath[LT]]]] = nodes.map{ inputnode =>
          val innodePaths = gc.findAllPathsFrom(inputnode).toList
          innodePaths.map { path =>
            // cut paths after the first standalone err (may be empty path!)
            // println(s"path: ${path.mkString("- \n", "\n- ", "")}")
            val withoutBreadcrumbs = path.takeWhile{ pathArrow =>
              val arrowStartVal = gtr.newState.getContentAny(pathArrow.from).asInstanceOf[Either[LT,RT]]
              val arrowEndVal = gtr.newState.getContentAny(pathArrow.to).asInstanceOf[Either[LT,RT]]
              arrowEndVal match
                case Left(err) if isBreadcrumbErr(err) => false
                case _ => true
            }
            // println(s"withoutBreadcrumbs: ${withoutBreadcrumbs.mkString("- \n", "\n- ", "")}")
            withoutBreadcrumbs.size match
              case 0 => gtr.newState.getContentAnyAs[Either[LT,RT]](inputnode) match
                case Left(err) => Some(InputNodeErrPath(inputnode, List(), inputnode, err))
                case _         => None
              case _ => gtr.newState.getContentAnyAs[Either[LT,RT]](withoutBreadcrumbs.last.to) match
                case Left(err) => Some(InputNodeErrPath(inputnode, withoutBreadcrumbs, withoutBreadcrumbs.last.to, err))
                case _         => None
          }
        }
        mapped.flatten.flatten.distinct

      def errTable =
        val ndErrEntries = gtr.entriesWithNondependentErrs
        val transformed = ndErrEntries.map{ entry =>
          val t = entry.transition
          val inVal = entry.transition match
            case GraphInstruction.Put(node,value) => value
            case GraphInstruction.Propagate(arrow) => entry.newState.getContentAny(arrow.from)
            case GraphInstruction.Refresh(node) => entry.newState.getContentAny(node)
          val outVal = entry.newState.getContentAny(t.targetNode)
          List(t,inVal,outVal)
        }
        transformed

      def summaryErrs =
        val header = List("Arrow showing an Error", "input", "output")
        util.Tabulator.format(header +: gtr.errTable)

      def summaryInputPathErrs(graph: PGraph[CT]): String =
        summaryInputPathErrs(graph.inputnodes.toList, graph)

      def summaryInputPathErrs(forNodes: List[Node[CT]], connections: PGraph[CT]): String =
        val header = List("Input node", "path", "node with Err", "error")
        val npce = gtr.nodePathsCausingErrs(forNodes, connections)
        val npceTable = npce.map{ errpath =>
          List(errpath.inputNode, errpath.path.mkString(","), errpath.errNode, errpath.err)
        }.toList
        util.Tabulator.format(header +: npceTable)

      def summaryPreamble = s"for instructions: ${gtr.plan.seedInstructions.mkString("\n- ", "\n- ", "")}\n... the following propagations and results followed:"

      def printSummary =
        println(gtr.summaryPreamble)
        println(gtr.summaryTransitions)
        println(gtr.summaryErrs)

      def printSummaryWithInputErrs(graph: PGraph[CT]) =
        println(gtr.summaryPreamble)
        println(gtr.summaryTransitions)
        println(gtr.summaryErrs)
        println(gtr.summaryInputPathErrs(graph))

  case class GraphTransitionResult[CT](
    plan: GraphTransitionPlan[CT],
    entries: List[GraphTransitionResultEntry[CT]],
    oldState: NodeState
    ):
    def newState = entries.lastOption.map{_.newState}.getOrElse(oldState)

    def onNodeValueChanged[NodeCT, NodeT <: Node[NodeCT], ResultT](node: NodeT)(thenDo: (NodeCT, NodeCT) => ResultT): Option[ResultT] = // TODO: lets register for any node. with Node[CT], it fails in UI Model, probably because Node[C] is invariant in C
      // println(s"oNVC: for node $node; old content: ${oldState.content.toList.mkString("- ", "\n- ", "")}")
      val oldContent = oldState.getContentAny(node).asInstanceOf[NodeCT]
      val newContent = newState.getContentAny(node).asInstanceOf[NodeCT]
      oldContent == newContent match
        case true => None
        case false => Some(thenDo(oldContent, newContent))

    def transitionTable = 
      val table = for { entry <- entries } yield {
        val action = entry.transition
        val target = action.targetNode
        List(action.toString, target, newState.getContentAny(target))
      }
      table

    def summaryTransitions =
      val header = List("Action ", "Node", "New node value")
      Tabulator.format(header +: transitionTable)

  class GraphInstanceHandler[CT](val graph: PGraph[CT]) {
    export graph.*

    // calculate the inital state as function of the initial put instructions
    // since no observers can already be present, and put does not read from the NodeState, there can be no inconsistencies.
    private var _state = {
      val initialInstructions: List[GraphInstruction[CT]] = graph._initialTransitions
      val initialTransitionPlan: GraphTransitionPlan[CT] = graph.planMulti(initialInstructions)
      // println(initialTransitionPlan.seedInstructions.mkString("- ", "\n- ", ""))
      // println(initialTransitionPlan.propagatedSeq.mkString("- ", "\n- ", ""))
      initialTransitionPlan.applyOnState(NodeState(Map()))
    }
    def state = _state
    
    var graphTransitionObservers = List[GraphTransitionResult[CT] => Unit]()
    def addGraphTransitionObserver(o: GraphTransitionResult[CT] => Unit) =
      this.graphTransitionObservers = this.graphTransitionObservers :+ o

    def setStateInternally(graphTransitionResult: GraphTransitionResult[CT]) =
      println(s"DBG: 2: ${graphTransitionObservers.size}")
      this._state = graphTransitionResult.newState
      graphTransitionObservers.foreach{_.apply(graphTransitionResult)}

    def dryRun(plan: GraphTransitionPlan[CT]): GraphTransitionResult[CT] =
      plan.applyForResult(state)

    def run(plan: GraphTransitionPlan[CT]): GraphTransitionResult[CT] = 
      println("DBG: 1")
      val dry = dryRun(plan)
      this.setStateInternally(dry)
      dry

  }

  object GraphHistory:
    def ofEmpty[CT](graph: PGraph[CT], limit: Int = 10) = 
      val initialInstructions: List[GraphInstruction[CT]] = graph._initialTransitions
      val initialTransitionPlan: GraphTransitionPlan[CT] = graph.planMulti(initialInstructions)
      GraphHistory(graph, initialState = initialTransitionPlan.applyOnState(NodeState(Map())))
  case class GraphHistory[CT](
    graph: PGraph[CT],
    initialState: NodeState,
    history: List[GraphTransitionResult[CT]] = List(),
    limit: Int = 10
  ):
    def run(plan: GraphTransitionPlan[CT]): GraphHistory[CT] =
      val transitionResult = plan.applyForResult(
        this.history.headOption.map{_.newState}.getOrElse(initialState)
      )
      this.copy(history = transitionResult :: this.history.take(limit-1))




end Graphs
