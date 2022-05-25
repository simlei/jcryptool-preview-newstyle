package bci.swt

import zio.stream.*

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

// trait HasSignalsGeneric[C](using rcc: RootControlContext[C]):
//   rcc

// trait HasSignalsSelectable
//   def clickSignal: StreamZ[Unit]
//   def selectionSignal: StreamZ[Unit]
// trait SignalsEditable[Domain]
//   def editSignal: StreamZ[Domain]

type SWTSink[-T] = SinkZ[T]
type SWTStream[+T] = StreamZ[T]

def getSWTStream[Ctrl <: Control, E <: SWTEvt, T](control: Ctrl, evt: E)(using pub: SWTPublisher[Ctrl, E, T]): SWTStream[T] =
  pub.register(control)

def getSWTSink[Ctrl <: Control, T](control: Ctrl)(using sink: SWTConsumer[Ctrl, T]): SWTSink[T] =
  return sink.of(control)

// to be populated very late
trait SWTIOControlExtensions[T <: Control] { self: ControlExtensionsMixins[T] =>
}



def promiseDisposalOf[R](c: Control) =
  Promise.make[R,SWTEvt.Dispose.type].flatMap{p =>
    waitForDisposalOf(c) *> p.complete(SWTEvt.Dispose.toZ).fork *> p.toZ
  }
def waitForDisposalOf[R,E](c: Control): ZIO[R,E,SWTEvt.Dispose.type] = {
  // TODO: race condition susceptible
  ZIO.async(callback => 
    var called_back = false
    def callbackOnce(fromEvt: Boolean) =
      if ! called_back && (fromEvt || c.isDisposed) then
        called_back = true
        callback(ZIO.succeed(SWTEvt.Dispose))
    c.getDisplay.syncExec{ () =>
      c.addDisposeListener{
        new DisposeListener {
          override def widgetDisposed(evt: DisposeEvent) = callbackOnce(true)
        }
      }
      callbackOnce(false)
    }
  )
}

object SWTEvt:
  case object Modify    extends SWTEvt(SWT.Modify)
  case object Selection extends SWTEvt(SWT.Selection)
  case object Dispose extends   SWTEvt(SWT.Dispose)
sealed trait SWTEvt(swtint: Int)

// trait SWTProperty[C <: Control,Data]
// object SWTProperty:
//   trait Settable[C <: Control, Data](val ctrl: C) extends SWTProperty[C,Data]
//   trait Gettable[C <: Control, Data](val ctrl: C) extends SWTProperty[C,Data]
//   trait Streamable[C <: Control, E <: SWTEvt, Data](val ctrl: C, val evt: E) extends SWTProperty[C,Data]

//   // multiplexers...
//   class SetGet[C <: Control, Data]
//   class SetGetStream[C <: Control,E<:SWTEvt,Data](ctrl: C, )
//     extends Settable[C,Data](ctrl) with Gettable[C,Data](ctrl) with Streamable[C,Data](ctrl, e)

  // object TextContent extends SetGetStream[Text,TextState]
  // object Lbl extends SetGet[Group|Label|Button, String]
  // object Selection extends Streamable[Control,Unit]



sealed trait SWTPublisher[Ctrl <: Control, E <: SWTEvt, T]:
  final def register(c: Ctrl): SWTStream[T] = 
    (c.isDisposed || c.getDisplay.isDisposed) match
      case true => { println("widget disposed, returning empty event stream"); ZStream.empty }
      case false => ZStream.async{cb =>
        c.getDisplay.syncExec{ () =>
          subscribe(c, t => cb(ZIO.succeed(Chunk(t))))
          c.addDisposeListener(new DisposeListener() {
            override def widgetDisposed(x: DisposeEvent) =
              cb.end
          })
        }
      }
  // implement: call swt methods on the object that registers the callback
  def subscribe(c: Ctrl, cb: T => Unit): Unit

