package gro.bouncycastle.math.field;

import gro.math.BigInteger;

public interface FiniteField
{
    BigInteger getCharacteristic();

    int getDimension();
}
