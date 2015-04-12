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
package CacheWolf.imp;

import CacheWolf.MainForm;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.Log;
import CacheWolf.utils.STRreplace;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.io.TextCodec;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.mString;

public class FieldnotesImporter {
    CacheDB cacheDB;
    String file;

    public FieldnotesImporter(String f) {
	cacheDB = MainForm.profile.cacheDB;
	file = f;
    }

    public void doIt() {
	try {
	    Vm.showWait(true);
	    FileReader r = null;

	    try {
		r = new FileReader(file);
		r.codec = new NoCodec();
		r.read(); // for checking of Codec
		r.close();
		r = new FileReader(file);
		r.codec = new NoCodec(true);
	    } catch (Error e) {
		r.close();
		r = new FileReader(file);
		if (e.getMessage().equals("UTF-8")) {
		    r.codec = new JavaUtf8Codec();
		} else if (e.getMessage().equals("ASCII")) {
		    r.codec = new ewe.io.AsciiCodec();
		} else {
		    Vm.showWait(false);
		    return;
		}
	    }
	    parse(r.readAll());
	    r.close();
	    // save Index
	    MainForm.profile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
	    Vm.showWait(false);
	} catch (Exception e) {
	    Vm.showWait(false);
	}
    }

    private void parse(String s) {
	//s = STRreplace.replace(s, "\r\n", "\n");
	//s = STRreplace.replace(s, "\n", "\r\n");
	final byte WPPOS = 0;
	final byte DATEPOS = 1;
	final byte LOGTYPPOS = 2;
	String[] l = mString.split(s, '"');

	long timeZoneOffset = MainForm.profile.getTimeZoneOffsetLong();

	for (int i = 0; i < l.length; i++) {
	    String s1 = l[i];
	    i++;
	    String logText = l[i];
	    String[] l1 = mString.split(s1, ',');
	    while (l1[WPPOS].charAt(0) < 48) {
		l1[WPPOS] = l1[WPPOS].substring(1);
	    }
	    while (l1[WPPOS].charAt(0) > 122) {
		l1[WPPOS] = l1[WPPOS].substring(1);
	    }
	    String wayPoint = l1[WPPOS];
	    CacheHolder ch = cacheDB.get(wayPoint);
	    Time logTime = new Time();
	    String foundIcon = "";
	    if (ch != null) {
		if (l1[LOGTYPPOS].equals(ch.getGCFoundText())) {
		    // String stmp=ch.getCacheStatus();

		    String logTimeString = l1[DATEPOS].replace('T', ' ').replace('Z', ' ').trim();

		    //if (timeZoneOffset != 0 || MainForm.profile.getTimeZoneAutoDST()) {
		    try {
			logTime.parse(logTimeString, "yyyy-MM-dd HH:mm");

			long timeZoneOffsetMillis = 0;

			if (timeZoneOffset == 100) { //autodetect
			    timeZoneOffsetMillis = Time.convertSystemTime(logTime.getTime(), false) - logTime.getTime();
			} else {
			    timeZoneOffsetMillis = timeZoneOffset * 3600000;
			}

			if (MainForm.profile.getTimeZoneAutoDST()) {
			    int lsM = (byte) (31 - ((int) (5 * logTime.year / 4) + 4) % 7);//last Sunday in March
			    int lsO = (byte) (31 - ((int) (5 * logTime.year / 4) + 1) % 7);//last Sunday in October

			    Time dstStart = new Time(lsM, 3, logTime.year);
			    dstStart.hour = 2;
			    dstStart.setTime(dstStart.getTime() - timeZoneOffsetMillis);
			    Time dstEnd = new Time(lsO, 10, logTime.year);
			    dstEnd.hour = 1;
			    dstEnd.minute = 59;
			    dstEnd.setTime(dstEnd.getTime() - timeZoneOffsetMillis);

			    if (logTime.after(dstStart) && logTime.before(dstEnd)) {
				timeZoneOffsetMillis += 3600000;
			    }
			}

			logTime.setTime(logTime.getTime() + timeZoneOffsetMillis);
			logTimeString = logTime.format("yyyy-MM-dd HH:mm");
		    } catch (IllegalArgumentException e) {
		    }
		    //}

		    ch.setCacheStatus(logTimeString);
		    ch.setFound(true);
		    foundIcon = ch.getGCFoundIcon();
		} else {
		    String stmp = ch.getCWLogText(l1[LOGTYPPOS]);
		    if (stmp.equals(""))
			ch.setCacheStatus(l1[LOGTYPPOS]); // eingelesener
		    else
			ch.setCacheStatus(stmp); // Statustext (ohne Datum/Uhrzeit)
		    ch.setFound(false);
		    foundIcon = "3.png";
		}
		if (logText.length() > 0) {
		    ch.getCacheDetails(false).OwnLog = new Log("", Preferences.itself().gcMemberId, foundIcon, logTime.format("yyyy-MM-dd"), Preferences.itself().myAlias, STRreplace.replace(logText, "\n", "<br />"));
		    ch.save();
		}
	    }
	}
    }

}

