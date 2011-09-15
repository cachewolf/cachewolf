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
package CacheWolf.imp;

import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.io.TextCodec;
import ewe.sys.Vm;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.mString;

public class FieldnotesImporter {
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	String file;

	public FieldnotesImporter(Preferences pf, Profile prof, String f) {
		pref = pf;
		profile = prof;
		cacheDB = profile.cacheDB;
		file = f;
	}

	public void doIt() {
		try {
			Vm.showWait(true);	
			FileReader r = null;

			try {
				r = new FileReader(file);
				r.codec=new NoCodec();				
				r.read(); // for checking of Codec
			}
			catch (Error e) {
				r.close();
				r = new FileReader(file);
				if (e.getMessage().equals("UTF-8")) {
					r.codec=new JavaUtf8Codec();
				}
				else if (e.getMessage().equals("ASCII")){
					r.codec=new NoCodec(true);
				}
				else {
					Vm.showWait(false);
					return;
				}
			}
			parse(r.readAll());
			r.close();
			// save Index
			profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
			Vm.showWait(false);
		} catch (Exception e) {
			Vm.showWait(false);
		}
	}
	
	private void parse(String s) {
		final byte WPPOS=0;
		final byte DATEPOS=1;
		final byte LOGTYPPOS=2;
		String[] l=mString.split(s,'"');
		for (int i = 0; i < l.length; i++) {
			String s1=l[i];
			i++;
			String logText=l[i];
			String[] l1=mString.split(s1,',');
			while (l1[WPPOS].charAt(0)<48) {
				l1[WPPOS]=l1[WPPOS].substring(1);
			}
			while (l1[WPPOS].charAt(0)>122) {
				l1[WPPOS]=l1[WPPOS].substring(1);
			}
			String wayPoint=l1[WPPOS];
			CacheHolder ch = cacheDB.get(wayPoint);
			if (ch!=null) {
				if (l1[LOGTYPPOS].equals(ch.getGCFoundText())) {
					// String stmp=ch.getCacheStatus();
					ch.setCacheStatus(l1[DATEPOS].replace('T',' ').replace('Z', ' ').trim());
					ch.setFound(true);
				} else {
					String stmp=ch.getCWLogText(l1[LOGTYPPOS]);
					if(stmp.equals("")) 
						ch.setCacheStatus(l1[LOGTYPPOS]); // eingelesener 
					else ch.setCacheStatus(stmp); // Statustext (ohne Datum/Uhrzeit)
					ch.setFound(false);
				}				
				if (!logText.equals("")) ch.getCacheDetails(false).setCacheNotes(logText);
				ch.save();
			}
		}
	}

}

//##################################################################
class NoCodec implements TextCodec{
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
public static final int STRIP_CR = STRIP_CR_ON_DECODE|STRIP_CR_ON_ENCODE;

private int flags = 0;
private boolean checked=false;

//===================================================================
public NoCodec(boolean _checked)
//===================================================================
{
	checked=_checked; flags=0;
}
//===================================================================
public NoCodec()
//===================================================================
{
	checked=false; flags=0;
}
//===================================================================
public ByteArray encodeText(char [] text, int start, int length, boolean endOfData, ByteArray dest) throws IOException
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	int size = length == 0 ? 2 : 2+text.length*2;
	if (dest.data == null || dest.data.length < size)
		dest.data = new byte[size];
	byte [] destination = dest.data;
	int s = 0;
	if (length>0){
		destination[s++] = (byte) 0xFF;
		destination[s++] = (byte) 0xFE;
	}
	for (int i = 0; i<length; i++){
		char c = text[i+start];
		if (c == 13 && ((flags & STRIP_CR_ON_ENCODE) != 0)) continue;
		destination[s++] = (byte)(c & 0xFF);
		destination[s++] = (byte)((c>>8) & 0xFF);
	}
	dest.length = s;
	return dest;
}

//===================================================================
public CharArray decodeText(byte [] encoded, int start, int length, boolean endOfData, CharArray dest) throws Error, IOException
//===================================================================
// strip CR ignored
{
	if (dest == null) dest = new CharArray();
	dest.length = 0;
	if (!checked && length>3) {
		checked=true;
		int magicNumber=encoded[start]+256*encoded[start+1];
		if (magicNumber!=-513) {
			if (magicNumber==-17681) {
				throw new Error("UTF-8");}
			else {throw new Error("ASCII");}
		}
	}
	for (int i = start; i < start+length; i++) {
		int k = encoded[i];
		i++;
		k=k + 256*encoded[i];
		String s=""+(char)k;
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
