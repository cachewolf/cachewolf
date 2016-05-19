/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
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

import CacheWolf.MainForm;
import CacheWolf.Preferences;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheHolderDetail;
import CacheWolf.database.CacheType;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.STRreplace;
import CacheWolf.utils.SafeXML;
import ewe.io.BufferedWriter;
import ewe.io.FileBase;
import ewe.io.FileOutputStream;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.PrintWriter;
import ewe.sys.Handle;
import ewe.ui.ProgressBarForm;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Map.MapEntry;
import ewe.util.Vector;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;

/**
 * Class to export the cache database (index) to an KML-File
 * which can be read by Google Earth
 * 
 */
public class KMLExporter extends Exporter {
    private static final String COLOR_FOUND = "ff98fb98";
    private static final String COLOR_OWNED = "ffffaa55";
    private static final String COLOR_AVAILABLE = "ffffffff";
    private static final String COLOR_NOT_AVAILABLE = "ff0000ff";

    static final int AVAILABLE = 0;
    static final int FOUND = 1;
    static final int OWNED = 2;
    static final int NOT_AVAILABLE = 3;
    static final int UNKNOWN = 4;

    String[] categoryNames = { "Available", "Found", "Owned", "Not Available", "UNKNOWN" };
    Hashtable[] outDB = new Hashtable[categoryNames.length];

    public KMLExporter() {
	super();
	this.setOutputFileExtension("*.kml");
    }

