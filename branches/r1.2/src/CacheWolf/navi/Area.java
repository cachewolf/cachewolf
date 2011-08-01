/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf.navi;
import CacheWolf.CWPoint;

public class Area {
	/* not used
	 public final static int NOT_ON_EDGE = -1;
	 public final static int AT_TOP_EDGE = 1;
	 public final static int AT_RIGHT_EDGE = 2;
	 public final static int AT_BOTTOM_EDGE = 3;
	 public final static int AT_LEFT_EDGE = 4;
	 public static double edgeTolerance = 3 * 360/40000000; // approx 3m will be seen as the same
*/
	 public CWPoint topleft;
	 public CWPoint bottomright;
	 
	 public Area() {
		 topleft = new CWPoint();
		 bottomright = new CWPoint();
	 }

	 public Area(TrackPoint tl, TrackPoint br){
		 topleft = new CWPoint(tl);
		 bottomright = new CWPoint(br);
	 }
	 
	 public Area(CWPoint tl, CWPoint br) {
		 topleft = tl;
		 bottomright = br;
	 }

	 public final boolean isInBound(TrackPoint p) {
		 if (topleft.latDec >= p.latDec && topleft.lonDec <= p.lonDec 
				 && bottomright.latDec <= p.latDec && bottomright.lonDec >= p.lonDec) return true;
		 else return false;
	 }
		
	 public final boolean isInBound(double lat, double lon) {
		 if (topleft.latDec >= lat && topleft.lonDec <= lon
				 && bottomright.latDec <= lat && bottomright.lonDec >= lon) return true;
		 else return false;
	 }

	/**
	 * test if a is completly within this
	 * @param a
	 * @return
	 */
	 public final boolean isInBound(Area a) {
		 return (isInBound(a.topleft) && isInBound(a.bottomright));
	 }
	 
	 
	 public final boolean isOverlapping(Area a) {
		 return ! ( // test if not overlapping and invert the result, see http://www.geoclub.de/viewtopic.php?f=40&t=38364&p=607033#p607033
				    this.bottomright.latDec > a.topleft.latDec
				 || this.topleft.latDec     < a.bottomright.latDec
				 || this.bottomright.lonDec < a.topleft.lonDec
				 || this.topleft.lonDec     > a.bottomright.lonDec);
	 }
	 
	 /* not used at the moment
	  public boolean equals(Area a) {
		 if(java.lang.Math.abs(topleft.latDec - a.topleft.latDec) < edgeTolerance 
				 && java.lang.Math.abs(topleft.lonDec - a.topleft.lonDec) < edgeTolerance
				 && java.lang.Math.abs(bottomright.latDec - a.bottomright.latDec) < edgeTolerance
				 && java.lang.Math.abs(bottomright.lonDec - a.bottomright.lonDec) < edgeTolerance )
			 return true;
		 else return false;
	 }
	 */
	 
	 /* not used at the moment
	 public int getEdge(CWPoint tl, CWPoint br) {
		 if (java.lang.Math.abs(topleft.latDec - br.latDec) < edgeTolerance 
				 && java.lang.Math.abs(topleft.lonDec - tl.lonDec) < edgeTolerance 
				 && java.lang.Math.abs(bottomright.lonDec - br.lonDec) < edgeTolerance)
			 return AT_TOP_EDGE;
			 if (java.lang.Math.abs(topleft.latDec - tl.latDec) < edgeTolerance 
					 && java.lang.Math.abs(bottomright.lonDec - tl.lonDec) < edgeTolerance 
					 && java.lang.Math.abs(bottomright.latDec - br.latDec) < edgeTolerance)
				 return AT_RIGHT_EDGE;
			 if (java.lang.Math.abs(topleft.lonDec - tl.lonDec) < edgeTolerance 
					 && java.lang.Math.abs(bottomright.latDec - tl.latDec) < edgeTolerance 
					 && java.lang.Math.abs(bottomright.lonDec - br.lonDec) < edgeTolerance)
				 return AT_BOTTOM_EDGE;
			 if (java.lang.Math.abs(topleft.latDec - tl.latDec) < edgeTolerance 
					 && java.lang.Math.abs(topleft.lonDec - br.lonDec) < edgeTolerance 
					 && java.lang.Math.abs(bottomright.latDec - br.latDec) < edgeTolerance)
				 return AT_LEFT_EDGE;
			 return NOT_ON_EDGE;
	 }
	 */
	  
	 /**
	  * get an easy find string for this area
	  * @return
	  */
	 public final String getEasyFindString() {
		 String ul = getEasyFindString(topleft, 30);
		 String br = getEasyFindString(bottomright, 30);
		 int i;
		 for (i=0; i<br.length(); i++ ) {
			 if (ul.charAt(i) != br.charAt(i)) break;
		 }
		 return ul.substring(0, i);
	 }
	 
	 /**
	  * get an easy find string for a given point with precision prec
	  * @param prec number of digits to return, min 2, max: 30
	  * @return
	  */
	 public static final String getEasyFindString(CWPoint p, int prec) {
		 double longinrange = p.lonDec;
		 if (longinrange > 180) longinrange -= 180;
		 int lat = (int) (((p.latDec+90d)/180d) * (1 << (prec)));
		 int lon = (int) (((longinrange+180)/360) * (1 << (prec)));
		 String ret = "";
		 int tmp;
		 for (int i=prec-1; i>=0;  i--) {
			 tmp = (1 << i);
			 tmp = (lat & (1 << i));
			 tmp = ((lat & (1 << i)) >> i);
			 tmp = ((lon & (1 << i)) >> i) + (((lat & (1 << i) ) << 1) >> i);
			 ret += Integer.toString(tmp);
		 }
		 return ret;
	 }
	 
	 public static final boolean containsRoughly(String boundingbox, String q) {
		 if (boundingbox.length() <= q.length() ) return q.startsWith(boundingbox);
		 else return boundingbox.startsWith(q);
	 }
	 
	 public String toString() {
		 return topleft.toString() + ", " + bottomright.toString();
	 }
	 
	 public final CWPoint getCenter() {
		 return new CWPoint((topleft.latDec + bottomright.latDec)/2, (topleft.lonDec + bottomright.lonDec)/2);
	 }
}
