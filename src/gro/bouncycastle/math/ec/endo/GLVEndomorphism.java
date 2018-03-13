package gro.bouncycastle.math.ec.endo;

import ewe.math.BigInteger;

public interface GLVEndomorphism extends ECEndomorphism {
    BigInteger[] decomposeScalar(BigInteger k);
}
