package exp;

import CacheWolf.*;
import ewe.sys.*;
import ewe.filechooser.FileChooser;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileWriter;
import ewe.io.PrintWriter;
import ewe.ui.ProgressBarForm;
import ewe.util.*;
import ewe.io.IOException;

/**
 * @author Kalle
 * @author TweetyHH Class for Exporting direct to Magellans *.gs Files. Caches
 *         will be exported in files with maximum of 200 Caches.
 */

public class MagellanExporter {
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

	public MagellanExporter(Preferences p, Profile prof) {
		profile = prof;
		pref = p;
		cacheDB = profile.cacheDB;
		expName = this.getClass().getName();
		// remove package
		expName = expName.substring(expName.indexOf(".") + 1);
	}

	/**
	 * Does the most work for exporting data
	 */
	public void doIt() {
		File outFile;
		String fileBaseName;
		String str = null;
		CacheHolder ch;
		CacheHolderDetail holder;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		outFile = getOutputFile();
		if (outFile == null)
			return;

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
			if (ch.is_black == false && ch.is_filtered == false)
				counter++;
		}

		try {
			PrintWriter outp = null;
			for (int i = 0; i < cacheDB.size(); i++) {
				ch = (CacheHolder) cacheDB.get(i);
				if (ch.is_black == false && ch.is_filtered == false) {
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
		FileChooser fc = new FileChooser(FileChooser.SAVE, pref
				.getExportPath(expName));
		fc.setTitle("Select target file:");
		fc.addMask(mask);
		if (fc.execute() != FileChooser.IDCANCEL) {
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
		sb.append("0000,");
		sb.append("F,"); // or "M" ?
		sb.append(chD.wayPoint);
		sb.append(",");
		sb.append(removeCommas(chD.CacheName));
		sb.append(",");
		sb.append(removeCommas(chD.CacheOwner));
		sb.append(",");
		sb.append(removeCommas(Common.rot13(chD.Hints)));
		sb.append(",");
		// Rewrite Unknown Caches
		if (!chD.type.equals("8")) {
			sb.append(CacheType.transType(chD.type));
		} else {
			sb.append("Mystery Cache");
		}
		sb.append(",");
		sb.append(""); // UNKNOWN // Time
		sb.append(",");
		sb.append(""); // UNKNOWN // TIME
		sb.append(",");
		sb.append(removeCommas(chD.hard));
		sb.append(",");
		sb.append(removeCommas(chD.terrain));
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
}
