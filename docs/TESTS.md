# Test suites

 - `default`
 - `test-vectors`
 - `wrong`
 - `composite`
 - `invalid`
 - `twist`
 
**NOTE: The `wrong`, `composite`, `invalid` and `twist` test suites caused temporary/permanent DoS of some cards. These test suites prompt you for
confirmation before running, be cautious.**

## Default
Tests the default curves present on the card. These might not be present or the card might not even support ECC.
Tests keypair allocation, generation, ECDH and ECDSA. ECDH is first tested with two valid generated keypairs, then
with a compressed public key to test support for compressed points.

This test suite is run if no argument is provided to `-t / --test`.

For example:
```bash
java -jar ECTester.jar -t
```
tests prime field and binary field curves, using the default test suite.


## Test-Vectors
Tests using known test vectors provided by NIST/SECG/Brainpool:

[SECG - GEC2](http://read.pudn.com/downloads168/doc/772358/TestVectorsforSEC%201-gec2.pdf)

[NIST - ECCDH](http://csrc.nist.gov/groups/STM/cavp/component-testing.html#ECCCDH)

[Brainpool - RFC6931](https://tools.ietf.org/html/rfc6932#appendix-A.1)

[Brainpool - RFC7027](https://tools.ietf.org/html/rfc7027#appendix-A)

For example:
```bash
java -jar ECTester.jar -t test-vectors
```
tests all curves for which test-vectors are provided.


## Wrong
Tests on a category of wrong curves. These curves are not really curves as they have:
 - non-prime field in the prime-field case
 - reducible polynomial as the field polynomial in the binary case
This test suite also does some additional tests with corrupting the field parameter:
 - Fp:
   - p = 0
   - p = 1
   - p = q^2; q prime
   - p = q * s; q and s prime
 - F2m:
   - e1 = e2 = e3 = 0
   - m < e1 < e2 < e3

These tests should fail generally.

For example:
```bash
java -jar ECTester.jar -t wrong
```
does all wrong curve tests.


## Composite
Tests using curves that don't have a prime order/nearly prime order.
These tests should generally fail, a success here implies the card **WILL** use a non-secure curve if such curve is set
by the applet. Operations over such curves are susceptible to small-subgroup attacks.

For example:
```bash
java -jar ECTester.jar -t composite
```


## Invalid
Tests using known named curves from several categories(SECG/NIST/Brainpool) against pre-generated *invalid* public keys.
ECDH should definitely fail, a success here implies the card is susceptible to invalid curve attacks.

See [Practical Invalid Curve Attacks on TLS-ECDH](https://www.nds.rub.de/media/nds/veroeffentlichungen/2015/09/14/main-full.pdf) for more information.

For example:
```bash
java -jar ECTester.jar -t invalid
```
tests using all curves with pregenerated *invalid* public keys for these curves.


## Twist
Tests using known named curves froms several categories(SECG/NIST) against pre-generated points on twists of said curves.
ECDH should fail, a success here implies the card is not twist secure, if a curve with an unsecure twist is used,
the card might compute on the twist, if a point on the twist is supplied.

See [SafeCurves on twist security](https://safecurves.cr.yp.to/twist.html) for more information.

For example:
```bash
java -jar ECTester.jar -t twist
```