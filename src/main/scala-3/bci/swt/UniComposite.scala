package bci.swt

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*
import org.eclipse.wb.swt.SWTResourceManager;

import bci.*
import rsaelgamal.RSAModel.*


class UniComposite(val parent: Composite)(using rcc: SWTLayoutRoot) {
  import UniComposite.*

  val holeSwtStyle = SWT.NONE
  val rootSwtStyle = SWT.NONE
  // val holeSwtStyle = SWT.BORDER
  // val rootSwtStyle = SWT.BORDER

  val root = parent.bearComposite(rootSwtStyle).glNoMargins.glSpacingBoth(0).glColumns(3).glWithGrid
  val topRoot = root.bearComposite(holeSwtStyle).glSpanH(3).glWithGrid.glFillBoth.glNoMargins
  val leftRoot = root.bearComposite(holeSwtStyle).glWithGrid.glFillBoth.glNoMargins
  val mainRoot = root.bearComposite(holeSwtStyle).glWithGrid.glGrabBoth.glFillBoth.glNoMargins
  val rightRoot = root.bearComposite(holeSwtStyle).glWithGrid.glFillBoth.glNoMargins
  val bottomRoot = root.bearComposite(holeSwtStyle).glSpanH(3).glWithGrid.glFillBoth.glNoMargins

  setVisible(Hole.values.toSeq.filter(_ != Hole.Main): _*)(false)

  def layoutOnRoot(holes: Hole*) = {
    // TODO: layout efficiency and correctness
    rcc.root.layout(holes.map{_.getter(this)}.flatMap{_.collectControlTree()}.distinct.toArray)
    rcc.root.layout()
  }
  def setVisibleWithoutLayout(holes: Hole*)(b: Boolean) = {
    for { hole <- holes } hole.getter(this).glVisible(b); 
  }
  def setVisible(holes: Hole*)(b: Boolean) = {
    setVisibleWithoutLayout(holes:_*)(b)
    layoutOnRoot(holes:_*)
  }
}

object UniComposite {
  enum Hole(val getter: UniComposite => Composite) {
    case Top extends Hole(_.topRoot)
    case Bottom extends Hole(_.bottomRoot)
    case Left extends Hole(_.leftRoot)
    case Right extends Hole(_.rightRoot)
    case Main extends Hole(_.mainRoot)

    // def setVisibleAndLayout(uni: UniComposite)(b: Boolean)(using rcc: RootCompositeContext) = {
    //   getter(uni).glVisible(b)
    //   rcc.root.layout(Array[Control](getter(uni)))
    //   rcc.root.layout()
    // }
  }
}

