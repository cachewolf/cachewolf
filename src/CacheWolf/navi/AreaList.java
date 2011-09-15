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

	 public void addArea(CWPoint topleft, CWPoint bottomright) {
		 Area tp = getArea(topleft);
		 if (tp != null && tp.isInBound(bottomright)) return; // area already completly in list
		 int edge = tp.getEdge(topleft, bottomright);
		 switch (edge) {
		 case Area.AT_TOP_EDGE: tp.topleft.latDec = topleft.latDec; break;
		 case Area.AT_RIGHT_EDGE: tp.bottomright.lonDec = bottomright.lonDec; break;
		 case Area.AT_BOTTOM_EDGE: tp.bottomright.latDec = bottomright.latDec; break;
		 case Area.AT_LEFT_EDGE: tp.topleft.lonDec = topleft.lonDec; break;
		 case Area.NOT_ON_EDGE: addAreaUnconditionally(new Area(topleft, bottomright));
		 }
	 }

	 public boolean AreaIsCovered(CWPoint topleft, CWPoint bottomright) {
		 Area tp = getArea(topleft);
		 if (tp != null && tp.isInBound(bottomright)) return true; // area already completly in list
		 else return false;

	 }

	 public void addArea(Area a) {
		 addArea(a.topleft, a.bottomright);
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
