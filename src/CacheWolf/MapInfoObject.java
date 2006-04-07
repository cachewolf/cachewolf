package CacheWolf;

import ewe.sys.*;

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
	public String fileNameWFL = new String();
	public String fileName = new String();
	public String mapName = new String();
	
	public boolean inBound(CWGPSPoint pos){
		boolean isInBound = false;
		Vm.debug(mapName);
		Vm.debug("Top: " + affine[4]);
		Vm.debug("Bottom: " + lowlat);
		Vm.debug("Test: " + pos.latDec);
		Vm.debug("Left: " + affine[5]);
		Vm.debug("Right: " + lowlon);
		Vm.debug("Test: " + pos.lonDec);
		if(affine[4] >= pos.latDec && pos.latDec >= lowlat && affine[5] <= pos.lonDec && pos.lonDec <= lowlon) isInBound = true;
		return isInBound;
	}
}