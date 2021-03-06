package rsaelgamal.help

val section_schema_body_en = """
  <p>After choosing p and q, the user chooses two natural numbers e and d such that (e*d) = 1 mod ((p-1)(q-1)). The product p*q = N and e then serve as the public key, d as the private key. The initially generated primes p and q must be kept secret or better deleted afterwards.</p>
  
  <p>As this encryption algorithm only works on numbers, first the plaintext letters must be converted. This is generally done using the ASCII code, but one can of course also agree on A=0, B=1 .... Z=25.</p>
  
  <p>However, the number of each plaintext letter must be smaller than the product N. This becomes clear when you examine the encryption and decryption functions (m = plaintext letter, c = ciphertext letter): c = m^e mod N and m = c^d mod N.</p>
  
  <p>Calculating modulo means to determine the remainder from a number modulo N. If m would be allowed to be bigger than N it would not always be possible to obtain a unique ciphertext character.</p>
  
  <p>The security of the RSA scheme is based on two mathematical problems. First, it is hard to extract a root modulo N (RSA problem). Second, factorization of a number N = p*q is hard, if the factors are sufficiently large (RSA key problem).<br/></p>

  
  <h3 id="KeyGen">RSA Key Generation</h3>
  
  <p> A special characteristic of public key cryptosystems like RSA is that extensive calculations are necessary to generate the key before en-/decryption or signing can take place.</p>
  
  <p>First of all, the RSA parameters p, q, N and the Euler number phi(N) are calculated by choosing two different prime numbers p and q at random, and then by calculating the so-called RSA modulus N = p*q. The Euler number phi(N) = (p-1)*(q-1) is calculated from the prime factors p and q.</p>
  
  <p>In a second step, the public RSA exponent e is determined and from this, together with phi(N), the secret RSA exponent d is calculated. The number e is chosen as follows 1 < e < phi(N), with the property that e is relatively prime to phi(N). An especially popular value for e is 2^16+1 = 65537, as in most cases this is co-prime to phi(N) and is especially well-suited for square and multiply exponentiation resulting in a very fast public key operation.</p>
  
  <p>The secret exponent \( d = e^{-1}\; \text{mod}\; \text{phi}(\mathbb{N}) \) is calculated as the multiplicative inverse of the public exponent e modulo phi(N).</p>
  
  <p>After the RSA key generation, N and e can be published. Then, anyone could encrypt a message for the owner of the according secret RSA key d or check a digital signature  &nbsp;&ndash;  using only the public key. </p>
  

  <h3 id="Enc">RSA Encryption / Decryption</h3>
  <p> To <b>encrypt</b> data with the RSA scheme, the ciphertext is calculated using the public key e of the recipient and the plaintext. The encryption function is c = m^e mod N. This means that anyone can encrypt a message using the public key. But only the owner of the associated secret key d can then decrypt the message again. </p>

  <p> To <b>decrypt</b> a ciphertext with the RSA scheme, one needs to know the private key d. The decryption function, c^d = (m^e)^d = m^(e*d) = m mod N, yields the plaintext m. </p>

  
  <h3 id="Sig">RSA Signing / Verifying</h3>
  <p> To <b>sign</b> a message m with the RSA scheme, you need to know the private key d. The signing function is applied to m or to a hash value of m: m^d = sign(m) or (hash(m))^d = sign(hash(m)). This yields the signature.</p>

  <p> You can <b>verify</b> a digital RSA signature using the signer's public key e by computing (sign(m))^e = m' or (sign(hash(m)))^e = hash(m)'. The result of the RSA exponentiation is m' or hash(m)' which has to be compared with the message m or the hash of the message hash(m) computed from the received (and decrypted) document. If both hash values are identical this is considered as a proof that the real owner of the corresponding private RSA key has signed the document.</p>

"""


