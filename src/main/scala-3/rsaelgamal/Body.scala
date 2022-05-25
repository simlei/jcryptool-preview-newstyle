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

object body:
  class Instance(val spec: Spec, val attentionInst: attention.Instance):
    val x = attentionInst.gatingInst
    def runnerZ(using instScope: app.InstanceScope) = for {
      _ <- ZIO.infinity
    } yield ()
  end Instance
  case class Spec(
    appSpec: app.Spec, 
    uiface: RSAUIMain.Interface, 
    ctrlInst: controller.Instance)(using val specScope: app.SpecScope) :

    val attentionSpec = attention.Spec(this)
    def makerZ() = for {
      attentionInst <- attentionSpec.makerZ()
    } yield Instance(this, attentionInst)
  end Spec
end body
