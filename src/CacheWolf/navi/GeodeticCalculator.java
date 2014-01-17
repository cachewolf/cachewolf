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

import CacheWolf.database.CoordinatePoint;

/* Geodesy by Mike Gavaghan
 * 
 * http://www.gavaghan.org/blog/free-source-code/geodesy-library-vincentys-formula/
 * 
 * This code may be freely used and modified on any personal or professional
 * project.  It comes with no warranty.
 */
//package org.gavaghan.geodesy;
/**
 * <p>
 * Implementation of Thaddeus Vincenty's algorithms to solve the direct and inverse
 * geodetic problems. For more information, see Vincent's original publication on
 * the NOAA website:
 * </p>
 * See http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
 * 
 * @author Mike Gavaghan
 * 
 * Adapted for CacheWolf by Michael Kunz (MiK77)
 */
public class GeodeticCalculator {
    static private final double TwoPi = 2.0 * Math.PI;

    /** Degrees/Radians conversion constant. */
    static private final double PiOver180 = Math.PI / 180.0;

    /**
     * Calculate the destination and final bearing after traveling a specified
     * distance, and a specified starting bearing, for an initial location. This
     * is the solution to the direct geodetic problem.
     * 
     * @param ellipsoid reference ellipsoid to use
     * @param start starting location
     * @param startBearing starting bearing (degrees)
     * @param distance distance to travel (meters)
     * @param endBearing bearing at destination (degrees) element at index 0 will
     *            be populated with the result
     * @return
     */
    static public CoordinatePoint calculateEndingGlobalCoordinates(Ellipsoid ellipsoid, CoordinatePoint start, double startBearing, double distance, double[] endBearing) {
	double a = ellipsoid.getSemiMajorAxis();
	double b = ellipsoid.getSemiMinorAxis();
	double aSquared = a * a;
	double bSquared = b * b;
	double f = ellipsoid.getFlattening();
	double phi1 = start.latDec * PiOver180;
	double alpha1 = startBearing * PiOver180;
	double cosAlpha1 = Math.cos(alpha1);
	double sinAlpha1 = Math.sin(alpha1);
	double s = distance;
	double tanU1 = (1.0 - f) * Math.tan(phi1);
	double cosU1 = 1.0 / Math.sqrt(1.0 + tanU1 * tanU1);
	double sinU1 = tanU1 * cosU1;

	// eq. 1
	double sigma1 = Math.atan2(tanU1, cosAlpha1);

	// eq. 2
	double sinAlpha = cosU1 * sinAlpha1;

	double sin2Alpha = sinAlpha * sinAlpha;
	double cos2Alpha = 1 - sin2Alpha;
	double uSquared = cos2Alpha * (aSquared - bSquared) / bSquared;

	// eq. 3
	double A = 1 + (uSquared / 16384) * (4096 + uSquared * (-768 + uSquared * (320 - 175 * uSquared)));

	// eq. 4
	double B = (uSquared / 1024) * (256 + uSquared * (-128 + uSquared * (74 - 47 * uSquared)));

	// iterate until there is a negligible change in sigma
	double deltaSigma;
	double sOverbA = s / (b * A);
	double sigma = sOverbA;
	double sinSigma;
	double prevSigma = sOverbA;
	double sigmaM2;
	double cosSigmaM2;
	double cos2SigmaM2;

	for (;;) {
	    // eq. 5
	    sigmaM2 = 2.0 * sigma1 + sigma;
	    cosSigmaM2 = Math.cos(sigmaM2);
	    cos2SigmaM2 = cosSigmaM2 * cosSigmaM2;
	    sinSigma = Math.sin(sigma);
	    double cosSignma = Math.cos(sigma);

	    // eq. 6
	    deltaSigma = B * sinSigma * (cosSigmaM2 + (B / 4.0) * (cosSignma * (-1 + 2 * cos2SigmaM2) - (B / 6.0) * cosSigmaM2 * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM2)));

	    // eq. 7
	    sigma = sOverbA + deltaSigma;

	    // break after converging to tolerance
	    if (Math.abs(sigma - prevSigma) < 0.0000000000001)
		break;

	    prevSigma = sigma;
	}

	sigmaM2 = 2.0 * sigma1 + sigma;
	cosSigmaM2 = Math.cos(sigmaM2);
	cos2SigmaM2 = cosSigmaM2 * cosSigmaM2;

