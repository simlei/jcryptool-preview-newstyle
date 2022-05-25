package rsaelgamal
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.{List => ListWidget, *}

import rsaelgamal.RSAModel.*
import bci.metamodel.*
import bci.swt.platform.*
import bci.swt.{*, given}
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Font

import rsaelgamal.Intl.IMsg
import simlei.util.*
import org.eclipse.swt.events.FocusListener
import org.eclipse.swt.events.FocusAdapter
import org.eclipse.swt.events.FocusEvent
import zio._
import zio.stream._
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.DisposeEvent

// TODO: move to package

object ui:
  object canon:
    import rsaelgamal.attach.*

    // sealed trait InputAny:
    //   def stream: StreamZ[Any]
    // sealed trait OutputAny:
    //   def sink: StreamZ[Any]
    trait Input[T]:
      def stream: StreamZ[T]
    trait Output[T]:
      def sink: SinkZ[T]

    // is that not super cool!! you can specify a bunch of fitting input types (TextState | String) and easily map them to the canonical repr.!!!
    // also, you can then offer various views on that canonical representation, like a dumbed-down StreamZ[String]!
    class TextStream(accept: StreamZ[TextState | String]) extends Input[TextState]:
      override def stream = accept.map{
        case stateData: TextState => stateData
        case str: String => TextState(str, (0,0))
      }
      def stringStream: StreamZ[String] = this.stream.map{_.text}

    class PresetSignal(accept: StreamZ[Any]) extends Input[Unit]:
      override def stream = accept.as(())
    class SimpleGatingSignal(accept: StreamZ[Any]) extends Input[Unit]:
      override def stream = accept.as(())

    // class AttentionNaviSignal(val stream: StreamZ[cerebellum.AttentionSignal]) extends Input(stream)
    class TextSink  (accept: SinkZ[TextStateTf]) extends Output[TextStateTf]:
      override def sink = accept
    class ParseDisplayPair(val source: TextStream, val sink: TextSink)

object RSAUIMain:
  import RSAModel.paramsGraph.*
  given bci.swt.RootControlContext[RSAUIMain] = _.root // tells interested parties what the SWT root control is
  extension(self: RSAUIMain)
    def interface = RSAUIMain.Interface(self)

  object Interface:
    given RootControlContext[Interface] = _.rootControl
  class Interface(val ui: RSAUIMain) { import ui.* // TODO: remove val
    import rsaelgamal.ui.canon.*
    import rsaelgamal.attach.*

    private def shell = RootControlContext.in(ui).getShell
    private def rootControl = RootControlContext.in(ui)

    val dialogNextStream: StreamZ["Next"] = getSWTStream(dialogbar.btnNext, SWTEvt.Selection).as("Next")
    val dialogBackStream: StreamZ["Back"] = getSWTStream(dialogbar.btnBack, SWTEvt.Selection).as("Back")
    def attnOperationStream(op: Operation) = getSWTStream(ui.opcontrols(op).btn, SWTEvt.Selection).as(Attention.OpAttn(op))

    val goEncryptStream: StreamZ[Attention] = getSWTStream(btnChooseEncrypt, SWTEvt.Selection).as(Attention.OpAttn(Operation.Encrypt))
    val goDecryptStream: StreamZ[Attention] = getSWTStream(btnChooseEncrypt, SWTEvt.Selection).as(Attention.OpAttn(Operation.Decrypt))
    val signStream: StreamZ[Attention] = getSWTStream(btnChooseEncrypt, SWTEvt.Selection).as(Attention.OpAttn(Operation.Sign))
    val verifyStream: StreamZ[Attention] = getSWTStream(btnChooseEncrypt, SWTEvt.Selection).as(Attention.OpAttn(Operation.Verify))

    object presetSmallSignal extends PresetSignal(getSWTStream(presetSmallBtn, SWTEvt.Selection))
    object presetBigSignal   extends PresetSignal(getSWTStream(presetBigBtn, SWTEvt.Selection))


    object attach extends rsaelgamal.RSAModel.paramsGraph.nodes.attach.RSAAttachObj:
      import rsaelgamal.RSAModel.paramsGraph.nodes.attach.*

      object p extends InputsAttachment(P)
      object q extends InputsAttachment(Q)
      object e extends InputsAttachment(E)
      object encryptInput extends InputsAttachment(Operation.Encrypt.OpInput)
      object decryptInput extends InputsAttachment(Operation.Decrypt.OpInput)
      object signInput extends InputsAttachment(Operation.Sign.OpInput)
      object verifyInput extends InputsAttachment(Operation.Verify.OpInput)

      object n extends ToDisplayAttachment(N)
      object phi extends ToDisplayAttachment(Phi)
      object d extends ToDisplayAttachment(D)

      class InputsAttachment(node: nodes.Inputs) extends RSAInputsAttachments(node):
        val uiForNode = UIForNode.forInput(node)
        // NOTE: validation should be a "general property" of the graph i.e. if e.g. PValid has a special role (it has) it should be inferred from the graph
        given out:    AttachmentHere[TextSink]           = uiForNode.toTextOutput
        given in:     AttachmentHere[TextStream]         = TextStream(uiForNode.textChangeStream)
        given parse:  AttachmentHere[ParseDisplayPair]   = ParseDisplayPair(in.attachment,out.attachment)
        given gating: AttachmentHere[SimpleGatingSignal] = SimpleGatingSignal(getSWTStream(uiForNode.textentryGroup.btn, SWTEvt.Selection)(using bci.swt.btnPub))

      class ToDisplayAttachment(node: nodes.ToDisplay) extends RSAToDisplayAttachments(node):
        val uiForNode = UIForNode.forToDisplay(node)
        given out: AttachmentHere[TextSink]   = uiForNode.toTextOutput

    end attach

    // private interface model
    sealed trait UIForNode:
      def textfield: Text
      // def textChangeSink: SinkZ[TextStateTf] = 
      def toTextOutput = rsaelgamal.ui.canon.TextSink(textfield.sinkTextStateTf)
    object UIForNode:
      class CanParseAndDisplay(val textentryGroup: TextentryGroup) extends UIForNode:
        override val textfield = textentryGroup.textenter.text
        val errorlabel = textentryGroup.textenter.lbl
        def textChangeStream: StreamZ[TextState] = getSWTStream(textfield, SWTEvt.Modify)
        // def textChangeStream: StreamZ[TextState] = textfield.getEventStream(SWTEvt.Modify)
      class CanDisplay(val displayer: CalcDisplayGroup) extends UIForNode:
        override val textfield = displayer.text

      def forInput(node: nodes.Inputs): UIForNode.CanParseAndDisplay = 
        node match
          case P   => UIForNode.CanParseAndDisplay(pChooser)
          case Q   => UIForNode.CanParseAndDisplay(qChooser)
          case E   => UIForNode.CanParseAndDisplay(eChooser)
          case opnode:  Operation.OpInputNode  => UIForNode.CanParseAndDisplay(ui.opcontrols(opnode.op).cryptUI.encryptInputChooser)
      def forToDisplay(node: nodes.ToDisplay): UIForNode.CanDisplay = 
        node match
          case N   => UIForNode.CanDisplay(nDisplay)
          case Phi => UIForNode.CanDisplay(phiDisplay)
          case D   => UIForNode.CanDisplay(dDisplay)
          case opnode:  Operation.OpOutputNode => UIForNode.CanDisplay(ui.opcontrols(opnode.op).cryptUI.encryptOutputDisplay)
    end UIForNode


  }

