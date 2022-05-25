import scala.deriving as drv
import scala.deriving.Mirror
import scala.compiletime as ct

sealed trait ExNode
case object EN1 extends ExNode
case object EN2 extends ExNode
sealed trait ExNodeMid extends ExNode
case object ENM1 extends ExNodeMid
case object ENM2 extends ExNodeMid

inline def summonAllShow[T <: Tuple]: List[Show[_]] =
  inline ct.erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts) => ct.summonInline[Show[t]] :: summonAllShow[ts]

trait Show[T]:
  def show(x: T): String
object Show:
  // given Show[Int] with
  //   def eqv(x: Int, y: Int) = x == y
  // def check(elem: Show[_])(x: Any, y: Any): Boolean =
  //   elem.asInstanceOf[Show[Any]].eqv(x, y)

  def iterator[T](p: T) = p.asInstanceOf[Product].productIterator

  def showSum[T](s: Mirror.SumOf[T], elems: => List[Show[_]]): Show[T] =
    new Show[T]:
      override def show(x: T): String = ???
        // val ordx = s.ordinal(x)
        // (s.ordinal(y) == ordx) && check(elems(ordx))(x, y)

  def showProduct[T](p: Mirror.ProductOf[T], elems: => List[Show[_]]): Show[T] =
    new Show[T]:
      override def show(x: T): String = ???
        // iterator(x).zip(iterator(y)).zip(elems.iterator).forall {
        //   case ((x, y), elem) => check(elem)(x, y)
        // }

  inline given derived[T](using m: Mirror.Of[T]): Show[T] =
    lazy val elemInstances = summonAllShow[m.MirroredElemTypes]
    inline m match
      case s: Mirror.SumOf[T]     => showSum(s, elemInstances)
      case p: Mirror.ProductOf[T] => showProduct(p, elemInstances)
end Show

// generated: given [T: Eq]     : Eq[Tree]     = Eq.derived
enum Tree[T] derives Show:
  case Branch(left: Tree[T], right: Tree[T])
  case Leaf(elem: T)

sealed trait X
object Y extends X
val of = summon[Mirror.Of[X]]
val sum = summon[Mirror.SumOf[X]]
println(sum)

// // val product = summon[Mirror.ProductOf[ExNode]]

// val x = summonAll[of.MirroredElemTypes]
// val x = summonAll[Mirror.ProductOf[]]

