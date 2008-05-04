package CacheWolf;

import ewe.fx.mImage;
import ewe.io.File;
import ewe.io.FileBase;

/**
 * This class represents a single attribute
 * @author skg
 *
 */
public class Attribute {
	// Constructors
	public Attribute() { attNo=0; }
	public Attribute(String attributeName) { attNo=attName2int(attributeName); }
	
	private int attNo;

	public int getAttrNr () { return attNo; }

	/**
	 * The attribute names are identical to the image names.
	 * Internally the first image name has number 0, the next number 1 and so on.
	 */
	public static final String [] attributeNames= {
			"error.gif",			//00 Unknown attribute
			"available-no.gif", 	//01 not 24-7
			"available-yes.gif", 	//02 available 24-7
			"bicycles-no.gif", 		//03 no bikes
			"bicycles-yes.gif", 	//04 bikes allowed
			"boat-no.gif", 			//05
			"boat-yes.gif", 		//06
			"cactus-no.gif", 		//07
			"cactus-yes.gif",		//08
			"campfires-no.gif", 	//09 no campfires
			"campfires-yes.gif", 	//10 campfires allowed
			"camping-no.gif", 		//11 No camping
			"camping-yes.gif", 		//12 Camping allowed
			"cliff-no.gif", 		//13
			"cliff-yes.gif", 		//14 falling-rocks nearby
			"climbing-no.gif", 		//15 no difficult climbing
			"climbing-yes.gif", 	//16 difficult climbing
			"compass-no.gif", 		//17 
			"compass-yes.gif",		//18
			"cow-no.gif", 			//19
			"cow-yes.gif", 			//20 watch for livestock
			"danger-no.gif", 		//21
			"danger-yes.gif", 		//22 dangerous area
			"dogs-no.gif", 			//23 no dogs
			"dogs-yes.gif", 		//24 dogs allowed
			"fee-no.gif", 			//25
			"fee-yes.gif", 			//26 access/parking fees
			"hiking-no.gif", 		//27
			"hiking-yes.gif", 		//28 significant hike
			"horses-no.gif", 		//29 no horses
			"horses-yes.gif", 		//30 horses allowed
			"hunting-no.gif", 		//31
			"hunting-yes.gif", 		//32 hunting area
			"jeeps-no.gif", 		//33 no off-road vehicles
			"jeeps-yes.gif", 		//34 off-road vehicles allowed
			"kids-no.gif", 			//35 no kids
			"kids-yes.gif", 		//36 kid friendly
			"mine-no.gif", 			//37
			"mine-yes.gif", 		//38
			"motorcycles-no.gif", 	//39 no motorcycles
			"motorcycles-yes.gif", 	//40 motorcycles allowed
			"night-no.gif", 		//41 not recommended at night
			"night-yes.gif", 		//42 recommended at night
			"onehour-no.gif", 		//43 takes more than one hour
			"onehour-yes.gif", 		//44 takes less than one hour
			"parking-no.gif", 		//45
			"parking-yes.gif", 		//46 parking available
			"phone-no.gif", 		//47
			"phone-yes.gif", 		//48 telephone nearby
			"picnic-no.gif", 		//49
			"picnic-yes.gif", 		//50 picnic tables available
			"poisonoak-no.gif", 	//51
			"poisonoak-yes.gif", 	//52
			"public-no.gif", 		//53
			"public-yes.gif", 		//54 public transit available
			"quads-no.gif", 		//55 no quads
			"quads-yes.gif", 		//56 quads allowed
			"rappelling-no.gif", 	//57
			"rappelling-yes.gif", 	//58 climbing gear
			"restrooms-no.gif", 	//59
			"restrooms-yes.gif", 	//60 restrooms available
			"scenic-no.gif", 		//61
			"scenic-yes.gif", 		//62 scenic view
			"scuba-no.gif", 		//63
			"scuba-yes.gif", 		//64
			"snakes-no.gif", 		//65
			"snakes-yes.gif", 		//66
			"snowmobiles-no.gif", 	//67 no snowmobiles
			"snowmobiles-yes.gif", 	//68
			"stealth-no.gif", 		//69 no stealth required
			"stealth-yes.gif", 		//70 stealth required
			"stroller-no.gif",		//71 not stroller accessible
			"stroller-yes.gif", 	//72 stroller accessible
			"swimming-no.gif", 		//73
			"swimming-yes.gif", 	//74
			"thorn-no.gif", 		//75
			"thorn-yes.gif", 		//76 thorns!
			"ticks-no.gif", 		//77
			"ticks-yes.gif", 		//78 ticks!
			"wading-no.gif", 		//79
			"wading-yes.gif", 		//80 may require wading
			"water-no.gif", 		//81
			"water-yes.gif", 		//82 drinking water nearby
			"wheelchair-no.gif", 	//83 not wheelchair accessible
			"wheelchair-yes.gif", 	//84 wheelchair accessible
			"winter-no.gif", 		//85 not available for winter
			"winter-yes.gif", 		//86 available in winter
			"firstaid-no.gif",              //87 does not need maintenance
			"firstaid-yes.gif"              //88 needs maintenance
	};
	private static mImage [] attributeImages=new mImage[89];
	private static String IMAGEDIR=STRreplace.replace(File.getProgramDirectory()+"/attributes/", "//", "/");
	
	public static String getImageDir() {
		return IMAGEDIR; 
	}
    
    /**
     * Returns the image name for a give attribute number
     * @return Image Name
     */
	public String getImageName(){
		if (attNo>attributeNames.length)
			return "error.gif";
		else return attributeNames[attNo];
	}
	
	/**
	 * Returns the text description of the image
	 * @return Text description of Image
	 */
	public String getInfo(){
		return MyLocale.getMsg(2500+attNo,"No attribute info found");
	}
	
	/**
	 * Returns the width of the attribute icons
	 * @return The width of the images 
	 */
	public static int getImageWidth() {
		initErrorImg();
		return attributeImages[0].image.getWidth();
	}
	
    /**
     * Get the image for a given attribute number. We use lazy initialisation here, i.e. the images are only
     * loaded when they are requested. 
     * @param attNo The attribute number
     * @return
     */
    public mImage getImage() {
    	if (attNo<0 || attNo>=attributeNames.length) {
    		return attributeImages[0];
    	}
    	if (attributeImages[attNo]==null) {
    		attributeImages[attNo]=new mImage(IMAGEDIR+attributeNames[attNo]);
    	}
    	return attributeImages[attNo];
    }
    
	/**
     * Encode an attribute name with the internal attribute number
     * @param attributeName The attribute name (=filename) to encode
     * @return The number of the attribute
     */
    private int attName2int(String attributeName) {
    	int size=attributeNames.length;
    	for (int i=1; i<size; i++) {
    		if (attributeName.equalsIgnoreCase(attributeNames[i])) return i;
    	}
    	return 0; // Error
    }

    private static void initErrorImg() {
		if (attributeImages[0]==null) attributeImages[0]=new mImage(IMAGEDIR+"error.gif");
    }
    
    { // Static initialisation of error image
    	initErrorImg();
    }

	
}