class RSAUIMain(parent: Composite)(using imageGetter: ImageGetter) {
  given SWTLayoutRoot = SWTLayoutRoot(parent) // this should rather be given from the outside, defaulting to this value, but later... TODO
  import rsaelgamal.RSAStrings.given

  val root         = parent.bearComposite().glColumns(1).glGrabFillBothNoMargin
  val tadComp = root.bearComposite().glGrabFillH
  val title = tadComp.bearLabel()
  val text = tadComp.bearLabel(SWT.WRAP).glWHint(800).glGrabFillH
  var fd = title.getFont.getFontData
  fd(0).setHeight(fd(0).getHeight + 8)
  title.setFont(new Font(root.getDisplay, fd))
  tadComp.setBackground(new Color(org.eclipse.swt.graphics.RGB(255,255,255)))
  title.setBackground(new Color(org.eclipse.swt.graphics.RGB(255,255,255)))
  text.setBackground(new Color(org.eclipse.swt.graphics.RGB(255,255,255)))
  title.withText("Textbook RSA")
  text.setText(RSAStrings.descriptionText)
  root.bearLabel()

  val centered = root.bearComposite(SWT.NONE).glColumns(2).glAlignCenterH.glGrabH.glFillV.glGrabV

  val leftColumn   = centered.bearGroup(SWT.NONE).glGrabH.glWHint(800).glGrabV.glFillV.withText("Choose RSA Parameters:")

  val presetUI = leftColumn.bearGroup(SWT.NONE).glGrabFillH.withText("Preset parameters")
  val presetCentering = presetUI.bearComposite().glGrabH.glAlignCenterH.glColumns(2)

  val presetSmallBtn = presetCentering.bearButton().withText("&Small parameters")
  val presetBigBtn = presetCentering.bearButton().withText("&Big parameters")

  val pChooser   = TextentryGroup  (leftColumn)
  val qChooser   = TextentryGroup  (leftColumn)
  val nDisplay   = CalcDisplayGroup(leftColumn)
  val phiDisplay = CalcDisplayGroup(leftColumn)
  val eChooser   = TextentryGroup  (leftColumn)
  val dDisplay   = CalcDisplayGroup(leftColumn)

  val compOperation = leftColumn.bearComposite().glGrabFillH.glNoMargins
  val grpOperationChoose = compOperation.bearGroup().withText("RSA operation").glColumns(4).glGrabFillH
  val btnChooseEncrypt = grpOperationChoose.bearButton().glGrabFillH
  val btnChooseDecrypt = grpOperationChoose.bearButton().glGrabFillH
  val btnChooseSign = grpOperationChoose.bearButton().glGrabFillH
  val btnChooseVerify = grpOperationChoose.bearButton().glGrabFillH

