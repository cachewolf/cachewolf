package CacheWolf;

import ewe.util.*;
import ewe.sys.*;

/**
*	Class to tokenize (break up) the code into single tokens, so the
*	parser my do its job.
*	@see Parser
*/
public class Tokenizer{

	String mySource = new String();
	char Look;
	int SourcePointer = 0;
	String CurrentStream = new String();
	Vector TokenStack = new Vector();
	int CurrentLine, CurrentPos;
	TokenObj thisToken;
	
	public Vector getStack(){
		TokenObj to = new TokenObj();
		
		for(int i = 0; i < TokenStack.size(); i++){
			to = (TokenObj)TokenStack.get(i);
			//Vm.debug("Tock: " + to.token);
		}
		return TokenStack;
	}
	
	public Tokenizer(){
	}

	public void setSource(String src){
		mySource = src;
		mySource = mySource + "\n";
		SourcePointer = 0;
		TokenStack = new Vector();
		//System.out.println("Source set to: " + mySource);
	}
	
	private boolean IsAlpha(char c){
		String t = new String();
		t = t + c;
		t = t.toUpperCase();
		if("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(t) != -1) return true;
		else return false;
	}
	
	private boolean IsDigit(char c){
		String t = new String();
		t = t + c;
		t = t.toUpperCase();
		if("0123456789.".indexOf(t) != -1) return true;
		else return false;
	}
	
	private boolean getChar(){
		Look = mySource.charAt(SourcePointer);
		SourcePointer++;
		if(SourcePointer >= mySource.length()) return true; // final reached
		else return false;
	}
	
	private boolean IsSymbol(char c){
		boolean retval = false;
		if(c == '+') retval = true;
		if(c == '-') retval = true;
		if(c == '=') retval = true;
		return retval;
	}
	
	private void emit(){
		//System.out.println(CurrentStream);
		thisToken = new TokenObj();
		thisToken.token = CurrentStream;
		thisToken.line = CurrentLine;
		thisToken.position = CurrentPos;
		TokenStack.add(thisToken);
		CurrentStream = "";
	}

	private void streamAlphas(){
		
		while(getChar() == false){
			CurrentPos++;
			if(IsAlpha(Look) == false) break;
			else CurrentStream += Look;
		} 
		SourcePointer--;
		CurrentPos--;
		Look = ' ';
	}
	
	private void streamDigits(){
		while(getChar() == false){
			CurrentPos++;
			if(IsDigit(Look) == false) break;
			CurrentStream += Look;
		}
		SourcePointer--;
		CurrentPos--;
		Look = ' ';
	}
	
	private void streamSymbols(){
		while(getChar() == false){
			CurrentPos++;
			if(IsSymbol(Look) == false) break;
			CurrentStream += Look;
		}
		SourcePointer--;
		CurrentPos--;
		Look = ' ';
	}

	private void streamString(){
		while(getChar() == false && Look != '\"'){
			CurrentPos++;
			CurrentStream += Look;
		}
	}
	
	public void TokenIt(){
		boolean inComment = false;
		CurrentLine = 1;
		CurrentPos = 0;
		while(getChar() == false){
			CurrentStream += Look;
			CurrentPos++;
			if(inComment == false){	
				if(IsAlpha(Look)) {streamAlphas();emit();}
				if(IsDigit(Look)) {streamDigits();emit();}
				if(Look == ')') emit();
				if(Look == '(') emit();
				if(Look == '{') emit();
				if(Look == '}') emit();
				if(Look == '*') emit();
				if(Look == '/') emit();
				if(Look == ';') emit();
				if(Look == '\"') emit();
				if(Look == '^') emit();
				if(Look == '+') {streamSymbols();emit();}
				if(Look == '-') {streamSymbols();emit();}
				if(Look == '=') {streamSymbols();emit();}
				if(Look == ' ') {CurrentStream = "";}
				if(Look == '\"') {
					emit();
					streamString();
					emit();
					CurrentStream += Look;
					emit();
					Look = ' ';
				}
			}
			if(Look == '\n') {CurrentStream = "";CurrentPos = 0; CurrentLine++; inComment = false;}
			if(Look == '#') {inComment = true;}			
		}
	}
}
