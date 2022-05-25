package simlei.util
import zio.{System => ZSystem, *}
import zio.stream._

type ManagedZ[T] = ZManaged[Any,Nothing,T]
type PipelineZ[-TIn, +TOut] = ZPipeline[Any, Nothing, TIn, TOut]
type HubZ[T] = Hub[Take[Nothing, T]]
type TakeZ[+T] = zio.stream.Take[Nothing, T]
type SinkZ[T] = ZSink[Any, Nothing, T, Nothing, Any]
type RSinkT[T,R] = ZSink[Any, Nothing, T, Nothing, R]
type StreamZ[T] = ZStream[Any,Nothing,T]
type FiberZ[T] = zio.Fiber[Nothing, T]
type IOZ[T] = ZIO[ZEnv, Nothing, T]


def ifZ[R, E, T](b: => Boolean)(onTrue: => ZIO[R,E,T])(onFalse: => ZIO[R,E,T]): ZIO[R, E, T] =
  ZIO.ifZIO(ZIO.succeed(b))(onTrue, onFalse)

def printSink(prefix: Any): SinkZ[Any] = ZSink.foreach{ arg => ZIO.succeed(println(s"${prefix.toString}: $arg")) }

// -------- extensions

extension[T <: Any](self: T)
  def toZ = ZIO.succeed(self)

extension[R,E,T](self: ZIO[R,E,T])
  def forkSpecced[Spec](spec: Spec): URIO[R,SpecFiber[Spec,E,T]] =
    SpecFiber.makeSpeccedForked(self, spec)

extension[R,E,T](self: ZStream[R,E,T])
  def runPrinting(label: Any = "DBG_DEFAULT") =
    self.run(printSink(label))
  def dbgtapping(lbl: Any = "DEFAULT") =
    self.tapSink(printSink(List("DBG",lbl.toString).mkString("_")))

extension[R,E,T](self: ZStream[R,E,Tuple2[T,T]])
  def updatesOn[B](lens: T => B) = self.filter{case (old,nu) => lens(old) != lens(nu)}.map{case (old,nu) => (lens(old), lens(nu))}
  def changesOn[B](lens: T => B) = self.filter{case (old,nu) => lens(old) != lens(nu)}.map{case (old,nu) => lens(nu)}

extension[R,E,TL,TR](stream: ZStream[R,E,Either[TL,TR]])
  def leftStream = stream.filter{_.isLeft}.map{_.left.get}
  def rightStream = stream.filter{_.isRight}.map{_.right.get}


// -------- running ZIOs in threads

def printExceptionTrace(e: Throwable): Unit =
  System.err.println(e.getMessage)
  e.getStackTrace.foreach{ traceElement =>
    System.err.println(s"  at $traceElement")
  }
  if(e.getCause != null)
    val cause = e.getCause
    System.err.print(s"Caused by: ")
    printExceptionTrace(cause)

// TODO: require only `R` that are actually needed here
extension[R >: ZEnv, E,T](prog: ZIO[R,E,T])
  def runSyncTraced(): T =
    try {
      val result = Runtime.default.unsafeRun(prog)
      println("DBG: RSA: reached the end of the main thread.")
      result
    } catch {
      case e: zio.FiberFailure => {
        throw new RuntimeException("ZIO program failure: " + e.getMessage) {
          override def toString = s"Exception: ${e.getMessage}\n${e.cause.prettyPrint}"
        }
      }
    }
  def runAsyncTracedInThread(callback: T => Unit = unitT): Thread =
      runInThread(runSyncTraced(), callback)

def runInThread[T](prog: => T, callback: T => Unit = unitT): Thread = 
  val runnerThread = new Thread( () => {
    callback(prog)
  } )
  runnerThread.start
  runnerThread
