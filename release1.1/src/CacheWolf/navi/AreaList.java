package CacheWolf.navi;


/**
 * Class for handling a list of areas covered by a list of maps.
 * this is needed to determine if a new map should be downloaded or
 * if a map for the requested area already exists
 *
 * @author pfeffer
 *
 */
 public class AreaList extends ewe.util.LinkedListElement { // in java since v1.2 I would use java.util.TreeSet
/*
	 public AreaList(int s) {super(s);}

	 public void addArea(CWPoint topleft, CWPoint buttomright) {
		 Area tp = getArea(topleft);
		 if (tp != null && tp.isInBound(buttomright)) return; // area already completly in list
		 int edge = tp.getEdge(topleft, buttomright);
		 switch (edge) {
		 case Area.AT_TOP_EDGE: tp.topleft.latDec = topleft.latDec; break;
		 case Area.AT_RIGHT_EDGE: tp.buttomright.lonDec = buttomright.lonDec; break;
		 case Area.AT_BUTTOM_EDGE: tp.buttomright.latDec = buttomright.latDec; break;
		 case Area.AT_LEFT_EDGE: tp.topleft.lonDec = topleft.lonDec; break;
		 case Area.NOT_ON_EDGE: addAreaUnconditionally(new Area(topleft, buttomright));
		 }
	 }

	 public boolean AreaIsCovered(CWPoint topleft, CWPoint buttomright) {
		 Area tp = getArea(topleft);
		 if (tp != null && tp.isInBound(buttomright)) return true; // area already completly in list
		 else return false;

	 }

	 public void addArea(Area a) {
		 addArea(a.topleft, a.buttomright);
	 }

	 public static AreaList joinAreas(AreaList al){
		 AreaList ret = null;
		 while (!al.equals(ret)) {
			 ret = new AreaList(al.size());
			 for (int i=al.size()-1; i >= 0; i--) {
				 ret.addArea((Area)al.get(i));
			 }
		 }
		 return ret;
	 }

	 public boolean equals(AreaList al){
		 if (size() != al.size()) return false;
		 for (int i = size()-1; i >= 0; i--) {
			 if (!( ((Area)get(i)).equals((Area)al.get(i)) )) return false;
		 }
		 return true;
	 }

	 private void addAreaUnconditionally(Area a) {
		 add(a); // TODO insert at the correct / sorted position
	 }

	 public Area getArea(CWPoint p) {
		 Area ret;
		 for (int i=size()-1; i>=0; i--) {
			 ret = ((Area)get(i));
			 if(ret.isInBound(p)) return ret;
		 }
		 return null;
	 }

*/
 }
