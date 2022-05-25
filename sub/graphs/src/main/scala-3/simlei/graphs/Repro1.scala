package repro

// object Repro1:
//   @main def Repro1Main(): Unit =
//     sealed trait EdgeN[NT]
//     object EdgeN:
//       case class Head[NT, From <: NT, To <: NT]    (from: From, to: To ) extends EdgeN[NT]
//       case class Cons[NT, From <: NT, ToE <: EdgeN[NT]](from: From, to: ToE) extends EdgeN[NT]
//       final type InNodesTupleOf[NT, E <: EdgeN[NT]] <: Tuple = E match
//         case Cons[nt,from,toE] => from *: InNodesTupleOf[nt,toE]
//         case Head[nt,from ,to] => from *: EmptyTuple
//       def inNodesTuple[NT,E <: EdgeN[NT]](edge: E): InNodesTupleOf[NT,E] = edge match
//         case e: Cons[nt,from,toE] => e.from *: inNodesTuple[nt,toE](e.to)
//         case e: Head[nt,from,to] => e.from *: EmptyTuple
//     end EdgeN
// end Repro1



// // this crashes 3.1.2-RC2
// object Repro1:
//   @main def Repro1Main(): Unit =
//     sealed trait EdgeN[+NT]
//     object EdgeN:
//       case class Head[+NT, +From <: NT, +To <: NT]    (from: From, to: To ) extends EdgeN[NT]
//       case class Cons[+NT, +From <: NT, +ToE <: EdgeN[NT]](from: From, to: ToE) extends EdgeN[NT]
//       final type InNodesTupleOf[NT, E <: EdgeN[NT]] <: Tuple = E match
//         case Cons[nt,from,toE] => from *: InNodesTupleOf[nt,toE]
//         case Head[nt,from ,to] => from *: EmptyTuple
//       def inNodesTuple[NT,E <: EdgeN[NT]](edge: E): InNodesTupleOf[NT,E] = edge match
//         case e: Cons[nt,from,toE] => e.from *: inNodesTuple[nt,toE](e.to)
//         case e: Head[nt,from,to] => e.from *: EmptyTuple
//     end EdgeN
// end Repro1

// object Repro1:
//   @main def Repro1Main(): Unit =
//     sealed trait EdgeN[+NT]
//     object EdgeN:
//       case class Head[+NT, From <: NT, To <: NT]    (from: From, to: To ) extends EdgeN[NT]
//       case class Cons[+NT, From <: NT, ToE <: EdgeN[NT]](from: From, to: ToE) extends EdgeN[NT]
//       final type InNodesTupleOf[+NT, E <: EdgeN[NT]] <: Tuple = E match
//         case Cons[nt,from,toE] => from *: InNodesTupleOf[nt,toE]
//         case Head[nt,from ,to] => from *: EmptyTuple
//       def inNodesTuple[NT,E <: EdgeN[NT]](edge: E): InNodesTupleOf[NT,E] = edge match
//         case e: Cons[nt,from,to] => e.from *: inNodesTuple[nt,to](e.to)
//         case e: Head[nt,from,to] => e.from *: EmptyTuple
//     end EdgeN
// end Repro1

// object Repro1_noNT:
//   @main def Repro1Main_noNT(): Unit =
//     sealed trait EdgeN
//     object EdgeN:
//       case class Head[+From, +To] (from: From, to: To ) extends EdgeN
//       case class Cons[+From, +ToE <: EdgeN](from: From, to: ToE) extends EdgeN
//       final type InNodesTupleOf[E <: EdgeN] <: Tuple = E match
//         case Head[fromh,toh] => fromh *: EmptyTuple
//         case Cons[fromc,toc] => fromc *: InNodesTupleOf[toc]
//       def inNodesTuple[E <: EdgeN](edge: E): InNodesTupleOf[E] = edge match
//         case e: Head[fh,th] => e.from *: EmptyTuple
//         case e: Cons[fc,tc] => e.from *: inNodesTuple[tc](e.to)
//     end EdgeN
// end Repro1_noNT

////doesn't seem to crash 3.1.3-rc2
//object Repro1_withNT:
//  @main def Repro1Main_withNT(): Unit =
//    sealed trait EdgeN[NT]
//    object EdgeN:
//      case class Head[NT, From, To] (from: From, to: To ) extends EdgeN[NT]
//      case class Cons[NT, From, ToE <: EdgeN[NT]](from: From, to: ToE) extends EdgeN[NT]
//      final type InNodesTupleOf[NT, E <: EdgeN[NT]] <: Tuple = E match
//        case Head[nth,fromh,toh] => fromh *: EmptyTuple
//        case Cons[ntc,fromc,toc] => fromc *: InNodesTupleOf[ntc,toc]
//      def inNodesTuple[NT, E <: EdgeN[NT]](edge: E): InNodesTupleOf[NT,E] = edge match
//        case e: Head[nh,fh,th] => e.from *: EmptyTuple
//        case e: Cons[nc,fc,tc] => e.from *: inNodesTuple[nc,tc](e.to)
//    end EdgeN
//end Repro1_withNT

// // doesn't seem to crash 3.1.3-rc2
// // all possible sites made covariant 
// object Repro1_withNT_cov:
//   @main def Repro1Main_withNT_cov1(): Unit =
//     sealed trait EdgeN[NT]
//     object EdgeN:
//       case class Head[NT, +From, +To] (from: From, to: To ) extends EdgeN[NT]
//       case class Cons[NT, +From, +ToE <: EdgeN[NT]](from: From, to: ToE) extends EdgeN[NT]
//       final type InNodesTupleOf[NT, E <: EdgeN[NT]] <: Tuple = E match
//         case Head[nth,fromh,toh] => fromh *: EmptyTuple
//         case Cons[ntc,fromc,toc] => fromc *: InNodesTupleOf[ntc,toc]
//       def inNodesTuple[NT, E <: EdgeN[NT]](edge: E): InNodesTupleOf[NT,E] = edge match
//         case e: Head[nh,fh,th] => e.from *: EmptyTuple
//         case e: Cons[nc,fc,tc] => e.from *: inNodesTuple[nc,tc](e.to)
//     end EdgeN
// end Repro1_withNT_cov

// doesn't seem to crash 3.1.3-rc2
// all possible sites made covariant 
// coerced it!
object Repro1_withNT_cov2:
  @main def Repro1Main_withNT_cov2(): Unit =
    sealed trait EdgeN[NT]
    object EdgeN:
      case class Head[NT, +From, +To] (from: From, to: To ) extends EdgeN[NT]
      case class Cons[NT, +From, +ToE <: EdgeN[NT]](from: From, to: ToE) extends EdgeN[NT]
      final type InNodesTupleOf[NT, E <: EdgeN[NT]] <: Tuple = E match
        case Head[_,fromh,toh] => fromh *: EmptyTuple
        case Cons[_,fromc,toc] => fromc *: InNodesTupleOf[NT,toc]
      def inNodesTuple[NT, E <: EdgeN[NT]](edge: E): InNodesTupleOf[NT,E] = edge match
        case e: Head[NT,fh,th] => (e.from *: EmptyTuple).asInstanceOf
        case e: Cons[NT,fc,tc] => (e.from *: inNodesTuple(e.to)).asInstanceOf
    end EdgeN
end Repro1_withNT_cov2

