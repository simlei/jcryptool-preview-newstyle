package simlei.graphs
import scala.collection.mutable.{Map => MutableMap}

object MultiMap:
  def makeEmpty[K,V]: MutableMap[K,Set[V]] = MutableMap[K,Set[V]]()
extension[K, V](self: MutableMap[K, Set[V]])
  def hasMulti(k: K, vPredicate: V => Boolean) =
    self.contains(k) match
      case false => false
      case true => self.get(k).get.exists(vPredicate)
  def addMulti(k: K, v: V) =
    if(self.contains(k))
      val existing = self.get(k).get
      self.addOne(k, existing + v)
    else
      self.addOne(k, Set(v))