//##################################################################
class NoCodec implements TextCodec {
    //##################################################################

    /**
     * This is a creation option. It specifies that CR characters should be removed when
     * encoding text into UTF.
     **/
    public static final int STRIP_CR_ON_DECODE = 0x1;
    /**
     * This is a creation option. It specifies that CR characters should be removed when
     * decoding text from UTF.
     **/
    public static final int STRIP_CR_ON_ENCODE = 0x2;
    /**
     * This is a creation option. It specifies that CR characters should be removed when
     * decoding text from UTF AND encoding text to UTF.
     **/
    public static final int STRIP_CR = STRIP_CR_ON_DECODE | STRIP_CR_ON_ENCODE;

    private int flags = 0;
    private boolean checked = false;

    //===================================================================
    public NoCodec(boolean _checked)
    //===================================================================
    {
	checked = _checked;
	flags = 0;
    }

    //===================================================================
    public NoCodec()
    //===================================================================
    {
	checked = false;
	flags = 0;
    }

    //===================================================================
    public ByteArray encodeText(char[] text, int start, int length, boolean endOfData, ByteArray dest) throws IOException
    //===================================================================
    {
	if (dest == null)
	    dest = new ByteArray();
	int size = length == 0 ? 2 : 2 + text.length * 2;
	if (dest.data == null || dest.data.length < size)
	    dest.data = new byte[size];
	byte[] destination = dest.data;
	int s = 0;
	if (length > 0) {
	    destination[s++] = (byte) 0xFF;
	    destination[s++] = (byte) 0xFE;
	}
	for (int i = 0; i < length; i++) {
	    char c = text[i + start];
	    if (c == 13 && ((flags & STRIP_CR_ON_ENCODE) != 0))
		continue;
	    destination[s++] = (byte) (c & 0xFF);
	    destination[s++] = (byte) ((c >> 8) & 0xFF);
	}
	dest.length = s;
	return dest;
    }

    //===================================================================
    public CharArray decodeText(byte[] encoded, int start, int length, boolean endOfData, CharArray dest) throws Error, IOException
    //===================================================================
    // strip CR ignored
    {
	if (dest == null)
	    dest = new CharArray();
	dest.length = 0;
	if (!checked && length > 3) {
	    checked = true;
	    int magicNumber = encoded[start] + 256 * encoded[start + 1];
	    if (magicNumber != -513) {
		if (magicNumber == -17681) {
		    throw new Error("UTF-8");
		} else {
		    if (encoded[start + 1] != 0)
			throw new Error("ASCII");
		    else
			return null;
		}
	    }
	}
	for (int i = start; i < start + length; i++) {
	    int k = encoded[i];
	    i++;
	    k = k + 256 * encoded[i];
	    String s = "" + (char) k;
	    dest.append(s);
	}
	return dest;
    }

    //===================================================================
    public void closeCodec() throws IOException
    //===================================================================
    {
    }

    //===================================================================
    public Object getCopy()
    //===================================================================
    {
	return new NoCodec(false);
    }
    //##################################################################
}
//##################################################################
