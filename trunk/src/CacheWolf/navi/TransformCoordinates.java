package CacheWolf.navi;

import CacheWolf.CWPoint;
import CacheWolf.Matrix;

import java.lang.Math;

public class TransformCoordinates {
//	public Ellipsoid ellipsoid;
//	public TransformParameters transParams;
	
	static final Ellipsoid Bessel = new Ellipsoid(6377397.155, 6356078.962);
	static final Ellipsoid WGS84 = new Ellipsoid(6378137.000, 6356752.314);
	static final TransformParameters GKToWGS84 = new TransformParameters(584.8, 67.0, 400.3, -0.105 * Math.PI/180/3600, -0.013 * Math.PI/180/3600, 2.378 * Math.PI/180/3600, 1/(1 - 10.290 * Math.pow(10, -6)) );
	static final TransformParameters WGS84ToGK = new TransformParameters(-584.8, -67.0, -400.3, 0.105 * Math.PI/180/3600, 0.013 * Math.PI/180/3600, -2.378 * Math.PI/180/3600, 1- 10.290 * Math.pow(10, -6) );
	
	public static GKPoint WGS84ToGK(CWPoint ll) {
		XyzCoordinates wgsxyz = latLon2xyz(ll, 0, WGS84);
		XyzCoordinates gkxyz = transform(wgsxyz, WGS84ToGK);
		CWPoint gkll = Xyz2Latlon(gkxyz, Bessel);
		return projectLatlon2GK(gkll, Bessel);
	}
	
	public static XyzCoordinates latLon2xyz(CWPoint ll, double alt, Ellipsoid ellipsoid) {
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		double N = ellipsoid.a/ Math.sqrt(1 - e2 * Math.pow(Math.sin(ll.latDec / 180*Math.PI), 2));
		XyzCoordinates ret = new XyzCoordinates(0,0,0);
		ret.x = (N+alt) * Math.cos(ll.latDec /180*Math.PI) * Math.cos(ll.lonDec /180*Math.PI);
		ret.y = (N+alt) * Math.cos(ll.latDec /180*Math.PI) * Math.sin(ll.lonDec /180*Math.PI);
		ret.z = (N * Math.pow(ellipsoid.b, 2) / Math.pow(ellipsoid.a , 2) + alt) * Math.sin(ll.latDec /180*Math.PI);
		return ret;
	}
	
	public static XyzCoordinates transform(XyzCoordinates from, TransformParameters transParams) {
		Matrix coos = new Matrix(3, 1);
		coos.matrix[0][0] = from.x;
		coos.matrix[1][0] = from.y;
		coos.matrix[2][0] = from.z;

		Matrix shift = new Matrix(3,1);
		shift.matrix[0][0] = transParams.dx;
		shift.matrix[1][0] = transParams.dy;
		shift.matrix[2][0] = transParams.dz;
		
		coos.add(shift);
		
		Matrix rotate = new Matrix(3,3);
		rotate.matrix[0][0] = 1;
		rotate.matrix[1][1] = 1;
		rotate.matrix[2][2] = 1;
		rotate.matrix[0][1] = transParams.ez; 
		rotate.matrix[0][2] = - transParams.ey;
		rotate.matrix[1][0] = - rotate.matrix[0][1];
		rotate.matrix[1][2] = transParams.ex;
		rotate.matrix[2][0] = - rotate.matrix[0][2];
		rotate.matrix[2][1] = - rotate.matrix[1][2];
		
		rotate.Multiply(coos);
		coos = rotate;
		coos.MultiplyByScalar(transParams.s); // scale
		
		return new XyzCoordinates(coos.matrix[0][0], coos.matrix[1][0], coos.matrix[2][0]);
	}
	
