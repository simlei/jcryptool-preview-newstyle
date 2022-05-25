type SpecificType = ((BigInt, String)) => String
val impl: SpecificType = (bi: BigInt, s: String) => s"Hi, msg=$s, number=$bi"

val anyList: List[Any] = List(BigInt(123), "This seems to work!")
val applied = impl.apply(Tuple.fromArray(anyList.toArray).asInstanceOf)
