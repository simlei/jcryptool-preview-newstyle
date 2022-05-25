package rsaelgamal

import bci.metamodel.Node
import bci.swt.{*, given}
import org.eclipse.jface.action.*
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.*
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.{Color, Image, RGB}
import org.eclipse.swt.layout.{GridData, GridLayout}
import org.eclipse.swt.widgets.{List as ListWidget, *}
import org.eclipse.wb.swt.SWTResourceManager
import rsaelgamal.RSAModel.*
import rsaelgamal.RSAStrings.*
import bci.metamodel.*
import bci.swt.platform.{ImageGetter, ImageSpec}


// object CryptUI:
//   given HasComposite[CryptUI] = _.root.root
// class CryptUI(val parent: Composite, node: Node[Either[Err, BigInt]])(using SWTLayoutRoot):
//   val root = parent.bearUniComposite()
//   val inputText = TextentryGroup(root, node)

object CalcDisplayGroup:
  // tell the language that "group" is the main SWT Control
  given rcc : RootControlContext[CalcDisplayGroup] = _.group
class  CalcDisplayGroup
  (val parent: Composite)(using img: ImageGetter):
  val group: Group = parent.bearGroup().glGrabFillH.glColumns(3)
  val leftLabel = group.bearLabel()
  val text = new Text(group, SWT.BORDER).glGrabFillH.glFillV; text.setEditable(false)
  // val img = UIInfoButton(group, ImageGetter.ICON_INFO)
  val calcButton = new Button(group, SWT.PUSH).withText("Calculate")
  calcButton.glVisible(false) // TODO: crutch
  def paintWith(info: NodeDescription[?,?]) =
    group.withText(s"Calculation of ${info.shortASCII} (automatic)")
    leftLabel.withText(s"${info.shortASCII} = ")


object TextentryGroup:
  given rcc: RootControlContext[TextentryGroup] = _.root
class TextentryGroup
  (val parent: Composite)
  (using rcc: SWTLayoutRoot, ig: ImageGetter):
  def setGroupLabel(text: String) = root.withText(text) // TODO: use!

  val root = parent.bearGroup().glGrabFillH
  val textenter = TextEnterCompositeOneline(root)
    textenter.uni.rightRoot.glColumns(2)
    textenter.uni.root.glGrabFillH
    textenter.uni.setVisible(UniComposite.Hole.Right)(true)
  val btn = textenter.uni.rightRoot.bearButton(SWT.PUSH).glFillV.withText("OK")
  val infobtn = UIInfoButton(textenter.uni.rightRoot, ImageGetter.ICON_INFO)

class TextEnterCompositeOneline(parent: Composite)(using rcc: SWTLayoutRoot):
  val uni = UniComposite(parent)
    uni.mainRoot.glColumns(3).glGrabFillH
  val errorDisplayLabel = ErrorDisplayLabel(uni.bottomRoot, uni.setVisible(UniComposite.Hole.Bottom))
  val lbl = uni.mainRoot.bearLabel().glAlignCenterV
  val text = uni.mainRoot.bearText(SWT.BORDER).glGrabFillBoth.glFillV
  def textstream = getSWTStream(text, SWTEvt.Modify)
  def textsink = text.sinkTextStateTf // TODO: rename sinkTextStateTf into sth better

class ErrorDisplayLabel(parent: Composite, val setVisible: Boolean => Unit = (_ => ())):
  val lbl = parent.bearLabel(SWT.WRAP).glGrabFillH.glWHint(200)
  lbl.setForeground(new Color(lbl.getDisplay, RGB(200, 100,100)))
  lbl.setText("lorem ipsum....")

class UIInfoButton(val parent: Composite, startImg: ImageSpec = ImageGetter.ICON_INFO)(using img: ImageGetter) {
  var currentImg = startImg
  val btn = new Button(parent, SWT.PUSH).glAlignCenterV
  def setImg(spec: ImageSpec) = {
    this.currentImg = spec
    btn.setImage(img.getImgOrNull(spec))
  }
  setImg(currentImg)
}

object DialogBar:
  given RootControlContext[DialogBar] = _.root
class DialogBar(parent: Composite):
  val root = parent.bearComposite().glGrabFillH
  val instructionLabel = root.bearLabel(SWT.WRAP | SWT.CENTER).glWHint(400).glGrabFillH
  val navigationBar = NavigationBar(root)
  navigationBar.glGrabFillH
  export navigationBar.{btnBack, middle, btnNext}

object NavigationBar:
  given bci.swt.RootControlContext[NavigationBar] = _.root
class NavigationBar(parent: Composite):
  val root = parent.bearComposite().glColumns(5).glGrabFillH
  val btnBack = root.bearButton(SWT.PUSH).withText("← Back")
  root.bearLabel(SWT.NONE).glWHint(20)
  // btnBack.glVisible(false)
  val middle = root.bearLabel(SWT.WRAP | SWT.CENTER).glWHint(100).glGrabFillH
  root.bearLabel(SWT.NONE).glWHint(20)
  val btnNext = root.bearButton(SWT.PUSH).glAlignRightH.withText("Next →")

object CryptUI:
  given RootControlContext[CryptUI] = _.root
class CryptUI(parent: Composite)(using SWTLayoutRoot, ImageGetter):
  val root = parent.bearComposite().glGrabFillH
  val encryptInputChooser = TextentryGroup(root)
  val encryptBtn = root.bearButton().withText("$Action").glAlignCenterH.glWHint(200)
  encryptBtn.glVisible(false)
  val encryptOutputDisplay = CalcDisplayGroup(root)



