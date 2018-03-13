package gro.bouncycastle.crypto.generators;

import ewe.math.BigInteger;
import ewe.security.SecureRandom;
import gro.bouncycastle.crypto.AsymmetricCipherKeyPair;
import gro.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import gro.bouncycastle.crypto.KeyGenerationParameters;
import gro.bouncycastle.crypto.params.ECDomainParameters;
import gro.bouncycastle.crypto.params.ECKeyGenerationParameters;
import gro.bouncycastle.crypto.params.ECPrivateKeyParameters;
import gro.bouncycastle.crypto.params.ECPublicKeyParameters;
import gro.bouncycastle.math.ec.*;

public class ECKeyPairGenerator
        implements AsymmetricCipherKeyPairGenerator, ECConstants {
    ECDomainParameters params;
    SecureRandom random;

    public void init(
            KeyGenerationParameters param) {
        ECKeyGenerationParameters ecP = (ECKeyGenerationParameters) param;

        this.random = ecP.getRandom();
        this.params = ecP.getDomainParameters();

        if (this.random == null) {
            this.random = new SecureRandom();
        }
    }

    /**
     * Given the domain parameters this routine generates an EC key
     * pair in accordance with X9.62 section 5.2.1 pages 26, 27.
     */

    public AsymmetricCipherKeyPair generateKeyPair() {
        BigInteger n = params.getN();
        int nBitLength = n.bitLength();
        int minWeight = nBitLength >>> 2;

        BigInteger d;
        for (; ; ) {
            d = new BigInteger(nBitLength, random);

            if (d.compareTo(TWO) < 0 || (d.compareTo(n) >= 0)) {
                continue;
            }

            if (WNafUtil.getNafWeight(d) < minWeight) {
                continue;
            }

            break;
        }

        ECPoint Q = createBasePointMultiplier().multiply(params.getG(), d);

        return new AsymmetricCipherKeyPair(
                new ECPublicKeyParameters(Q, params),
                new ECPrivateKeyParameters(d, params));
    }

    protected ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }
}
