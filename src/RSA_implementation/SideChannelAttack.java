package RSA_implementation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class SideChannelAttack {

    long avgBitZeroRed;
    long cntBitZeroRed;

    long avgBitZeroNRed;
    long cntBitZeroNRed;

    long avgBitOneRed;
    long cntBitOneRed;

    long avgBitOneNRed;
    long cntBitOneNRed;

    long time;
    boolean bool0;
    boolean bool1;

    public SideChannelAttack(){

        avgBitZeroRed = 0;
        avgBitZeroNRed = 0;
        avgBitOneRed = 0;
        avgBitOneNRed = 0;

        cntBitZeroRed = 0;
        cntBitZeroNRed = 0;
        cntBitOneRed = 0;
        cntBitOneNRed = 0;
    }

    public void collectTMeasurs(int nSamples, BigInteger modulus, BigInteger d, BigInteger e) {
        Random rnd;
        BigInteger m;
        BigInteger c;

        for (int i=0;i<nSamples;i++) {
            rnd = new Random();
            m = new BigInteger(modulus.bitLength() - 1, rnd);

            c = montEncrypt(m, e, modulus);

            /* Assuming bit is zero */
            montDecrypt(c, d, modulus, false);
            if (bool0) { // This number has been extracted by several experiments
                avgBitZeroRed += time;
                cntBitZeroRed ++;
            }
            else {
                avgBitZeroNRed += time;
                cntBitZeroNRed ++;
            }

//            System.out.println("Assumed zero time : " + (time));

            /* Assuming bit is one */
            montDecrypt(c, d, modulus, true);
            if (bool1) {// This number has been extracted by several experiments
                avgBitOneRed += time;
                cntBitOneRed ++;
            }
            else {
                avgBitOneNRed += time;
                cntBitOneNRed ++;
            }

//            System.out.println("Assumed one  time : " + (time));
        }
    }

    public int execute() {

        if (cntBitOneNRed != 0) avgBitOneNRed /= cntBitOneNRed;
        if (cntBitOneRed != 0) avgBitOneRed /= cntBitOneRed;
        if (cntBitZeroNRed != 0) avgBitZeroNRed /= cntBitZeroNRed;
        if (cntBitZeroRed != 0) avgBitZeroRed /= cntBitZeroRed;

        System.out.println("avgBitOneNRed "+ avgBitOneNRed );
        System.out.println("avgBitOneRed "+ avgBitOneRed );
        System.out.println("avgBitZeroNRed "+ avgBitZeroNRed );
        System.out.println("avgBitZeroRed "+ avgBitZeroRed );


        if ( avgBitOneRed - avgBitOneNRed  > avgBitZeroRed - avgBitZeroNRed ) {
            System.out.println("bit is one !!");
            return 1;
        }
        System.out.println("bit is zero !!");
        return 0;
    }

    private BigInteger montModExp(BigInteger a, BigInteger exponent, BigInteger N, boolean One) {
        // Note: This code assumes the most significant bit of the exponent is 1, i.e., the exponent is not zero.
        MontogomerySCA mont = new MontogomerySCA(N);
        BigInteger aBar = mont.toMSpace(a);
        long start,end;

        BigInteger result = aBar;// since 1st MSB bit is already one

        /* simulating bit two = 0 */
        if (!One) {
            result = mont.multiply(result, result);// step 1 (2bd MSB)
            start = System.nanoTime();
            result = mont.multiply(result, result);// step 2 (3rd MSB)
            end = System.nanoTime();
            time = end-start;
            bool0 = mont.getBool();
//            System.out.println(bool0);
        }

        /* simulating bit two = 1 */
        else {
            result = mont.multiply(result, result);// step 1 (2bd MSB)
            result = mont.multiply(result, aBar);// step 1 (2bd MSB)
            start = System.nanoTime();
            result = mont.multiply(result, result);// step 3 (3rd MSB)
            end = System.nanoTime();
            time = end-start;
            bool1 = mont.getBool();
//            System.out.println(bool1);
        }

        int expBitlength = exponent.bitLength();
        /* Starts from expBitlength - 3 since the 1st bit is known to be
                         1 and 2nd bit is already assumed before */
        for (int i = expBitlength - 3; i >= expBitlength - 100; i--) {
            result = mont.multiply(result,result);
            if (exponent.testBit(i)) {
                result = mont.multiply(result,aBar);
            }
        }
        return mont.fromMSpace(result);
    }

    private BigInteger montEncrypt(BigInteger m, BigInteger e, BigInteger modulus) {
        return PlainRSADemo.montExp(m, e, modulus);
    }

    private BigInteger montDecrypt(BigInteger c, BigInteger d, BigInteger modulus, boolean One) {
        return montModExp(c, d, modulus, One);
    }

}
