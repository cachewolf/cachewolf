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

public final class GkProjection extends Projection {
    double falseEasting;
    double falseNorthing;
    double degreeOfStripe0;
    double stripeWidth;
    double stripeFactor;
    double scale;
    Ellipsoid ellip;

    public GkProjection(int startEpsg_, double falseNorthing_, double falseEasting_, double stripeWidth_, double stripeFactor_, double lonOfStripe0, double scale_, Ellipsoid ellip_) {
        epsgCode = startEpsg_;
        falseNorthing = falseNorthing_;
        falseEasting = falseEasting_;
        stripeWidth = stripeWidth_;
        degreeOfStripe0 = lonOfStripe0;
        stripeFactor = stripeFactor_;
        scale = scale_;
        ellip = ellip_;
    }

    /**
     * Converts Gauß-Krüger-coordinates into lat/lon on the respective ellipsoid
     *
     * @param gkp
     * @param stripelon:  Lon of the center of the stripe
     * @param ellipsoid
     * @param stripewidth width in degree of the stripe of the Gauß-Krüger-System (3 degreee usually used in Gauß-Krüger, 6 degree usually in UTM)
     * @return
     */
    public static CWPoint unproject(ProjectedPoint gkp, double stripelon, Ellipsoid ellipsoid, double scale) {
        double L0 = stripelon; // decimal degree of the center of the stripe
        double y = gkp.getRawEasting() / scale;

        double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b) / (ellipsoid.a * ellipsoid.a);
        // note: n1-n6 are similiar to the n1-n6 in projectLatlon2GK, but some term have different factors
        double n1 = (ellipsoid.a - ellipsoid.b) / (ellipsoid.a + ellipsoid.b);
        double n2 = (ellipsoid.a + ellipsoid.b) / 2 * (1 + Math.pow(n1, 2) / 4 + Math.pow(n1, 4) / 64);
        double n3 = n1 * 3 / 2 - Math.pow(n1, 3) * 27 / 32 + Math.pow(n1, 5) * 269 / 32;
        double n4 = Math.pow(n1, 2) * 21 / 16 - Math.pow(n1, 4) * 55 / 32;
        double n5 = Math.pow(n1, 3) * 151 / 96 - Math.pow(n1, 5) * 417 / 128;
        double n6 = Math.pow(n1, 4) * 1097 / 512;

        double B0 = (gkp.getRawNorthing() / scale) / n2;
        double Bf = B0 + n3 * Math.sin(B0 * 2) + n4 * Math.sin(B0 * 4) + n5 * Math.sin(B0 * 6) + n6 * Math.sin(B0 * 8);

        double Nf = ellipsoid.a / Math.sqrt(1 - e2 * Math.pow(Math.sin(Bf), 2));
        double nuef = Math.sqrt(ellipsoid.a * ellipsoid.a / ellipsoid.b / ellipsoid.b * e2 * Math.pow(Math.cos(Bf), 2));
        double tf = Math.tan(Bf);

        double la1 = tf / 2 / Nf / Nf * (-1 - nuef * nuef) * y * y;
        double la2 = tf / 24 / Math.pow(Nf, 4) * (5 + 3 * tf * tf + 6 * nuef * nuef - 6 * tf * tf * nuef * nuef - 4 * Math.pow(nuef, 4) - 9 * tf * tf * Math.pow(nuef, 4)) * Math.pow(y, 4);
        // these deal with less than the overall calculation precision: double la3 = tf /720 / Math.pow(Nf, 6) * (-61 - 90*tf*tf - 45*Math.pow(tf,4) - 107*nuef*nuef + 162*tf*tf * Math.pow(nuef, 2) + 45*Math.pow(tf,4)*tf*Math.pow(nuef, 2)) * Math.pow(y, 6);
        // these deal with less than the overall calculation precision: double la4 = tf /40320 / Math.pow(Nf, 8) * (1385+3663*tf*tf - 4095*Math.pow(tf,4) + 1575*Math.pow(nuef, 6)) * Math.pow(y, 8);
        double lat = (Bf + la1 + la2) * 180 / Math.PI;

