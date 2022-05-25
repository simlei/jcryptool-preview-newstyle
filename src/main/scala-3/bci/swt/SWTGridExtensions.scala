package bci.swt
import org.eclipse.swt.widgets.{List => ListWidget, *}
import org.eclipse.swt.SWT
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

trait SWTGridCompositeExtensions[T <: Composite] { self: CompositeExtensionsMixins[T] =>
  // this refers to "itself". T is a Control (see above), but the specific type must remain T
  def thisComp: T = self.mixedInAsComp
  def testeComp: String = "Hi from compMixin: " + thisComp
  def gridLayout: GridLayout = {
    val l = thisComp.getLayout()
    l match {
      case gl: GridLayout => gl
      case _ => {
        thisComp.setLayout(new GridLayout())
        this.gridLayout
      }
    }
  }
  def onGridLayout(action: GridLayout => Unit) = {
    thisComp.onControl { ctrl => 
      val l = ctrl.gridLayout
      action(l)
      thisComp.setLayout(l)
    }
  }
  def glWithGrid = {gridLayout; thisComp}
  def glColumns(nr: Int) = onGridLayout {_.numColumns = nr}
  def glSpacingBoth(spacing: Int) = onGridLayout {l => l.horizontalSpacing = spacing; l.verticalSpacing = spacing}
  def glMarginsBoth(margin: Int) = onGridLayout {l => l.marginWidth = margin; l.marginHeight = margin}
  def glGrabFillBothNoMargin = thisComp.glNoMargins.glGrabBoth.glFillBoth
  def glNoMargins = glMarginsBoth(0)
}

trait SWTGridControlExtensions[T <: Control] { self: ControlExtensionsMixins[T] =>
  // this refers to "itself". T is a Control (see above), but the specific type must remain T
  def thisCtrl: T = self.mixedInAsCtrl
  def testeCtrl: String = "Hi from ctrlMixin: " + thisCtrl
  def gridData: GridData = {
    val ldata = thisCtrl.getLayoutData()
    ldata match {
      case gd: GridData => gd
      case _ => {
        thisCtrl.setLayoutData(new GridData())
        this.gridData
      }
    }
  }
  def onGridData(action: GridData => Unit) = {
    thisCtrl.onControl { ctrl =>
      val d = ctrl.gridData
      action(d)
      thisCtrl.setLayoutData(d)
    }
  }
  def glAlignRightH = onGridData {d => d.horizontalAlignment = SWT.CENTER}
  def glAlignRightV = onGridData {d => d.verticalAlignment = SWT.CENTER}
  def glAlignCenterH = onGridData {d => d.horizontalAlignment = SWT.CENTER}
  def glAlignCenterV = onGridData {d => d.verticalAlignment = SWT.CENTER}

  def glFillBoth = onGridData {d => d.horizontalAlignment = SWT.FILL; d.verticalAlignment = SWT.FILL}
  def glFillH = onGridData {d => d.horizontalAlignment = SWT.FILL}
  def glFillV = onGridData {d => d.verticalAlignment = SWT.FILL}

  def glGrabBoth = onGridData {d => d.grabExcessHorizontalSpace = true; d.grabExcessVerticalSpace = true}
  def glGrabH = onGridData {d => d.grabExcessHorizontalSpace = true}
  def glGrabV = onGridData {d => d.grabExcessVerticalSpace = true}

  def glWHint(v: Int) = onGridData {d => d.widthHint = v}
  def glHHint(v: Int) = onGridData {d => d.heightHint = v}

  def glSpanH(num: Int) = onGridData(d => d.horizontalSpan = num)
  def glSpanV(num: Int) = onGridData(d => d.verticalSpan = num)

  def glGrabFillBoth = glGrabBoth.glFillBoth
  def glGrabFillH = glGrabH.glFillH
  def glGrabFillV = glGrabV.glFillV

  def glExclude(b: Boolean) = onGridData(d => d.exclude = b)
  def glVisible(b: Boolean) = {
    thisCtrl.setVisible(b)
    thisCtrl.glExclude(! b)
  }

  def glVisibleAndLayout(b: Boolean)(using rcc: SWTLayoutRoot) = {
    rcc.root.collectControlTree().contains(thisCtrl) match
      case false => throw new RuntimeException(s"control to layout $thisCtrl is not a member of its SWTLayoutRoot tree $rcc")
      case true => ()
    thisCtrl.glVisible(b)
    val composites = (thisCtrl.collectControlTree() ++ thisCtrl.collectControlPathUpward(Some(rcc.root)))
    // TODO "withoutRoot"
    val compositesUpwardOfRoot = rcc.root.collectControlPathUpward()
    val compositesWithoutUp = composites.filterNot{compositesUpwardOfRoot.contains(_)}
    val compositesWithoutUpAndRoot = compositesWithoutUp.filterNot{_ == rcc.root}
    val nums = List(composites.toList, compositesUpwardOfRoot.toList, compositesWithoutUp.toList, compositesWithoutUpAndRoot.toList).map{_.size}
    rcc.root.layout(compositesWithoutUpAndRoot)
    rcc.root.layout(compositesWithoutUpAndRoot)
    // TODO: cruft. why is this necessary? reverse order? only in RCP (cut offs...)
    rcc.root.layout()
    Option(rcc.root.getParent).foreach{_.layout} // TODO: crutch: RootCompositeContext of RCP doesn't quite work??? multiple layouts...
  }
  def glLayoutChanged(layoutRoot: Composite) = layoutRoot.layout(Array[Control](thisCtrl))
}
