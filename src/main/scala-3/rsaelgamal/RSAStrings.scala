package rsaelgamal

import bci.metamodel.*
import bci.metamodel.Graphs.*
import bci.metamodel.{Node, NodeAny}
import rsaelgamal.RSAModel.*
import rsaelgamal.RSAModel.paramsGraph.*
import bci.swt.*


object RSAStrings:

  val descriptionText = {
"""RSA is the most famous asymmetric cipher. It is called asymmetric, because it is used with a key that has two components: a public and a private part. In this plugin and in literature, the public part is called "e" and the private part is called "d". In contrast to symmetric ciphers, which use the same key for encryption and decryption, the public key allows anyone whom it is shared with to encrypt (e.g. "send") a message to the owner of the private key. But only with the private key, this message can be decrypted.
In this plug-in, messages are represented as single numbers for simplicity. The idea is, that any text can be expressed as a number — as long as the number can be arbitrarily big. But before messages can be encrypted and decrypted, the keys for RSA must be chosen, which is a multi-step process. This plug-in guides you through it and after the keys are chosen, you may encrypt or decrypt messages with the public and private keys."""
}

  type INodeInfo[NodeT <: Node[?]] = NodeT match
    case Node[t] => NodeDescription[t, NodeT]
  // type INodeValidationInfo[NodeT <: Node[?]] = NodeT match
  //   case Node[t] => NodeValidationDescription[t,NodeT]


  case class NodeDescription[CT, NT <: Node[CT]](
      shortASCII: String,
      explanation: String, // whole sentence
      form: String = "a positive whole number", 
      formulaStringOpt: Option[String] = None,
      titleOpt: Option[String] = None,
    ):
    def title = titleOpt.getOrElse(s"Parameter ${shortASCII.toLowerCase}")
    def formulaString = formulaStringOpt.getOrElse(shortASCII)

  given pDescr: INodeInfo[P.type] = NodeDescription(
    shortASCII = "p",
    explanation = s"This is the first prime for RSA key generation. It is used together with q to calculate n and phi.",
    )

  given qDescr: INodeInfo[Q.type] = NodeDescription(
    shortASCII = "q",
    explanation = s"This is the first prime for RSA key generation. It is used together with p to calculate n and phi.",
    )

  given phiDescr: INodeInfo[Phi.type] = NodeDescription(
    shortASCII = "phi",
    explanation = s"This is Euler's totient function of n.",
    )

  given nDescr: INodeInfo[N.type] = NodeDescription(
    shortASCII = "n",
    explanation = s"This is the RSA module. It is part of the eventual RSA configuration, together with private and public key exponents.",
    )

  given dDescr: INodeInfo[D.type] = NodeDescription(
    shortASCII = "d",
    explanation = s"This is the private part of the RSA key. It is calculated from e, the public part, as it's inverse w.r.t. the module phi. Notably, phi is later discarded.",
    )

  given eDescr: INodeInfo[E.type] = NodeDescription(
    shortASCII = "e",
    explanation = s"This is the public part of the RSA key.",
    )

  val emptyNodeDescr = NodeDescription("<not described yet>", "<not described yet>")
  def nodeDescriptionFor(node: NodeAny): NodeDescription[?,?] = node match
    case P           => pDescr
    case Q           => qDescr
    // case QValid      => ???
    // case PQValid     => ???
    case Phi         => phiDescr
    case N           => nDescr
    case E           => eDescr
    // case EValid      => ???
    // case EPhiValid   => ???
    case D           => dDescr
    // case EDN         => ???
    // case ProductPQ   
    // case ProductEPhi 
    // case ProductDN   
    case _ => emptyNodeDescr



  case class StepInfo(instruction: String, thisStepIsAboutThe: String)
  def stepInfoFor(step: Attention): StepInfo  = step match
    case Attention.EmptyAttention => StepInfo(
      "In the following steps, you will specify the parameters for RSA. The public and private key is calculated from these parameters, and this dialog will guide you through the process.", 
      "greeting"
    )
    case Attention.PAttn => StepInfo(
      "The first parameter is p, a positive number which must be a prime.", 
      "input of parameter p")
    case Attention.NPhiAttn      => StepInfo(
      "The second parameter is q, a positive number which must also be a prime. From p and q, the totient (phi) and the module (n) are calculated.", 
      "input of parameter q")
    case Attention.EDNAttn      => StepInfo(
      "The third parameter is e. It is the public component of the RSA key. The private component of the key is calculated by inverting the public component with respect to phi. To that end, e is subject to a few constraints:\n  - it must be coprime with phi (the greatest common divisor must be 1).\n  - It must be smaller than phi  - it must be greater than one.\nThis dialog will tell you when there are issues with your input.", 
      "input of parameter e")
    case Attention.OpAttn(op) => {
      val about = op match
        case Operation.Encrypt => "encryption with RSA"
        case Operation.Decrypt => "decryption with RSA"
        case Operation.Sign => "signature with RSA"
        case Operation.Verify => "signature verification with RSA"
      val instruction = op match
        case Operation.Encrypt => "Turn a 'plain text' number into a 'cipher text' number with the help of the public part of the RSA key (e,n)."
        case Operation.Decrypt => "Turn a 'cipher text' number into a 'plain text' number with the help of the private part of the RSA key (d,n)."
        case Operation.Sign =>    "Sign a 'plain text' number (which would usually be a hash of the full text) with the private part of the RSA key (d,n). This produces a 'signature' number that can be later verified against the same plain text and the public part of the key."
        case Operation.Verify => "Verify a 'signature' number with the public part of the RSA key (e,n). This produces a 'plain text' number (which would usually be a hash of the full text). The verification would be successful in practical examples, if the resulting 'plain text' number matches the hash of the signed plain text."
      StepInfo(instruction, about)

    }
    // case params.Finished    => StepInfo("", "encryption and decryption using the entered parameters")

  case class ErrInfo(infoSentence: String, node: Option[NodeAny] = None)
  val emptyErrInfo = ErrInfo("not described yet")

  def errInfoFor(err: Err): ErrInfo = err match
    case Err.NotYetSet => ErrInfo("Please enter a value.")
    case Err.Dependent => ErrInfo("")
    case Err.EMustBeGreaterThanOne => ErrInfo("Parameter e must be greater than one.")
    case Err.EMustBeCoprimeWithPhi(e,phi) => ErrInfo(s"Parameter e ($e) must be coprime with phi ($phi), but their greatest common divisor is ${e.gcd(phi)}. It should be 1.", Some(E))
    case Err.IsNoPrime(p) => ErrInfo(s"A prime number is expected; $p is no prime. The next prime is ${nextPrime(p)}.")
    case a@_ => ErrInfo(s"there was an issue with the input: ${a.toString}.")

  def nextPrime(p: BigInt): BigInt = {
    var next = p
    while (! next.isProbablePrime(98)) {
      next = next + 1
    }
    return next
  }



  // given qValidation: INodeValidationInfo[Q.type] = NodeValidationDescription(
  //   edge = Some(EdgeQValid),
  //   )

  // given eValidation: INodeValidationInfo[E.type] = NodeValidationDescription(
  //   edge = Some(EdgeEValid),
  //   )

  // given phiValidation: INodeValidationInfo[Phi.type] = NodeValidationDescription(
  //   validations = List()
  //   )

  // given nValidation: INodeValidationInfo[N.type] = NodeValidationDescription(
  //   validations = List()
  //   )

end RSAStrings
