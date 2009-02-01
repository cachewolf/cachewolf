
package eve.ui.formatted;
import java.util.Hashtable;
import java.util.Vector;

import eve.data.MutableTreeNodeObject;
import eve.data.PropertyList;
import eve.fx.Color;
import eve.fx.Dimension;
import eve.fx.DisplayLine;
import eve.fx.DisplayLineSpecs;
import eve.fx.Font;
import eve.fx.FormattedTextSpecs;
import eve.fx.IImage;
import eve.fx.ImageDecoder;
import eve.fx.PixelBuffer;
import eve.io.File;
import eve.sys.Convert;
import eve.sys.Event;
import eve.sys.EventListener;
import eve.sys.Gate;
import eve.sys.Handle;
import eve.sys.Task;
import eve.sys.TimeOut;
import eve.sys.Vm;
import eve.sys.YieldToEvents;
import eve.sys.mThread;
import eve.ui.Application;
import eve.ui.ReportException;
import eve.ui.TextPad;
import eve.ui.event.ControlEvent;
import eve.util.ByteArray;
import eve.util.CharArray;
import eve.util.FormattedDataSource;
import eve.util.Range;
import eve.util.SubString;
import eve.util.TagList;
import eve.util.Utils;
import eve.util.mString;
/**
* This is used to build up formatted text - via add() or parseHtml(), which
* is then added to a TextDisplay.
**/
//##################################################################
public class FormattedTextMaker extends FormattedTextMakerBase implements EventListener, ImageResolver{
//##################################################################

CharArray text = new CharArray();
Vector entries = new Vector();
Vector creating = new Vector();

//===================================================================
public FormattedTextMaker()
//===================================================================
{
	//rmatters.set(TEST,new TestFormatter());
}
//===================================================================
public void add(String toAdd)
//===================================================================
{
	add(Vm.getStringChars(toAdd),0,toAdd.length());
}
//===================================================================
public void add(char []toAdd,int start,int length)
//===================================================================
{
	//if (!stopAdding)
	text.append(toAdd,start,length);
}

boolean inPreFormat = false;

/*
//===================================================================
int indexOf(char[] data,int start,int length,char[] look,int lookStart,int lookLength,int startIndex)
//===================================================================
{
	if (lookLength > length) return -1;
	for (int s = startIndex; s<length; s++){
		int idx = SubString.indexOf(look[0],data,start,length,s,0);
		if (idx == -1) return -1;
		if (SubString.equals(data,idx,length-idx,look,lookStart,lookLength,SubString.STARTS_WITH)) return idx;
		s = idx;
	}
	return -1;
}
boolean debug = false;
*/
SubString replaced = new SubString();
//===================================================================
void doReplacements(SubString data,Object [] replacers)
//===================================================================
{
	if (replacers.length == 0) return;
	if (replacers[0] instanceof String)
		for (int i = 0; i<replacers.length; i++)
			replacers[i] = Vm.getStringChars((String)replacers[i]);
	for (int i = 0; i<replacers.length; i+=2)
		data.replace((char [])replacers[i],(char [])replacers[i+1]);
}
//===================================================================
public void addHtml(String toAdd)
//===================================================================
{
	if (ampList == null)
		new SubString().set(amps).split('|',(ampList = new Vector()),0);
	if (replaced.data == null || replaced.data.length < toAdd.length())
		replaced.data = new char[toAdd.length()];
	mString.copyInto(toAdd,replaced.data,0);
	replaced.length = toAdd.length();
	replaced.start = 0;
	//doReplacements(replaced,replacements);
	char [] chars = replaced.data;
	int end = replaced.start+replaced.length;
	int start = replaced.start;
	for (int i = start; i<end; i++){
		if (chars[i] == '&'){
			int e = i+1;
			for (; e<end; e++)
				if (chars[e] == ';') break;
			if (e == end) break;
			char toSub = '?';
			if (chars[i+1] == '#')
				toSub = (char)Convert.toInt(new String(chars,i+2,e-i-2));
			else{
				int max = ampList.size();
				for (int j = 0; j<max; j++){
					SubString s = (SubString)ampList.get(j);
					if (SubString.equals(chars,i+1,e-(i+1),s.data,s.start+1,s.length-1,SubString.IGNORE_CASE))
						toSub = s.data[s.start];
				}
			}
			if (e != end-1) System.arraycopy(chars,e+1,chars,i+1,end-(e+1));
			chars[i] = toSub;
			end -= (e+1)-(i+1);
		}
	}	
	replaced.length = end-replaced.start;
	if (inPreFormat) {
		for (int i = start; i<end; i++)
			if (chars[i] == ' ') chars[i] = 160;
			else if (chars[i] == '\r') chars[i] = ' ';
		//doReplacements(replaced,preReplacements);
	}else {
		//doReplacements(replaced,htmlReplacements);
		for (int i = start; i<end; i++){
			char c = chars[i];
			if (c <= 27) {
				chars[i] = ' ';
			}
		}
		int first = -1;
		for (int i = start; i<end; i++){
			if (chars[i] == ' '){
				if (first == -1) first = i;
			}else{
				if (first != -1 && first != i-1){
					System.arraycopy(chars,i,chars,first+1,end-i);
					end -= i-first-1;
				}
				first = -1;
			}
		}
		if (first != -1) end = first+1;
		replaced.length = end-replaced.start;
		
		if (replaced.length > 0){
			if (replaced.data[replaced.start] == ' '){
				char c = text.length == 0 ? '\n' : text.data[text.length-1];
				if (c == '\n' || c == ' ') {
					replaced.start++;
					replaced.length--;
				}
			}
		}
	}
	if (replaced.length != 0){
		add(replaced.data,replaced.start,replaced.length);
		startFresh = endedParagraph = endedTag = false;
	}
}

//===================================================================
public void lineBreak()
//===================================================================
{
	//if (cancel(PARAGRAPH) != null) addBlankLine();
	//else 
		add("\n");
}
//===================================================================
public void conditionalLineBreak()
//===================================================================
{
	//if (cancel(PARAGRAPH) != null) addBlankLine();
	//else{
		if (text.length == 0) return;
		else if (text.data[text.length-1] == '\n') return;
		add("\n");
	//}
}
//===================================================================
public void addBlankLine()
//===================================================================
{
	if (text.length != 0 && text.data[text.length-1] == '\n'){
		if (text.length == 1) ;
		else if (text.data[text.length-2] != '\n')
			add("\n");
	}else if (text.length == 0) add("\n");
	else add("\n\n");
}
//===================================================================
Color toColor(Object colorOrName)
//===================================================================
{
	return toColor(colorOrName,Color.Black);
}
//===================================================================
Color toColor(Object colorOrName,Color defaultColor)
//===================================================================
{
	if (colorOrName instanceof Color) return ((Color)colorOrName);
	String color = (colorOrName instanceof String) ? (String)colorOrName : null;
	if (color == null || color.length() == 0) return defaultColor;
	int col = 0, max = color.length();
	if (color.charAt(0) != '#'){
		color = color.toLowerCase();
		int where = colors.indexOf(color);
		if (where == -1) color = '#'+color;//return Color.Black;
		else try{
			color = '#'+colors.substring(where+color.length(),where+color.length()+6);
		}catch(Exception e){
			return defaultColor;
		}
	}
	max = color.length();
	color = color.toUpperCase();
	for (int i = 1; i<max; i++){
		char c = color.charAt(i);
		if (c >= '0' && c <= '9')
			col = col*16+(c-'0');
		else if (c >= 'A' && c <= 'F')
			col = col*16+10+(c-'A');
		else
			return defaultColor;
	}
	return new Color((col >> 16) & 0xff,(col >> 8) & 0xff, col & 0xff);
}


//===================================================================
String findStyle(char [] source,String styleName)
//===================================================================
{
	if (source == null || styleName == null) return null;
	char [] look = Vm.getStringChars(styleName);
	
	for (int i = 0;;i++){
		i = SubString.indexOf(':',source,0,source.length,i,0);
		//Vm.debug("Look: "+styleName+" at "+i);
		if (i == -1) return null;
		if (i == 0) continue;
		int e, s;
		for (e = i-1; e>=0 && Character.isWhitespace(source[e]); e--)
			;
		if (e<0) continue;
		for (s = e; s>=0 && source[s] != ';' && !Character.isWhitespace(source[s]); s--)
			;
		s++;
		int len = e-s+1;
		if (!SubString.equals(look,0,look.length,source,s,len,SubString.IGNORE_CASE))
			continue;
		//
		//Vm.debug("Found: "+styleName);
		//
		for (s = i+1; s<source.length && Character.isWhitespace(source[s]); s++)
			;
		if (s >= source.length) return null;
		if (source[s] == '"' || source[s] == '\''){
			char eq = source[s++];
			for (e = s; e<source.length && source[e] != eq; e++)
				;
			e--;
		}else{
			for (e = s; e<source.length && source[e] != ';' && !Character.isWhitespace(source[e]); e++)
				;
			e--;
		}
		if (e<s) return "";
		else return new String(source,s,e-s+1);
	}
}

//===================================================================
int nextProperty(int start,char [] source,PropertyList list)
//===================================================================
{
	int ei = -1;
	for (int i = start; i<source.length; i++)
		if (source[i] == '=') {
			ei = i;
			break;
		}
	if (ei == -1) return -1;
	String name = new String(source,start,ei-start).trim().toLowerCase();
	char quote = 0;
	int si = ei+1;
	if (si < source.length && (source[si] == '"' || source[si] == '\''))
		quote = source[si++];
	for (ei = si; ei<source.length; ei++){
		char c = source[ei];
		if (((c == ' ' || c == '\n' || c == '\t')&& quote == 0) || c == quote) break;
	}
	if (ei > si){
		String value = new String(source,si,ei-si);
		if (name.equals("color")){
			list.add(name,toColor(value));
		}else
			list.add(name,value);
	}	
	return quote == 0 ? ei : ei+1;
}

//===================================================================
PropertyList toProperties(String attributes)
//===================================================================
{
	if (attributes == null || attributes.length() == 0) return null;
	char [] source = Vm.getStringChars(attributes);
	PropertyList pl = new PropertyList();
	for (int i = nextProperty(0,source,pl); i != -1; i = nextProperty(i,source,pl))
		;
	String name = pl.getString("id",null);
	if (name != null) pl.set("name",name);
	return pl;
}

void printTree()
{
	String depth = "";
	for (Entry et = curEntry; et != null; et = (Entry)et.getParent())
		depth += "->"+valueToName(et.type);
	Vm.debug(depth);
}

int maxEntries = 0, nowEntries = 0;
//-------------------------------------------------------------------
Entry addType(int type,PropertyList attributes)
//-------------------------------------------------------------------
{
	Entry e = new Entry();
	e.type = type;
	e.startIndex = text.length;
	e.attributes = attributes;
	creating.add(e);
	curEntry.addChild(e);
	curEntry = e;
	nowEntries++;
	if (nowEntries > maxEntries) {
		//Vm.debug("Max: "+nowEntries);
		//if (true || nowEntries == 50){
			//printTree();
		//}
		maxEntries = nowEntries;
	}
	return e;
}
TagList formatters = new TagList();

//-------------------------------------------------------------------
protected void setupHotspot(HotSpot hs)
//-------------------------------------------------------------------
{
	//hs.color = null;
	//hs.fontFlags |= hs.FONT_CHANGE|Font.UNDERLINE;
}
//-------------------------------------------------------------------
Entry findLast(int type,boolean removeIt)
//-------------------------------------------------------------------
{
	int cs = creating.size()-1;
	for (int i = cs; i >= 0; i--){
		Entry e = (Entry)creating.get(i);
		if (e.type == type) {
			e.length = text.length-e.startIndex;
			//if (e.length == 0 && type == HYPERLINK) e.length = 1;
			TextFormatter tf = (TextFormatter)formatters.getValue(type,null);
			String anchor = null;
			if (tf == null){
				if (type == HYPERLINK && e.attributes != null){
					tf = new HotSpot();
					if ((anchor = PropertyList.getString(e.attributes,"href",null)) != null){
						tf.data = anchor;
						tf.toolTip = PropertyList.getString(e.attributes,"title",null);
						setupHotspot((HotSpot)tf);
						
					}else if ((anchor = e.attributes.getString("name",null)) != null){
						tf.data = "!"+anchor;
						tf.fontFlags = 0;
						tf.cursor = 0;
						tf.color = null;
					}
				}
				if (tf == null) tf = new TextFormatter();
				gatherFormats(e,tf);
			}
			e.formatter = tf;
			//
			/*
			if (i != cs){
				while(true){
					int j = creating.size()-1;
					if (j <= i) break;
					int ty = ((Entry)creating.get(j)).type;
					Vm.debug("Force ending: "+valueToName(ty));
					end(ty);
				}
			}
			*/
			if (removeIt) creating.remove(e);
			//
			// If any of the children are still open, then consider them my siblings instead.
			//
			/*
			if (e.getParent() != null){
				int max = e.getChildCount();
				for (int c = 0; c<
			}
			*/
			return e;
		}
	}
	return null;
}

//-------------------------------------------------------------------
Color getStyleColor(char [] styles,String name)
//-------------------------------------------------------------------
{
	String c = findStyle(styles,name);
	if (c == null) return null;
	return toColor(c,null);
}

//===================================================================
public void setFormatFlags(Entry myEntry,TextFormatter tf, Entry e,boolean isFirst,PropertyList attributes)
//===================================================================
{
	attributes = PropertyList.toPropertyList(attributes);
	PropertyList ea = PropertyList.toPropertyList(e.attributes);
	String style = ea.getString("style",null);
	char [] st = style == null ? null : Vm.getStringChars(style);
	
	if (st != null){
		if ((tf.fontFlags & tf.FONT_FREEZE) == 0){
			Color color = getStyleColor(st,"color");
			if (color != null) {
				tf.fontFlags |= tf.FONT_CHANGE;
				tf.color = color;
			}
		}
		Color back = getStyleColor(st,"background");
		if (back != null) {
			tf.backgroundColor = back;
			tf.lineFlags |= tf.LINE_BACKGROUND_COLOR_CHANGE;
		}
	}
	
		switch(e.type){
			case HYPERLINK:
				if (PropertyList.getString(e.attributes,"href",null) != null){
					if (tf.color == null || (tf.fontFlags & tf.FONT_FREEZE) == 0) {
						tf.color = (Color)bodyData.getValue("link",HotSpot.hotColor);
					}
					tf.fontFlags |= tf.FONT_CHANGE|Font.UNDERLINE;
				}
				break;
			case HR:
				tf.lineFlags |= tf.LINE_DRAW_RULE;
				break;
			case STRONG:
			case BOLD:
				tf.fontFlags |= tf.FONT_CHANGE|Font.BOLD;
				break;
			case DFN:
			case EM:
			case ITALIC:
				tf.fontFlags |= tf.FONT_CHANGE|Font.ITALIC;
				break;
			case UNDERLINE:
				tf.fontFlags |= tf.FONT_CHANGE|Font.UNDERLINE;
				break;
			case CENTERED:
				if (isFirst){
					tf.lineFlags |= tf.LINE_CENTERED;
				}
				break;
			case BLOCKQUOTE:
				if (isFirst){
					tf.lineFlags |= tf.LINE_RESPLIT;
					tf.leftMarginShift += 40;
				}
				break;
				
			case TITLE:
				tf.lineFlags |= tf.LINE_RIGHT_ALIGNED;
				tf.fontFlags |= tf.FONT_CHANGE|Font.ITALIC;
				break;
				
			case TELETYPE:
				tf.fontFlags |= tf.FONT_CHANGE|tf.FONT_FREEZE;
				tf.fontName = Application.findFont("fixed",true).getName();
				break;
			case BIG:
				tf.fontFlags |= tf.FONT_CHANGE;
				tf.fontSizeChange += 2;
				break;
			case SMALL:
				tf.fontFlags |= tf.FONT_CHANGE;
				tf.fontSizeChange -= 4;
				break;
			case FONT:
				if ((tf.fontFlags & tf.FONT_FREEZE) != 0) break;
				tf.fontFlags |= tf.FONT_CHANGE|tf.FONT_FREEZE;
				Color c = (Color)ea.getValue("color",null);
				if (c != null) tf.color = c;
				String face = ea.getString("face",null);
				if (face != null) tf.fontName = face;
				Object size = ea.getValue("size",null);
				if (size instanceof String){
					String siz = (String)size;
					if (siz.charAt(0) == '+' || siz.charAt(0) == '-'){
						int sz = Convert.toInt(siz.substring(1));
						if (siz.charAt(0) == '-') sz *= -1;
						ea.set("sizeChange",new Integer(sz*2));
						size = null;
					}else{
						int sz = Convert.toInt(siz);
						ea.set("size",size = new Integer(sz*2+12));
					}
				}
				if (size instanceof Integer){
					tf.fontSize = ((Integer)size).intValue();
				}
				tf.fontSizeChange = ea.getInt("sizeChange",0);
				break;
				
			case PREFORMAT:
				tf.fontFlags |= tf.FONT_CHANGE|tf.FONT_FREEZE;
				tf.fontName = Application.findFont("fixed",true).getName();
				if (isFirst){
					tf.lineFlags |= tf.LINE_RESPLIT;
				}
				break;
			case DD:
				if (isFirst){
					tf.lineFlags |= tf.LINE_RESPLIT;
					tf.leftMarginShift += 40;
				}
				break;
			case OLI:
			case ULI:
				if (isFirst){
					tf.lineFlags |= tf.LINE_RESPLIT|tf.LINE_HEADER_RIGHT_ALIGN|tf.LINE_ADD_FIRST_LINE_HEADER;
					tf.leftMarginShift += 40;
				}
				break;
			case SPAN:
				break;
			case TABLE:
				if (isFirst)
					tf.lineFlags |= tf.LINE_CANCEL_ALIGNMENTS;
					//break;
			case TROW:
			case TCELL:
				if (isFirst){
					String bg = ea.getString("bgcolor",null);
					if (bg != null){
						Color col = toColor(bg,null);
						if (col != null) {
							tf.lineFlags |= tf.LINE_BACKGROUND_COLOR_CHANGE;
							tf.backgroundColor = col;
						}
					}
				}
				break;
			case DIV:
			case PARAGRAPH:
				String align = PropertyList.getString(e.attributes,"align","").toLowerCase();
				if (isFirst){
					if (align.equals("right")){
						tf.lineFlags |= tf.LINE_RIGHT_ALIGNED;
					}else if (align.equals("center")){
						tf.lineFlags |= tf.LINE_CENTERED;
					}
				}
				break;
			case BODY:
				/*
				Object tc = PropertyList.getValue(e.attributes,"text",null);
				if (tc != null){
					Color color = toColor(tc);
					if (color != null){
						tf.fontFlags |= tf.FONT_CHANGE;
						tf.color = color;
						if (e.attributes != null) e.attributes.set("text",color);
					}
				}
				*/
				break;
				
			case TEST:
				tf.fontFlags |= tf.FONT_CHANGE;
				tf.fontName = Application.findFont("fixed",true).getName();
				if (isFirst){
					tf.lineFlags |= tf.LINE_RESPLIT;
					tf.lineFlags |= tf.LINE_RIGHT_ALIGNED;
				}
				break;
			case ADDRESS:
				tf.fontFlags |= tf.FONT_CHANGE|Font.ITALIC;
				break;
			case HEADING1:
					tf.fontFlags |= tf.FONT_CHANGE|Font.BOLD;
					tf.fontSizeChange = 2;
					break;
			case HEADING2:
					tf.fontFlags |= tf.FONT_CHANGE|Font.BOLD;
					tf.fontSizeChange = 1;
					break;
			case HEADING3:
					tf.fontFlags |= tf.FONT_CHANGE|Font.BOLD;
					break;
			case HEADING4: case HEADING5: case HEADING6:
					tf.fontFlags |= tf.FONT_CHANGE|Font.UNDERLINE;
					break;
	}
	if (e.type >= HEADING1 && e.type <= HEADING6){
		String align = PropertyList.getString(e.attributes,"align","").toLowerCase();
		if (isFirst){
			if (align.equals("right"))
				tf.lineFlags |= tf.LINE_RIGHT_ALIGNED;
			else if (align.equals("center")) 
				tf.lineFlags |= tf.LINE_CENTERED;
		}
	}
}
//===================================================================
void gatherFormats(Entry e,TextFormatter tf)
//===================================================================
{
	int idx = creating.indexOf(e);
	for (int i = idx; i >= 0; i--){
		Entry et = (Entry)creating.get(i);
		setFormatFlags(e,tf,et,i == idx,e.attributes);
		//if (et.type >= CENTERED) break;
	}
}
//===================================================================
public Entry start(int type)
//===================================================================
{
	return addType(type,null);
}
//===================================================================
public Entry start(int type,PropertyList attributes)
//===================================================================
{
	//if (type >= FIRST_SINGLE_LINE && type <= LAST_SINGLE_LINE) conditionalLineBreak();
	if ((type & BLOCK_LEVEL) != 0){
		end(PARAGRAPH);
		//addBlankLine();
		conditionalLineBreak();
		//if (((type & IS_PARAGRAPH) != 0) && (endedParagraph||!endedTag)) addBlankLine();
		if (((type & IS_PARAGRAPH) != 0) && !startFresh) addBlankLine();
	}
	endedParagraph = false;
	if ((type & STARTS_FRESH) != 0) startFresh = true;
	return addType(type,attributes);
}

Entry lastEntry = null;
boolean endedParagraph = false;
boolean startFresh = false;
boolean endedTag = false;
//===================================================================
public Entry end(int type)
//===================================================================
{
	Entry e = findLast(type,true);
	if (e == null) return null;
	entries.add(e);
	//if (type >= FIRST_SINGLE_LINE && type <= LAST_SINGLE_LINE) conditionalLineBreak();
	if ((type & BLOCK_LEVEL) != 0){
		end(PARAGRAPH);
		conditionalLineBreak();
	}
	//if (type >= HEADING1 && type <= HEADING6) addBlankLine();
	if (type == PARAGRAPH) endedParagraph = true;
	if ((type & IS_PARAGRAPH) != 0) endedParagraph = true;
	endedTag = true;
	if (e == curEntry){
		curEntry = (Entry)e.getParent();
		nowEntries--;
	}else{
		Entry tn;
		for(tn = curEntry; tn != null && tn.getParent() != e; tn = (Entry)tn.getParent())
			;		
		if (tn != null){
			e.removeChild(tn);
			((Entry)e.getParent()).addChild(tn);
		}
		nowEntries--;
	}
	if (curEntry == null) curEntry = root;
	//printTree();	
	return lastEntry = e;
}
//===================================================================
public void removeEntry(Entry e)
//===================================================================
{
	entries.remove(e);
}
//===================================================================
public Entry startOrEnd(int type,PropertyList attributes,boolean ending)
//===================================================================
{
	if (ending) return end(type);
	else return start(type,attributes);
}

//===================================================================
public Entry findFirst(int [] types)
//===================================================================
{
	for (int i = creating.size()-1; i >= 0; i--){
		Entry e = (Entry)creating.get(i);
		for (int j = 0; j<types.length; j++){
			if (e.type == types[j]) return e;
		}
	}
	return null;
}
//===================================================================
public Entry cancel(int type)
//===================================================================
{
	for (int i = creating.size()-1; i >= 0; i--){
		Entry e = (Entry)creating.get(i);
		if (e.type == type) {
			creating.removeElementAt(i);
			return e;
		}
	}
	return null;
}

int building = 0;
boolean shown = false;

Gate splitLock = new Gate();

//===================================================================
public void linesSplit(TextDisplay pad)
//===================================================================
{
	splitLock.synchronize(); try{
	try{
		/*
		if (false && !shown){
			TreeControl tc = new TreeControl();
			tc.getTreeTableModel().setRootObject(root);
			Form f = new Form();
			f.addLast(new ScrollBarPanel(tc)).setPreferredSize(300,400);
			f.title = "HTML Elements";
			f.show();
			shown = true;
		}
		*/
		if (pad.getDim(null).width <= 0) return;
		//Vm.debug("-----------------");
		pad.clearTextFormatters();
		Vector v = new Vector(entries.size()+10);
		root.startIndex = 0;
		root.length = text.length;
		root.formatter = new TextFormatter();
		root.formatter.startLine = 0;
		root.formatter.numLines = pad.getNumLines();
		
		if (!root.reformat(pad,new TextPosition(pad),v)) {
			return;
		}
		if (root.needPass2){
			//Vm.debug("Need pass 2");
			Vector s = new Vector();
			root.getLineSpacers(pad,0,pad.getLine(0),s);
			int numSpacers = s.size();
			if (numSpacers != 0){
				int [] all = new int[pad.getNumLines()];
				Utils.getIntSequence(all,0);
				for (int i = 0; i<numSpacers; i++)
					((Entry)s.get(i)).addLineSpacers(pad,all);
				root.fixFormatter(all);
			}
		}
		/*
		IntArray blanks = new IntArray();
		root.addBlankLines(pad,blanks);
		for (int i = 0; i<blanks.length; i+=2){
			Vm.debug("Line: "+blanks.data[i]+", add: "+blanks.data[i+1]);
		}
		*/
		int max = v.size();
		for (int i = 0; i<max; i++){
			TextFormatter tf = (TextFormatter)v.get(i);
			pad.addTextFormatter(tf);
		}
		pad.formattersSet();
	}catch(Throwable e){
		new ReportException(e,null,null,false).execute();//show();
		//Vm.debug(Vm.getStackTrace(e,10));
	}
	}finally{splitLock.unlock();}
}

TextDisplay myDisplay;
//===================================================================
public void addTo(TextDisplay pad)
//===================================================================
{
	myDisplay = pad;
	pad.addListener(this);
	pad.setText(new String(text.data,0,text.length));
}
//===================================================================
public void removeFrom(TextDisplay pad)
//===================================================================
{
	pad.removeListener(this);
	if (pad == myDisplay)
		myDisplay = null;
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof ControlEvent && ev.type == TextDisplay.LINES_SPLIT){
		linesSplit((TextDisplay)ev.target);
	}
}


