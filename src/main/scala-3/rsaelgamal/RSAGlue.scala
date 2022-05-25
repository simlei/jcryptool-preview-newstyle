package rsaelgamal

import zio._
import zio.stream._

import bci.*
import bci.metamodel.*
import bci.metamodel.Graphs.*
import bci.swt.platform.*
import bci.swt.{*, given}
import bci.view.Parsing.*

import rsaelgamal.RSAModel.*
import rsaelgamal.RSAModel.paramsGraph.*
import rsaelgamal.behavior.*

import simlei.util.*



object RSAGlueSpec:
  // extension(self: RSAGlueSpec)
  //   def makerZ(ctrl: RSAUIMainController) =
  //     for {
  //       ponsInstance <- self.pons.makerZ(ctrl) // NOTE: hard requirement since attention needs to know state. 
  //     } yield Instance(self, ctrl, ponsInstance)

  // class Instance(val spec: RSAGlueSpec, val ctrl: RSAUIMainController, val ponsInstance: spec.PonsInstanceT):
  //   type Spec = spec.type

  //   // def ponsPushingSink = ZSink.foreach{ ctrl.queue.offer(_) }
  //   val acquisitionZ          = ponsInstance.runnerZ               // makes the gating, attention, etc. run, so that they are available through the sync primitives (queue / stream)
  //   val toControllerStreamZ   = ponsInstance.planStream            // represents the available data
  //   val toControllerPushZ     = toControllerStreamZ.run(ctrl.sink) // takes the data made available through the infrastructure and enqueues them to the controller

  //   val fromControllerPullZs = ???
  //   // val fromControllerPullZs = spec.outputs.gluesUIFacing.map{ glue => 
  //   //     glue.progUpdateUI( ctrl.graphResultsStream.map{_.result} ) // TODO:now: do not yet reduce to non-attention plan; before, refactor UIFacingGlue etc
  //   //   }

  //   val runner_acquisition        = acquisitionZ                             .forkSpecced(this -> "infrastructure")
  //   val runner_toControllerPush   = toControllerPushZ                        .forkSpecced(this -> "toControllerPushZ")
  //   val runner_fromControllerPull = ???
  //   // val runner_fromControllerPull = ZIO.collectAllPar(fromControllerPullZs)  .forkSpecced(this -> "fromControllerPullZs")
end RSAGlueSpec

