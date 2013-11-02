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
package CacheWolf;

import ewe.util.Vector;

/**
 * Class to tokenise (break up) the code into single tokens, so the
 * parser my do its job.
 * 
 * @see Parser
 */
public class Tokenizer {

	/**
	 * Normally only a semicolon (;) can be used to separate commands. If this variable is set to true,
	 * newlines also terminate a command. If a newline is preceded with a backslash (=line continuation character),
	 * the newline does not terminate the command even if this variable is true.
	 */
	public boolean newLineIsSeparator = true;
	/** instructions to tokenise */
	String mySource;
	/** source character */
	char look;
	/** pointer to next character to read */
	int sourcePointer = 0;
	/** (partial) token */
	String currentStream;
	Vector TokenStack = new Vector();
	/** position of token */
	int currentLine, currentPos;
	TokenObj thisToken;
	Vector messageStack;

	public Tokenizer() { // Public constructor
	}

	private void err(String str) throws Exception {
		messageStack.add(MyLocale.getMsg(1700, "Error on line: ") + currentLine + "  " + MyLocale.getMsg(1701, " position: ") + currentPos);
		messageStack.add(str);
		Global.mainTab.solverPanel.setSelectionRange(0, currentLine - 1, currentPos, currentLine - 1);
		throw new Exception("Error " + str);
	}

	private boolean isAlpha(char c) {
		return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(c) != -1;
	}

	private boolean isDigit(char c) {
		return "0123456789".indexOf(c) != -1;
	}

	private boolean isSymbol(char c) {
		return "?!<>(){}*/,;^+-=".indexOf(c) != -1;
	}

	/**
	 * Convert Unicode version of special chars to normal
	 * 
	 * @param c
	 *            Char to convert
	 * @return Converted char
	 */
	private char standardiseSourceChar(char c) {
		if (c == '\u00A0' || (c >= '\u2002' && c <= '\u200b'))
			c = ' ';
		if (c >= '\u2010' && c <= '\u2015')
			c = '-';
		if (c >= '\u201c' && c <= '\u201f')
			c = '"';
		if (c == '[')
			c = '(';
		if (c == ']')
			c = ')';
		if (c == '\u00f7' || c == '\u2044')
			c = '/';
		if (c == '\u2024')
			c = '.';
		return c;
	}

	private boolean getChar() {
		if (sourcePointer >= mySource.length()) {
			look = '\n';
			return false;
		}
		look = mySource.charAt(sourcePointer++);
		currentPos++;
		return true;
	}

	private char lookAhead() {
		if (sourcePointer >= mySource.length())
			return '\n';
		else {
			char c = standardiseSourceChar(mySource.charAt(sourcePointer));
			return c;
		}
	}

	private void backUp() {
		sourcePointer--;
		currentPos--;
	}

	/**
	 * Create a new token object and remember the place where it started.
	 * String tokens could span several lines, so we need to remember the starting line and position.
	 */
	private void startToken() {
		thisToken = new TokenObj();
		thisToken.line = currentLine;
		thisToken.position = currentPos;
	}

	/** Add the previously started token to the token stack */
	private void emitToken(int tt) {
		thisToken.token = currentStream;
		thisToken.tt = tt;
		TokenStack.add(thisToken);
		currentStream = "";
	}

	private void streamAlphas() {
		startToken();
		while (getChar()) {
			if (isAlpha(look) || isDigit(look))
				currentStream += look;
			else
				break;
		}
		String s = currentStream.toUpperCase();
		if (s.equals("STOP") || s.equals("ST"))
			emitToken(TokenObj.TT_STOP);
		else if (s.equals("IF"))
			emitToken(TokenObj.TT_IF);
		else if (s.equals("THEN"))
			emitToken(TokenObj.TT_THEN);
		else if (s.equals("ENDIF") || s.equals("FI")) {
			currentStream = "ENDIF";
			emitToken(TokenObj.TT_ENDIF);
		}
		else
			emitToken(TokenObj.TT_VARIABLE);
		// We have read one character too far, so back off
		backUp();
	}

	private void streamDigits() {
		boolean foundDecSep = false; // To check that only one decimal point is allowed in a number
		startToken();
		while (getChar()) {
			look = standardiseSourceChar(look);
			if (isDigit(look) || (look == '.' && !foundDecSep)) {
				currentStream += look;
				if (look == '.')
					foundDecSep = true;
			}
			else
				break;
		}
		emitToken(TokenObj.TT_NUMBER);
		// We have read one character too far, so back off
		backUp();
	}

