/* A parser that parses the following grammar:
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

import ewe.util.*;
import ewe.sys.*;
import java.lang.Double;

/**
*	The wolf language parser. New version - January 2007
*   
*   New features:
*   - Improved error handling
*   - Strings and doubles can be freely mixed as appropriate. Depending on context a conversion is performed,
*   - Variables can store strings or doubles
*   - Global variables (starting with $) are remembered across multiple calls to parser
*   - Global variables are initialised with "", local variables result in error if used before setting value
*   - IF statement added
*   - Many new functions (encode,format,goto,len,mid,count, substring,ucase,lcase,val,sval,replace, reverse,project)
*   - less typing
*   	- Function aliases
*   	- Function names can be flexibly abbreviated, i.e. instead of crosstotal write cr or cross or crosst ...
*   	- show no longer needed
*   	- Command terminator ; no longer compulsory (only between multiple commands on same line)
*   - New functions can easily be added
*   - Can select whether variable names are case sensitive
*   
*   To add a new function:
*     1) Add its name and alias and allowed number of args to array functions
*     2) Add a new private method in the "functions" section
*     3) Add call to private method in executeFunction 
*   @author salzkammergut January 2007
*/ 
public class Parser{

	private class fnType { 
		public String funcName; 	 // the function name in the user input
		public String alias;         // the funcName is mapped to this alias
		public int nargs;            // bitmap for number of args, i.e. 14 = 1 or 2 or 3 args; 5 = 0 or 2 args
									 // i.e. 1<<nargs ORed together
		fnType(String funcName, String alias, int nargs) {
			this.funcName=funcName; this.alias=alias; this.nargs=nargs;
		}
		boolean nargsValid(int testNargs){
			return ((1<<testNargs)&this.nargs)!=0;
		}
	}
    fnType[] functions=new fnType[]{ // in alphabetical order
    	new fnType("abs","abs",2),	
    	new fnType("acos","acos",2),
    	new fnType("asin","asin",2),
    	new fnType("atan","atan",2),
      	new fnType("bearing","bearing",4),
    	new fnType("center","center",3),
    	new fnType("cls","cls",1),
    	new fnType("clearscreen","cls",1),
    	new fnType("cos","cos",2),
    	new fnType("count","count",4),
     	new fnType("cp","cp",1),
    	new fnType("crosstotal","ct",6),
    	new fnType("ct","ct",2),
     	new fnType("curpos","cp",1),
     	new fnType("distance","distance",2),
     	new fnType("encode","encode",8),
    	new fnType("format","format",6),
    	new fnType("goto","goto",6),
    	new fnType("ic","ic",3),
    	new fnType("ignorecase","ic",3),
    	new fnType("instr","instr",12),
    	new fnType("lcase","lc",2),
    	new fnType("length","len",2),
    	new fnType("mid","mid",12),
     	new fnType("pc","pz",3),
     	new fnType("profilecenter","pz",3),
     	new fnType("profilzentrum","pz",3),
    	new fnType("project","project",8),
     	new fnType("pz","pz",3),
    	new fnType("quersumme","ct",6),
    	new fnType("replace","replace",8),
    	new fnType("reverse","reverse",2),
    	new fnType("rot13","rot13",2),
    	new fnType("show","show",2),
    	new fnType("sin","sin",2),
    	new fnType("sqrt","sqrt",2),
    	new fnType("sval","sval",2),
    	new fnType("tolowercase","lc",2),
    	new fnType("touppercase","uc",2),
    	new fnType("tan","tan",2),
    	new fnType("ucase","uc",2),
    	new fnType("val","val",2),
     	new fnType("zentrum","center",3)
     	    	};
	private static int scanpos = 0;
	CWPoint cwPt=new CWPoint();
	Vector calcStack=new Vector();
	Hashtable symbolTable = new Hashtable(50);
	TokenObj thisToken = new TokenObj();
	Vector tokenStack;
	Vector messageStack;
	
	public Parser(){
	}

///////////////////////////////////////////
//  Utility functions
///////////////////////////////////////////
	
	/* All errors are handled via function 'err'. Rather than creating many different Exceptions,
	 * only the standard Exception is used. err raises this exception and thereby causes the stack to be
	 * unwound until 'parse' eventually catches the exception and returns to SolverPanel, which displays
	 * the messageStack containing the error message.
	 */
	
