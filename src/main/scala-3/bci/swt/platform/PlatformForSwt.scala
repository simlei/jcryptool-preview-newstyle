package bci.swt.platform
import bci.myrx.Box
import bci.swt.{*, given}
import org.eclipse.swt.widgets.{Composite, Shell}
import org.osgi.framework.BundleContext

object SwtPlatform
trait SwtPlatform {
  protected var viewSpecs: List[SwtViewDefinition] = List()
  def findViewSpecWithId(id: String): Option[SwtViewDefinition] = this.viewSpecs.filter(_.id == id).headOption

  val viewContexts = new ViewContextsManager
  viewContexts.arrived.pub.subscribe { ctx =>
    val spec = viewSpecs.filter(_.id == ctx.viewId).head
    spec.callback(ctx)
  }

  // API:
  def addViewImpl[UIType](spec: SwtViewDefinition) =
    if (findViewSpecWithId(spec.id).isDefined)
      throw new RuntimeException(s"currently, only no duplicate-id specs allowed; but found one present already for id=${spec.id}")
    this.viewSpecs = this.viewSpecs :+ spec

  def requestView(id: String, force: Boolean = false): Unit

  def imageGetter: ImageGetter
  given ImageGetter = this.imageGetter
}

class SwtViewDefinition(
  val id: String, 
  val callback: (SWTViewContext) => Unit
):
  def title = s"View: $id"


// rename: SwtViewContext
object SWTViewContext:
  given HasComposite[SWTViewContext] = context => HasComposite.in(context.baseCanvas)
  def mainViewStump[C: HasComposite](c: C) =
    val base = BaseComposite(HasComposite.in(c)); base.root.glGrabFillBoth; base // TODO: factor out grid stuff

trait SWTViewContext {
  val platform: SwtPlatform
  def viewId: String
  val parentComposite: Composite // either the shell, or the viewPart parent
  def baseCanvas: SwtCanvas[BaseComposite]
  // export platform.given
}

case class BundleActivated (
  activator: RCPActivatorImpl, 
  bundle: BundleContext
  )

// depends on other manager's publishers
// for handling standalone views
class ViewContextsManager:
  val arrived = Box[SWTViewContext]
  def contextInstantiated(context: SWTViewContext) =
    arrived.push(context)


