package gro.bouncycastle.crypto.tls;

import ewe.io.ByteArrayOutputStream;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.util.Vector;

import gro.bouncycastle.asn1.x509.KeyUsage;
import gro.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import gro.bouncycastle.crypto.params.AsymmetricKeyParameter;
import gro.bouncycastle.crypto.params.ECDomainParameters;
import gro.bouncycastle.crypto.params.ECPrivateKeyParameters;
import gro.bouncycastle.crypto.params.ECPublicKeyParameters;
import gro.bouncycastle.crypto.util.PublicKeyFactory;

/**
 * (D)TLS ECDH key exchange (see RFC 4492).
 */
public class TlsECDHKeyExchange extends AbstractTlsKeyExchange
{
    protected TlsSigner tlsSigner;
    protected int[] namedCurves;
    protected short[] clientECPointFormats, serverECPointFormats;

    protected AsymmetricKeyParameter serverPublicKey;
    protected TlsAgreementCredentials agreementCredentials;

    protected ECPrivateKeyParameters ecAgreePrivateKey;
    protected ECPublicKeyParameters ecAgreePublicKey;

    public TlsECDHKeyExchange(int keyExchange, Vector supportedSignatureAlgorithms, int[] namedCurves,
        short[] clientECPointFormats, short[] serverECPointFormats)
    {
        super(keyExchange, supportedSignatureAlgorithms);

        switch (keyExchange)
        {
        case KeyExchangeAlgorithm.ECDHE_RSA:
            this.tlsSigner = new TlsRSASigner();
             break;
        case KeyExchangeAlgorithm.ECDHE_ECDSA:
            this.tlsSigner = new TlsECDSASigner();
            break;
        case KeyExchangeAlgorithm.ECDH_anon:
        case KeyExchangeAlgorithm.ECDH_RSA:
        case KeyExchangeAlgorithm.ECDH_ECDSA:
            this.tlsSigner = null;
            break;
        default:
            throw new IllegalArgumentException("unsupported key exchange algorithm");
        }

        this.namedCurves = namedCurves;
        this.clientECPointFormats = clientECPointFormats;
        this.serverECPointFormats = serverECPointFormats;
    }

    public void init(TlsContext context)
    {
        super.init(context);

        if (this.tlsSigner != null)
        {
            this.tlsSigner.init(context);
        }
    }
    public void skipServerCredentials() throws IOException
    {
        if (keyExchange != KeyExchangeAlgorithm.ECDH_anon)
        {
            throw new TlsFatalAlert(AlertDescription.unexpected_message);
        }
    }

    
    public void processServerCertificate(Certificate serverCertificate) throws IOException
    {
        if (keyExchange == KeyExchangeAlgorithm.ECDH_anon)
        {
            throw new TlsFatalAlert(AlertDescription.unexpected_message);
        }
        if (serverCertificate.isEmpty())
        {
            throw new TlsFatalAlert(AlertDescription.bad_certificate);
        }

        gro.bouncycastle.asn1.x509.Certificate x509Cert = serverCertificate.getCertificateAt(0);

        SubjectPublicKeyInfo keyInfo = x509Cert.getSubjectPublicKeyInfo();
        try
        {
            this.serverPublicKey = PublicKeyFactory.createKey(keyInfo);
        }
        catch (RuntimeException e)
        {
            throw new TlsFatalAlert(AlertDescription.unsupported_certificate, e);
        }

        if (tlsSigner == null)
        {
            try
            {
                this.ecAgreePublicKey = TlsECCUtils.validateECPublicKey((ECPublicKeyParameters) this.serverPublicKey);
            }
            catch (ClassCastException e)
            {
                throw new TlsFatalAlert(AlertDescription.certificate_unknown, e);
            }

        /*Unreachable ->	throw new UnsupportedClassVersionError();/*
            TlsUtils.validateKeyUsage(x509Cert, KeyUsage.keyAgreement);
*/        }
        else
        {
                        if (!tlsSigner.isValidPublicKey(this.serverPublicKey))
            {
                throw new TlsFatalAlert(AlertDescription.certificate_unknown);
            }

            TlsUtils.validateKeyUsage(x509Cert, KeyUsage.digitalSignature);
        }

        super.processServerCertificate(serverCertificate);
    }

    public boolean requiresServerKeyExchange()
    {
        switch (keyExchange)
        {
        case KeyExchangeAlgorithm.ECDH_anon:
        case KeyExchangeAlgorithm.ECDHE_ECDSA:
        case KeyExchangeAlgorithm.ECDHE_RSA:
            return true;
        default:
            return false;
        }
    }

