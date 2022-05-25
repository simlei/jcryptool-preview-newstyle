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

// object parsing // extends app.BodyPart:
object parsing:

  class Instance(spec: Spec,
      parseDisplayersBigInt: List[TextParserDisplayer.Instance[BigInt]],
      justDisplayersBigInt: List[TextDisplayer.Instance[BigInt]],
      dDisplayer: TextDisplayer.Instance[DAsInverseData],
    ):
    import rsaelgamal.ui.canon.*
    val uiface = spec.specScope.uiface

    val nodes_allBigIntToDisplay = nodes.allToDisplay.diff(List(D)) // all toDisplay, but not D

    def parseDisplayerFor(node: NodeAny): TextParserDisplayer.Instance[BigInt] = // TODO: typing
      parseDisplayersBigInt(nodes.allInputs.indexOf(node))
    def justDisplayer(node: NodeAny): TextDisplayer.Instance[BigInt] = // TODO: typing
      justDisplayersBigInt(nodes_allBigIntToDisplay.indexOf(node))

    val presetSmall = PresetInput(apriori.smallParams, uiface.presetSmallSignal)
    val presetBig   = PresetInput(apriori.bigParams, uiface.presetBigSignal)
    // val pHandler = 

    sealed trait Displayed[T] extends Output[T]: // well-formed "data" that can go straight into gates
      def runnerZ: UIO[Any] = ZIO.unit
    sealed trait Parsed[T] extends Input[T]: // well-formed "data" that can go straight into gates
      def runnerZ: UIO[Any] = ZIO.unit

    case class PresetInput( preset: apriori.Params, signal: ui.canon.PresetSignal ) extends Parsed[apriori.Params]:
      override def stream = signal.stream.as(preset)

    case class SingleParsedGatedInput( uiAttached: uiface.attach.InputsAttachment ) extends Parsed[BigInt]:
      val parser: TextParserDisplayer.Instance[BigInt] = parseDisplayerFor(uiAttached.node)
      override def stream = parseDisplayerFor(uiAttached.node).streamParsed.rightStream
      val errStream = parseDisplayerFor(uiAttached.node).streamParsed.leftStream
      override def runnerZ = parser.runnerZ // TODO: show errors!

    case class SingleParsedGatedOutput( uiAttached: uiface.attach.ToDisplayAttachment ) extends Displayed[BigInt]:
      val parser: TextParserDisplayer.Instance[BigInt] = parseDisplayerFor(uiAttached.node)
      override def sink = ZSink.fromHub(justDisplayer(uiAttached.node).input)
      override def runnerZ = parser.runnerZ // TODO: show errors!

    def runnerZ(using instScope: app.InstanceScope) = for {
      _ <- ZIO.infinity
    } yield ()
  end Instance

  case class Spec(gatingSpec: gating.Spec)(using val specScope: app.SpecScope) :
    import rsaelgamal.attach.*
    import rsaelgamal.RSAParsing
    import rsaelgamal.RSAStrings.*
    val bigIntParser = RSAParsing.given_Parseable_BigInt.parseFromStr(_)
    val bigIntDisplayer = RSAParsing.given_Show_BigInt
    def bigIntTextDisplayer(data: BigInt)(old: TextState): TextState =
      old.withTextHeuristically(bigIntDisplayer.show(data))
    def dTextDisplayer(data: DAsInverseData)(old: TextState): TextState =
      old.withTextHeuristically(bigIntDisplayer.show(data.d))
      
    // type ParseGlueT[NT <: nodes.Inputs] = glueParticles.NodeParsingGlue[BigInt, NT]
    // type DisplayGlueT[NT <: nodes.Inputs | nodes.ToDisplay] = glueParticles.NodeParsingGlue[BigInt | DAsInverseData, NT]
    // val parseP = TextParserDisplayer.Spec(bigIntParser, bigIntTextDisplayer).makerZ()
    // val parseQ = TextParserDisplayer.Spec(bigIntParser, bigIntTextDisplayer).makerZ()
    // val parseE = TextParserDisplayer.Spec(bigIntParser, bigIntTextDisplayer).makerZ()
    // def parseOp(op: Operation) = TextParserDisplayer.Spec(bigIntParser, bigIntTextDisplayer).makerZ()

    // def parserSpec[NT <: nodes.Inputs](node: NT) = 
    //   ZIO.succeed(node).zip(TextParserDisplayer.Spec(bigIntParser, bigIntTextDisplayer).makerZ())

    // val nDisplayer       = TextDisplayer.Spec(bigIntTextDisplayer).makerZ()
    // val phiDisplayer     = TextDisplayer.Spec(bigIntTextDisplayer).makerZ()
    // val dDisplayer       = TextDisplayer.Spec(dTextDisplayer).makerZ()
    // val encryptDisplayer = TextDisplayer.Spec(bigIntTextDisplayer).makerZ()
    // val decryptDisplayer = TextDisplayer.Spec(bigIntTextDisplayer).makerZ()
    // val signDisplayer    = TextDisplayer.Spec(bigIntTextDisplayer).makerZ()
    // val verifyDisplayer  = TextDisplayer.Spec(bigIntTextDisplayer).makerZ()

    val allInputNodes: List[nodes.Inputs] = List(P,Q,E,Operation.Encrypt.OpInput,Operation.Decrypt.OpInput,Operation.Sign.OpInput,Operation.Verify.OpInput)
    val allToDisplayNodes: List[nodes.ToDisplay] = List(N,Phi,D,Operation.Encrypt.OpOutput,Operation.Decrypt.OpOutput,Operation.Sign.OpOutput,Operation.Verify.OpOutput)

    val bigIntParserMaker    = TextParserDisplayer.Spec(bigIntParser, bigIntTextDisplayer).makerZ()
    val bigIntDisplayerMaker = TextDisplayer.Spec(bigIntTextDisplayer).makerZ()
    val dDisplayerMaker      = TextDisplayer.Spec(dTextDisplayer).makerZ()

    def makerZ() = for {
      parseDisplayers <- ZIO.collectAll( List.tabulate(7)(_ =>  bigIntParserMaker) )
      justDisplayersBigInt <- ZIO.collectAll( List.tabulate(6)(_ =>  bigIntDisplayerMaker) )
      justDisplayerD       <- dDisplayerMaker
    } yield Instance(this, parseDisplayers, justDisplayersBigInt, justDisplayerD)
  end Spec
end parsing

