package CacheWolf.navi;

import ewe.fx.*;
import ewe.graphics.*;

public class MapSymbol extends AniImage { // TODO make this implement MapImage, so that it will be invisible automatically if not on screen. When doing so, test if setgoto-pos -> open map from gotopanel shows the map symbols (directly after starting CW)
	Object mapObject;
	String name;
	String filename;
	double lat, lon;
	public MapSymbol(String namei, String filenamei, double lati, double loni) {
		name = namei;
		filename = filenamei;
		lat = lati;
		lon = loni;
	}
	public MapSymbol(String namei, Object mapObjecti, Image fromIm, double lati, double loni) {
		name = namei;
		lat = lati;
		lon = loni;
		mapObject = mapObjecti;
		setImage(fromIm);
	}
	public void loadImage(){
		setImage(new Image(filename),0); freeSource();;
		//properties = AniImage.AlwaysOnTop;
	}
}