class RSAGlueSpec(val uiface: RSAUIMain.Interface):
  // general parsing and string representation
  import rsaelgamal.RSAParsing.given // TODO: necessary?
  import rsaelgamal.RSAStrings.*     // TODO: necessary?
  // pull in the members of the interface of the UI
  // import the generic behaviorial constructs
  import behavior.*
  import behavior.Streamgate.*

  type PonsT = pons.type
  // type PonsInstanceT = pons.type#PonsInstance
  type GlueParticlesT = glueParticles.type

  // create a "glue particle home improvement shop" specialized for this graph-ui glue application
  val glueParticles = new GraphGlues[Err, ParamsGraph.type](ParamsGraph)
  import glueParticles.*


  // asking for glue constructs for the type of the current graph // TODO: canonicalize and document NodeE/D/V data types
  type NodeE = glueParticles.NodeE
  type NodeV = glueParticles.ThisGraphV
  type PlanGateSpec = Streamgate.Spec[PlanT,AttentionPlan]
  type PlanGate = Streamgate.Instance[PlanT,PlanT] // TODO: rename ->PlanstreamGate, etc


  object pons:
    // import rsaelgamal.ui.canon.{PresetSignal}

    object parsing2:
      // import rsaelgamal.ui.canon.*
      // val presetSmall = ??? // PresetInput(uiface.???,  apriori.bigParams)
      // val presetBig = ???
      // sealed trait Parsed[T] extends Input[T]  // well-formed "data" that can go straight into some planner, possibly gate before
      // case class PresetInput( preset: apriori.Params, signal: ui.canon.PresetSignal ) extends Parsed[apriori.Params]:
      //   override def stream = ???
      // case class SingleParsedGatedInput[T]( uiAttached: uiface.attach.InputsAttachment ) extends Parsed[Either[ParseErr, T]]:
      //   val parser: TextParserDisplayer.Instance[T] = ???
      //   override def stream = ??? // parser.

      

      // ParamsGraph.planMulti(paramsGraph.pqePresetGraphInstruction(p,q,e))
      // val presetSmall = PresetInput(uiface.???,  apriori.smallParams) // TODO :rename
      // val presetBig   = PresetInput(uiface.???  ,  apriori.bigParams)
          // TODO: refactor: attach glues and uncomment, reinstate
          // val gluesUIFacing = List( pGlue, qGlue, eGlue, nGlue, phiGlue, dGlue ) ++ operationGlues.values.map{_.displayGlue}
          // val gluesControllerFacing = List( pGlue, qGlue, eGlue ) ++ operationGlues.values.map{_.parsingGlue} :+ presetSmall.glue :+ presetBig.glue
    end parsing2


    // // an object of this type is the main thing the backbone produces
    // object gating2:
    //   import rsaelgamal.ui.canon
    //   sealed trait Gated[T] extends Input[T]: // data that goes into a planner
    //     def makeGateSpec: StreamgateSpec[PlanT,PlanT] = ???
    //     def streamWithGatesignals: StreamZ[T | GateSignal] = self match
    //   class GatedForwarding: // gated, but gate is always open
        

    //   // ----- "attach further meaning" extension
    //   // decide about the initial gating (open/close) of any input streams, essentially creating the StreamgateSpec
    //   extension[T](self: parsing.Parsed)
    //     // decides about parameters of the gate (initial GateState(Up/Down), folding of streamed data)
    //     // def makeGateSpec: StreamgateSpec[PlanT,PlanT] =
    //     //   val initialGateState = self match
    //     //     case in: parsing.PresetInput => GateState.Up
    //     //     case in: parsing.SingleParsedGatedInput[t] => GateState.Down
    //     //   StreamgateSpec.justBufferHead[PlanT](initialGateState)

    //     // // decides what exact data, on the basis of the input (self), is fed to the gate (GateSignal + data)
    //     // def streamWithGatesignals: StreamZ[T | GateSignal] = self match
    //     //   // presets just feed the input stream (already gated, sorta, since each element originates through button clicks)
    //     //   case in: inputs.PresetInput => self.stream
    //     //   // for TextfieldInputs, for now: if the gate's button fires, open, and then immediately close the gate.
    //     //   case ti @ inputs.SingleParsedInput(glue) =>
    //     //     val btn = uiface.getParseUI(glue.node.asInstanceOf[uiface.ParseableNodes]).textentryGroup.btn // NOTE:later: investigate casting necessity.
    //     //     val btnstream = getSWTStream(btn, SWTEvt.Selection).flatMap{_ => ZStream(GateSignal.Open, GateSignal.Close)} // just lift the gate for the shortest time
    //     //     self.stream.merge(btnstream)
    //     //   case _ => sys.error("pattern matching can't somehow process inputs.Input[T]") // TODO: why necessary?
    //   end extension
    // end gating2



    // case class PonsInstance(attentionCtrl: cerebellum.Instance, inputsGated: backbone.Instance):
    //   val runnerZ = attentionCtrl.runnerZ <&> inputsGated.runnerZ // TODO: later: investigate failure effect on each other

    //   // NOTE: the stream can be obtained before the runner is run! (it won't have any elements though until it is run...)
    //   def planStream = inputsGated.mergedGateOutputStream.mapZIO{ plan => 
    //     attentionCtrl.mapPlan(plan)
    //   }

    // end PonsInstance

    // // this sets up the whole infrastructure, hierarchically calling cerebellum, backbone, inputs, periphery, ... to set up their synchronization primitives.
    // def makerZ(ctrl: RSAUIMainController) = for {
    //     inputsGated <- backbone.makerZ
    //     attentionCtrl <- cerebellum.makerZ(inputsGated, ctrl)
    //   } yield PonsInstance(attentionCtrl, inputsGated) // ... and again, a "higher" API, composed of the lower APIs, is constructed.

  end pons


    // object cerebellum: // the attention (precision) center
    //   sealed trait AttentionCtrl(tf: AttentionCtrlState => UIO[AttentionCtrlState])
    //   case class AttentionHistory(asList: List[Attention]):
    //     def current = asList.head
    //   case class AttentionCtrlState(
    //     history: AttentionHistory, 
    //     projectedNext: List[Attention],
    //     possibleNext: List[Attention],
    //     )
    //   case class Instance(
    //     ctrl         : RSAUIMainController,
    //     state      : SubscriptionRef[AttentionCtrlState]
    //   ):
    //     // seems like the attention instance, for now, will remain "passive" (act on behalf of pons)
    //     def runnerZ: IOZ[Any] = for {
    //       _ <- ZIO.infinity
    //     } yield ()
    //     def calcProjectedNextAttentions(currentAttention: Attention): List[Attention] =
    //       currentAttention match
    //         case Attention.PAttn      => List(Attention.NPhiAttn)
    //         case Attention.NPhiAttn   => List(Attention.EDNAttn)
    //         case Attention.EDNAttn    => Operation.allOps.map{Attention.OpAttn(_)}
    //         case Attention.OpAttn(op) => Operation.allOps.splitAt(Operation.allOps.indexOf(op))._2.drop(1).map(Attention.OpAttn(_)) // take the next op until all are performed
    //     def calcPossibleNextAttentions(currentAttention: Attention): List[Attention] =
    //       val projected = calcProjectedNextAttentions(currentAttention)
    //       projected.filter{proj =>
    //         ??? // proj.requiredInputs
    //       }
    //     def transaction(plan: PlanT): UIO[AttentionPlan] = for {
    //       currentAtt <- state.ref.map{_.history.asList.head}.get
    //       updatedAtt <- currentAtt
    //     } yield AttentionPlan(updatedAtt, plan)
    //   end Instance
    //   val initialAttention = Attention.EmptyAttention
    //   val initialProjectedAttentions = List(Attention.PAttn)
    //   val initialPossibleAttentions = List(Attention.PAttn)
    //   def makerZ(inputsGated: backbone.Instance, ctrl: RSAUIMainController): UIO[Instance] = // TODO: narrow arguments after solidification (ctrl)
    //     for {
    //       attention <- SubscriptionRef.make[Attention](initialAttention)
    //       history <- SubscriptionRef.make[AttentionHistory](AttentionHistory(List(initialAttention)))
    //       nextProjected    <- SubscriptionRef.make[List[Attention]](initialProjectedAttentions)
    //       nextPossible     <- SubscriptionRef.make[List[Attention]](initialPossibleAttentions)
    //     } yield Instance(ctrl, attention, history, nextProjected, nextPossible)
    // end cerebellum



    // case class OperationGlue[OpT <: Operation](operation: Operation)(using parser: Parseable[BigInt], shower: Show[BigInt]) {
    //   val in = operation.OpInput
    //   val out = operation.OpOutput
    //   val parsingGlue   = glueParticles.NodeParsingGlue(in, getParseUI(in).textChangeStream, getParseUI(in).textChangeSink) 
    //   val displayGlue   = glueParticles.NodeDisplayingGlue(out, getDisplayUI(out).textChangeSink) 
    // }
    // val operationGlues = Map[Operation, OperationGlue[Operation]](
    //   Operation.Encrypt -> OperationGlue(Operation.Encrypt),
    //   Operation.Decrypt -> OperationGlue(Operation.Decrypt),
    //   Operation.Sign    -> OperationGlue(Operation.Sign)   ,
    //   Operation.Verify  -> OperationGlue(Operation.Verify) ,
    //   )
    // // bundling up the glue parts that go from controller to UI
    // // val gluesUIFacing: List[glueParticles.type#UIFacingGlue] = 
    // // TODO: move?
    // // this is the "chaos" we want to manage
    // val parsedStreams: List[NodeParsingGlue[?,?]] = List(pGlue, qGlue, eGlue)
    // val presetStreams: List[Preset]               = List(presetSmall, presetBig)
    // val allInputs = 
    //   parsedStreams.map{TextfieldInput(_)} ++
    //   presetStreams.map(PresetInput(_))




      // case class Instance( inputs_and_gates: Map[inputs.Input[PlanT], Streamgate[PlanT, PlanT]] ):

      //   // internal representation: a pair of ( input x gate ), not yet running though. Specifies how it's going to be run.
      //   case class InputWithGate(input: inputs.Input[PlanT], gate: Streamgate[PlanT,PlanT]):

      //     // representation of the actual in-parallel running fibers, with access to the surrounding context.
      //     case class Forked(fiberIntoGate: FiberZ[Unit], fiberGateMgmt: FiberZ[Unit]):
      //       def inputsContext = Instance.this   // provide easy access to the neighborhood context data
      //       def inputContext = InputWithGate.this // provide easy access to the context data
      //     end Forked

      //     // part of the whole: how to run data into the gate...
      //     def runIntoGateZ = input
      //                   .streamWithGatesignals // use the extension method from above
      //                   .tapSink(printSink(s"DBG:intoGates"))
      //                   .foreach { drop => gate.input.publish(drop) }

      //     // the whole (for a single input): how it's run (input and gate) and representation by `Forked`
      //     def runnerZ = for {
      //       fiberGateMgmt <- gate.processor.fork
      //       fiberIntoGate <- runIntoGateZ.fork
      //     } yield Forked(fiberIntoGate, fiberGateMgmt)

      //   end InputWithGate

      //   // Canonical Repr.: transform the Map[...] to business logic: the programmatically useful representation of this whole backbone.Instance affair.
      //   val inputs_with_gates = inputs_and_gates.toList.map{case (input, gate) => InputWithGate(input, gate)}

      //   // the merged stream (and the individual ones, too!) are available even when the inputs and gates are not yet run, due to the already-present synchronization primitives (queues) of the gates
      //   // individual gated streams are accessible via inputs_with_gates.get(<idx>).gate.outputStream
      //   def mergedGateOutputStream =
      //     val gateOutputStreams = for { inp <- inputs_with_gates } yield inp.gate.outputStream
      //     ZStream.mergeAllUnbounded(outputBuffer = 10000)(gateOutputStreams*)

      //   def runnerZ: IOZ[List[InputWithGate#Forked]] =
      //     val fibersInputsGatedZ = for { inp <- inputs_with_gates } yield inp.runnerZ
      //     ZIO.collectAllPar(fibersInputsGatedZ)

      // end Instance

    // def makerZ: IOZ[backbone.Instance] = 
      // val makegates: IOZ[List[Streamgate[PlanT, PlanT]]] = ZIO.collectAll(
      //   inputs.allInputs.map{ _.makeGateSpec.makeGate } 
      // )
      // return makegates
      //           .map{gates => backbone.Instance(inputs.allInputs.zip(gates).toMap) }



    // trait Has[InStream[T]]:
    //   def node: NodeT
    // trait In[RawT]:
    //   def rawIn: StreamZ[RawT]
    // trait Out[RawT]:
    //   def rawOut: SinkZ[RawT]
    // trait InStreamAny:
    //   val stream: StreamZ[Any]
    // trait NodeStream[RawT](val stream: StreamZ[RawT]) extends InStreamAny
    // object inouts:
    // end inouts
    // object outputs:
    // end outputs
    // object inputs:
    // end inputs


    // val attentionSink = ZSink.foreach[Any,Nothing,Attention]{ attention =>
    //   ZIO.succeed{
    //     dialogbar.mixedInAsCtrl.getDisplay.asyncExec{() =>
    //       val currentStepInfo = RSAStrings.stepInfoFor(attention)
    //       dialogbar.instructionLabel.withText(currentStepInfo.instruction)
    //       dialogStepFor(attention).visible.foreach{ case (c,v) => c.glVisibleAndLayout(v)(using ui.given_SWTLayoutRoot) }
    //       // dialogStepMap(currentStep).focused.setFocus() // TODO: focuses on graph changes i.e. surprisingly
    //   }}
    // }
    // val nextProjectedAttentionsSink = ZSink.foreach[Any,Nothing,List[Attention]]{ nextProjectedAttentions =>
    //   ZIO.succeed{
    //     dialogbar.mixedInAsCtrl.getDisplay.asyncExec{() =>
    //       val possibleNextStep = nextProjectedAttentions.headOption
    //       val nextStepInfo = possibleNextStep.map(s => 
    //           val alsoShowing = nextProjectedAttentions.drop(1)
    //             .map{also => RSAStrings.stepInfoFor(also)}
    //           val stump = s"The next step is about the ${RSAStrings.stepInfoFor(s).thisStepIsAboutThe}."
    //           val withAlso = stump + (alsoShowing match
    //             case Nil => ""
    //             case _ => "\nAlso possible: " + alsoShowing.mkString(", ", "", "."))
    //           withAlso
    //         )
    //       dialogbar.navigationBar.middle.withText(nextStepInfo.getOrElse(""))
    //   }}
    // }
    // var nextAttention: Option[Attention] = None // TODO: later: currently duplicated info
    // val nextPossibleAttentionsSink = ZSink.foreach[Any,Nothing,List[Attention]] { nextPossibleAttentions =>
    //   ZIO.succeed{
    //     dialogbar.mixedInAsCtrl.getDisplay.asyncExec{() =>
    //       nextAttention = nextPossibleAttentions.headOption
    //       dialogbar.btnNext.setEnabled(nextAttention.isDefined)
    //   }}
    // }
    // val attentionHistorySink = ZSink.foreach[Any,Nothing,List[Attention]] { history =>
    //   ZIO.succeed{
    //     dialogbar.mixedInAsCtrl.getDisplay.asyncExec{() =>
    //       val prevAttention = history.drop(1).headOption
    //       dialogbar.btnBack.setEnabled(prevAttention.isDefined)
    //   }}
    // }






    // (legacy-1) early draft of participant API in backbone
    // register here to participate :)
    // what is this signature supposed to mean? well: given the api, construct a ZIO subprogram (that can participate in the synchronization primitives of the API)
    // case class InputAPI(map: Map[Input[PlanT], PlanGate])
    // val inputParticipantMakers: collection.mutable.Seq[InputAPI => IOZ[Any]] = collection.mutable.Seq.empty
      // participantAPI         = InputAPI(inputs_and_gates.toMap)
      // participants           =  for { makeParticipant <- inputParticipantMakers }
      //                             yield makeParticipant(participantAPI)


  // object RSAUIAttentionBasedPlanner {
  //   import RSAUIFloodAnalogy as Upstream
  //   type H2O = AttentionPlan

  //   def makeUpstreamSinkFor(downstreamSink: SinkZ[H2O]): SinkZ[Upstream.H2O] =
  //     downstreamSink.contramap{ (upstreamContent:Upstream.H2O) =>
  //       AttentionPlan(???, ???)
  //     }
      
  //   def floodgateExecution(downstreamSink: SinkZ[H2O]) = for {
  //     upstreamExec <- Upstream.floodgateExecution(makeUpstreamSinkFor(downstreamSink))
  //   } yield ()
  // }



  // val pHandlerFromUI = pHandler.wireNodeValueStream()


  // val qHandler = parsingHandler(qChooser)
  // val eHandler = parsingHandler(eChooser)

  // val dHandler = InputNodeToTextDisplay(
  //   dDisplay.text,
  //   ParamsGraph, 
  //   paramsGraph.D,
  // )
  // val nHandler = InputNodeToTextDisplay(
  //   nDisplay.text,
  //   ParamsGraph, 
  //   paramsGraph.N,
  // )

  // val phiHandler = InputNodeToTextDisplay(
  //   phiDisplay.text,
  //   ParamsGraph, 
  //   paramsGraph.Phi,
  // )

  // val encryptInputHandler = InputNodeToTextHandler(
  //   encryptInputChooser.textenter.text,
  //   ParamsGraph,
  //   paramsGraph.Plaintext,
  //   errorDisplayLabel = encryptInputChooser.textenter.errorDisplayLabel,
  //   infoRequestSource = encryptInputChooser.btn,
  //   userInfoSink = paramUserInfoDocumentHandler(_, encryptInputChooser.btn.getShell)
  // )

  // val encryptOutputHandler = InputNodeToTextDisplay(
  //   encryptOutputDisplay.text,
  //   ParamsGraph,
  //   paramsGraph.EncrPlaintext,
  // )

  // object RSADialog:
  //   enum Action:
  //     case Go(step: steps.params.Step)
  //     case GoPrev
  //     case GoNext

  //   // var stepHistory = steps.params.StepHistory(List(steps.params.Init))
    
  //   case class DialogState( visible: Map[Control, Boolean], focused: Control)

  //   val allVisiControls = List(
  //     pChooser  .getRootControl,
  //     qChooser  .getRootControl,
  //     nDisplay  .getRootControl,
  //     phiDisplay.getRootControl,
  //     eChooser  .getRootControl,
  //     dDisplay  .getRootControl,
  //     compOperation.getRootControl,
  //   )

  //   val stateInit = DialogState(
  //     visible = allVisiControls.map{_ -> false}.toMap,
  //     focused = dialogbar.navigationBar.btnNext
  //     )
  //   val stateEnterP = DialogState(
  //     visible = stateInit.visible ++ List(pChooser.getRootControl -> true),
  //     focused = pHandler.text
  //     )
  //   val stateEnterQ = DialogState(
  //     visible = stateEnterP.visible ++ List(qChooser.getRootControl -> true),
  //     focused = qHandler.text
  //     )
  //   val stateEnterE = DialogState(
  //     visible = stateEnterQ.visible ++ List(
  //         nDisplay.getRootControl   -> true,
  //         phiDisplay.getRootControl -> true,
  //         eChooser.getRootControl   -> true,
  //       ),
  //     focused = eHandler.text
  //     )
  //   val statePerformOperation = DialogState(
  //     visible = stateEnterE.visible ++ List(
  //         dDisplay.getRootControl -> true,
  //         compOperation.getRootControl -> true,
  //       ),
  //     focused = compOperation
  //     )

  //   var dialogStepMap = Map[steps.params.Step, DialogState](
  //     steps.params.Init     -> stateInit,
  //     steps.params.EnterP   -> stateEnterP,
  //     steps.params.EnterQ   -> stateEnterQ,
  //     steps.params.EnterE   -> stateEnterE,
  //     steps.params.PerformOperation -> statePerformOperation,
  //   )

  //   def refreshDialog(currentNodeState: NodeState, stepHistory: steps.params.StepHistory) =
  //     val currentStep= stepHistory.current
  //     val nextStep = stepHistory.next
  //     val currentStepNeeds = currentStep.rightNodes
  //     val rights = currentStepNeeds.map { nodeNeed =>
  //       nodeNeed -> currentNodeState.getContentAnyAs[Either[Err,Any]](nodeNeed).isRight
  //     }
  //     val conditionsMet = rights.foldLeft(true)(_ && _._2)
  //     val currentStepInfo = stepInfoFor(currentStep)
  //     val nextStepInfo = nextStep.map(s => s"The next step is about the ${stepInfoFor(s).thisStepIsAboutThe}.")
  //     dialogbar.instructionLabel.withText(currentStepInfo.instruction)
  //     dialogbar.navigationBar.middle.withText(nextStepInfo.getOrElse(""))
  //     dialogStepMap(currentStep).visible.foreach{ case (c,v) => c.glVisibleAndLayout(v) }
  //     // dialogStepMap(currentStep).focused.setFocus() // TODO: focuses on graph changes i.e. surprisingly
  //     dialogbar.btnNext.setEnabled(conditionsMet && stepHistory.next.isDefined)
  //     dialogbar.btnBack.setEnabled(stepHistory.prev.isDefined)
  //     // TODO: rename prev/back, stepHistory into sth., clearer stepHistory.next/prev (nextOfCurrent...)

  //   var stepHistoryRef = steps.params.StepHistory(List(steps.params.Init)) // TODO: dirty hack
  //   var currentNodeStateRef = { // TODO: dirty hack
  //     GraphInstanceHandler(ParamsGraph).state // TODO: dirty hack
  //   }
  //   val dialogForthStream = dialogbar.btnNext.getEventStream(SWTEvt.Selection).as(Action.GoNext)
  //   val dialogBackStream = dialogbar.btnBack.getEventStream(SWTEvt.Selection).as(Action.GoPrev)
  //   val dialogActionStream = (
  //     // ZStream(RSADialog.Action.Go(steps.params.Init)) ++ 
  //     dialogBackStream.merge(dialogForthStream)
  //   )
  //   val dialogGraphSink = ZSink.foreach[Any,Nothing,GraphTransitionResult[ParamGraphT]]{ transition =>
  //     ZIO.succeed{
  //       dialogbar.getRootControl.getDisplay.asyncExec{() =>
  //       currentNodeStateRef = transition.newState
  //       refreshDialog(currentNodeStateRef, stepHistoryRef)
  //     }}
  //   }
  //   val stepChangedSink = ZSink.foreach[Any,Nothing,steps.params.StepHistory] {newHistory =>
  //     ZIO.succeed{
  //       dialogbar.getRootControl.getDisplay.asyncExec{() =>
  //       stepHistoryRef = newHistory
  //       refreshDialog(currentNodeStateRef, stepHistoryRef)
  //     }}
  //   }

  //   def printSink(prefix: Any) = ZSink.foreach{ arg => Console.printLine(s"${prefix.toString}: $arg") }
  //   val stepMorphStream = dialogActionStream.scan(stepHistoryRef)((acc, action) => 
  //       val newState = action match
  //         case Action.GoNext => acc.next.get // TODO: only the UI makes the user not crash the program here
  //         case Action.GoPrev => acc.prev.get
  //         case Action.Go(step) => step
  //       acc.copy(history = newState :: acc.history)
  //     )

  // this is the communication with the controller: input and output





















  // val backchannel = graphChangeStream.broadcast(3, 5).use{streamchunk =>
  //   val broadcasts = streamchunk.toList.zip(List(pHandler,qHandler,eHandler)).map{(stream,handle) =>
  //     println(s"$stream$handle")
  //     stream.tapSink(printSink("test")).run(handle.graphChangeSink)
  //   }
  //   ZIO.collectAllPar(broadcasts)
  // }




   // val oneShot = zio.internal.OneShot.make[Unit]
   // Runtime.default.unsafeRunAsync(prog2 *> ZIO.succeed(oneShot.set(()))) // doesn't work
   // oneShot.get()


  // // works
  // val connection = pHandler.parsed.run(printSink("test for parsed"))
  // println("running tapped")
  // Runtime.default.unsafeRunAsync(connection)
  // println("finished running tapped")

  // val connection = stream.run(printSink("test for splitted"))
  // println("running tapped")
  // Runtime.default.unsafeRunAsync(connection)
  // println("finished running tapped")

  // // doesnt work...
  // val splitStream = pHandler.splitted.use{(stream1, stream2) => 
  //   // val s1Tapped = stream1.tap{s1v => printLine(s1v)}
  //   // val s2Tapped = stream2.tap{s2v => printLine(s2v)}
  //   val sunk1 = stream1.run(printSink("111"))
  //   val sunk2 = stream2.run(printSink("222"))
  //   sunk1
  //   // ZIO.collectAllPar(Seq(sunk1, sunk2))
  // }
  // println("running zio...")
  // Runtime.default.unsafeRunAsync(splitStream)

  // val splitStream = tapped.useNow
  // val tapped = for {
  //   (s1, s2) <- splitStream
  //   s1Tapped = s1.tap{s1v => printLine(s1v)}
  //   s2Tapped = s2.tap{s2v => printLine(s2v)}
  //   sunk1 <- s1Tapped.run(printSink("111"))
  //   sunk2 <- s2Tapped.run(printSink("222"))
  // } yield (sunk1, sunk2)
  // println("running tapped")
  // Runtime.default.unsafeRunAsync(tapped)
  // println("finished running tapped")











