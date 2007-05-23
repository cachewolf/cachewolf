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
