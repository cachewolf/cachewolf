package CacheWolf;

/**
 * This class represents the settings of the filter that can be done when the users changes the
 * filter in CacheWolf.
 *
 */
public class FilterData {

	// When extending the filter check "normaliseFilters"
	// which ensures backward compatibility. Normally no change should be needed
	public final static String FILTERTYPE = "1111111111111111111";
	public final static String FILTERROSE = "1111111111111111";
	public final static String FILTERVAR = "11111111";
	public final static String FILTERSIZE = "111111";

	private String filterType = new String(FILTERTYPE);
	private String filterRose = new String(FILTERROSE);
	private String filterSize = new String(FILTERSIZE);

	// filter settings for archived ... owner (section) in filterscreen
	private String filterVar = new String(FILTERVAR);
	private String filterDist = new String("L");
	private String filterDiff = new String("L");
	private String filterTerr = new String("L");

	private long filterAttrYes = 0l;
	private long filterAttrNo = 0l;
	private int filterAttrChoice = 0;

	// filter setting for state of cache
	private String filterStatus = "";
	private boolean useRegexp = false;

	/**
	 * Constructor for a profile
	 *
	 */
	public FilterData(){ // public constructor
	}

	/**
	 * Returns an XML representation of the filter data. If a non empty String is passed as
	 * parameter, then this String is used as ID-tag for the filter. If it is empty, then the ID
	 * tag will not appear in the cache data. The ID tag is the string which is used in the filter
	 * screen to appear in the filter list.
	 * @param ID ID tag of filter 
	 * @return XML represenation of filter
	 */
	public String toXML(String ID) {
		String saveID="";
		if (ID != null && ! ID.equals("")) {
			saveID = "id = \""+SafeXML.strxmlencode(ID)+"\" ";
		}
	    return "    <FILTERDATA "+saveID+"rose = \""+getFilterRose()+"\" type = \""+getFilterType()+
		"\" var = \""+getFilterVar()+"\" dist = \""+getFilterDist().replace('"',' ')+"\" diff = \""+
		getFilterDiff()+"\" terr = \""+getFilterTerr()+"\" size = \""+getFilterSize()+"\" attributesYes = \""+getFilterAttrYes()+
		"\" attributesNo = \""+getFilterAttrNo()+"\" attributesChoice = \""+getFilterAttrChoice()+
		"\" status = \""+SafeXML.strxmlencode(getFilterStatus())+"\" useRegexp = \""+useRegexp()+"\" />\n";	
	}
	/**
	 * Ensure that all filters have the proper length so that the 'charAt' access in the filter do
	 * not cause nullPointer Exceptions
	 */
	public void normaliseFilters() {
		String manyOnes = "11111111111111111111111111111";
		if (getFilterRose().length() < FILTERROSE.length()) {
			setFilterRose((getFilterRose() + manyOnes).substring(0, FILTERROSE.length()));
		}
		if (getFilterVar().length() < FILTERVAR.length()) {
			setFilterVar((getFilterVar() + manyOnes).substring(0, FILTERVAR.length()));
		}
		if (getFilterType().length() < FILTERTYPE.length()) {
			setFilterType((getFilterType() + manyOnes).substring(0, FILTERTYPE.length()));
		}
		if (getFilterSize().length() < FILTERSIZE.length()) {
			setFilterSize((getFilterSize() + manyOnes).substring(0, FILTERSIZE.length()));
		}
		if (getFilterDist().length() == 0)
			setFilterDist("L");
		if (getFilterDiff().length() == 0)
			setFilterDiff("L");
		if (getFilterTerr().length() == 0)
			setFilterTerr("L");
	}

	// Getter and Setter for private properties

	public String getFilterType() {
		return filterType;
	}

	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	public String getFilterRose() {
		return filterRose;
	}

	public void setFilterRose(String filterRose) {
		this.filterRose = filterRose;
	}

	public String getFilterSize() {
		return filterSize;
	}

	public void setFilterSize(String filterSize) {
		this.filterSize = filterSize;
	}

	public String getFilterVar() {
		return filterVar;
	}

	public void setFilterVar(String filterVar) {
		this.filterVar = filterVar;
	}

	public String getFilterDist() {
		return filterDist;
	}

	public void setFilterDist(String filterDist) {
		this.filterDist = filterDist;
	}

	public String getFilterDiff() {
		return filterDiff;
	}

	public void setFilterDiff(String filterDiff) {
		this.filterDiff = filterDiff;
	}

	public String getFilterTerr() {
		return filterTerr;
	}

	public void setFilterTerr(String filterTerr) {
		this.filterTerr = filterTerr;
	}

	public long getFilterAttrYes() {
		return filterAttrYes;
	}

	public void setFilterAttrYes(long filterAttrYes) {
		this.filterAttrYes = filterAttrYes;
	}

	public long getFilterAttrNo() {
		return filterAttrNo;
	}

	public void setFilterAttrNo(long filterAttrNo) {
		this.filterAttrNo = filterAttrNo;
	}

	public int getFilterAttrChoice() {
		return filterAttrChoice;
	}

	public void setFilterAttrChoice(int filterAttrChoice) {
		this.filterAttrChoice = filterAttrChoice;
	}

	public String getFilterStatus() {
    	return filterStatus;
    }

	public void setFilterStatus(String filterStatus) {
    	this.filterStatus = filterStatus;
    }

	public boolean useRegexp() {
    	return useRegexp;
    }

	public void setUseRegexp(boolean useRegexp) {
    	this.useRegexp = useRegexp;
    }

}
