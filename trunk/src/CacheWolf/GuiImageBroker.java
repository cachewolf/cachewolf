package CacheWolf;

import CacheWolf.utils.FileBugfix;
import ewe.fx.Image;
import ewe.io.FileBase;

/**
 * hold preloaded versions of GUI images in a single place
 *
 * Do not instantiate this class, only use it in a static way.
 */

public final class GuiImageBroker {

	// TODO: check with Image and mImage

	/** image to be displayed in case of error */
	public static Image imageError = new Image("guiError.png");
	
	/**
	 * images to be displayed for cache types in GUI
	 * @see getTypeImage
	 * @see CacheTypes
	 */
	private static final Image[] TYPEIMAGES = {
		new Image(CacheType.CW_GUIIMG_CUSTOM),		// 0
		new Image(CacheType.CW_GUIIMG_APE),			// 1
		new Image(CacheType.CW_GUIIMG_CITO),		// 2
		new Image(CacheType.CW_GUIIMG_DRIVE_IN),	// 3
		new Image(CacheType.CW_GUIIMG_EARTH),		// 4
		new Image(CacheType.CW_GUIIMG_EVENT),		// 5
		new Image(CacheType.CW_GUIIMG_FINAL),		// 6
		new Image(CacheType.CW_GUIIMG_LETTERBOX),	// 7
		new Image(CacheType.CW_GUIIMG_LOCATIONLESS),// 8
		new Image(CacheType.CW_GUIIMG_MAZE),		// 9
		new Image(CacheType.CW_GUIIMG_MEGA_EVENT),	// 10
		new Image(CacheType.CW_GUIIMG_MULTI),		// 11
		new Image(CacheType.CW_GUIIMG_PARKING),		// 12
		new Image(CacheType.CW_GUIIMG_QUESTION),	// 13
		new Image(CacheType.CW_GUIIMG_REFERENCE),	// 14
		new Image(CacheType.CW_GUIIMG_STAGE),		// 15
		new Image(CacheType.CW_GUIIMG_TRADITIONAL),	// 16
		new Image(CacheType.CW_GUIIMG_TRAILHEAD),	// 17
		new Image(CacheType.CW_GUIIMG_UNKNOWN),		// 18
		new Image(CacheType.CW_GUIIMG_VIRTUAL),		// 19
		new Image(CacheType.CW_GUIIMG_WEBCAM),		// 20
		new Image(CacheType.CW_GUIIMG_WHEREIGO)		// 21
	};

	// TODO: move size images here
//	private static final Image[] sizeImages = {
//		new Image(CacheSize.CW_GUIIMG_NONPHYSICAL),
//		new Image(CacheSize.CW_GUIIMG_MICRO),
//		new Image(CacheSize.CW_GUIIMG_SMALL),
//		new Image(CacheSize.CW_GUIIMG_NORMAL),
//		new Image(CacheSize.CW_GUIIMG_LARGE),
//		new Image(CacheSize.CW_GUIIMG_VERYLARGE)
//	};

	/** thou shallst not instantiate this object */
	private GuiImageBroker() { 
		// Noting to do
	}
	

	/**
	 * select image to be displayed for a given cache type
	 * @param type internal cache type id
	 * @return <code>Image</code> object to be displayed
	 */
	public static Image getTypeImage(final byte type) {
		switch (type) {
		case CacheType.CW_TYPE_CUSTOM: return TYPEIMAGES[0];
		case CacheType.CW_TYPE_APE: return TYPEIMAGES[1];
		case CacheType.CW_TYPE_CITO: return TYPEIMAGES[2];
		case CacheType.CW_TYPE_DRIVE_IN: return TYPEIMAGES[3];
		case CacheType.CW_TYPE_EARTH: return TYPEIMAGES[4];
		case CacheType.CW_TYPE_EVENT: return TYPEIMAGES[5];
		case CacheType.CW_TYPE_FINAL: return TYPEIMAGES[6];
		case CacheType.CW_TYPE_LETTERBOX: return TYPEIMAGES[7];
		case CacheType.CW_TYPE_LOCATIONLESS: return TYPEIMAGES[8];
		case CacheType.CW_TYPE_MAZE: return TYPEIMAGES[9];
		case CacheType.CW_TYPE_MEGA_EVENT: return TYPEIMAGES[10];
		case CacheType.CW_TYPE_MULTI: return TYPEIMAGES[11];
		case CacheType.CW_TYPE_PARKING: return TYPEIMAGES[12];
		case CacheType.CW_TYPE_QUESTION: return TYPEIMAGES[13];
		case CacheType.CW_TYPE_REFERENCE: return TYPEIMAGES[14];
		case CacheType.CW_TYPE_STAGE: return TYPEIMAGES[15];
		case CacheType.CW_TYPE_TRADITIONAL: return TYPEIMAGES[16];
		case CacheType.CW_TYPE_TRAILHEAD: return TYPEIMAGES[17];
		case CacheType.CW_TYPE_UNKNOWN: return TYPEIMAGES[18];
		case CacheType.CW_TYPE_VIRTUAL: return TYPEIMAGES[19];
		case CacheType.CW_TYPE_WEBCAM: return TYPEIMAGES[20];
		case CacheType.CW_TYPE_WHEREIGO: return TYPEIMAGES[21];
		default: return imageError;
		}
	}

	/**
	 * Replaces the build-in symbols by images stored in /symbols:
	 * If the sub directory symbols exists in CW-directory *.png-files
	 * are read in and roughly checked for validity (names must be
	 * convertible to integers between 0 and 21).
	 * For every valid file x.png the corresponding typeImages[x] is
	 * replaced by the image in x.png.
	 * Images are NOT checked for size etc.
	 */
	public static void customizedSymbols() {
		final FileBugfix dir = new FileBugfix(FileBase.getProgramDirectory()+"/symbols");
		if (dir.isDirectory()){
			int type;
			String name = "";
			String [] pngFiles;
			pngFiles=dir.list("*.png",0);
			for (int i=0; i < pngFiles.length; i++) {
				name = pngFiles[i].substring(0,pngFiles[i].length()-4);
				try {
					type = Integer.parseInt(name);
				}
				catch (Exception E){
					type = -1; //filename invalid for symbols
				}
				if (0<=type && type<=TYPEIMAGES.length){
					TYPEIMAGES[type] = new Image(FileBase.getProgramDirectory()+"/symbols/"+pngFiles[i]);
				}
			}
		}
	}

}
