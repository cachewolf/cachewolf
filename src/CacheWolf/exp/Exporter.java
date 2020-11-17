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
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheType;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.MyLocale;
import com.stevesoft.ewe_pat.Regex;
import com.stevesoft.ewe_pat.Transformer;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.io.*;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.ProgressBarForm;
import ewe.util.Hashtable;
import ewe.util.Vector;

/**
 * @author Kalle
 * Base class for exporter, handles basic things like selecting outputfile, display a counter etc.
 * <p>
 * setExportMethod must be called with  to identify which method should be called.
 * A new Exporter must only override the header(), record() and trailer() methods, corresponding to the setExportMethod.
 */

public class Exporter {

    // export methods
    final static int EXPORT_METHOD_NO_PARAMS = 0;
    final static int EXPORT_METHOD_LAT_LON = 1;
    final static int EXPORT_METHOD_COUNT = 2;
    private static Hashtable iso2simpleMappings = new Hashtable(250);

    //  ISO-8859-1 is CP1252 without chars 80-9f (=128..159) which is = 00..1f
    //  will be converted to  ISO-646 ( = ASCII, or US-ASCII)
    static {
        String[] mappingArray = new String[]{"34", "'", //
                "160", " ", "161", "i", "162", "c", "163", "$", "164", "o", "165", "$", "166", "!", "167", "$", "168", " ", "169", " ", //
                "170", " ", "171", "<", "172", " ", "173", "-", "174", " ", "175", "-", "176", " ", "177", "+/-", "178", "2", "179", "3", //
                "180", "'", "181", " ", "182", " ", "183", " ", "184", ",", "185", "1", "186", " ", "187", ">", "188", "1/4", "189", "1/2", //
                "190", "3/4", "191", "?", "192", "A", "193", "A", "194", "A", "195", "A", "196", "Ae", "197", "A", "198", "AE", "199", "C", //
                "200", "E", "201", "E", "202", "E", "203", "E", "204", "I", "205", "I", "206", "I", "207", "I", "208", "D", "209", "N", //
                "210", "O", "211", "O", "212", "O", "213", "O", "214", "Oe", "215", "x", "216", "O", "217", "U", "218", "U", "219", "U", //
                "220", "Ue", "221", "Y", "222", " ", "223", "ss", "224", "a", "225", "a", "226", "a", "227", "a", "228", "ae", "229", "a", //
                "230", "ae", "231", "c", "232", "e", "233", "e", "234", "e", "235", "e", "236", "i", "237", "i", "238", "i", "239", "i", //
                "240", "o", "241", "n", "242", "o", "243", "o", "244", "o", "245", "o", "246", "oe", "247", "/", "248", "o", "249", "u", //
                "250", "u", "251", "u", "252", "ue", "253", "y", "254", "p", "255", "y"};
        for (int i = 0; i < mappingArray.length; i = i + 2) {
            iso2simpleMappings.put(Integer.valueOf(mappingArray[i]), mappingArray[i + 1]);
        }
    }

    // name of exporter for saving pathname and its preferences
    protected String exporterName;
    protected TextCodec useCodec;
    protected int anzVisibleCaches;
    protected PrintWriter outWriter;
    protected File outFile = null;
    Vector DB;
    // mask in file chooser
    String outputFileExtension = "*.*";
    String outputFileName;
    // decimal separator for lat- and lon-String
    char decimalSeparator = '.';
    // selection, which export method should be called
    int recordMethod;
    int incompleteWaypoints = 0;
    int doneTillNow;
    ProgressBarForm pbf = new ProgressBarForm();
    Handle h = new Handle();

    public Exporter() {
        recordMethod = EXPORT_METHOD_LAT_LON;
        exporterName = this.getClass().getName();
        // remove package and path from Classname
        int ab = exporterName.lastIndexOf(".") + 1;
        exporterName = exporterName.substring(ab);
        useCodec = new JavaUtf8Codec();
        anzVisibleCaches = MainForm.profile.cacheDB.countVisible();
        doneTillNow = 0;
    }

    protected static String char2simpleChar(char c) {
        if (c < 127) {
            // leave alone as equivalent string.
            return null;
        } else {
            String s = (String) iso2simpleMappings.get(new Integer(c));
            if (s == null) // 127..159 not in table, replace with empty string
                return "";
            else
                return s;
        }
    } // end charToEntity

