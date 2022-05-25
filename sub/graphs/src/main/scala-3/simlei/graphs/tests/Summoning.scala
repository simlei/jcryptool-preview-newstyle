package simlei.graphs.tests

object Summoning:
  @main def SummoningMain(): Unit =

    import scala.deriving.Mirror
    import scala.compiletime.summonAll

    println("-".repeat(30))
    println("hello world")
    println("-".repeat(30))
    sealed trait ExNode
    case object EN1 extends ExNode
    case object EN2 extends ExNode
    sealed trait ExNodeMid extends ExNode
    case object ENM1 extends ExNodeMid
    case object ENM2 extends ExNodeMid

    trait Show[T]:
      def show(t: T): String
    object Show:
      def derived[T](using Mirror.Of[T]): Show[T] = ???
    end Show

    // generated: given [T: Show]     : Show[Tree]     = Show.derived
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
  end SummoningMain
end Summoning

