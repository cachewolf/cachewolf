﻿    /*
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
package CacheWolf;

import CacheWolf.navi.TransformCoordinates;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.RandomAccessFile;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.sys.mThread;
 

public class Test extends mThread{
	boolean allPassed=true; 
	public static void main(String args[]) {
		new Test().start();
	}

	public void run() {
		testAll();
	}
	void testAll(){
		testPerformance();
	}
	void testPerformance(){
		Time start, end;
		int i;
		// 100.000 Sinus
		start = new Time();
		for (i=0; i<100000; i++){
			Math.sin(53);
		}
		end = new Time();
		printResult("sin(53)", start, end, i);
		
		// 1.000 CWPoint via constructor
		start = new Time();
		for (i=0; i<100; i++){
			new CWPoint("N 51° 27.635 E 009° 37.621", TransformCoordinates.CW);
		}
		end = new Time();
		printResult("CWPoint(\"N 51° 27.635 E 009° 37.621\", CWPoint.CW)", start, end, i);

		// 1.000 CWPoint via constructor ohne Lat/Lon
		start = new Time();
		for (i=0; i<100; i++){
			CWPoint cwP = new CWPoint();
			cwP.set("N 51° 27.635 E 009° 37.621", TransformCoordinates.CW);
			
		}
		end = new Time();
		printResult("cwp = new CWPoint(); cwp.set(\"N 51 27.635 E 009 37.621\", CWPoint.CW); ", start, end, i);

		// 1.000 CWPoint via constructor ohne Lat/Lon
		start = new Time();
		CWPoint[] a = new CWPoint[10000];
		for (i=0; i<10000; i++){
			CWPoint cwP = new CWPoint(20, 20);
			cwP.latDec = 41.123;
			cwP.lonDec = 9.2388;
			a[i] = cwP;
		}
		end = new Time();
		printResult("cwp = new CWPoint(); cwP.latDec = 41.123; cwP.lonDec = 9.2388;", start, end, i);

		// 1.000 CWPoint via set
		start = new Time();
		CWPoint cwSet = new CWPoint();
		for (i=0; i<100; i++){
			cwSet.set("N 51° 27.635 E 009° 37.621", TransformCoordinates.CW);
		}
		end = new Time();
		printResult("cwSet.set(\"N 51° 27.635 E 009° 37.621\", CWPoint.CW) CWPoint set", start, end, i);

		// 1.000 filewrite
		String fileName = new String("test.tmp");
		try {
			RandomAccessFile out =  new RandomAccessFile(fileName,"rw");
			start = new Time();
			for (i=0; i<10000; i++){
				out.writeBytes(fileName);
			}
			end = new Time();
			out.close();
			File dfile = new File(fileName);
			dfile.delete();
		} catch (IOException e) {
			Global.getPref().log("",e,true);
		}
		printResult("Filewrite 10 Bytes", start, end, i);
		
	}
	
	void printResult(String what, Time start, Time end, int count){
		String VM = Vm.getPlatform();
		long time;
		time = end.getTime() - start.getTime();
		
		Global.getPref().log(VM + " " +  Convert.toString(time) + " msec " + Convert.toString(count) + " * " + what);
	}
	
	void testPassedRegex(String pattern, String expectedResult, int format) {
		CWPoint coord = new CWPoint(pattern, TransformCoordinates.REGEX);
		String res = coord.toString(format);

		if (expectedResult.equals(res)) return; 

		Global.getPref().log("Failed test Regex: "+pattern+" Expected="+expectedResult+" Actual="+res); 
		allPassed=false; 
	}
	
	void testRegex(){
		testPassedRegex("S1 2.3W4 5.6","S 01° 02.300 W 004° 05.600", TransformCoordinates.CW); 
		testPassedRegex("N1 12.123 E7 34.345","N 01° 12.123 E 007° 34.345", TransformCoordinates.CW);
		testPassedRegex("S1 12.123 E7 34.345","S 01° 12.123 E 007° 34.345", TransformCoordinates.CW); 
		testPassedRegex("N1 12.123 W7 34.345","N 01° 12.123 W 007° 34.345", TransformCoordinates.CW); 
		testPassedRegex("S1 12.123 W7 34.345","S 01° 12.123 W 007° 34.345", TransformCoordinates.CW); 
		testPassedRegex("N1 12.123 E7 34,345","N 01° 12.123 E 007° 34.345", TransformCoordinates.CW); 
		testPassedRegex("n1 1.0 E12 34.3456363","N 01° 01.000 E 012° 34.346", TransformCoordinates.CW); //Kleinbuchstaben erlaubt 
		testPassedRegex("S99 60.2345 W180 65.34534","S 90° 00.000 W 180° 00.000", TransformCoordinates.CW); 
		testPassedRegex("N1 12.1234 O12 34.3456","N 01° 12.123 E 012° 34.346", TransformCoordinates.CW); 
		testPassedRegex("N1 12.1234E12 34.3456","N 01° 12.123 E 012° 34.346", TransformCoordinates.CW);
		testPassedRegex("asdfka N1° 12.1234 E12°34.345 sdfskd ","N 01° 12.123 E 012° 34.345", TransformCoordinates.CW);
		testPassedRegex("N1° 12.1234 E12°34.345","N 01° 12.123 E 012° 34.345", TransformCoordinates.CW); 
		testPassedRegex("N1° 12.1234E12°34.345","N 01° 12.123 E 012° 34.345", TransformCoordinates.CW); 
		testPassedRegex(" S17° 23 13.12345 w 127° 34 34.567 ","S 17° 23' 13.1\" W 127° 34' 34.6\"", TransformCoordinates.DMS); // Deg Min Sek
		testPassedRegex(" S17° 23 ' 13.12345    \" w 127° 34'34.567\" ","S 17° 23' 13.1\" W 127° 34' 34.6\"", TransformCoordinates.DMS); // Deg Min Sek
		testPassedRegex("12.3456 23.4567","N 12.34560° E 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("12.3456° 23.4567°","N 12.34560° E 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("12.3456° 23.4567 °","N 12.34560° E 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("12.3456°23.4567 °","N 12.34560° E 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("12.3456+23.4567 °","N 12.34560° E 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("N  12.3456   W  23.4567 °","N 12.34560° W 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("12.3457-23.4567 °","N 12.34570° W 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("-12.3456 23.4567","S 12.34560° E 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("12.3456 -23.4567","N 12.34560° W 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("-12.345668 -23,456734","S 12.34567° W 023.45673°", TransformCoordinates.DD); 
		testPassedRegex("12.3456-23.4567","N 12.34560° W 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("12.3456+23.4567","N 12.34560° E 023.45670°", TransformCoordinates.DD); 
		testPassedRegex("91.2345 180.23456","N 90.00000° E 180.00000°", TransformCoordinates.DD); 
		testPassedRegex("91.2345 180.23456","N 90.00000° E 180.00000°", TransformCoordinates.DD); 
		testPassedRegex("32U 475592 5584875","32U E 475592 N 5584875", TransformCoordinates.UTM); 
		testPassedRegex("14X 1 2","13N E 668186 N 2", TransformCoordinates.UTM); 
	}
}