    public static String simplifyString(String text) {
        if (text == null)
            return null;
        int originalTextLength = text.length();
        StringBuffer sb = new StringBuffer(50);
        int charsToAppend = 0;
        for (int i = 0; i < originalTextLength; i++) {
            char c = text.charAt(i);
            String entity = char2simpleChar(c);
            if (entity == null) {
                // we could sb.append( c ), but that would be slower
                // than saving them up for a big append.
                charsToAppend++;
            } else {
                if (charsToAppend != 0) {
                    sb.append(text.substring(i - charsToAppend, i));
                    charsToAppend = 0;
                }
                sb.append(entity);
            }
        } // end for
        // append chars to the right of the last entity.
        if (charsToAppend != 0) {
            sb.append(text.substring(originalTextLength - charsToAppend, originalTextLength));
        }
        // if result is not longer, we did not do anything. Save RAM.
        return (sb.length() == originalTextLength) ? text : sb.toString();
    } // end insertEntities

    public static String getShortDetails(CacheHolder ch) {
        StringBuffer strBuf = new StringBuffer(7);
        strBuf.append(CacheType.getExportShortId(ch.getType()).toLowerCase());
        if (!ch.isAddiWpt()) {
            strBuf.append(ch.getDifficulty());
            strBuf.append("/");
            strBuf.append(ch.getTerrain());
            strBuf.append(CacheSize.getExportShortId(ch.getSize()));
        }

        return strBuf.toString();
    }

    /**
     * Does the work for exporting data
     */
    public void doIt() {
        doItStart();
        export();
        doItEnd();
    }

    public void doItStart() {
        pbf.showMainTask = false;
        pbf.setTask(h, "Exporting ...");
        pbf.exec();
    }

    public void doItEnd() {
        pbf.exit(0);
        if (incompleteWaypoints > 0) {
            new InfoBox(MyLocale.getMsg(5500, "Error"), incompleteWaypoints + " incomplete waypoints have not been exported. See log for details.").wait(FormBase.OKB);
        }
    }

    /**
     * Do one File export, can be overwritten
     */
    public void export() {
        if (DB == null) {
            DB = MainForm.profile.cacheDB.getVectorDB();
        }

        if (outFile == null) {
            askForOutputFile();
            if (outFile == null)
                return;
        }
        exportHeader();
        exportBody();
        exportTrailer();
    }

    public void exportHeader() {

        try {
            FileWriter fw = new FileWriter(outFile);
            fw.codec = this.useCodec;
            outWriter = new PrintWriter(new BufferedWriter(fw));
        } catch (IOException ioE) {
            Preferences.itself().log("Error opening " + outputFileName, ioE);
        }

        String str = this.header();
        if (str != null)
            outWriter.write(str);
    }

    public void exportBody() {
        exportCaches();
    }

    public void exportTrailer() {
        String str;
        switch (this.recordMethod & EXPORT_METHOD_COUNT) {
            case EXPORT_METHOD_NO_PARAMS:
                str = trailer();
                break;
            case EXPORT_METHOD_COUNT:
                str = trailer(anzVisibleCaches);
                break;
            default:
                str = null;
                break;
        }
        if (str != null)
            outWriter.write(str);

        outWriter.close();

    }

    private void exportCaches() {
        String str;
        for (int i = 0; i < DB.size(); i++) {
            str = exportCache(i);
            if (str != null) {
                h.progress = (float) doneTillNow / (float) anzVisibleCaches;
                h.changed();
                outWriter.write(str);
            }
        }
    }

