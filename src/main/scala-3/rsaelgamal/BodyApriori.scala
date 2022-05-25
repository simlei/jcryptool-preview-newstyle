package rsaelgamal

import zio._
import zio.stream._

import bci.*
import bci.metamodel.*
import bci.metamodel.Graphs.*
import bci.swt.platform.*
import bci.swt.{*, given}
import bci.view.Parsing.*

import rsaelgamal.RSAModel.*
import rsaelgamal.RSAModel.paramsGraph.*
import rsaelgamal.behavior.*

import simlei.util.*

object apriori:
  // define what preset parameters mean: how are they represented as a Plan to alter the RSA graph model? Which UI signal do they correspond to?
  // TODO: improve notion of the glue knowing about "places" in the UI. A stream may not be enough to identify the button and feed back info
  case class Params(p: BigInt, q: BigInt, e: BigInt)
  val smallParams = Params(p = 3,  q = 7,  e = 11 )
  val bigParams =   Params(p = 23, q = 73, e = 127)
