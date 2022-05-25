sealed trait ExNode

import scala.deriving.Mirror
import scala.compiletime.summonAll

case object PIn extends ExNode
case object QIn extends ExNode
// sealed trait ExNodeMid extends ExNode
// case object ENM1 extends ExNodeMid
// case object ENM2 extends ExNodeMid


summon[Mirror.Of[ExNode]]

val cases = summon[Mirror.SumOf[ExNode]]
type MET = cases.MirroredElemTypes

// val x = summonAll[MET]
val products = summonAll[Tuple.Map[MET, Mirror.ProductOf]]

products

println("1: " + products)
println("2: " + (products == (PIn, QIn)))

// but the type of products is still wrapped ...

type Unproduct[PT] = PT match
 case Mirror.ProductOf[t] => t

val res: (PIn.type, QIn.type) = products.map[Unproduct]([t] => (m: t) => m.asInstanceOf[Unproduct[t]])
println(res)

println(res == (PIn, QIn))



