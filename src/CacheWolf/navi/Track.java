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

import CacheWolf.utils.MyLocale;
import CacheWolf.controls.InfoBox;
import CacheWolf.database.CoordinatePoint;
import ewe.fx.Color;
import ewe.io.BufferedWriter;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.PrintWriter;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.util.Utils;

public class Track {
    public Color trackColor;
    private CoordinatePoint trackPoints[];
    private int index;
    private int num;

    public Track(Color f, int trackSize) {
	trackColor = f;
	trackPoints = new CoordinatePoint[trackSize];
	num = 0;
	index = 0;
    }

    public Track(Color f) {
	this(f, 5000);
    }

    public Track() {
	this(new Color(0xff, 0, 0));
    }

    public void add(double lat, double lon) {
	add(new CoordinatePoint(lat, lon));
    }

    /**
     */
    public void add(CoordinatePoint t) {
	if (t == null)
	    return;

	trackPoints[index] = new CoordinatePoint(t);
	index++;
	if (index >= trackPoints.length)
	    index = 0;
	num++;
	if (num >= trackPoints.length)
	    num = trackPoints.length;
    }

    /**
     * returns number of Point in this track
     */
    public int size() {
	return num;
    }

    /**
     * returns the i.th point in this track
     */
    public CoordinatePoint get(int i) {
	//The array has never been filled, so first position is 0:
	if (num < trackPoints.length)
	    return trackPoints[i];
	//Once filled, the least inserted position is marked by index
	int tmpIndex = (index + i) % trackPoints.length;
	return trackPoints[tmpIndex];
    }

    public void loadTrack(String filename) { // TODO untested!
	byte[] all = Vm.readResource(null, filename);
	if (all == null)
	    return; // TODO error handling
	int numOfPoints = Utils.readInt(all, 0, 4);
	for (int i = 0; i <= numOfPoints; i++) {
	    CoordinatePoint point = new CoordinatePoint();
	    point.latDec = Double.longBitsToDouble(Utils.readLong(all, (i * 2) * 8 + 4));
	    point.lonDec = Double.longBitsToDouble(Utils.readLong(all, (i * 2 + 1) * 8 + 4));
	    add(point);
	}
    }

    public void saveTrack(String filename) { // TODO untested!
	byte[] ba = new byte[8 * 2 * size() + 4]; // 8 bytes is one double int has size 4
	Utils.writeInt(size(), ba, 0, 4);
	for (int i = 0; i <= size(); i++) {
	    Utils.writeLong(Double.doubleToLongBits(get(i).latDec), ba, (i * 2) * 8 + 4);
	    Utils.writeLong(Double.doubleToLongBits(get(i).lonDec), ba, (i * 2 + 1) * 8 + 4);
	}
	try {
	    PrintWriter outp = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	    outp.print(ba.toString());
	} catch (IOException e) {
	    new InfoBox(MyLocale.getMsg(5500, "Error"), "Error reading trackfile:\n" + e.toString()).wait(FormBase.OKB);
	}
    }
}
