package CacheWolf.navi;

import CacheWolf.CWPoint;

public class Area {
	 public final static int NOT_ON_EDGE = -1;
	 public final static int AT_TOP_EDGE = 1;
	 public final static int AT_RIGHT_EDGE = 2;
	 public final static int AT_BUTTOM_EDGE = 3;
	 public final static int AT_LEFT_EDGE = 4;
	 public static double edgeTolerance = 3 * 360/40000000; // approx 3m will be seen as the same

	 CWPoint topleft;
	 CWPoint buttomright;

	 public Area(CWPoint tl, CWPoint br){
		 topleft = new CWPoint(tl);
		 buttomright = new CWPoint(br);
	 }

	 public boolean isInBound(CWPoint p) {
		 if (topleft.latDec >= p.latDec && topleft.lonDec <= p.lonDec 
				 && buttomright.latDec <= p.latDec && buttomright.lonDec >= p.lonDec) return true;
		 else return false;
	 }
	 //if(affine[4] >= lati && lati >= lowlat && affine[5] <= loni && loni <= lowlon) isInBound = true;
		
	 public boolean isInBound(double lat, double lon) {
		 if (topleft.latDec >= lat && topleft.lonDec <= lon
				 && buttomright.latDec <= lat && buttomright.lonDec >= lon) return true;
		 else return false;
	 }
	 
	 public boolean isOverlapping(Area a) {
		 if (       isInBound(a.topleft) || isInBound(a.buttomright) 
				 || isInBound(a.buttomright.latDec, a.topleft.lonDec) // buttom left
				 || isInBound(a.topleft.latDec, a.buttomright.lonDec) // top right
				 // in case this is completly within a, the above tests will give false, so testing the otherway around
				 || a.isInBound(this.topleft) || a.isInBound(this.buttomright)
				 || a.isInBound(this.buttomright.latDec, this.topleft.lonDec) // buttom left
				 || a.isInBound(this.topleft.latDec, this.buttomright.lonDec)) // top right
			 return true;
		 else return false;
	 }

	 public boolean equals(Area a) {
		 if(java.lang.Math.abs(topleft.latDec - a.topleft.latDec) < edgeTolerance 
				 && java.lang.Math.abs(topleft.lonDec - a.topleft.lonDec) < edgeTolerance
				 && java.lang.Math.abs(buttomright.latDec - a.buttomright.latDec) < edgeTolerance
				 && java.lang.Math.abs(buttomright.lonDec - a.buttomright.lonDec) < edgeTolerance )
			 return true;
		 else return false;
	 }
	 
	 public int getEdge(CWPoint tl, CWPoint br) {
		 if (java.lang.Math.abs(topleft.latDec - br.latDec) < edgeTolerance 
				 && java.lang.Math.abs(topleft.lonDec - tl.lonDec) < edgeTolerance 
				 && java.lang.Math.abs(buttomright.lonDec - br.lonDec) < edgeTolerance)
			 return AT_TOP_EDGE;
			 if (java.lang.Math.abs(topleft.latDec - tl.latDec) < edgeTolerance 
					 && java.lang.Math.abs(buttomright.lonDec - tl.lonDec) < edgeTolerance 
					 && java.lang.Math.abs(buttomright.latDec - br.latDec) < edgeTolerance)
				 return AT_RIGHT_EDGE;
			 if (java.lang.Math.abs(topleft.lonDec - tl.lonDec) < edgeTolerance 
					 && java.lang.Math.abs(buttomright.latDec - tl.latDec) < edgeTolerance 
					 && java.lang.Math.abs(buttomright.lonDec - br.lonDec) < edgeTolerance)
				 return AT_BUTTOM_EDGE;
			 if (java.lang.Math.abs(topleft.latDec - tl.latDec) < edgeTolerance 
					 && java.lang.Math.abs(topleft.lonDec - br.lonDec) < edgeTolerance 
					 && java.lang.Math.abs(buttomright.latDec - br.latDec) < edgeTolerance)
				 return AT_LEFT_EDGE;
			 return NOT_ON_EDGE;
	 }
}