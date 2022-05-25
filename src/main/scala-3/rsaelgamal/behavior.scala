package rsaelgamal.behavior // TODO: rename to behavior

import bci.*
import bci.metamodel.Graphs.*
import bci.metamodel.*
import bci.myrx.*
import bci.swt.*
import bci.swt.given
import bci.view.Parsing.*
import cats.Show
import org.eclipse.swt.widgets.{List as ListWidget, *}
import rsaelgamal.*
import rsaelgamal.RSAModel.*
import rsaelgamal.RSAModel.paramsGraph.*
import rsaelgamal.RSAStrings.*
import rsaelgamal.ErrorDisplayLabel

import zio._
import zio.stream._
import zio.Console._

import simlei.util.*
import bci.metamodel.Graphs.GraphInstruction.Put
import bci.metamodel.Graphs.GraphInstruction.Refresh


// === One of the main abstractions of UI<-> controller flow: "Floodgates": streams of graph plans, and how to gate + transform them

object Streamgate:
  enum GateState(val isOpen: Boolean):
    case Up extends GateState(true)
    case Down extends GateState(false)
  enum GateSignal(shouldOpen: Boolean):
    case Open extends GateSignal(true)
    case Close extends GateSignal(false)
  class Instance[Water,H2O](
    val spec:             Spec[Water,H2O],
    val gateState:        SubscriptionRef[Streamgate.GateState],
    val input:            Hub[Water | Streamgate.GateSignal],
    qBeforeGate:      Queue[Water],
    qAfterGate:       Queue[Water],
    // gateOutputQueue:  Queue[H2O],
    val gateOutputHub:    Hub[H2O],
    letThroughSignal: SubscriptionRef[Long],
    processSignal:    SubscriptionRef[Long]
  ):
    val onReceive =  input.subscribe.use { hubDequeue =>
      ZStream.fromQueue(hubDequeue)
      .dbgprinting("SG_onReceive")
      .foreach {
        case signal: GateSignal => signal match
          case GateSignal.Open => gateState.ref.set(GateState.Up)
          case GateSignal.Close => gateState.ref.set(GateState.Down)
        case data @ _ => for { // type union is unsafe here... What about the case Water == GateSignal? Proper modeling would use Either[...]
          gateState <- gateState.ref.get
          _ <- qBeforeGate.offer(data.asInstanceOf[Water]) // the "hack" is here. no biggie though
          _ <- gateState match
            case GateState.Up   => letThroughSignal.ref.update(_+1) // NOTE: for consistently open gates, debouncing ideas should take this into account
            case GateState.Down => ZIO.unit
        } yield ()
      }
    }
    val onGateChange = gateState.changes
      // .dbgprinting("gateState")
      .foreach {
        case GateState.Up => letThroughSignal.ref.update{_+1}
        case GateState.Down => ZIO.unit
      }
    val onLetThrough = letThroughSignal.changes
      // .dbgprinting("letThroughSignal")
      .foreach { _ => for {
          passengers <- qBeforeGate.takeAll
          letThrough <- qAfterGate.offerAll(passengers)
          letsProcess <- processSignal.ref.update(_+1)
        } yield ()
      }
    val onProcess = processSignal.changes
      // .dbgprinting("processSignal")
      .foreach { _ => for {
          processed <- spec.processing(qAfterGate)
          // _ = println(s"processSignal: processed $processed")
          published <- gateOutputHub.publishAll(processed)
        } yield published
      }
    // probably the main output API, but `outputHub` also
    def outputStream = ZStream.fromHub(gateOutputHub)
      .dbgprinting("queue_output_stream")

    def runnerZ = ZIO.collectAllPar(
      List(onReceive, onGateChange, onLetThrough, onProcess)
    ).as(())
  end Instance
    // TODO: define private and public ZStreams, ZSinks etc that shape the intended behavior
  case class Spec[Water,H2O](val firstState: Streamgate.GateState, val processing: Queue[Water] => IOZ[Iterable[H2O]]):
    def makerZ() =
      for {
        gateState        <- SubscriptionRef.make(firstState)
        gateInput        <- ZHub.unbounded[Water | GateSignal]
        qBeforeGate      <- ZQueue.unbounded[Water]
        qAfterGate       <- ZQueue.unbounded[Water]
        // gateOutputQueue  <- ZQueue.unbounded[H2O]
        gateOutputHub    <- ZHub.unbounded[H2O]
        letThroughSignal <- SubscriptionRef.make[Long](0) // TODO:later: remodel to use boolean because alternating "flanks" are nice and no overflow?
        processSignal    <- SubscriptionRef.make[Long](0)
      // } yield Streamgate(this, gateState, gateInput, qBeforeGate, qAfterGate, gateOutputQueue, gateOutputHub, letThroughSignal, processSignal)
      } yield Instance(this, gateState, gateInput, qBeforeGate, qAfterGate, gateOutputHub, letThroughSignal, processSignal)
  end Spec
  object Spec:
    // - merge the events by successively combining (1) the more recent with (2) the next one
    // - transform the result at the end
    def ofCombining[Water,H2O](firstState: Streamgate.GateState)(combinator: (Water,Water) => Water)(transformator: Water => H2O) =
      def processAfterGate(buffer: Queue[Water]): IOZ[Iterable[H2O]] = for {
        water <- buffer.takeAll.map{_.toList}
        merged = water match
          case firstDrop :: rest => Some(rest.foldLeft(firstDrop)(combinator))
          case Nil => None
        result = merged.map{transformator}.toList
      } yield result
      Spec[Water,H2O](firstState, buffer => processAfterGate(buffer))
    // - merge the events by successively combining (1) the more recent with (2) the next one
    def mergeBuffer[H2O](firstState: Streamgate.GateState)(merger: (H2O,H2O) => H2O) =
      ofCombining(firstState)(merger)(identity)
    def justBufferHeadAndTransform[Water,H2O](firstState: Streamgate.GateState)(transformator: Water => H2O) =
      ofCombining[Water,H2O](firstState){ case (drop1, drop2) => drop1 }(transformator)
    def justBufferHead[Water](firstState: Streamgate.GateState) =
      justBufferHeadAndTransform[Water,Water](firstState)(identity)
  end Spec
