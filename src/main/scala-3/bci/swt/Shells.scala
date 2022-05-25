package bci.swt

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.{List => ListWidget, *}
import bci.swt.{*, given}
import org.eclipse.swt.layout.GridLayout

// TODO: possible supertype of ShellCfg?
// trait CompositeCfg[-T: HasComposite]
//   def config(c)

@FunctionalInterface
trait ShellCfg:
  def config(shell: Shell): Unit
object ShellCfg:
  case class Of(f: Shell => Unit) extends ShellCfg:
    override def config(s: Shell) = f(s)
  case class AndThen(cfg: ShellCfg, f: ShellCfg) extends ShellCfg:
    override def config(s: Shell) = { cfg.config(s); f.config(s) }
  case class Sequence(cfgs: List[ShellCfg]) extends ShellCfg:
    override def config(s: Shell) = 
      ShellCfg.empty.config(s)
      for {cf <- cfgs} 
        cf.config(s)

  val gridDefault:         ShellCfg = _.setLayout(new GridLayout())
  val empty:               ShellCfg = _ => ()

  def shellDimensions(shell:  Shell): (Int, Int) = (shell.getBounds.width, shell.getBounds.height)
  def shellLeftTop(shell:  Shell): (Int, Int) = (shell.getBounds.x, shell.getBounds.y)
  def shellRightTop(shell: Shell): (Int, Int) = (shell.getBounds.x + shell.getBounds.width, shell.getBounds.y)
  def shellLeftBot(shell:  Shell): (Int, Int) = (shell.getBounds.x, shell.getBounds.y + shell.getBounds.height)
  def shellRightBot(shell: Shell): (Int, Int) = (shell.getBounds.x + shell.getBounds.width, shell.getBounds.y + shell.getBounds.height)

  extension(self: ShellCfg)
    def asShellSpec[T](drawer: SwtDrawer[T]): ShellContent[T] = ShellContent(drawer, self)
    def andThen(cfg: ShellCfg): ShellCfg = AndThen(self, cfg)

    def withDimensions(wh: (Int,Int)): ShellCfg = andThen{ shell =>
      val (left,top) = shellLeftTop(shell)
      println(s"setting dimensions ${(left, top, wh._1, wh._2)}")
      shell.setBounds(left, top, wh._1, wh._2)
    }

    def withTitle(title: String): ShellCfg = andThen{ _.setText(title) }
    def withWidth(width: Int): ShellCfg = andThen{ shell =>
      val (left,top) = shellLeftTop(shell)
      shell.setBounds(left, top, width, shellDimensions(shell)._2)
    }
    def withHeight(height: Int): ShellCfg = andThen { shell =>
      val (left,top) = shellLeftTop(shell)
      shell.setBounds(left, top, shellDimensions(shell)._1, height)
    }
    def withPosition(xy: (Int,Int)): ShellCfg = andThen { shell =>
      val (width,height) = shellDimensions(shell)
      shell.setBounds(xy._1, xy._2, width, height)
    }
    def withPosLeftTo(parentShell: Shell): ShellCfg = andThen { shell =>
      val (left,top)= shellRightTop(parentShell)
      val (width,height) = shellDimensions(shell)
      shell.setBounds(left, top, width, height)
    }
    def withPosLeftToParentOr(x: Int = 0, y: Int = 0): ShellCfg = andThen { shell =>
      val pos: (Int,Int) = shell.getParent() match {
        case ps: Shell => shellRightTop(ps)
        case _ => (x,y)
      }
      val (width,height) = shellDimensions(shell)
      shell.setBounds(pos._1, pos._2, width, height)
    }

object ShellContent:
  type ShellHeritage = Shell | Display | None.type // None means Display.getDefault()
  val defaultFlags = SWT.CLOSE
  val floatingFlags = SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.ON_TOP | SWT.MODELESS | SWT.CLOSE | SWT.NO_TRIM

  extension[T](self: ShellContent[T])
    def loopStandalone(display: Display): Unit =
      val shell = self.createLayoutOpen(display)
      // val display = shell.getDisplay()
      while (!shell.isDisposed() && ! display.isDisposed()) {
        if (!display.readAndDispatch())
          display.sleep();
      }
      if (! shell.isDisposed())
        shell.dispose();
      if (! display.isDisposed())
        display.dispose();

    def map[V](f: T => V): ShellContent[V] = self.copy(swtDrawer= self.swtDrawer.map(f))
