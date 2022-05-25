package bci.view

import cats.Show
import cats.data.Validated

import scala.util.Try
import rsaelgamal.RSAModel.Err

object Parsing {

  // used to represent parse errs. could be extended to indicate where exactly parsing fails if necessary.
  case class ParseErr(text: String, reason: String)

  trait Parseable[T] {
    def parseFromStr(text: String): Either[ParseErr, T]
  }

  object bigint {

    // the representation of BigInts w.r.t. parsing (Parseable) and display as strings (Show)
    //
    enum Representation(
                         val parseFromString: String => Either[ParseErr, BigInt],
                         val base: Int,
                         val parseToString: BigInt => String = {
                           _.toString
                         },
                         val ignoreChars: List[Char] = List(),
                         val canonicalization: String => String = identity, // e.g. filter whitespace etc. As-is, cannot be expected to have any specific properties as the default is useless.
                       ) {
      case Decimal extends Representation(
        base = 10,
        parseFromString = { s =>
          Validated.fromTry(Try {
            BigInt.apply(s)
          }).leftMap { e => ParseErr(s, s"is not in decimal format") }.toEither
        }
      )

      // a representation implicitly provides a to-string (show) and a from-string (parse) instance
      given parserBigInt: Parseable[BigInt] = bigint.parseable(bigint.Representation.Decimal)

      given showerBigInt: Show[BigInt] = bigint.showable(bigint.Representation.Decimal)

    }

    def showable(repr: Representation) = Show.show {
      repr.parseToString(_)
    }

    def parseable(repr: Representation) = new Parseable[BigInt] {
      def parseFromStr(text: String) = repr.parseFromString(text)
    }

  }

}
