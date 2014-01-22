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
/* 
 A parser that parses the following grammar:
 EBNF Meta-Symbols:
 {xx}        xx can occur any number of times incl 0
 [xx]        xx or empty
 |           or
 "x"         x is terminal symbol

 command -> if |
 simplecommand

 simplecommand -> "stop" | "st" |
 assign
 stringexp |

 if -> "IF" stringexpr compop stringexpr "THEN" simplecommand { ";" simplecommand } "ENDIF"          // Nested IF's not allowed

 compop -> "=" | "<" | ">" | "<=" | "==" | ">=" | "<>" | "!=" | "><"

 assign -> ident = [ stringexpr ]

 stringexp -> (string | expr ) {string | tailexp }

 expr -> ["+" | "-"] tailexp [ formatstring ]

 tailexp -> term { ("+" | "-") term }

 term -> factor { ("*" | "/") factor }

 factor -> expfactor { "^" expfactor }

 expfactor -> ident |
 number |
 "(" stringexpr ")" |
 function "(" stringexpr { "," stringexpr }")"

 function -> "sin" | "cos" | "tan" | "asin" | "acos" | "atan" | "goto" | "project" | "show"  | "crosstotal" |
 "rot13" | "len" | "mid"

 ident -> valid identifier
 number -> valid number


 */
package CacheWolf;

import CacheWolf.database.CWPoint;
import CacheWolf.database.CacheHolder;
import CacheWolf.database.CacheSize;
import CacheWolf.database.CacheTerrDiff;
import CacheWolf.database.CacheType;
import CacheWolf.navi.Metrics;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import CacheWolf.utils.MyLocale;
import CacheWolf.utils.STRreplace;

import com.stevesoft.ewe_pat.Regex;

import ewe.sys.Convert;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Vector;
import ewe.util.mString;

/**
 * The wolf language parser. New version - January 2007
 * 
 * New features:
 * - Improved error handling
 * - Strings and doubles can be freely mixed as appropriate. Depending on context a conversion is performed,
 * - Variables can store strings or doubles
 * - Global variables (starting with $) are remembered across multiple calls to parser
 * - Global variables are initialised with "", local variables result in error if used before setting value
 * - IF statement added
 * - Many new functions (encode,format,goto,len,mid,count, substring,ucase,lcase,val,sval,replace, reverse,project)
 * - less typing
 * - Function aliases
 * - Function names can be flexibly abbreviated, i.e. instead of crosstotal write cr or cross or crosst ...
 * - show no longer needed
 * - Command terminator ; no longer compulsory (only between multiple commands on same line)
 * - New functions can easily be added
 * - Can select whether variable names are case sensitive
 * 
 * To add a new function:
 * 1) Add its name and alias and allowed number of args to array functions
 * 2) Add a new private method in the "functions" section
 * 3) Add call to private method in executeFunction
 * 
 * @author salzkammergut Januay 2007
 */
public class Parser {

    private class fnType {
	public String funcName; // the function name in the user input
	public String alias; // the funcName is mapped to this alias
	public int nargs; // bitmap for number of args, i.e. 14 = 1 or 2 or 3 args; 5 = 0 or 2 args
			  // i.e. 1<<nargs ORed together

	fnType(String funcName, String alias, int nargs) {
	    this.funcName = funcName;
	    this.alias = alias;
	    this.nargs = nargs;
	}

	boolean nargsValid(int testNargs) {
	    return ((1 << testNargs) & this.nargs) != 0;
	}
    }

    fnType[] functions = new fnType[] { // in alphabetical order
    new fnType("abs", "abs", 2), new fnType("acos", "acos", 2), new fnType("asin", "asin", 2), new fnType("atan", "atan", 2), new fnType("bearing", "bearing", 4), new fnType("cb", "cb", 16), new fnType("centre", "center", 3),
	    new fnType("center", "center", 3), new fnType("cls", "cls", 1), new fnType("clearscreen", "cls", 1), new fnType("cos", "cos", 2), new fnType("count", "count", 4), new fnType("cp", "cp", 1), new fnType("crossbearing", "cb", 16),
	    new fnType("crosstotal", "ct", 6), new fnType("ct", "ct", 2), new fnType("curpos", "cp", 1), new fnType("d2r", "deg2rad", 2), new fnType("deg", "deg", 1), new fnType("deg2rad", "deg2rad", 2), new fnType("distance", "distance", 4),
	    new fnType("encode", "encode", 8), new fnType("format", "format", 12), new fnType("goto", "goto", 6), new fnType("ic", "ic", 3), new fnType("ignorecase", "ic", 3), new fnType("instr", "instr", 12), new fnType("int", "int", 2),
	    new fnType("lcase", "lc", 2), new fnType("length", "len", 2), new fnType("mid", "mid", 12), new fnType("mod", "mod", 4), new fnType("pc", "pz", 3), new fnType("profilecenter", "pz", 3), new fnType("profilecentre", "pz", 3),
	    new fnType("profilzentrum", "pz", 3), new fnType("project", "project", 8), new fnType("pz", "pz", 3), new fnType("quersumme", "ct", 6), new fnType("r2d", "rad2deg", 2), new fnType("rad", "rad", 1), new fnType("rad2deg", "rad2deg", 2),
	    new fnType("replace", "replace", 8), new fnType("reverse", "reverse", 2), new fnType("rot13", "rot13", 2), new fnType("show", "show", 2), new fnType("sin", "sin", 2), new fnType("skeleton", "skeleton", 3), new fnType("sqrt", "sqrt", 2),
	    new fnType("sval", "sval", 2), new fnType("tolowercase", "lc", 2), new fnType("touppercase", "uc", 2), new fnType("tan", "tan", 2), new fnType("ucase", "uc", 2), new fnType("val", "val", 2), new fnType("zentrum", "center", 3) };
    private static int scanpos = 0;
    CWPoint cwPt = new CWPoint();
    Vector calcStack = new Vector();
    Hashtable symbolTable = new Hashtable(50);
    TokenObj thisToken = new TokenObj();
    Vector tokenStack;
    Vector messageStack;

    public Parser() { // Global constructor
    }

    // /////////////////////////////////////////
    // Utility functions
    // /////////////////////////////////////////

    /*
     * All errors are handled via function 'err'. Rather than creating many different Exceptions,
     * only the standard Exception is used. err raises this exception and thereby causes the stack to be
     * unwound until 'parse' eventually catches the exception and returns to SolverPanel, which displays
     * the messageStack containing the error message.
     */

