package CacheWolf;

import ewe.fx.mImage;
import ewe.io.File;

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

	/**
	 * The attribute names are identical to the image names.
	 * Internally the first image name has number 0, the next number 1 and so on.
	 */
	private String [] attributeNames= {
			"error.gif",			//00 Unknown attribute
			"available-no.gif", 	//01
			"available-yes.gif", 	//02 available 24-7
			"bicycles-no.gif", 		//03 no bikes
			"bicycles-yes.gif", 	//04 bikes allowed
			"boat-no.gif", 			//05
			"boat-yes.gif", 		//06
			"cactus-no.gif", 		//07
			"cactus-yes.gif",		//08
			"campfires-no.gif", 	//09 no campfires
			"campfires-yes.gif", 	//10
			"camping-no.gif", 		//11
			"camping-yes.gif", 		//12
			"cliff-no.gif", 		//13
			"cliff-yes.gif", 		//14
			"climbing-no.gif", 		//15
			"climbing-yes.gif", 	//16
			"compass-no.gif", 		//17
			"compass-yes.gif",		//18
			"cow-no.gif", 			//19
			"cow-yes.gif", 			//20
			"danger-no.gif", 		//21
			"danger-yes.gif", 		//22
			"dogs-no.gif", 			//23
			"dogs-yes.gif", 		//24 dogs allowed
			"fee-no.gif", 			//25
			"fee-yes.gif", 			//26
			"hiking-no.gif", 		//27
			"hiking-yes.gif", 		//28
			"horses-no.gif", 		//29 no horses
			"horses-yes.gif", 		//30
			"hunting-no.gif", 		//31
			"hunting-yes.gif", 		//32
			"jeeps-no.gif", 		//33
			"jeeps-yes.gif", 		//34
			"kids-no.gif", 			//35
			"kids-yes.gif", 		//36 kid friendly
			"mine-no.gif", 			//37
			"mine-yes.gif", 		//38
			"motorcycles-no.gif", 	//39
			"motorcycles-yes.gif", 	//40
			"night-no.gif", 		//41 not recommended at night
			"night-yes.gif", 		//42
			"onehour-no.gif", 		//43 takes more than one hour
			"onehour-yes.gif", 		//44 takes less than one hour
			"parking-no.gif", 		//45
			"parking-yes.gif", 		//46 parking available
			"phone-no.gif", 		//47
			"phone-yes.gif", 		//48
			"picnic-no.gif", 		//49
			"picnic-yes.gif", 		//50
			"poisonoak-no.gif", 	//51
			"poisonoak-yes.gif", 	//52
			"public-no.gif", 		//53
			"public-yes.gif", 		//54 public transit available
			"quads-no.gif", 		//55
			"quads-yes.gif", 		//56 quads allowed
			"rappelling-no.gif", 	//57
			"rappelling-yes.gif", 	//58
			"restrooms-no.gif", 	//59
			"restrooms-yes.gif", 	//60
			"scenic-no.gif", 		//61
			"scenic-yes.gif", 		//62 scenic view
			"scuba-no.gif", 		//63
			"scuba-yes.gif", 		//64
			"snakes-no.gif", 		//65
			"snakes-yes.gif", 		//66
			"snowmobiles-no.gif", 	//67
			"snowmobiles-yes.gif", 	//68
			"stealth-no.gif", 		//69
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
			"wading-yes.gif", 		//80
			"water-no.gif", 		//81
			"water-yes.gif", 		//82
			"wheelchair-no.gif", 	//83 not wheelchair accessible
			"wheelchair-yes.gif", 	//84
			"winter-no.gif", 		//85 not available for winter
			"winter-yes.gif"}; 		//86 available in winter
	private mImage [] attributeImages=new mImage[87];
	private static String IMAGEDIR=File.getProgramDirectory()+"\\Attributes\\";
	
	public static String getImageDir() {
		return IMAGEDIR; 
	}
    
    
	public String getImageName(){
		if (attNo>attributeNames.length)
			return "error.gif";
		else return attributeNames[attNo];
	}
	
	public String getInfo(){
		return MyLocale.getMsg(2500+attNo,"No attribute info found");
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

    { // Static initialisation of error image
		attributeImages[0]=new mImage(IMAGEDIR+"error.gif");
	}

	
}