	public static CWPoint Xyz2Latlon(XyzCoordinates from, Ellipsoid ellipsoid) {
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		double s = Math.sqrt( Math.pow(from.x,2) + Math.pow(from.y,2));
		double T = Math.atan( from.z * ellipsoid.a / (s * ellipsoid.b));
		double B = Math.atan( (from.z + e2 * Math.pow(ellipsoid.a, 2) / ellipsoid.b * Math.pow(Math.sin(T), 3) )/(s - e2 * ellipsoid.a * Math.pow(Math.cos(T),3)));
		double L = Math.atan(from.y / from.x);
		double N = ellipsoid.a / Math.sqrt(1 - e2 * Math.pow(Math.sin(B),2));
		double h = s / Math.cos(B)- N;
		CWPoint ret = new CWPoint();
		ret.latDec = B * 180/Math.PI;
		ret.lonDec = L * 180/Math.PI;
		//ret.alt = h;
		return ret;
	}
	
	public static GKPoint projectLatlon2GK(CWPoint ll, Ellipsoid ellipsoid) {
		int stripe;
		for (stripe=0; stripe <= 360; stripe += 3) { // TODO -180 bis +180
			if (Math.abs(ll.lonDec - stripe) < 1.5) break;
		}
		double e2 = (ellipsoid.a * ellipsoid.a - ellipsoid.b * ellipsoid.b)/(ellipsoid.a * ellipsoid.a);
		double l = (ll.lonDec - stripe) /180*Math.PI; // TODO see is int to double works
		double B = ll.latDec /180*Math.PI;
		double N = ellipsoid.a/ Math.sqrt(1- e2 * Math.pow(Math.sin(B),2));
		double nue = Math.sqrt(Math.pow(ellipsoid.a, 2) / Math.pow(ellipsoid.b, 2)* e2 * Math.pow(Math.cos(B), 2));
		double t = Math.tan(B);
		
		double n1 = (ellipsoid.a-ellipsoid.b)/(ellipsoid.a+ellipsoid.b);
		double n2 = (ellipsoid.a+ellipsoid.b)/2 * (1+ Math.pow(n1, 2)/4 + Math.pow(n1, 4)/64);
		double n3 = n1 * -3/2 + Math.pow(n1, 3) * 9/16  - Math.pow(n1, 5) * 3/32;
		double n4 = Math.pow(n1, 2) * 15/16 - Math.pow(n1, 4) * 15/32;
		double n5 = Math.pow(n1, 3) * -35/48 + Math.pow(n1, 5) * 105/256;
		double n6 = Math.pow(n1, 4) * 315/512;
		double arclength = n2 * (B + n3 * Math.sin(B*2) + n4 * Math.sin(B*4) + n5 * Math.sin(B*6) + n6 * Math.sin(B*8));
		
		double h1 = t/2 * N * Math.pow(Math.cos(B), 2) * l*l;
		double h2 = t/24 * N * Math.pow(Math.cos(B),4) * (5 - t*t + 9 * nue*nue + 4*Math.pow(nue, 4)) * Math.pow(l,4);
		double northing = arclength + h1 + h2;
		
		
		double r1 = N * Math.cos(B) * l;
		double r2 = N/6 * Math.pow(Math.cos(B), 3) * (1-t*t+nue*nue)*l*l*l;
		double easting = r1 + r2 + stripe / 3 * 1000000 + 500000;
		GKPoint ret = new GKPoint();
		ret.easting = easting;
		ret.northing = northing;
		return ret;
	}
}

class XyzCoordinates {
	double x, y, z;
	public XyzCoordinates (double xi, double yi, double zi) {
		x = xi;
		y = yi;
		z = zi;
	}
}

class Ellipsoid {
	double a, b;
	public Ellipsoid(double ai, double bi) {
		a = ai;
		b = bi;
	}
}

class TransformParameters {
	// shift parameter
	double dx, dy, dz, 
	// rotation parameter in rad
	ex, ey, ez, 
	// scale as multiplicator
	s;
	
	public TransformParameters(double dxi, double dyi, double dzi, double exi, double eyi, double ezi, double si) {
		dx = dxi; dy = dyi; dz = dzi; ex = exi; ey =eyi; ez = ezi; s = si;
	}
}

class GKPoint {
	double northing;
	double easting;
	public String toString() {
		return Double.toString(easting) + ";" + Double.toString(northing);
	}
}