	private void streamString() throws Exception {
		startToken();
		currentStream = "";
		while (getChar()) { // collect chars until next "
			if (look == '"') {
				if (lookAhead() != '"')
					break; // " not followed by " => End of string
				// Two " following each other are replaced by " 
				currentStream += "\"";
				getChar();
			}
			else if (look == '\\') {
				if (!getChar())
					break;
				if (look == 'n')
					currentStream += "\n";
				else
					currentStream += look;
			}
			else
				currentStream += look;
			// Need to count newlines inside a string spanning multiple lines so that we don't loose track
			if (look == '\n') {
				currentLine++;
				currentPos = 0;
			}
		} // EOT or look=="
		if (look != '"') {
			// Restore start position of string for correct indication of error
			currentLine = thisToken.line;
			currentPos = thisToken.position;
			err(MyLocale.getMsg(1730, "Unterminated string"));
		}
		emitToken(TokenObj.TT_STRING);

	}

	private void streamSymbol() {
		startToken();
		// Check for == != <= >= <> >< 
		if (look == '=' || look == '!' || look == '<' || look == '>') {
			getChar();
			currentStream += look;
			if (currentStream.equals("==")) {
				emitToken(TokenObj.TT_EQ);
				return;
			}
			if (currentStream.equals("!=") || currentStream.equals("><") || currentStream.equals("<>")) {
				emitToken(TokenObj.TT_NE);
				;
				return;
			}
			if (currentStream.equals("<=")) {
				emitToken(TokenObj.TT_LE);
				return;
			}
			if (currentStream.equals(">=")) {
				emitToken(TokenObj.TT_GE);
				return;
			}
			backUp(); // Not a valid comparison symbol, forget the last character
			currentStream = currentStream.substring(0, 1);
			if (currentStream.equals("="))
				emitToken(TokenObj.TT_EQ);
			else if (currentStream.equals("<"))
				emitToken(TokenObj.TT_LT);
			else if (currentStream.equals(">"))
				emitToken(TokenObj.TT_GT);
			else
				emitToken(TokenObj.TT_SYMBOL);
		}
		else
			emitToken(TokenObj.TT_SYMBOL);
	}

	/** Eat up all characters until next newline as we are in a comment */
	private void eatUpComment() {
		while (getChar() && look != '\n')
			;
		currentStream = ";"; // Insert a dummy ;
		startToken();
		emitToken(TokenObj.TT_SYMBOL);
		currentStream = "";
		currentLine++;
		currentPos = 0;
	}

	private void formatString() throws Exception {
		currentStream = "";
		startToken();
		while (getChar() && look != ':') {
			look = standardiseSourceChar(look);
			currentStream += look;
			if (look != '.' && look != '0' && look != '#')
				err(MyLocale.getMsg(1731, "Invalid format character"));
		}
		emitToken(TokenObj.TT_FORMATSTR);
	}

	public void tokenizeSource(String src, Vector msg) {
		mySource = src + "\n";
		sourcePointer = 0;
		TokenStack.clear();
		messageStack = msg;
		currentLine = 1;
		currentPos = 0;
		currentStream = "";
		try {
			while (getChar()) {
				look = standardiseSourceChar(look);
				if (look == ' ')
					continue;
				currentStream += look;
				if (isAlpha(look) || look == '$')
					streamAlphas();
				else if (isDigit(look))
					streamDigits();
				else if (isSymbol(look))
					streamSymbol();
				else if (look == '"')
					streamString();
				else if (look == '\n') {
					if (newLineIsSeparator && !currentStream.equals("\\\n") && !currentStream.equals("_\n")) {
						currentStream = ";";
						startToken();
						emitToken(TokenObj.TT_SYMBOL);
					}
					currentStream = "";
					currentLine++;
					currentPos = 0;
				}
				else if (look == '#')
					eatUpComment(); // Ignore characters until EOL
				else if (look == ':')
					formatString();
				else if (newLineIsSeparator && (look == '\\' || look == '_'))
					;
				else
					err(MyLocale.getMsg(1732, "Invalid character"));
			}
		}
		catch (Exception e) {
			// Global.pref.log("Ignored Exception", e, true);
		}
	}
}
