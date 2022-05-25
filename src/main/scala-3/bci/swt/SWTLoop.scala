package bci.swt
import org.eclipse.swt.widgets.{List as _, *}
import org.eclipse.swt.SWT

import bci.swt.given

object SWTLoop {

  private var loopedShells: List[Shell] = List()
  private var loopIsLooping = false
  // makes the current thread the SWT thread
  def loop() = 
    if (loopIsLooping) throw new RuntimeException("SWT thread is already looping...")
    if (detectDifferentSWTThreadStarted) {
      throw new RuntimeException("SWT thread is already looping...")
    }
    loopedShells = loopedShells.filterNot{_.isDisposed()}
    loopIsLooping = true
    while(! loopedShells.isEmpty)
      for {presentShell <- loopedShells}
        presentShell.isDisposed() match
          case true => ()
          case false =>
            val display = presentShell.getDisplay()
            if (! display.isDisposed())
              if (! display.readAndDispatch())
                display.sleep()
      loopedShells = loopedShells.filterNot{_.isDisposed()}
    for {s <- loopedShells}
      if (! s.isDisposed())
        s.dispose();
      val d = s.getDisplay()
      if (! d.isDisposed())
        d.dispose();
    loopIsLooping = false
    println("finished SWT loop because all managed shells are disposed")

  def detectDifferentSWTThreadStarted =
    false

    // TODO: detect RCP threads and handle correctly
  private def addShellToLoop(shell: Shell) = 
    loopedShells = loopedShells :+ shell
    if (detectDifferentSWTThreadStarted)
      // TODO: may not work without adjustments, not much tested...
      // e.g. would this shell maybe be closed with other shells...?
      shell.getDisplay().syncExec { () =>
        shell.layout()
        shell.open()
      }
    else
      ()

  def expectSomeShell(timeout: Int = 3000) =
    val timeAtStart = System.nanoTime
    while(this.loopedShells.isEmpty) {
      val timeElapsed = (System.nanoTime - timeAtStart) / 1000000
      if (timeElapsed > timeout)
        throw new RuntimeException("waiting for some shell to be registered for too long")
    }

  def manageStandaloneShell(shell: Shell) =
    loopedShells.contains(shell) match
      case true => ()
      case false => addShellToLoop(shell)
}