val old_help_en = """
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>RSA encryption</title>
	<script id="MathJax-script" async src="${JCTJS_HOST}/javascript/MathJax-master/es5/tex-mml-svg.js"></script>
	<script src="${JCTJS_HOST}/javascript/jquery.js"></script>
	<script>TOC_generate_default("h2, h3")</script>
	
<!--	<style>
		.toc-list-item {list-style-type: circle;}
	</style>
-->
</head>

<body>

  <h1>RSA Cryptosystem (Encrypt, Decrypt, Sign, and Verify)</h1>

<div class="TOC"></div>
<hr />

<div class="content"> <!-- the TOC generation currently only looks within this 'content' class-->
<!-- example for a in-line and multi-line equations-->
<!-- <p>
  When \(a \ne 0\), there are two solutions to \(ax^2 + bx + c = 0\) and they are
  \[x = {-b \pm \sqrt{b^2-4ac} \over 2a}.\]
</p> -->

  <p>This help file describes two things:<br/>
  <a href="#term">The RSA Scheme</a><br/>
  <a href="#plugin">Handling Instructions: How to Use this Visualization</a></p>
  
  <p> <b>Please note</b> that in this visualization the operations are performed on each single character. In practice, the operations are performed on blocks or even the whole message. Normally, RSA is not used to encrypt large amounts of data. Mostly, it is used to encrypt a session key, which later is used to encrypt the data with a symmetric cryptosystem (e.g. AES or 3DES), or to sign the hash value of a message. </p>

  <h2 id="term">The RSA Scheme</h2>

  <p> The most well-known asymmetric cryptosystem, RSA, was developed in 1977 by Ronald Rivest, Adi Shamir, and Leonard Adleman. The private and public keys are constructed using two randomly selected big prime numbers p and q which the user can specify himself</p>
  
  <p>Then, the user chooses two natural numbers e and d such that (e*d) = 1 mod ((p-1)(q-1)). The product p*q = N and e then serve as the public key, d as the private key. The initially generated primes p and q must be kept secret or better deleted afterwards.</p>
  
  <p>As this encryption algorithm only works on numbers, first the plaintext letters must be converted. This is generally done using the ASCII code, but one can of course also agree on A=0, B=1 .... Z=25.</p>
  
  <p>However, the number of each plaintext letter must be smaller than the product N. This becomes clear when you examine the encryption and decryption functions (m = plaintext letter, c = ciphertext letter): c = m^e mod N and m = c^d mod N.</p>
  
  <p>Calculating modulo means to determine the remainder from a number modulo N. If m would be allowed to be bigger than N it would not always be possible to obtain a unique ciphertext character.</p>
  
  <p>The security of the RSA scheme is based on two mathematical problems. First, it is hard to extract a root modulo N (RSA problem). Second, factorization of a number N = p*q is hard, if the factors are sufficiently large (RSA key problem).<br/></p>

  
  <h3 id="KeyGen">RSA Key Generation</h3>
  
  <p> A special characteristic of public key cryptosystems like RSA is that extensive calculations are necessary to generate the key before en-/decryption or signing can take place.</p>
  
  <p>First of all, the RSA parameters p, q, N and the Euler number phi(N) are calculated by choosing two different prime numbers p and q at random, and then by calculating the so-called RSA modulus N = p*q. The Euler number phi(N) = (p-1)*(q-1) is calculated from the prime factors p and q.</p>
  
  <p>In a second step, the public RSA exponent e is determined and from this, together with phi(N), the secret RSA exponent d is calculated. The number e is chosen as follows 1 < e < phi(N), with the property that e is relatively prime to phi(N). An especially popular value for e is 2^16+1 = 65537, as in most cases this is co-prime to phi(N) and is especially well-suited for square and multiply exponentiation resulting in a very fast public key operation.</p>
  
  <p>The secret exponent \( d = e^{-1}\; \text{mod}\; \text{phi}(\mathbb{N}) \) is calculated as the multiplicative inverse of the public exponent e modulo phi(N).</p>
  
  <p>After the RSA key generation, N and e can be published. Then, anyone could encrypt a message for the owner of the according secret RSA key d or check a digital signature  &nbsp;&ndash;  using only the public key. </p>
  

  <h3 id="Enc">RSA Encryption / Decryption</h3>
  <p> To <b>encrypt</b> data with the RSA scheme, the ciphertext is calculated using the public key e of the recipient and the plaintext. The encryption function is c = m^e mod N. This means that anyone can encrypt a message using the public key. But only the owner of the associated secret key d can then decrypt the message again. </p>

  <p> To <b>decrypt</b> a ciphertext with the RSA scheme, one needs to know the private key d. The decryption function, c^d = (m^e)^d = m^(e*d) = m mod N, yields the plaintext m. </p>

  
  <h3 id="Sig">RSA Signing / Verifying</h3>
  <p> To <b>sign</b> a message m with the RSA scheme, you need to know the private key d. The signing function is applied to m or to a hash value of m: m^d = sign(m) or (hash(m))^d = sign(hash(m)). This yields the signature.</p>

  <p> You can <b>verify</b> a digital RSA signature using the signer's public key e by computing (sign(m))^e = m' or (sign(hash(m)))^e = hash(m)'. The result of the RSA exponentiation is m' or hash(m)' which has to be compared with the message m or the hash of the message hash(m) computed from the received (and decrypted) document. If both hash values are identical this is considered as a proof that the real owner of the corresponding private RSA key has signed the document.</p>

  
  <h2 id="plugin">Handling Instructions: How to Use this Visualization</h2>
  
  <p> When the RSA visualization is started, you can select the desired operation, encrypt, decrypt, sign, or verify  &nbsp;&ndash;  by choosing the corresponding tab.</p>
  
  <p> The color of the buttons on the left side shows their current status. Red means that the step is not complete. When a button is green the corresponding step is complete. If the first two buttons are green, it is possible to start the encryption either with a click on <strong>Start</strong> or to directly finish it with a click on <strong>Encrypt</strong>. Using the button <strong>Reset</strong> it is possible to choose new values.</p>
  
  <p> First you need to select the required RSA keys. To do so, click on the <strong>Key generation / selection</strong> button on the left. You can either create a new keypair or load a saved keypair. When the encryption or verify tab is selected it is also possible to create or choose only a public key. Note that for these operations, encrypt and verify, a secret key is not required.</p>
  
  <p>When you choose the option <strong>New key pair</strong> you can either choose the parameters p, q, and e out of the drop down list, or fill in custom values (the numbers can be of any size as JCT uses an arbitrary-precision arithmetic library). Marking the checkbox <strong>Save key pair</strong> you can store the key for later use.</p>
  
  <p>The option <strong>Existing key pair</strong> loads a previously generated key pair. Choose a key out of the list and enter the corresponding password.</p>
  
  <p>When the encryption or verify tab is selected it is also possible to choose the options <strong>New public key</strong> and <Strong>Existing public key</Strong>. The option <strong>New public key</strong> allows you to enter N and e, and to choose if the key should be saved or not. The option <Strong>Existing public key</Strong> loads a previously stored public key.</p>
  
  <p>When you click on <strong>Finish</strong> the chosen key is automatically inserted and can be seen in the "Key" field (converted to hex).</p>
  
  <p>As sample how to proceed we describe the handling in the <strong>Encrypt</strong> tab. Next, click the <strong>Enter text</strong> button on the left. You can enter a text using ASCII characters. Every character will be converted to hex and can be seen in the "Entered text" field. If you mark the <strong>Enter numbers directly</strong> checkbox, you can insert decimal numbers separated by "SPACE". These numbers are converted to ASCII characters and can be seen in the "Entered text" field. On the next page it is possible to apply some filters on the text.</p>
  
  <p> Now, all required information is available and it is possible to start the operation. If you just want to see the result, you can click on the button <strong>Encrypt</strong> (below the <strong>Enter text</strong> button). Alternatively, in order to see the calculation of each number click on the <strong>Start</strong> button in the "Step-by-step calculations" field. Both ways (going stepwise or directly through all steps), the final result can be seen in the "Result" field.</p>
  
  <p> At the bottom, you can see additional options. There, you have the possibility to choose whether the key and result should be displayed in decimal, binary, or hex values, whether to inherit values from one of the three other tabs, and whether to show additional help in dialogs while performing the operation.</p>
  
  <p>For further information about the RSA cryptosystem please refer to:</p>
  <ul>
  <li><a href="PLUGINS_ROOT/org.jcryptool.visual.extendedrsa/$nl$/help/content/rsaHelp.html">JCT Plugin: Visualization of an Extended RSA Cryptosystem (including Identities and Multi-Prime RSA)</a>
  <li><a target="_blank" href="http://www.springer.com/de/book/9783662484234">Joachim von zur Gathen: CryptoSchool, Springer, 2015, Chapter 3</a></li>
  <li><a target="_blank" href="http://www.springer.com/de/book/9780387207568">Johannes Buchmann: Introduction to Cryptography, Springer, 2004, Chapter 8</a></li>
  <li><a target="_blank" href="http://en.wikipedia.org/wiki/RSA_(algorithm)">Wikipedia: RSA</a></li>
  <li><a target="_blank" href="https://www.cryptool.org/download/ctb/CT-Book-en.pdf#chapter.4">The CrypTool Book, Chapter 4</a></li>
  </ul>

<br/>
</div>

</body>
</html>
"""