// Documentation
// -------------
// (1)
    // testing how to build a "flood gate" that fires graph transitions in a queue only when the button is clicked
    // in particular, it should discard "stale" transition plans at the head of the queue
    //   -- more specifically, this would be a fold operation on all elements in the queue
    //   -- flood = floodGate.takeAll --> Collection (chunk) of transition plans. inspect, and yield a single transition plan :v:
    // even better: one can leave elements of this flood through selectively, and put the rest back into the queue!

    // Paradigm terms: Stream, Flood, Flood gate
    // - Wikipedia: "A flood is an overflow of water that submerges land that is usually dry"
    //              "Cities and towns built on waterbodies or with infrastructure designed around 
    //               historical rainfall patterns are increasingly susceptible to flooding."
    // - we already know streams. Multiple streams (a list of streams) are regarded a flood. 
    //   Programming languages differ somewhat from natural language, in the following: 
    //     >> if a List of streams are a flood, even a List of one stream _must_ pass as a "flood".
    //        But there is a useful term "degenerated list/graph/etc" -- "degenerated flood", 
    //        which in this ex. is a list of a single stream. It's not too dry, and it's not a real flood!
    //   We are goldilock and we want _that_. So:
    //   - Degenerate the flood (filter, debounce, buffer, etc...)
    //   - Unpack (merge, channel) the flood into a nice single stream that doesn't overflow our main logic
    //   - ==> the (logic) land stays saturated with information (H2O) while not being submerged.

    // To draw the connection to UI: by text fields, buttons, etc. we are bombarded by (_potential_...) _ideas_ on 
    // how the RSA logic graph could progress next — this is the flood.
    //
    // - one way to handle it is to discard information "upstream" already. This is not ideal, because we can't abstract over that.
    // - another way to go is let them all through directly, this is yielding to the flood which we won't do.
    // - another is: to build a control structure! :)
    // we call the stream of "graph ideas / plans" a "Flood".
    // the control structure is a FloodGate. implemented by concurrency primitives

// (2) -- SimpleInput
    // - this is just one variety; it is however probably the most common one.
    // - data that comes through is "interchangeable" with the next datapoint: We always only care about the most recent one
    // - i.e., if WaterT is, as used in this file, PlanT of the RSA graph model, that PlanT should always address the same nodes, or else, meaning would be lost
    //   if this require