	/** 
     * Add an error message to the message stack and raise an Exception.
    */
	private void err(String str) throws Exception {
    	messageStack.add(MyLocale.getMsg(1700,"Error on line: ") + thisToken.line + "  "+MyLocale.getMsg(1701,"position: ") + thisToken.position);
    	messageStack.add(str);
    	// move cursor to error location
    	Global.mainTab.solverP.mText.setSelectionRange(thisToken.position-1,thisToken.line-1,thisToken.position+thisToken.token.length()-1,thisToken.line-1);
    	throw new Exception("Error "+str);
    }
    
    /** Shows global symbols */
    private void showGlobals() throws Exception {
    	Iterator it=symbolTable.entries();
    	while (it.hasNext()) {
    		String varName=((String)((ewe.util.Map.MapEntry) it.next()).getKey());
    		if (varName.startsWith("$")) {
    			String value=(String) getVariable(varName);
    			if (java.lang.Double.isNaN(toNumber(value)))
    				messageStack.add(varName+" = \""+STRreplace.replace(value.toString(),"\"","\"\"")+"\"");
    			else
    				messageStack.add(varName+" = "+value);
    		}
    	}
    }

	/** Clears the symbol table of all non-global symbols (those not starting with $) */
    private void clearLocalSymbols() {
    	Iterator it=symbolTable.entries();
    	while (it.hasNext()) {
    		if (((String)((ewe.util.Map.MapEntry) it.next()).getKey()).startsWith("$")) 
    			symbolTable.remove(it);
    	}
    }
    
	private boolean isVariable(String varName) {
		return varName.startsWith("$") ||  // Global variables exist per default 
		       symbolTable.containsKey(Global.getPref().solverIgnoreCase?varName.toUpperCase():varName);
	}
	
	private boolean isInteger(double d) {
		return java.lang.Math.ceil(d)==d && java.lang.Math.floor(d)==d;
	}
	
    private boolean isValidCoord(String coord) {
    	cwPt.set(coord);
    	return cwPt.isValid();
    }
    
	private Object getVariable(String varName) throws Exception {
		if (varName.startsWith("$")) { // Potential coordinate
			int idx=Global.getProfile().getCacheIndex(varName.substring(1));
			if (idx!=-1) { // Found it!
				CacheHolder ch=(CacheHolder)Global.getProfile().cacheDB.get(idx);
				// Check whether coordinates are valid
				cwPt.set(ch.LatLon);
				if (cwPt.isValid() ) 
					return ch.LatLon;
				else
					return ""; // Convert invalid coordinates (N 0 0.0 E 0 0.0) into empty string
			}
		}
		Object result = symbolTable.get(Global.getPref().solverIgnoreCase?varName.toUpperCase():varName);
		if(result == null) {
			// If it is a global variable, add it with a default value
			if (varName.startsWith("$")) {
				result="";
				symbolTable.put(varName,"");
			} else
				err (MyLocale.getMsg(1702,"Variable not defined: ")+varName);
		}
		return result;
	}
	
	private double toNumber(String str) {
		try {
			if (Global.getPref().digSeparator.equals(","))	str = str.replace('.', ',');
			 return java.lang.Double.parseDouble(str);
		} catch (NumberFormatException e) {
			 return java.lang.Double.NaN;
		}
	}
	
	private Double getNumber(String str) throws Exception {
		double ret=toNumber(str);
		if (java.lang.Double.isNaN(ret))
			err(MyLocale.getMsg(1703,"Not a valid number: ") + str);
		return new java.lang.Double(ret);
	}
	
	/** Get the top element of the calculation stack and try and convert it to a number if it is a string */
	private double popCalcStackAsNumber(double defaultForEmptyString) throws Exception {
		double num;
		if (calcStack.get(calcStack.size()-1) instanceof String) {
			if (((String)calcStack.get(calcStack.size()-1)).equals(""))
				num=defaultForEmptyString;
			else	
				num = getNumber((String)calcStack.get(calcStack.size()-1)).doubleValue();
		} else {
			num = ((java.lang.Double)calcStack.get(calcStack.size()-1)).doubleValue();
		}
		calcStack.removeElementAt(calcStack.size()-1);
		return num;	
	}
	
