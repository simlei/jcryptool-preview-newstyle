package simlei.util
import cats.Show

def ShowList[V](using shower: Show[V] = Show.fromToString): Show[Iterable[V]] = _.map{shower.show}.mkString("- ", "\n- ", "")
def ShowListGen[V]: Show[Iterable[V]] = _.map{_.toString}.mkString("- ", "\n- ", "")

extension[T](self: T)
  def dbgprinting(lbl: String = "DBG"): T = dbgshowing(lbl)(using Show.fromToString[T])
  def dbgshowing(lbl: String = "DBG")(using shower: Show[T]): T =
    def singleFmt(lbl: String)(content: String) = s"<> $lbl <> $content"
    def multiFmt(lbl: String)(content: String) =
      s"<$lbl> " + "<> ".repeat(15) + s"\n$content\n" + s">$lbl< " + ">< ".repeat(15)
    val out = shower.show(self)
    val toPrint = out.lines.count match
      case 1 => singleFmt(lbl)(out)
      case _ => multiFmt(lbl)(out)
    println(toPrint)
    self

extension[T, S <: Iterable[T]](self: S)
  def dbglistingToString(lbl: String = "DBG"): S = dbglisting(lbl)(using Show.fromToString)
  def dbglisting(lbl: String = "DBG")(using innerShow: Show[T]): S =
    val shower: Show[Iterable[T]] = ShowList[T](using innerShow)
    given Show[S] = shower.show(_)
    self.dbgshowing(lbl)

def expectException[T](code: => T) = {
  var caught = false
  var result: Option[T] = None
  try {
    result = Some(code)
  } catch {
    case e: Throwable => caught = true
  } finally {
    if(! caught) {
      throw new RuntimeException(s"expected exception but got normal result: ${result.get}")
    }
  }
}
