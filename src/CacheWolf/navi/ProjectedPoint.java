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

import CacheWolf.database.CWPoint;
import CacheWolf.database.CoordinatePoint;
import CacheWolf.utils.Common;

public final class ProjectedPoint {

    public static final LambertProjection PJ_AUSTRIAN_LAMBERT_OLD = new LambertProjection(TransformCoordinates.EPSG_AUSTRIAN_LAMBERT_OLD, TransformCoordinates.BESSEL);
    public static final LambertProjection PJ_AUSTRIAN_LAMBERT_NEW = new LambertProjection(TransformCoordinates.EPSG_AUSTRIAN_LAMBERT_NEW, TransformCoordinates.WGS84);
    public static final LambertProjection PJ_FRENCH_LAMBERT_NTF_II = new LambertProjection(TransformCoordinates.EPSG_FRENCH_LAMBERT_NTF_II, TransformCoordinates.CLARKE1880IGN);
    public static final LambertProjection PJ_TEST = new LambertProjection(TransformCoordinates.EPSG_TEST, TransformCoordinates.CLARKE1866);
    public static final GkProjection PJ_GERMAN_GK = new GkProjection(TransformCoordinates.EPSG_GERMAN_GK2 - 2, 0, 500000, 3, 1000000, 0, 1, TransformCoordinates.BESSEL);
    public static final GkProjection PJ_ITALIAN_GB = new GkProjection(TransformCoordinates.EPSG_ITALIAN_GB_EW1 - 1, 0, 500000, 6, 1000000, 0, 0.9996, TransformCoordinates.HAYFORD1909);
    public static final UTMProjection PJ_UTM_WGS84 = new UTMProjection(TransformCoordinates.WGS84);
    public static final UTMProjectionFixZone PJ_UTM_WGS84FZ = new UTMProjectionFixZone(TransformCoordinates.WGS84);

    static {
        PJ_AUSTRIAN_LAMBERT_OLD.setup(400000, 400000, 49.0, 46.0, 1, 47.5, 13.333333);
        PJ_AUSTRIAN_LAMBERT_NEW.setup(400000, 400000, 49.0, 46.0, 1, 47.5, 13.333333);
        PJ_FRENCH_LAMBERT_NTF_II.setup(2200000, 600000, 46.8, 46.8, 0.99987742, 46.8, 2.337229172 /*(2+20/60+14.025/3600) */);
        PJ_TEST.setup(150000, 250000, 18, 18, 1, 18, -77);
    }

    public double rawNorthing;
    public double rawEasting;
    // because it is not clear for routines from outside if the stripe number is included, make this available only through methods
    public int zone;
    public Projection projection;

    public ProjectedPoint() {
    }

    public ProjectedPoint(Projection projection) {
        this.projection = projection;
    }

    public ProjectedPoint(ProjectedPoint pp) {
        rawNorthing = pp.rawNorthing;
        rawEasting = pp.rawEasting;
        zone = pp.zone;
        this.projection = pp.projection;
    }

    public ProjectedPoint(CWPoint wgs84, Projection projection) {
        this.projection = projection;
        this.projection.project(wgs84, this);
    }

    /**
     * automatically projects wgs84 onto epsg OR
     * creates an ProjectedPoint with <br>
     * lat/lon = northing/easting (in local notation)
     *
     * @param p                Point to be projected OR lat/lon = northing/easting
     * @param epsg_localsystem EPSG-Code OR ProjecetPoint.LOCALSYSTEM_XXX
     * @param isProjected      if true, p contains northing in lat and easting in lon <br>
     *                         if false p will be projected to epsg
     */
    public ProjectedPoint(CWPoint p, int epsg_localsystem, boolean isProjected, boolean isLocalsystem) {
        if (isProjected)
            set(p, null, epsg_localsystem, isLocalsystem);
        else {
            projection = (isLocalsystem ? getProjectionFromLs(epsg_localsystem) : getProjection(epsg_localsystem));
            if (projection.epsgCode == 0) {
                if (isLocalsystem)
                    projection.epsgCode = projection.getEpsgcode(epsg_localsystem);
                else
                    projection.epsgCode = epsg_localsystem; // pass the epsg code to the projection if the projection works for several epsg code which are not directly one after the other
            }
            if (isLocalsystem)
                projection.project(p, this);
            else
                projection.project(p, this, epsg_localsystem); // the epsg is required here because each zone has a different epsg, so the zone is already fixed
        }
    }

    public ProjectedPoint(CWPoint p, String zone, int epsg_localsystem, boolean isLocalsystem) {
        set(p, zone, epsg_localsystem, isLocalsystem);
    }

