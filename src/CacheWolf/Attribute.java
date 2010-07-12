package CacheWolf;

import ewe.fx.mImage;
import ewe.io.FileBase;

/**
 * This class represents a single attribute
 * @author skg
 *
 */
public class Attribute {
	// Constructors
	public Attribute(int id, int inc) {_Id=id;setInc(inc);setIdBit();}
	public Attribute(String attributeName) { attName2attNo(attributeName); setIdBit();}
	public Attribute(int attIdOC) { OCAttNo2attNo(attIdOC); setIdBit();}
	public Attribute(int attIdGC, String Yes1No0) { GCAttNo2attNo(attIdGC, Yes1No0); setIdBit();}
	// Constructors end
	private int _Id;
	private int _Inc; // Yes=1 No=0 non=2
	private String _ImageName;
	private long[] _bit = {0l,0l};
	// for GC Constructor Spider
	private void attName2attNo(String attributeName) {
    	for (int i=0; i<maxAttRef; i++) {
    		if (attributeName.toLowerCase().startsWith(attRef[i][PIC_NAME])) {
				_Id=i;
				_Inc=attributeName.toLowerCase().endsWith("-no.gif") ? 0 : 1;
				_ImageName=attRef[i][PIC_NAME]+(_Inc==0 ? "-no.gif" : "-yes.gif");
				return;
    		}
    	}
    	_Id=-1; // Error
    	_ImageName="error.gif";
    	}
	// for OC Constructor
    private void OCAttNo2attNo(int attIdOC) {
    	for (int i=0; i<maxAttRef; i++) {
    		if (attIdOC == Integer.parseInt(attRef[i][OC_ID])) {
				_Id=i;
				_Inc=1;
				_ImageName=attRef[i][PIC_NAME]+"-yes.gif";
				return;
    		}
    	}
    	_Id=-1; // Error
    	_ImageName="error.gif";
    }
    // for GC Constructor gpx-Import
    private void GCAttNo2attNo(int attIdGC, String Yes1No0 ) {
    	for (int i=0; i<maxAttRef; i++) {
    		if (attIdGC == Integer.parseInt(attRef[i][GC_ID])) {
				_Id=i;
    			_Inc=Yes1No0.equals("1") ? 1 : 0;
				_ImageName=attRef[i][PIC_NAME]+(_Inc==0 ? "-no.gif" : "-yes.gif");
				return;				
    		}
    	}
    	_Id=-1; // Error
    	_ImageName="error.gif";
    }
    // used by all Constructors
    private void setIdBit() {
    	_bit=getIdBit(_Id);
    }

    // *** public part
    public static long[] getIdBit(int id) {
    	long [] bit = new long[2];
    	if (id>-1) {
        	int b = Integer.parseInt(attRef[id][BIT_NR]);
    		bit[0] = b>63 ? 0l : (1L << b);
    		bit[1] = b>63 ? (1L << 64-b) : 0;
    	}
    	else {
        	bit[0]=0;
        	bit[1]=0;
    	}
    	return bit;
    }    
    /**
     * get GC_TEXT string
	 */
    public String getGCText () { return attRef[_Id][GC_TEXT]; }
    /**
     * get GC_ID string
	 */
    public String getGCId () { return attRef[_Id][GC_ID]; }
    /**
     * getting attribute given=1,negative=0,not specified=2  
     */
	public int getInc () { return _Inc; }
    /**
     * setting/changing attribute given=1,negative=0,not specified=2  
     */
    public void setInc(int inc) {
    	_Inc=inc;
		_ImageName=attRef[_Id][PIC_NAME];
		if (inc==0) _ImageName+="-no.gif";
		else if (inc==1) _ImageName+="-yes.gif";
		else _ImageName+="-non.gif";
    }
    /**
     * getting name of corresponding image stored in attributes subdirectory 
     */
    public String getImageName() {
    	return _ImageName;
    }   
    /**
     * getting path+name of corresponding image stored in attributes subdirectory 
     */
    public String getPathAndImageName() {
    	return IMAGEDIR+_ImageName;
    }
    /**
     * set/unset the bit in the long array that belongs to the Id of the attribute  
     */
    public long[] getYesBit(long[] yes) {
    	if (_Inc==1) {
    		yes[0]|=_bit[0];
    		yes[1]|=_bit[1];    		
    	}
    	else {
    		yes[0]&=~_bit[0];
    		yes[1]&=~_bit[1];    		
    	}
    	return yes;
    }
    /**
     * set/unset the bit in the long array that belongs to the Id of the attribute  
     */
    public long[] getNoBit(long[] no) {
    	if (_Inc==0) {
    		no[0]|=_bit[0];
    		no[1]|=_bit[1];
    	}
    	else {
    		no[0]&=~_bit[0];
    		no[1]&=~_bit[1];    		
    	}
    	return no;
    }
    /**
     * get the language dependant description of the attribute  
     */
    public String getMsg() {
    	return getMsg(_Id,_Inc);
    }
	
