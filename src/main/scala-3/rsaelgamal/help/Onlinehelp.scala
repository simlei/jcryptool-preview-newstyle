package rsaelgamal.help
import rsaelgamal.Intl
import rsaelgamal.Intl.Translatable

// cmdline parameters to start RSA help directly: -StartupHelp /org.jcryptool.bci/$nl$/help/content/index.html
enum Placeholder(val default: String, val translations: (String, String)*) extends Translatable:
  override def others = translations.toList

  case heading              extends Placeholder(heading_en            , "de" -> heading_de)
  case intro                extends Placeholder(intro_en              , "de" -> intro_de)
  case section_schema_title extends Placeholder("The RSA Scheme"      , "de" -> "Das RSA-Verfahren")
  case section_usage_title  extends Placeholder("Using the plug-in"   , "de" -> "Benutzung des Plugins")
  case section_schema_body  extends Placeholder(section_schema_body_en, "de" -> section_schema_body_de)
  case section_usage_body   extends Placeholder("<strong>TODO: to be written</strong>")
  case TOC                  extends Placeholder("""<div class="TOC"></div>""")

import Placeholder.*

def make_body() = s"""
${heading.translate}
${intro.translate}
${TOC.translate}

<h2 id=schema>${section_schema_title.translate}</h2>
${section_schema_body.translate}

<h2 id=usage>${section_usage_title.translate}</h2>
${section_usage_body.translate}
"""

val heading_en = "<h1>Textbook RSA!!</h1>"
val heading_de = "<h1>Akademisches RSA</h1>"

val intro_en = """<p> The most well-known asymmetric cryptosystem, RSA, was developed in 1977 by Ronald Rivest, Adi Shamir, and Leonard Adleman. The private and public keys are constructed using two randomly selected big prime numbers p and q which the user can specify himself </p>"""
val intro_de = """<p> Die bekannteste asymmetrische Verschlüsselung, die RSA-Verschlüsselung, wurde 1977 von Ronald Rivest, Adi Shamir und Leonard Adleman entwickelt. Die privaten und öffentlichen Schlüssel werden mit Hilfe von zwei zufälligen, großen Primzahlen, p und q, konstruiert, die vom Nutzer gewählt werden. In diese Hife werden Details zum RSA-Verschlüsselungssystem, und zur Bedienung des Plug-ins erklärt. </p> """

val javascriptInjectorPattern = "${JCTJS_HOST}"
def make_help = s"""
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	  <title>RSA</title>
	<script src="$javascriptInjectorPattern/javascript/jquery.js"></script>
	<script>TOC_generate_default("h2, h3")</script>
</head>

<body>
${make_body()}
</body>
<hr />
"""