end Streamgate


object TextDisplayer:
  case object Spec:
    def generalFunOfSimple[DataT](simpleFun: DataT => String)(data: DataT)(oldState: TextState): TextState =
        TextState.ofString(simpleFun(data)) // oldState is ignored
    def ofSimple[DataT](simpleFun: DataT => String) = Spec(generalFunOfSimple(simpleFun))
  case class Spec[DataT](displayFun: DataT => TextState => TextState):
    def makerZ() = for {
      input  <- ZHub.unbounded[DataT]
      output <- ZHub.unbounded[TextState => TextState]
    } yield Instance(this, input, output)

  class Instance[DataT](val spec: Spec[DataT],
    val input: Hub[DataT],
    val output: Hub[TextState => TextState]
  ):
    def streamTextChange: StreamZ[TextState => TextState] = ZStream.fromHub(output)
    def runnerZ = input.subscribe.use { dequeue =>
      val stream = ZStream.fromQueue(dequeue)
      stream.foreach{ (data: DataT) =>
        output.publish(spec.displayFun(data))
      }
    }
end TextDisplayer

object TextParser:
  case class Spec[DataT](parseFun: String => Either[ParseErr, DataT]):
    def makerZ() = for {
      input <- ZHub.unbounded[String]
      output <- ZHub.unbounded[Either[ParseErr, DataT]]
    } yield Instance(this, input, output)
  class Instance[DataT](val spec: Spec[DataT],
    val input: Hub[String],
    val output: Hub[Either[ParseErr, DataT]],
  ):
    def runnerZ = input.subscribe.use { dequeue =>
      val stream = ZStream.fromQueue(dequeue)
      stream.foreach{ (data: String) =>
        output.publish(spec.parseFun(data))
      }
    }
