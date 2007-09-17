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
	 
	 public Area() {
		 topleft = new CWPoint();
		 buttomright = new CWPoint();
	 }

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

	/**
	 * test if a is completly within this
	 * @param a
	 * @return
	 */
	 public boolean isInBound(Area a) {
		 return (isInBound(a.topleft) && isInBound(a.buttomright));
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
	 
	 public String getEasyFindString() {
		 String ul = getEasyFindString(topleft, 60);
		 String br = getEasyFindString(buttomright, 60);
		 int i;
		 for (i=0; i<br.length(); i++ ) {
			 if (ul.charAt(i) != br.charAt(i)) break;
		 }
		 ewe.sys.Vm.debug(ul+"\n"+br+"\n i:"+i);
		 return ul.substring(0, i);
	 }
	 
	 /**
	  * 
	  * @param prec number of digits to return, min 2, max: 63
	  * @return
	  */
	 public static String getEasyFindString(CWPoint p, int prec) {
		 double longinrange = p.lonDec;
		 if (longinrange > 180) longinrange -= 180;
		 Double kw = new Double(((p.latDec+90)/180) * (double) (1l << (prec)));
		 Long lat = new Double(((p.latDec+90)/180) * (double) (1l << (prec))).longValue(); // TODO handle negative values
		 lat = kw.longValue();
		 kw = (double) (1l << (prec));
		 
		 kw = new Double(((longinrange+180)/360) * (2 ^ (prec -1)));
		 Long lon = new Double(((longinrange+180)/360) * (double) (1l << (prec))).longValue(); // 180 = 10110100
		 String ret = "";
		 Long tmp;
		 for (int i=prec-1; i>=0;  i--) {
			 tmp = (1l << i);
			 tmp = (lat & (1l << i));
			 tmp = ((lat & (1l << i)) >> i);
			 tmp = ((lon & (1l << i)) >> i) + (((lat & (1l << i) ) << 1) >> i);
			 ret += tmp.toString();
		 }
/*		 Area cmp = new Area(new CWPoint (90,0), new CWPoint(-90,180));
		 if (cmp.isInBound(this)) ret += "0";
		 else ret += "1";
		 int i;
		 while (true) {
			 for (i=0) 
			 break;
		 }
	*/	 return ret;
	 }
}