int lastNewType = 200;

Entry root = new Entry(), curEntry = root;

//===================================================================
public int addType(TextFormatter f)
//===================================================================
{
	formatters.set(lastNewType++,f);
	return lastNewType-1;
}

static int EndFormatterFlags = TextFormatter.LINE_CENTERED|TextFormatter.LINE_RIGHT_ALIGNED;

	
	TextPosition temp = new TextPosition();
	static Range range = new Range(0,0);
	
	//##################################################################
	public class Entry extends MutableTreeNodeObject{
	//##################################################################
	int startIndex;
	int length;
	TextFormatter formatter, endFormatter;
	PropertyList attributes;
	int type = 0;
	int curSequence;
	int groupID;
	Object data;
	int visit;
	public String getName() {return toString()+", "+attributes;}
	public String toString() {return valueToName(type)+": ("+startIndex+"->"+(startIndex+length)+")";}
	boolean needPass2 = false;
	int putLinesAbove = 0;
	int needAbove, needBelow;
	
	//===================================================================
	public Entry getRoot()
	//===================================================================
	{
		for (Entry e = this; e != null; e = (Entry)e.getParent())
			if (e.getParent() == null) return e;
		return null;
	}
	
	//===================================================================
	public void reset()
	//===================================================================
	{
		curSequence = 0;
	}
	//===================================================================
	int countLinesInMe(DisplayLine start, int indexOfLineCharacters)
	//===================================================================
	{
		int num = 1;
		int curIndex = indexOfLineCharacters+start.trueLength();
		for (DisplayLine s = (DisplayLine)start.next; s != null; s = (DisplayLine)s.next){
			int tl = 0;
			if (curIndex >= startIndex && curIndex < startIndex+length){
				num++;
				curIndex += s.trueLength();
			}else 
				break;
		}
		return num;
	}
	/*
	//===================================================================
	TextFormatter getFormatterFor(int lineIndex)
	//===================================================================
	{
		try{
			TextFormatter ret = (TextFormatter)formatter.getCopy();
			ret.line = lineIndex;
			ret.groupID = groupID;
			ret.groupIndex = curSequence++;
			return ret;
		}catch(RuntimeException e){
			Vm.debug(Vm.getStackTrace(e,10));
			return new TextFormatter();
		}
	}
	*/
	boolean amOpen = true;
	/*
	//===================================================================
	public void getChildrenFormatters(TextPad pad,TextPosition t,Vector dest)
	//===================================================================
	{
		int max = getChildCount();
		for (int i = 0; i<max; i++){
			Entry e = (Entry)getChild(i);
			e.getFormatter(pad,t,dest);
			if (e.needPass2) needPass2 = true;
		}
	}
	*/
	/*
	//===================================================================
	public void addBlankLines(TextPad pad,IntArray blanks)
	//===================================================================
	{
		if (!hasImage) return;
		if (putLinesAbove != 0){
			DisplayLine dl = pad.getLine(formatter.startLine);
			int added = pad.ensureBlankLines(dl,putLinesAbove,true);
			if (added != 0){
				int len = blanks.length;
				if (len == 0) {
					blanks.append(formatter.startLine);
					blanks.append(added);
				}else if (blanks.data[len-2] == formatter.startLine){
					blanks.data[len-1] += added;
				}else{
					blanks.append(formatter.startLine);
					blanks.append(added+blanks.data[len-1]);
				}
			}
		}
		int max = getChildCount();
		for (int i = 0; i<max; i++){
			((Entry)getChild(i)).addBlankLines(pad,blanks);
		}
		
	}
	*/
	
	TextPosition start;
	
	//-------------------------------------------------------------------
	void linesInsertedAbove(TextPosition t, int startLine, int numInserted)
	//-------------------------------------------------------------------
	{
		//Vm.debug(numInserted+" inserted above: "+startLine);
		if (start == null){
			int sl = formatter.startLine, nl = formatter.numLines;
			if (formatter.startLine == startLine){
				formatter.startLine += numInserted;
				if (type == IMAGE || type == BLOCKIMAGE)
					formatter.imageLine += numInserted;
			}else if (formatter.startLine+formatter.numLines > startLine){
				if (type == IMAGE || type == BLOCKIMAGE){
					formatter.startLine += numInserted;
					formatter.imageLine += numInserted;
				}else
					formatter.numLines += numInserted;
			}else return;
			//Vm.debug("Type: "+type+", I  was: "+sl+"->"+nl);
			//Vm.debug("Type: "+type+", am now: "+formatter.startLine+"->"+formatter.numLines);
		}else{
			int sl = start.lineIndex, nl = start.characterIndex;
			start.copyFrom(t);
			start.findCharacter(startIndex);
			t.copyFrom(start);
			int max = getChildCount();
			for (int i = 0; i<max; i++)
				((Entry)getChild(i)).linesInsertedAbove(t,startLine,numInserted);
			if (length != 0) t.findCharacter(startIndex+length-1);
			//Vm.debug(hashCode()+" Type: "+type+", I  was: "+sl+" @ "+nl);
			//Vm.debug(hashCode()+" Type: "+type+", am now: "+start.lineIndex+" @ "+start.characterIndex);
		}
	}
	
	//-------------------------------------------------------------------
	void updateEndFormatter()
	//-------------------------------------------------------------------
	{
		if (endFormatter != null){
			endFormatter.startLine = formatter.startLine;
			endFormatter.numLines = formatter.numLines;
			endFormatter.startCharacter = formatter.startCharacter;
			endFormatter.endCharacter = formatter.endCharacter;
		}
	}
	//-------------------------------------------------------------------
	void setupFormatter(TextPosition start,TextPosition current)
	//-------------------------------------------------------------------
	{
		if (formatter == null){
			formatter = new TextFormatter();
			length = text.length-startIndex;
		}
		TextPosition t = current;
		TextFormatter tf = formatter;
		
		if (type == IMAGE){
			for (Entry p = (Entry)getParent(); p != null; p = (Entry)p.getParent()){
				if (p.type == HYPERLINK && PropertyList.getString(p.attributes,"href",null) != null){
					formatter.linkedTo = p.formatter;
					break;
				}
			}
		}

		//
		// An image type with an imageChar of -1 is a right or left aligned image.
		if (type == IMAGE && tf.imageChar == -1)
			return;
		//
		if (length > 0)
			t.findCharacter(startIndex+length-1);
		tf.startLine = start.lineIndex;
		tf.numLines = t.lineIndex-start.lineIndex+1;
		//
		if (type == IMAGE){
			tf.startCharacter = 0;
			tf.endCharacter = -1;
		}else if ((type & BLOCK_LEVEL) != 0){
			tf.startCharacter = 0;
			tf.endCharacter = -1;
		}else if (length == 0){
			tf.startCharacter =  tf.endCharacter = -1;	
			if (type != IMAGE && type != HYPERLINK) tf.notUsed = true;
		}else{
			tf.startCharacter = startIndex-start.characterIndex;
			tf.endCharacter = (startIndex+length-1)-t.characterIndex;
		}
		if (endFormatter != null) updateEndFormatter();
	}
	
	//-------------------------------------------------------------------
	boolean getFormatterForLine(int lineIndex,DisplayLine line,Vector dest,Entry stopAt)
	//-------------------------------------------------------------------
	{
		if (stopAt == this) return false;
		if (start != null){
			temp.copyFrom(start);
			setupFormatter(start,temp);
		}
		if (formatter == null) return true;
		if (formatter.getCharRange(lineIndex,line,range) == null) {
			return true;
		}
		dest.add(formatter);
		int max = getChildCount();
		for (int i = 0; i<max; i++)
			if (!((Entry)getChild(i)).getFormatterForLine(lineIndex,line,dest,stopAt))
				return false;
		return true;
	}
	//-------------------------------------------------------------------
	void addLineSpacers(TextPad pad,int lineIndex,DisplayLine line,int above,int below,int [] indexes)
	//-------------------------------------------------------------------
	{
		if (above > 0) above = pad.ensureBlankLines(line,above,true);
		if (below > 0) below = pad.ensureBlankLines(line,below,false);
		if (above == 0 && below == 0) return;
		indexes[lineIndex] += above;
		above += below;
		for (int i = lineIndex+1; i<indexes.length; i++)
			indexes[i] += above;
	}
	//-------------------------------------------------------------------
	void addLineSpacers(TextPad pad,int [] indexes)
	//-------------------------------------------------------------------
	{
		addLineSpacers(pad,myLineIndex,myLine,needAbove,needBelow,indexes);
	}
	DisplayLine myLine;
	int myLineIndex;
	//-------------------------------------------------------------------
	void getLineSpacers(TextPad pad,int lineIndex,DisplayLine line,Vector dest)
	//-------------------------------------------------------------------
	{
		if (!needPass2) return;
		int more = formatter.startLine-lineIndex;
		if (more != 0){
			line = (DisplayLine)line.getNext(line,more);
			lineIndex += more;
		}
		myLineIndex = lineIndex;
		myLine = line;
		if (needAbove != 0 || needBelow != 0)
			dest.add(this);
		int max = getChildCount();
		for (int i = 0; i<max; i++){
			Entry e = (Entry)getChild(i);
			if (!e.needPass2) continue;
			e.getLineSpacers(pad,lineIndex,line,dest);
		}
	}
	//-------------------------------------------------------------------
	void fixFormatter(int [] indexes)
	//-------------------------------------------------------------------
	{
		try{
		if (type == IMAGE){
			formatter.imageLine = indexes[formatter.imageLine];
			if (formatter.imageChar == -1){
				formatter.startLine = formatter.imageLine;
			}else{
				formatter.startLine = formatter.imageLine-needAbove;
				formatter.numLines = 1+needAbove+needBelow;
			}
		}else{
			int endLine = formatter.startLine+formatter.numLines-1;
			formatter.startLine = indexes[formatter.startLine];
			formatter.numLines = indexes[endLine]-formatter.startLine+1;
		}
		if (endFormatter != null) updateEndFormatter();
		}catch(Exception e){}
		int max = getChildCount();
		for (int i = 0; i<max; i++)
			((Entry)getChild(i)).fixFormatter(indexes);
	}
	/**
	* This reformats the lines.
	**/
	//===================================================================
	public boolean reformat(TextPad pad,TextPosition t,Vector addTo)
	//===================================================================
	{
		try{
			start = t.getCopy();
			//Vm.debug(type+" for: "+start.characterIndex+" on "+start.lineIndex);
			if (!start.findCharacter(startIndex)){
				//Vm.debug("Not found: "+type+", "+startIndex);
				return false;
			}
			/*
			int depth = 0;
			for (ewe.data.TreeNode tn = this; tn != null; tn = tn.getParent())
				depth++;
			Vm.debug(type+", "+depth);
			*/
			//
			// Now start points to the start of the text for this Entry.
			//
			t.copyFrom(start);
			setupFormatter(start,t);
			if (formatter.applyBefore())
				addTo.add(formatter.getStartFormatter(formatter));
			
			//
			// Resplit if necessary.
			//
			if (length != 0){
					DisplayLine newLine = formatter.creatingFor(pad,start.line,t.lineIndex-start.lineIndex+1,start.lineIndex);
			}
			if (type == IMAGE){
				//needPass2 = true;
				TextFormatter tf = formatter;
				tf.vSpace = PropertyList.getInt(attributes,"vspace",tf.vSpace);
				tf.hSpace = PropertyList.getInt(attributes,"hspace",tf.hSpace);
				tf.borderWidth = PropertyList.getInt(attributes,"border",tf.borderWidth);
				final int extraWidth = tf.image.getWidth()+(tf.borderWidth+tf.hSpace)*2;
				String align = PropertyList.getString(attributes,"align","bottom").toLowerCase();
				int lh = pad.getLineHeight();
				int ih = tf.image == null ? lh : tf.image.getHeight()+(tf.borderWidth+tf.vSpace)*2;
				//
				if (align.equals("bottom")){ 
					ih += pad.getLineHeight()-pad.getBaselineHeight();
					//Vm.debug(tf.image.getHeight()+", "+pad.getLineHeight()+", "+pad.getBaselineHeight());
				}
				//
				int numLines = tf.image == null ? 1 : (ih+lh-1)/lh;
				Vector v = new Vector();
				root.getFormatterForLine(start.lineIndex,start.line,v,this);
				FormattedTextSpecs fts = pad.getTextPositions(start.lineIndex,start.line,null,null,false);
				int max = v.size();
				for (int i = 0; i<max; i++){
					((TextFormatter)v.get(i)).applySpecialFormat(pad,start.lineIndex,start.line,fts);
				}
				int allowedSpace = start.line.displayWidth;
				final int fullLineSpace = pad.getAvailableWidth()-fts.leftMargin-fts.rightMargin;
				//
				// First see if inserting the image will cause it to go over the line.
				//
				if (align.equals("left") || align.equals("right")){
					tf.startLine = start.lineIndex;
					tf.numLines = numLines;
					tf.startCharacter = 0;
					tf.endCharacter = -1;
					tf.imageLine = tf.startLine;
					tf.imageChar = -1;
					tf.imageY = 0;
					tf.imageX = align.equals("left") ? 0 : -1;
					
					int didLines = 0;
					DisplayLine first = start.line;
					final int linesToDo = numLines;
					final int fullWidth = fullLineSpace;
					final int lineWidth = allowedSpace-extraWidth;
					while(didLines < numLines){
						int num = first.countToSectionEnd();
						final int done = didLines;
						pad.resplit(first,num,0,new DisplayLine.WidthProvider(){
							int total = done;
							public int getWidthFor(DisplayLineSpecs specs, int lineIndex, int startingCharacterIndex, DisplayLine splitSoFar)
							{
								int ret = (total >= linesToDo) ? fullWidth : lineWidth;
								total++;
								//Vm.debug(lineIndex+", "+ret+", "+fullWidth+", "+lineWidth);
								return ret;
							}
						}
							,null,null);
						num = first.countToSectionEnd();
						didLines += num;
						if (num >= numLines) break;
						first = (DisplayLine)first.getNext(first,num-1);//Go to line before the section end.
						if (first.next == null) break; //No more lines? Then break.
						first = (DisplayLine)first.next;
					}
					if (didLines < numLines){
						pad.ensureBlankLines(first,numLines-didLines,false);
					}
					return true;
				}else{
					//
					//
					//
					int before = startIndex-start.characterIndex;
					int width = before == 0 ? 0 : fts.calculatedPositions[before-1];
					width += fts.leftMargin+pad.spacing;
					int widthUsed = before == 0 ? 0 : fts.calculatedPositions[before-1];
					if (widthUsed+extraWidth >= allowedSpace){
						DisplayLine dl = pad.breakLineBefore(start.line,startIndex-start.characterIndex);
						if (dl != null){
							start.lineIndex++;
							start.line = dl;
							start.characterIndex += startIndex-start.characterIndex;
							start.line.displayWidth = allowedSpace;//normalWidth;
						}
						allowedSpace = fullLineSpace;
					}
					tf.numLines = 1;
					tf.startLine = start.lineIndex;
					tf.imageLine = start.lineIndex;
					tf.imageChar = startIndex-start.characterIndex;
					tf.startCharacter = 0;
					tf.endCharacter = -1;
					//
					//
					int num = start.line.countToSectionEnd();
					final int firstLineWidth = allowedSpace-extraWidth;
					start.line = pad.resplit(start.line,num,0,new DisplayLine.WidthProvider(){
						public int getWidthFor(DisplayLineSpecs specs, int lineIndex, int startingCharacterIndex, DisplayLine splitSoFar)
						{
							int ret = lineIndex == 0 ? firstLineWidth : fullLineSpace;
							return ret;
						}
					},null,null);
					//
					// Find out how much space we need above and below.
					//
					if (numLines != 1 || true){
						if (align.equals("top")) {
							needBelow = numLines-1;
							tf.imageY = 0;
						}else if (align.equals("middle")){
							needAbove = needBelow = numLines/2;
							numLines = needAbove+needBelow+1;
							tf.imageY = ((numLines*lh)-ih)/2;
						}else{
							needAbove = numLines-1;
							tf.imageY = (numLines*lh)-ih;
							//Vm.debug("Bottom: "+tf.image.getHeight()+", "+ih+", "+numLines+", "+tf.imageY);
						}
						needPass2 = numLines != 1;//true;
					}else{
						if (align.equals("top"))
							tf.imageY = 0;
						else if (align.equals("middle"))
							tf.imageY = (lh-ih)/2;
						else
							tf.imageY = (lh-ih);
					}
					return true; //Images never have any children.
				}
			}		
			//
			t.copyFrom(start);
			int max = getChildCount();
			for (int i = 0; i<max; i++){
				Entry e = (Entry)getChild(i);
				t.copyFrom(start);
				if (!e.reformat(pad,t,addTo))
					;
					//return false;
				if (e.needPass2) needPass2 = true;
			}
			if (length != 0 && formatter.applyAfter()){
				endFormatter = formatter.getEndFormatter(endFormatter);
				addTo.add(endFormatter);
			}
			t.copyFrom(start);
			setupFormatter(start,t);
			return true;
		}finally{
		
		}
	}
	/*
	//===================================================================
	public boolean getTextFormatter(TextPad pad,TextPosition t,Vector addTo)
	//===================================================================
	{
		start = t.getCopy();
		if (!start.findCharacter(startIndex)) return false;
		//
		// Now start points to the start of the text for this Entry.
		//
		t.copyFrom(start);
		addTo.add(formatter);
		int max = getChildCount();
		for (int i = 0; i<max; i++)
			if (!((Entry)getChild(i)).getTextFormatter(pad,t,addTo))
				return false;
		setupFormatter(start,t);
		return true;
	}
	*/
/*	
	//===================================================================
	public boolean getFormatter(TextPad pad,TextPosition t,Vector dest)
	//===================================================================
	{
		if (type == IMAGE) needPass2 = true;
		if (formatter == null) return false;
		start = t.getCopy();
		start.copyFrom(t);
		if (!start.findCharacter(startIndex)) return false;
		if (length != 0){
			if (!t.findCharacter(startIndex+length-1))
				return false;
		}else{
			t.copyFrom(start);
		}
		//
		// Resplit if necessary.
		//
		if (length != 0){
			DisplayLine newLine = formatter.creatingFor(pad,start.line,t.lineIndex-start.lineIndex+1,start.lineIndex);
			if (newLine != start.line && newLine != null) start.line = newLine;
		}
		// Setup the formatter to cover the lines as they now are.
		if (type != IMAGE) {
			setupFormatter(start,t);
		}
		dest.add(formatter);
		//
		if (type == IMAGE){ //length will be zero!
			TextFormatter tf = formatter;
			tf.vSpace = PropertyList.getInt(attributes,"vspace",tf.vSpace);
			tf.hSpace = PropertyList.getInt(attributes,"hspace",tf.hSpace);
			tf.borderWidth = PropertyList.getInt(attributes,"border",tf.borderWidth);
			final int extraWidth = tf.image.getWidth()+(tf.borderWidth+tf.hSpace)*2;
			String align = PropertyList.getString(attributes,"align","bottom").toLowerCase();
			//
			// First see if inserting the image will cause it to go over the line.
			//
			Vector v = new Vector();
			root.getFormatterForLine(start.lineIndex,start.line,v,this);
			FormattedTextSpecs fts = pad.getTextPositions(start.lineIndex,start.line,null,null,false);
			int max = v.size();
			for (int i = 0; i<max; i++)
				((TextFormatter)v.get(i)).applySpecialFormat(pad,start.lineIndex,start.line,fts);
			//
			//
			//
			int before = startIndex-start.characterIndex;
			int width = before == 0 ? 0 : fts.calculatedPositions[before-1];
			width += fts.leftMargin+pad.spacing;
			//Vm.debug(start.line.displayWidth+", "+width+", "+extraWidth);
			int pw = pad.getSize(null).width;
			final int normalWidth = pw-(pad.spacing*2)-pad.rightMargin-fts.leftMargin;
			//Vm.debug((pw-pad.spacing-pad.rightMargin)+", "+width+", "+extraWidth+", "+(displayWidth-extraWidth));
			if (width+extraWidth >= normalWidth){
				//Vm.debug("Splitting!");
				DisplayLine dl = pad.breakLineBefore(start.line,startIndex-start.characterIndex);
				if (dl != null){
					start.lineIndex++;
					start.line = dl;
					start.line.displayWidth = normalWidth;
					start.characterIndex += startIndex-start.characterIndex;
				}
			}
			final int displayWidth = start.line.displayWidth;
			tf.numLines = 1;
			tf.startLine = start.lineIndex;
			tf.imageLine = start.lineIndex;
			tf.imageChar = startIndex-start.characterIndex;
			//Vm.debug(tf.imageLine+", "+tf.imageChar);
			tf.startCharacter = 0;
			tf.endCharacter = -1;
			
			int lh = pad.getLineHeight();
			int ih = tf.image == null ? lh : tf.image.getHeight()+(tf.borderWidth+tf.vSpace)*2;
			int numLines = tf.image == null ? 1 : (ih+lh-1)/lh;
			int needAbove = 0, needBelow = 0;
			if (numLines != 1){
				if (align.equals("top")) {
					needBelow = numLines-1;
					tf.imageY = 0;
				}else if (align.equals("center")){
					needAbove = needBelow = numLines/2;
					numLines = needAbove+needBelow+1;
					tf.imageY = ((numLines*lh)-ih)/2;
				}else{
					needAbove = numLines-1;
					tf.imageY = (numLines*lh)-ih;
				}
				int num = start.line.countToSectionEnd();
				start.line = pad.resplit(start.line,num,0,new DisplayLine.WidthProvider(){
					public int getWidthFor(DisplayLineSpecs specs, int lineIndex, int startingCharacterIndex, DisplayLine splitSoFar)
					{
						int ret = lineIndex == 0 ? displayWidth-extraWidth : normalWidth;
						return ret;
					}
				},null,null);
				t.copyFrom(start);
			}
			int lineIndex = start.lineIndex;
			tf.numLines = 1;
			int added = true ? 0 : pad.ensureBlankLines(start.line,needAbove,true);
			if (added > 0) {
				//Vm.debug(hashCode()+" - adding: "+added);
				getRoot().linesInsertedAbove(new TextPosition(pad),start.lineIndex,added);
			}
			tf.numLines += added;
			t.lineIndex += added;
			
			start.lineIndex = lineIndex+added;
			tf.imageLine = start.lineIndex;
			
			added = true ? 0 : pad.ensureBlankLines(start.line,needBelow,false);
			
			putLinesAbove = needAbove;
		}else{
			// Reposition to start.
			if (start.line.invalid) start = new TextPosition(pad,start.lineIndex,start.characterIndex);
			t.copyFrom(start);
			getChildrenFormatters(pad,t,dest);
			//
			// Now lines may have been modified, so setup Formatter again.
			//
			setupFormatter(start,t);
			if (type == ITALIC) Vm.debug(formatter.toString());
		}
		start = null;
		return true;
	}
	*/
	//##################################################################
	}
	//##################################################################


