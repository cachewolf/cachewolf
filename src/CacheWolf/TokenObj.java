    /*
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



/**
*	Class to hold a token object.
*	@see Tokenizer
*	@see Parser
*/
public class TokenObj{
	/** Token types */
	public static final int TT_VARIABLE=0;
	public static final int TT_STRING=1;
	public static final int TT_NUMBER=2;
	public static final int TT_SYMBOL=3;
	public static final int TT_FORMATSTR=4;
	public static final int TT_IF=5;
	public static final int TT_THEN=6;
	public static final int TT_ENDIF=7;
	public static final int TT_STOP=8;
	public static final int TT_OPENBRACKET=9;
	public static final int TT_CLOSEBRACKET=10;
	public static final int TT_LT=20;   // Don't change the sequence from LT to NT 
	public static final int TT_GT=21;
	public static final int TT_LE=22;
	public static final int TT_GE=23;
	public static final int TT_EQ=24;
	public static final int TT_NE=25;
	
	int tt; // Tokentype
	String token;
	int line, position;
}
