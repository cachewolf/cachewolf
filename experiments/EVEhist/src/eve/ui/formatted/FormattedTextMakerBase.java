
package eve.ui.formatted;
import java.util.Vector;

//##################################################################
class FormattedTextMakerBase{
//##################################################################


final static String nbsp = ""+(char)0xa0;
static Object [] replacements = {"&gt;",">","&lt;","<","&nbsp;",nbsp,"&copy;",""+(char)169,"&quot;","\"","&amp;","&"};
static Object [] htmlReplacements = {"\r"," ","\n"," ","\t"," ","  "," "};
static Object [] preReplacements = {" ",nbsp};

static String amps =(char)34+"quot|"+(char)38+"amp|"+(char)60+"lt|"+(char)62+"gt|"+
(char)128+"euro|"+(char)130+"sbquo|"+(char)131+"fnof|"+(char)132+"bdquo|"+
(char)133+"hellip|"+(char)134+"dagger|"+(char)135+"Dagger|"+(char)136+"circ|"+
(char)137+"permil|"+(char)138+"Scaron|"+(char)139+"lsaquo|"+(char)140+"OElig|"+
(char)145+"lsquo|"+(char)146+"rsquo|"+(char)147+"ldquo|"+(char)148+"rdquo|"+
(char)149+"bull|"+(char)150+"ndash|"+(char)151+"mdash|"+(char)152+"tilde|"+
(char)153+"trade|"+(char)154+"scaron|"+(char)155+"rsaquo|"+(char)156+"oelig|"+
(char)159+"Yuml|"+(char)160+"nbsp|"+(char)161+"iexcl|"+(char)162+"cent|"+
(char)163+"pound|"+(char)164+"curren|"+(char)165+"yen|"+(char)166+"brvbar|"+
(char)167+"sect|"+(char)168+"uml|"+(char)169+"copy|"+(char)170+"ordf|"+
(char)171+"laquo|"+(char)172+"not|"+(char)173+"shy|"+(char)174+"reg|"+
(char)175+"macr|"+(char)176+"deg|"+(char)177+"plusmn|"+(char)178+"sup2|"+
(char)179+"sup3|"+(char)180+"acute|"+(char)181+"micro|"+(char)182+"para|"+
(char)183+"middot|"+(char)184+"cedil|"+(char)185+"sup1|"+(char)186+"ordm|"+
(char)187+"raquo|"+(char)188+"frac14|"+(char)189+"frac12|"+(char)190+"frac34|"+
(char)191+"iquest|"+(char)192+"Agrave|"+(char)193+"Aacute|"+(char)194+"Acirc|"+
(char)195+"Atilde|"+(char)196+"Auml|"+(char)197+"Aring|"+(char)198+"AElig|"+
(char)199+"Ccedil|"+(char)200+"Egrave|"+(char)201+"Eacute|"+(char)202+"Ecirc|"+
(char)203+"Euml|"+(char)204+"Igrave|"+(char)205+"Iacute|"+(char)206+"Icirc|"+
(char)207+"Iuml|"+(char)208+"ETH|"+(char)209+"Ntilde|"+(char)210+"Ograve|"+
(char)211+"Oacute|"+(char)212+"Ocirc|"+(char)213+"Otilde|"+(char)214+"Ouml|"+
(char)215+"times|"+	(char)216+"Oslash|"+(char)217+"Ugrave|"+(char)218+"Uacute|"+
(char)219+"Ucirc|"+	(char)220+"Uuml|"+(char)221+"Yacute|"+(char)222+"THORN|"+
(char)223+"szlig|"+	(char)224+"agrave|"+(char)225+"aacute|"+(char)226+"acirc|"+
(char)227+"atilde|"+(char)228+"auml|"+(char)229+"aring|"+(char)230+"aelig|"+
(char)231+"ccedil|"+(char)232+"egrave|"+(char)233+"eacute|"+(char)234+"ecirc|"+
(char)235+"euml|"+(char)236+"igrave|"+(char)237+"iacute|"+(char)238+"icirc|"+
(char)239+"iuml|"+(char)240+"eth|"+(char)241+"ntilde|"+(char)242+"ograve|"+
(char)243+"oacute|"+(char)244+"ocirc|"+(char)245+"otilde|"+(char)246+"ouml|"+
(char)247+"divide|"+(char)248+"oslash|"+(char)249+"ugrave|"+(char)250+"uacute|"+
(char)251+"ucirc|"+(char)252+"uuml|"+(char)253+"yacute|"+(char)254+"thorn|"+
(char)255+"yuml";


static Vector ampList;
static final String colors = "black000000green008000silverC0C0C0lime00FF00"+
"gray808080olive808000"+
"whiteFFFFFFyellowFFFF00"+
"maroon800000navy000080"+
"redFF0000blue0000FF"+
"purple800080teal008080"+
"fuchsiaFF00FFaqua00FFFF"; 
protected static final int LF_ORDERED_LIST = 0x80000000;
public static final int BOLD = 1;
public static final int ITALIC = 2;
public static final int UNDERLINE = 3;
public static final int HYPERLINK = 9;
public static final int ANCHOR = 10;
public static final int SPAN = 12;
public static final int FONT = 14;
public static final int BIG = 15;
public static final int SMALL = 16;
public static final int TELETYPE = 17;
public static final int EM = 18;
public static final int STRONG = 19;
public static final int DFN = 20;
public static final int IMAGE = 21;

//public static final int FIRST_SINGLE_LINE = 100;
//public static final int FIRST_BLOCK_LEVEL = 100;

public static final int BLOCK_LEVEL = 0x08000000;
public static final int STARTS_FRESH = 0x04000000;
public static final int IS_PARAGRAPH = 0x02000000;

public static final int CENTERED = 100|BLOCK_LEVEL;
public static final int HEADING1 = 101|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING2 = 102|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING3 = 103|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING4 = 104|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING5 = 105|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING6 = 106|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int TITLE = 107|BLOCK_LEVEL;
public static final int HR = 108|BLOCK_LEVEL;
public static final int PARAGRAPH = 109|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int DIV = 110|BLOCK_LEVEL;
public static final int PREFORMAT = 111|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int BLOCKQUOTE = 112|BLOCK_LEVEL;
public static final int BLOCKIMAGE = 113|BLOCK_LEVEL;
public static final int ADDRESS = 114|BLOCK_LEVEL;
public static final int BODY = 115|BLOCK_LEVEL;
public static final int TABLE = 116|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int TROW = 117|BLOCK_LEVEL;//|IS_PARAGRAPH;
public static final int TCELL = 118|BLOCK_LEVEL;//|IS_PARAGRAPH;
//public static final int LAST_SINGLE_LINE = 199;

public static final int TEST = 211|BLOCK_LEVEL;

public static final int UL = 212|BLOCK_LEVEL;
public static final int OL = 213|BLOCK_LEVEL;
public static final int ULI = 214|BLOCK_LEVEL|STARTS_FRESH;
public static final int OLI = 215|BLOCK_LEVEL|STARTS_FRESH;
public static final int DL = 216|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int DT = 217|BLOCK_LEVEL|STARTS_FRESH;
public static final int DD = 218|BLOCK_LEVEL;
//public static final int LAST_BLOCK_LEVEL = 299;

private static int[] theValues = {BOLD,ITALIC,UNDERLINE,HYPERLINK,ANCHOR,SPAN,FONT,BIG,SMALL,TELETYPE,EM,STRONG,DFN,IMAGE,BLOCK_LEVEL,STARTS_FRESH,IS_PARAGRAPH,CENTERED,HEADING1,HEADING2,HEADING3,HEADING4,HEADING5,HEADING6,TITLE,HR,PARAGRAPH,DIV,PREFORMAT,BLOCKQUOTE,BLOCKIMAGE,ADDRESS,BODY,TABLE,TROW,TCELL,TEST,UL,OL,ULI,OLI,DL,DT,DD};
private static String[] theNames = {"BOLD","ITALIC","UNDERLINE","HYPERLINK","ANCHOR","SPAN","FONT","BIG","SMALL","TELETYPE","EM","STRONG","DFN","IMAGE","BLOCK_LEVEL","STARTS_FRESH","IS_PARAGRAPH","CENTERED","HEADING1","HEADING2","HEADING3","HEADING4","HEADING5","HEADING6","TITLE","HR","PARAGRAPH","DIV","PREFORMAT","BLOCKQUOTE","BLOCKIMAGE","ADDRESS","BODY","TABLE","TROW","TCELL","TEST","UL","OL","ULI","OLI","DL","DT","DD"};
public String valueToName(int value)
{
for (int i = 0; i<theValues.length; i++)
	if (theValues[i] == value) return theNames[i];
return null;
}

//##################################################################
}
//##################################################################