  val encryptUI = CryptUI(compOperation)
  val decryptUI = CryptUI(compOperation)
  val signUI    = CryptUI(compOperation)
  val verifyUI  = CryptUI(compOperation)


  val visGrp = centered.bearGroup().onGridData{_.minimumWidth = 500}.glGrabFillBoth
    visGrp.withText("Visualization")

  val visLbl = visGrp.bearLabel(SWT.WRAP).glGrabFillH.glWHint(380)
    visLbl.withText("This column shows the math behind RSA, much like textbook calculations. Enter parameters and perform en- and decryption, and the visualization will follow it.")
  val visBrowser = org.eclipse.swt.browser.Browser(visGrp, SWT.NONE)
    visBrowser.glGrabFillBoth
    visBrowser.setJavascriptEnabled(true)
    val tmpfile = "/home/snuc/sandbox/jct_styles/rsa.html"
    os.write.over(os.Path(tmpfile), vis.page)
    visBrowser.setUrl(s"file://$tmpfile")
    val adapter = new FocusAdapter{
      override def focusGained(evt: FocusEvent) = {
        println("refreshing browser!")
        visBrowser.setUrl(s"file://$tmpfile")
        visBrowser.refresh()
      }
    }
    visBrowser.getShell.addFocusListener(adapter)


  // object OperationUI:
  //   given RootControlContext[OperationUI] = _.cryptUI
  case class OperationUI(btn: Button, cryptUI: CryptUI)

  val opcontrols = Map[Operation, OperationUI](
    Operation.Encrypt -> OperationUI(btnChooseEncrypt, encryptUI),
    Operation.Decrypt -> OperationUI(btnChooseDecrypt, decryptUI),
    Operation.Sign    -> OperationUI(btnChooseSign   , signUI)   ,
    Operation.Verify  -> OperationUI(btnChooseVerify , verifyUI) ,
    )

  for { ((op, OperationUI(chooseOpBtn,cryptUI))) <- opcontrols } {
    val txtLabel = Intl.of(s"Text to be ${op.word.firstLower}:", s"Text zum ${op.word.firstLower}:")
    cryptUI.glGrabFillH
    cryptUI.encryptInputChooser.textenter.lbl.withText(op.word.firstUpper)
    chooseOpBtn.withText(op.word.mnemonic)
    cryptUI.glVisibleAndLayout(false)
    chooseOpBtn.withAction {
      val otherOps = Operation.allOps.filter{_ != op}
      for {otherOp <- otherOps} {
        opcontrols(otherOp).cryptUI.glVisibleAndLayout(false)
      }
      cryptUI.glVisibleAndLayout(true)
    }
  }

  val dialogbar = DialogBar(leftColumn)


  root.layout()
}





    // val allVisiControls = List(
    //   pChooser     .mixedInAsCtrl,
    //   qChooser     .mixedInAsCtrl,
    //   nDisplay     .mixedInAsCtrl,
    //   phiDisplay   .mixedInAsCtrl,
    //   eChooser     .mixedInAsCtrl,
    //   dDisplay     .mixedInAsCtrl,
    //   compOperation.mixedInAsCtrl,
    //   encryptUI    .mixedInAsCtrl,
    //   decryptUI    .mixedInAsCtrl,
    //   signUI       .mixedInAsCtrl,
    //   verifyUI     .mixedInAsCtrl,
    // )
    // val stateInit = DialogState(
    //   visible = allVisiControls.map{_ -> false}.toMap,
    //   focused = dialogbar.navigationBar.btnNext
    //   )
    // val stateEnterP = DialogState(
    //   visible = stateInit.visible ++ List(pChooser.mixedInAsCtrl -> true),
    //   focused = pChooser.textenter.text
    //   )
    // val stateEnterQ = DialogState(
    //   visible = stateEnterP.visible ++ List(qChooser.mixedInAsCtrl -> true),
    //   focused = qChooser.textenter.text
    //   )
    // val stateEnterE = DialogState(
    //   visible = stateEnterQ.visible ++ List(
    //       nDisplay.mixedInAsCtrl   -> true,
    //       phiDisplay.mixedInAsCtrl -> true,
    //       eChooser.mixedInAsCtrl   -> true,
    //     ),
    //   focused = eChooser.textenter.text
    //   )
    // def statePerformOperation(op: Operation) = DialogState(
    //   // TODO: make other ops invisible
    //   visible = stateEnterE.visible ++ List(
    //       dDisplay.mixedInAsCtrl -> true,
    //       compOperation.mixedInAsCtrl -> true,
    //       getOpUI(op).cryptUI.mixedInAsCtrl -> true,
    //     ),
    //   focused = compOperation
    //   )
    // def dialogStepFor(attention: Attention) = attention match
    //   case Attention.EmptyAttention      => stateInit
    //   case Attention.PAttn      => stateEnterP
    //   case Attention.NPhiAttn   => stateEnterQ
    //   case Attention.EDNAttn    => stateEnterE
    //   case Attention.OpAttn(op) => statePerformOperation(op)
