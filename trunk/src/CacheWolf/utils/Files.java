package CacheWolf.utils;

import ewe.io.File;
import ewe.io.FileInputStream;
import ewe.io.FileOutputStream;

public class Files {
    /**
     * copy a file
     * 
     * @param sFileSrc
     *            source file name
     * @param sFileDst
     *            destination file name
     * @return true on success, false if an error occurred
     */
    public static boolean copy(String sFileSrc, String sFileDst) {
	try {
	    File fSrc = new File(sFileSrc);
	    int len = 32768;
	    byte[] buff = new byte[(int) java.lang.Math.min(len, fSrc.length())];
	    FileInputStream fis = new FileInputStream(fSrc);
	    File dDst = new File(File.getDrivePath(sFileDst));
	    if (!dDst.exists()) {
		dDst.createDir();
	    }
	    FileOutputStream fos = new FileOutputStream(sFileDst);
	    while (0 < (len = fis.read(buff)))
		fos.write(buff, 0, len);
	    fos.flush();
	    fos.close();
	    fis.close();
	} catch (Exception ex) {
	    return false;
	}
	return true;
    }

}
