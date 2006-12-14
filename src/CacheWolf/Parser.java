/*
A parser that parses and interprets the following grammer:
program -> command | assign
command -> 	   show(var) | 
		   (*) show(expr) |
		   ( ) project(varN,varE,deg,distance,varNN,varNE) |
		   (*)  "...text..."
assign -> 	string = expr | 
		( ) string = distance(N,E,N,E) | 
		( ) string = bearing(N,E,N,E) | 
		( ) string = coord([N|S|E|W DD MM SS] | [N|S|E|W DD MM.mmm] | [N|S|E|W DD.ddd]) // North,East = "+" South,West = "-"
		( ) string = coordUTM(???) |
		( ) string = convert2UTM(var) |
		( ) string = convert2Deg(var)
expr -> expr + term | 
		expr - term | 
		term
term -> term * factor | 
		term / factor | 
		factor
factor -> number | 
		  string | 
		  ( expr ) | 
		  (*) sqrt ( expr ) | 
		  (*) ^ expr |
		  (*) cos() |
		  (*) sin() |
		  (*) tan() |
		  (*) acos() |
		  (*) asin() |
		  (*) atan() |
		 
*/
package CacheWolf;

import ewe.util.*;
import ewe.sys.*;

/**
*	The wolf language parser. This class needs thorough documentation!
*/ 
public class Parser{

	private static int scanpos = 0;
	Vector TokenStack = new Vector();
	Vector CalcStack = new Vector();
	Vector MessageStack = new Vector();
	Hashtable symbolTable = new Hashtable(50);
	TokenObj thisToken = new TokenObj();
	boolean runFlag = true;
	String emit_buffer = new String();
	
	public Parser(){
		
	}

	public Parser(Vector tck){
		TokenStack = tck;
	}

	public void setTockenStack(Vector tck){
		TokenStack = new Vector();
		CalcStack = new Vector();
		MessageStack = new Vector();
		symbolTable = new Hashtable(50);
		thisToken = new TokenObj();
		scanpos = 0;
		runFlag = true;
		TokenStack = tck;
	}
	
	public void testSymbolTable(){
		symbolTable.put("A", "12");
		symbolTable.put("B", "13");
		symbolTable.put("C", "14");
	}
	
	public void parse(){
		try{
		  parseprogram();
		}catch(Exception ex){
			////Vm.debug(ex.toString());
		}
	}
	
	public Vector getMessages(){
		return MessageStack;
	}
	
	private void getToken(){
		if(scanpos < TokenStack.size() && runFlag == true){
			thisToken = (TokenObj)TokenStack.get(scanpos);
			////Vm.debug(thisToken.token);
			scanpos++;
		}
	}
		
	private boolean match(String str){
		getToken();
		////Vm.debug("Matching: " + str + " with " +thisToken.token);
		if(thisToken.token.equals(str)){
			////Vm.debug("Match ok!");
			return true;
		} else {
			if(str.equals(";")) str = ";";
			////Vm.debug("Error");
			err("# Error: expected " + str + " on line: " + thisToken.line + " position: " + thisToken.position);
			return false;
		}
	}
	
	private void err(String str){
		emit(str);
		runFlag = false;
	}
	
	private void emit(String str){
		MessageStack.add(str);
	}
	
	/* Replace all instances of a String in a String.
	 *   @param  s  String to alter.
	 *   @param  f  String to look for.
	 *   @param  r  String to replace it with, or null to just remove it.
	 */ 
	public String replace( String s, String f, String r )
	{
	   if (s == null)  return s;
	   if (f == null)  return s;
	   if (r == null)  r = "";
	
	   int index01 = s.indexOf( f );
	   while (index01 != -1)
	   {
	      s = s.substring(0,index01) + r + s.substring(index01+f.length());
	      index01 += r.length();
	      index01 = s.indexOf( f, index01 );
	   }
	   return s;
	}
	
