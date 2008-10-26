package cachewolf.navi;

import cachewolf.CWPoint;
import eve.fx.*;

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
	public MapSymbol(String namei, Object mapObjecti, Picture fromIm, CWPoint where_) {
		name = namei;
		where = where_;
		mapObject = mapObjecti;
		setImage(fromIm);
	}
	public void loadImage(){
		setImage((new Picture(filename)).toImage(0),null); //freeSource();
		//properties = AniImage.AlwaysOnTop;
	}
}
