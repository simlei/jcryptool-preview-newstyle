package simlei.util
import zio.{System => ZSystem, *}
import zio.stream._
import cats.Show

def unitT[T](t: T): Unit = ()

extension[S](self: S)
  def ~>[T <: Singleton](v: T) = (self, v)

extension(self: StringBuilder)
  def addLine(x: Any) =
    self.append(x)
    self.append("\n")

extension(self: String)
  def ifNotEmpty(f: => String) = self match
    case "" => self
    case _ => f
    def firstLower = self.ifNotEmpty{
      self.charAt(0).toLower + self.substring(1)
    }
    def firstUpper = self.ifNotEmpty{
      self.charAt(0).toUpper + self.substring(1)
    }
    def mnemonic = "&" + self
