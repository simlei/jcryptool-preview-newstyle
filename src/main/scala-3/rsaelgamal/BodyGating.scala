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

object gating:
  case class Instance(spec: Spec, parsingInst: parsing.Instance):
    def runnerZ(using instScope: app.InstanceScope) = for {
      _ <- ZIO.infinity
    } yield ()
  end Instance
  case class Spec(val attentionSpec: attention.Spec)(using val specScope: app.SpecScope) :
    val parsingSpec = parsing.Spec(this)
    def makerZ() = for {
      parsingInst <- parsingSpec.makerZ()
    } yield Instance(this, parsingInst)
  end Spec
end gating

