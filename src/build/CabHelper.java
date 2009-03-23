package build;

// this is plain java, since it is only needed if you want to build CABs for
// PocketPCs with the ant build. It will not be used by CacheWolf itself

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class CabHelper {

	private static final Boolean DEBUG = true;
	private static final String PROJECT_NAME = "CW-Test";
	private static final String DOSNL = "\r\n";
	
	private static HashMap<String, HashMap<String, String>> cabMapping = new HashMap<String, HashMap<String, String>>();
	private static Integer numFiles = 0;
	private static Integer numDirectories = 0;

	public static void main(String[] args) {
		File zipFile;
		File outDir;
		String outDirName;
		StringBuffer outBuffer = new StringBuffer();
		
		//
		// check arguments and environment
		//
		
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"please specify zipfile and output directory");
		}

		zipFile = new File(args[0]);
		outDirName = args[1];
		outDir = new File(outDirName);

		if (!zipFile.exists() || !zipFile.canRead() || !zipFile.isFile()) {
			throw new IllegalArgumentException("zip file not readable");
		}

		if (outDir.exists()) {
			if (outDir.isDirectory()) {
				String[] files = outDir.list();
				if (files.length > 0) {
					throw new IllegalArgumentException(
							"out directory not empty");
				}
			} else {
				throw new IllegalArgumentException(
						"output directory exists, but is not a directory");
			}
		} else {
			boolean success = outDir.mkdir();
			if (!success) {
				throw new IllegalArgumentException(
						"unable to create output directory");
			}
		}
		
		//
		// input parsing
		//

		try {
			ZipFile zip = new ZipFile(zipFile);
			Enumeration<? extends ZipEntry> zipList;
			zipList = zip.entries();

			while (zipList.hasMoreElements()) {
				
				ZipEntry zipEntry = (ZipEntry) zipList.nextElement();
				
				if (zipEntry.isDirectory()) {
					String dirName = winifyPath(zipEntry.getName());
					pushDirToMap(winifyPath(dirName));
				} else {
					String inCabFileName;
					String outCabFileName;
					String inCabDirName;
					File dummyFile;
					
					dummyFile = new File(zipEntry.getName());
					
					inCabFileName = dummyFile.getName();
					inCabDirName = pushDirToMap(winifyPath(dummyFile.getParent()));
					outCabFileName = String.format("%08d.DMY", numFiles);
					
					copyInputStream(zip.getInputStream(zipEntry),
							new BufferedOutputStream(new FileOutputStream(
									outDirName + File.separator + outCabFileName)));

					cabMapping.get(inCabDirName).put(outCabFileName, inCabFileName);

					numFiles++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// create an empty logfile and push it on the stack
		try {
			File dummyFile = new File(outDirName + File.separator + String.format("%08d.DMY", numFiles));
			dummyFile.createNewFile();
			cabMapping.get("").put(String.format("%08d.DMY", numFiles), "log.txt");
			numFiles++;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		//
		// output generation
		//

		// header of the xml file, mandatory
		outBuffer.append("<wap-provisioningdoc>").append(DOSNL);
		
		// header section with summary
		outBuffer.append("\t<characteristic type=\"Install\">").append(DOSNL);
		outBuffer.append("\t\t<parm name=\"InstallPhase\" value=\"install\" />").append(DOSNL);
		// this should remove the version warning at the end of the cab install 
		// but even with a warning it will work
		outBuffer.append("\t\t<parm name=\"OSVersionMin\" value=\"4.0\" />").append(DOSNL);
		outBuffer.append("\t\t<parm name=\"OSVersionMax\" value=\"5.9\" />").append(DOSNL);
		outBuffer.append(String.format("\t\t<parm name=\"AppName\" value=\"%s\" />",PROJECT_NAME)).append(DOSNL);
		outBuffer.append(String.format("\t\t<parm name=\"InstallDir\" value=\"%%CE1%%\\%s\" translation=\"install\" />",PROJECT_NAME)).append(DOSNL);
		outBuffer.append(String.format("\t\t<parm name=\"NumDirs\" value=\"%d\" />",numDirectories)).append(DOSNL);
		outBuffer.append(String.format("\t\t<parm name=\"NumFiles\" value=\"%d\" />",numFiles)).append(DOSNL);
		outBuffer.append("\t\t<parm name=\"NumRegKeys\" value=\"0\" />").append(DOSNL);
		outBuffer.append("\t\t<parm name=\"NumRegVals\" value=\"0\" />").append(DOSNL);
		outBuffer.append("\t\t<parm name=\"NumShortcuts\" value=\"1\" />").append(DOSNL);
		outBuffer.append("\t</characteristic>").append(DOSNL);
		
		// start of fileoperation section
		outBuffer.append("\t<characteristic type=\"FileOperation\">").append(DOSNL);
		
		for (String outKey : cabMapping.keySet()) {
			
			// begin of directory structure
			outBuffer.append(
					String.format("\t\t<characteristic type=\"%s\" translation=\"install\">",winifyPath("%InstallDir%\\" + outKey))
				).append(DOSNL);
			outBuffer.append("\t\t\t<characteristic type=\"MakeDir\" />").append(DOSNL);

			// iterate through the file entries. file entries must follow the the directory definition 
			for (String inKey : cabMapping.get(outKey).keySet()) {
				outBuffer.append(
						generateCabFileEntry(
								cabMapping.get(outKey).get(inKey), 
								inKey
						)
				);
			}
			
			// end of directory structur
			outBuffer.append("\t\t</characteristic>").append(DOSNL);
		}
		
		// add shortcut to start menu

		outBuffer.append("\t\t<characteristic type=\"%CE11%\" translation=\"install\">").append(DOSNL);
		outBuffer.append("\t\t\t<characteristic type=\"MakeDir\" />").append(DOSNL);
		outBuffer.append(String.format("\t\t\t<characteristic type=\"%s.lnk\" translation=\"install\">", PROJECT_NAME)).append(DOSNL);
		outBuffer.append("\t\t\t\t<characteristic type=\"Shortcut\">").append(DOSNL);
		outBuffer.append("\t\t\t\t\t<parm name=\"Source\" value=\"%InstallDir%\\CacheWolf.exe\" translation=\"install\" />").append(DOSNL);
		outBuffer.append("\t\t\t\t</characteristic>").append(DOSNL);
		outBuffer.append("\t\t\t</characteristic>").append(DOSNL);
		outBuffer.append("\t\t</characteristic>").append(DOSNL);

		// end of fileoperation section
		outBuffer.append("\t</characteristic>").append(DOSNL);

		// add favorite to internet explorer
/*
		// does not work yet, since %InstallDir% is not expanded correctly :-(
		outBuffer.append("\t<characteristic type=\"BrowserFavorite\">").append(DOSNL);
		outBuffer.append(String.format("\t\t<characteristic type=\"%s local\">", PROJECT_NAME)).append(DOSNL);
		outBuffer.append("\t\t\t<parm name=\"URL\" value=\"file://%InstallDir%\\temp.html\"/>").append(DOSNL);
		outBuffer.append("\t\t</characteristic>").append(DOSNL);
		outBuffer.append("\t</characteristic>").append(DOSNL);
*/

		// footer of the xml file, mandatory
		outBuffer.append("</wap-provisioningdoc>").append(DOSNL);

		//
		// store output to file
		//

		try {
			
			BufferedOutputStream outStream = new BufferedOutputStream(
					new FileOutputStream(
							outDirName + File.separator + "_setup.xml"
					)
			);
			outStream.write(outBuffer.toString().getBytes());
			outStream.flush();
			outStream.close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.exit(1);
			
		}
	}

	/**
	 * copies content of a given input stream to a given output stream
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @throws IOException
	 */
	private static final void copyInputStream(InputStream in, OutputStream out)	throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}

		in.close();
		out.close();
	}
	
	/**
	 * generate the XML snippet that represents a file for the CAB
	 * 
	 * @param original filename that will later be seen in the PocktPC file system
	 * @param translated safe 8.3 noted filename stored inside the CAB
	 * @return characteristic structure for including a file in an installable PocketPC CAB
	 */
	private static final String generateCabFileEntry(String original, String translated) {
		StringBuffer retBuffer = new StringBuffer();
		
		retBuffer.append(String.format("\t\t\t<characteristic type=\"%s\" translation=\"install\">",original)).append(DOSNL);
		retBuffer.append("\t\t\t\t<characteristic type=\"Extract\">").append(DOSNL);
		retBuffer.append(String.format("\t\t\t\t\t<parm name=\"Source\" value=\"%s\"/>",translated)).append(DOSNL);
		retBuffer.append("\t\t\t\t</characteristic>").append(DOSNL);
		retBuffer.append("\t\t\t</characteristic>").append(DOSNL);

		return retBuffer.toString();
	}
	
	/**
	 * generate a path information from the given paramter suitable for usage in
	 * a cab file this includes converting all separators from the current os (/
	 * or \)to the format later used by the PocketPC (\) and cutting of any
	 * leading or trainling \
	 * 
	 * @param path path to normalize for use in cab
	 * @return normalized path
	 */
	private static final String winifyPath(String path) {
		if (path == null) {
			return "";
		}
		path = path.replace(File.separatorChar, '\\');
		if (path.indexOf('\\') == 0) {
			path = path.substring(1);
		}
		if (path.lastIndexOf('\\') == path.length() - 1) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
	
	/**
	 * mostly for my convenience so I won't have to search for all the System.out.println
	 * to disable debug output
	 * 
	 * @param str string to log
	 */
	private static final void log(String str) {
		if (DEBUG) {
			System.out.println(str);
		}
	}
	
	/**
	 * check if a given directory already is a key in cabMapping
	 * if directory is not a key yet, put it on the map and
	 * associate an empty hashmap to it
	 *
	 * @param directory name of directory that was pushed to the map
	 */
	private static final String pushDirToMap(String directory) {
		if ( directory == null ) {
			directory = "";
		}
		
		if (cabMapping.get(directory) == null) {
			cabMapping.put(directory, new HashMap<String, String>());
			numDirectories++;
		}
		
		return directory;
	}
}
