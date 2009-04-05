package exp;

import CacheWolf.*;
import ewe.sys.*;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.BufferedReader;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileNotFoundException;
import ewe.io.FileReader;
import ewe.io.FileWriter;
import ewe.io.LineNumberReader;
import ewe.io.PrintWriter;
import ewe.ui.FormBase;
import ewe.ui.ProgressBarForm;
import ewe.util.*;
import ewe.io.IOException;

/**
 * @author Kalle
 * @author TweetyHH Class for Exporting direct to Explorists *.gs Files. Caches
 *         will be exported in files with maximum of 200 Caches.
 */

public class ExploristExporter {
	// starts with no ui for file selection
	final static int TMP_FILE = 0;
	// brings up a screen to select a file
	final static int ASK_FILE = 1;

	// selection, which method should be called
	final static int NO_PARAMS = 0;
	final static int LAT_LON = 1;
	final static int COUNT = 2;

	Vector cacheDB;
	Preferences pref;
	Profile profile;
	// mask in file chooser
	String mask = "*.gs";
	// decimal separator for lat- and lon-String
	char decimalSeparator = '.';
	// if true, the complete cache details are read
	// before a call to the record method is made
	boolean needCacheDetails = true;

	// name of exporter for saving pathname
	String expName;

	public ExploristExporter(Preferences p, Profile prof) {
		profile = prof;
		pref = p;
		cacheDB = profile.cacheDB;
		expName = this.getClass().getName();
		// remove package
		expName = expName.substring(expName.indexOf(".") + 1);
	}