    public void doIt() {
	String str;
	CacheHolder ch;
	CacheHolder addiWpt;
	ProgressBarForm pbf = new ProgressBarForm();
	Handle h = new Handle();

	if (outFile == null) {
	    askForOutputFile();
	    if (outFile == null)
		return;
	}

	pbf.showMainTask = false;
	pbf.setTask(h, "Exporting ...");
	pbf.exec();

	DB = MainForm.profile.cacheDB.getVectorDB();
	int expCount = 0;
	copyIcons(outFile.getParent());
	buildOutDB();

	try {
	    PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
	    str = STRreplace.replace(this.header(), "CacheWolf", MainForm.profile.name);
	    if (str != null)
		outp.print(str);
	    for (int cat = 0; cat < categoryNames.length; cat++) {
		// skip over empty categories
		if (outDB[cat].size() == 0)
		    continue;

		Iterator outLoop = outDB[cat].entries();
		outp.print(startFolder(categoryNames[cat]));

		Vector tmp;
		MapEntry entry;
		while (outLoop.hasNext()) {
		    entry = (MapEntry) outLoop.next();
		    tmp = (Vector) entry.getValue();
		    // skip over empty cachetypes
		    if (tmp.size() == 0)
			continue;
		    outp.print(startFolder(CacheType.type2Gui(Integer.valueOf(entry.getKey().toString()).byteValue())));

		    for (int i = 0; i < tmp.size(); i++) {
			ch = (CacheHolder) tmp.get(i);
			if (ch.isAddiWpt())
			    continue;
			expCount++;
			h.progress = (float) expCount / (float) anzVisibleCaches;
			h.changed();

			if (ch.getWpt().isValid()) {
			    str = record(ch, ch.getWpt().getLatDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator), ch.getWpt().getLonDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator));
			    if (str != null)
				outp.print(str);
			}
			if (ch.hasAddiWpt()) {
			    boolean createdAdditionalWaypointsFolder = false;
			    for (int j = 0; j < ch.addiWpts.size(); j++) {
				addiWpt = (CacheHolder) ch.addiWpts.get(j);
				expCount++;
				if (ch.getWpt().isValid() && addiWpt.isVisible()) {
				    if (!createdAdditionalWaypointsFolder) {
					outp.print(startFolder("Additional Waypoints", false));
					createdAdditionalWaypointsFolder = true;
				    }
				    str = record(addiWpt, addiWpt.getWpt().getLatDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator), addiWpt.getWpt().getLonDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator));
				    if (str != null)
					outp.print(str);
				}

			    }
			    if (createdAdditionalWaypointsFolder) {
				outp.print(endFolder());// addi wpts
			    }
			}
		    }
		    outp.print(endFolder());// cachetype
		}
		outp.print(endFolder());// category
	    }

	    str = trailer();
	    if (str != null)
		outp.print(str);
	    outp.close();
	    pbf.exit(0);
	} catch (IOException ioE) {
	    Preferences.itself().log("Error opening " + outFile.getName(), ioE);
	}
	// try

    }

    private void buildOutDB() {
	CacheHolder ch;
	Vector tmp;

	// create the roots for the different categories
	for (int i = 0; i < categoryNames.length; i++) {
	    outDB[i] = new Hashtable();
	}

	// fill structure with data from cacheDB
	for (int i = 0; i < DB.size(); i++) {
	    ch = (CacheHolder) DB.get(i);
	    if (ch.isVisible() && !ch.isAddiWpt()) {
		if (ch.isFound()) {
		    tmp = (Vector) outDB[FOUND].get(new Byte(ch.getType()));
		    if (tmp == null) {
			tmp = new Vector();
			outDB[FOUND].put(new Byte(ch.getType()), tmp);
		    }
		} else if (ch.isOwned()) {
		    tmp = (Vector) outDB[OWNED].get(new Byte(ch.getType()));
		    if (tmp == null) {
			tmp = new Vector();
			outDB[OWNED].put(new Byte(ch.getType()), tmp);
		    }
		} else if (ch.isArchived() || !ch.isAvailable()) {
		    tmp = (Vector) outDB[NOT_AVAILABLE].get(new Byte(ch.getType()));
		    if (tmp == null) {
			tmp = new Vector();
			outDB[NOT_AVAILABLE].put(new Byte(ch.getType()), tmp);
		    }
		} else if (ch.isAvailable()) {
		    tmp = (Vector) outDB[AVAILABLE].get(new Byte(ch.getType()));
		    if (tmp == null) {
			tmp = new Vector();
			outDB[AVAILABLE].put(new Byte(ch.getType()), tmp);
		    }
		} else {
		    tmp = (Vector) outDB[UNKNOWN].get(new Byte(ch.getType()));
		    if (tmp == null) {
			tmp = new Vector();
			outDB[UNKNOWN].put(new Byte(ch.getType()), tmp);
		    }
		}
		tmp.add(ch);
	    }
	}
    }

    private String startFolder(String name) {
	return startFolder(name, true);
    }

    private String startFolder(String name, boolean open) {
	StringBuffer strBuf = new StringBuffer(200);
	strBuf.append("<Folder>\r\n");
	strBuf.append("<name>" + name + "</name>\r\n");
	strBuf.append("<open>" + (open ? "1" : "0") + "</open>\r\n");

	return strBuf.toString();
    }

    private String endFolder() {

	return "</Folder>\r\n";
    }

    public void copyIcons(String dir) {
	ZipFile zif = null;
	try {
	    zif = new ZipFile(FileBase.getProgramDirectory() + FileBase.separator + "exporticons" + FileBase.separator + "GoogleEarth.zip");
	} catch (IOException e) {
	}
	try {
	    if (zif == null) {
		zif = new ZipFile(FileBase.getProgramDirectory() + FileBase.separator + "exporticons" + FileBase.separator + "exporticons" + FileBase.separator + "GoogleEarth.zip");
	    }
	    ZipEntry zipEnt;
	    int len;
	    String fileName;

	    for (int i = 0; i < CacheType.guiTypeStrings().length; i++) {
		fileName = CacheType.typeImageForId(CacheType.guiSelect2Cw(i));
		zipEnt = zif.getEntry(fileName);
		if (zipEnt == null)
		    continue;
		byte[] buff = new byte[zipEnt.getSize()];
		InputStream fis = zif.getInputStream(zipEnt);
		FileOutputStream fos = new FileOutputStream(dir + "/" + fileName);
		while (0 < (len = fis.read(buff)))
		    fos.write(buff, 0, len);
		fos.flush();
		fos.close();
		fis.close();
	    }

	} catch (ZipException e) {
	    Preferences.itself().log("Problem copying Icon", e, true);
	} catch (IOException e) {
	    Preferences.itself().log("Problem copying Icon", e, true);
	}
    }

    StringBuffer strBuf = new StringBuffer(200);

    public String header() {
	strBuf.setLength(0);

	strBuf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
	strBuf.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n");
	strBuf.append("<Folder>\r\n");
	strBuf.append("<name>CacheWolf</name>\r\n");
	strBuf.append("<open>1</open>\r\n");

	return strBuf.toString();
    }

    public String record(CacheHolder ch, String lat, String lon) {
	strBuf.setLength(0);
	CacheHolderDetail det = ch.getDetails();

	strBuf.append("   <Placemark>\r\n");
	if (det.URL != null) {
	    strBuf.append("      <description>" + SafeXML.string2Html(det.URL) + "</description>\r\n");
	}
	strBuf.append("      <name>" + ch.getCode() + " - " + SafeXML.string2Html(ch.getName()) + "</name>\r\n");
	strBuf.append("      <LookAt>\r\n");
	strBuf.append("         <latitude>" + lat + "</latitude>\r\n");
	strBuf.append("         <longitude>" + lon + "</longitude>\r\n");
	strBuf.append("         <range>10000</range><tilt>0</tilt><heading>0</heading>\r\n");
	strBuf.append("      </LookAt>\r\n");
	strBuf.append("      <Point>\r\n");
	strBuf.append("         <coordinates>" + lon + "," + lat + "</coordinates>\r\n");
	strBuf.append("      </Point>\r\n");
	strBuf.append("      <Style>\r\n");
	strBuf.append("      <IconStyle>\r\n");
	strBuf.append("         <Icon>\r\n");
	// strBuf.append("            <href>"+ File.getProgramDirectory()+ "/" + CacheType.type2pic(Convert.parseInt(ch.type))+ "</href>\r\n");
	strBuf.append("            <href>" + CacheType.typeImageForId(ch.getType()) + "</href>\r\n");
	strBuf.append("         </Icon>\r\n");
	strBuf.append("      </IconStyle>\r\n");
	strBuf.append("      <LabelStyle>\r\n");
	strBuf.append("         <color>" + getColor(ch) + "</color>\r\n");
	strBuf.append("         <scale>" + "0.7" + "</scale>\r\n");
	strBuf.append("      </LabelStyle>\r\n");
	strBuf.append("      </Style>\r\n");
	strBuf.append("   </Placemark>\r\n");

	return strBuf.toString();
    }

    public String trailer() {
	strBuf.setLength(0);

	strBuf.append("</Folder>\r\n");
	strBuf.append("</kml>\r\n");

	return strBuf.toString();
    }

    private String getColor(CacheHolder ch) {
	if (ch.isFound())
	    return COLOR_FOUND;
	if (ch.isOwned())
	    return COLOR_OWNED;
	if (ch.isArchived() || !ch.isAvailable())
	    return COLOR_NOT_AVAILABLE;

	return COLOR_AVAILABLE;
    }

}
