sealed trait Knot
case object Knot1 extends Knot
case object Knot2 extends Knot

// type X[A] = A match
//   case Knot1.type => Int
//   case Knot2.type => Int
//   case Knot => Int | String
// summon[X[Knot] <:< Int | String] // error: cannot prove...

//   // While this was very insightful to ponder... There should be a way to reduce and prove subtype relation for such sealed hierarchies — but I don't seem to find it.
//   // Something like the `_` case, but it would be required to reduce to a supertype of all the other branched types. Not sure if possible though.

// type Foo[X] = X match {
//   case 1 => String
//   case Int => List[String]
// }

type X[A <: Knot] <: Int | String = A match
  case Knot1.type => Int
  case Knot2.type => String
summon[X[Knot] <:< Int | String] // error: cannot prove...

   // Cannot prove that App.this.X[App.this.Knot] <:< Int.
   // Note: a match type could not be fully reduced:
   //   trying to reduce  App.this.X[App.this.Knot]
   //   failed since selector  App.this.Knot
   //   does not match  case App.this.SoleNode1 => Int
   //   and cannot be shown to be disjoint from it either.


sealed trait Root
final class Leaf extends Root


// // narrow -> wide order of branches (intuitive)
// type Select1[T] = T match
//   case Leaf => Int
//   case Root => Int

// summon[Select1[Leaf] =:= Int] // does typecheck (expected)
// summon[Select1[Root] =:= Int] // doesn't typecheck (and confuses conformance proofs while overriding types)

// // wide -> narrow order of branches (counterintuitive & bad, but may be a confused "solution" of a user)
// type Select2[T] = T match
//   case Root => String
//   case Leaf => Int

// summon[Select2[Leaf] =:= Int]    // doesn't typecheck while the intention may be, that it does
// summon[Select2[Root] =:= String] // typechecks (expected)...
// summon[Select2[Leaf] =:= String] // does typecheck while the intention may be, that it doesn't!!

