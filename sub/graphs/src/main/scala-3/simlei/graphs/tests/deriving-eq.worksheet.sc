import scala.deriving as drv
import scala.deriving.Mirror
import scala.compiletime as ct
// import scala.compiletime.erasedValue

println("-".repeat(30))
println("hello world")
println("-".repeat(30))
sealed trait ExNode
case object EN1 extends ExNode
case object EN2 extends ExNode
sealed trait ExNodeMid extends ExNode
case object ENM1 extends ExNodeMid
case object ENM2 extends ExNodeMid

inline def summonAllEq[T <: Tuple]: List[Eq[_]] =
  inline ct.erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts) => ct.summonInline[Eq[t]] :: summonAllEq[ts]

trait Eq[T]:
  def eqv(x: T, y: T): Boolean
object Eq:
  def check(elem: Eq[_])(x: Any, y: Any): Boolean =
      elem.asInstanceOf[Eq[Any]].eqv(x, y)

  def iterator[T](p: T) = p.asInstanceOf[Product].productIterator

  def eqSum[T](s: Mirror.SumOf[T], elems: => List[Eq[_]]): Eq[T] =
    new Eq[T]:
      def eqv(x: T, y: T): Boolean =
        val ordx = s.ordinal(x)
        (s.ordinal(y) == ordx) && check(elems(ordx))(x, y)

  def eqProduct[T](p: Mirror.ProductOf[T], elems: => List[Eq[_]]): Eq[T] =
    new Eq[T]:
      def eqv(x: T, y: T): Boolean =
        iterator(x).zip(iterator(y)).zip(elems.iterator).forall {
          case ((x, y), elem) => check(elem)(x, y)
        }

  inline given derived[T](using m: Mirror.Of[T]): Eq[T] =
    lazy val elemInstances = summonAllEq[m.MirroredElemTypes]
    inline m match
      case s: Mirror.SumOf[T]     => eqSum(s, elemInstances)
      case p: Mirror.ProductOf[T] => eqProduct(p, elemInstances)
end Eq

// generated: given [T: Eq]     : Eq[Tree]     = Eq.derived
enum Tree[T] derives Eq:
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