        double lo1 = 1 / Nf / Math.cos(Bf) * y;
        double lo2 = 1 / Math.pow(Nf, 3) / Math.cos(Bf) * (-1 - tf * tf * 2 - nuef * nuef) * Math.pow(y, 3) / 6;
        double lon = L0 + (lo1 + lo2) * 180 / Math.PI;
        return new CWPoint(lat, lon);
    }

    public static ProjectedPoint project(CWPoint latlon, Ellipsoid ellipsoid, double stripewidth, int stripe, double degreeOfStripe0, double scale, ProjectedPoint gkp) {
        double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b) / (ellipsoid.a * ellipsoid.a);
        double l = latlon.lonDec;
        if (l < 0)
            l += 360;
        l = (l - degreeOfStripe0 - stripe * stripewidth) / 180 * Math.PI;
        //		if (l < - 2* Math.PI) l += 4 * Math.PI;
        double B = latlon.latDec / 180 * Math.PI;
        double N = ellipsoid.a / Math.sqrt(1 - e2 * Math.pow(Math.sin(B), 2));
        double nue = Math.sqrt(Math.pow(ellipsoid.a, 2) / Math.pow(ellipsoid.b, 2) * e2 * Math.pow(Math.cos(B), 2));
        double t = Math.tan(B);

        double n1 = (ellipsoid.a - ellipsoid.b) / (ellipsoid.a + ellipsoid.b);
        double n2 = (ellipsoid.a + ellipsoid.b) / 2 * (1 + Math.pow(n1, 2) / 4 + Math.pow(n1, 4) / 64);
        double n3 = n1 * -3 / 2 + Math.pow(n1, 3) * 9 / 16 - Math.pow(n1, 5) * 3 / 32;
        double n4 = Math.pow(n1, 2) * 15 / 16 - Math.pow(n1, 4) * 15 / 32;
        double n5 = Math.pow(n1, 3) * -35 / 48 + Math.pow(n1, 5) * 105 / 256;
        double n6 = Math.pow(n1, 4) * 315 / 512;
        double arclength = n2 * (B + n3 * Math.sin(B * 2) + n4 * Math.sin(B * 4) + n5 * Math.sin(B * 6) + n6 * Math.sin(B * 8));

        double h1 = t / 2 * N * Math.pow(Math.cos(B), 2) * l * l;
        double h2 = t / 24 * N * Math.pow(Math.cos(B), 4) * (5 - t * t + 9 * nue * nue + 4 * Math.pow(nue, 4)) * Math.pow(l, 4);
        double northing = (arclength + h1 + h2) * scale;

        double r1 = N * Math.cos(B) * l;
        double r2 = N / 6 * Math.pow(Math.cos(B), 3) * (1 - t * t + nue * nue) * l * l * l;
        double easting = (r1 + r2) * scale; //+ stripe / stripewidth * 1000000 + 500000;
        gkp.setRaw(northing, easting);
        return gkp;
    }

    //Overrides: getEasting(...) in Projection
    public double getEasting(ProjectedPoint pp) {
        return pp.rawEasting + falseEasting + pp.zone * stripeFactor;
    }

    //Overrides: getNorthing(...) in Projection
    public double getNorthing(ProjectedPoint pp) {
        return pp.rawNorthing + falseNorthing;
    }

    /**
     * Project latlon to Gauß-Krüger-Coordinates on ellipsoid
     *
     * @param latlon
     * @param ellipsoid
     * @return
     */
    public ProjectedPoint project(CWPoint ll, ProjectedPoint pp) {
        double lonDec = ll.lonDec - degreeOfStripe0 + stripeWidth / 2;
        if (lonDec < 0)
            lonDec += 360;
        int stripe = (int) Math.floor(lonDec / stripeWidth);
        if (pp == null)
            pp = new ProjectedPoint(this);
        pp.setzone(stripe);
        return project(ll, ellip, stripeWidth, stripe, degreeOfStripe0, scale, pp);
    }

    //Overrides: unproject(...) in Projection

    //Overrides: project(...) in Projection
    public ProjectedPoint project(CWPoint ll, ProjectedPoint pp, int epsg) {
        if (pp == null)
            pp = new ProjectedPoint(this);
        pp.setzone(epsg - epsgCode);
        return project(ll, ellip, stripeWidth, epsg - epsgCode, degreeOfStripe0, scale, pp);
    }

    //Overrides: set(...) in Projection
    public ProjectedPoint set(double northing, double easting, ProjectedPoint pp) {
        double stripei = Math.floor(easting / stripeFactor);
        pp.setzone((int) stripei);
        pp.setRaw(northing - falseNorthing, easting - stripeFactor * stripei - falseEasting);
        return pp;
    }

    /**
     * Converts Gauß-Krüger-coordinates into lat/lon on the respective ellipsoid
     *
     * @param gkp
     * @param ellipsoid
     * @param stripewidth width in degree of the stripe of the Gauß-Krüger-System (3 degreee usually used in Gauß-Krüger, 6 degree usually in UTM)
     * @return
     */
    public CWPoint unproject(ProjectedPoint gkp) {
        double L0 = gkp.zone * stripeWidth + degreeOfStripe0; // decimal degree of the center of the stripe
        return unproject(gkp, L0, ellip, scale);
    }

}
