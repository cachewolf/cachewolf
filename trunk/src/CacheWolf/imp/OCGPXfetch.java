package CacheWolf.imp;

import ewe.io.File;
import ewe.io.IOException;
import ewe.ui.FormBase;
import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.UrlFetcher;
import CacheWolf.utils.FileBugfix;

public class OCGPXfetch {
	static Preferences pref = Global.getPref();
	static Profile prof = Global.getProfile();;
	
	public static void doIt() {
		String hostname = pref.lastOCSite;
		boolean oldDownloadAllOC=pref.downloadAllOC;
		boolean onlyListedAtOC = false;
		OCXMLImporterScreen importOpt = new OCXMLImporterScreen(
				MyLocale.getMsg(130,"Download from opencaching"),
				OCXMLImporterScreen.IMAGES|
				OCXMLImporterScreen.ALL|
				OCXMLImporterScreen.HOST);
		importOpt.missingCheckBox.setText(MyLocale.getMsg(164,"only listed at OC"));
		importOpt.missingCheckBox.setState(onlyListedAtOC);
		if (importOpt.execute() == FormBase.IDCANCEL) {	return; }
		onlyListedAtOC=pref.downloadAllOC;
		pref.downloadAllOC=oldDownloadAllOC;
		if (importOpt.domains.getSelectedItem()!=null) {
			hostname = (String)importOpt.domains.getSelectedItem();
			pref.lastOCSite=hostname;
		}

		try {
			String address="http://"+hostname+"/search.php?";
			address+="searchto=searchbyfinder"; // searchbydistance
			address+="&showresult=1&expert=0&sort=bydistance&orderRatingFirst=0";
			address+="&f_userowner=0&f_userfound=0&f_inactive=0&f_ignored=0";
			address+="&f_otherPlatforms="; // 0 = all 1 = nur OC
			if (onlyListedAtOC)
				address+="1";
			else 
				address+="0"; 
			address+="&country=&difficultymin=0&difficultymax=0&terrainmin=0&terrainmax=0&cachetype=1;2;3;4;5;6;7;8;9;10&cachesize=1;2;3;4;5;6;7&cache_attribs=&cache_attribs_not=";
			address+="&logtype=1,7";
			address+="&utf8=1&output=gpx&zip=1";
			address+="&count=max";
			address+="&finder="+pref.myAlias;
			String tmpFile=prof.dataDir + "dummy.zip";
			login();
			UrlFetcher.fetchDataFile(address, tmpFile);
			File ftmp = new FileBugfix(tmpFile);
			if (ftmp.exists() && ftmp.length()>0) {
				GPXImporter gpx = new GPXImporter(pref, prof, tmpFile);
				if (pref.downloadPics) 
					gpx.doIt(GPXImporter.DOIT_WITHSPOILER);				
				else gpx.doIt(GPXImporter.DOIT_NOSPOILER);
			}
			ftmp.delete();
		} catch (IOException e) {
		}
	}
	
	private static boolean login() {
		// TODO this is only a preliminary Version of login
		boolean loggedIn = false;
		String page;
		try {
			String loginDaten="target=myhome.php&action=login&email="+pref.myAlias+"&password="+pref.password;
			try {
				UrlFetcher.setpostData(loginDaten);
				UrlFetcher.setMaxRedirections(1);
				UrlFetcher.fetch("http://www.opencaching.de/login.php");
			} catch (IOException e) {
				if (e.getMessage().startsWith("too many http redirections")) {
					String cookie = (String) UrlFetcher.getDocumentProperties().getValue("Set-Cookie","");
					UrlFetcher.setPermanentRequestorProperty("Cookie",cookie);
					page=UrlFetcher.fetch("http://www.opencaching.de/login.php?action=cookieverify&target=myhome.php");
					page=UrlFetcher.fetch("http://www.opencaching.de/myhome.php");
					// control , that I am logged in
					// (by ?) get finderid ?
					// todo set language ?
					loggedIn = page.indexOf("Eingeloggt als") > -1;
				}
			}			
		} catch (IOException e) {
			pref.log("Fehler", e);
		}
		return loggedIn;
	}
}
