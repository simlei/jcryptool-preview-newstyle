
sealed trait Key
object K1 extends Key
object K2 extends Key

type Err[K <: Key] /*<: Errs*/ = K match // commented seems optional
  case K1.type => E1[K]
  case K2.type => E2.type

sealed trait Errs
case class E1[K <: Key](k:K) extends Errs
case object E2 extends Errs

// summon[E1[K1.type] <:< Err[Key]] // doesn't work!
summon[E1[K1.type] <:< Errs] // works!



// match type subjects
sealed trait K
object Key1 extends K
object Key2 extends K

sealed trait EE
case class EE1[K <: Key](k:K) extends EE
case object EE2 extends EE
case object EEDefault extends EE
type MT[KT <: K]  = KT match
  case Key1.type => EE1[K]
  case Key2.type => EE2.type
  case K => EEDefault.type

summon[EE1[K1.type] <:< MT[K]]

   // Cannot prove that App.this.EE1[App.this.K1.type] <:< App.this.MT[App.this.K].
   // Note: a match type could not be fully reduced:
   //   trying to reduce  App.this.MT[App.this.K]
   //   failed since selector  App.this.K
   //   does not match  case App.this.Key1.type => App.this.E1[App.this.K]
   //   and cannot be shown to be disjoint from it either.
   //   Therefore, reduction cannot advance to the remaining case
   //     case App.this.Key2.type => App.this.E2.type