    public byte[] generateServerKeyExchange()
        throws IOException
    {
        if (!requiresServerKeyExchange())
        {
            throw new UnsupportedClassVersionError();/*
            return null;
*/        }

        // ECDH_anon is handled here, ECDHE_* in a subclass
    	throw new UnsupportedClassVersionError();/*

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        this.ecAgreePrivateKey = TlsECCUtils.generateEphemeralServerKeyExchange(context.getSecureRandom(), namedCurves,
            clientECPointFormats, buf);
        return buf.toByteArray();
*/    }

    public void processServerKeyExchange(InputStream input)
        throws IOException
    {
        if (!requiresServerKeyExchange())
        {
            throw new TlsFatalAlert(AlertDescription.unexpected_message);
        }

        // ECDH_anon is handled here, ECDHE_* in a subclass

        ECDomainParameters curve_params = TlsECCUtils.readECParameters(namedCurves, clientECPointFormats, input);

        byte[] point = TlsUtils.readOpaque8(input);

        this.ecAgreePublicKey = TlsECCUtils.validateECPublicKey(TlsECCUtils.deserializeECPublicKey(
            clientECPointFormats, curve_params, point));
    }

    public void validateCertificateRequest(CertificateRequest certificateRequest) throws IOException
    {
    	throw new UnsupportedClassVersionError();/*
    
        if (keyExchange == KeyExchangeAlgorithm.ECDH_anon)
        {
            throw new TlsFatalAlert(AlertDescription.handshake_failure);
        }

        /*
         * RFC 4492 3. [...] The ECDSA_fixed_ECDH and RSA_fixed_ECDH mechanisms are usable with
         * ECDH_ECDSA and ECDH_RSA. Their use with ECDHE_ECDSA and ECDHE_RSA is prohibited because
         * the use of a long-term ECDH client key would jeopardize the forward secrecy property of
         * these algorithms.
         * /
        short[] types = certificateRequest.getCertificateTypes();
        for (int i = 0; i < types.length; ++i)
        {
            switch (types[i])
            {
            case ClientCertificateType.rsa_sign:
            case ClientCertificateType.dss_sign:
            case ClientCertificateType.ecdsa_sign:
            case ClientCertificateType.rsa_fixed_ecdh:
            case ClientCertificateType.ecdsa_fixed_ecdh:
                break;
            default:
                throw new TlsFatalAlert(AlertDescription.illegal_parameter);
            }
        }
    	 */
    }
    public void processClientCredentials(TlsCredentials clientCredentials) throws IOException
    {
    throw new UnsupportedClassVersionError();/* 
        if (keyExchange == KeyExchangeAlgorithm.ECDH_anon)
        {
            throw new TlsFatalAlert(AlertDescription.internal_error);
        }

        if (clientCredentials instanceof TlsAgreementCredentials)
        {
            // TODO Validate client cert has matching parameters (see 'TlsECCUtils.areOnSameCurve')?

            this.agreementCredentials = (TlsAgreementCredentials)clientCredentials;
        }
        else if (clientCredentials instanceof TlsSignerCredentials)
        {
            // OK
        }
        else
        {
            throw new TlsFatalAlert(AlertDescription.internal_error);
        }
*/    }
    public void generateClientKeyExchange(OutputStream output) throws IOException
    {
        if (agreementCredentials == null)
        {
            this.ecAgreePrivateKey = TlsECCUtils.generateEphemeralClientKeyExchange(context.getSecureRandom(),
                serverECPointFormats, ecAgreePublicKey.getParameters(), output);
        }
    }

    public void processClientCertificate(Certificate clientCertificate) throws IOException
    {
    	throw new UnsupportedClassVersionError();/*
        if (keyExchange == KeyExchangeAlgorithm.ECDH_anon)
        {
            throw new TlsFatalAlert(AlertDescription.unexpected_message);
        }

        // TODO Extract the public key
        // TODO If the certificate is 'fixed', take the public key as ecAgreeClientPublicKey
*/    }

    public void processClientKeyExchange(InputStream input) throws IOException
    {
    	throw new UnsupportedClassVersionError();/*
        if (ecAgreePublicKey != null)
        {
            // For ecdsa_fixed_ecdh and rsa_fixed_ecdh, the key arrived in the client certificate
            return;
        }

        byte[] point = TlsUtils.readOpaque8(input);

        ECDomainParameters curve_params = this.ecAgreePrivateKey.getParameters();

        this.ecAgreePublicKey = TlsECCUtils.validateECPublicKey(TlsECCUtils.deserializeECPublicKey(
            serverECPointFormats, curve_params, point));
*/    }

    public byte[] generatePremasterSecret() throws IOException
    {
        if (agreementCredentials != null)
        {
            return agreementCredentials.generateAgreement(ecAgreePublicKey);
        }

        if (ecAgreePrivateKey != null)
        {
            return TlsECCUtils.calculateECDHBasicAgreement(ecAgreePublicKey, ecAgreePrivateKey);
        }

        throw new TlsFatalAlert(AlertDescription.internal_error);
    }
}
