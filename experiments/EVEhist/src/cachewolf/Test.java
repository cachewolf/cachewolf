package cachewolf;

import eve.sys.*;
import com.stevesoft.eve_pat.*;
import java.lang.Math;;
 

public class Test extends mThread{
	boolean allPassed=true; 
	public static void main(String args[]) {
		new Test().start();
	}

	public void run() {
		testAll();
	}
	void testAll(){
//		testPerformance();
	    Regex r = new Regex("x\\s*(a|b)y");
	    r.search("abcx   ay");
	    eve.sys.Vm.debug("sub = "+r.stringMatched(1));
		
		Regex rex = new Regex("(?:" +
		"([NSns])\\s*([0-9]{1,2})(?:[°\uC2B0]\\s*|\\s+[°\uC2B0]?\\s*)([0-9]{1,2})(?:(?:['’]\\s*|\\s+['’]?\\s*)([0-9]{1,2}))?(?:[,.]([0-9]{1,8}))?\\s*['’\"]?\\s*" +
		"([EWewOo])\\s*([0-9]{1,3})(?:[°\uC2B0]\\s*|\\s+[°\uC2B0]?\\s*)([0-9]{1,2})(?:(?:['’]\\s*|\\s+['’]?\\s*)([0-9]{1,2}))?(?:[,.]([0-9]{1,8}))?\\s*['’\"]?" +
		")|(?:" +
		"(?:([NnSs])\\s*(?![+-]))?"   +     "([+-]?[0-9]{1,2})[,.]([0-9]{1,8})(?:(?=[+-EeWwOo])|\\s+|\\s*[°\uC2B0]\\s*)" +
	  	"(?:([EeWwOo])\\s*(?![+-]))?"    +     "([+-]?[0-9]{1,3})[,.]([0-9]{1,8})\\s*[°\uC2B0]?" +
		")|(?:" +
		"([0-9]{1,2}[C-HJ-PQ-X])\\s*[EeOo]?\\s*([0-9]{1,7})\\s+[Nn]?\\s*([0-9]{1,7})" +
		")|(?:" +
		"[Rr]:?\\s*([+-]?[0-9]{1,7})\\s+[Hh]:?\\s*([+-]?[0-9]{1,7})" +
		")");		
	    rex.search("N 23° 12.1234 E 23° 45.234");
	    eve.sys.Vm.debug("rex = "+rex.stringMatched(1)+" "+rex.stringMatched(2)+" "+rex.stringMatched(3)+" "+rex.stringMatched(4)+
	    		" "+rex.stringMatched(5)+" "+rex.stringMatched(6)+" "+rex.stringMatched(7)+" "+rex.stringMatched(8)+" "+rex.stringMatched(9)+
	    		" "+rex.stringMatched(10)
	    );
	    
		//testRegex();
		if (allPassed) 
			Vm.debug("SUCCESS: All tests passed"); 
		else  
			Vm.debug("FAILURE: At least one test failed"); 
	}
	void testPerformance(){
		Time start, end;
		int i;
		double tmp=0.0;
		
		// 100.000 Sinus
		start = new Time();
		for (i=0; i<100000; i++){
			tmp = Math.sin(53);
		}
		tmp=tmp+0;
		end = new Time();
		printResult("sin(53)", start, end, i);
		
		// 1.000 CWPoint via constructor
		start = new Time();
		for (i=0; i<100; i++){
			new CWPoint("N 51° 27.635 E 009° 37.621", CWPoint.CW);
		}
		end = new Time();
		printResult("CWPoint(\"N 51° 27.635 E 009° 37.621\", CWPoint.CW)", start, end, i);

		// 1.000 CWPoint via constructor ohne Lat/Lon
		start = new Time();
		for (i=0; i<100; i++){
			CWPoint cwP = new CWPoint();
			cwP.set("N 51° 27.635 E 009° 37.621", CWPoint.CW);
			
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
			cwSet.set("N 51° 27.635 E 009° 37.621", CWPoint.CW);
		}
		end = new Time();
		printResult("cwSet.set(\"N 51° 27.635 E 009° 37.621\", CWPoint.CW) CWPoint set", start, end, i);

	}
	
