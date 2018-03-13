package gro.bouncycastle.crypto.tls;

import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;

public class DigitallySigned {
    protected SignatureAndHashAlgorithm algorithm;
    protected byte[] signature;

    public DigitallySigned(SignatureAndHashAlgorithm algorithm, byte[] signature) {
        if (signature == null) {
            throw new IllegalArgumentException("'signature' cannot be null");
        }

        this.algorithm = algorithm;
        this.signature = signature;
    }

    /**
     * Parse a {@link DigitallySigned} from an {@link InputStream}.
     *
     * @param context the {@link TlsContext} of the current connection.
     * @param input   the {@link InputStream} to parse from.
     * @return a {@link DigitallySigned} object.
     * @throws IOException
     */
    public static DigitallySigned parse(TlsContext context, InputStream input) throws IOException {
        SignatureAndHashAlgorithm algorithm = null;
        if (TlsUtils.isTLSv12(context)) {
            algorithm = SignatureAndHashAlgorithm.parse(input);
        }
        byte[] signature = TlsUtils.readOpaque16(input);
        return new DigitallySigned(algorithm, signature);
    }

    /**
     * @return a {@link SignatureAndHashAlgorithm} (or null before TLS 1.2).
     */
    public SignatureAndHashAlgorithm getAlgorithm() {
        return algorithm;
    }

    public byte[] getSignature() {
        return signature;
    }

    /**
     * Encode this {@link DigitallySigned} to an {@link OutputStream}.
     *
     * @param output the {@link OutputStream} to encode to.
     * @throws IOException
     */
    public void encode(OutputStream output) throws IOException {
        if (algorithm != null) {
            algorithm.encode(output);
        }
        TlsUtils.writeOpaque16(signature, output);
    }
}