    /**
     * Add an error message to the message stack and raise an Exception.
     */
    private void err(String str) throws Exception {
	messageStack.add(MyLocale.getMsg(1700, "Error on line: ") + thisToken.line + "  " + MyLocale.getMsg(1701, "position: ") + thisToken.position);
	messageStack.add(str);
	// move cursor to error location		
	MainTab.itself.solverPanel.setSelectionRange(0, thisToken.line - 1, thisToken.position + thisToken.token.length() - 1, thisToken.line - 1);
	throw new Exception("Error " + str);
    }

    /** Shows global symbols */
    private void showVars(boolean globals) throws Exception {
	Iterator it = symbolTable.entries();
	while (it.hasNext()) {
	    String varName = ((String) ((ewe.util.Map.MapEntry) it.next()).getKey());
	    if (globals == varName.startsWith("$")) {
		String value = (String) getVariable(varName);
		if (java.lang.Double.isNaN(toNumber(value)))
		    messageStack.add(varName + " = \"" + STRreplace.replace(value.toString(), "\"", "\"\"") + "\"");
		else
		    messageStack.add(varName + " = " + value);
	    }
	}
    }

    /** Clears the symbol table of all non-global symbols (those not starting with $) */
    private void clearLocalSymbols() {
	Iterator it = symbolTable.entries();
	while (it.hasNext()) {
	    ewe.util.Map.MapEntry sym = (ewe.util.Map.MapEntry) it.next();
	    if (!((String) sym.getKey()).startsWith("$"))
		symbolTable.remove(sym.getKey());
	}
	Double pi = new Double(java.lang.Math.PI);
	symbolTable.put("PI", pi);
	symbolTable.put("pi", pi); // To make it easier for the user we also add a lowercase version of pi
    }

    private boolean isVariable(String varName) {
	return varName.startsWith("$") || // Global variables exist per default
		symbolTable.containsKey(Preferences.itself().solverIgnoreCase ? varName.toUpperCase() : varName);
    }

    private boolean isInteger(double d) {
	return java.lang.Math.ceil(d) == d && java.lang.Math.floor(d) == d;
    }

    private boolean isValidCoord(String coord) {
	cwPt.set(coord);
	return cwPt.isValid();
    }

    private Object getVariable(String varName) throws Exception {
	if (varName.startsWith("$")) { // Potential coordinate
	    CacheHolder ch = MainForm.profile.cacheDB.get(varName.substring(1));
	    if (ch != null) { // Found it!
		// Check whether coordinates are valid
		cwPt.set(ch.getPos());
		if (cwPt.isValid())
		    return cwPt.toString();
		else
		    return ""; // Convert invalid coordinates (N 0 0.0 E 0 0.0) into empty string
	    }
	}
	Object result = symbolTable.get(Preferences.itself().solverIgnoreCase ? varName.toUpperCase() : varName);
	if (result == null) {
	    // If it is a global variable, add it with a default value
	    if (varName.startsWith("$")) {
		result = "";
		symbolTable.put(Preferences.itself().solverIgnoreCase ? varName.toUpperCase() : varName, "");
	    } else
		err(MyLocale.getMsg(1702, "Variable not defined: ") + varName);
	}
	return result;
    }

    private double toNumber(String str) {
	try {
	    if (MyLocale.getDigSeparator().equals(","))
		str = str.replace('.', ',');
	    else
		str = str.replace(',', '.');
	    return java.lang.Double.parseDouble(str);
	} catch (NumberFormatException e) {
	    return java.lang.Double.NaN;
	}
    }

    private Double getNumber(String str) throws Exception {
	double ret = toNumber(str);
	if (java.lang.Double.isNaN(ret))
	    err(MyLocale.getMsg(1703, "Not a valid number: ") + str);
	return new java.lang.Double(ret);
    }

    /** Get the top element of the calculation stack and try and convert it to a number if it is a string */
    private double popCalcStackAsNumber(double defaultForEmptyString) throws Exception {
	double num;
	if (calcStack.get(calcStack.size() - 1) instanceof String) {
	    if (((String) calcStack.get(calcStack.size() - 1)).equals(""))
		num = defaultForEmptyString;
	    else
		num = getNumber((String) calcStack.get(calcStack.size() - 1)).doubleValue();
	} else {
	    num = ((java.lang.Double) calcStack.get(calcStack.size() - 1)).doubleValue();
	}
	calcStack.removeElementAt(calcStack.size() - 1);
	return num;
    }

    private String popCalcStackAsString() {
	String s;
	if (calcStack.get(calcStack.size() - 1) instanceof Double) {
	    java.lang.Double D = ((java.lang.Double) calcStack.get(calcStack.size() - 1));
	    // Double.toString() formats numbers > 1E7 and < 1E-3 with exponential notation
	    // For large integers we therefore use Longs
	    double d = D.doubleValue();
	    // If the double is an integer and within range of longs, use Long
	    if (java.lang.Math.floor(d) == d && d < java.lang.Long.MAX_VALUE && d > java.lang.Long.MIN_VALUE) {
		java.lang.Long L = new java.lang.Long((long) d);
		s = L.toString();
	    } else { // Use the default Double format
		s = D.toString().replace(',', '.'); // always show numbers with decimal point;
		if (s.endsWith(".0"))
		    s = s.substring(0, s.length() - 2);
	    }
	} else
	    s = (String) calcStack.get(calcStack.size() - 1);
	calcStack.removeElementAt(calcStack.size() - 1);
	return s;
    }

    private void getToken() throws Exception {
	if (scanpos < tokenStack.size()) {
	    thisToken = (TokenObj) tokenStack.get(scanpos);
	    scanpos++;
	} else
	    err(MyLocale.getMsg(1704, "Unexpected end of source"));
    }

    private TokenObj peekToken() {
	if (scanpos < tokenStack.size()) {
	    return (TokenObj) tokenStack.get(scanpos);
	} else
	    return new TokenObj();
    }

    private void getNextTokenOtherThanSemi() throws Exception {
	do {
	    getToken();
	} while (thisToken.token.equals(";"));
    }

    private void skipPastEndif(TokenObj ifToken) throws Exception {
	while (scanpos < tokenStack.size()) {
	    thisToken = (TokenObj) tokenStack.get(scanpos);
	    scanpos++;
	    if (thisToken.tt == TokenObj.TT_ENDIF) {
		getToken();
		return;
	    }
	}
	thisToken = ifToken;
	err(MyLocale.getMsg(1705, "Missing ENDIF"));
    }

    private TokenObj lookAheadToken() {
	return (TokenObj) tokenStack.get(scanpos);
    }

    private boolean checkNextSymIs(String str) throws Exception {
	if (thisToken.token.toUpperCase().equals(str)) {
	    return true;
	} else {
	    err(MyLocale.getMsg(1706, "Expected ") + str + "  " + MyLocale.getMsg(1707, "Found: ") + thisToken.token);
	    return false; // Dummy as err does not return
	}
    }

