package bci.swt

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.{List => ListWidget, *}
import bci.myrx.*

// parameterless global which marks the place where layout calls go
object SWTLayoutRoot:
  def of[C: HasComposite](c: C) = SWTLayoutRoot(HasComposite.in(c))
case class SWTLayoutRoot(root: Composite)

trait HasMainLabel[-T]:
  def set(t: String): Control

object RootControlContext:
  def in[T: RootControlContext](t: T) = summon[RootControlContext[T]].rootOf(t)
trait RootControlContext[-T]:
  def rootOf(t: T): Control
given forSWT[Ctrl <: Control]: RootControlContext[Ctrl] with
  override def rootOf(ctrl: Ctrl): Ctrl = ctrl

@FunctionalInterface
trait HasComposite[-T] extends RootControlContext[T]:
  final override def rootOf(t: T) = composite(t)
  def composite(t: T): Composite
object HasComposite:
  def in[C: HasComposite](c: C) = summon[HasComposite[C]].composite(c)
given forSWTCanvas[SWTC <: Composite]: HasComposite[SWTC] = comp => comp

// marks swt canvasses that are "ready to be drawn on"
trait SwtCanvas[+T]:
  lazy val stump: T
object SwtCanvas:
  given canvasHasComposite[T: HasComposite]: HasComposite[SwtCanvas[T]] = t => HasComposite.in(t.stump)
    case class Of[T: HasComposite](value: () => T) extends SwtCanvas[T]:
    override lazy val stump: T = value()
  def of[T: HasComposite](value: => T) = Of(() => value)
  extension[T: HasComposite](self: SwtCanvas[T])
    def composite = summon[HasComposite[T]].composite(self.stump)
    def map[V: HasComposite](f: T=>V) = new SwtCanvas[V]:
      override lazy val stump: V = f(self.stump)

//TODO: remove [HasComposite] from signatures, replace with straight composite
@FunctionalInterface
trait SwtDrawer[+T] {
  def draw(parent: Composite): T
}
object SwtDrawer:
  // case class Nested[T: HasComposite,+V](subj: SwtDrawer[T], inner: T => SwtDrawer[V]) extends SwtDrawer[V]:
  //   override def draw(parent: Composite): V = 
  //     val subjDrawn: T = subj.draw(parent)
  //     val innerDrawn: V = inner(subjDrawn).draw(HasComposite.in(subjDrawn))
  //     innerDrawn
  case class Mapped[T,+V](subj: SwtDrawer[T], f: T => V) extends SwtDrawer[V]:
    override def draw(parent: Composite) =
      val subjDrawn = subj.draw(parent)
      f(subjDrawn)
  def of[T](f: Composite => T) = new SwtDrawer[T]:
    override def draw(parent: Composite) = f(parent)

  // extension[T: HasComposite](self: SwtDrawer[T])
  //   def nest[V](f: T => SwtDrawer[V]): SwtDrawer[V] = Nested[T,V](self, f)
  extension[T](self: SwtDrawer[T])
    def map[V](f: T => V): SwtDrawer[V] = Mapped[T,V](self, f)
    def asShellSpec(cfgs: ShellCfg*) = ShellContent(self, ShellCfg.Sequence(cfgs.toList))

object Thunk:
  val empty: Thunk = () => ()
  def apply(fval: => Unit): Thunk = () => fval
type Thunk = () => Unit
// @FunctionalInterface
// trait Thunk[-T]:
//   def apply(): Unit

// TODO: %: integrate elsewhere
object BaseComposite:
  given hasComposite: HasComposite[BaseComposite] = _.root
class BaseComposite(val parent: Composite) {
  val root = parent.bearComposite().glWithGrid.glNoMargins
}

type TextStateTf = TextState => TextState

// selection is (idxStart, idxEnd) (index == 0 => cursor is at leftmost position)
case class TextState(text: String, selection: (Int, Int) = (0,0)) {
  import TextState.*
  def withTextHeuristically(newText: String): TextState = {
    val isAtEnd = selection._1 >= text.length -1
    val newSelection: (Int,Int) = isAtEnd match {
      case true => fitSelectionToText((text.length, text.length), text)
      case _ => fitSelectionToText(selection, text)
    }
    copy(text = newText, selection = newSelection)
  }
}
object TextState {
  def ofString(s: String) = TextState(s, (s.length, s.length))
  def of(t: Text) = TextState(t.getText(), (t.getSelection.x, t.getSelection.y))

  extension(t: Text) {
    def writeState(s: TextState) = {
      t.withText(s.text)
      t.withSelection(s.selection)
    }
  }

  def fitSelectionToText(sel: (Int,Int), text: String) = sel match {
    case (start,end) => (start max 0, end min text.length)
  }
}
