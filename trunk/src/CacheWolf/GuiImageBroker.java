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

	/** thou shallst not instantiate this object */
	private GuiImageBroker() {
		// Noting to do
	}

	public static Image getTypeImage(byte typeId,boolean map) {
		if (!map) {
			return CacheType.getTypeImage(typeId);
		}
		else {
			return CacheType.getMapImage(typeId);
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
		final String sdir="/symbols/";
		final FileBugfix dir=new FileBugfix(FileBase.getProgramDirectory()+sdir);
		if (dir.isDirectory()){
			int id;
			boolean size=false;
			String name = "";
			String [] pngFiles;
			pngFiles=dir.list("*.png",0);
			Global.getPref().log("Nr. of own symbols (png-files) : "+pngFiles.length);
			for (int i=0; i<pngFiles.length; i++) {
				name = pngFiles[i].substring(0,pngFiles[i].length()-4);
				if (name.toLowerCase().endsWith("size")){
					size=true;
					name=name.substring(0,name.length()-4);
				}
				try {
					id = Integer.parseInt(name);
				}
				catch (Exception E){
					id = -1; //filename invalid for symbols
				}
				if (0<=id && id<=CacheType.maxCWCType){
					String s=FileBase.getProgramDirectory()+sdir+pngFiles[i];
					Global.getPref().log("own symbol: "+(i+1)+" = "+pngFiles[i]);
					if (size){
						CacheType.setMapImage((byte) id, new Image(s));
						size=false;
					}
					else{
						CacheType.setTypeImage((byte) id, new Image(s));
					}
				}
			}
		}
	}

}
