package gro.bouncycastle.crypto.params;

import gro.bouncycastle.crypto.CipherParameters;

public class AsymmetricKeyParameter
        implements CipherParameters {
    boolean privateKey;

    public AsymmetricKeyParameter(
            boolean privateKey) {
        this.privateKey = privateKey;
    }

    public boolean isPrivate() {
        return privateKey;
    }
}
