import scala.deriving as drv
import scala.deriving.Mirror
import scala.compiletime as ct

sealed trait ExNode
case object EN1 extends ExNode
case object EN2 extends ExNode
sealed trait ExNodeMid extends ExNode
case object ENM1 extends ExNodeMid
case object ENM2 extends ExNodeMid

import magnolia1.*
import magnolia1.CaseClass.Param
// import com.github.arturopala.tree.*

trait MPrint[T] {
  extension (x: T) def print: String
}
object MPrint extends AutoDerivation[MPrint]:
  def join[T](ctx: CaseClass[Typeclass, T]): MPrint[T] = (value: T) =>
    ctx.params.map { (param: Param[Typeclass, T]) =>
      println(s"value: ${value}")
      println(s"ctx: ${ctx}")
      println(s"param: ${param}")
      println(s"param.typeclass: ${ param.typeclass }")
      param.typeclass.print(param.deref(value))
    }.mkString(s"${ctx.typeInfo.short}(", ",", ")")

  override def split[T](ctx: SealedTrait[MPrint, T]): MPrint[T] = value =>
    ctx.choose(value) { sub => sub.typeclass.print(sub.cast(value)) }
  
  given MPrint[Int] = _.toString


enum MTree[+T] derives MPrint:
  case MBranch(left: MTree[T], right: MTree[T])
  case MLeaf(value: T)
given MPrint[ExNode] = (subject: ExNode) => s"ExNodeTop: ${subject.toString}"


import MTree.*
val t1 = MBranch(MLeaf(EN1), MBranch(MLeaf(EN2), MLeaf(ENM1)))
val t2 = MBranch(MLeaf(EN2), MBranch(MLeaf(EN1), MLeaf(ENM2)))
val t3 = MBranch(MLeaf(1), MBranch(MLeaf(2), MLeaf(3)))

println(">< ".repeat(30))
println(t3.print)
println("<> ".repeat(30))

println(">< ".repeat(30))
println(t1.print)
println("<> ".repeat(30))
