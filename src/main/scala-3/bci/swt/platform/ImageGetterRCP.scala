package bci.swt.platform

import bci.swt.platform.{ImageGetter, ImageSpec}
import org.eclipse.core.runtime.{FileLocator, Platform, Path as RTPath}
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.swt.graphics.Image
import org.osgi.framework.Bundle

import java.net.URL

class ImageGetterRCP extends ImageGetter {
  import ImageGetter.*
  override def getDescr(spec: ImageSpec): Option[ImageDescriptor] = {
    spec match {
      case ImageSpec.InPlugin(pluginId, path) => Option(getImageDescriptor(pluginId, path))
    }
  }

  def getImageDescriptor(PLUGIN_ID: String, filepath: String): ImageDescriptor = {
    // Get the bundle ID of the callers plugin.
    val bundle: Bundle = Platform.getBundle(PLUGIN_ID)
    val path: RTPath = new RTPath(filepath)
    val fullPathString: URL = FileLocator.find(bundle, path, null)
    val imageDesc: ImageDescriptor = ImageDescriptor.createFromURL(fullPathString)
    return imageDesc
  }
}

