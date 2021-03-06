package rsaelgamal.help

val section_schema_body_de = """
  <p>Nachdem p und q gewählt sind, berechnet der Nutzer zwei natürliche Zahlen e und d, so dass (e*d) = 1 mod ((p-1)(q-1)). Das Produkt p*q = N und der Exponent e dienen dann als öffentlicher, und der Exponent d als privater Schlüssel. Die ursprünglich erzeugten Primzahlen p und q müssen geheim gehalten, oder besser gelöscht werden.</p>
  
  <p>Da dieser Verschlüsselungsalgorithmus nur auf ganzen Zahlen funktioniert, werden vor dem Verschlüsseln die Klartextzeichen in Zahlen konvertiert. Normalerweise wird dies mit Hilfe des ASCII-Codes durchgeführt. Es ist aber auch möglich, sich auf A=0, B=1, ...., Z=25 zu einigen.</p>
  
  <p>In dieser Visualisierung wird die ASCII-Code-Variante umgesetzt.</p>
  
  <p> Die Zahl jedes Klartextzeichens muss kleiner sein als das Produkt N. Warum das so ist, wird klar wenn man die Ver- und Entschlüsselungsfunktionen betrachtet (m = Zahlenwert des Klartextzeichen, c = Zahlenwert des Chiffratzeichen): c = m^e mod N und m = c^d mod N.</p>
  
  <p>Modulo-Rechnung bedeutet, den Rest einer Zahl modulo N zu berechnen. Wenn der Klartextzeichen-Wert m größer als N wäre, wäre nicht sicher gestellt, dass man in jedem Fall ein eindeutiges Geheimtextzeichen c erhält.</p>
  
  <p>Die Sicherheit des RSA Schemas basiert auf zwei mathematischen Problemen: Erstens ist es schwierig, für große Zahlen eine Wurzel modulo N zu berechnen (RSA-Problem). Zweitens ist die Faktorisierung von N = p*q schwierig, wenn die Faktoren ausreichend groß gewählt werden (RSA-Schlüsselproblem).</p>

  
  <h3 id="KeyGen">RSA-Schlüsselgenerierung</h3>
  <p> Eine besondere Charakteristik von Public-Key-Kryptosystemen wie RSA ist, dass umfangreiche Berechnungen nötig sind, um einen Schlüssel zu generieren. Erst danach kann die Ver- und Entschlüsselung durchgeführt werden.</p>
  
  <p>Als erstes werden die RSA-Parameter p, q, N und die Eulersche Zahl phi(N) mit Hilfe von zwei zufälligen Primzahlen p und q berechnet. Der sogenannte RSA-Modulus N wird als Produkt von p und q berechnet, N = p*q. Die Eulersche Zahl phi(N) ergibt sich als Produkt (p-1)*(q-1). Im Standardfall werden hiernach p und q gelöscht (somit kennt sie auch der Schlüsselerzeuger nicht mehr und braucht sie auch nicht geheim zu halten).</p>
  
  <p>Als zweiter Schritt wird der öffentliche Exponent e bestimmt. Mit dessen Hilfe, sowie phi(N), wird der private Exponent d berechnet. Der Exponent e wird wie folgt gewählt: 1 < e < phi(N), mit der Voraussetzung, dass e relativ prim zu phi(N) ist. Ein besonders beliebter Wert für e ist 2^16+1 = 65537. Diese Zahl ist meistens co-prim zu phi(N) und besonders gut für "square and multiply" Exponentiationen geeignet. Dies resultiert in sehr schnellen Operationen mit dem öffentlichen Schlüssel.</p>
  
  <p>Der private Exponent d = e^(-1) mod phi(N) wird als multiplikative Inverse des öffentlichen Exponenten e modulo phi(N) berechnet. Anders geschrieben: e*d = 1 mod ((p-1)(q-1)).</p>
  
  <p>Nach der Schlüsselerzeugung können N und e öffentlich gemacht werden. Anschließend kann jeder eine Nachricht für den Besitzer des zugehörigen privaten Schlüssels d verschlüsseln oder eine digitale Signatur überprüfen  &nbsp;&ndash;  einzig und allein durch die Verwendung des öffentlichen Schlüssels. </p>

  <h3 id="Enc">RSA-Ver- / Entschlüsselung</h3>
  <p> Um Daten mit RSA zu <b>verschlüsseln</b>, wird das Chiffrat mit Hilfe des öffentlichen Schlüssels e des Empfängers und des Klartexts berechnet. Die Verschlüsselungsfunktion ist c = m^e mod N. Das heißt, dass jeder eine Nachricht mit Hilfe des öffentlichen Schlüssels verschlüsseln kann. Allerdings kann nur der Besitzer des zugehörigen privaten Schlüssels d die Nachricht wieder entschlüsseln.</p>

  <p> Um ein Chiffrat mit RSA zu <b>entschlüsseln</b>, muss der private Schlüssel d bekannt sein. Die Entschlüsselungsfunktion, c^d = (m^e)^d = m^(e*d) = m mod N, berechnet den Klartext m.</p>

  
  <h3 id="Sig">RSA-Signatur / Verifizierung</h3>
  <p> Um eine Nachricht m mit RSA zu <b>signieren</b>, muss der private Schlüssel d bekannt sein. Die Signaturfunktion wird auf m oder auf einen Hashwert von m angewendet: m^d = sign(m) oder (hash(m))^d = sign(hash(m)). Dies erzeugt die Signatur.</p>

  <p> Um eine RSA-Signatur zu <b>verifizieren</b>, wird der öffentliche Schlüssel e des Signierers wie folgt verwendet: (sign(m))^e = m' oder (sign(hash(m)))^e = hash(m)'. Das Ergebnis der RSA-Exponentiation ist m' oder hash(m)' und muss mit der Nachricht m oder dem Hashwert der Nachricht hash(m), berechnet aus dem erhaltenen (und entschlüsselten) Dokument, verglichen werden. Wenn beide Werte identisch sind, wird dies als Beweis gesehen, dass der echte Besitzer des zugehörigen privaten Schlüssels d das Dokument signiert hat.</p>
"""

