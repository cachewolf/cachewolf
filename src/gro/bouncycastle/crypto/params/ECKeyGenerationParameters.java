package gro.bouncycastle.crypto.params;

import ewe.security.SecureRandom;
import gro.bouncycastle.crypto.KeyGenerationParameters;

public class ECKeyGenerationParameters
        extends KeyGenerationParameters {
    private ECDomainParameters domainParams;

    public ECKeyGenerationParameters(
            ECDomainParameters domainParams,
            SecureRandom random) {
        super(random, domainParams.getN().bitLength());

        this.domainParams = domainParams;
    }

    public ECDomainParameters getDomainParameters() {
        return domainParams;
    }
}
