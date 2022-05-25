package bci.swt.platform

import bci.myrx.Box
import bci.swt.*
import bci.swt.platform.*
import org.eclipse.swt.widgets.{Control, Shell}
import org.osgi.framework.BundleContext
import org.eclipse.ui.part.ViewPart
import org.eclipse.swt.widgets.Composite

trait RCPPlatform extends SwtPlatform:
  override def requestView(id: String, force: Boolean = false): Unit =
    RCPPlatformMethods.show(id, force)
  val activators = new ActivatorsManager
  val viewParts = new ViewPartsManager
  viewParts.arrived.pub.pubflatmap{vp => vp.created.pub}.subscribe{ vpc =>
    val ctx = RCPViewContext(vpc)
    this.viewContexts.arrived.sink.push(ctx)
  }

object RCPPlatformInstance extends RCPPlatform:
  override def imageGetter = new ImageGetterRCP

case class RCPViewContext(val vpc: ViewPartComposite) extends SWTViewContext:
  override val parentComposite = vpc.partParent
  override val platform = RCPPlatformInstance
  override def viewId = vpc.part.id
  override def baseCanvas: SwtCanvas[BaseComposite] = 
    SwtCanvas.of(vpc).map(SWTViewContext.mainViewStump)

class ActivatorsManager:
  val arrived = Box[RCPActivatorImpl]
  def activatorInstantiated(activator: RCPActivatorImpl) =
    arrived.push(activator)

class ViewPartsManager:
  val arrived = Box[RCPViewPartImpl]
  def viewInstantiated(impl: RCPViewPartImpl) = 
    arrived.push(impl)

object ViewPartComposite:
  given rcc: HasComposite[ViewPartComposite] = _.partParent
case class ViewPartComposite (
  part: RCPViewPartImpl, 
  partParent: Composite
  )

trait RCPViewPartImpl(val id: String, platform: RCPPlatform) extends ViewPart:
  val created = Box[ViewPartComposite]
  override def createPartControl(parent: Composite): Unit = 
    created.push(ViewPartComposite(this, parent))
  // ------------------------------------------------------
  val focused = Box[Unit]
  override def setFocus(): Unit = focused.push(())
  // ------------------------------------------------------
  // println("RCP view part signal sent...")
  platform.viewParts.arrived.sink.push(this)

// "org.jcryptool.bci"
trait RCPActivatorImpl(val pluginId: String, platform: RCPPlatform) extends org.eclipse.ui.plugin.AbstractUIPlugin:
  val started = Box[BundleActivated]
  def platformSetup(platform: RCPPlatform) = {}
  def onStartRun(mainFun: RCPPlatform => Unit) = platform
    .activators.arrived.pub.pubflatmap{act => act.started.pub}
    .subscribe{ (b: BundleActivated) => mainFun(platform) }

  override def start(context: BundleContext) = started.push(BundleActivated(this, context))
  // ------------------------------------------------------
  val stopped = Box[BundleContext]
  override def stop (context: BundleContext) = stopped.push(context)
  // ------------------------------------------------------
  
  this.platformSetup(platform) // hook to let implementors add their hooks to the system before events get fired. Probably should setup only w.r.t. separation of concerns!
  platform.activators.arrived.sink.push(this)


import org.eclipse.ui.PlatformUI
import org.eclipse.ui.part.ViewPart
import org.eclipse.ui.IViewPart

object RCPPlatformMethods {
        // bci.rcp.view.ViewsManager.storeViewRef.get().setInput(bci.bc.BCParams.IV(Array(1, 2)));

    def getIfActive(id:String): Option[IViewPart] = Option(
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(id)
    )
    def get(id:String): IViewPart = getIfActive(id).getOrElse(
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id)
      )
    def show(id: String, force: Boolean = false): IViewPart = getIfActive(id).getOrElse(
      // TODO: force is ignored!
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id)
      )

   case class ViewRef[VT <: ViewPart](id: String) {
     def getIfActive: Option[VT] = Option(
       PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(id).asInstanceOf[VT]
     )
     def get(): VT = {
       getIfActive.getOrElse(
         PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id).asInstanceOf[VT]
       )
     }
   }

}
