package gro.bouncycastle.math.field;

import ewe.math.BigInteger;

public interface FiniteField {
    BigInteger getCharacteristic();

    int getDimension();
}