sealed trait SWTConsumer[Ctrl <: Control, T]:
  final def of(c: Ctrl): SWTSink[T] = 
    ZSink.foreach[Any,Nothing,T]{ data =>
      ZIO.succeed { c.getDisplay.asyncExec(() =>
          push(c, data)
        )
      }
    }
  // implement: call swt methods and submit the data
  def push(control: Ctrl, data: T): Unit

given SWTPublisher[Text, SWTEvt.Modify.type, TextState] with
  def subscribe(text: Text, cb: TextState => Unit) = text.withStateListener(cb)

given btnPub: SWTPublisher[Button, SWTEvt.Selection.type, Unit] with
  def subscribe(button: Button, cb: Unit => Unit) = {
    button.withAction(cb(()))
  }

given SWTPublisher[Control, SWTEvt.Dispose.type, Unit] with
  def subscribe(c: Control, cb: Unit => Unit) = {
    c.addDisposeListener(new DisposeListener() {
      override def widgetDisposed(evt: DisposeEvent) = cb(())
    })
  }

given SWTConsumer[Control, Visibility] with
  def push(control: Control, data: Visibility) = control.glVisible(data.isVisible) // TODO: bug: glVisibleAndLayout can't be used here b/c context bound
given SWTConsumer[Text, String] with
  def push(control: Text, data: String) = control.withText(data)
given SWTConsumer[Text, TextStateTf] with
  def push(control: Text, transform: TextStateTf) = control.withState(transform(control.getState))






// -----


class SWTGenericIOSource[T <: Control](thisCtrl: T) {
  // thisCtrl.addListener()
}
class SWTTextIOSource[T <: Text](thisCtrl: T) extends SWTGenericIOSource(thisCtrl) {
}
class SWTBtnIOSource[T <: Button](thisCtrl: T) extends SWTGenericIOSource(thisCtrl) {
}


// val x = SWT.Modify
// see: https://livebook.manning.com/book/swt-jface-in-action/chapter-4/16
// ArmEvent       	ArmListener       	widgetArmed()                                                                        	MenuItem
// ControlEvent   	ControlListener   	controlMoved() controlResized()                                                      	Control, TableColumn, Tracker
// DisposeEvent   	DisposeListener   	widgetDisposed()                                                                     	Widget
// FocusEvent     	FocusListener     	focusGained() focusLost()                                                            	Control
// HelpEvent      	HelpListener      	helpRequested()                                                                      	Control, Menu, MenuItem
// KeyEvent       	KeyListener       	keyPressed() keyReleased()                                                           	Control
// MenuEvent      	MenuListener      	menuHidden() menuShown()                                                             	Menu
// ModifyEvent    	ModifyListener    	modifyText()                                                                         	CCombo, Combo, Text, StyledText
// MouseEvent     	MouseListener     	mouseDoubleClick() mouseDown() mouseUp()                                             	Control
// MouseMoveEvent 	MouseMoveListener 	mouseMove()                                                                          	Control
// MouseTrackEvent	MouseTrackListener	mouseEnter() mouseExit() mouseHover()                                                	Control
// PaintEvent     	PaintListener     	paintControl()                                                                       	Control
// SelectionEvent 	SelectionListener 	widgetDefaultSelected() widgetSelected()                                             	Button, CCombo, Combo, CoolItem, CTabFolder, List, MenuItem, Sash, Scale, ScrollBar, Slider, StyledText, TabFolder, Table, TableCursor, TableColumn, TableTree, Text, ToolItem, Tree
// ShellEvent     	ShellListener     	shellActivated() shellClosed() shellDeactivated() shellDeiconified() shellIconified()	Shell
// TraverseEvent  	TraverseListener  	keyTraversed()                                                                       	Control
// TreeEvent      	TreeListener      	treeCollapsed() treeExpanded()                                                       	Tree, TableTree
// VerifyEvent    	VerifyListener    	verifyText()                                                                         	Text, StyledText