	void printResult(String what, Time start, Time end, int count){
		long time;
		time = end.getTime() - start.getTime();
		
		Vm.debug(" " +  Convert.toString(time) + " msec " + Convert.toString(count) + " * " + what);
	}
	
	void testPassedRegex(String pattern, String expectedResult, int format) {
		CWPoint coord = new CWPoint(pattern, CWPoint.REGEX);
		String res = coord.toString(format);

		if (expectedResult.equals(res)) return; 

		Vm.debug("Failed test Regex: "+pattern+" Expected="+expectedResult+" Actual="+res); 
		allPassed=false; 
	}
	
	void testRegex(){
		testPassedRegex("S1 2.3W4 5.6","S 01° 02.300 W 004° 05.600", CWPoint.CW); 
		testPassedRegex("N1 12.123 E7 34.345","N 01° 12.123 E 007° 34.345", CWPoint.CW);
		testPassedRegex("S1 12.123 E7 34.345","S 01° 12.123 E 007° 34.345", CWPoint.CW); 
		testPassedRegex("N1 12.123 W7 34.345","N 01° 12.123 W 007° 34.345", CWPoint.CW); 
		testPassedRegex("S1 12.123 W7 34.345","S 01° 12.123 W 007° 34.345", CWPoint.CW); 
		testPassedRegex("N1 12.123 E7 34,345","N 01° 12.123 E 007° 34.345", CWPoint.CW); 
		testPassedRegex("n1 1.0 E12 34.3456363","N 01° 01.000 E 012° 34.346", CWPoint.CW); //Kleinbuchstaben erlaubt 
		testPassedRegex("S99 60.2345 W180 65.34534","S 90° 00.000 W 180° 00.000", CWPoint.CW); 
		testPassedRegex("N1 12.1234 O12 34.3456","N 01° 12.123 E 012° 34.346", CWPoint.CW); 
		testPassedRegex("N1 12.1234E12 34.3456","N 01° 12.123 E 012° 34.346", CWPoint.CW);
		testPassedRegex("asdfka N1° 12.1234 E12°34.345 sdfskd ","N 01° 12.123 E 012° 34.345", CWPoint.CW);
		testPassedRegex("N1° 12.1234 E12°34.345","N 01° 12.123 E 012° 34.345", CWPoint.CW); 
		testPassedRegex("N1° 12.1234E12°34.345","N 01° 12.123 E 012° 34.345", CWPoint.CW); 
		testPassedRegex(" S17° 23 13.12345 w 127° 34 34.567 ","S 17° 23' 13.1\" W 127° 34' 34.6\"", CWPoint.DMS); // Deg Min Sek
		testPassedRegex(" S17° 23 ' 13.12345    \" w 127° 34'34.567\" ","S 17° 23' 13.1\" W 127° 34' 34.6\"", CWPoint.DMS); // Deg Min Sek
		testPassedRegex("12.3456 23.4567","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456° 23.4567°","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456° 23.4567 °","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456°23.4567 °","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456+23.4567 °","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("N  12.3456   W  23.4567 °","N 12.34560° W 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3457-23.4567 °","N 12.34570° W 023.45670°", CWPoint.DD); 
		testPassedRegex("-12.3456 23.4567","S 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456 -23.4567","N 12.34560° W 023.45670°", CWPoint.DD); 
		testPassedRegex("-12.345668 -23,456734","S 12.34567° W 023.45673°", CWPoint.DD); 
		testPassedRegex("12.3456-23.4567","N 12.34560° W 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456+23.4567","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("91.2345 180.23456","N 90.00000° E 180.00000°", CWPoint.DD); 
		testPassedRegex("91.2345 180.23456","N 90.00000° E 180.00000°", CWPoint.DD); 
		testPassedRegex("32U 475592 5584875","32U E 475592 N 5584875", CWPoint.UTM); 
		testPassedRegex("14X 1 2","13N E 668186 N 2", CWPoint.UTM); 
	}
}
