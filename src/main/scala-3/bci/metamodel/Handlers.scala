package bci.metamodel

trait StateHandler[T](initial: T):
  var observers: List[T => Unit] = List()
  var currentState: T = initial
  def addObserver(observe: T => Unit, fireLastDirectly: Boolean = true): Unit =
    observers = observers :+ observe
    fireLastDirectly match
      case true => observe(currentState)
      case false => ()
  def setNewState(newState: T): Unit =
    currentState = newState
    observers.foreach{ _.apply(newState) }


