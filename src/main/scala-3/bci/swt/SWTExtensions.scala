package bci.swt

import bci.myrx.Publisher
import org.eclipse.swt.widgets.{List => ListWidget, *}
import org.eclipse.swt.SWT
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import scala.util.Try
import java.math.BigInteger;

import bci.swt.*

import zio.stream.*
import zio.{System => ZIOSystem, *}
import zio.stream.ZStream.Emit

import simlei.util.*

object Visibility:
  def of(b: Boolean) = b match
    case true => Visible
    case false => Invisible
enum Visibility:
  case Visible
  case Invisible
  def isVisible = this == Visible


implicit class RCCControlUtil[T] (rccc: T) (using ctx: RootControlContext[T])
  extends ControlExtensions(ctx.rootOf(rccc))

implicit class GenericCtrlUtil[T <: Control](c: T) extends ControlExtensions(c)
implicit class GenericCompositeUtil[T <: Composite](c: T) extends CompositeExtensions[T](c)
implicit class GenericGroupUtil[T <: Group](c: T) extends GroupExtensions[T](c)
implicit class LabelUtil(lbl: Label) extends LabelExtensions(lbl)
implicit class TextUtil(txt: Text) extends TextExtensions(txt)
implicit class ButtonUtil(btn: Button) extends ButtonExtensions(btn)

trait ControlExtensionsMixins[T <: Control](val mixedInAsCtrl: T) // this is the top type for all Composite Extensions
  extends SWTGridControlExtensions[T] // List of all generally-applicable Mixins follow...
  with SWTIOControlExtensions[T] // List of all generally-applicable Mixins follow...

trait CompositeExtensionsMixins[T <: Composite](val mixedInAsComp: T) // this is the top type for all Control Extensions
  extends SWTGridCompositeExtensions[T] // List of all generally-applicable Mixins follow...


// TODO: replace `c` everywhere with thisCtrl / thisComp/ etc
class ControlExtensions[T <: Control](c: T) extends ControlExtensionsMixins[T](c)  {
  def syncdRet[RT](f: => RT): RT = {
    if(thisCtrl.isDisposed) then
      throw new RuntimeException(s"WARNING: is disposed, no action: ${thisCtrl.toString}")
    var result: Option[RT] = None
    thisCtrl.getDisplay().syncExec(() => {
      result = Some(f)
    })
    result.get
  }
  def syncd(f: => Unit): Unit = {
    if(thisCtrl.isDisposed) then
      System.err.println(s"WARNING: is disposed, no action: ${thisCtrl.toString}")
      return ()
    thisCtrl.getDisplay().syncExec(() => f)
  }
  def asyncd(f: => Unit): Unit = {
    if(thisCtrl.isDisposed) then
      throw new RuntimeException(s"WARNING: is disposed, no action: ${thisCtrl.toString}")
      return ()
    thisCtrl.getDisplay().syncExec(() => f)
  }
  def asyncdZIO[RT](f: => RT): ZIO[Any, "SWTDisposed", RT] = {
    ZIO.async{callback =>
      thisCtrl.getDisplay().asyncExec(() => 
          if(thisCtrl.isDisposed) then
            callback(ZIO.fail("SWTDisposed"))
          else
            callback(ZIO.succeed(f))
        )
    }
  }

  def promiseDisposal = promiseDisposalOf(thisCtrl)
  def waitForDisposal: UIO[SWTEvt.Dispose.type] = waitForDisposalOf(thisCtrl)

  def onControl(action: T => Unit) = {
    action(c)
    c
  }
  def collectControlPathUpward(until: Option[Control] = None): List[Control] = {
    Option(c.getParent).map { p =>
      p == until match
        case true => List(p)
        case false => p +: p.collectControlPathUpward(until)
    }.getOrElse(List())
  }
  def collectControlTree(): Array[Control] =
    (c.isInstanceOf[Composite]) match
      case true => Array[Control](c) ++ c.asInstanceOf[Composite].getChildren.flatMap{child =>
        child.collectControlTree()
      }.distinct
      case _ => Array(c)

  def io = SWTGenericIOSource(thisCtrl)
}

class GroupExtensions[T <: Group](l: T) extends ControlExtensions[T](l) {
  def withText(s: String) = onControl {_.setText(s)}
}

class LabelExtensions(l: Label) extends ControlExtensions[Label](l) {
  def withText(s: String) = onControl {_.setText(s)}
}

class TextExtensions(l: Text) extends ControlExtensions[Text](l) {
  // TODO: io: remove
  def sinkText: SWTSink[String] = getSWTSink(l)
  def sinkTextStateTf: SWTSink[TextStateTf] = getSWTSink(l)

