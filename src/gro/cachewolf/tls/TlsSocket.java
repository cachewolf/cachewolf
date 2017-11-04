package gro.cachewolf.tls;

import ewe.security.SecureRandom;



import gro.bouncycastle.crypto.tls.Certificate;
import gro.bouncycastle.crypto.tls.CertificateRequest;
//import org.bouncycastle.crypto.tls.Certificate;
import gro.bouncycastle.crypto.tls.DefaultTlsClient;
import gro.bouncycastle.crypto.tls.ProtocolVersion;
//import org.bouncycastle.crypto.tls.ProtocolVersion;
//import org.bouncycastle.crypto.tls.ServerOnlyTlsAuthentication;
import gro.bouncycastle.crypto.tls.TlsAuthentication;
import gro.bouncycastle.crypto.tls.TlsClient;
import gro.bouncycastle.crypto.tls.TlsClientProtocol;
//import org.bouncycastle.crypto.tls.TlsKeyExchange;
//import org.bouncycastle.crypto.tls.TlsSession;


import gro.bouncycastle.crypto.tls.TlsCredentials;
import gro.bouncycastle.crypto.tls.TlsSession;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.net.Socket;
import ewe.net.UnknownHostException;

public class TlsSocket {
	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;

	public TlsSocket(boolean useSsl, Socket s) throws UnknownHostException, IOException, Throwable {
		socket = s;
try{
		if (!useSsl) {
			inputStream = s.getInputStream();
			outputStream = s.getOutputStream();
		} else {
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			// https://stackoverflow.com/questions/18065170/how-do-i-do-tls-with-bouncycastle
			TlsClientProtocol tlsClientProtocol = new TlsClientProtocol(is, os, new SecureRandom());
			TlsClient tc = new DefaultTlsClient() {

				public TlsAuthentication getAuthentication() throws IOException {
					return new TlsAuthentication() {
						public void notifyServerCertificate(Certificate serverCertificate) throws IOException {
							// validateCertificate(serverCertificate);
						}

						public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
							throw new UnsupportedClassVersionError();
						}
					};
				}

//				public ProtocolVersion getClientHelloRecordLayerVersion() {
//					// TODO Auto-generated method stub
//					return null;
//				}

//				public ProtocolVersion getClientVersion() {
//					return ProtocolVersion.TLSv12;
//				}

//				public TlsKeyExchange getKeyExchange() throws ewe.io.IOException {
//					// TODO Auto-generated method stub
//					return null;
//				}

			};

			tlsClientProtocol.connect(tc);
			
			inputStream = tlsClientProtocol.getInputStream();
			outputStream = tlsClientProtocol.getOutputStream();			
		}}
		catch (Throwable t){
			t.printStackTrace();
			throw t;
		}
	}

	public boolean close() {
		boolean b = true;
		/*
		 * try { outputStream.close(); inputStream.close(); } catch (IOException
		 * e) { b = false; }
		 */
		boolean a = true; // socket.close();
		return a && b;
	}
}