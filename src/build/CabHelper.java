package build;

// yes, it is possible to write terrible code in any language ;-)

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class CabHelper {
	
	public static void main(String[] args) {
		File zipFile;
		File outDir;
		String outDirName;
		Integer numDirectories = 0;
		Integer numFiles = 0;
		HashMap<String,HashMap<String,String>> cabMapping = new HashMap<String, HashMap<String, String>> ();
		
		if (args.length != 2) {
			throw new IllegalArgumentException("please specify zipfile and output directory");
		}
		
		zipFile = new File(args[0]);
		outDirName = args[1];
		outDir = new File(outDirName);
		
		if (! zipFile.exists() || ! zipFile.canRead() || ! zipFile.isFile()) {
			throw new IllegalArgumentException("zip file not readable");
		}
		
		if (outDir.exists()) {
			if (outDir.isDirectory()) {
				String[] files = outDir.list();
				if (files.length > 0) {
					throw new IllegalArgumentException("out directory not empty");
				}
			} else {
				throw new IllegalArgumentException("output directory exists, but is not a directory");
			}
		} else {
			boolean success = outDir.mkdir();
			if (!success) {
				throw new IllegalArgumentException("unable to create output directory");
			}
		}
		
		try {
			ZipFile zip = new ZipFile(zipFile);
			Enumeration<? extends ZipEntry> zipList;
			zipList = zip.entries();

			while (zipList.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) zipList.nextElement();
				if (zipEntry.isDirectory()) {
					String dirName = zipEntry.getName();
					dirName = dirName.replace(File.separatorChar, '\\');
					if (dirName.endsWith("\\")) {
						dirName = dirName.substring(0, dirName.length()-1);
					}
					if (cabMapping.get(dirName) == null) {
						cabMapping.put(dirName, new HashMap <String, String>());
						numDirectories++;
					}
				} else {
					String inFileName = zipEntry.getName().replace(File.separatorChar, '\\');
					String outFileName = String.format("CW%06d.DMY", numFiles);
					String inDirName = "";
					copyInputStream(zip.getInputStream(zipEntry), new BufferedOutputStream(new FileOutputStream(outDirName+File.separator+outFileName)));
					Integer lastSlash = inFileName.lastIndexOf('\\');
					if (lastSlash == -1) {
						inDirName = "";
					} else {
						inDirName = inFileName.substring(0,lastSlash);
						inFileName = inFileName.substring(lastSlash+1);
					}
					if (cabMapping.get(inDirName) == null) {
						cabMapping.put(inDirName, new HashMap <String, String>());
						numDirectories++;
					}
					cabMapping.get(inDirName).put(outFileName, inFileName);
										
					numFiles++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		StringBuffer outBuffer = new StringBuffer();
		String dosNL = "\r\n";
		outBuffer.append("<wap-provisioningdoc>").append(dosNL);
		outBuffer.append("\t<characteristic type=\"Install\">").append(dosNL);
		outBuffer.append("\t\t<parm name=\"InstallPhase\" value=\"install\" />").append(dosNL);
		outBuffer.append("\t\t<parm name=\"AppName\" value=\"CW-test\" />").append(dosNL);
		outBuffer.append("\t\t<parm name=\"InstallDir\" value=\"%CE1%\\CW-test\" translation=\"install\" />").append(dosNL);
		outBuffer.append(String.format("\t\t<parm name=\"NumDirs\" value=\"%d\" />",numDirectories)).append(dosNL);
		outBuffer.append(String.format("\t\t<parm name=\"NumFiles\" value=\"%d\" />",numFiles)).append(dosNL);
		outBuffer.append("\t\t<parm name=\"NumRegKeys\" value=\"0\" />").append(dosNL);
		outBuffer.append("\t\t<parm name=\"NumRegVals\" value=\"0\" />").append(dosNL);
		outBuffer.append("\t\t<parm name=\"NumShortcuts\" value=\"0\" />").append(dosNL);
		outBuffer.append("\t</characteristic>").append(dosNL);
		outBuffer.append("\t<characteristic type=\"FileOperation\">").append(dosNL);
		for ( String outKey : cabMapping.keySet() ) {
			outBuffer.append(String.format("\t\t<characteristic type=\"%%InstallDir%%\\%s\" translation=\"install\">",outKey)).append(dosNL);
			outBuffer.append("\t\t\t<characteristic type=\"MakeDir\" />").append(dosNL);
			
			for (String inKey: cabMapping.get(outKey).keySet()) {
				outBuffer.append(String.format("\t\t\t<characteristic type=\"%s\" translation=\"install\">",cabMapping.get(outKey).get(inKey))).append(dosNL);
				outBuffer.append("\t\t\t\t<characteristic type=\"Extract\">").append(dosNL);
				outBuffer.append(String.format("\t\t\t\t\t<parm name=\"Source\" value=\"%s\"/>",inKey)).append(dosNL);
				outBuffer.append("\t\t\t\t</characteristic>").append(dosNL);
				outBuffer.append("\t\t\t</characteristic>").append(dosNL);
			}
			outBuffer.append("\t\t</characteristic>").append(dosNL);
		}
		outBuffer.append("\t</characteristic>").append(dosNL);
		outBuffer.append("</wap-provisioningdoc>").append(dosNL);
		try {
			BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outDirName+File.separator+"_setup.xml"));
			outStream.write(outBuffer.toString().getBytes());
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
		
		in.close();
		out.close();
	}
}
