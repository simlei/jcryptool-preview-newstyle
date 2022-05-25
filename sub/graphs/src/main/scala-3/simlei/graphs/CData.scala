package simlei.graphs

// this is the "gritty" backbone where Any (Object) is a thing
// arities: 1: regular value, 2+: is a tuple (for product nodes)
// "tuples" is used here as a model for nodes with N > 1 inputs.
// They are special as these are transitions that are infrastructural
// computations that can't fail, and are not implemented directly by API
trait CData:
  type KeyT = Any
  def get(key: KeyT): Any
  def regularSet(key: KeyT)(value: Any): CData = set(key, 0)(value)
  def productSet(key: KeyT, tupleIdx: Int)(value: Any): CData = set(key, tupleIdx)(value)
  // tupleIdx == 0 for regular values, and Tuple1, positive for TupleN with N>1
  def set(key: KeyT, tupleIdx: Int = 0)(value: Any): CData

// todo: improve implementation w.r.t speed
case class CDataImpl private(
  keys: List[Any], 
  arities: Map[Any,Int],
  buffer: Map[Any,List[Option[Any]]]
) extends CData:
  def checkKey(key: Any) =
    if(! buffer(key).forall{_.isDefined}) throw new RuntimeException(s"CData fail for $key with pertinent buffer: ${buffer(key)}. see CData doc")
  override def get(key: Any): Any | Tuple =
    val isTupled = getArityFor(key) > 1
    checkKey(key)
    def listToTuple(in: List[Any]): Tuple =
      in match
        case Nil => EmptyTuple
        case x :: xs => x *: listToTuple(xs)
    isTupled match
      case true => listToTuple(buffer(key).flatten)
      case false => buffer(key)(0).get
  def getArityFor(key: Any) = arities(key) // (1)
  def set(key: Any, tupleIdx: Int = 0)(value: Any): CDataImpl =
    assert(tupleIdx > -1)
    assert(tupleIdx < getArityFor(key))
    val old = buffer(key)
    val updated = old.updated(tupleIdx, Some(value))
    this.copy(buffer = buffer + (key -> updated))

object CDataImpl:
  def of( keys: List[Any], arities: Map[Any,Int] ): CData =
    val buffer: Map[Any,List[Option[Any]]] = Map.from{keys.map{
      key => key -> List.fill(arities(key))(Option.empty[Any])
    }}
    CDataImpl(keys, arities, buffer)


// Comments:
//
// (1)
// how many inputs N are there for a product node (N >= 1)
// N == 1 => directly operate on data
// N > 1 => use products and assume that the content type is TupleN. provide transition
// for accumulating the inputs. As soon as all parts of the tuple are assembled, assign to data
// // 2) cyclic graph as GDAG
