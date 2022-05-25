package rsaelgamal

object vis {

val local_styles = s"file:///home/snuc/sandbox/jct_styles"
val page = s"""
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
    <script src="$local_styles/javascript/bootstrap_jct_utilities.js"></script>
          <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
          <title>RSA Textbook visualization</title>
          <script id="MathJax-script" async src="$local_styles/javascript/MathJax-master/es5/tex-mml-svg.js"></script>
      <link rel="stylesheet" href="$local_styles/css/book.css" type="text/css"></link>
      <!-- <link rel="stylesheet" href="$local_styles/css/book.css" type="text/css"></link> -->

    <script type="text/javascript">
    </script>
</head>
<body>
    <h1>Hello!</h1>
    
    Introduction

<div class="TOC"></div>
<div id="section1">
    <h2>Parameters<h2>
    <p>
        <ul><li>p = 137</li><li>q = 131</li><li>n = 137.131</li><li>\\(\\Phi\\) = 17947</li><li>e = 3</li><li>d = 11787</li></ul>
    </p>
    <p>
Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
    </p>
    <p>
Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
    </p>
</div>
<div id="section2">
    <h2>Operation<h2>

    <div>formula: \\( \\frac{1}{2+3}\\)</div>

    <p>
Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
    </p>
<p>
p = 137, q = 131, n = 137.131 = 17947, e = 3, d = 11787.
</p>

<p>
m = 513
</p>

<p>
c = 5133 mod n = 8363.
</p>

<p>
To decrypt c we could compute cd mod n directly
</p>

<p>
m = 836311787 mod 17947 = 513.
</p>


<p>
Pretty difficult to do on your pocket calculator. Now let's use the CRT method - notice how the exponent and modulus values are much smaller and manageable. This simple (but obviously insecure) example should demonstrate how much easier it is to break down the RSA calculation into two smaller ones.
</p>

<p>
dP = e-1 mod (p-1) = d mod (p-1) = 11787 mod 136 = 91
</p>

<p>
dQ = e-1 mod (q-1) = d mod (q-1) = 11787 mod 130 = 87
</p>

<p>
qInv = q-1 mod p = 131-1 mod 137 = 114
</p>

<p>
m1 = cdP mod p = 836391 mod 137 = 102
</p>

<p>
m2 = cdQ mod q = 836387 mod 131 = 120
</p>

<p>
h = qInv.(m1 - m2) mod p = 114.(102-120+137) mod 137 = 3 [we add in an extra p here to keep the sum positive]
</p>

<p>
m = m2 + h.q = 120 + 3.131 = 513.
</p>

<p>
This gives the same result and we can compute this on a pocket calculator. Note how we add in an extra 137 on line 6 to avoid having to deal with a negative number (remember with modular arithmetic that a + p ≡ a (mod p)). The modular inverse on line 3 can be computed by hand using the Euclidean algorithm. Here is one way to compute m1 on line 4 without needing more than 7 digits on our calculator.
</p>

<p>
8363 ≡ 6 (mod 137), so
</p>

<p>
836391 ≡ 691 (mod 137). Now
</p>

<p>
63 = 216 ≡ 79 (mod 137),
</p>

<p>
69 = (63)3 ≡ 793 = 493039 ≡ 113 (mod 137),
</p>

<p>
627 = (69)3 ≡ 1133 = 1442897 ≡ 13 (mod 137),
</p>

<p>
681 = (627)3 ≡ 133 = 2197 ≡ 5 (mod 137). Hence
</p>

<p>
691 = 681+9+1 = 681.69.61 ≡ 5.113.6 = 3390 ≡ 102 (mod 137).
</p>

</div>
</body>
"""

}
// vim:shiftwidth=4
