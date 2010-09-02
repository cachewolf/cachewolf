package CacheWolf.imp;

import ewe.data.Property;
import ewe.data.PropertyList;
import ewe.io.BufferedReader;
import ewe.io.IOException;
import ewe.io.InputStreamReader;
import ewe.io.JavaUtf8Codec;
import ewe.net.Socket;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.util.CharArray;
import CacheWolf.CacheDB;
import CacheWolf.CacheHolder;
import CacheWolf.Common;
import CacheWolf.Global;
import CacheWolf.HttpConnection;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.imp.SpiderGC.SpiderProperties;
import CacheWolf.utils.CWWrapper;
import ewe.io.*;
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
	private static SpiderProperties p=null;

	/**
	*	Constructor initalizing profile and cacheDB
	*/
	public GCVoteImporter(Preferences prf, Profile profile, boolean bypass){
		this.profile=profile;
		this.cacheDB = profile.cacheDB;
		pref = prf;
		if (p == null) {
			pref.logInit();
		}
		// initialiseProperties();
	}

	/**
	*	Read cacheDB and check for GCVotes
	*/
	public void doIt() {
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		this.GCVUser = pref.myAlias;
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
				if (pref.debug) pref.log("[GCVote]:Requesting ratings");

				// request web page http://gcvote.de/getVotes.php
				GCVResults = getResponse(GCVURL);
				if (GCVResults.equals("")) {
					(new MessageBox(MyLocale.getMsg(0,"Error"), MyLocale.getMsg(0,"Error loading GCVote page.%0aPlease check your internet connection."), FormBase.OKB)).execute();
					if (pref.debug) pref.log("[GCVote]:Could not fetch: getVotes.php page");
				} else {
					// parse response for votes
					if (pref.debug) pref.log("[GCVote]:GCVotes = "+GCVResults);
					Reader r = new StringReader(GCVResults);
					parse(r);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
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
		if(name.equals("vote")) {
			waypoint = atts.getValue("waypoint");
			voteAvg = Common.parseDouble(atts.getValue("voteAvg")) * 10.0;
			// voteCnt = atts.getValue("voteCnt");
			if (pref.debug) pref.log("[GCVote]:WQaypoint = " + waypoint + "-" + voteAvg);

			CacheHolder cb = cacheDB.get(waypoint);
			cb.setNumRecommended((int)(voteAvg + 0.5));
		}
	}

	/**
	*	Perform an request to fetch the GCVote results
	*/
	public static String getResponse(String address) {
		CharArray c_data;
		try{
			HttpConnection conn;
			if(pref.myproxy.length() > 0 && pref.proxyActive){
				pref.log("[GCVote]:Using proxy: " + pref.myproxy + " / " +pref.myproxyport);
			}
			conn = new HttpConnection(address);
			conn.setRequestorProperty("User-Agent", "Mozilla/5.0 (compatible; Cachewolf; GCVoteImporter)");
			conn.setRequestorProperty("Connection", "close");
			conn.documentIsEncoded = true;
			if (pref.debug) pref.log("[GCVote]:Connecting "+address);
			Socket sock = conn.connect();
			if (pref.debug) pref.log("[GCVote]:Connect ok! "+address);
			JavaUtf8Codec codec = new JavaUtf8Codec();
			c_data = conn.readText(sock, codec);
			sock.close();
			if (pref.debug) pref.log("[GCVote]:Read data ok "+address);
			return c_data.toString();
		}catch(IOException ioex){
			pref.log("IOException in fetch", ioex);
		}finally{
			//continue
		}
		return "";
	}
}
