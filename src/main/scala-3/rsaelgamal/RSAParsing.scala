package rsaelgamal

import bci.view.Parsing.*
import cats.Show
import rsaelgamal.RSAModel
import rsaelgamal.RSAModel

object RSAParsing {
  // decimal representation is default and its components (toString="Show" typeclass, fromString="Parseable" typeclass)
  var representation         = bigint.Representation.Decimal
  given Parseable[BigInt]    = representation.parserBigInt
  given Show[BigInt]         = representation.showerBigInt
  given Show[RSAModel.DAsInverseData] = dData => summon[Show[BigInt]].show(dData.d)
  given Show[Either[RSAModel.Err,RSAModel.DAsInverseData]] = {
    case Left(err) => err.toString
    case Right(data) => summon[Show[RSAModel.DAsInverseData]].show(data)
  }

  given parserBigIntE(using oneDown:Parseable[BigInt]): Parseable[Either[RSAModel.Err,BigInt]] with
    override def parseFromStr(text: String): Either[ParseErr, Either[RSAModel.Err, BigInt]] =
      oneDown.parseFromStr(text) match
        case Left(parseErr) => parseErr match
          case ParseErr("", _) => Right(Left(RSAModel.Err.NotYetSet))
          case _ => Left(parseErr)
        case Right(parsed) => Right(Right(parsed))

  given showerBigIntE(using oneDown:Show[BigInt]): Show[Either[RSAModel.Err,BigInt]] = obj => obj match
    case Left(err) => rsaelgamal.RSAStrings.errInfoFor(err).infoSentence
    case Right(i) => oneDown.show(i)
}

