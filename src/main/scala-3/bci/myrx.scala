package bci.myrx

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.{List => ListWidget, *}

class IdwiseShops[T]:
  protected var shops: Map[String, Box[T]] = Map()
  protected def ensurePresent(id: String): Box[T] =
    shops.get(id) match
      case Some(box) => box
      case None => {val box = Box[T]; shops = shops + (id->box); box}
  def get(id: String): Box[T] = ensurePresent(id)

class PublisherShop[T]() extends Publisher[T]:
  var issues: List[T] = List()
  def head = issues.head
  def headOption = issues.headOption
  var subscribers: List[T => Unit] = List()
  override def subscribe(inbox: T => Unit): Unit = subscribers = subscribers :+ inbox
  def subscribeAndNotify(inbox: T => Unit): Unit =
    subscribe(inbox)
    headOption.foreach(inbox(_))
  def issue(newIssue: T) =
    this.issues = newIssue:: this.issues
    for {s <- subscribers} s(newIssue)

// applicable to buttons, text field, mouse events, links, etc...
object Box:
  def of[T](t: T) =
    val result = Box[T]()
    result.push(t)
    result
class Box[T]():
  private val shop = new PublisherShop[T]
  def pub: Publisher[T] = shop
  export shop.{head, headOption, issues}
  val sink = new MySink[T]:
    def push(t: T) = Box.this.shop.issue(t)
  export sink.push

@FunctionalInterface
trait Publisher[+T]:
  def subscribe(inbox: T => Unit): Unit
object Publisher:
  extension[T](self: Publisher[T])
    def pubmap[V](f: T => V) = new Publisher[V]:
      def subscribe(inbox: V => Unit): Unit = self.subscribe(t => inbox(f(t)))
    def pubflatmap[V](f: T => Publisher[V]) = new Publisher[V]:
      def subscribe(inbox: V => Unit): Unit = self.subscribe{ (t: T) => 
          val nested: Publisher[V] = f(t)
          nested.subscribe { v => inbox(v) }
        }

// applicable to buttons, text field, mouse events, links, etc...
@FunctionalInterface
trait MySink[-T]:
  def push(content: T): Unit

def noopPublisher[T] = new Publisher[T]:
  override def subscribe(inbox: T=>Unit): Unit = ()
def noopSink[T] = new MySink[T]:
  override def push(content: T): Unit = println(s"$content pushed into noop sink...")

//class IdSingleSubManager[T] extends Publisher[T]:
//  protected var shops: Map[String, Box[T => Unit]] = Map()
//  protected def ensurePresent(id: String): Box[T => Unit] =
//    shops.get(id) match
//      case Some(box) => box
//      case None => {val box = Box[T => Unit]; shops = shops + (id->box); box}
//  def becomeHandler(id: String, impl: T => Unit) =
//    val present: Box[T => Unit] = ensurePresent(id)
//    present.push(impl)
//  def submit(id: String)(incoming: T) = 
//    val observerOpt = ensurePresent(id).headOption
//    for { o <- observerOpt } o(incoming)
//    //TODO: somehow report events without subscriber?