	private String popCalcStackAsString() {
		String s;
		if (calcStack.get(calcStack.size()-1) instanceof Double) {
			s = ((java.lang.Double)calcStack.get(calcStack.size()-1)).toString().replace(',','.'); // always show numbers with decimal point;
			if (s.endsWith(".0")) s=s.substring(0,s.length()-2);
		} else
			s = (String)calcStack.get(calcStack.size()-1);
		calcStack.removeElementAt(calcStack.size()-1);
		return s;	
	}
	
	private void getToken() throws Exception {
		if(scanpos < tokenStack.size()){
			thisToken = (TokenObj)tokenStack.get(scanpos);
			//Vm.debug(thisToken.token);
			scanpos++;
		} else err(MyLocale.getMsg(1704,"Unexpected end of source"));
	}

	private void getNextTokenOtherThanSemi() throws Exception {
		do {
			getToken();
		} while (thisToken.token.equals(";"));	
	}

	private void skipPastEndif(TokenObj ifToken) throws Exception {
		while(scanpos < tokenStack.size()){
			thisToken = (TokenObj)tokenStack.get(scanpos);
			scanpos++;
			if (thisToken.tt==TokenObj.TT_ENDIF) {
				getToken();
				return;
			}
		}
		thisToken=ifToken;
		err(MyLocale.getMsg(1705,"Missing ENDIF"));
	}
	private TokenObj lookAheadToken() {
		return (TokenObj)tokenStack.get(scanpos);
	}
	
	private boolean checkNextSymIs(String str) throws Exception {
		if(thisToken.token.toUpperCase().equals(str)){
			return true;
		} else {
			err(MyLocale.getMsg(1706,"Expected ") + str + "  "+MyLocale.getMsg(1707,"Found: ")+thisToken.token);
			return false; //Dummy as err does not return
		}
	}

	private fnType getFunctionDefinition(String str) throws Exception {
    	fnType fnd=null;
    	str=str.toLowerCase();
    	for (int i=functions.length-1; i>=0; i--) {
    		if (functions[i].funcName.startsWith(str)) {
    			if (fnd!=null) err(MyLocale.getMsg(1708,"Ambiguous function name: ")+str);
    			fnd=functions[i];
    		}
    	}
    	if (fnd==null) err(MyLocale.getMsg(1709,"Unknown function: ")+str);
    	return fnd;
    }

///////////////////////////////////////////
//  FUNCTIONS
///////////////////////////////////////////
    
	/** Get or set the current center */
	private void funcCenter(int nargs) throws Exception {
		if (nargs==0) {
			calcStack.add(Global.getPref().curCentrePt.toString());
		} else {
	    	String coordA=popCalcStackAsString();
			if (!isValidCoord(coordA)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coordA);
			Global.getPref().curCentrePt.set(coordA);
		}
	}
	
	/** Clear Screen */
	private void funcCls() {
		// OutputPanel is private, so need to cast to base class
		((ewe.ui.mTextPad) Global.mainTab.solverP.mOutput).setText("");
	}
	
	private int funcCountChar(String s, char c) {
    	int count=0;
    	for (int i=0; i<s.length(); i++)
    		if (s.charAt(i)==c) count++;
    	return count;
    }
    
    /** count(string1,string2) 
     * */
    private void funcCount()throws Exception {
       	String s2=popCalcStackAsString();
    	String s1=popCalcStackAsString();
    	if (s2.length()==0) err(MyLocale.getMsg(1710,"Cannot count empty string"));
    	if (s2.length()==1) {
    		calcStack.add(new Double(funcCountChar(s1,s2.charAt(0))));
    	} else {
    		String res="";
    		for(int i=0; i<s2.length(); i++) {
    			res+=s2.charAt(i)+"="+funcCountChar(s1,s2.charAt(i))+" ";
    		}
    		calcStack.add(res);
    	} 
    }
    
    private String funcCp(){
    	return Global.mainTab.gotoP.gpsPosition.toString();
    }
    
    private double funcCrossTotal(int nargs) throws Exception {
    	int cycles=1;
		if (nargs==2) cycles=(int)popCalcStackAsNumber(1);
		double a=java.lang.Math.abs(popCalcStackAsNumber(0));
		if (cycles<0) cycles=1;
    	if (cycles>5) cycles=5;
    	while (a>10 && cycles-->0) {
	    	// Cross total = Quersumme berechnen
			String aString = Convert.toString(a); // 
			// bei 1.8e2 nur 1.8 verwenden 
			if (aString.toLowerCase().indexOf("e") > 0) aString = aString.substring(0, aString.toLowerCase().indexOf("e"));
			a=0;
			for (int i=0; i<aString.length(); i++) {
			 a += Convert.toDouble(Convert.toString(aString.charAt(i)));	
			}
    	}return a;
    }
    
