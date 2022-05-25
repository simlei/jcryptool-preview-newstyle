package rsaelgamal

import java.util.Locale

object Intl:
  val locale = Locale.getDefault().getLanguage().toString
  // val locale = "de"
  val defaultLocale = "en"
  def get(i: Translatable): String =
    if(locale.contains(defaultLocale)) {
      return i.default
    } else {
      i.others.map{ other =>
        locale.contains(other._1) match
          case true => Some(other._2)
          case false => None
      }.flatten.headOption.getOrElse(i.default)
    }

  object Translatable:
    given Conversion[Translatable, String] = _.translate
  trait Translatable {
    def default: String
    def others: List[(String,String)]
    def translate = Intl.get(this)
  }

  object IMsg:
    def of(en: String, de: String*) =
      new Translatable {
        override def default = en
        override def others = de.headOption.map{o => "de" -> o}.toList
      }
  enum IMsg(val default: String, val translations: (String, String)*) extends Translatable:
    override def others = translations.toList
    case Dummy extends IMsg("")
  def of(en: String, de: String*) =
    new Translatable {
      override def default = en
      override def others = de.headOption.map{o => "de" -> o}.toList
    }