private static String [] htmlTags = 
{"p","b","i","u","center",
"h1","h2","h3","h4","h5","h6",
"tt","big","small","div","span","font","a","title","strong","em","dfn","blockquote","address",
"body","dir","menu","code"
//,"table","tr","td","th"
};
private static int [] htmlTypes =
{PARAGRAPH,BOLD,ITALIC,UNDERLINE,CENTERED,
HEADING1,HEADING2,HEADING3,HEADING4,HEADING5,HEADING6,
TELETYPE,BIG,SMALL,DIV,SPAN,FONT,HYPERLINK,TITLE,STRONG,EM,DFN,BLOCKQUOTE,ADDRESS,
BODY,PARAGRAPH,PARAGRAPH,TELETYPE
//,TABLE,TROW,TCELL,TCELL
};
private static int [] listTypes = {UL,OL,OLI,ULI};
private static int [] listStarts = {UL,OL};
private static int [] defLists = {DT,DD,DL};
private static int [] tableTypes = {TABLE,TROW,TCELL};
private static int [] tableStarts = {TABLE};

//===================================================================
void endTableCellOrRow(boolean isTR)
//===================================================================
{
	Entry s = findFirst(tableStarts);
	if (s == null) return;
	while(true){
		Entry e = findFirst(tableTypes);
		if (e == null) return;
		if (e.type == TABLE) return;
		if (e.type == TROW && !isTR) return;
		conditionalLineBreak();
		end(e.type);
	}
}
//===================================================================
void endListItem()
//===================================================================
{
	Entry s = findFirst(listStarts);
	if (s == null) return;
	Object data = s.data;
	Entry e = findFirst(listTypes);
	if (e == null) return;
	if (e.type != OLI && e.type != ULI) return;
	conditionalLineBreak();
	e = end(e.type);
	e.formatter.data = data;
	// Update the data.
	if (data != null && e.type == OLI){
		String dt = data.toString();
		int value = Convert.toInt(dt);
		if (value == 0 && !dt.equals("0")){
			char [] ch = dt.toCharArray();
			if (ch.length != 0) ch[0]++;
			dt = new String(ch);
		}else{
			dt += ".";
			e.formatter.data = dt;
			dt = Convert.toString(value+1);
		}
		s.data = dt;
	}
}
//===================================================================
void endDefDataOrTerm()
//===================================================================
{
	Entry e = findFirst(defLists);
	if (e == null) return;
	if (e.type != DD && e.type != DT) return;
	conditionalLineBreak();
	end(e.type);
}
public PropertyList headerData = new PropertyList();
public PropertyList bodyData = new PropertyList();

