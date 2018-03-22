package gro.bouncycastle.crypto.generators;

import gro.bouncycastle.crypto.AsymmetricCipherKeyPair;
import gro.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import gro.bouncycastle.crypto.KeyGenerationParameters;
import gro.bouncycastle.crypto.params.DHKeyGenerationParameters;
import gro.bouncycastle.crypto.params.DHParameters;
import gro.bouncycastle.crypto.params.DHPrivateKeyParameters;
import gro.bouncycastle.crypto.params.DHPublicKeyParameters;

import ewe.math.BigInteger;

/**
 * a basic Diffie-Hellman key pair generator.
 *
 * This generates keys consistent for use with the basic algorithm for
 * Diffie-Hellman.
 */
public class DHBasicKeyPairGenerator
    implements AsymmetricCipherKeyPairGenerator
{
    private DHKeyGenerationParameters param;

    public void init(
        KeyGenerationParameters param)
    {
        this.param = (DHKeyGenerationParameters)param;
    }

    public AsymmetricCipherKeyPair generateKeyPair()
    {
        DHKeyGeneratorHelper helper = DHKeyGeneratorHelper.INSTANCE;
        DHParameters dhp = param.getParameters();

        BigInteger x = helper.calculatePrivate(dhp, param.getRandom()); 
        BigInteger y = helper.calculatePublic(dhp, x);

        return new AsymmetricCipherKeyPair(
            new DHPublicKeyParameters(y, dhp),
            new DHPrivateKeyParameters(x, dhp));
    }
}
