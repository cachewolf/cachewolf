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
import CacheWolf.database.CacheDB;
import CacheWolf.database.CacheHolder;
import CacheWolf.utils.Common;
import CacheWolf.utils.UrlFetcher;
import ewe.io.Reader;
import ewe.io.StringReader;
import ewe.sys.Handle;
import ewe.ui.ProgressBarForm;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

public class GCVoteImporter extends MinML {
    private CacheDB cacheDB;
    private String GCVUser;
    private String GCVPassword;
    private String GCVURL;
    private String GCVWaypoints;
    private String GCVResults;

    /**
     * Constructor initalizing profile and cacheDB
     */
    public GCVoteImporter() {
	this.cacheDB = MainForm.profile.cacheDB;
    }

    /**
     * Read cacheDB and check for GCVotes
     */
    public void doIt() {
	ProgressBarForm pbf = new ProgressBarForm();
	Handle h = new Handle();
	this.GCVUser = ""; // Preferences.itself().myAlias;
	this.GCVPassword = "";

	int totalWaypoints = cacheDB.countVisible();
	int countWaypoints = 0;

	pbf.showMainTask = false;
	pbf.setTask(h, "Import GCVote ratings ...");
	pbf.exec();

	for (int o = 0; o < cacheDB.size(); o += 100) {
	    GCVWaypoints = "";
	    for (int i = o; (i < (o + 100)) & (i < cacheDB.size()); i++) {
		CacheHolder ch = cacheDB.get(i);
		if (ch.isVisible()) {
		    if (ch.isCacheWpt()) {
			GCVWaypoints += ch.getCode() + ",";
		    }
		    countWaypoints++;
		    h.progress = (float) countWaypoints / (float) totalWaypoints;
		    h.changed();
		}
	    }

	    GCVURL = "http://gcvote.de/getVotes.php?";
	    GCVURL += "userName=" + GCVUser + "&password=" + GCVPassword + "&waypoints=" + GCVWaypoints;

	    try {
		Preferences.itself().log("[GCVote]:Requesting ratings");
		GCVResults = UrlFetcher.fetch(GCVURL);
		// parse response for votes
		Preferences.itself().log("[GCVote]:GCVotes = " + GCVResults);
		Reader r = new StringReader(GCVResults);
		parse(r);
	    } catch (Exception ex) {
		Preferences.itself().log("[GCVote:DoIt]", ex, true);
	    }
	}
	pbf.exit(0);
    }

    /**
     * Eventhandler for XML votes
     */
    public void startElement(String name, AttributeList atts) {
	String waypoint;
	double voteAvg;
	int voteCnt;
	if (name.equals("vote")) {
	    waypoint = atts.getValue("waypoint");
	    voteAvg = Common.parseDouble(atts.getValue("voteAvg")) * 10.0;
	    voteCnt = Common.parseInt(atts.getValue("voteCnt"));
	    Preferences.itself().log("[GCVote:startElement]:Waypoint = " + waypoint + " - " + voteAvg + "(" + voteCnt + " votes)");
	    CacheHolder cb = cacheDB.get(waypoint);
	    cb.setNumRecommended(100 * voteCnt + (int) (voteAvg + 0.5));
	}
    }
}
