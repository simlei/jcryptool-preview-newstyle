package rsaelgamal

import org.eclipse.swt.widgets.{List as _, *}
import org.eclipse.wb.swt.SWTResourceManager
import bci.view.Parsing.*
import bci.swt.SWTLoop
import bci.swt.SwtCanvas
import bci.swt.BaseComposite
import bci.swt.HasComposite
import bci.swt.TextState
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.ModifyEvent

import zio._
import zio.stream._

import bci.metamodel.Graphs.*
import rsaelgamal.RSAModel.*
import rsaelgamal.RSAModel.paramsGraph.*

import simlei.util.*
import java.io.IOException
import java.io.StringWriter
import java.io.PrintWriter
import bci.swt.*
import bci.swt.platform.*

// here go all the hooks required that the RCP and the standalone impl. are in lockstep
// ENTRY POINT: for Eclipse RCP usage. provides all the lifecycle objects (TODO: also SWTLoop)
package rcpImpl {
  import rsaelgamal.RSAAppService.* // the view is defined there
  val platform = RCPPlatformInstance

  // classes where plugin.xml and MANIFEST.MF can point to
  class RSAMainView  extends RCPViewPartImpl (RSAMainViewService.mainViewId, platform)
  class RSAActivator extends RCPActivatorImpl(RSAMainViewService.pluginId, platform):
    override def platformSetup(platform: RCPPlatform) = this.onStartRun(rcpMain)

  // "main" method for the RCP, if launched from there
  def rcpMain(platform: RCPPlatform) = 
    given SwtPlatform = platform
    val mainView = new RSAMainViewService // registers itself
    val viewRef = new SwtViewDefinition( "rsaelgamal.RSAMainView", mainView.viewContextCreated )
    org.jcryptool.core.help.HelpInjectionService.addInjector("${RSAHELP}", () => rsaelgamal.help.make_help)
    platform.addViewImpl(viewRef)
    // platform.requestView(mainView.viewId, true) // TODO: make that a lazy instruction. currently fails in RCP "abnormal workbench condition"
}


// ENTRY POINT: for command line and IDE (work in progress)
// provides a mock RCP platform
//
// - it configures the platform e.g. with views (addViewImpl)
// - the platform in turn provides ZIOs and implicits to build the program.
// - the platform should be kept minimal — services can do the rest
@main def RSAAppServiceMain(): Unit =
  import rsaelgamal.RSAAppService.*
  val platform = DevRCPContext()
  given SwtPlatform = platform
  platform.launchedShellManager = SWTLoop.manageStandaloneShell(_) // this is the default, but for clarity...

  val mainView = new RSAMainViewService
  platform.addViewImpl(mainView.viewRef)
  // val launcherShell = platform.launchPlatformUI() // optional
  platform.requestView(RSAMainViewService.mainViewId, true) // optional
  SWTLoop.loop()

// TODO: goes somewhere else when mature
object MainViewService:
  // case class Controlled(controller: RSAUIMainController, ui: RSAUIMain, behavior: RSAGlue)
  val dummy = ()

trait MainViewService(val viewId: String):
  val dummy = ()

// --------------- above ^^^ --- platform-specific stuff and entry points for dev+rcp platforms
// --------------- below vvv --- view implementation



object RSAAppService { 

  object RSAMainViewService:
    val mainViewId = "rsaelgamal.RSAMainView"
    val pluginId   = "org.jcryptool.bci"
    // case class Created(controller: RSAUIMainController, ui: RSAUIMain, behavior: RSAGlueSpec)
    // var created: List[Created] = Nil

  class RSAMainViewService extends MainViewService(RSAMainViewService.mainViewId):
    val viewRef = new SwtViewDefinition( this.viewId, viewContextCreated )

    // this means, a client is present and it's UI is running (baseCanvas etc in SWT)
    def viewContextCreated(context: SWTViewContext) = // hook for creating the view
      val appSpec = app.Spec(context)
      val program = for {
        _ <- ZIO.succeed(println("DBG: RSA service: creating the app infrastructure"))
        instance <- appSpec.makerZ()
            _ <- ZIO.succeed(println("DBG: RSA service: running the app.runnerZ in a thread..."))
        running <- instance.runnerZ
        exitStrategy = ZIO.collectAll(running.map{_.fiber.interrupt})
            _ <- ZIO.succeed(println("DBG: RSA service: waiting for disposal of main GUI..."))
        disposalOnSWTExit <- context.parentComposite.promiseDisposal.flatMap{ _.await } *> exitStrategy
      } yield ()
      
      program.runAsyncTracedInThread()

}

