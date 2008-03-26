package CacheWolf.navi;

import CacheWolf.CWPoint;
import ewe.fx.*;

public class MapSymbol extends MapImage { // TODO make this implement MapImage, so that it will be invisible automatically if not on screen. When doing so, test if setgoto-pos -> open map from gotopanel shows the map symbols (directly after starting CW)
	Object mapObject;
	String name;
	String filename;
	CWPoint where;
	
	public MapSymbol(String namei, String filenamei, CWPoint where_) {
		name = namei;
		filename = filenamei;
		where = where_;
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