    static Projection getProjection(int epsg) {
        if (epsg >= 25828 && epsg <= 25838)
            return PJ_UTM_WGS84FZ;
        switch (epsg) {
            case TransformCoordinates.EPSG_AUSTRIAN_LAMBERT_OLD:
                return PJ_AUSTRIAN_LAMBERT_OLD;
            case TransformCoordinates.EPSG_AUSTRIAN_LAMBERT_NEW:
                return PJ_AUSTRIAN_LAMBERT_NEW;
            case TransformCoordinates.EPSG_GERMAN_GK2:
            case TransformCoordinates.EPSG_GERMAN_GK3:
            case TransformCoordinates.EPSG_GERMAN_GK4:
            case TransformCoordinates.EPSG_GERMAN_GK5:
                return PJ_GERMAN_GK;
            case TransformCoordinates.EPSG_ITALIAN_GB_EW1:
            case TransformCoordinates.EPSG_ITALIAN_GB_EW2:
                return PJ_ITALIAN_GB;
            case TransformCoordinates.EPSG_FRENCH_LAMBERT_NTF_II:
                return PJ_FRENCH_LAMBERT_NTF_II;
            case TransformCoordinates.LOCALSYSTEM_UTM_WGS84:
                return PJ_UTM_WGS84;
            case TransformCoordinates.EPSG_SwedenUTM:
                return PJ_UTM_WGS84FZ;
            default:
                throw new IllegalArgumentException("ProjectedPoint.getProjection: epsg-code: " + epsg + "not supported");
        }
    }

    private static Projection getProjectionFromLs(int localsystem) {
        switch (localsystem) {
            case TransformCoordinates.LOCALSYSTEM_AUSTRIAN_LAMBERT_OLD:
                return PJ_AUSTRIAN_LAMBERT_OLD;
            case TransformCoordinates.LOCALSYSTEM_AUSTRIAN_LAMBERT_NEW:
                return PJ_AUSTRIAN_LAMBERT_NEW;
            case TransformCoordinates.LOCALSYSTEM_GERMAN_GK:
                return PJ_GERMAN_GK;
            case TransformCoordinates.LOCALSYSTEM_ITALIAN_GB:
                return PJ_ITALIAN_GB;
            case TransformCoordinates.LOCALSYSTEM_FRANCE_LAMBERT_IIE:
                return PJ_FRENCH_LAMBERT_NTF_II;
            case TransformCoordinates.LOCALSYSTEM_UTM_WGS84:
                return PJ_UTM_WGS84;
            case TransformCoordinates.LOCALSYSTEM_SWEDEN:
                return PJ_UTM_WGS84FZ;
            case TransformCoordinates.LOCALSYSTEM_UTM28Nto38N:
                return PJ_UTM_WGS84FZ;
            default:
                throw new IllegalArgumentException("ProjectedPoint(CWPoint, int): region " + localsystem + " not supported");
        }
    }

    /**
     * Set with local notation, incl. falsenorthing and -easting
     *
     * @param northing_
     * @param easting_
     * @param zone      only put something here if the zone is not included in easting or northing and must be known, otherwise zone should be null
     */
    public void set(CWPoint projected, String zone, int epsg_localsystem, boolean isLocalsystem) {
        projection = (isLocalsystem ? getProjectionFromLs(epsg_localsystem) : getProjection(epsg_localsystem));
        if (projection.epsgCode == 0) {
            if (isLocalsystem)
                projection.epsgCode = projection.getEpsgcode(epsg_localsystem);
            else
                projection.epsgCode = epsg_localsystem; // pass the epsg code to the projection if the projection works for several epsg code which are not directly one after the other
        }
        set(projected.latDec, projected.lonDec, zone);
    }

    /**
     * easting measured in meters from stripe middle
     *
     * @return
     */
    public double getRawEasting() {
        return rawEasting;
    }

    /**
     * northing measured in meters from stripe middle
     *
     * @return
     */
    public double getRawNorthing() {
        return rawNorthing;
    }

    public void setRaw(double northing_, double easting_) {
        rawNorthing = northing_;
        rawEasting = easting_;
    }

    public double getNorthing() {
        return projection.getNorthing(this);
    }

    public double getEasting() {
        return projection.getEasting(this);
    }

    public ProjectedPoint cloneIt() {
        return new ProjectedPoint(this);
    }

    public CoordinatePoint toCoordinatePoint() {
        return new CoordinatePoint(getNorthing(), getEasting());
    }

    public void setzone(int z) {
        zone = z;
    }

    public int getZone() {
        return zone;
    }

    public String getZoneString() {
        if (projection.zoneSeperately)
            return projection.getZone(this);
        else
            return "";
    }

    public int getEpsgCode() {
        return projection.getEpsgcode(this);
    }

    /**
     * Set with local notation, incl. falsenorthing and -easting
     *
     * @param northing_
     * @param easting_
     * @param zone      only put something here if the zone is not included in easting or northing and must be known, otherwise zone should be null
     */
    public void set(double northing_, double easting_, String zone) {
        if (zone == null)
            projection.set(northing_, easting_, this);
        else
            projection.set(northing_, easting_, zone, this);
    }

    public CWPoint unproject() {
        return projection.unproject(this);
    }

    public String toString() {
        return toString(2, "", " ");
    }

    public String toHumanReadableString() {
        return toString(0, "", " ");
    }

    public String toString(int decimalplaces, String prefix, String separator) {
        double n = projection.getNorthing(this);
        double e = projection.getEasting(this);
        String z = (projection.zoneSeperately ? projection.getZone(this) + " " : "");
        return z + prefix + Common.DoubleToString(e, 0, 0) + separator + Common.DoubleToString(n, 0, 0);
    }

    /**
     * shift the point
     *
     * @param meters    positive to north (east), negative to south (west)
     * @param direction 0 north-south, 1 east-west
     */
    public void shift(double meters, int direction) {
        switch (direction) { // TODO this works correctly only within a stripe/zone
            case 0:
                rawNorthing += meters;
                return;
            case 1:
                rawEasting += meters;
                return;
        }
    }
}
