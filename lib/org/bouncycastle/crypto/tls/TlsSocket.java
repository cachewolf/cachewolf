package org.bouncycastle.crypto.tls;

import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.net.Socket;
import ewe.net.UnknownHostException;


public class TlsSocket {
	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;

	public TlsSocket(CertificateVerifyer certificateVerifyer, Socket s) throws UnknownHostException, IOException {
		socket = s;

		if (certificateVerifyer == null) {
			inputStream = s.getInputStream();
			outputStream = s.getOutputStream();
		} else {
	        OutputStream os = socket.getOutputStream();
	        InputStream is = socket.getInputStream();

	        TlsProtocolHandler tph = new TlsProtocolHandler(is, os);
	        tph.connect(certificateVerifyer);

			inputStream  = tph.getInputStream();
			outputStream = tph.getOutputStream();
		}
	}

	public boolean close() {
		boolean b = true;
/*		try {
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			b = false;
		} */
		boolean a = true; // socket.close();
		return a && b;
	}
}