end TextParser

object TextParserDisplayer:
  case class Spec[DataT](
    parseFun: String => Either[ParseErr, DataT], 
    displayFun: DataT => TextState => TextState
  ):
    def displayFunFilteredByParser(data: DataT)(oldState: TextState): TextState =
      parseFun(oldState.text) match
        case Left(err) => displayFun(data)(oldState) // use the original on parse err
        case Right(parsed) => if parsed == data then oldState else displayFun(data)(oldState)
    def makerZ() = for {
      displayer <- TextDisplayer.Spec(displayFunFilteredByParser).makerZ() // display function gets modulated in this context
      parser <- TextParser.Spec(parseFun).makerZ()
      textIn <- ZHub.unbounded[String]
      dataIn <- ZHub.unbounded[DataT]
      textDisplayedOut <- ZHub.unbounded[TextState => TextState]
      dataParsedOut <- ZHub.unbounded[Either[ParseErr, DataT]]
    } yield Instance(this, parser, displayer, textIn, dataIn, textDisplayedOut, dataParsedOut)
  end Spec
  class Instance[DataT](val spec: Spec[DataT],
    parser: TextParser.Instance[DataT],
    displayer: TextDisplayer.Instance[DataT],
    val textIn: Hub[String],
    val dataIn: Hub[DataT],
    val textDisplayedOut: Hub[TextState => TextState],
    val dataParsedOut:Hub[Either[ParseErr, DataT]],
  ):
    def streamTextTf = ZStream.fromHub(textDisplayedOut)
    def streamParsed = ZStream.fromHub(dataParsedOut)
    def runnerZ = 
      val fwdTextIn = textIn.subscribe.use { queue => ZStream.fromQueue(queue).foreach{ parser.input.publish(_) } }
      val fwdDataIn = dataIn.subscribe.use { queue => ZStream.fromQueue(queue).foreach{ displayer.input.publish(_) } }
      val fwdParserOut = parser.output.subscribe.use { queue => ZStream.fromQueue(queue).foreach{ dataParsedOut.publish(_) } }
      val fwdDisplayerOut = displayer.output.subscribe.use{ queue => ZStream.fromQueue(queue).foreach{ textDisplayedOut.publish(_) } }
      ZIO.collectAllPar(fwdTextIn :: fwdDataIn :: fwdParserOut :: fwdDisplayerOut :: Nil)
  end Instance
end TextParserDisplayer

class GraphGlues[ErrT, ThisGraphT <: PGraph[Either[ErrT, Any]]](thisGraph: ThisGraphT) {
  // abstract types to be overridden by implementors

  type NodeE      = ErrT
  type ThisGraphV = Either[NodeE,Any]

  trait NodeGlue[NodeD, NodeT <: Node[Either[NodeE,NodeD]]]:
    type NodeV = Either[NodeE,NodeD]
    val node: NodeT

  // trait UIFacingGlue:
  //   def progUpdateUI(ctrlStream: StreamZ[GraphTransitionResult[ThisGraphV]]): UIO[Any]
  // trait ControllerFacingGlue:
  //   def streamToGraph: StreamZ[GraphTransitionPlan[ThisGraphV]]

  class NodeParsingGlue[NodeD, NodeT <: Node[Either[NodeE,NodeD]]]
  (val node: NodeT)
  extends NodeGlue[NodeD,NodeT]:

    def filterNodeChanges(input: StreamZ[GraphTransitionResult[ThisGraphV]]): StreamZ[NodeV] =
      input.map{ _.onNodeValueChanged(node){ (oldV,newV) => newV } }.filter{_.isDefined}.map{_.get}

    def instructionToGraph(putData: NodeV): List[GraphInstruction[ThisGraphV]] =
      val thisNodePut     = GraphInstruction.Put(node, putData)
      val allNodesButThis = thisGraph.inputnodes.filterNot{_.equals(node)}.toList
      val otherNodesPut   = allNodesButThis.map{ inode => GraphInstruction.Refresh(inode) }
      thisNodePut +: otherNodesPut

