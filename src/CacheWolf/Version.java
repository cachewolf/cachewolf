package CacheWolf;

public class Version {
	static final String VER_MAJOR = "0.9";
	static final String VER_MINOR = "l";
	static final String VER_BUILD = " 1003";
	
	public static String getRelease() {
		return VER_MAJOR + VER_MINOR + " " + VER_BUILD;
	}

}