case class ShellContent[+T](
  swtDrawer: SwtDrawer[T], 
  shellcfg: ShellCfg,
  flags: Int = ShellContent.defaultFlags
):
  import ShellContent.*
  def withFlagsAnd(andFlags: Int) = this.copy(flags = this.flags | andFlags)
  def withFlags(flags: Int) = this.copy(flags = flags)
  def withFloatingFlags() = this.withFlags(ShellContent.floatingFlags)

  def createLayoutOpen(heritage: ShellHeritage = None): Shell = {
    val shell = create(heritage)
    shell.layout()
    shell.open();
    shell
  }
  def create(heritage: ShellHeritage): Shell = 
    val shell = heritage match
      case parentShell: Shell     => new Shell(parentShell, this.flags)
      case parentDisplay: Display => new Shell(parentDisplay, this.flags)
      case None                   => new Shell(Display.getDefault(), this.flags)
    val made = this.swtDrawer.draw(shell)
    val cfgd = this.shellcfg.config(shell)
    shell

def currentMouseLocationSetter(s: Shell, offset: Int = 5) = {
  val cursorLoc = s.getDisplay.getCursorLocation();
  import org.eclipse.swt.graphics.Point;
  s.setLocation(new Point(cursorLoc.x + offset, cursorLoc.y + offset));
}



val openFloatingDefaultStyle = SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.ON_TOP | SWT.MODELESS | SWT.CLOSE | SWT.NO_TRIM;
def openFloating[T : RootControlContext]( // `: RootControlContext` means, there is an implicit function 
                                          // producing a `Control` from T, the root of the SWT UI it represents
  content: ShellContent[T],               // represents flags, instructs how the shell is opened, 
                                          // and paints the shell, resulting in object of type T
  maxWidth: Int = 400, 
  shellflags: Int => Int = identity,      // transforms (an OR set of) existing shell flags into 
                                          // another set of shell flags (configuration)
  d: Option[Display]=None,                // the parent display, or None to default to the global.
                                          // providing a parent shell supersedes this.
  parent: Option[Shell] = None,           // the parent shell. supersedes param `display`: 
                                          // if param `parent` is set, the display from that control is taken.
): OverlayShell[T] = {
  val display: Display = parent.map{_.getDisplay}.getOrElse(d.getOrElse(Display.getCurrent))
                                          // val shell: Shell  = parent.map(p => new Shell(parent)).getOrElse(new Shell(display));
  val style = shellflags(openFloatingDefaultStyle)
  val overlay: OverlayShell[T] = new OverlayShell( display, content.swtDrawer, maxWidth, style)
  overlay.shell.layout()
    // val cfgd = content.shellcfg.config(overlay.shell) // this 
    // alternatively: shell.setBounds(...), ...
  overlay.shell.open();
  overlay.shell.setActive
  overlay
}

class OverlayShell[T : RootControlContext]( // interesting are probably just the listeners (ShellEvent) and maybe the initial size settings
  val dsp: Display,
  val drawer: SwtDrawer[T], 
  val maxWidth: Int, style: Int) {
  val shell = new Shell(dsp, style)
  shell.glWithGrid

  implicit val rootCompositeContext: SWTLayoutRoot = SWTLayoutRoot(shell)
  val uniRoot = shell.bearUniComposite()
  uniRoot.root.glGrabFillBothNoMargin
  val content = drawer.draw(uniRoot.mainRoot)
  def contentControl = summon[RootControlContext[T]].rootOf(content)

  import org.eclipse.swt.events.{ShellEvent, ShellAdapter}
  shell.addShellListener(new ShellAdapter() {
    override def shellDeactivated(e: ShellEvent) = shell.close()
    override def shellActivated(e: ShellEvent) = {}
  })

  val openFloatingDefaultStyle = SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.ON_TOP | SWT.MODELESS | SWT.CLOSE | SWT.NO_TRIM;

  val necessarySpace = {
    val n1 = this.contentControl.computeSize(maxWidth, SWT.DEFAULT)
    this.contentControl.computeSize(SWT.DEFAULT, n1.y)
  }
  contentControl.glGrabH.glFillBoth.glWHint(necessarySpace.x).glHHint(necessarySpace.y)
}