    /** Calculate distance between 2 points */
    private double funcDistance() throws Exception {
    	String coordB=popCalcStackAsString();
    	String coordA=popCalcStackAsString();
    	cwPt.set(coordA);
		if (!isValidCoord(coordA)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coordA);
		if (!isValidCoord(coordB)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coordB);
    	return cwPt.getDistance(new CWPoint(coordB));
    }
    
    /** Calculate brearing from one point to the next */
    private double funcBearing() throws Exception {
    	String coordB=popCalcStackAsString();
    	String coordA=popCalcStackAsString();
    	cwPt.set(coordA);
		if (!isValidCoord(coordA)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coordA);
		if (!isValidCoord(coordB)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coordB);
    	return cwPt.getBearing(new CWPoint(coordB));
    }
    /**
     * Encode a string by replacing all characters in a string with their corresponding characters in
     * another string
     * @throws Exception
     */
    private String funcEncode() throws Exception {
    	String newChars=popCalcStackAsString();
    	String oldChars=popCalcStackAsString();
    	if (newChars.length()!=oldChars.length()) err(MyLocale.getMsg(1711,"Replacement characters strings must be of equal length"));
    	String s=popCalcStackAsString();
    	String encodedStr="";
    	for (int i=0; i<s.length(); i++) {
    		int pos;
    		if ((pos=oldChars.indexOf(s.charAt(i)))!=-1) {
    			encodedStr+=newChars.charAt(pos);
    		} else 
    			encodedStr+=s.charAt(i);
    	}
    	 return encodedStr;
    }
    
    /** Format a valid coordinate 
     *  If called with one args, format the argument on the stack to CW standard
     *  The optional second argument is one of these strings "UTM","DMS","DD","DMM" or "CW"
     * @param nargs 1 or 2 args
     */
    private String funcFormat(int nargs) throws Exception {
    	String fmtStr="";
    	if (nargs==2)fmtStr=popCalcStackAsString().toLowerCase();
    	String coord=popCalcStackAsString();
		if (!isValidCoord(coord)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coord);
    	cwPt.set(coord);
    	int fmt=CWPoint.CW;
    	if (fmtStr.equals("dd")) fmt=CWPoint.DD;
    	else if (fmtStr.equals("dmm")) fmt=CWPoint.DMM;
    	else if (fmtStr.equals("dms")) fmt=CWPoint.DMS;
    	else if (fmtStr.equals("utm")) fmt=CWPoint.UTM;
    	else if (!fmtStr.equals("cw")) err(MyLocale.getMsg(1713,"Invalid coordinate format. Allowed are CW/DD/DMM/DMS/UTM"));
    	return cwPt.toString(fmt);
    }
    
    /** Implements a goto command goto(coordinate,optionalWaypointName).
     */
    private void funcGoto(int nargs) throws Exception {
    	GotoPanel gotoP=Global.mainTab.gotoP;
		String waypointName=null;
        if (nargs==2) waypointName=popCalcStackAsString();  
		String coord=popCalcStackAsString();
		if (!isValidCoord(coord)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coord);
		// Don't want to switch to goto panel, just set the values
		gotoP.setDestination(coord);
		if (nargs==2) { // Now set the value of the addi waypoint (it must exist already)
    		int i=Global.getProfile().getCacheIndex(waypointName);
    		if (i<0) err(MyLocale.getMsg(1714,"Goto: Waypoint does not exist: ")+waypointName);
    		cwPt.set(coord);
    		((CacheHolder)Global.getProfile().cacheDB.get(i)).LatLon=cwPt.toString(CWPoint.CW);
    	}
    }
    
    /** Display or change the case sensitivity of variable names */
    private void funcIgnoreVariableCase(int nargs) throws Exception {
    	if (nargs==0) 
    		calcStack.add(""+Global.getPref().solverIgnoreCase);
    	else {
    		Global.getPref().solverIgnoreCase=(popCalcStackAsNumber(0)!=0)?true:false;
    	}
    }
    
