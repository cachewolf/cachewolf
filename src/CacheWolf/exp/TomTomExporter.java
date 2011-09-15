/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package CacheWolf.exp;

import CacheWolf.CWPoint;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.CacheSize;
import CacheWolf.CacheType;
import CacheWolf.Common;
import CacheWolf.Global;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileOutputStream;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.RandomAccessFile;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.ProgressBarForm;
import ewe.util.ByteArray;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;

public class TomTomExporter {
	public final static int TT_ASC = 0;
	public final static int TT_OV2 = 1;
	public final static int TT_WPT_TEXT = 0;
	public final static int TT_WPT_NUM = 1;
	public final static String expName = "TomTom";

	CacheDB cacheDB;
	Preferences pref;
	Profile profile;

	public TomTomExporter() {
		profile = Global.getProfile();
		pref = Global.getPref();
		cacheDB = profile.cacheDB;
	}

	public void doIt() {
		String fileName, dirName, prefix;
		int fileFormat;

		TomTomExporterScreen infoScreen = new TomTomExporterScreen("TomTomExport");
		if (infoScreen.execute() == FormBase.IDCANCEL)
			return;
		fileFormat = infoScreen.getFormat();

		dirName = pref.getExportPath(expName);

		if (infoScreen.oneFilePerType()) {
			FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, dirName);
			fc.setTitle("Select target dir:");
			if (fc.execute() == FormBase.IDCANCEL)
				return;
			dirName = fc.getChosen();
			pref.setExportPath(expName, dirName);
			prefix = infoScreen.getPrefix();
			writeOneFilePerType(fileFormat, dirName, prefix);
		} else {
			FileChooser fc = new FileChooser(FileChooserBase.SAVE, dirName);
			fc.setTitle("Select target file:");

			if (fileFormat == TT_ASC)
				fc.addMask("*.asc");
			else
				fc.addMask("*.ov2");

			if (fc.execute() == FormBase.IDCANCEL)
				return;
			fileName = fc.getChosen();
			pref.setExportPathFromFileName(expName, fileName);
			writeSingleFile(fileFormat, fileName);
		}
	}

	public void writeOneFilePerType(int format, String dirName, String prefix) {
		RandomAccessFile out = null;
		File dfile;
		String ext, fileName = null;

		CacheHolder holder;
		ProgressBarForm progressForm = new ProgressBarForm();
		Handle h = new Handle();
		int currExp, counter;

		progressForm.showMainTask = false;
		progressForm.setTask(h, "Exporting ...");
		progressForm.exec();

		currExp = 0;
		counter = cacheDB.countVisible();

		ext = format == TT_ASC ? ".asc" : ".ov2";

		try {
			// loop through type
			for (int j = 0; j < CacheType.guiTypeStrings().length; j++) {
				/*
				 * String typeName = CacheType.guiTypeStrings()[j];
				 * if (typeName.startsWith("Addi: ")) {
				 * typeName = typeName.substring(6);
				 * }
				 */
				String typeName = CacheType.typeImageForId(CacheType.guiSelect2Cw(j));
				typeName = typeName.substring(0, typeName.length() - 4);

				fileName = dirName + "/" + prefix + typeName + ext;
				dfile = new File(fileName);
				dfile.delete();
				out = new RandomAccessFile(fileName, "rw");
				for (int i = 0; i < cacheDB.size(); i++) {
					holder = cacheDB.get(i);

					if (holder.getType() == CacheType.guiSelect2Cw(j) && holder.isVisible()) {
						currExp++;
						h.progress = (float) currExp / (float) counter;
						h.changed();
						if (holder.getPos().isValid() == false)
							continue;
						if (format == TT_ASC) {
							writeRecordASCII(out, holder, holder.getPos().getLatDeg(CWPoint.DD), holder.getPos().getLonDeg(CWPoint.DD));
						} else {
							writeRecordBinary(out, holder, holder.getPos().getLatDeg(CWPoint.DD), holder.getPos().getLonDeg(CWPoint.DD));
						}
					}// if
				}// for cacheDB
				out.close();
				// check for empty files and delete them
				dfile = new File(fileName);
				if (dfile.length() == 0) {
					dfile.delete();
				} else {
					copyIcon(j, dirName + "/" + prefix, typeName);
				}
			}// for wayType
			progressForm.exit(0);
		} catch (IOException e) {
			pref.log("Problem creating file! " + fileName, e, true);
		}// try
	}

	public void writeSingleFile(int format, String fileName) {
		RandomAccessFile out = null;
		File dfile;

		CacheHolder holder;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		pbf.showMainTask = false;
		pbf.setTask(h, "Exporting ...");
		pbf.exec();

		int counter = cacheDB.countVisible();
		int expCount = 0;

		try {
			dfile = new File(fileName);
			dfile.delete();
			out = new RandomAccessFile(fileName, "rw");
			for (int i = 0; i < cacheDB.size(); i++) {
				holder = cacheDB.get(i);
				if (holder.isVisible()) {
					expCount++;
					h.progress = (float) expCount / (float) counter;
					h.changed();
					if (holder.getPos().isValid() == false)
						continue;
					if (format == TT_ASC) {
						writeRecordASCII(out, holder, holder.getPos().getLatDeg(CWPoint.DD), holder.getPos().getLonDeg(CWPoint.DD));
					} else {
						writeRecordBinary(out, holder, holder.getPos().getLatDeg(CWPoint.DD), holder.getPos().getLonDeg(CWPoint.DD));
					}
				}// if
			}// for
			out.close();
			copyIcon(0, fileName.substring(0, fileName.indexOf(".")), "");
			pbf.exit(0);
		} catch (Exception e) {
			pref.log("Problem writing to file! " + fileName, e, true);
		}// try
	}

	public void writeRecordASCII(RandomAccessFile outp, CacheHolder ch, String lat, String lon) {
		try {
			outp.writeBytes(lon);
			outp.writeBytes(",");
			outp.writeBytes(lat);
			outp.writeBytes(",");
			// outp.writeBytes("\"" + ch.CacheName.replace(',',' ') + "\"\r\n");
			outp.writeBytes("\"");
			outp.writeBytes(ch.getWayPoint());
			outp.writeBytes(" - ");
			outp.writeBytes(ch.getCacheName().replace(',', ' '));
			outp.writeBytes(" by ");
			outp.writeBytes(ch.getCacheOwner());
			outp.writeBytes("- ");
			outp.writeBytes(String.valueOf(ch.getHard()));
			outp.writeBytes("/");
			outp.writeBytes(String.valueOf(ch.getTerrain()));
			outp.writeBytes(" - ");
			outp.writeBytes(CacheSize.cw2ExportString(ch.getCacheSize()));
			outp.writeBytes("\"\r\n");
		} catch (IOException e) {
			pref.log("Error writing to file", e, true);
		}
		return;
	}

	public void writeRecordBinary(RandomAccessFile outp, CacheHolder ch, String lat, String lon) {
		int d, data;
		double latlon;

		try {
			d = 2;
			outp.writeByte((byte) d);
			data = ch.getWayPoint().length() + ch.getCacheName().length() + ch.getCacheOwner().length() + String.valueOf(ch.getHard()).length() + String.valueOf(ch.getTerrain()).length() + CacheSize.cw2ExportString(ch.getCacheSize()).length() + 27;
			writeIntBinary(outp, data);
			latlon = Common.parseDouble(lon);
			latlon *= 100000;
			writeIntBinary(outp, (int) latlon);
			latlon = Common.parseDouble(lat);
			;
			latlon *= 100000;
			writeIntBinary(outp, (int) latlon);
			outp.writeBytes(ch.getWayPoint());
			outp.writeBytes(" - ");
			outp.writeBytes(ch.getCacheName());
			outp.writeBytes(" by ");
			outp.writeBytes(ch.getCacheOwner());
			// Wenn Leerzeichen am Ende von Cache.Owner entfernt:
			// Hier wieder einfügen
			// und data = holder.wayPoint.length()+holder.CacheName.length()+.....
			// wider um 1 erhöhen
			outp.writeBytes("- ");
			outp.writeBytes(String.valueOf(ch.getHard()));
			outp.writeBytes("/");
			outp.writeBytes(String.valueOf(ch.getTerrain()));
			outp.writeBytes(" - ");
			outp.writeBytes(CacheSize.cw2ExportString(ch.getCacheSize()));
			d = 0;
			outp.writeByte((byte) d);
		} catch (IOException e) {
			pref.log("Error writing to file", e, true);
		}

		return;
	}

	public void writeIntBinary(RandomAccessFile outp, int data) {

		ByteArray buf = new ByteArray();
		buf.appendInt(data);
		try {
			outp.writeByte(buf.data[3]);
			outp.writeByte(buf.data[2]);
			outp.writeByte(buf.data[1]);
			outp.writeByte(buf.data[0]);
		} catch (IOException e) {
			pref.log("Error writing to file", e, true);
		}

		return;
	}

	public void copyIcon(int intWayType, String prefix, String typeName) {
		ZipFile zif = null;
		try {
			zif = new ZipFile(FileBase.getProgramDirectory() + FileBase.separator + "exporticons" + FileBase.separator + "TomTom.zip");
		} catch (IOException e) {
		}
		try {
			if (zif == null) {
				zif = new ZipFile(FileBase.getProgramDirectory() + FileBase.separator + "exporticons" + FileBase.separator + "exporticons" + FileBase.separator + "TomTom.zip");
			}
			ZipEntry zipEnt;
			int len;
			String entName;

			entName = "GC-" + typeName + ".bmp";
			zipEnt = zif.getEntry(entName);
			if (zipEnt == null)
				return;

			byte[] buff = new byte[zipEnt.getSize()];
			InputStream fis = zif.getInputStream(zipEnt);
			FileOutputStream fos = new FileOutputStream(prefix + typeName + ".bmp");
			while (0 < (len = fis.read(buff)))
				fos.write(buff, 0, len);
			fos.flush();
			fos.close();
			fis.close();
		} catch (ZipException e) {
			pref.log("Problem copying Icon " + "GC-" + typeName + ".bmp", e, true);
		} catch (IOException e) {
			pref.log("Problem copying Icon " + "GC-" + typeName + ".bmp", e, true);
		}
	}

}
