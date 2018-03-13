package gro.bouncycastle.math.field;

public interface Polynomial {
    int getDegree();

//    BigInteger[] getCoefficients();

    int[] getExponentsPresent();

//    Term[] getNonZeroTerms();
}
