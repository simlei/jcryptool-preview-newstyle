package bci.swt.platform

import bci.swt.{*, given}
import bci.swt.widgets.*
// import bci.controller.*
import org.eclipse.swt.widgets.{List => _, *}
import org.eclipse.swt.SWT

// the following simulates the functions of an RCP
object DevRCPContextInstance extends DevRCPContext()

// TODO: Rename w.r.t. being single-view-specific
case class ShellDevContext(val shell: Shell, idOfView: String) extends SWTViewContext:
  override val parentComposite = shell
  override val platform = DevRCPContextInstance
  override def viewId = idOfView
  override def baseCanvas: SwtCanvas[BaseComposite] = 
    SwtCanvas.of(shell).map(SWTViewContext.mainViewStump)


// TODO: rename to DevRCPPlatform
class DevRCPContext() extends SwtPlatform:
  
  override def imageGetter = ImageGetterStandalone()
  var launcherShells: List[Shell] = Nil
  def newestLauncherShell = launcherShells.headOption

  var launchedShellManager: Shell => Unit = SWTLoop.manageStandaloneShell(_)

  // only for the dev api
  def launchPlatformUI() =
    val launcherShellCfg = ShellCfg
      .gridDefault
      .withPosition((250,0))
      .withDimensions((400,100))
    val launcherSpec = viewSpecs.foldLeft(LauncherSpec())((l, spec) => 
        l.withLabelledAction(spec.title)(
          requestView(spec.id, true)
        ))
    val launcherShellSpec = launcherSpec.drawer.asShellSpec(launcherShellCfg)
    val shell = launcherShellSpec.create(None)
    this.launcherShells = shell :: this.launcherShells
    shell.layout()
    shell.open()
    shell.addListener(SWT.Dispose, {ev => this.launcherShells = this.launcherShells.filterNot{_ == shell}})
    launchedShellManager(shell)
    shell

  def openView(viewDefinition: SwtViewDefinition) =
    val heritage: Shell | None.type = None
    // val heritage: Shell | None.type = newestLauncherShell.getOrElse(None)
    val cfg = ShellCfg
      .gridDefault
      .withTitle(viewDefinition.title)
      .withPosLeftToParentOr(500,0)
      .withDimensions((1200,900))
    val created = cfg.asShellSpec(_ => ()).create(heritage)
    // val created = viewDefinition.drawer.asShellSpec(cfg).create(heritage)
    val context = ShellDevContext(created, viewDefinition.id)
    this.viewContexts.arrived.push(context)
    created.layout()
    created.open()
    launchedShellManager(created)
    

  override def requestView(id: String, force: Boolean = false) =
    findViewSpecWithId(id) match
      case None => println(s"ERROR: view $id requested but no handler present")
      case Some(spec) => openView(spec)
  