	private void parseCommand(){
		boolean foundCommand = false;
		////Vm.debug("In command");
		getToken();
		Vm.debug(thisToken.token);
		if(thisToken.token.equals("project")){
			foundCommand = true;
			if(runFlag) match("(");
			
			if(runFlag) match(")");
		}
		if(thisToken.token.equals("st")){
			foundCommand = true;
			runFlag = false;
		}
		if(thisToken.token.equals("stop")){
			foundCommand = true;
			runFlag = false;
		}
		if(thisToken.token.equals("show")){
			foundCommand = true;
			////Vm.debug("Got a show");
			////Vm.sleep(1500);
			if(runFlag) match("(");
			String result = new String();
			getToken(); // gettin the '(';
			// do a lookahead for a ')'
			getToken();
			////Vm.debug("-->: " + thisToken.token);
			if(thisToken.token.equals(")")){ // ok we have a variable
				//reset to old position
				scanpos--;
				scanpos--;
				getToken();
				if(runFlag) result = (String)symbolTable.get(thisToken.token);
				if(result != null){
					result.trim();
					result = replace(result, ".0", "");
					result = replace(result, ",0", "");
					emit_buffer = emit_buffer + result;
				} else {
					err("# Error: Variable " +thisToken.token+ " not defined");
				}
				
			} else { // not a variable, we should have an expression
				////Vm.debug("Going to parse expression!");
				
				scanpos--;
				scanpos--;
				////Vm.debug("**" + thisToken.token);
				////Vm.debug("runflag: " + Convert.toString(runFlag));
				//////Vm.sleep(500);
				////Vm.debug("in the show and parsexpr");
				////Vm.sleep(1500);
				if(runFlag) parseExpr();
				
				
				java.lang.Double a = new java.lang.Double(0);
				if(runFlag) {
					a = (java.lang.Double)(CalcStack.get(CalcStack.size()-1));
					CalcStack.removeElementAt(CalcStack.size()-1);
				}
				String stra = a.toString();
				stra.trim();
				stra = replace(stra, ",0", "");
				stra = replace(stra, ".0", "");
				emit_buffer = emit_buffer + stra;
				
			}
			if(runFlag) match(")");
			getToken();
			if(thisToken.token.equals(";")){
				////Vm.debug("In show: " + emit_buffer);
				emit(emit_buffer);
				emit_buffer = "# ";
				scanpos--;
				match(";");
			} else scanpos--;
		}
		if(thisToken.token.equals("\"") && foundCommand == false){
			foundCommand = true;
			getToken();
			while(!thisToken.token.equals("\"")){
				emit_buffer = emit_buffer + thisToken.token;
				getToken();
			}
			getToken();
			if(thisToken.token.equals(";")) {
				////Vm.debug("In thing: " + emit_buffer);
				emit(emit_buffer);
				emit_buffer = "# ";
			} else scanpos--;
		}
		if(foundCommand == false) scanpos--;
	}	
	
