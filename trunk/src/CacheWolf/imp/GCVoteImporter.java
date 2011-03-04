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
import CacheWolf.Common;
import CacheWolf.HttpConnection;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.UrlFetcher;
import CacheWolf.imp.SpiderGC.SpiderProperties;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.io.Reader;
import ewe.io.StringReader;
import ewe.net.Socket;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.util.CharArray;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;



public class GCVoteImporter extends MinML{

	private Profile profile;
	private CacheDB cacheDB;
	private String GCVUser;
	private String GCVPassword;
	private String GCVURL;
	private String GCVWaypoints;
	private String GCVResults;
	private static Preferences pref;

	/**
	*	Constructor initalizing profile and cacheDB
	*/
	public GCVoteImporter(Preferences prf, Profile _profile){
		this.profile=_profile;
		this.cacheDB = profile.cacheDB;
		pref = prf;
		// pref.logInit();
	}

	/**
	*	Read cacheDB and check for GCVotes
	*/
	public void doIt() {
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		this.GCVUser = ""; // pref.myAlias;
		this.GCVPassword = "";

		int totalWaypoints = cacheDB.countVisible();
		int countWaypoints = 0;

		pbf.showMainTask = false;
		pbf.setTask(h, "Import GCVote ratings ...");
		pbf.exec();

		for (int o = 0; o < cacheDB.size(); o += 100) {
		GCVWaypoints = "";
			for (int i = o; (i < (o+100)) & (i < cacheDB.size()); i++) {
				CacheHolder ch = cacheDB.get(i);
				if (ch.isVisible()) {
					if (ch.isCacheWpt()) {
						GCVWaypoints += ch.getWayPoint() + ",";
					}
					countWaypoints++;
					h.progress = (float) countWaypoints / (float) totalWaypoints;
					h.changed();
				}
			}

			GCVURL = "http://gcvote.de/getVotes.php?";
			GCVURL += "userName="+GCVUser+"&password="+GCVPassword+"&waypoints="+GCVWaypoints;

			try {
				pref.log("[GCVote]:Requesting ratings");

				// request web page http://gcvote.de/getVotes.php
				// UrlFetcher.setRequestorProperty("User-Agent", "Mozilla/5.0 (compatible; Cachewolf; GCVoteImporter)");
				GCVResults = UrlFetcher.fetch(GCVURL);
				if (GCVResults.equals("")) {
					(new MessageBox(MyLocale.getMsg(0,"Error"), MyLocale.getMsg(0,"Error loading GCVote page.%0aPlease check your internet connection."), FormBase.OKB)).execute();
					pref.log("[GCVote]:Could not fetch: getVotes.php page",null);
				} else {
					// parse response for votes
					pref.log("[GCVote]:GCVotes = "+GCVResults);
					Reader r = new StringReader(GCVResults);
					parse(r);
				}
			} catch (Exception ex) {
				pref.log("[GCVote:DoIt]",ex,true);
			}
		}
		pbf.exit(0);
	}

	/**
	*	Eventhandler for XML votes
	*/
	public void startElement(String name, AttributeList atts){
		String waypoint;
		double voteAvg;
		int voteCnt;
		if(name.equals("vote")) {
			waypoint = atts.getValue("waypoint");
			voteAvg = Common.parseDouble(atts.getValue("voteAvg")) * 10.0;
			voteCnt = Common.parseInt(atts.getValue("voteCnt"));
			pref.log("[GCVote:startElement]:Waypoint = " + waypoint + " - " + voteAvg + "(" + voteCnt + " votes)" );
			CacheHolder cb = cacheDB.get(waypoint);
			cb.setNumRecommended( 100*voteCnt + (int)(voteAvg + 0.5));
		}
	}
}