    /** VB instr function 
     * instr([start],string1,string2)
     * */
    private int funcInstr(int nargs) throws Exception {
    	String s2=popCalcStackAsString();
    	String s1=popCalcStackAsString();
    	int start=1;
    	if (nargs==3) start=(int) popCalcStackAsNumber(1);
    	if (start>s1.length()) err(MyLocale.getMsg(1715,"instr: Start position not in string"));
    	if(s2.equals("")) {
    		if (s1.equals("")) 
    			return 0;
    		else
    			return 1;
    	}
    	return s1.indexOf(s2,start-1)+1;
    }

    /** MID function as in Basic */
    private String funcMid(int nargs) throws Exception {
    	if (nargs==2) {
        	double start=popCalcStackAsNumber(0);
    		String s=popCalcStackAsString();
    		if (!isInteger(start)) err(MyLocale.getMsg(1716,"mid: Integer argument expected"));
    		if (start<1 || start>s.length()) err(MyLocale.getMsg(1717,"mid: Argument out of range"));
    		return s.substring((int)start-1);
    	} else {
        	double len=popCalcStackAsNumber(0);
        	double start=popCalcStackAsNumber(0);
    		String s=popCalcStackAsString();
    		if (!isInteger(start) || !isInteger(len)) err(MyLocale.getMsg(1716,"mid: Integer argument expected"));
    		int end=(int)(start+len-1);
    		if (start>s.length() || start<1 || end>s.length()) err(MyLocale.getMsg(1717,"mid: Argument out of range"));
    		return s.substring((int)start-1,end);
    	}
    }
 
	/** Get or set the profile center */
	private void funcPz(int nargs) throws Exception {
		if (nargs==0) {
			calcStack.add(Global.getProfile().centre.toString());
		} else {
	    	String coordA=popCalcStackAsString();
			if (!isValidCoord(coordA)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coordA);
			Global.getProfile().centre.set(coordA);
		}
	}
    
    /** Project a waypoint at some angle and some distance */
    private String funcProject() throws Exception {
    	double distance=popCalcStackAsNumber(0);
    	if (distance<0) err(MyLocale.getMsg(1718,"Cannot project a negative distance"));
    	double degrees=popCalcStackAsNumber(0);
    	if (degrees<0 || degrees>360) err(MyLocale.getMsg(1719,"Projection degrees must be in interval [0;360]"));
    	String coord=popCalcStackAsString();
		if (!isValidCoord(coord)) err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coord);
    	cwPt.set(coord);
    	return cwPt.project(degrees,distance/1000.0).toString();
    }

    /** Replace all occurrences of a string with another string */
    private String funcReplace() throws Exception {
    	String replaceWith=popCalcStackAsString();
    	String whatToReplace=popCalcStackAsString();
    	String s=popCalcStackAsString();
        if (whatToReplace.equals("") || replaceWith.equals("")) return s;
        return STRreplace.replace(s,whatToReplace,replaceWith);
    }
    
    /** Reverse a string */
    private String funcReverse(String s) {
    	String res="";
    	for (int i=s.length()-1; i>=0; i--) res+=s.charAt(i);
    	return res;
    }
    
    private double funcSqrt() throws Exception {
    	double a=popCalcStackAsNumber(0);
    	if (a<0) err(MyLocale.getMsg(1720,"Cannot calculate square root of a negative number"));
    	return java.lang.Math.sqrt(a);
    }
    
    /** Replace each character by its number A=1, B=2 etc. and put result into a string */
    private String funcSval(String s) {
       	s=s.toLowerCase();
    	String res="";
       	for (int i=0; i<s.length(); i++) {
    		int pos="abcdefghijklmnopqrstuvwxyz".indexOf(s.charAt(i));
    		if (pos>=0) 
    			res+=(res==""?"":" ")+MyLocale.formatLong(pos+1,"00");
    	}
    	return res;
    }
    
    /** Replace each character by its number A=1, B=2 etc. and sum them */
    private double funcVal(String s) {
    	s=s.toLowerCase();
    	int sum=0;
    	for (int i=0; i<s.length(); i++) {
    		sum+="abcdefghijklmnopqrstuvwxyz".indexOf(s.charAt(i))+1;
    	}
    	return sum;
    }
    
