package gro.bouncycastle.math.ec.endo;

import gro.math.BigInteger;

public interface GLVEndomorphism extends ECEndomorphism
{
    BigInteger[] decomposeScalar(BigInteger k);
}