	double cosSigma = Math.cos(sigma);
	sinSigma = Math.sin(sigma);

	// eq. 8
	double phi2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1, (1.0 - f) * Math.sqrt(sin2Alpha + Math.pow(sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1, 2.0)));

	// eq. 9
	double tanLambda = sinSigma * sinAlpha1 / (cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
	double lambda = Math.atan(tanLambda);

	// eq. 10
	double C = (f / 16) * cos2Alpha * (4 + f * (4 - 3 * cos2Alpha));

	// eq. 11
	double L = lambda - (1 - C) * f * sinAlpha * (sigma + C * sinSigma * (cosSigmaM2 + C * cosSigma * (-1 + 2 * cos2SigmaM2)));

	// eq. 12
	double alpha2 = Math.atan2(sinAlpha, -sinU1 * sinSigma + cosU1 * cosSigma * cosAlpha1);

	// build result
	double latitude = phi2 / PiOver180;
	double longitude = start.lonDec + (L / PiOver180);

	if ((endBearing != null) && (endBearing.length > 0)) {
	    endBearing[0] = alpha2 / PiOver180;
	}

	return new CoordinatePoint(latitude, longitude);
    }

    /**
     * Calculate the destination after traveling a specified distance, and a
     * specified starting bearing, for an initial location. This is the solution
     * to the direct geodetic problem.
     * 
     * @param ellipsoid reference ellipsoid to use
     * @param start starting location
     * @param startBearing starting bearing (degrees)
     * @param distance distance to travel (meters)
     * @return
     */
    static public CoordinatePoint calculateEndingGlobalCoordinates(Ellipsoid ellipsoid, CoordinatePoint start, double startBearing, double distance) {
	return calculateEndingGlobalCoordinates(ellipsoid, start, startBearing, distance, null);
    }

    /**
     * Calculate the geodetic curve between two points on a specified reference
     * ellipsoid. This is the solution to the inverse geodetic problem.
     * 
     * @param ellipsoid reference ellipsoid to use
     * @param start starting coordinates
     * @param end ending coordinates
     * @return
     */
    //   public GeodeticCurve calculateGeodeticCurve(Ellipsoid ellipsoid, CoordinatePoint start, CoordinatePoint end)
    static public double calculateDistanceAndBearing(Ellipsoid ellipsoid, CoordinatePoint start, CoordinatePoint end, double[] bearing) {
	//
	// All equation numbers refer back to Vincenty's publication:
	// See http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
	//

	// get constants
	double a = ellipsoid.getSemiMajorAxis();
	double b = ellipsoid.getSemiMinorAxis();
	double f = ellipsoid.getFlattening();

	// get parameters as radians
	double phi1 = start.latDec * PiOver180;
	double lambda1 = start.lonDec * PiOver180;
	double phi2 = end.latDec * PiOver180;
	double lambda2 = end.lonDec * PiOver180;

	// calculations
	double a2 = a * a;
	double b2 = b * b;
	double a2b2b2 = (a2 - b2) / b2;

	double omega = lambda2 - lambda1;

	double tanphi1 = Math.tan(phi1);
	double tanU1 = (1.0 - f) * tanphi1;
	double U1 = Math.atan(tanU1);
	double sinU1 = Math.sin(U1);
	double cosU1 = Math.cos(U1);

	double tanphi2 = Math.tan(phi2);
	double tanU2 = (1.0 - f) * tanphi2;
	double U2 = Math.atan(tanU2);
	double sinU2 = Math.sin(U2);
	double cosU2 = Math.cos(U2);

	double sinU1sinU2 = sinU1 * sinU2;
	double cosU1sinU2 = cosU1 * sinU2;
	double sinU1cosU2 = sinU1 * cosU2;
	double cosU1cosU2 = cosU1 * cosU2;

	// eq. 13
	double lambda = omega;

	// intermediates we'll need to compute 's'
	double A = 0.0;
	double B = 0.0;
	double sigma = 0.0;
	double deltasigma = 0.0;
	double lambda0;
	boolean converged = false;

	for (int i = 0; i < 20; i++) {
	    lambda0 = lambda;

	    double sinlambda = Math.sin(lambda);
	    double coslambda = Math.cos(lambda);

	    // eq. 14
	    double sin2sigma = (cosU2 * sinlambda * cosU2 * sinlambda) + (cosU1sinU2 - sinU1cosU2 * coslambda) * (cosU1sinU2 - sinU1cosU2 * coslambda);
	    double sinsigma = Math.sqrt(sin2sigma);

	    // eq. 15
	    double cossigma = sinU1sinU2 + (cosU1cosU2 * coslambda);

	    // eq. 16
	    sigma = Math.atan2(sinsigma, cossigma);

	    // eq. 17 Careful! sin2sigma might be almost 0!
	    double sinalpha = (sin2sigma == 0) ? 0.0 : cosU1cosU2 * sinlambda / sinsigma;
	    double alpha = Math.asin(sinalpha);
	    double cosalpha = Math.cos(alpha);
	    double cos2alpha = cosalpha * cosalpha;

	    // eq. 18 Careful! cos2alpha might be almost 0!
	    double cos2sigmam = cos2alpha == 0.0 ? 0.0 : cossigma - 2 * sinU1sinU2 / cos2alpha;
	    double u2 = cos2alpha * a2b2b2;

	    double cos2sigmam2 = cos2sigmam * cos2sigmam;

	    // eq. 3
	    A = 1.0 + u2 / 16384 * (4096 + u2 * (-768 + u2 * (320 - 175 * u2)));

	    // eq. 4
	    B = u2 / 1024 * (256 + u2 * (-128 + u2 * (74 - 47 * u2)));

	    // eq. 6
	    deltasigma = B * sinsigma * (cos2sigmam + B / 4 * (cossigma * (-1 + 2 * cos2sigmam2) - B / 6 * cos2sigmam * (-3 + 4 * sin2sigma) * (-3 + 4 * cos2sigmam2)));

	    // eq. 10
	    double C = f / 16 * cos2alpha * (4 + f * (4 - 3 * cos2alpha));

	    // eq. 11 (modified)
	    lambda = omega + (1 - C) * f * sinalpha * (sigma + C * sinsigma * (cos2sigmam + C * cossigma * (-1 + 2 * cos2sigmam2)));

	    // see how much improvement we got
	    // modification by MiK77: check if lambda is zero to avoid division by zero. Use lambda0 for division in this case.
	    double change = 1.0;
	    if (lambda != 0) {
		change = Math.abs((lambda - lambda0) / lambda);
	    } else if (lambda0 != 0) {
		change = Math.abs((lambda - lambda0) / lambda0);
	    } else {
		change = 0.0;
	    }

	    if ((i > 1) && (change < 0.0000000000001)) {
		converged = true;
		break;
	    }
	}

	// eq. 19
	double s = b * A * (sigma - deltasigma);
	double alpha1;
	double alpha2;

	// didn't converge? must be N/S
	if (!converged) {
	    if (phi1 > phi2) {
		alpha1 = 180.0;
		alpha2 = 0.0;
	    } else if (phi1 < phi2) {
		alpha1 = 0.0;
		alpha2 = 180.0;
	    } else {
		alpha1 = Double.NaN;
		alpha2 = Double.NaN;
	    }
	}

	// else, it converged, so do the math
	else {
	    double radians;

	    // eq. 20
	    radians = Math.atan2(cosU2 * Math.sin(lambda), (cosU1sinU2 - sinU1cosU2 * Math.cos(lambda)));
	    if (radians < 0.0)
		radians += TwoPi;
	    alpha1 = radians / PiOver180;

	    // eq. 21
	    radians = Math.atan2(cosU1 * Math.sin(lambda), (-sinU1cosU2 + cosU1sinU2 * Math.cos(lambda))) + Math.PI;
	    if (radians < 0.0)
		radians += TwoPi;
	    alpha2 = radians / PiOver180;
	}

	if (alpha1 >= 360.0)
	    alpha1 -= 360.0;
	if (alpha2 >= 360.0)
	    alpha2 -= 360.0;

	if ((bearing != null) && (bearing.length > 0)) {
	    bearing[0] = alpha1;
	}

	//      return new GeodeticCurve(s, alpha1, alpha2);
	return s;
    }

    static public double calculateDistance(Ellipsoid ellipsoid, CoordinatePoint start, CoordinatePoint end) {
	return calculateDistanceAndBearing(ellipsoid, start, end, null);
    }

    static public double calculateBearing(Ellipsoid ellipsoid, CoordinatePoint start, CoordinatePoint end) {
	double bearing[] = { 0.0 };
	calculateDistanceAndBearing(ellipsoid, start, end, bearing);
	return bearing[0];
    }

}