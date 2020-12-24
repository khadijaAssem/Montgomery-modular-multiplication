package RSA_implementation;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

public class MontogomerySCA {
    BigInteger N;
    BigInteger NBar; // NBar = -N^-1 mod R
    int n; // number of bits of N
    BigInteger R; // R = 2^n
    boolean bool;

    public MontogomerySCA(BigInteger N) {
        if (N.compareTo(ZERO) <= 0 || !N.testBit(0)) {
            throw new IllegalArgumentException();
        }
        this.N = N;
        this.n = N.bitLength ();
        this.R = ONE.shiftLeft (n);// R = 2^n
        this.NBar = ZERO.subtract(N.modInverse(R)); // NBar = -N^-1 mod R
    }

    public BigInteger multiply (BigInteger num1, BigInteger num2){// Output : a'b'R^-1 mod R
        return this.reduce(num1.multiply(num2));
    }

    public BigInteger reduce (BigInteger num) {// Output : tR^-1 mod R
        bool = false;
        BigInteger m = num.multiply(NBar).and(R.subtract(ONE));// mod R
        num = num.add(m.multiply(N)).shiftRight(n);// divided R

        if (num.compareTo(N) < 0) return num;

        num = num.multiply(NBar);
        num = num.divide(NBar);

        bool = true;
        return num.subtract(this.N);
    }

    public BigInteger toMSpace (BigInteger num) {
        return num.shiftLeft(n).mod(N);// a' = aR mod N
    }

    public BigInteger fromMSpace (BigInteger num) {// Output : c'R^-1 mod N
        BigInteger RBar = R.modInverse(N);/* R^-1 mod N
        (Should have used Extended Euclidean theorem but it doesn't decrease time !!!*/
        return num.multiply(RBar).mod(N);
    }

    public boolean getBool (){
        return bool;
    }
}