    // former args: , val textStream: StreamZ[TextState], val textSink: SinkZ[TextStateTf]
    // former using: (using parser: Parseable[NodeD], shower: Show[NodeD])

    // def progUpdateUI_displaying(ctrlStream: StreamZ[GraphTransitionResult[ThisGraphV]]) =
    //   parserBox.wireNodeValueStream(filterNodeChanges(ctrlStream))
    //     .map{ _.textTf }.filter{_.isDefined}.map{_.get}
    //     .run(textSink)
    // def progUpdateUI_parsing(
    //   ctrlStream: StreamZ[GraphTransitionResult[ThisGraphV]],
    //   textSink: SinkZ[TextStateTf]
    // ) = 
    //   parserBox.wireNodeValueStream(filterNodeChanges(ctrlStream))
    //   .map{ _.textTf }.filter{_.isDefined}.map{_.get} // nodeAsTextStream contains even more info, but right now, we only need valid Text Tfs
    //   .run(textSink)
    // def streamToGraph(textStream: StreamZ[TextState]) =
    //   parserBox
    //     .wireTextfieldStream(textStream)
    //     .rightStream
    //     .map{ instructionToGraph(_) }
    //     .map{ thisGraph.planMulti(_) }

  end NodeParsingGlue

}








object ErrorDisplayHandler:
  
  case class ToUIStream(errVisi: StreamZ[Visibility], errText: StreamZ[String])
  sealed trait EDHInfo[+GraphE]
  enum ParseI extends EDHInfo[Nothing]:
    case Success
    case Error(err: ParseErr)
  enum GraphI[+GraphErr] extends EDHInfo[GraphErr]:
    case Success
    case Error[GraphErr](errs: List[GraphErr]) extends GraphI[GraphErr]

class ErrorDisplayHandler[NodeE](
  label: ErrorDisplayLabel
  ):

  import ErrorDisplayHandler.*
  def getErrSentenceEmpty = "Please provide an input."
  def getErrSentence(err: NodeE) = err.toString // TODO: temporary commented: re-establish or improve
  // def getParseErrSentence(err: ParseErr) = err.text match
  //     case "" => s"Please provide ${info.form} for ${info.shortASCII}"
  //     case _ => s"This input (${err.text.slice(0,10)}) ${err.reason}."
  def getParseErrSentence(err: ParseErr) = err.text match
      case "" => s"Please provide an input."
      case _ => s"This input (${err.text.slice(0,10)}) ${err.reason}."

  def parseSuccessOccurred() = label.setVisible(false)
  def graphSuccessOccurred() = label.setVisible(false)

  def pushZIO(data: EDHInfo[NodeE]) = ZIO.succeed(label.lbl.getDisplay.syncExec(() => push(data)))
  def push(data: EDHInfo[NodeE]) = data match
    case ParseI.Success    => label.setVisible(false)
    case GraphI.Success    => label.setVisible(false)
    case ParseI.Error(err) => parseErrOccurred(err)
    case GraphI.Error(err) => graphErrOccurred(err)
  val sink = ZSink.foreach{ ZIO.succeed(this.push(_)) }

  def parseErrOccurred(err: ParseErr) = 
    label.lbl.setText(getParseErrSentence(err))
    label.setVisible(true)

  def graphErrOccurred(errs: List[NodeE]) = 
    val toDisplay = formatErrSentences(errs.map{ getErrSentence(_) })
    label.lbl.setText(toDisplay)
    label.setVisible(true)

  def formatErrSentences(errs: List[String]): String = errs match
      case Nil => throw new RuntimeException("illegal argument: empty error list")
      case single :: Nil => single
      case errs => errs.mkString(
        start = s"There are issues with this input:\n- ",
        sep = "\n- ",
        end=""
        )

end ErrorDisplayHandler

