/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
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

abstract class Projection {
    /** when implemented: <br>
     * a) if the projection covers only one epsgCode here it should be written <br>
     * b) if the projection covers several zones which have different epsg codes
     * put here the figure which must be added to the zone number to get the 
     * corresponding epsg-codes (see method getEpsgcode)
     */
    public int epsgCode;
    public boolean zoneSeperately = false;

    /**
     * The zone is automatically determined
     * rember to set the zone in pp when you implement this method 
     * @param ll
     * @param pp: pp will be filled with the projected ll. If null, a new ProjectedPoint will be created
     * @return
     */
    public abstract ProjectedPoint project(CWPoint wgs84, ProjectedPoint pp);

    /**
     * Zone is fixed by epsg-code
     * @param ll
     * @param pp
     * @param epsg
     * @return
     */
    public abstract ProjectedPoint project(CWPoint ll, ProjectedPoint pp, int epsg);

    public abstract CWPoint unproject(ProjectedPoint pp);

    /**
     * Returns the projected Northing in local notation
     * @param pp
     * @return
     */
    public abstract double getNorthing(ProjectedPoint pp);

    public abstract double getEasting(ProjectedPoint pp);

    public int getEpsgcode(ProjectedPoint pp) {
	return epsgCode + pp.zone;
    }

    public abstract ProjectedPoint set(double northing, double easting, ProjectedPoint pp);

    public ProjectedPoint set(double northing, double easting, String zone, ProjectedPoint pp) {
	throw new UnsupportedOperationException("Projection.set (double, double String, ProjectedPoint): This projection uses no seperate zones");
    }

    public String getZone(ProjectedPoint pp) {
	throw new UnsupportedOperationException("Projection.getZone (double, double String, ProjectedPoint): This projection uses no seperate zones");
    }

    public String toHumanReadableString(ProjectedPoint pp) {
	return pp.toString(0, "", " ");
    }

    /**
     * In case the same Projection-class is used for several epsg codes, this
     * method translates the localsystem to the corresponding epsg code.  
     * It is used by UTMProjectionFixZone which can be used to project all
     * epsg codes which represent just one UTM stripe, like Sweden. 
     * ProjectedPoint sets projection.epsgCode, if it is zero. 
     */
    public int getEpsgcode(int localsystem) {
	throw new UnsupportedOperationException("Projection.getEpsg(localsystem): This projection has getEpsg not implemented.");
    }

}
