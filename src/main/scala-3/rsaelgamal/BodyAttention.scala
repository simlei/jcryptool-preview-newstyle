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

object attention:
  case class Instance(spec: Spec, gatingInst: gating.Instance):
    def runnerZ(using instScope: app.InstanceScope) = for {
      _ <- ZIO.infinity
    } yield ()
  end Instance
  case class Spec(bodySpec: body.Spec)(using val specScope: app.SpecScope) :
    val gatingSpec = gating.Spec(this)
    def makerZ() = for {
      gatingInst <- gatingSpec.makerZ()
    } yield Instance(this, gatingInst)
  end Spec
end attention

