package CacheWolf;

public class MapInfoObject{
	//World file:
	// x scale
	// y scale
	// x rotation
	// y rotation
	// lon of upper left corner of image
	// lat of upper left corner of image
	// lon of lower right corner of image
	// lat of lower right corner of image
	public double[] affine = {0,0,0,0,0,0};
	public double lowlat = 0;
	public double lowlon = 0;
	public String fileName = new String();
	public String mapName = new String();
	boolean inBound = false;
}