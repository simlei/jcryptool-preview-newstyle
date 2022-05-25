package bci

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

class Activator() extends AbstractUIPlugin {
  val PLUGIN_ID: String = "org.jcryptool.bci"; //$NON-NLS-1$
  override def start(context: BundleContext) = {
    super.start(context);
    Activator.inst = this;
  }
  override def stop(context: BundleContext) = {
    Activator.inst = null;
    super.stop(context);
  }
}
object Activator {
  var inst: Activator = null // TODO: null ref
}