    private fnType getFunctionDefinition(String str) throws Exception {
	fnType fnd = null;
	str = str.toLowerCase();
	for (int i = functions.length - 1; i >= 0; i--) {
	    // Return the function if there is an exact match
	    if (functions[i].funcName.equals(str))
		return functions[i];
	    if (functions[i].funcName.startsWith(str)) { // Partial match?
		// Only one partial match allowed
		if (fnd != null)
		    err(MyLocale.getMsg(1708, "Ambiguous function name: ") + str);
		fnd = functions[i];
	    }
	}
	if (fnd == null)
	    err(MyLocale.getMsg(1709, "Unknown function: ") + str);
	return fnd;
    }

    // /////////////////////////////////////////
    // FUNCTIONS
    // /////////////////////////////////////////

    /** If we are in DEGree mode, convert the argument to RADiants, if not leave it unchanged */
    private double makeRadiant(double arg) {
	if (Preferences.itself().solverDegMode)
	    return arg * java.lang.Math.PI / 180.0;
	else
	    return arg;
    }

    /** If we are in DEGree mode, convert the argument to degrees */
    private double makeDegree(double arg) {
	if (Preferences.itself().solverDegMode)
	    return arg / java.lang.Math.PI * 180.0;
	else
	    return arg;
    }

    /** Calculate brearing from one point to the next */
    private double funcBearing() throws Exception {
	String coordB = popCalcStackAsString();
	String coordA = popCalcStackAsString();
	if (!isValidCoord(coordA))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coordA);
	if (!isValidCoord(coordB))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coordB);
	cwPt.set(coordA);
	double angleDeg = cwPt.getBearing(new CWPoint(coordB));
	// getBearing returns a result in degrees
	return Preferences.itself().solverDegMode ? angleDeg : angleDeg * java.lang.Math.PI / 180.0;
    }

    /** Get or set the current centre */
    private void funcCenter(int nargs) throws Exception {
	if (nargs == 0) {
	    calcStack.add(Preferences.itself().curCentrePt.toString());
	} else {
	    String coordA = popCalcStackAsString();
	    if (!isValidCoord(coordA))
		err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coordA);
	    MainForm.itself.setCurCentrePt(new CWPoint(coordA));
	}
    }

    /** Clear Screen */
    private void funcCls() {
	MainTab.itself.solverPanel.cls();
    }

    private int funcCountChar(String s, char c) {
	int count = 0;
	for (int i = 0; i < s.length(); i++)
	    if (s.charAt(i) == c)
		count++;
	return count;
    }

    /**
     * count(string1,string2)
     * */
    private void funcCount() throws Exception {
	String s2 = popCalcStackAsString();
	String s1 = popCalcStackAsString();
	if (s2.length() == 0)
	    err(MyLocale.getMsg(1710, "Cannot count empty string"));
	if (s2.length() == 1) {
	    calcStack.add(new Double(funcCountChar(s1, s2.charAt(0))));
	} else {
	    String res = "";
	    for (int i = 0; i < s2.length(); i++) {
		res += s2.charAt(i) + "=" + funcCountChar(s1, s2.charAt(i)) + " ";
	    }
	    calcStack.add(res);
	}
    }

    private String funcCp() {
	return Navigate.gpsPos.toString();
    }

    /**
     * Crosstotal: Works for both strings and numbers. For strings any non-numeric character is ignored
     * Warning: When the number is non-integer or > 9223372036854775807, it is formatted using the E
     * notation, i.e. x.xxxxxxEyy. In this case the exponent yy is also included in the crosstotal
     */
    private double funcCrossTotal(int nargs) throws Exception {
	int cycles = 1;
	if (nargs == 2)
	    cycles = (int) popCalcStackAsNumber(1);
	String aString = popCalcStackAsString().replace('-', '0').trim();
	double a = 0;
	if (cycles < 0)
	    cycles = 1;
	if (cycles > 5)
	    cycles = 5;
	while (cycles-- > 0) {
	    // Cross total = Quersumme berechnen
	    a = 0;
	    for (int i = 0; i < aString.length(); i++) {
		if (aString.charAt(i) >= '0' && aString.charAt(i) <= '9')
		    a += aString.charAt(i) - '0';
	    }
	    aString = Convert.toString(a);
	}
	return a;
    }

    private void funcDeg(boolean arg) {
	Preferences.itself().solverDegMode = arg;
	MainTab.itself.solverPanel.showSolverMode();
    }

    /** Convert degrees into Radiants */
    private double funcDeg2Rad() throws Exception {
	double a = popCalcStackAsNumber(0);
	return a / 180.0 * java.lang.Math.PI;
    }

    /** Calculate distance between 2 points */
    private double funcDistance() throws Exception {
	String coordB = popCalcStackAsString();
	String coordA = popCalcStackAsString();
	double result = 0;
	// Attention: isValidCoord has sideeffect of setting cwPt
	if (!isValidCoord(coordA))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coordA);
	if (!isValidCoord(coordB))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coordB);
	cwPt.set(coordA);
	double distKM = cwPt.getDistance(new CWPoint(coordB));
	result = distKM * 1000.0;
	if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
	    result = Metrics.convertUnit(distKM, Metrics.KILOMETER, Metrics.YARDS);
	}
	return result;
    }

    /**
     * Encode a string by replacing all characters in a string with their corresponding characters in
     * another string
     * 
     * @throws Exception
     */
    private String funcEncode() throws Exception {
	String newChars = popCalcStackAsString();
	String oldChars = popCalcStackAsString();
	if (newChars.length() != oldChars.length())
	    err(MyLocale.getMsg(1711, "Replacement characters strings must be of equal length"));
	String s = popCalcStackAsString();
	String encodedStr = "";
	for (int i = 0; i < s.length(); i++) {
	    int pos;
	    if ((pos = oldChars.indexOf(s.charAt(i))) != -1) {
		encodedStr += newChars.charAt(pos);
	    } else
		encodedStr += s.charAt(i);
	}
	return encodedStr;
    }

    /**
     * Format a valid coordinate
     * If called with one args, format the argument on the stack to CW standard
     * The optional second argument is one of these strings "UTM","DMS","DD","DMM" or "CW"
     * 
     * @param nargs
     *            1 or 2 args
     */
    private String funcFormat(int nargs) throws Exception {
	int spart = 0;
	if (nargs == 3)
	    spart = (int) popCalcStackAsNumber(0);
	String fmtStr = "";
	if (nargs >= 2)
	    fmtStr = popCalcStackAsString().toLowerCase();
	String coord = popCalcStackAsString();
	if (!isValidCoord(coord))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coord);
	cwPt.set(coord);
	int fmt = TransformCoordinates.getLocalSystemCode(fmtStr);
	if (fmt == TransformCoordinates.LOCALSYSTEM_NOT_SUPPORTED)
	    err(MyLocale.getMsg(1713, "Invalid coordinate format. Allowed are cw / dd / dmm / dms / ") + Common.arrayToString(TransformCoordinates.getProjectedSystemIDs(), " / "));
	String ret = cwPt.toString(fmt);
	if (nargs == 3) {
	    String[] parts = mString.split(ret, ' ');
	    if (spart > 0 && parts.length >= spart)
		ret = parts[spart - 1];
	    else
		err("Param 3 !!! " + MyLocale.getMsg(1713, "Invalid coordinate format."));
	}
	return ret;
    }

    /**
     * Implements a goto command goto(coordinate,optionalWaypointName).
     */
    private void funcGoto(int nargs) throws Exception {
	Navigate nav = MainTab.itself.navigate;
	String waypointName = null;
	if (nargs == 2)
	    waypointName = popCalcStackAsString();
	String coord = popCalcStackAsString();
	if (!isValidCoord(coord))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coord);
	// Don't want to switch to goto panel, just set the values
	nav.setDestination(coord);
	if (nargs == 2) { // Now set the value of the addi waypoint (it must exist already)
	    cwPt.set(coord);
	    CacheHolder ch = MainForm.profile.cacheDB.get(waypointName);
	    if (ch == null) {
		err(MyLocale.getMsg(1714, "Goto: Waypoint does not exist: ") + waypointName);
		return;
	    }
	    ch.setPos(cwPt);
	    ch.calcDistance(Preferences.itself().curCentrePt); // Update distance/bearing
	    nav.setDestination(ch);
	    MainForm.profile.selectionChanged = true; // Tell moving map to updated displayed waypoints
	}
    }

    /** Display or change the case sensitivity of variable names */
    private void funcIgnoreVariableCase(int nargs) throws Exception {
	if (nargs == 0)
	    calcStack.add("" + Preferences.itself().solverIgnoreCase);
	else {
	    Preferences.itself().solverIgnoreCase = (popCalcStackAsNumber(0) != 0) ? true : false;
	}
    }

    /**
     * VB instr function
     * instr([start],string1,string2)
     * */
    private int funcInstr(int nargs) throws Exception {
	String s2 = popCalcStackAsString();
	String s1 = popCalcStackAsString();
	int start = 1;
	if (nargs == 3)
	    start = (int) popCalcStackAsNumber(1);
	if (start > s1.length())
	    err(MyLocale.getMsg(1715, "instr: Start position not in string"));
	if (s2.equals("")) {
	    if (s1.equals(""))
		return 0;
	    else
		return 1;
	}
	return s1.indexOf(s2, start - 1) + 1;
    }

    /** MID function as in Basic */
    private String funcMid(int nargs) throws Exception {
	if (nargs == 2) {
	    double start = popCalcStackAsNumber(0);
	    String s = popCalcStackAsString();
	    if (!isInteger(start))
		err(MyLocale.getMsg(1716, "mid: Integer argument expected"));
	    if (start < 1 || start > s.length())
		err(MyLocale.getMsg(1717, "mid: Argument out of range"));
	    return s.substring((int) start - 1);
	} else {
	    double len = popCalcStackAsNumber(0);
	    double start = popCalcStackAsNumber(0);
	    String s = popCalcStackAsString();
	    if (!isInteger(start) || !isInteger(len))
		err(MyLocale.getMsg(1716, "mid: Integer argument expected"));
	    int end = (int) (start + len - 1);
	    if (start > s.length() || start < 1 || end > s.length())
		err(MyLocale.getMsg(1717, "mid: Argument out of range"));
	    return s.substring((int) start - 1, end);
	}
    }

    /** MOD function as in Basic */
    private Double funcMod() throws Exception {
	double b = popCalcStackAsNumber(0);
	double a = popCalcStackAsNumber(0);
	if (b == 0.0)
	    err(MyLocale.getMsg(1729, "Division by 0"));
	return new java.lang.Double(a % b);
    }

    /** Get or set the profile centre */
    private void funcPz(int nargs) throws Exception {
	if (nargs == 0) {
	    calcStack.add(MainForm.profile.centre.toString());
	} else {
	    String coordA = popCalcStackAsString();
	    if (!isValidCoord(coordA))
		err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coordA);
	    MainForm.profile.centre.set(coordA);
	}
    }

    /**
     * Calculates the crossbearing from point1 with bearing 1 and point2 with bearing2
     * point1 and point 2 must be different.
     * Not very well tested. No guarantee for correct result if any of the distance is greater than 300 kilometers and / or any of the angles in the spherical triangle id greater then 90degrees
     * 
     * @return
     * @throws Exception
     */
    private String funcCrossBearing() throws Exception {
	// parameters come in reversed order!
	double degrees2 = popCalcStackAsNumber(-1);
	String coordinates2 = popCalcStackAsString();
	double degrees1 = popCalcStackAsNumber(-1);
	String coordinates1 = popCalcStackAsString();
	if (!isValidCoord(coordinates1))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coordinates1);
	if (!isValidCoord(coordinates2))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coordinates2);

	// Check parameters: Range
	if (degrees1 < 0 || degrees1 > 360 || degrees2 < 0 || degrees2 > 360) {
	    if (Preferences.itself().solverDegMode) {
		err(MyLocale.getMsg(1740, "Crossbearing degrees must be in interval [0;360]"));
	    } else {
		err(MyLocale.getMsg(1741, "Crossbearing degrees must be in interval [0;2*PI]"));
	    }
	}
	double rAN = Preferences.itself().solverDegMode ? degrees1 / 180.0 * java.lang.Math.PI : degrees1;
	double rBN = Preferences.itself().solverDegMode ? degrees2 / 180.0 * java.lang.Math.PI : degrees2;

	CWPoint point1 = new CWPoint(coordinates1);
	CWPoint point2 = new CWPoint(coordinates2);

	// check Parameters: bearings to project must be different from the bearing between point1 and point2
	if (degrees1 == degrees2) {
	    double bearing1 = point1.getBearing(point2);
	    double bearing2 = point1.getBearing(point1);
	    if (bearing1 == degrees1 || bearing2 == degrees2) {
		err(MyLocale.getMsg(1740, "Invalid crossbearing angles"));
	    }
	}

	CWPoint result2 = crossbearingCalculation(point1, point2, rAN, rBN);
	return result2.toString();
    }

    private CWPoint crossbearingCalculation(CWPoint point1, CWPoint point2, double rAN, double rBN) throws Exception {
	// see german wikipedia keyword vorwaertsschnitt for the calculation.
	// peilung von a->b
	// Yes we will make an error, therefore we have to calculate the target-point iteratively.
	// Testcode for crossbearing:
	// MP="S35 47.100 W089 43.200" # MP is centre of circle, could be any waypoint
	// A=project(MP,0,1000); B=project(MP,120,1000) # Points of equilateral triangle on circle
	// C1=project(MP,240,1000); C2=cb(A,210 ,B,270)
	// C1 "=" C2
	final int maxRadius = 6378;
	double distance = point1.getDistance(point2);
	if (Math.abs(distance) <= 0.0000000001) {
	    err(MyLocale.getMsg(1742, "Crossbearing: distance between points to small"));
	}
	double distanceInRad = distance / maxRadius;
	double phiAB = point1.getBearing(point2);
	if (Preferences.itself().solverDegMode)
	    phiAB = phiAB / 180.0 * java.lang.Math.PI;
	double phiBA = point2.getBearing(point1);
	if (Preferences.itself().solverDegMode)
	    phiBA = phiBA / 180.0 * java.lang.Math.PI;

	double psi = phiAB - rAN;
	double phi = rBN - phiBA;

	// calculate projetiondistance
	double bInRad = distanceInRad * java.lang.Math.sin(phi) / java.lang.Math.sin(phi + psi);
	double b = bInRad * maxRadius;// * (1-flattening);
	double aInRad = distanceInRad * java.lang.Math.sin(psi) / java.lang.Math.sin(phi + psi);
	double a = aInRad * maxRadius;// * (1-flattening);
	double phiAN = phiAB - psi;
	double phiANDegrees = phiAN * 180.0 / java.lang.Math.PI;
	double phiBN = phiBA + phi;
	double phiBNDegrees = phiBN * 180.0 / java.lang.Math.PI;
	CWPoint result2 = point2.project(phiBNDegrees, a);
	CWPoint result = point1.project(phiANDegrees, b);
	double errorDistance = result.getDistance(result2);
	// if the distance between the points is to large, we will restart the calculation with the new points found.
	// since the error is mostly very small these iterations are seldom used and the needed depth is very low.
	// First we will make sure, that this calculation will terminate
	if (distance < errorDistance) {
	    err(MyLocale.getMsg(1743, "Crossbearing calculation failed. Please inform the developers at geoclub.de"));
	}
	if (errorDistance * 1000 > 1) {
	    return crossbearingCalculation(result, result2, rAN, rBN);
	}
	return result2;
    }

    /** Project a waypoint at some angle and some distance */
    private String funcProject() throws Exception {
	double distance = popCalcStackAsNumber(0);
	if (distance < 0)
	    err(MyLocale.getMsg(1718, "Cannot project a negative distance"));
	double degrees = popCalcStackAsNumber(0);
	// If we are not in degree mode, arg is in radiants ==> convert it
	if (!Preferences.itself().solverDegMode)
	    degrees = degrees * 180.0 / java.lang.Math.PI;
	if (degrees < 0 || degrees > 360)
	    if (Preferences.itself().solverDegMode)
		err(MyLocale.getMsg(1719, "Projection degrees must be in interval [0;360]"));
	    else
		err(MyLocale.getMsg(1739, "Projection degrees must be in interval [0;2*PI]"));
	String coord = popCalcStackAsString();
	if (!isValidCoord(coord))
	    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coord);
	cwPt.set(coord);
	if (Preferences.itself().metricSystem == Metrics.IMPERIAL) {
	    distance = Metrics.convertUnit(distance, Metrics.YARDS, Metrics.KILOMETER);
	} else {
	    distance = distance / 1000.0;
	}
	return cwPt.project(degrees, distance).toString();
    }

    /** Convert Radiants into degrees */
    private double funcRad2Deg() throws Exception {
	double a = popCalcStackAsNumber(0);
	return a * 180.0 / java.lang.Math.PI;
    }

    /** Replace all occurrences of a string with another string */
    private String funcReplace() throws Exception {
	String replaceWith = popCalcStackAsString();
	String whatToReplace = popCalcStackAsString();
	String s = popCalcStackAsString();
	if (whatToReplace.equals(""))
	    return s;
	return STRreplace.replace(s, whatToReplace, replaceWith);
    }

    /** Reverse a string */
    private String funcReverse(String s) {
	String res = "";
	for (int i = s.length() - 1; i >= 0; i--)
	    res += s.charAt(i);
	return res;
    }

    /**
     * Create a skeleton for multis. This function can be called in three ways:<br>
     * 
     * <pre>
     * sk()                Create skeleton for current cache (must have addi wpts)
     * sk(number)          Create skeleton for number variables
     */
    private void funcSkeleton(int nargs) throws Exception {
	String waypointName = MainTab.itself.lastselected;
	CacheHolder c = MainForm.profile.cacheDB.get(waypointName);
	if (c == null)
	    return;
	// If it is an addi, find its main cache
	if (c.isAddiWpt()) {
	    waypointName = c.mainCache.getWayPoint();
	}
	int nStages = -1;
	if (nargs == 1) {
	    nStages = (int) popCalcStackAsNumber(-1.0);
	}
	// Remove the sk command from the instructions
	Regex rex = new Regex("sk\\(.*?\\)", "");
	MainTab.itself.solverPanel.setText(rex.replaceFirst(MainTab.itself.solverPanel.getInstructions()));
	StringBuffer op = new StringBuffer(1000);
	// Check for sk(number)
	if (nStages > 0 && nStages < 30) { // e.g. sk(3)
	    /*
	     * IF $01xxxx="" THEN
	     * $01xxxx=""
	     * "Station 1 = " $01xxxx
	     * goto($01xxxx); STOP
	     * ENDIF
	     */
	    boolean didCreateWp = false;
	    for (int i = 0; i < nStages; i++) {
		String stage = MyLocale.formatLong(i, "00");
		String stageWpt = "$" + stage + waypointName.substring(2);
		String stageName = "Stage " + (i + 1);
		byte type = CacheType.CW_TYPE_STAGE;
		if (i == nStages - 1) {
		    stageName = "Final";
		    type = CacheType.CW_TYPE_FINAL;
		}
		didCreateWp |= createWptIfNeeded(stage + waypointName.substring(2), stageName, type);
		op.append("IF " + stageWpt + "=\"\" THEN\n");
		op.append("  " + stageWpt + " = \"\"\n");
		op.append("  \"" + stageName + " = \" " + stageWpt + "\n");
		op.append("  goto(" + stageWpt + "); STOP\n");
		op.append("ENDIF\n");
	    }
	    MainTab.itself.solverPanel.appendText(op.toString(), true);
	    if (didCreateWp) {
		MainTab.itself.updatePendingChanges();
		MainTab.itself.tablePanel.refreshTable();
	    }
	} else {
	    CacheHolder ch = MainForm.profile.cacheDB.get(waypointName);
	    if (ch == null) {
		err(MyLocale.getMsg(1714, "Goto: Waypoint does not exist: ") + waypointName);
		return;
	    }
	    CacheHolder addiWpt;
	    if (ch.hasAddiWpt()) {
		op.append("cls()\n");
		for (int j = 0; j < ch.addiWpts.getCount(); j++) {
		    addiWpt = (CacheHolder) ch.addiWpts.get(j);
		    op.append("IF $");
		    op.append(addiWpt.getWayPoint());
		    op.append("=\"\" THEN\n   $");
		    op.append(addiWpt.getWayPoint());
		    op.append("=\"\"");
		    // op.append(addiWpt.pos.toString());
		    op.append("\n   \"Punkt ");
		    op.append(addiWpt.getWayPoint().substring(0, 2));
		    op.append(" [");
		    op.append(addiWpt.getCacheName());
		    op.append("] = \" $");
		    op.append(addiWpt.getWayPoint());
		    if (addiWpt.getCacheDetails(true).LongDescription.trim().length() > 0)
			op.append("\n   \"" + STRreplace.replace(addiWpt.getCacheDetails(true).LongDescription, "\"", "\"\"") + "\"");
		    op.append("\n   goto($");
		    op.append(addiWpt.getWayPoint());
		    op.append("); STOP\nENDIF\n\n");
		}
		MainTab.itself.solverPanel.appendText(op.toString(), true);
	    }// if hasAddiWpt
	}
    }

    private double funcSqrt() throws Exception {
	double a = popCalcStackAsNumber(0);
	if (a < 0)
	    err(MyLocale.getMsg(1720, "Cannot calculate square root of a negative number"));
	return java.lang.Math.sqrt(a);
    }

    /** Replace each character by its number A=1, B=2 etc. and put result into a string */
    private String funcSval(String s) {
	s = s.toLowerCase();
	String res = "";
	for (int i = 0; i < s.length(); i++) {
	    int pos = "abcdefghijklmnopqrstuvwxyz".indexOf(s.charAt(i));
	    if (pos >= 0)
		res += (res == "" ? "" : " ") + MyLocale.formatLong(pos + 1, "00");
	}
	return res;
    }

    /** Replace each character by its number A=1, B=2 etc. and sum them */
    private double funcVal(String s) {
	s = s.toLowerCase();
	int sum = 0;
	for (int i = 0; i < s.length(); i++) {
	    sum += "abcdefghijklmnopqrstuvwxyz".indexOf(s.charAt(i)) + 1;
	}
	return sum;
    }

    // /////////////////////////////////////////
    // PARSER
    // /////////////////////////////////////////

    /**
     * The following methods implement a recursive descent parser.
     * Each method is called with 'thisToken' containing a valid token. It must return with 'thisToken' again containing
     * a valid token.
     */

    private void parseCommand() throws Exception {
	while (scanpos < tokenStack.size()) {
	    getToken();
	    if (thisToken.token.equals(";"))
		continue; // skip an empty command
	    if (thisToken.tt == TokenObj.TT_IF)
		parseIf();
	    else
		parseSimpleCommand();
	    checkNextSymIs(";");
	}
    }

    private void parseSimpleCommand() throws Exception {
	if (thisToken.tt == TokenObj.TT_STOP)
	    throw new Exception("STOP"); // Terminate without error message
	if (thisToken.token.equals("$")) { // Show all global variables
	    showVars(true);
	    getToken();
	} else if (thisToken.token.equals("?")) { // Show all local variables
	    showVars(false);
	    getToken();
	} else if (thisToken.tt == TokenObj.TT_VARIABLE && lookAheadToken().tt == TokenObj.TT_EQ)
	    parseAssign();
	else {
	    parseStringExp();
	    while (calcStack.size() > 0)
		messageStack.add(popCalcStackAsString());
	}
    }

    private void parseIf() throws Exception {
	int compOp;
	boolean compRes = false;
	TokenObj ifToken = thisToken;
	getToken();
	// Check for "IF varName THEN" construct to check whether a variable is defined
	if (thisToken.tt == TokenObj.TT_VARIABLE && peekToken().token.toUpperCase().equals("THEN")) {
	    String varName = thisToken.token;
	    getToken(); // THEN
	    Object result = symbolTable.get(Preferences.itself().solverIgnoreCase ? varName.toUpperCase() : varName);
	    if (result == null) { // Var not found check whether it is a waypoint
		if (varName.startsWith("$")) { // Could be a cachename
		    varName = varName.substring(1);
		    compRes = MainForm.profile.getCacheIndex(varName) != -1;
		} else
		    compRes = false;
	    } else
		// Found the variable, it must have a value
		compRes = true;
	    getNextTokenOtherThanSemi();
	} else { // Normal: IF expression THEN
	    parseStringExp();
	    compOp = thisToken.tt;
	    if (compOp < TokenObj.TT_LT || compOp > TokenObj.TT_NE)
		err(MyLocale.getMsg(1723, "Comparison operator expected"));
	    getToken();
	    parseStringExp();
	    checkNextSymIs("THEN");
	    getNextTokenOtherThanSemi();
	    boolean compAsString = false; // calcStack.get(calcStack.size()-2) instanceof String;
	    // If we can parse the first argument as a double, we will do a numeric comparison
	    try {
		Common.parseDoubleException((String) calcStack.get(calcStack.size() - 2));
	    } catch (Exception ex) {
		compAsString = true;
	    }
	    // If the first expression is not a double, compare as string.
	    if (compAsString) {
		String b = popCalcStackAsString();
		String a = popCalcStackAsString();
		switch (compOp) {
		case TokenObj.TT_EQ:
		    compRes = a.equals(b);
		    break;
		case TokenObj.TT_NE:
		    compRes = !a.equals(b);
		    break;
		case TokenObj.TT_LT:
		    compRes = a.compareTo(b) < 0;
		    break;
		case TokenObj.TT_GT:
		    compRes = a.compareTo(b) > 0;
		    break;
		case TokenObj.TT_LE:
		    compRes = a.compareTo(b) <= 0;
		    break;
		case TokenObj.TT_GE:
		    compRes = a.compareTo(b) >= 0;
		    break;
		}
	    } else { // First expression is a number, compare as numbers
		double b = popCalcStackAsNumber(0);
		double a = popCalcStackAsNumber(0);
		switch (compOp) {
		case TokenObj.TT_EQ:
		    compRes = a == b;
		    break;
		case TokenObj.TT_NE:
		    compRes = a != b;
		    break;
		case TokenObj.TT_LT:
		    compRes = a < b;
		    break;
		case TokenObj.TT_GT:
		    compRes = a > b;
		    break;
		case TokenObj.TT_LE:
		    compRes = a <= b;
		    break;
		case TokenObj.TT_GE:
		    compRes = a >= b;
		    break;
		}
	    }
	}
	if (compRes) { // comparison resulted in TRUE
	    if (thisToken.tt != TokenObj.TT_ENDIF) {
		parseSimpleCommand();
		while (thisToken.token.equals(";")) {
		    getNextTokenOtherThanSemi(); // Now we have either an ENDIF or the start of a simpleexpression
		    if (thisToken.tt == TokenObj.TT_ENDIF)
			break;
		    parseSimpleCommand();
		}
		checkNextSymIs("ENDIF");
	    }
	    getToken();
	} else
	    // comparison failed
	    skipPastEndif(ifToken);
    }

    private void parseAssign() throws Exception {
	String varName = new String(thisToken.token);
	getToken(); // =
	getToken();
	// Assigns of the format A=; are ignored so that they can stay as placeholders and
	// we can fill the data progressively during a multicache
	if (thisToken.tt == TokenObj.TT_ENDIF || thisToken.token.equals(";"))
	    return;
	parseStringExp();
	if (varName.startsWith("$")) { // Potential coordinate
	    CacheHolder ch = MainForm.profile.cacheDB.get(varName.substring(1));
	    if (ch != null) { // Yes, is a coordinate
		// Check whether new coordinates are valid
		String coord = popCalcStackAsString();
		cwPt.set(coord);
		if (cwPt.isValid() || coord.equals("")) { // Can clear coord with empty string
		    ch.setPos(cwPt);
		    ch.calcDistance(Preferences.itself().curCentrePt); // Update distance and bearing
		    MainForm.profile.selectionChanged = true; // Tell moving map to updated displayed waypoints
		    return;
		} else
		    err(MyLocale.getMsg(1712, "Invalid coordinate: ") + coord);
	    }
	    // Name starts with $ but is not a waypoint, fall through and set it as global variable
	}
	symbolTable.put(Preferences.itself().solverIgnoreCase ? varName.toUpperCase() : varName, popCalcStackAsString());
    }

    private void parseStringExp() throws Exception {
	if (thisToken.tt == TokenObj.TT_STRING) {
	    calcStack.add(thisToken.token);
	    getToken();
	} else {
	    parseExp();
	}
	// calcStack.add(popCalcStackAsString());
	while (thisToken.tt == TokenObj.TT_STRING || thisToken.tt == TokenObj.TT_NUMBER || thisToken.tt == TokenObj.TT_VARIABLE || thisToken.tt == TokenObj.TT_SYMBOL && thisToken.token.equals("(")) {
	    if (thisToken.tt == TokenObj.TT_STRING) {
		calcStack.add(thisToken.token);
		getToken();
	    } else {
		parseTailExp('+');
	    }
	    String b = popCalcStackAsString();
	    String a = popCalcStackAsString();
	    calcStack.add(a + b);
	}
    }

    private void parseExp() throws Exception {
	char unaryOp = '+';
	if (thisToken.token.equals("+") || thisToken.token.equals("-")) {
	    unaryOp = thisToken.token.charAt(0);
	    getToken();
	}
	parseTailExp(unaryOp);
    }

    private void parseTailExp(char unaryOp) throws Exception {
	parseTerm();
	if (unaryOp == '-') { // Unary minus, negate the first term
	    calcStack.add(new java.lang.Double(-popCalcStackAsNumber(0)));
	}
	while (thisToken.token.equals("+") || thisToken.token.equals("-")) {
	    char op = thisToken.token.charAt(0);
	    getToken();
	    parseTerm();
	    double b = popCalcStackAsNumber(0);
	    double a = popCalcStackAsNumber(0);
	    if (op == '+')
		calcStack.add(new java.lang.Double(a + b));
	    else
		calcStack.add(new java.lang.Double(a - b));
	}
	// If expression is followed by a formatstring, format it
	if (thisToken.tt == TokenObj.TT_FORMATSTR) {
	    calcStack.add(MyLocale.formatDouble(popCalcStackAsNumber(0), thisToken.token).replace(',', '.'));
	    getToken();
	}
    }

    private void parseTerm() throws Exception {
	parseFactor();
	while (thisToken.token.equals("*") || thisToken.token.equals("/")) {
	    char op = thisToken.token.charAt(0);
	    getToken();
	    parseFactor();
	    double b = popCalcStackAsNumber(1);
	    double a = popCalcStackAsNumber(1);
	    if (op == '*')
		calcStack.add(new java.lang.Double(a * b));
	    else if (b == 0.0)
		err(MyLocale.getMsg(1729, "Division by 0"));
	    else
		calcStack.add(new java.lang.Double(a / b));
	}
    }

    private void parseFactor() throws Exception {
	parseExpFactor();
	while (thisToken.token.equals("^")) {
	    getToken();
	    parseExpFactor();
	    double exp = popCalcStackAsNumber(0);
	    double base = popCalcStackAsNumber(0);
	    calcStack.add(new java.lang.Double(java.lang.Math.pow(base, exp)));
	}
    }

    private void parseExpFactor() throws Exception {
	fnType funcDef;
	if (thisToken.tt == TokenObj.TT_VARIABLE) {
	    if (isVariable(thisToken.token) && !lookAheadToken().token.equals("("))
		calcStack.add(getVariable(thisToken.token));
	    else if (!lookAheadToken().token.equals("("))
		err(MyLocale.getMsg(1724, "Variable not set: ") + thisToken.token);
	    else {// Must be a function definition
		funcDef = getFunctionDefinition(thisToken.token); // Does not return if function not defined or ambiguous
		parseFunction(funcDef);
	    }
	} else if (thisToken.tt == TokenObj.TT_NUMBER) {
	    calcStack.add(getNumber(thisToken.token));
	} else if (thisToken.tt == TokenObj.TT_STRING) {
	    calcStack.add(thisToken.token);
	} else if (thisToken.token.equals("(")) {
	    getToken();
	    parseStringExp();
	    checkNextSymIs(")");
	} else
	    err(MyLocale.getMsg(1725, "Unexpected character(s): ") + thisToken.token);
	getToken();
    }

    private void parseFunction(fnType funcDef) throws Exception {
	String funcName = new String(thisToken.token);
	int nargs = 0;
	getToken();
	checkNextSymIs("(");
	getToken();
	if (!thisToken.token.equals(")")) { // at least one argument
	    parseStringExp();
	    nargs = 1;
	    while (thisToken.token.equals(",")) {
		if (nargs == 4)
		    err(MyLocale.getMsg(1726, "Too many arguments for function ") + funcName);
		getToken();
		parseStringExp();
		nargs++;
	    }
	    checkNextSymIs(")");
	}
	// getToken(); done in parseFactor
	executeFunction(funcName, nargs, funcDef);
    }

    private void executeFunction(String funcName, int nargs, fnType funcDef) throws Exception {
	if (!funcDef.nargsValid(nargs))
	    err(MyLocale.getMsg(1727, "Invalid number of arguments"));
	if (funcDef.alias.equals("asin"))
	    calcStack.add(new java.lang.Double(makeDegree(java.lang.Math.asin(popCalcStackAsNumber(0)))));
	else if (funcDef.alias.equals("abs"))
	    calcStack.add(new java.lang.Double(java.lang.Math.abs(popCalcStackAsNumber(0))));
	else if (funcDef.alias.equals("acos"))
	    calcStack.add(new java.lang.Double(makeDegree(java.lang.Math.acos(popCalcStackAsNumber(0)))));
	else if (funcDef.alias.equals("atan"))
	    calcStack.add(new java.lang.Double(makeDegree(java.lang.Math.atan(popCalcStackAsNumber(0)))));
	else if (funcDef.alias.equals("bearing"))
	    calcStack.add(new java.lang.Double(funcBearing()));
	else if (funcDef.alias.equals("center"))
	    funcCenter(nargs);
	else if (funcDef.alias.equals("cls"))
	    funcCls();
	else if (funcDef.alias.equals("cos"))
	    calcStack.add(new java.lang.Double(java.lang.Math.cos(makeRadiant(popCalcStackAsNumber(0)))));
	else if (funcDef.alias.equals("count"))
	    funcCount();
	else if (funcDef.alias.equals("cp"))
	    calcStack.add(funcCp());
	else if (funcDef.alias.equals("ct"))
	    calcStack.add(new java.lang.Double(funcCrossTotal(nargs)));
	else if (funcDef.alias.equals("deg"))
	    funcDeg(true);
	else if (funcDef.alias.equals("deg2rad"))
	    calcStack.add(new java.lang.Double(funcDeg2Rad()));
	else if (funcDef.alias.equals("distance"))
	    calcStack.add(new java.lang.Double(funcDistance()));
	else if (funcDef.alias.equals("encode"))
	    calcStack.add(funcEncode());
	else if (funcDef.alias.equals("format"))
	    calcStack.add(funcFormat(nargs));
	else if (funcDef.alias.equals("goto"))
	    funcGoto(nargs);
	else if (funcDef.alias.equals("ic"))
	    funcIgnoreVariableCase(nargs);
	else if (funcDef.alias.equals("instr"))
	    calcStack.add(new Double(funcInstr(nargs)));
	else if (funcDef.alias.equals("int"))
	    calcStack.add(new Double(new Double(popCalcStackAsNumber(0)).longValue()));
	else if (funcDef.alias.equals("lc"))
	    calcStack.add(popCalcStackAsString().toLowerCase());
	else if (funcDef.alias.equals("len"))
	    calcStack.add(new Double(popCalcStackAsString().length()));
	else if (funcDef.alias.equals("mid"))
	    calcStack.add(funcMid(nargs));
	else if (funcDef.alias.equals("mod"))
	    calcStack.add(funcMod());
	else if (funcDef.alias.equals("project"))
	    calcStack.add(funcProject());
	else if (funcDef.alias.equals("pz"))
	    funcPz(nargs);
	else if (funcDef.alias.equals("rad"))
	    funcDeg(false);
	else if (funcDef.alias.equals("rad2deg"))
	    calcStack.add(new java.lang.Double(funcRad2Deg()));
	else if (funcDef.alias.equals("replace"))
	    calcStack.add(funcReplace());
	else if (funcDef.alias.equals("reverse"))
	    calcStack.add(funcReverse(popCalcStackAsString()));
	else if (funcDef.alias.equals("rot13"))
	    calcStack.add(Common.rot13(popCalcStackAsString()));
	// else if (funcDef.alias.equals("rs")) funcRequireSemicolon(nargs);
	else if (funcDef.alias.equals("show"))
	    ;
	else if (funcDef.alias.equals("sin"))
	    calcStack.add(new java.lang.Double(java.lang.Math.sin(makeRadiant(popCalcStackAsNumber(0)))));
	else if (funcDef.alias.equals("skeleton"))
	    funcSkeleton(nargs);
	else if (funcDef.alias.equals("sqrt"))
	    calcStack.add(new java.lang.Double(funcSqrt()));
	else if (funcDef.alias.equals("sval"))
	    calcStack.add(funcSval(popCalcStackAsString()));
	else if (funcDef.alias.equals("tan"))
	    calcStack.add(new java.lang.Double(java.lang.Math.tan(makeRadiant(popCalcStackAsNumber(0)))));
	else if (funcDef.alias.equals("uc"))
	    calcStack.add(popCalcStackAsString().toUpperCase());
	else if (funcDef.alias.equals("val"))
	    calcStack.add(new java.lang.Double(funcVal(popCalcStackAsString())));
	else if (funcDef.alias.equals("cb"))
	    calcStack.add(funcCrossBearing());
	else
	    err(MyLocale.getMsg(1728, "Function not yet implemented: ") + funcName);
    }

    public void parse(Vector tck, Vector msgStack) {
	calcStack.clear();
	clearLocalSymbols();
	tokenStack = tck;
	messageStack = msgStack;
	scanpos = 0;
	try {
	    parseCommand();
	} catch (Exception ex) {
	}
    }

    private boolean createWptIfNeeded(String wayPoint, String name, byte type) {
	int ci = MainForm.profile.getCacheIndex(wayPoint);
	if (ci >= 0)
	    return false;

	CacheHolder ch = new CacheHolder();
	ch.setWayPoint(wayPoint);
	ch.setType(type);
	ch.setCacheSize(CacheSize.CW_SIZE_NOTCHOSEN);
	ch.setHard(CacheTerrDiff.CW_DT_UNSET);
	ch.setTerrain(CacheTerrDiff.CW_DT_UNSET);
	ch.setCacheName(name);

	MainForm.profile.setAddiRef(ch);

	MainForm.profile.cacheDB.add(ch);
	MainTab.itself.tablePanel.myTableModel.numRows++;
	return true;
    }

}
