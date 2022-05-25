package rsaelgamal

import bci.metamodel.*
import bci.metamodel.Graphs.*
import RSAModel.*

object RSAImplementation {

  object params:
    import RSAModel.paramsGraph.*

    given pValidImpl: FuncEdgeImpl[EdgePValid.type] = primeValidationFun

    given qValidImpl: FuncEdgeImpl[EdgeQValid.type] = primeValidationFun

    given eValidImpl: FuncEdgeImpl[EdgeEValid.type] = { // does nothing for now;
      case Left(err) => Left(Err.Dependent)
      case Right(e) => Right(e)
    }
    
    given ePhiValidImpl: FuncEdgeImpl[EdgeEPhiValid.type] = {
      case Left(err) => Left(Err.Dependent)
      case Right(e,phi) => e > 1 match // TODO: can we lose the type annotations?
        case false         => Left(Err.EMustBeGreaterThanOne)
        case true          => e < phi match
          case false         => Left(Err.EMustBeSmallerThanPhi(e,phi))
          case true          => e.gcd(phi) == 1 match
            case false         => Left(Err.EMustBeCoprimeWithPhi(e, phi))
            case true          => Right(EPhiData(e, phi))
    }

    given dCalcImpl: FuncEdgeImpl[EdgeDCalc.type] = {
      case Left(err) => Left(Err.Dependent)
      case Right(EPhiData(e, phi)) => { // we directly unpack e and phi from the case class :)
        Right(DAsInverseData(e, phi, e.modInverse(phi)))
      }
    }

    given ednImpl: FuncEdgeImpl[EdgeEDNTf.type] = {
      case Left(err) => Left(Err.Dependent)
      case Right((d_e_phi: DAsInverseData, n: BigInt)) => Right(EDNData(d_e_phi.e, d_e_phi.d, n)) // TODO: can we lose type annotations?
    }

    given pqValidImpl: FuncEdgeImpl[EdgePQValid.type] = {
      case Left(err) => Left(Err.Dependent)
      case Right((p,q)) => p == q match
        case true  => Left(Err.SameValueNotAllowedFor(P, Q))
        case false => Right(PQData(p,q))              // this is the "happy path"
    }


    given nImpl: FuncEdgeImpl[EdgeNCalc.type] = (pq: ErrOrPQ) =>
      pq.right.map{ pq_valid => // right.map transforms the data only if it is not an Err
        pq_valid.p * pq_valid.q
      }

    given phiImpl: FuncEdgeImpl[EdgePhiCalc.type] = pq =>
      pq.right.map{ pqValid =>
        (pqValid.p - 1) * (pqValid.q - 1)
      }

    def primeValidationFun(in: ErrOrInt) = 
      in
        .left.map{_ => Err.Dependent}
        .flatMap{ i => // flatMap/map means, inspect the BigInt (not the Err, which would be in.left.map/flatMap)
          i.isProbablePrime(9999) match {
            case true => Right(i)
            case false => Left(Err.IsNoPrime(i))
          }
        }
  end params

  // TODO: validate positive ints
}