boolean waitingToRefresh = false, needRefresh = false;

//-------------------------------------------------------------------
protected Handle getAnImage(Object nameOrProperties,boolean documentImage)
//-------------------------------------------------------------------
{
	if (properties.getBoolean("allowImages",true) && nameOrProperties != null) {
		ImageResolver ir = (ImageResolver)properties.getValue("imageResolver",imageResolver);
		if (ir != null){
			PropertyList pl = (nameOrProperties instanceof PropertyList) ? (PropertyList)nameOrProperties : null;
			if (pl == null) pl = PropertyList.make("src|"+nameOrProperties);
			return ir.resolveImage(pl,documentImage ? properties.getBoolean("allowAnimatedImages",true) : false,
			documentImage ? (Dimension)properties.getValue("maxImageSize",null) : null);
		}
	}
	return new Handle(Handle.Failed,null);
}
//===================================================================
public void parseHtml(final TextDisplay forDisplay,String text,Handle handle)
//===================================================================
{
	//ewe.ui.Notepad np = new ewe.ui.Notepad();
	//np.theText = text;
	//np.execute();
	myDisplay = forDisplay;
	splitLock.synchronize(); try{
	char [] source = Vm.getStringChars(text);
	int len = source.length;
	String headerTag = null;
	boolean pastHeader = false;
	boolean inHeader = false;
	boolean inForm = false;
	if (handle == null) handle = new Handle();
	handle.resetProgress(0.1f);
	YieldToEvents ye = new YieldToEvents(10,100);
	for(int reached = 0; reached < len && !handle.shouldStop;){
		ye.yield();
		float progress = (float)((double)reached/len);
		handle.setProgress(progress);
		int idx = text.indexOf('<',reached);
		if (idx != -1){
			if (idx <= len-6 && source[idx+1] == '!' && source[idx+2] == '-' && source[idx+3] == '-'){ // Comment
				int end = text.indexOf("-->",idx+3);
				if (end != -1){ // Missing --> implies a comment in error.
					if (!inForm) addHtml(text.substring(reached,idx));
					reached = end+3;
					continue;
				}/*else
					break;*/
			}
		}
		int idx2 = idx == -1 ? -1 : text.indexOf('>',idx);
		if (idx == -1 || idx2 == -1){
			String ss = text.substring(reached);
			if (!inForm){
				if (headerTag == null) addHtml(ss);
				else headerData.set(headerTag,headerData.getString(headerTag,"")+ss);
			}
			break;
		}
		int idx3 = text.indexOf('<',idx+1);
		if (idx3 != -1 && idx3 < idx2){
			String ss = text.substring(reached,idx3);
			if (!inForm){
				if (headerTag == null) addHtml(ss);
				else headerData.set(headerTag,headerData.getString(headerTag,"")+ss);
			}
			reached = idx3;
			continue;
		}
		if (!inForm){
			if (headerTag == null) addHtml(text.substring(reached,idx));
			else headerData.set(headerTag,headerData.getString(headerTag,"")+text.substring(reached,idx));
		}		
		reached = idx2+1;
		if (idx+1 == idx2) continue;
		String insideTag = text.substring(idx+1,idx2);
		boolean ending = insideTag.charAt(0) == '/';
		int space = insideTag.indexOf(' ');
		int sp2 = insideTag.indexOf('\n');
		if (sp2 != -1 && (space == -1 || sp2 < space)) space = sp2;
		sp2 = insideTag.indexOf('\r');
		if (sp2 != -1 && (space == -1 || sp2 < space)) space = sp2;
		sp2 = insideTag.indexOf('\t');
		if (sp2 != -1 && (space == -1 || sp2 < space)) space = sp2;
			
		String tag = space == -1 ? insideTag : insideTag.substring(0,space);
		if (ending) tag = tag.substring(1);
		tag = tag.toLowerCase();
		if (inForm){
			if (tag.equals("form") || tag.equals("script"))
				if (ending) inForm = false;
			continue;
		}else if ((tag.equals("form") || tag.equals("script"))&& !inForm){
			inForm = true;
			continue;
		}
		if (!pastHeader){
			if (tag.equals("head")){
				if (ending){
					headerTag = null;
					pastHeader = true;
					inHeader = false;
				}else{
					inHeader = true;
				}
				continue;
			}else if (tag.equals("body")){
				headerTag = null;
				pastHeader = true;
				inHeader = false;
				//continue;
			}else if (inHeader){
				if (ending) headerTag = null;
				else headerTag = tag;
				continue;
			}else if (tag.equals("title")){
				inHeader = true;
				headerTag = tag;
				continue;
			}
		}
		if (tag.equals("pre") && !inPreFormat){
			if (reached < len && text.charAt(reached) == '\r') reached++;
			if (reached < len && text.charAt(reached) == '\n') reached++;
		}
		String attr = space == -1 ? null : insideTag.substring(space+1);
		PropertyList attributes = toProperties(attr);
		boolean did = false;
		Entry didE = null;
		for (int i = 0; !did && i<htmlTags.length; i++){
			if (htmlTags[i].equals(tag)){
				did = true;
				didE = startOrEnd(htmlTypes[i],attributes,ending);
			}
		}
		if (tag.equals("body")){
			if (!ending){
				PropertyList a = PropertyList.toPropertyList(attributes);
				final Handle h = getAnImage(a.getString("background",null),false);
				if ((h.check() & h.Success) != 0)
					bodyData.set("backgroundImage",h.returnValue);
				else if ((h.check() & h.Stopped) == 0){
					new Task(){
						protected void doRun(){
							if (waitOnSuccess(h,TimeOut.Forever,false)){
								bodyData.set("backgroundImage",h.returnValue);
								if (myDisplay != null) {
									myDisplay.displayPropertiesChanged();
								}
							}
						}
					}.start();
				}
				bodyData.set("background",toColor(a.getValue("bgcolor",Color.White),Color.White));
				bodyData.set("foreground",toColor(a.getValue("text",Color.Black),Color.Black));
				bodyData.set("link",toColor(a.getValue("link",HotSpot.hotColor),HotSpot.hotColor));
			}
			continue;
		}else if (tag.equals("a") && ending && didE != null){
			if (this.text.length != 0 && this.text.data[this.text.length-1] == ' ')
				didE.length--;
		}
		if (!did){
			if (tag.equals("br")) add("\n");
			else if (tag.equals("hr")){
				conditionalLineBreak();
				start(HR,attributes);
				add(" ");
				end(HR);
				/*
			}else if (tag.equals("test")){
				if (!ending) lineBreak();
				startOrEnd(TEST,attributes,ending);
				if (ending) lineBreak();
				*/
			}else if (tag.equals("pre")){
				//conditionalLineBreak();
				inPreFormat = !ending;
				startOrEnd(PREFORMAT,attributes,ending);
			}else if (tag.equals("table")){
				if (ending) endTableCellOrRow(true);
				startOrEnd(TABLE,attributes,ending);
			}else if (tag.equals("tr")){
				if (ending) endTableCellOrRow(false);
				else endTableCellOrRow(true);
				startOrEnd(TROW,attributes,ending);
			}else if (tag.equals("td") || tag.equals("th")){
				if (!ending) endTableCellOrRow(false);
				startOrEnd(TCELL,attributes,ending);
			}else if (tag.equals("ul")){
				if (ending) endListItem();
				else {
					conditionalLineBreak();
					//lineBreak();
				}
				Entry e = startOrEnd(UL,attributes,ending);
				if (!ending) e.data = TextFormatter.SOLID_CIRCLE;
				//else removeEntry(e);
			}else if (tag.equals("ol")){
				if (ending) endListItem();
				else {
					conditionalLineBreak();
					//lineBreak();
				}
				Entry e = startOrEnd(OL,attributes,ending);
				if (!ending) e.data = PropertyList.getString(attributes,"start","1");
				//else removeEntry(e);
			}else if (tag.equals("li")){
				endListItem();
				if (!ending){
					int typeToUse = OLI;
					Entry e = findFirst(listStarts);
					if (e == null) continue;
					if (e.type == UL) typeToUse = ULI;
					startOrEnd(typeToUse,attributes,ending);
				}
			}else if (tag.equals("dl")){
				if (ending) endDefDataOrTerm();
				else conditionalLineBreak();
				startOrEnd(DL,attributes,ending);
			}else if (tag.equals("dt")){
				if (!ending) {
					endDefDataOrTerm();
					conditionalLineBreak();
				}
				startOrEnd(DT,attributes,ending);
			}else if (tag.equals("dd")){
				if (!ending) {
					endDefDataOrTerm();
					conditionalLineBreak();
				}
				startOrEnd(DD,attributes,ending);
				}else if (tag.equals("img")){
					start(IMAGE,attributes);
					final Entry e = end(IMAGE);
					final IImage ui = TextFormatter.getUnknownImage();
					e.formatter.image = ui;
					if (properties.getBoolean("allowImages",true)){
						Object toGet = PropertyList.toPropertyList(attributes);
						final Handle h = getAnImage(toGet,true);
						/*
						ImageResolver ir = (ImageResolver)properties.getValue("imageResolver",imageResolver);
						final Handle h = ir.resolveImage(PropertyList.toPropertyList(attributes),
						properties.getBoolean("allowAnimatedImages",true),
						(Dimension)properties.getValue("maxImageSize",null));
						*/
						//mThread.yield();
						if ((h.check() & h.Success) != 0)
							e.formatter.image = (IImage)h.returnValue;
						else if ((h.check() & h.Failure) != 0){
							//Vm.debug("Could not resolve: "+toGet);
							e.formatter.image = TextFormatter.getBrokenImage();
						}else
							new Thread(){
								public void run(){
										try{
											h.waitOn(h.Success);
											e.formatter.image = (IImage)h.returnValue;
										}catch(Exception ex){
											e.formatter.image = TextFormatter.getBrokenImage();
										}
										if (e.formatter.image.getWidth() == ui.getWidth() && e.formatter.image.getHeight() == ui.getHeight()){
											e.formatter.repaint(forDisplay,null);
											return;
										}
										if (waitingToRefresh){
											needRefresh = true;
										}else{
											waitingToRefresh = true;
											needRefresh = false;
											while(true){
												mThread.nap(500);
												if (!needRefresh) break;
												needRefresh = false;
											}
											waitingToRefresh = false;
											splitLock.synchronize(); try{
												if (forDisplay == myDisplay)
													forDisplay.setText(forDisplay.text);
											}finally{splitLock.unlock();}
										}
								}						
							}.start();
					}
					String prop = PropertyList.getString(attributes,"align","").toLowerCase();
					if (!prop.equals("left") && !prop.equals("right"))
						add(" ");
				/*
					start(ITALIC,null);
					add("<Image");
					if (alt != null) add(": "+alt);
					add(">");
					end(ITALIC);
				*/
				}
		}
	}
	}catch(Exception e){
		Vm.debug(Vm.getStackTrace(e,10));
	}finally{splitLock.unlock(); if (handle != null) handle.setProgress(1.0f);}
}
//===================================================================
public void endHtml()
//===================================================================
{
	if (end(PARAGRAPH) != null) addBlankLine();
	conditionalLineBreak();
}