	private final static int BIT_NR = 0; 
	private final static int MSG_NR = 1;
	private final static int PIC_NAME = 2;
	private final static int OC_ID = 3;
	private final static int GC_ID = 4;
	private final static int GC_TEXT = 5; // for export , didn't extract by myself, copied from forum	
	private static final String[][] attRef = {
		// Empfehlungen / Personen - Conditions (Yes/No)
		{"30","2562","scenic","0","8","Scenic view"},//62 scenic view
		{"17","2536","kids","59","6","Recommended for kids"},//36 kid friendly
		{"35","2572","stroller","0","41","Stroller accessible"},//72 stroller accessible	
		{"41","2584","wheelchair","0","24","Wheelchair accessible"},//84 wheelchair accessible
		//  vorhanden / Eigenschaften / Infrastruktur - Facilities (Yes/No)
		{"22","2546","parking","18","25","Parking available"},//46 parking available
		{"26","2554","public","19","26","Public transportation"},//54 public transit available
		{"40","2582","water","20","27","Drinking water nearby"},//82 drinking water nearby
		{"29","2560","restrooms","21","28","Public restrooms nearby"},//60 restrooms available
		{"23","2548","phone","22","29","Telephone nearby"},//48 telephone nearby
		{"24","2550","picnic","0","30","Picnic tables nearby"},//50 picnic tables available
		{"43","2588","firstaid","23","42","Firstaid"}, // GC: Cachewartung notwendig (Auto Attribut) , OC: erste Hilfe 
		{"73","2654","rv","0","46","Truck Driver/RV"},// changed by Moorteufel 12.07.10 
		// Erlaubt - Permissions (Allowed/Not Allowed)
		{"11","2524","dogs","0","1","Dogs"},//24 dogs allowed
		{"05","2512","camping","0","31","Camping available"},//12 Camping allowed
		{"01","2504","bicycles","0","32","Bicycles"},//04 bikes allowed
		{"19","2540","motorcycles","0","33","Motorcycles"},//40 motorcycles allowed
		{"27","2556","quads","0","34","Quads"},//56 quads allowed
		{"16","2534","jeeps","0","35","Off-road vehicles"},//34 off-road vehicles allowed
		{"33","2568","snowmobiles","0","36","Snowmobiles"},//68
		{"14","2530","horses","0","37","Horses"},//30 horses allowed
		{"04","2510","campfires","0","38","Campfires"},//10 campfires allowed
		// Eigenschaften / Gefahren - Hazards (Present/Not Present)
		{"10","2522","danger","9","23","Dangerous area"},//22 dangerous area
		{"62","2644","train","10","0",""},// 144 aktive Eisenbahnlinien in der Nähe
		{"06","2514","cliff","11","21","Cliff / falling rocks"},//14 falling-rocks nearby
		{"15","2532","hunting","12","22","Hunting"},//32 hunting area
		{"37","2576","thorn","13","39","Thorns"},//76 thorns!
		{"38","2578","ticks","14","19","Ticks"},//78 ticks!
		{"18","2538","mine","15","20","Abandoned mines"},//38
		{"25","2552","poisonoak","16","17","Poison plants"},//52 Giftige Pflanzen
		{"46","2594","animals","17","0",""},// 94 Giftige/gef%e4hrliche Tiere
		{"03","2508","cactus","0","0",""},//08
		{"32","2566","snakes","0","18","Snakes"},//66
		{"09","2520","cow","0","43","Watch for livestock"},//20 watch for livestock
		// Eigenschaften / Der Weg - Conditions (Yes/No)
		{"49","2600","car","24","0",""},// 100 Nahe beim Auto
		{"21","2544","onehour","0","7","Takes less than an hour"},//44 takes less than one hour
		{"13","2528","hiking","25","9","Significant hike"},//28 significant hike
		{"39","2580","wading","26","11","May require wading"},//80 may require wading
		{"65","2634","steep","27","0",""},// 134 Hügeliges Gelände
		{"07","2516","climbing","28","10","Difficult climbing"},//16 easy climbing(OC-28), difficult climbing(GC-10) 
		{"36","2574","swimming","29","12","May require swimming"},//74
		// Eigenschaften / Wegpunkte  - Conditions (Yes/No)
		{"55","2612","letter","8","0",""},// 112 Letterbox (benötigt Stempel)
		{"54","2610","interestsign","30","0",""},// 110 Interessanter Ort ev mit scenic zusammenfassen?
		{"56","2614","moving","31","0",""},// 114 Bewegliches Ziel
		{"64","2646","webcam","32","0",""},// 146 Webcam am Ziel
		{"53","2608","indoor","33","0",""},// 108 In geschlossenen Räumen
		{"66","2636","submerged","34","0",""},// 136 Im Wasser
		{"58","2618","nogps","35","0",""},// 118 Ohne GPS
		{"34","2570","stealth","0","40","Stealth required"},//70 stealth required (Heimlich,List,Schläue)
		// Einschränkungen und Voraussetzungen / Allgemein
		{"12","2526","fee","36","2","Access or parking fee"},//26 access/parking fees
		{"61","2624","overnight","37","0",""},// 124 Übernachtung erforderlich
		// Einschränkungen und Voraussetzungen / Zeitlich
		{"20","2542","night","1","14","Recommended at night"},//42 recommended at night
		{"00","2502","available","38","13","Available at all times"},//02 available 24-7
		{"68","2640","time","39","0",""},// 140 An bestimmte Zeiten gebunden
		{"52","2606","day","40","0",""},// 106 nur tagsüber
		{"67","2638","tide","41","0",""},// 138 Gezeiten
		// Einschränkungen und Voraussetzungen / Saisonbedingt
		{"51","2604","date","42","0",""},// 104 während des ganzen Jahres zugänglich
		{"57","2616","naturschutz","43","0",""},// 116 Brutsaison/Naturschutz
		{"42","2586","winter","44","15","Available during winter"},//86 available in winter 132 Schneesicheres Versteck
		// Einschränkungen und Voraussetzungen / Systembedingt
		{"59","2620","oconly","6","0",""},// 120 Nur bei Opencaching logbar
		{"71","2650","wwwlink","7","0",""},// 150 Nur Hyperlink zu OC-externen Portalen
		// Einschränkungen und Voraussetzungen / Benötigt Werkzeug - Special Equipment (Required/Not Required)
		{"69","2642","tools","46","0",""},// 142 Spezielle Ausrüstung
		{"08","2518","compass","47","0",""},//18 Kompass
		{"44","2590","flashlight","48","44","Flashlight required"}, // 90 Flashlight required
		{"28","2558","rappelling","49","3","Climbing gear"},//58 climbing gear Kletterausrüstung
		{"50","2602","cave","50","0",""},// 102 Höhlenausrüstung
		{"31","2564","scuba","51","5","Scuba gear"},//64 Tauchausrüstung
		{"02","2506","boat","52","4","Boat"},//06 Wasserfahrzeug
		{"45","2592","aircraft","53","38",""},// 92
		// Einschränkungen und Voraussetzungen / Benötigt Vorarbeit
		{"70","2648","wiki","54","0",""},// 148 Recherche
		{"63","2630","riddle","55","0",""},// 130 Rätsel
		{"74","2656","field_puzzle","0","47","Field Puzzle"},// changed by Moorteufel 12.07.10
		{"47","2596","arith_prob","56","0",""},// 96 Rechenaufgabe
		{"60","2622","othercache","57","0",""},// 122 besondere Cacheart
		{"48","2598","ask","58","0",""},// 98 Startbedingungen beim Owner erfragen
		// !!! todo correct Filter Implementation
		{"72","2652","landf","0","45","Lost And Found Tour"}, // thx to Kappler and MiK
		// {"-1","2500","error","0","0",""}, //

	};
    public static int maxAttRef=attRef.length;	
    private static String IMAGEDIR=STRreplace.replace(FileBase.getProgramDirectory()+"/attributes/", "//", "/");
    /*
    private static String getImageName(int cw_Id, int cw_Inc){
		if (cw_Id<0 || cw_Id>maxAttRef)
			return "error.gif";
		else {
			switch (cw_Inc) {
			case 1: return attRef[cw_Id][PIC_NAME]+"-yes.gif";
			case 0: return attRef[cw_Id][PIC_NAME]+"-no.gif";
			case 2: return attRef[cw_Id][PIC_NAME]+"-non.gif";
			default:return "error.gif";
			}
		}
	}
	*/
    private static String getMsg(int cw_Id, int cw_Inc){
    	if (cw_Id<0 || cw_Id>=maxAttRef) {
    		return MyLocale.getMsg(2500,"error attribute");
    	}
		if (cw_Inc==0)
			return MyLocale.getMsg(Integer.parseInt(attRef[cw_Id][MSG_NR])-1,"");
		else
			return MyLocale.getMsg(Integer.parseInt(attRef[cw_Id][MSG_NR]),"");
	}
    private static mImage[] yesImages=new mImage[maxAttRef];
	private static mImage[] noImages=new mImage[maxAttRef];
	private static mImage[] nonImages=new mImage[maxAttRef];
	private static final mImage errorImage=new mImage(IMAGEDIR+"error.gif");	
	/**
	 * Returns the width of the attribute icons
	 * @return The width of the images 
	 */
	public static int getImageWidth() {
		return errorImage.image.getWidth();
	}	
    /**
     * Get the image for a given attribute number. 
     * We use lazy initialisation here, i.e. the images are only loaded when they are requested. 
     * @return
     */
    public mImage getImage() {
    	if (_Id<0 || _Id>=maxAttRef) {
    		return errorImage;
    	}
    	if (_Inc==1) {
    		if (yesImages[_Id]==null) {yesImages[_Id]=new mImage(IMAGEDIR+getImageName());}
    		return yesImages[_Id];
    	}
    	else if (_Inc==0) {
    		if (noImages[_Id]==null) {noImages[_Id]=new mImage(IMAGEDIR+getImageName());}
    		return noImages[_Id];
    	}
    	else {
    		if (nonImages[_Id]==null) {nonImages[_Id]=new mImage(IMAGEDIR+getImageName());}
    		return nonImages[_Id];
    	}
    }	
}
