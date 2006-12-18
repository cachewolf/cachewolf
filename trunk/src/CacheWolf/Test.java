package CacheWolf;

import ewe.sys.Vm;

public class Test {
	boolean allPassed=true; 

	void testAll(){
		testRegex();
		if (allPassed) 
			Vm.debug("SUCCESS: All tests passed"); 
		else 
			Vm.debug("FAILURE: At least one test failed"); 
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
		testPassedRegex("S99 60.2345 W180 65.34534","S ##° 00.234 W 181° 05.345", CWPoint.CW); 
		testPassedRegex("N1 12.1234 O12 34.3456","N 01° 12.123 E 012° 34.346", CWPoint.CW); 
		testPassedRegex("N1 12.1234E12 34.3456","N 01° 12.123 E 012° 34.346", CWPoint.CW);
		testPassedRegex("asdfka N1° 12.1234 E12°34.345 sdfskd ","N 01° 12.123 E 012° 34.345", CWPoint.CW);
		testPassedRegex("N1° 12.1234 E12°34.345","N 01° 12.123 E 012° 34.345", CWPoint.CW); 
		testPassedRegex("N1° 12.1234E12°34.345","N 01° 12.123 E 012° 34.345", CWPoint.CW); 
		testPassedRegex(" S17° 23 13.12345 w 127° 34 34.567 ","S 17° 23' 13.1\" W 127° 34' 34.6\"", CWPoint.DMS); // Deg Min Sek
		testPassedRegex("12.3456 23.4567","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456° 23.4567°","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456° 23.4567 °","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456°23.4567 °","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456+23.4567 °","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3457-23.4567 °","N 12.34570° W -023.45670°", CWPoint.DD); 
		testPassedRegex("-12.3456 23.4567","S -12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456 -23.4567","N 12.34560° W -023.45670°", CWPoint.DD); 
		testPassedRegex("-12.345668 -23,456734","S -12.34567° W -023.45673°", CWPoint.DD); 
		testPassedRegex("12.3456-23.4567","N 12.34560° W -023.45670°", CWPoint.DD); 
		testPassedRegex("12.3456+23.4567","N 12.34560° E 023.45670°", CWPoint.DD); 
		testPassedRegex("91.2345 180.23456","N 91.23450° E 180.23456°", CWPoint.DD); 
		testPassedRegex("91.2345 180.23456","N 91.23450° E 180.23456°", CWPoint.DD); 
		testPassedRegex("32U 475592 5584875","32U E 475592 N 5584875", CWPoint.UTM); 
		testPassedRegex("14X 1 2","13N E 668186 N 2", CWPoint.UTM); 
	}
}