  def getState = TextState.of(l)
  def withState(s: TextState) = {
    l.withText(s.text)
    l.withSelection(s.selection)
  }
  def withText(s: String) = onControl(_setText(s))
  def withSelection(s: (Int, Int)) = onControl(_setSelection(s))
  def withStateListener(f: TextState => Unit) = onControl { txt =>
    val modListener = txt.addModifyListener(new ModifyListener() {
      override def modifyText(e: ModifyEvent) = {
        textModifyLocks.getOrElse(txt, false) match {
          case false => f(TextState(txt.getText, (txt.getSelection.x, txt.getSelection.y)))
          case true => ()
        }//f(e)
      }
    })
    val selListener = txt.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) = {
        textSelectionLocks.getOrElse(txt, false) match {
          case false => f(TextState(txt.getText, (txt.getSelection.x, txt.getSelection.y)))
          case true => ()
        }//f(e)
      }
    })
  }
  def withListener(f: ModifyEvent => Unit) = onControl { txt =>
    val listener = txt.addModifyListener(new ModifyListener() {
      override def modifyText(e: ModifyEvent) = {
        textModifyLocks.getOrElse(txt, false) match {
          case false => f(e)
          case true => ()
        }//f(e)
      }
    })
  }

  // override def io = SWTTextIOSource(thisCtrl)
}

class ButtonExtensions(b: Button) extends ControlExtensions[Button](b) {
  def withText(s: String) = onControl { _.setText(s) }
  def withAction(f: => Unit) = onControl { ctrl =>
    val x: SelectionAdapter = null;
    ctrl.addSelectionListener(new SelectionAdapter {
      override def widgetSelected(evt: SelectionEvent) = f
    })
  }

  // override def io = SWTBtnIOSource(thisCtrl)
}
class CompositeExtensions[T <: Composite](c: T) extends ControlExtensions[T](c) with CompositeExtensionsMixins[T](c) {
  def clearAllChildren(): T = {
    c.getChildren.foreach { _.dispose }
    c
  }
  def bearUniComposite()(using ui: SWTLayoutRoot) = new UniComposite(c)
  def bearComposite(swtarg: Int = SWT.NONE) = new Composite(c, swtarg).glWithGrid
  def bearButton(swtarg: Int = SWT.PUSH) = new Button(c, swtarg)
  def bearLabel(swtarg: Int = SWT.NONE) = new Label(c, swtarg)
  def bearText(swtarg: Int = SWT.NONE) = new Text(c, swtarg)
  def bearLink(swtarg: Int = SWT.NONE) = new Link(c, swtarg)
  def bearGroup(swtarg: Int = SWT.NONE) = new Group(c, swtarg).glWithGrid
}



// TODO: move elsewhere

enum LinkContentElement:
  case LinkElement(text: String, onClick: () => Unit)
  case Text(text: String)

implicit class LinkExtensions(l: Link) extends ControlExtensions[Link](l) {
  def withText(s: String) = onControl {_.setText(s)}
  def withLinkText(elements: LinkContentElement*) = {
    var counter = 0
    val contentlist = elements.zipWithIndex.map { case (el,i) => el match
      case LinkContentElement.LinkElement(t, clk) => (s"""<a href="${i}">$t</a>""", Some(i.toString -> clk))
      case LinkContentElement.Text(t) => (t, None)
    }
    val clickmap = contentlist.map { case (content, clickpair) => clickpair }.flatten.map{case (i,a) => (i.toString, a)}.toMap
    val content = contentlist.map { case (content, clickpair) => content}.mkString("")
    l.setText(content)
    l.addListener(SWT.Selection, new Listener() {
      def handleEvent(event: Event) = {
        val action = clickmap(event.text)
        action()
      }
    })
  }

}


// for locking modification and listing to modification out from each other

var textSelectionLocks = Map[Text, Boolean]()
var textModifyLocks = Map[Text, Boolean]()
def tmlLock(t: Text) = { textModifyLocks = textModifyLocks ++ Map(t -> true) }
def tmlRelease(t: Text) = { textModifyLocks = textModifyLocks ++ Map(t -> false) }
def tslLock(t: Text) = { textSelectionLocks = textSelectionLocks ++ Map(t -> true) }
def tslRelease(t: Text) = { textSelectionLocks = textSelectionLocks ++ Map(t -> false) }
def _setText(s: String)(txt: Text) = {
  tmlLock(txt)
  Try {
    txt.setText(s)
  }.fold(t => { tmlRelease(txt); throw t }, _ => tmlRelease(txt))
}
def _setSelection(sel: (Int, Int))(txt: Text) = {
  tslLock(txt)
  Try {
    txt.setSelection(sel._1, sel._2)
  }.fold(t => { tslRelease(txt); throw t }, _ => tslRelease(txt))
}


