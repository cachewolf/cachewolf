package CacheWolf.navi;

import CacheWolf.CWPoint;
import ewe.fx.*;

public class MapSymbol extends MapImage {
	Object mapObject;
	String name;
	String filename;
	CWPoint where;
	
	public MapSymbol(String namei, String filenamei, CWPoint where_) {
		name = namei;
		filename = filenamei;
		where = where_;
	}
	public MapSymbol(String namei, Object mapObjecti, String filenamei, CWPoint where_) {
		name = namei;
		filename = filenamei;
		where = where_;
		mapObject = mapObjecti;
	}
	public MapSymbol(String namei, Object mapObjecti, Image fromIm, CWPoint where_) {
		name = namei;
		where = where_;
		mapObject = mapObjecti;
		setImage(fromIm);
	}
	public void loadImage(){
		setImage(new Image(filename),0); freeSource();;
		//properties = AniImage.AlwaysOnTop;
	}
}
