package bci.swt.platform

import bci.swt.platform.{ImageGetter, ImageSpec}
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.Path as RTPath
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.swt.graphics.Image
import org.osgi.framework.Bundle
import os.Path
import os.RelPath

import java.net.URL

class ImageGetterStandalone extends ImageGetter {
  import bci.swt.platform.ImageGetter._

  val tmpdir = os.temp.dir()
  // val tmpdir = Path("/tmp/imagegetter")
  os.makeDir.all(tmpdir)

  def extractdir_for(plugin: String) = { val result = tmpdir / plugin; os.makeDir.all(result); result }
  def extractpath_for(plugin: String, relPath: String) = {
    val result = extractdir_for(plugin) / RelPath(relPath); os.makeDir.all(result / os.up); result
  }

  def getJarFor(inPlugin: ImageSpec.InPlugin): Option[Path] = {
    // TODO: improve!
    val jars = os.list(Path("/home/snuc/sandbox/bct/BCI/jctjars/swtplus")).toList ++ os.list(Path("/home/snuc/sandbox/bct/BCI/jctjars/jctplatform")).filter(_.ext == "jar").toList
    val result = jars.filter(_.baseName.contains(inPlugin.plugin))
    result.sorted.reverse.headOption
  }

  override def getDescr(spec: ImageSpec): Option[ImageDescriptor] = {
    spec match {
      case ip@ImageSpec.InPlugin(pluginId, path) => getIDFromMyPath(ip)
    }
  }

  def getIDFromMyPath(ip: ImageSpec.InPlugin): Option[ImageDescriptor] = {
    val jarpath = getJarFor(ip)
    jarpath match {
      case None => None
      case Some(path) => getImageDescriptorFromJar(ip, path)
    }
  }

  def unzip(zipPath: Path, subPath: String, target: Path): Option[Path] = {
    import java.io.{FileInputStream, FileOutputStream}
    import java.util.zip.ZipInputStream
    val fis = new FileInputStream(zipPath.toIO)
    val zis = new ZipInputStream(fis)
    val entry = Stream.continually(zis.getNextEntry).takeWhile(_ != null).map{ file =>
      file.getName == subPath match {
        case false => None
        case true => Some(file)
      }
    }.flatten.headOption
    val result = entry.map { e =>
      val fout = new FileOutputStream(target.toIO)
      val buffer = new Array[Byte](1024)
      Stream.continually(zis.read(buffer)).takeWhile(_ != -1).foreach(fout.write(buffer, 0, _))
      zis.close
      fis.close
      fout.flush
      fout.close
      target
    }
    result.isEmpty match {
      case true => println(s"could not find $subPath in $zipPath")
      case _ => ()
    }
    result
  }

  def getImageDescriptorFromJar(ip: ImageSpec.InPlugin, jarpath: Path): Option[ImageDescriptor] = {
    val targetPath = extractpath_for(ip.plugin, ip.path)
    val unzipped = os.exists(targetPath) match {
      case true => Some(targetPath)
      case false => unzip(jarpath, ip.path, targetPath)
    }
    unzipped map { u =>
      val url = URL.apply("file://" + u)
      ImageDescriptor.createFromURL(url)
    }
    // ImageDescriptor.createFromFile(getClass, tmpfile)
  }


}