	public boolean IsAlpha(String c){
		//check for reserved words
		if(c.equals("show")) c = "-1";
		if(c.equals("st")) c = "-1";
		if(c.equals("stop")) c = "-1";
		c = c.toUpperCase();
		char ch = c.charAt(0);
		if("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(ch) != -1) return true;
		else return false;
	}
	
	public boolean IsDigit(String c){
		//java.lang.Double test;
		boolean code = false;
		try {
			//Vm.debug(""+c);
			//Vm.sleep(500);
			//test = java.lang.Double.valueOf(c);
			Convert.parseDouble(c);
			code = true;
		} catch (NumberFormatException e){
			////Vm.debug("Throwing an exeption");
			//////Vm.sleep(500);
			code = false;
		}
		return code;
	}
	
	private void parseAssign(){
		////Vm.debug("In assign");
		java.lang.Double a = new java.lang.Double(0);
		boolean foundAssign = false;
		getToken();
		if(IsAlpha(thisToken.token)){
			foundAssign = true;
			String thisVar = new String();
			thisVar = thisToken.token;
			////Vm.debug("pos: " + scanpos);
			if(runFlag) match("=");
			getToken();
				scanpos--; // because of the getToken above!
				if(runFlag) parseExpr();
				if(runFlag){
					//Vm.debug("This should be the crack!");
					//Vm.sleep(1500);
					a = (java.lang.Double)(CalcStack.get(CalcStack.size()-1));
					CalcStack.removeElementAt(CalcStack.size()-1);
				}
				//Vm.debug("This should be a: " + a);
				symbolTable.put(thisVar, a.toString());
			//}
			if(runFlag) match(";");
		}
		if(foundAssign == false) scanpos--;
	}
	
	private void parseExpr(){
		//Vm.debug("in parseexpr");
		////Vm.sleep(1500);
		boolean foundExpr = false;
		boolean doit = true;
		parseTerm();
		////Vm.debug("Back from term");
		//////Vm.sleep(500);
		while(doit){
			getToken();
			doit = false;	
			foundExpr = false;
			/*
			for(int i = 0; i< CalcStack.size(); i++){
					////Vm.debug("Calcstack: " + CalcStack.get(i));
				}*/
			if(thisToken.token.equals("+")){
				foundExpr = true;
				parseTerm();
				java.lang.Double a = new java.lang.Double(0);
				java.lang.Double b = new java.lang.Double(0);
				double c;
				//Vm.debug("We are here!");
				////Vm.sleep(1500);
				a = (java.lang.Double)CalcStack.get(CalcStack.size()-1);
				//Vm.debug("first var: " + a.toString());
				////Vm.sleep(1500);
				CalcStack.removeElementAt(CalcStack.size()-1);
				b = (java.lang.Double)CalcStack.get(CalcStack.size()-1);
				//Vm.debug("second var: " + b.toString());
				////Vm.sleep(1500);
				CalcStack.removeElementAt(CalcStack.size()-1);
				c = a.doubleValue() + b.doubleValue();
				//Vm.debug("A:: " + c);
				//Vm.sleep(1500);
				CalcStack.add(new java.lang.Double(c));
				//Vm.debug("!" + "+");
				//Vm.sleep(1500);
				doit = true;
				
			}
			if(thisToken.token.equals("-") && foundExpr == false){
				foundExpr = true;
				parseTerm();
				java.lang.Double a = new java.lang.Double(0);
				java.lang.Double b = new java.lang.Double(0);
				double c;
				b = (java.lang.Double)CalcStack.get(CalcStack.size()-1);
				CalcStack.removeElementAt(CalcStack.size()-1);
				a = (java.lang.Double)CalcStack.get(CalcStack.size()-1);
				CalcStack.removeElementAt(CalcStack.size()-1);
				c = a.doubleValue() - b.doubleValue();
				//Vm.debug("B:: " + c);
				//Vm.sleep(1500);
				CalcStack.add(new java.lang.Double(c));
				doit = true;
			}
		}
		if(foundExpr == false) scanpos--;
	}	
	
	private void parseTerm(){
		//Vm.debug("In parseterm");
		////Vm.sleep(1500);
		boolean foundTerm = false;
		boolean doit = true;
		parseFactor();
		//Vm.debug("Back from factor");
		//Vm.sleep(500);
		while(doit){
			doit = false;
			foundTerm = false;
			getToken();
			if(thisToken.token.equals("*")){
				foundTerm = true;
				parseFactor();
				java.lang.Double a = new java.lang.Double(0);
				java.lang.Double b = new java.lang.Double(0);
				double c;
				a = (java.lang.Double)CalcStack.get(CalcStack.size()-1);
				CalcStack.removeElementAt(CalcStack.size()-1);
				b = (java.lang.Double)CalcStack.get(CalcStack.size()-1);
				CalcStack.removeElementAt(CalcStack.size()-1);
				c = a.doubleValue() * b.doubleValue();
				//Vm.debug("D:: " + c);
				//Vm.sleep(1500);
				CalcStack.add(new java.lang.Double(c));
				////Vm.debug("!" + "*");
				doit = true;
			}
			if(thisToken.token.equals("/") && foundTerm == false){
				foundTerm = true;
				parseFactor();
				java.lang.Double a = new java.lang.Double(0);
				java.lang.Double b = new java.lang.Double(0);
				double c;
				b = (java.lang.Double)CalcStack.get(CalcStack.size()-1);
				CalcStack.removeElementAt(CalcStack.size()-1);
				a = (java.lang.Double)CalcStack.get(CalcStack.size()-1);
				CalcStack.removeElementAt(CalcStack.size()-1);
				c = a.doubleValue() / b.doubleValue();
				//Vm.debug("D:: " + c);
				//Vm.sleep(1500);
				CalcStack.add(new java.lang.Double(c));
				////Vm.debug("!" + "/");
				doit = true;
			}
			if(thisToken.token.equals("^") && foundTerm == false){
				foundTerm = true;
				parseFactor();
				double a,b,c;
				b = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
				CalcStack.removeElementAt(CalcStack.size()-1);
				a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
				CalcStack.removeElementAt(CalcStack.size()-1);
				c = java.lang.Math.pow(a,b);
				//Vm.debug("E:: " + c);
				//Vm.sleep(1500);
				CalcStack.add(new java.lang.Double(c));
				////Vm.debug("!" + "/");
				doit = true;
			}
		}
		if(foundTerm == false) scanpos--;
	}
	
	private void parseFactor(){
		//Vm.debug("In parsefactor");
		boolean foundFactor = false;
		getToken();
		//Vm.debug("**" + thisToken.token);
		////Vm.sleep(1500);
		if(thisToken.token.equals("sqrt")){
			foundFactor = true;
			if(runFlag) match("(");
			parseExpr();
			double a;
			a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
			CalcStack.removeElementAt(CalcStack.size()-1);
			a = java.lang.Math.sqrt(a);
			//Vm.debug("F:: " + a);
			//Vm.sleep(1500);
			CalcStack.add(new java.lang.Double(a));
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}
		if(thisToken.token.equals("cos") && foundFactor == false){
			foundFactor = true;
			if(runFlag) match("(");
			parseExpr();
			double a;
			a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
			CalcStack.removeElementAt(CalcStack.size()-1);
			a = java.lang.Math.cos(a);
			//Vm.debug("G:: " + a);
			//Vm.sleep(1500);
			CalcStack.add(new java.lang.Double(a));
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}
		if(thisToken.token.equals("sin") && foundFactor == false){
			foundFactor = true;
			if(runFlag) match("(");
			parseExpr();
			double a;
			a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
			CalcStack.removeElementAt(CalcStack.size()-1);
			a = java.lang.Math.sin(a);
			//Vm.debug("H:: " + a);
			//Vm.sleep(1500);
			CalcStack.add(new java.lang.Double(a));
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}
		if(thisToken.token.equals("tan") && foundFactor == false){
			foundFactor = true;
			if(runFlag) match("(");
			parseExpr();
			double a;
			a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
			CalcStack.removeElementAt(CalcStack.size()-1);
			a = java.lang.Math.tan(a);
			//Vm.debug("i:: " + a);
			//Vm.sleep(1500);
			CalcStack.add(new java.lang.Double(a));
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}		
		if(thisToken.token.equals("asin") && foundFactor == false){
			foundFactor = true;
			if(runFlag) match("(");
			parseExpr();
			double a;
			a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
			CalcStack.removeElementAt(CalcStack.size()-1);
			a = java.lang.Math.asin(a);
			//Vm.debug("J:: " + a);
			//Vm.sleep(1500);
			CalcStack.add(new java.lang.Double(a));
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}
		if(thisToken.token.equals("acos") && foundFactor == false){
			foundFactor = true;
			if(runFlag) match("(");
			parseExpr();
			double a;
			a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
			CalcStack.removeElementAt(CalcStack.size()-1);
			a = java.lang.Math.acos(a);
			//Vm.debug("K:: " + a);
			//Vm.sleep(1500);
			CalcStack.add(new java.lang.Double(a));
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}
		if(thisToken.token.equals("atan") && foundFactor == false){
			foundFactor = true;
			if(runFlag) match("(");
			parseExpr();
			double a;
			a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
			CalcStack.removeElementAt(CalcStack.size()-1);
			a = java.lang.Math.atan(a);
			CalcStack.add(new java.lang.Double(a));
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}

		if(thisToken.token.equals("crosstotal") && foundFactor == false){
			foundFactor = true;
			if(runFlag) match("(");
			parseExpr();
			double a;
			a = ((java.lang.Double)CalcStack.get(CalcStack.size()-1)).doubleValue();
			CalcStack.removeElementAt(CalcStack.size()-1);
			// Cross total = Quersumme berechnen
			String aString = Convert.toString(a); // 
			// bei 1.8e2 nur 1.8 verwenden 
			if (aString.toLowerCase().indexOf("e") > 0) aString = aString.substring(0, aString.toLowerCase().indexOf("e"));
			a=0;
			for (int i=0; i<aString.length(); i++) {
			 a += Convert.toDouble(Convert.toString(aString.charAt(i)));	
			}
			CalcStack.add(new java.lang.Double(a));
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}
		
		if(thisToken.token.equals("(") && foundFactor == false){
			foundFactor = true;
			parseExpr();
			if(runFlag) match(")");
			////Vm.debug("!" + ")");
		}
		if(IsAlpha(thisToken.token) && foundFactor == false){
			String result = new String();
			foundFactor = true;
			result = (String)symbolTable.get(thisToken.token);
			if(result != null){
				try {
					java.lang.Double test;
					//result = replace(result, ".", ",");
					////Vm.debug("testing var" + result);
					////Vm.sleep(1500);
					test = java.lang.Double.valueOf(result);
					////Vm.debug("going to add var" + result);
					////Vm.sleep(1500);
					//Vm.debug("l:: " + test);
					//Vm.sleep(1500);
					CalcStack.add(test);
					//Vm.debug("test good");
					//Vm.sleep(1500);
				} catch (NumberFormatException e){}
			} else {
				err("# Error: Variable " +thisToken.token+ " not defined");
			}
			
		}
		////Vm.debug("going to test if digit?");
		////Vm.sleep(1500);
		if(IsDigit(thisToken.token) && foundFactor == false){
			java.lang.Double test;
			try {
				////Vm.debug("testing isdigit: " +thisToken.token);
				////Vm.sleep(1500);
				test = java.lang.Double.valueOf(thisToken.token);
				//Vm.debug("m:: " + test);
				//Vm.sleep(1500);
				CalcStack.add(test);
			} catch (NumberFormatException e){}
			foundFactor = true;
		}
		if(foundFactor == false) err("# Error: Expected a number,variable or function function on line: " + thisToken.line + " position: " + thisToken.position);
		//Vm.debug("And out of parsfactor...");
		//Vm.sleep(500);
	}
		
	private void parseprogram(){
		emit_buffer = "# ";
		while(scanpos < TokenStack.size() && runFlag == true){
			////Vm.debug("In PP: " + scanpos);
			if(runFlag) parseCommand();
			if(runFlag) parseAssign();
			if(scanpos == TokenStack.size()-1) runFlag = false;
		}
	}
}
