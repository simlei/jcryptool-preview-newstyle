package simlei.util
import zio.*
import zio.stream.*

// my personal tweaks for SubscriptionRef, adding a history and an updates stream of pairs of values
final class SubscriptionUpdateRef[A] private(
    origRef: SubscriptionRef[A],
    historyRef: Ref[List[A]],
    historySize: Int = 1,
  ):
  assert(historySize >= 1)
  export origRef.*
  val updates = changes.mapZIO{ added => for {
      lastSeen <- historyRef.get.map{_.head}
      updateHist <- historyRef.update(old => (added :: old).dropRight((old.size+1) - historySize))
    } yield (lastSeen, added)
  }

object SubscriptionUpdateRef:
  def make[A](initial: => A, historySize: Int = 1) =
    for {
      origRef <- SubscriptionRef.make(initial)
      historyRef <- Ref.make(List(initial))
    } yield SubscriptionUpdateRef(origRef, historyRef, historySize)
