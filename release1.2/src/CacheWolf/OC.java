package CacheWolf;

public final class OC {

	/** thou shallst not instantiate this object */
	private OC() {
		// Nothing to do
	}

	public final static int OC_HOSTNAME = 0; 
	public final static int OC_PREFIX = 1; 
	public final static String[][] OCSites = {
		{"www.opencaching.de", "OC"},
		{"www.opencaching.pl", "OP"},
		{"www.opencaching.cz", "OZ"},
		{"www.opencaching.org.uk", "OK"},
		{"www.opencaching.se", "OS"},
		{"www.opencaching.no", "ON"},
		{"www.opencaching.us", "OU"}
		};

	public final static String[] OCHostNames() {
		String[] ret=new String[OCSites.length];
		for (int i = 0; i < OCSites.length; i++) {
			ret[i]=OCSites[i][OC_HOSTNAME];
		}
		return ret;
	}
	
	public final static String getOCHostName(String wpName){
		for (int i = 0; i < OCSites.length; i++) {
			if(wpName.startsWith(OCSites[i][OC_PREFIX])) {
				return OCSites[i][OC_HOSTNAME];
			}
		}
		return null;
	}
	
	public final static boolean isOC(String wpName) {
		return (getOCHostName(wpName.toUpperCase()) != null);		
	}
	
	public final static int getSiteIndex(String site) {
		for (int i = 0; i < OCSites.length; i++) {
			if(site.equalsIgnoreCase(OCSites[i][OC_HOSTNAME])) {
				return i;
			}
		}
		return 0; // don't get a fault
	}
}
