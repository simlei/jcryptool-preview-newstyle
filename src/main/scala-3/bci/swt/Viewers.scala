package bci.swt

import cats.Show
import org.eclipse.jface.action.Action
import org.eclipse.jface.viewers.{ITableLabelProvider, LabelProvider}
import org.eclipse.swt.graphics.Image
import org.eclipse.ui.{ISharedImages, PlatformUI}

import scala.reflect.ClassTag

object Viewers {


  def action(label: String)(thunk: => Unit): Action = {
    val result = new Action() {
      override def run(): Unit = thunk
    }
    result.setText(label)
    result.setImageDescriptor(PlatformUI.getWorkbench.getSharedImages.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK))
    result
  }

  class ToStringViewLabelProvider[Data]() extends LabelProvider with ITableLabelProvider {
    override def getColumnText(obj: Object, index: Int): String = getText(obj);

    override def getColumnImage(obj: Object, index: Int): Image = getImage(obj);

    override def getImage(obj: Object): Image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
  }

  class ViewLabelProvider[Data](renderer: Show[Data]) extends LabelProvider with ITableLabelProvider {
    override def getColumnText(obj: Object, index: Int): String = {
      println(s"requested show for $obj}")
      renderer.show(obj.asInstanceOf[Data])
    }

    override def getColumnImage(obj: Object, index: Int): Image = getImage(obj);

    override def getImage(obj: Object): Image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
  }

  case class BiMap[A, B](elems: (A, B)*) {
    def groupBy[X, Y](pairs: Seq[(X, Y)]) = pairs groupBy {
      _._1
    } mapValues {
      _.map {
        _._2
      }.toSet
    }

    val (left, right) = (groupBy(elems), groupBy(elems map {
      _.swap
    }))

    def forth(key: A) = left(key)

    def back[C: ClassTag](key: B) = right(key)
  }
}
