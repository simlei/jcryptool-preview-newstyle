package bci.swt.widgets

import org.eclipse.swt.widgets.{Control, Shell, Text, Label, Button, Composite, Display, Group}
import org.eclipse.swt.SWT
import org.eclipse.swt.layout._

import bci.swt.{*, given}

object LauncherSpec:
  type Action = (String, String, Shell=>Unit)
  def of(actions: Action*) = LauncherSpec(actions.toList)
// case class LauncherSpec(display: Display, cfg: ShellCfg, actions: List[LauncherSpec.Action] = Nil):
case class LauncherSpec(actions: List[LauncherSpec.Action] = Nil):
  // val stump: SwtCanvas[ActionGUI] = SwtCanvas.of
  val drawer: SwtDrawer[ActionGUI] = SwtDrawer.of{ ActionGUI(_) }.map{ actionGUI =>
    actions.foreach { case (lbl,cat,action) =>
      actionGUI.addAction(lbl, cat)(action(actionGUI.root.getShell()))
    }
    actionGUI
  }
  def withLabelledSAction(label: String, cat: String = "Default")(action: Shell => Unit) = this.copy(
    actions = actions :+ (label, cat, action)
  )
  def withLabelledAction(label: String, cat: String = "Default")(action: => Unit) = this.copy(
    actions = actions :+ (label, cat, (agui: Shell) => action)
  )

object ActionGUI:
  given HasComposite[ActionGUI] = _.root
class ActionGUI(val parent: Composite) {
  var actions: Map[String, () => Unit] = Map()
  var groups: Map[String, Group] = Map()
  var buttons: Map[String, Button] = Map()
  val root = parent.bearComposite().glFillBoth.glGrabBoth

  def getGroup(cat: String): Group = {
    groups.getOrElse(cat, {
        val newgroup = root.bearGroup().glColumns(4).glFillH.glGrabH.glWithGrid.withText(cat)
        groups = groups + (cat -> newgroup)
        newgroup
      }
    )
  }

  // TODO: rename "withAction"
  def addAction(label: String, cat: String = "Default")(f: => Unit): ActionGUI = {
    this.actions = actions + (label -> Thunk(f))
    this.buttons = this.buttons + ( label -> getGroup(cat).bearButton().withText(label).withAction(f) )
    this
  }

}

