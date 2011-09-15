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

public class Ellipsoid {
	public double a, b;
	/**
	 * 
	 * @param ai
	 * @param bi
	 * @param isminoraxis if true bi is interpreted as axis, if false bi is interpreted as flattening
	 */
	public Ellipsoid(double ai, double bi, boolean isminoraxis ) {
		a = ai;
		if (isminoraxis) b = bi; // flattening = (a - b) / a
		else {
			b = a - (1/bi) * a;
		}
	}
	
	   /**
	    * Get semi-major axis.
	    * @return semi-major axis (in meters).
	    */
	   public double getSemiMajorAxis()
	   {
	     return a;
	   }

	   /**
	    * Get semi-minor axis.
	    * @return semi-minor axis (in meters).
	    */
	   public double getSemiMinorAxis()
	   {
	     return b;
	   }

	   /**
	    * Get flattening
	    * @return
	    */
	   public double getFlattening()
	   {
	     return (a - b) / a;
	   }

	   /**
	    * Get inverse flattening.
	    * @return
	    */
	   public double getInverseFlattening()
	   {
	     return a / (a - b);
	   }}
