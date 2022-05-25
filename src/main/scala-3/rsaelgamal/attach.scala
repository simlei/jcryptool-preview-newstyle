package rsaelgamal

import bci.metamodel.*
import bci.metamodel.Graphs.*
import rsaelgamal.RSAModel.*
import rsaelgamal.RSAModel.paramsGraph.*
import simlei.util.*
import zio.*
import zio.stream.*

object attach:
  import rsaelgamal.ui.canon.*
  class NodeAttachment[NT <: NodeAny, AttT](val node: NT, val attachment: AttT)
  // class NodeInputAttachment[NT <: NodeAny, InT](val node: NT, val attachment: Input[InT]) extends NodeAttachment(node, attachment)
  // class NodeOutputAttachment[NT <: NodeAny, AttT <: OutputAny](val node: NT, val attachment: AttT) extends NodeAttachment(node, attachment)

  trait NodeAttachments[NT <: NodeAny]:
    def node: NT
    // convenience constructors inside the node scope: just specify attachment. saves a LOT of boilerplate!
    class AttachmentHere[AttT](attachment: AttT) extends NodeAttachment[NT, AttT](node, attachment)
    // class InputAttachmentHere[AttT <: InputAny](val attachment: AttT) extends NodeInputAttachment[NT, AttT](node, attachment)
    // class OutputAttachmentHere[AttT <: OutputAny](val attachment: AttT) extends NodeOutputAttachment[NT, AttT](node, attachment)
    // NOTE: implicit conversions. without: e.g. `given OutputAttachmentHere[TextSink] = OutputAttachmentHere(TextSink(getParseUI(P).textChangeSink))`
    given [Att]              : Conversion[Att, AttachmentHere[Att]]       = (att: Att) => AttachmentHere(att)
    // given [In <: InputAny]   : Conversion[In, InputAttachmentHere[In]]    = (in: In)   => InputAttachmentHere(in)
    // given [Out <: OutputAny] : Conversion[Out, OutputAttachmentHere[Out]] = (out: Out) => OutputAttachmentHere(out)
end attach