    private String exportCache(int i) {
        CacheHolder ch = (CacheHolder) DB.get(i);
        String str = null;
        if (ch.isVisible()) {
            doneTillNow++;
            if (ch.isIncomplete()) {
                Preferences.itself().log("Incomplete waypoint " + ch.getCode(), null);
                incompleteWaypoints++;
                return null;
            } else {
                switch (this.recordMethod) {
                    case EXPORT_METHOD_NO_PARAMS:
                        str = record(ch);
                        break;
                    case EXPORT_METHOD_LAT_LON:
                        if (!ch.getWpt().isValid())
                            return null;
                        str = record(ch, ch.getWpt().getLatDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator), ch.getWpt().getLonDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator));
                        break;
                    case EXPORT_METHOD_LAT_LON | EXPORT_METHOD_COUNT:
                        if (!ch.getWpt().isValid())
                            return null;
                        str = record(ch, ch.getWpt().getLatDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator), ch.getWpt().getLonDeg(TransformCoordinates.DD).replace('.', this.decimalSeparator), i);
                        break;
                    default:
                        str = null;
                        break;
                }
            } // else if incomplete
        } // if visible
        return str;
    }

    /**
     * sets mask for filechooser
     *
     * @param mask
     */
    public void setOutputFileExtension(String mask) {
        this.outputFileExtension = mask;
    }

    /**
     * sets ExportMethod
     *
     * @param paramBits
     */
    public void setExportMethod(int paramBits) {
        this.recordMethod = paramBits;
    }

    /**
     * sets tmpFileName
     *
     * @param fName
     */
    public void setOutputFile(String fName) {
        this.outputFileName = fName;
        outFile = new File(outputFileName);
    }

    /**
     * uses a filechooser to get the name of the export file
     *
     * @return
     */
    public void askForOutputFile() {
        File file;
        FileChooser fc = new FileChooser(FileChooserBase.SAVE, Preferences.itself().getExportPath(exporterName + "-path"));
        fc.setTitle(MyLocale.getMsg(2102, "Choose target file"));
        fc.addMask(outputFileExtension);
        if (fc.execute() != FormBase.IDCANCEL) {
            file = fc.getChosenFile();
            this.outputFileName = fc.getChosen();
            Preferences.itself().setExportPref(exporterName + "-path", file.getPath());
            outFile = file;
        } else {
            outFile = null;
        }
    }

    public String getOutputPath() {
        FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, Preferences.itself().getExportPath(exporterName + "-path"));
        fc.setTitle(MyLocale.getMsg(148, "Select Target directory"));
        String targetDir;
        if (fc.execute() == FormBase.IDCANCEL)
            return "";
        targetDir = fc.getChosen() + "/";
        Preferences.itself().setExportPref(exporterName + "-path", targetDir);
        return targetDir;
    }

    /**
     * this method can be overided by an exporter class
     *
     * @return formated header data
     */
    public String header() {
        return null;
    }

    // /////////////////////////////////////////////////
    // Helper functions for string sanitisation
    // /////////////////////////////////////////////////

    /**
     * this method can be overided by an exporter class
     *
     * @param ch cachedata
     * @return formated cache data
     */
    public String record(CacheHolder chD) {
        return null;
    }

    /**
     * this method can be overided by an exporter class
     *
     * @param ch  cachedata
     * @param lat
     * @param lon
     * @return formated cache data
     */
    public String record(CacheHolder ch, String lat, String lon) {
        return null;
    }

    /**
     * this method can be overided by an exporter class
     *
     * @param ch    cachedata
     * @param lat
     * @param lon
     * @param count of actual record
     * @return formated cache data
     */
    public String record(CacheHolder ch, String lat, String lon, int count) {
        return null;
    }

    /**
     * this method can be overided by an exporter class
     *
     * @return formated trailer data
     */
    public String trailer() {
        return null;
    }

    /**
     * this method can be overided by an exporter class
     *
     * @param total count of exported caches
     * @return
     */
    public String trailer(int total) {
        return null;
    }

    protected String removeHtmlTags(String inString) {

        Transformer removeNumericEntities = new Transformer(true);
        removeNumericEntities.add(new Regex("&#([xX]?)([a-fA-F0-9]*?);", ""));

        Transformer handleLinebreaks = new Transformer(true);
        handleLinebreaks.add(new Regex("\r", ""));
        handleLinebreaks.add(new Regex("\n", " "));
        handleLinebreaks.add(new Regex("<br>", "\n"));
        handleLinebreaks.add(new Regex("<p>", "\n"));
        handleLinebreaks.add(new Regex("<hr>", "\n"));
        handleLinebreaks.add(new Regex("<br />", "\n"));

        Transformer removeHTMLTags = new Transformer(true);
        removeHTMLTags.add(new Regex("<(.*?)>", ""));

        return removeHTMLTags.replaceAll(handleLinebreaks.replaceAll(removeNumericEntities.replaceAll(inString)));

    }
}
