package bci.swt.platform

import org.eclipse.core.runtime.{FileLocator, Platform, Path as RTPath}
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.swt.graphics.Image
import org.osgi.framework.Bundle
import os.{Path, RelPath}

import java.net.URL

trait ImageGetter {
  import ImageGetter.*
  def getDescr(spec: ImageSpec): Option[ImageDescriptor]
  def getImg(spec: ImageSpec): Option[Image] = getDescr(spec).map{_.createImage}
  def getImgOrNull(spec: ImageSpec): Image = getImg(spec).getOrElse(null)
}
enum ImageSpec {
  case InPlugin(plugin: String, path: String)
}
object ImageGetter {

  val ICON_INFO                  = ImageSpec.InPlugin("org.eclipse.jface", "icons/full/message_info.png")
  val ICON_WARNING               = ImageSpec.InPlugin("org.eclipse.ui", "icons/full/obj16/warn_tsk.png")
  val ICON_ERROR                 = ImageSpec.InPlugin("org.eclipse.ui", "icons/full/obj16/error_tsk.png")
  val ICON_HELP                  = ImageSpec.InPlugin("org.eclipse.ui", "icons/full/etool16/help_contents.png")
  val ICON_RESET                 = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/icon_reset.png")
  val ICON_VISUALIZATIONS        = ImageSpec.InPlugin("org.eclipse.ui", "icons/full/eview16/defaultview_misc.png")
  val ICON_ANALYSIS              = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/analysis_icon.gif")
  val ICON_CHECKBOX              = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/check.png")
  val ICON_SEARCH                = ImageSpec.InPlugin("org.eclipse.ui", "icons/full/etool16/search.png")
  val ICON_RUN                   = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/run_exc.png")
  val ICON_FILE                  = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/fileType_filter.png")  
  val ICON_NOTFOUND              = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/red_square.png")
  val ICON_PERSPECTIVE_STANDARD  = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/Perspective_Standard.png")
  val ICON_PERSPECTIVE_ALGORITHM = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/Perspective_Algorithm.png")
  val ICON_GAMES                 = ImageSpec.InPlugin("org.jcryptool.core.util", "icons/games.png")

}



