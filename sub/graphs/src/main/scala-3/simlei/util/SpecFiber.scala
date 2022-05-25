package simlei.util
import zio.*
import zio.stream.*

// this represents a running fiber which contains a reference to the spec that produced it.
// recipe: for any class `Spec` with s: Spec, for which f(s) or s.f produces z:ZIO:
//         z.fork.map(forked => SpecFiber.referencing(represents=s)(forked)) : ZIO[_,_,SpecFiber[...]]
//         z.fork.map(SpecFiber.referencing(s)(_))                            (equivalent)
//         SpecFiber.makeSpeccedForked(z, s)                                              (equivalent)
//         z.forkSpecced(s)                                                   (equivalent, using an extension method)
trait SpecFiber[+Spec,+E,+T]:
  def fiber: Fiber[E,T]
  def spec: Spec
object SpecFiber:
  // constructors
  def makeSpeccedForked[Spec,R,E,T](program: ZIO[R,E,T], represents: Spec): URIO[R,SpecFiber[Spec,E,T]] = 
    program.fork.map{referencing(represents)(_)}
  def referencing[Spec,E,T](represents: Spec)(forked: Fiber[E,T]) = new SpecFiber[Spec,E,T]:
    override def fiber = forked
    override def spec = represents