//===================================================================
public PropertyList properties = new PropertyList();
//===================================================================

ImageResolver imageResolver = this;

Hashtable imageCache;

//===================================================================
public Handle resolveImage(final PropertyList imageProperties,final boolean allowAnimatedImages,final Dimension maxSize)
//===================================================================
{
	final String src = imageProperties.getString("src",null);
	if (src == null) return new Handle(Handle.Failed,null);
	if (false){
		IImage got = resolveImageNow(src,allowAnimatedImages,maxSize,imageProperties);
		if (got == null) return new Handle(Handle.Failed,null);
		else return new Handle(Handle.Succeeded,got);
	}
	
	return new Task(){
		protected void doRun(){
			Handle handle = this;
			IImage got = resolveImageNow(src,allowAnimatedImages,maxSize,imageProperties);
			if (got == null) handle.set(Handle.Failed);
			else {
				handle.returnValue = got;
				handle.set(Handle.Succeeded);
			}
		}
	}.start();
}

private static ByteArray scaleArray;
//===================================================================
public IImage resolveImageNow(String src,boolean allowAnimatedImages,Dimension maxSize,PropertyList imageProperties)
//===================================================================
{
	//Vm.debug("Going to resolve: "+src);
	if (scaleArray == null) scaleArray = new ByteArray();
	String path = File.removeTrailingSlash(properties.getString("documentRoot",""));
	if (path.equals("/")) path += src;
	else path += "/"+src;
	if (imageCache == null) imageCache = new Hashtable();
	Object found = imageCache.get(path);
	if (found instanceof IImage) {
		//Vm.debug("I found in cache: "+path);
		return (IImage)found;
	}
	try{
		IImage got = null;
		ByteArray all = Vm.readResource(null,path,null);
		//Vm.debug("Read resource: "+path);
		if (all == null) throw new IllegalArgumentException();
		//Vm.debug("I resolved: "+path);
		/*
		ImageInfo info = Image.getImageInfo(ba,null);
		if (info.format == ImageInfo.FORMAT_GIF && allowAnimatedImages){
			ewe.io.MemoryFile mf = new ewe.io.MemoryFile();
			mf.data = ba;
			got = ewe.graphics.AnimatedIcon.getAnimatedImageFromGIF(mf);
		}
		if (maxSize != null && !(got instanceof AniImage)){
			if (got == null) 
				got = info.canScale && (info.width > maxSize.width || info.height > maxSize.height) 
					? 
					new Image(ba,0,maxSize.width,maxSize.height) : 
					new Image(ba,0);
			if (got.getWidth() > maxSize.width || got.getHeight() > maxSize.height){
				got = new PixelBuffer(got).scale(maxSize.width,maxSize.height,null,PixelBuffer.SCALE_KEEP_ASPECT_RATIO,scaleArray).toMImage();
			}
			if (got instanceof Image) got = new mImage(got);
		}else if (got == null)
			got = new Picture(new FormattedDataSource().set(ba),0);
		*/
		if (got == null){
			FormattedDataSource fds = new FormattedDataSource().set(all);
			got = ImageDecoder.decodeScaledPicture(fds, maxSize, true, true, null);
		}
		PropertyList pl = PropertyList.toPropertyList(imageProperties);
		int ww = pl.getInt("width",got.getWidth());
		int hh = pl.getInt("height",got.getHeight());
		if (ww <= 0) ww = got.getWidth();
		if (hh <= 0) hh = got.getHeight();
		if (maxSize != null){
			if (ww > maxSize.width) ww = maxSize.width;
			if (hh > maxSize.height) hh = maxSize.height;
		}
		if (ww != got.getWidth() && hh != got.getHeight())
			got = new PixelBuffer(got).scale(ww,hh,null,0,scaleArray).toPicture();
		imageCache.put(path,got);
		return got;
	/*
		if (imageCache == null) imageCache = new ImageCache();
		return imageCache.getImage(path);
	*/
	}catch(Exception e){
		//Vm.debug("Bad image: "+path);
		//Vm.debug(Vm.getStackTrace(e,10));
	}
	return null;
}
//##################################################################
}
//##################################################################


