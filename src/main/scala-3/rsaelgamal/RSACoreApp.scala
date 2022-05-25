package rsaelgamal

import zio.*
import zio.stream.*

import bci.swt.*
import bci.swt.platform.*

import bci.metamodel.Graphs.*
import rsaelgamal.RSAModel.*
import rsaelgamal.RSAModel.paramsGraph.*
import rsaelgamal.behavior.*

import simlei.util.*


object app:

  // "global" scope of the spec phase (shape of the program, all static)
  case class SpecScope(
    val ctrlInst: controller.Instance, 
    val uiface: RSAUIMain.Interface
  )

  // "global" scope of the ZIO phase (after makerZ was successfully called, with all sync primitives)
  case class InstanceScope(
    bodyInst: body.Instance,
    globalRef1: SubscriptionRef[Int] // like this, one can "cheat" with global ZIO values
  ):
    val x = bodyInst.attentionInst.gatingInst.parsingInst.justDisplayer(P).input
    val specScope = bodyInst.spec.specScope // of course specScope is available here, too

  // type aliases and glue primitives for this type of graph app (tailored to ParamsGraph)
  trait BodyPart:
    val glueParticles = new GraphGlues[Err, ParamsGraph.type](ParamsGraph)
    type NodeE = glueParticles.NodeE
    type NodeV = glueParticles.ThisGraphV
    type PlanGateSpec = Streamgate.Spec[PlanT,AttentionPlan]
    type PlanGate = Streamgate.Instance[PlanT,PlanT] // TODO: rename ->PlanstreamGate, etc

  class Instance(val spec: Spec,
    val ctrlInst: controller.Instance,
    val bodyInst: body.Instance
  )(using val instanceScope: InstanceScope):

    def runnerZ = for {
      fiber_controller           <- ctrlInst.runnerZ.forkSpecced(ctrlInst ~> "execution")
      fiber_body                 <- bodyInst.runnerZ.forkSpecced(bodyInst ~> "execution")
      fibers = List( fiber_controller, fiber_body )
    } yield fibers
  end Instance


  case class Spec(context: SWTViewContext):
    import context.platform.given // using export in context yields cyclic error... TODO
    import context.given
    val swtCanvas: SwtCanvas[BaseComposite] = context.baseCanvas
    val ctrlSpec = controller.Spec()
    val ui = RSAUIMain(HasComposite.in(swtCanvas.stump))

    // define global values here that will be accessible everywhere in the app
    def zScope(bodyInst: body.Instance): IOZ[InstanceScope] = for {
      globalRef1 <- SubscriptionRef.make(1)
    } yield InstanceScope(bodyInst, globalRef1)

    def makerZ() = for {
      ctrlInst   <- ctrlSpec.makerZ()    // makes all the synchronization primitives of the RSA model
      bodyScope  =  SpecScope(ctrlInst, ui.interface) // the body is set between the "environment", the UI, and the controller
      bodySpec   =  body.Spec(this, ui.interface, ctrlInst)(using bodyScope)
      bodyInst   <- bodySpec.makerZ() // makes all the synchronization primitives of I/O
      zScopeInst <- zScope(bodyInst)
    } yield Instance(this, ctrlInst, bodyInst)(using zScopeInst)
  end Spec

end app