	public void doIt() {
		File configFile = new File("magellan.cfg");
		if (configFile.exists()) {
			FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, pref.getExportPath(expName+"Dir"));
			fc.setTitle(MyLocale.getMsg(2104, "Choose directory for exporting .gs files"));
			String targetDir;
			if(fc.execute() != FormBase.IDCANCEL){
				targetDir = fc.getChosen() + "/";
				pref.setExportPath(expName+"Dir", targetDir);

				CWPoint centre = profile.centre;
				try {
					LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(configFile)));
					String line, fileName, coordinate;
					while ((line = reader.readLine()) != null)  {
						StringTokenizer tokenizer = new StringTokenizer(line,"=");
						fileName = targetDir + tokenizer.nextToken().trim() + ".gs";
						coordinate = tokenizer.nextToken().trim();
						CWPoint point = new CWPoint(coordinate);
						DistanceComparer dc = new DistanceComparer(point);
						cacheDB.sort(dc, false);
						doIt(fileName);
					}
					reader.close();
				} catch (FileNotFoundException e) {
					InfoBox info = new InfoBox(MyLocale.getMsg(2100, "Explorist Exporter"),MyLocale.getMsg(2101, "Failure at loading magellan.cfg\n" + e.getMessage()));
					info.show();
				} catch (IOException e) {
					InfoBox info = new InfoBox(MyLocale.getMsg(2100, "Explorist Exporter"),MyLocale.getMsg(2103, "Failure at reading magellan.cfg\n" + e.getMessage()));
					info.show();
				} finally {
					cacheDB.sort(new DistanceComparer(centre),false);
				}
			}
		}
		else {
			doIt(null);
		}
	}
	
	/**
	 * Does the most work for exporting data
	 */
	public void doIt(String baseFileName) {
		File outFile;
		String fileBaseName;
		String str = null;
		CacheHolder ch;
		CacheHolderDetail holder;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		if (baseFileName == null) {
			outFile = getOutputFile();
			if (outFile == null)
				return;
		} else {
			outFile = new File(baseFileName);
		}

		fileBaseName = outFile.getFullPath();
		// cut .gs
		fileBaseName = fileBaseName.substring(0, fileBaseName.length() - 3);

		pbf.showMainTask = false;
		pbf.setTask(h, "Exporting ...");
		pbf.exec();

		int counter = 0;
		int expCount = 0;
		for (int i = 0; i < cacheDB.size(); i++) {
			ch = (CacheHolder) cacheDB.get(i);
			if (ch.is_black() == false && ch.is_filtered() == false)
				counter++;
		}

		try {
			PrintWriter outp = null;
			for (int i = 0; i < cacheDB.size(); i++) {
				ch = (CacheHolder) cacheDB.get(i);
				if (ch.is_black() == false && ch.is_filtered() == false) {
					// all 200 caches we need a new file
					if (expCount % 200 == 0) {
						if (outp != null) {
							outp.close();
						}
						outp = new PrintWriter(new BufferedWriter(
								new FileWriter(new File(fileBaseName + expCount
										/ 200 + ".gs"))));
					}

					holder = new CacheHolderDetail(ch);
					expCount++;
					h.progress = (float) expCount / (float) counter;
					h.changed();
					try {
						if (needCacheDetails) {
							holder.readCache(profile.dataDir);
						}
					} catch (IOException e) {
						continue;
					}
					str = record(holder);
					if (str != null)
						outp.print(str);
				}// if

			}// for
			str = trailer();

			if (str != null)
				outp.print(str);

			outp.close();
			pbf.exit(0);
		} catch (IOException ioE) {
			Vm.debug("Error opening " + outFile.getName());
		}
		// try
	}

	/**
	 * uses a filechooser to get the name of the export file
	 * 
	 * @return
	 */
	public File getOutputFile() {
		File file;
		FileChooser fc = new FileChooser(FileChooserBase.SAVE, pref
				.getExportPath(expName));
		fc.setTitle(MyLocale.getMsg(2102, "Select target file:"));
		fc.addMask(mask);
		if (fc.execute() != FormBase.IDCANCEL) {
			file = fc.getChosenFile();
			pref.setExportPath(expName, file.getPath());
			return file;
		} else {
			return null;
		}
	}

	/**
	 * this method can be overided by an exporter class
	 * 
	 * @param ch
	 *            cachedata
	 * @return formated cache data
	 */
	public String record(CacheHolderDetail chD) {
		/*
		static protected final int GC_AW_PARKING = 50;
		static protected final int GC_AW_STAGE_OF_MULTI = 51;
		static protected final int GC_AW_QUESTION = 52;
		static protected final int GC_AW_FINAL = 53;
		static protected final int GC_AW_TRAILHEAD = 54;
		static protected final int GC_AW_REFERENCE = 55;
		*/
		StringBuffer sb = new StringBuffer();
		sb.append("$PMGNGEO,");
		sb.append(chD.pos.getLatDeg(CWPoint.DMM));
		sb.append(chD.pos.getLatMin(CWPoint.DMM));
		sb.append(",");
		sb.append("N,");
		sb.append(chD.pos.getLonDeg(CWPoint.DMM));
		sb.append(chD.pos.getLonMin(CWPoint.DMM));
		sb.append(",");
		sb.append("E,");
		sb.append("0000,"); // Height
		sb.append("M,"); // in meter
		sb.append(chD.getWayPoint());
		sb.append(",");
		String add = "";
		if (chD.isAddiWpt()) {
			if (chD.getType() == 50) {
				add = "Pa:";
			} else if (chD.getType() == 51) {
				add = "St:";
			} else if (chD.getType() == 52) {
				add = "Qu:"; 
			} else if (chD.getType() == 53) {	
				add = "Fi:";
			} else if (chD.getType() == 54) {
				add = "Tr:";
			} else if (chD.getType() == 55) {	
				add = "Re:";
			}
			sb.append(add).append(removeCommas(chD.getCacheName()));
		} else {
			sb.append(removeCommas(chD.getCacheName()));
		}		
		sb.append(",");
		sb.append(removeCommas(chD.getCacheOwner()));
		sb.append(",");
		sb.append(removeCommas(Common.rot13(chD.Hints)));
		sb.append(",");
		
		if (!add.equals("")) { // Set Picture in Explorist to Virtual
			sb.append("Virtual Cache");
		} else if (chD.getType() != 8) { // Rewrite Unknown Caches
			sb.append(CacheType.transType(chD.getType()));
		} else {
			sb.append("Mystery Cache");
		}
		sb.append(",");
		sb.append(toGsDateFormat(chD.getDateHidden()));  // created - DDMMYYY, YYY = year - 1900
		sb.append(",");
		String lastFound = "0000";
		for (int i = 0; i < chD.CacheLogs.size(); i++) {
			if (chD.CacheLogs.getLog(i).isFoundLog() && chD.CacheLogs.getLog(i).getDate().compareTo(lastFound) > 0 ) {
				lastFound = chD.CacheLogs.getLog(i).getDate();
			}
		}
		
		sb.append(toGsDateFormat(lastFound)); // lastFound - DDMMYYY, YYY = year - 1900
		sb.append(",");
		sb.append(removeCommas(chD.getHard()));
		sb.append(",");
		sb.append(removeCommas(chD.getTerrain()));
		sb.append("*41");
		return Exporter.simplifyString(sb.toString() + "\r\n");
	}

	/**
	 * this method can be overided by an exporter class
	 * 
	 * @return formated trailer data
	 */
	public String trailer() {
		return "$PMGNCMD,END*3D\n";
	}
	
	/**
	 * Changes "," in "." in the input String
	 * @param input
	 * @return changed String
	 */
	private String removeCommas(String input) {
		return input.replace(',', '.');
	}
	
	/**
	 * change the Dateformat from "yyyy-mm-dd" to ddmmyyy, where yyy is years after 1900 
	 * @param input Date in yyyy-mm-dd
 	 * @return Date in ddmmyyy
	 */
	private String toGsDateFormat(String input) {
		if (input.length() >= 10) {
			return input.substring(8, 10) + input.substring(5, 7) + "1" + input.substring(2, 4);
		} 
		else {
			return "";
		}
	}
	
}