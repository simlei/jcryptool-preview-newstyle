package rsaelgamal
import zio.*
import zio.stream.*
import rsaelgamal.RSAModel.*
import rsaelgamal.RSAModel.paramsGraph.*
import bci.metamodel.Graphs.*
import simlei.util.*

object controller:

  case class ControllerState(currentExec: Option[AttentionExec], history: AttentionPlanHistory)

  class Instance(
    val spec: Spec,
    val queue          : Queue[AttentionPlan],
    val state          : SubscriptionUpdateRef[ControllerState],
    val historyMaxLength: Int = 5
  ):
    val sink = ZSink.foreach[Any,Nothing,AttentionPlan]{ queue.offer(_) }
    val currentExecGet = state.ref.map{_.currentExec}.get
    val historyGet     = state.ref.map{_.history}.get
    def currentExecSet(newExec: Option[AttentionExec])  = state.ref.update{_.copy(currentExec = newExec)}
    def historySet(newHistory: AttentionPlanHistory)  = state.ref.update{_.copy(history = newHistory)}
    def historyUpdate(update: AttentionPlanHistory => AttentionPlanHistory)  = state.ref.update{old => old.copy(history = update(old.history))}

    // TODO: soon: move this async graph algorithm to a new place
    val runnerZ =
      ZStream.fromQueue(queue).foreach{ att_plan =>
        for {
          current <- historyGet
          currentHist <- historyGet
          initialState = currentHist.initialState
          startingState = currentHist.history.headOption.map{_.result.newState}.getOrElse(initialState)
          currentState <- Ref.make(startingState)
          newState <- Ref.make(startingState)
          entries: Ref[List[GraphResultEntry]] <- Ref.make[List[GraphResultEntry]](List())
          graphUpdateAlgoSteps = att_plan.plan.propagatedSeq.map{ transition => for {
              curS            <- currentState.get
              attstate        =  AttentionExec(att_plan, curS, transition)
              published       <- state.ref.update{_.copy(currentExec = Some(attstate))} // currentExec.ref.set(Some(attstate))
              newS            =  transition.transition(curS)
              updatedNewState <- for {
                updatedNewState <- newState.set(newS)
              } yield newS
              updatedEntries <- for {
                newS          <- newState.get
                curEntries    <- entries.get
                newEntries    =  curEntries :+ GraphTransitionResultEntry(curS, newS, transition)
                updateEntries <- entries.set(newEntries)
              } yield newEntries
              updateCurrentState <- currentState.set(updatedNewState) *> updatedNewState.toZ
            } yield () }
          runGraphProgram <- ZIO.collectAll(graphUpdateAlgoSteps)
          entriesAfter <- entries.get
          updateResult <- historyUpdate{ oldHistory =>
            val attResult = AttentionPlanResult(att_plan, GraphTransitionResult(att_plan.plan, entriesAfter, startingState))
            oldHistory.copy(history = attResult :: oldHistory.history.take(historyMaxLength-1))
          }
          _ <- currentExecSet(None)
        } yield ()
      }

    val graphResultsStream = state.updates.changesOn(_.history).map{_.history.headOption}.filter{_.isDefined}.map{_.get}
    // val graphResultsStream = state.updates.filter{_.}.map{_.history.headOption}.filter{_.isDefined}.map{_.get}
      
    val runnableSync: Runnable = (
      () => Runtime.default.unsafeRun(runnerZ)
    )
    val runningInThread = new Thread(runnableSync)
  end Instance

    // TODO: define private and public ZStreams, ZSinks etc that shape the intended behavior
  case class Spec():
    def makerZ() = for {
    queue <- Queue.unbounded[AttentionPlan]
    state <- SubscriptionUpdateRef.make(ControllerState(None, AttentionPlanHistory.ofEmpty(ParamsGraph)))
  } yield Instance(this, queue, state)
  end Spec
end controller