///////////////////////////////////////////
//  PARSER
///////////////////////////////////////////
   
    
    /** The following methods implement a recursive descent parser.
     * Each method is called with 'thisToken' containing a valid token. It must return with 'thisToken' again containing
     * a valid token.
     */
 
	private void parseCommand()  throws Exception {
		while(scanpos < tokenStack.size()) {
			getToken();
			if (thisToken.token.equals(";")) continue;  // skip an empty command
			if (thisToken.tt==TokenObj.TT_IF) 
				parseIf();
			else 
				parseSimpleCommand();
			checkNextSymIs(";");
			while (calcStack.size()>0) messageStack.add(popCalcStackAsString());
		}
	}

	private void parseSimpleCommand() throws Exception{
		if (thisToken.tt==TokenObj.TT_STOP) throw new Exception("STOP");  // Terminate without error message
		if (thisToken.token.equals("$")) {
			showGlobals();
			getToken();
		} else if (thisToken.tt==TokenObj.TT_VARIABLE && lookAheadToken().tt==TokenObj.TT_EQ) 
			parseAssign();
		else 
			parseStringExp();		
	}
	
	private void parseIf() throws Exception{
		int compOp;
		boolean compRes=false;
		TokenObj ifToken=thisToken;
		getToken();
		parseStringExp();
		compOp=thisToken.tt;
		if (compOp<TokenObj.TT_LT || compOp>TokenObj.TT_NE) err(MyLocale.getMsg(1723,"Comparison operator expected"));
		getToken();
		parseStringExp();
		checkNextSymIs("THEN");
		getNextTokenOtherThanSemi();
		// If the first expression is a string, compare as string.
		if (calcStack.get(calcStack.size()-2) instanceof String) {
			String b=popCalcStackAsString();
			String a=popCalcStackAsString();
			switch (compOp) {
				case TokenObj.TT_EQ: compRes=a.equals(b); break;
				case TokenObj.TT_NE: compRes=!a.equals(b); break;
				case TokenObj.TT_LT: compRes=a.compareTo(b)<0; break;
				case TokenObj.TT_GT: compRes=a.compareTo(b)>0; break;
				case TokenObj.TT_LE: compRes=a.compareTo(b)<=0; break;
				case TokenObj.TT_GE: compRes=a.compareTo(b)>=0; break;
			}
		} else { // First expression is a number, compare as numbers
			double b=popCalcStackAsNumber(0);
			double a=popCalcStackAsNumber(0);
			switch (compOp) {
				case TokenObj.TT_EQ: compRes=a==b; break;
				case TokenObj.TT_NE: compRes=a!=b; break;
				case TokenObj.TT_LT: compRes=a<b; break;
				case TokenObj.TT_GT: compRes=a>b; break;
				case TokenObj.TT_LE: compRes=a<=b; break;
				case TokenObj.TT_GE: compRes=a>=b; break;
			}
		}
		if (compRes) { // comparison resulted in TRUE
			if (thisToken.tt!=TokenObj.TT_ENDIF) {
				parseSimpleCommand();
				while (thisToken.token.equals(";")) {
					getNextTokenOtherThanSemi(); // Now we have either an ENDIF or the start of a simpleexpression
					if (thisToken.tt==TokenObj.TT_ENDIF) break;
					parseSimpleCommand();
				}
				checkNextSymIs("ENDIF");
			}
			getToken();
		} else // comparison failed
			skipPastEndif(ifToken);
	}
	
	private void parseAssign() throws Exception  {
		String varName=new String(thisToken.token);
		getToken(); //=
		getToken();
		// Assigns of the format A=; are ignored so that they can stay as placeholders and 
		// we can fill the data progressively during a multicache
		if (thisToken.tt==TokenObj.TT_ENDIF || thisToken.token.equals(";")) return;
		parseStringExp();
		if (varName.startsWith("$")) { // Potential coordinate
			int idx=Global.getProfile().getCacheIndex(varName.substring(1));
			if (idx!=-1) { // Yes, is a coordinate
				CacheHolder ch=(CacheHolder)Global.getProfile().cacheDB.get(idx);
				// Check whether new coordinates are valid
				String coord=popCalcStackAsString();
				cwPt.set(coord);
				if (cwPt.isValid() || coord.equals("")) { // Can clear coord with empty string
					ch.LatLon=cwPt.toString(CWPoint.CW);
				    return;
				} else
					err(MyLocale.getMsg(1712,"Invalid coordinate: ")+coord);
			}
			// Name starts with $ but is not a waypoint, fall through and set it as global variable
		}
		symbolTable.put(varName, popCalcStackAsString());
	}
	
	private void parseStringExp()throws Exception {
		if (thisToken.tt==TokenObj.TT_STRING) {
			calcStack.add(thisToken.token);
			getToken();
		} else {
			parseExp();
		}
		//calcStack.add(popCalcStackAsString());
		while (thisToken.tt==TokenObj.TT_STRING || 
			   thisToken.tt==TokenObj.TT_NUMBER || 
			   thisToken.tt==TokenObj.TT_VARIABLE ||
			   thisToken.tt==TokenObj.TT_SYMBOL && thisToken.token.equals("(")) {
			if (thisToken.tt==TokenObj.TT_STRING) {
				calcStack.add(thisToken.token);
				getToken();
			} else {
				parseTailExp('+');
			}
			String b=popCalcStackAsString();
			String a=popCalcStackAsString();
			calcStack.add(a+b);
		}
	}
	
	private void parseExp()throws Exception {
		char unaryOp='+';
		if (thisToken.token.equals("+") || thisToken.token.equals("-") ) {
			unaryOp=thisToken.token.charAt(0);
			getToken();
		}
		parseTailExp(unaryOp);
	}

	private void parseTailExp(char unaryOp)throws Exception {
		parseTerm();
		if (unaryOp=='-') { // Unary minus, negate the first term
			calcStack.add(new java.lang.Double(-popCalcStackAsNumber(0)));
		}
		while (thisToken.token.equals("+") || thisToken.token.equals("-") ) {
			char op=thisToken.token.charAt(0);
			getToken();
			parseTerm();
			double b=popCalcStackAsNumber(0);
			double a=popCalcStackAsNumber(0);
			if (op=='+')
				calcStack.add(new java.lang.Double(a+b));
			else
				calcStack.add(new java.lang.Double(a-b));
		}
		// If expression is followed by a formatstring, format it
		if (thisToken.tt==TokenObj.TT_FORMATSTR) {
			calcStack.add(MyLocale.formatDouble(popCalcStackAsNumber(0),thisToken.token).replace(',','.'));
			getToken();
		}
	}
	
	private void parseTerm() throws Exception{
		parseFactor();
		while (thisToken.token.equals("*") || thisToken.token.equals("/") ) {
			char op=thisToken.token.charAt(0);
			getToken();
			parseFactor();
			double b=popCalcStackAsNumber(1);
			double a=popCalcStackAsNumber(1);
			if (op=='*')
				calcStack.add(new java.lang.Double(a*b));
			else
				if (b==0.0) 
					err(MyLocale.getMsg(1729,"Division by 0"));
				else 
					calcStack.add(new java.lang.Double(a/b));
		}
	}
	
	private void parseFactor() throws Exception{
		parseExpFactor();
		while (thisToken.token.equals("^")) {
			getToken();
			parseExpFactor();
			double exp=popCalcStackAsNumber(0);
			double base=popCalcStackAsNumber(0);
			calcStack.add(new java.lang.Double(java.lang.Math.pow(base,exp)));
		}
	}

	private void parseExpFactor() throws Exception {
		fnType funcDef;
		if (thisToken.tt==TokenObj.TT_VARIABLE) {
			if (isVariable(thisToken.token))
				calcStack.add(getVariable(thisToken.token));
			else if (!lookAheadToken().token.equals("(")) err(MyLocale.getMsg(1724,"Variable not set: ")+thisToken.token);
			else {// Must be a function definition
				funcDef=getFunctionDefinition(thisToken.token); // Does not return if function not defined or ambiguous
				parseFunction(funcDef);
			} 
		} else if (thisToken.tt==TokenObj.TT_NUMBER) {
			calcStack.add(getNumber(thisToken.token));
		} else if (thisToken.tt==TokenObj.TT_STRING) {
			calcStack.add(thisToken.token);
		} else if (thisToken.token.equals("(")) {
			getToken();
			parseStringExp();
			checkNextSymIs(")");
		}
		else err(MyLocale.getMsg(1725,"Unexpected character(s): ")+thisToken.token);
		getToken();
	}
	
	private void parseFunction(fnType funcDef) throws Exception {
		String funcName=new String(thisToken.token);
        int nargs=0;
		getToken();
		checkNextSymIs("(");
		getToken();
		if (!thisToken.token.equals(")")) { // at least one argument
			parseStringExp();
			nargs=1;
			while (thisToken.token.equals(",")) {
				if (nargs==4) err(MyLocale.getMsg(1726,"Too many arguments for function ")+funcName);
				getToken();
				parseStringExp(); 
				nargs++;
			}
			checkNextSymIs(")");
		}
		//getToken(); done in parseFactor
		executeFunction(funcName,nargs,funcDef);
	}
	
	private void executeFunction(String funcName, int nargs, fnType funcDef) throws Exception {
		if (!funcDef.nargsValid(nargs)) err(MyLocale.getMsg(1727,"Invalid number of arguments"));
	         if (funcDef.alias.equals("asin")) calcStack.add(new java.lang.Double(java.lang.Math.asin(popCalcStackAsNumber(0))));
	 	else if (funcDef.alias.equals("abs")) calcStack.add(new java.lang.Double(java.lang.Math.abs(popCalcStackAsNumber(0))));
	    else if (funcDef.alias.equals("acos")) calcStack.add(new java.lang.Double(java.lang.Math.acos(popCalcStackAsNumber(0))));
	    else if (funcDef.alias.equals("atan")) calcStack.add(new java.lang.Double(java.lang.Math.atan(popCalcStackAsNumber(0))));
	    else if (funcDef.alias.equals("bearing")) calcStack.add(new java.lang.Double(funcBearing()));
	    else if (funcDef.alias.equals("center")) funcCenter(nargs);
	    else if (funcDef.alias.equals("cls")) funcCls();
	    else if (funcDef.alias.equals("cos")) calcStack.add(new java.lang.Double(java.lang.Math.cos(popCalcStackAsNumber(0))));
	    else if (funcDef.alias.equals("count")) funcCount();
	    else if (funcDef.alias.equals("cp")) calcStack.add(funcCp());     
	    else if (funcDef.alias.equals("ct")) calcStack.add(new java.lang.Double(funcCrossTotal(nargs)));
	    else if (funcDef.alias.equals("distance")) calcStack.add(new java.lang.Double(funcDistance()));
	    else if (funcDef.alias.equals("encode")) calcStack.add(funcEncode());
	    else if (funcDef.alias.equals("format")) calcStack.add(funcFormat(nargs));
	    else if (funcDef.alias.equals("goto")) funcGoto(nargs);
	    else if (funcDef.alias.equals("ic")) funcIgnoreVariableCase(nargs);
	    else if (funcDef.alias.equals("instr")) calcStack.add(new Double(funcInstr(nargs)));
	    else if (funcDef.alias.equals("lc")) calcStack.add(popCalcStackAsString().toLowerCase());
	    else if (funcDef.alias.equals("len")) calcStack.add(new Double(popCalcStackAsString().length()));
	    else if (funcDef.alias.equals("mid")) calcStack.add(funcMid(nargs));
	    else if (funcDef.alias.equals("project")) calcStack.add(funcProject());     
	    else if (funcDef.alias.equals("pz")) funcPz(nargs);     
	    else if (funcDef.alias.equals("replace")) calcStack.add(funcReplace());
	    else if (funcDef.alias.equals("reverse")) calcStack.add(funcReverse(popCalcStackAsString()));
	    else if (funcDef.alias.equals("rot13")) calcStack.add(Common.rot13(popCalcStackAsString()));
//	    else if (funcDef.alias.equals("rs")) funcRequireSemicolon(nargs);
	    else if (funcDef.alias.equals("show"));
	    else if (funcDef.alias.equals("sin")) calcStack.add(new java.lang.Double(java.lang.Math.sin(popCalcStackAsNumber(0))));
	    else if (funcDef.alias.equals("sqrt")) calcStack.add(new java.lang.Double(funcSqrt())); 
	    else if (funcDef.alias.equals("sval")) calcStack.add(funcSval(popCalcStackAsString()));
	    else if (funcDef.alias.equals("tan")) calcStack.add(new java.lang.Double(java.lang.Math.tan(popCalcStackAsNumber(0))));
	    else if (funcDef.alias.equals("uc")) calcStack.add(popCalcStackAsString().toUpperCase());
	    else if (funcDef.alias.equals("val")) calcStack.add(new java.lang.Double(funcVal(popCalcStackAsString())));
	    else err(MyLocale.getMsg(1728,"Function not yet implemented: ")+funcName);
	}
	
	public void parse(Vector tck, Vector msgStack){
		calcStack.clear();
		clearLocalSymbols();
		tokenStack = tck;
		messageStack = msgStack;
		scanpos = 0;
		try{
			parseCommand();
		}catch(Exception ex){
			//Vm.debug(ex.toString());
		}
	}
	
}