val old_help_de = """
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	  <title>RSA-Verschlüsselung</title>
	<script src="${JCTJS_HOST}/javascript/jquery.js"></script>
	<script>TOC_generate_default("h2, h3")</script>
</head>

<body>
  <h1>RSA-Kryptosystem (verschlüsseln, entschlüsseln, signieren und verifizieren)</h1>
<div class="TOC"></div>
<hr />

  <p>Diese Hilfe beschreibt zwei Punkte:<br/>
  <a href="#term">Das RSA-Schema</a><br/>
  <a href="#plugin">Benutzerinstruktionen für diese Visualisierung</a></p>
  
  <p> <b>Bitte beachten</b> Sie, dass diese Visualisierung die Operationen auf einzelnen Zeichen durchführt. In der Praxis werden die Operationen auf Zeichenblöcken oder der gesamten Nachricht durchgeführt. RSA wird normalerweise nicht verwendet, um große Datenmengen zu verschlüsseln, sondern um einen Sessionkey zu verschlüsseln (mit diesem werden dann die Daten mit einem symmetrischen Verfahren wie AES oder 3DES verschlüsselt) oder um einen Hashwert der Nachricht zu signieren.</p>

  <h2 id="term">Das RSA-Schema</h2>

  <p> Die bekannteste asymmetrische Verschlüsselung, die RSA-Verschlüsselung, wurde 1977 von Ronald Rivest, Adi Shamir und Leonard Adleman entwickelt. Die privaten und öffentlichen Schlüssel werden mit Hilfe von zwei zufälligen, großen Primzahlen, p und q, konstruiert, die vom Nutzer gewählt werden.</p>
  
  <p>Anschließend berechnet der Nutzer zwei natürliche Zahlen e und d, so dass (e*d) = 1 mod ((p-1)(q-1)). Das Produkt p*q = N und der Exponent e dienen dann als öffentlicher, und der Exponent d als privater Schlüssel. Die ursprünglich erzeugten Primzahlen p und q müssen geheim gehalten, oder besser gelöscht werden.</p>
  
  <p>Da dieser Verschlüsselungsalgorithmus nur auf ganzen Zahlen funktioniert, werden vor dem Verschlüsseln die Klartextzeichen in Zahlen konvertiert. Normalerweise wird dies mit Hilfe des ASCII-Codes durchgeführt. Es ist aber auch möglich, sich auf A=0, B=1, ...., Z=25 zu einigen.</p>
  
  <p>In dieser Visualisierung wird die ASCII-Code-Variante umgesetzt.</p>
  
  <p> Die Zahl jedes Klartextzeichens muss kleiner sein als das Produkt N. Warum das so ist, wird klar wenn man die Ver- und Entschlüsselungsfunktionen betrachtet (m = Zahlenwert des Klartextzeichen, c = Zahlenwert des Chiffratzeichen): c = m^e mod N und m = c^d mod N.</p>
  
  <p>Modulo-Rechnung bedeutet, den Rest einer Zahl modulo N zu berechnen. Wenn der Klartextzeichen-Wert m größer als N wäre, wäre nicht sicher gestellt, dass man in jedem Fall ein eindeutiges Geheimtextzeichen c erhält.</p>
  
  <p>Die Sicherheit des RSA Schemas basiert auf zwei mathematischen Problemen: Erstens ist es schwierig, für große Zahlen eine Wurzel modulo N zu berechnen (RSA-Problem). Zweitens ist die Faktorisierung von N = p*q schwierig, wenn die Faktoren ausreichend groß gewählt werden (RSA-Schlüsselproblem).</p>

  
  <h3 id="KeyGen">RSA-Schlüsselgenerierung</h3>
  <p> Eine besondere Charakteristik von Public-Key-Kryptosystemen wie RSA ist, dass umfangreiche Berechnungen nötig sind, um einen Schlüssel zu generieren. Erst danach kann die Ver- und Entschlüsselung durchgeführt werden.</p>
  
  <p>Als erstes werden die RSA-Parameter p, q, N und die Eulersche Zahl phi(N) mit Hilfe von zwei zufälligen Primzahlen p und q berechnet. Der sogenannte RSA-Modulus N wird als Produkt von p und q berechnet, N = p*q. Die Eulersche Zahl phi(N) ergibt sich als Produkt (p-1)*(q-1). Im Standardfall werden hiernach p und q gelöscht (somit kennt sie auch der Schlüsselerzeuger nicht mehr und braucht sie auch nicht geheim zu halten).</p>
  
  <p>Als zweiter Schritt wird der öffentliche Exponent e bestimmt. Mit dessen Hilfe, sowie phi(N), wird der private Exponent d berechnet. Der Exponent e wird wie folgt gewählt: 1 < e < phi(N), mit der Voraussetzung, dass e relativ prim zu phi(N) ist. Ein besonders beliebter Wert für e ist 2^16+1 = 65537. Diese Zahl ist meistens co-prim zu phi(N) und besonders gut für "square and multiply" Exponentiationen geeignet. Dies resultiert in sehr schnellen Operationen mit dem öffentlichen Schlüssel.</p>
  
  <p>Der private Exponent d = e^(-1) mod phi(N) wird als multiplikative Inverse des öffentlichen Exponenten e modulo phi(N) berechnet. Anders geschrieben: e*d = 1 mod ((p-1)(q-1)).</p>
  
  <p>Nach der Schlüsselerzeugung können N und e öffentlich gemacht werden. Anschließend kann jeder eine Nachricht für den Besitzer des zugehörigen privaten Schlüssels d verschlüsseln oder eine digitale Signatur überprüfen  &nbsp;&ndash;  einzig und allein durch die Verwendung des öffentlichen Schlüssels. </p>

  <h3 id="Enc">RSA-Ver- / Entschlüsselung</h3>
  <p> Um Daten mit RSA zu <b>verschlüsseln</b>, wird das Chiffrat mit Hilfe des öffentlichen Schlüssels e des Empfängers und des Klartexts berechnet. Die Verschlüsselungsfunktion ist c = m^e mod N. Das heißt, dass jeder eine Nachricht mit Hilfe des öffentlichen Schlüssels verschlüsseln kann. Allerdings kann nur der Besitzer des zugehörigen privaten Schlüssels d die Nachricht wieder entschlüsseln.</p>

  <p> Um ein Chiffrat mit RSA zu <b>entschlüsseln</b>, muss der private Schlüssel d bekannt sein. Die Entschlüsselungsfunktion, c^d = (m^e)^d = m^(e*d) = m mod N, berechnet den Klartext m.</p>

  
  <h3 id="Sig">RSA-Signatur / Verifizierung</h3>
  <p> Um eine Nachricht m mit RSA zu <b>signieren</b>, muss der private Schlüssel d bekannt sein. Die Signaturfunktion wird auf m oder auf einen Hashwert von m angewendet: m^d = sign(m) oder (hash(m))^d = sign(hash(m)). Dies erzeugt die Signatur.</p>

  <p> Um eine RSA-Signatur zu <b>verifizieren</b>, wird der öffentliche Schlüssel e des Signierers wie folgt verwendet: (sign(m))^e = m' oder (sign(hash(m)))^e = hash(m)'. Das Ergebnis der RSA-Exponentiation ist m' oder hash(m)' und muss mit der Nachricht m oder dem Hashwert der Nachricht hash(m), berechnet aus dem erhaltenen (und entschlüsselten) Dokument, verglichen werden. Wenn beide Werte identisch sind, wird dies als Beweis gesehen, dass der echte Besitzer des zugehörigen privaten Schlüssels d das Dokument signiert hat.</p>

  
  <h2 id="plugin">Benutzerinstruktionen für diese Visualisierung</h2>
  
  <p> Wenn die RSA-Visualisierung gestartet wird, kann man die gewünschte Operation (verschlüsseln, entschlüsseln, signieren oder verifizieren) auszuwählen  &nbsp;&ndash; durch die Wahl des entsprechenden Tabs.</p>

  <p>Die Farbe der ersten drei Buttons links zeigt den aktuellen Status an. Rot bedeutet: Dieser Schritt wurde noch nicht bearbeitet. Ist ein Button grün, bedeutet das, diese Aktion ist erledigt. Wenn die ersten beiden Buttons grün sind, kann die Verschlüsselung schrittweise mit einem Klick auf <strong>Start</strong> gestartet oder direkt mit einem Klick auf <strong>Verschlüsseln</strong> abgeschlossen werden. Mit dem Button <strong>Zurücksetzen</strong> können Sie die Eingaben immer wieder neu durchführen.</p>

  <p> Zuerst müssen die benötigten RSA-Schlüssel ausgewählt werden. Klicken Sie auf den Button <strong>Schlüsselwahl</strong> auf der linken Seite. Man kann dann entweder ein neues Schlüsselpaar erzeugen oder ein gespeichertes Schlüsselpaar laden. Wenn der Verschlüsselungs- oder Verifizier-Tab ausgewählt ist, ist es außerdem möglich, nur einen öffentlichen Schlüssel zu erzeugen oder zu laden (denn für die Operationen Verschlüsselung und Verifizierung wird kein privater Schlüssel benötigt).</p>
  
  <p>Wenn Sie die Option <strong>Neues Schlüsselpaar</strong> wählen, kann man die Parameter p, q, und e entweder aus der Drop-down Liste wählen, oder eigene Werte eingeben (die Zahlen können beliebig groß sein, da JCT eine Langzahlarithmetik-Bibliothek benutzt). Wenn Sie den Schlüssel für eine spätere Verwendung speichern wollen, setzen Sie einen Haken bei der Checkbox <strong>Schlüsselpaar speichern</strong>.</p>
  
  <p>Die Option <strong>Bestehendes Schlüsselpaar</strong> lädt ein zuvor generiertes und gespeichertes Schlüsselpaar. Wählen Sie einen Schlüssel aus der Liste und geben Sie das entsprechende Passwort ein.</p>
  
  <p>Wenn der Verschlüsselungs- oder Verifizier-Tab ausgewählt wurde, ist es außerdem möglich, die Optionen <strong>Neuer öffentlicher Schlüssel</strong> und <Strong>Bestehender öffentlicher Schlüssel</Strong> auszuwählen. Die Option <strong>Neuer öffentlicher Schlüssel</strong> erlaubt die Eingabe von N und e, sowie die Option den Schlüssel zu speichern. Die Option <Strong>Bestehender öffentlicher Schlüssel</Strong> lädt einen zuvor gespeicherten öffentlichen Schlüssel.</p>
  
  <p>Wenn Sie auf <strong>Fertigstellen</strong> klicken, wird der gewählte Schlüssel automatisch übernommen und im Feld "Schlüssel" angezeigt (in Hexadezimal-Darstellung).</p>
  
  <p>Als Beispiel für die weitere Vorgehen wird die Bedienung im Tab <strong>Verschlüsseln</strong> beschrieben. Als nächstes klicken Sie auf den Button <strong>Text eingeben</strong> auf der linken Seite. Sie können Text mit ASCII-Zeichen eingeben. Jedes Zeichen wird nach Hex konvertiert und im Feld "Eingegebener Text" angezeigt. Wenn die Checkbox <strong>Zahlen direkt eingeben</strong> markiert ist, können Sie direkt Dezimalzahlen, separiert mit "SPACE" eingeben. Diese Zahlen werden zu ASCII-Zeichen konvertiert und im Feld "Eingegebener Text" angezeigt. Auf der nächsten Seite haben Sie die Möglichkeit, den eingegebenen Text automatisch formatieren zu lassen.</p>
  
  <p> Jetzt sind alle benötigten Informationen verfügbar und es ist möglich, die Operation zu starten. Wenn Sie nur das Ergebnis sehen möchten, klicken Sie auf den Button <strong>Verschlüsseln</strong> (unterhalb des Buttons <strong>Text eingeben</strong>). Wenn Sie stattdessen die Berechnung jeder Zahl einzeln sehen möchten, klicken Sie auf den <strong>Start</strong>-Button im Feld "Schrittweise Berechnungen". In beiden Fällen (schrittweise oder direkt durch alle Schritte) wird das endgültige Ergebnis im Feld "Ergebnis" angezeigt.</p>
  
  <p> Am unteren Rand der Maske können Sie verschiedene zusätzliche Optionen auswählen. Es besteht die Möglichkeit sich den Schlüssel und das Ergebnis entweder in Hex-, Binär- oder Dezimalwerten anzeigen zu lassen. Außerdem können Sie Werte von einem der drei anderen Tabs übernehmen und sich zusätzliche Informationen in Dialogen während der Operation anzeigen lassen.</p>
  
  <p>Weitere Informationen zum RSA-Kryptosystem finden sich z.B. unter:</p>
  <ul>
  <li><a href="PLUGINS_ROOT/org.jcryptool.visual.extendedrsa/$nl$/help/content/rsaHelp.html">JCT-Plugin: Erweitertes RSA-Kryptosystem (inklusive Identitäten und multi-primem RSA)</a>
  <li><a target="_blank" href="http://www.springer.com/de/book/9783662484234">Joachim von zur Gathen: CryptoSchool, Springer, 2015, Kapitel 3</a></li>
  <li><a target="_blank" href="http://www.springer.com/de/book/9783662484234">Johannes Buchmann: Einführung in die Kryptografie, Springer, 2010, Kapitel 8</a></li>
  <li><a target="_blank" href="http://de.wikipedia.org/wiki/RSA-Kryptosystem">Wikipedia: RSA</a></li>
  <li><a target="_blank" href="https://www.cryptool.org/download/ctb/CT-Book-de.pdf">Das CrypTool-Buch, Kapitel 4</a></li>
  </ul>
  
<br/>
</body>
</html>
"""
