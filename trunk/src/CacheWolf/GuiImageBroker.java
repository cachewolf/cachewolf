package CacheWolf;

import ewe.fx.Image;
import ewe.io.FileBase;
import utils.FileBugfix;

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
	private static final Image[] typeImages = {
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
	private static final Image[] sizeImages = {
//		new Image(CacheSize.CW_GUIIMG_NONPHYSICAL),
//		new Image(CacheSize.CW_GUIIMG_MICRO),
//		new Image(CacheSize.CW_GUIIMG_SMALL),
//		new Image(CacheSize.CW_GUIIMG_NORMAL),
//		new Image(CacheSize.CW_GUIIMG_LARGE),
//		new Image(CacheSize.CW_GUIIMG_VERYLARGE)
	};

	/** constructor does nothing */
	private GuiImageBroker() { // no instantiation needed
	}

	/**
	 * select image to be displayed for a given cache type
	 * @param id internal cache type id
	 * @return <code>Image</code> object to be displayed
	 */
	public static Image getTypeImage(byte id) {
		switch (id) {
		case CacheType.CW_TYPE_CUSTOM: return typeImages[0];
		case CacheType.CW_TYPE_APE: return typeImages[1];
		case CacheType.CW_TYPE_CITO: return typeImages[2];
		case CacheType.CW_TYPE_DRIVE_IN: return typeImages[3];
		case CacheType.CW_TYPE_EARTH: return typeImages[4];
		case CacheType.CW_TYPE_EVENT: return typeImages[5];
		case CacheType.CW_TYPE_FINAL: return typeImages[6];
		case CacheType.CW_TYPE_LETTERBOX: return typeImages[7];
		case CacheType.CW_TYPE_LOCATIONLESS: return typeImages[8];
		case CacheType.CW_TYPE_MAZE: return typeImages[9];
		case CacheType.CW_TYPE_MEGA_EVENT: return typeImages[10];
		case CacheType.CW_TYPE_MULTI: return typeImages[11];
		case CacheType.CW_TYPE_PARKING: return typeImages[12];
		case CacheType.CW_TYPE_QUESTION: return typeImages[13];
		case CacheType.CW_TYPE_REFERENCE: return typeImages[14];
		case CacheType.CW_TYPE_STAGE: return typeImages[15];
		case CacheType.CW_TYPE_TRADITIONAL: return typeImages[16];
		case CacheType.CW_TYPE_TRAILHEAD: return typeImages[17];
		case CacheType.CW_TYPE_UNKNOWN: return typeImages[18];
		case CacheType.CW_TYPE_VIRTUAL: return typeImages[19];
		case CacheType.CW_TYPE_WEBCAM: return typeImages[20];
		case CacheType.CW_TYPE_WHEREIGO: return typeImages[21];
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
	public static void customizedSymbols()
	{
		FileBugfix dir=new FileBugfix(FileBase.getProgramDirectory()+"/symbols");
		if (dir.isDirectory()){
			int id;
			String name = "";
			String [] pngFiles;
			pngFiles=dir.list("*.png",0);
			for (int i=0; i<pngFiles.length; i++) {
				name = pngFiles[i].substring(0,pngFiles[i].length()-4);
				try {
					id = Integer.parseInt (name);
				}
				catch (Exception E){
					id = -1; //filename invalid for symbols
				}
				if (0<=id && id<=typeImages.length){
					typeImages[id]= new Image(FileBase.getProgramDirectory()+"/symbols/"+pngFiles[i]);
				}
			}
		}
	}

}
