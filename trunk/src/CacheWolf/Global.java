package CacheWolf;

/**
 * Global data: Preferences and Profile
 * 
 * @author salzkammergut
 */
public class Global {
	  private static Preferences pref=Preferences.getPrefObject();
	  private static Profile profile=new Profile();

	  static public Preferences getPref() {
		  return pref;
	  }
	
	  static public Profile getProfile() {
		  return profile;
	  }

}

