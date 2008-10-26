    /*
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
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */

package cachewolf.imp;

import eve.sys.Vm;
import java.util.*;
import java.io.*;

import cachewolf.CacheHolder;
import cachewolf.CacheHolderDetail;
import cachewolf.Profile;
import cachewolf.utils.Common;


import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;
import eve.ui.Form;

/**
 * @author Kalle
 *
 */
public class LOCXMLImporter extends MinML {
	boolean debugXML = false;
	Vector cacheDB;
	Profile profile;
	String file;
	CacheHolderDetail chD;

	Hashtable DBindexWpt = new Hashtable();
	String strData = "";

	
	public LOCXMLImporter ( Profile prof, String f ){
		profile=prof;
		cacheDB = profile.cacheDB;
		file = f;
		CacheHolder ch;
		for(int i = 0; i<cacheDB.size();i++){
			ch = (CacheHolder)cacheDB.get(i);
			DBindexWpt.put(ch.wayPoint, new Integer(i));
		}//for
	}
	
	public void doIt() {
		try{
			Reader r;
			Form.showWait();
			//Test for zip.file
						r = new FileReader(file);
						parse(r);
						r.close();
			// save Index 
			profile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
			Form.cancelWait();
		}catch(Exception e){
			//Vm.debug(e.toString());
			Form.cancelWait();
		}
		
	}
	
	public void startElement(String name, AttributeList atts){
		if (debugXML){
			for (int i = 0; i < atts.getLength(); i++) {
				Vm.debug(" Name: " + atts.getName(i)+ " Value: "+atts.getValue(i));
			}
		}
		strData ="";
		if (name.equals("name")){
			chD = getHolder(atts.getValue("id"));
			return;
		}
		if (name.equals("coord")){
			chD.pos.set(Common.parseDouble(atts.getValue("lat")),Common.parseDouble(atts.getValue("lon")));
			chD.latLon = chD.pos.toString();
			return;
		}

		
	}
	
	public void endElement(String name){
		if (name.equals("name")){
			chD.cacheName = strData;
		}

		if (name.equals("waypoint")){
			int index;
			index = searchWpt(chD.wayPoint);
			if (index == -1){
				chD.is_new = true;
				cacheDB.add(new CacheHolder(chD));
				DBindexWpt.put(chD.wayPoint, new Integer(cacheDB.size()-1));
			}
			// update (overwrite) data
			else {
				chD.is_new = false;
				cacheDB.setElementAt(new CacheHolder(chD),index);
			}
			// save all  (after each cache???)
			chD.saveCacheDetails(profile.dataDir);
			profile.saveIndex(Profile.NO_SHOW_PROGRESS_BAR);
			return;
		}

		if (name.equals("link")){
			chD.URL = strData;
			return;
		}



	}
	

	public void characters(char[] ch,int start,int length){
		String chars = new String(ch,start,length);
		strData += chars;
		if (debugXML) Vm.debug(strData);
	}

	
	/**
	* Method to iterate through cache database and look for waypoint.
	* Returns value >= 0 if waypoint is found, else -1
	*/
	private int searchWpt(String wpt){
		Integer INTR = (Integer)DBindexWpt.get(wpt);
		if(INTR != null)
			return INTR.intValue();
		return -1;
	}

	private CacheHolderDetail getHolder(String wpt){// See also OCXMLImporter
		int index;
		CacheHolderDetail ch;
		
		index = searchWpt(wpt);
		if (index == -1){
			ch = new CacheHolderDetail();
			ch.wayPoint = wpt;
			return ch;
		}
		ch = new CacheHolderDetail((CacheHolder) cacheDB.get(index));
		try {
			ch.readCache(profile.dataDir);
		} catch (Exception e) {Vm.debug("Could not open file: " + e.toString());};
		return ch;
	}


}
