
package eve.ui.formatted;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import eve.fx.Color;
import eve.fx.Dimension;
import eve.fx.DisplayLine;
import eve.fx.FormattedTextSpecs;
import eve.fx.Graphics;
import eve.fx.IImage;
import eve.fx.ImageRefresher;
import eve.fx.OnScreenImage;
import eve.fx.Point;
import eve.ui.Gui;
import eve.ui.TextPad;
import eve.ui.event.ControlEvent;
import eve.ui.event.PenEvent;
import eve.util.IntArray;
import eve.util.Range;

//##################################################################
public class TextDisplay extends TextPad implements ImageRefresher{
//##################################################################

//===================================================================
public TextDisplay()
//===================================================================
{
	
}
//===================================================================
public TextDisplay(int rows,int columns)
//===================================================================
{
	super(rows,columns);
}

{
	modify(DisplayOnly,0);
	disableCursor = true;
	disableTextChanges = true;
	rightMargin = leftMargin = 5;
	dontWantPopup = true;
}

Vector formatters = new Vector();

/**
* The color for hot spots. By default it is bright blue.
**/
public Color hotColor = new Color(0,0,255);

public void repaintDataNow()
{
	repaintNow();
}
//-------------------------------------------------------------------
private TextFormatter getTextFormatterFor(int hotIndex,int lineIndex)
//-------------------------------------------------------------------
{
	if (formatterOffsets != null && lineIndex < formatterOffsets.length/2){
		int where = formatterOffsets[lineIndex*2], num = formatterOffsets[lineIndex*2+1];
		if (where != 0){
			if (hotIndex >= num) return null;
			else return (TextFormatter)formatters.get(formatterRefs.data[where+hotIndex]);
		}
	}
	for (int i = 0; i<formatters.size(); i++){
		TextFormatter hs = (TextFormatter)formatters.get(i);
		int ll = getLineLength(lineIndex,false);
		if ((lineIndex == hs.startLine) || (lineIndex > hs.startLine && lineIndex < hs.startLine+hs.numLines))
		//if (hs.line == lineIndex && hs.character < ll && ((hs.length == 0) || (hs.length+hs.character <= ll)))
			if (hotIndex == 0) return hs;
			else hotIndex--;
	}
	return null;
}
//-------------------------------------------------------------------
protected int getSpecialFormatCount(int lineIndex,DisplayLine theLine)
//-------------------------------------------------------------------
{
	if (formatterOffsets == null || lineIndex >= formatterOffsets.length/2)
		formattersSet();
	int num = 0;
	if (theLine == null) return 0;
	int len = theLine.length();
	if (lineIndex < formatterOffsets.length/2)
			formatterOffsets[lineIndex*2] = formatterRefs.length;
	for (int i = 0; i<formatters.size(); i++){
		TextFormatter hs = (TextFormatter)formatters.get(i);
		
		if ((lineIndex == hs.startLine) || (lineIndex > hs.startLine && lineIndex < hs.startLine+hs.numLines)){
		// && hs.character < len && hs.length+hs.character <= len){
			num++;
			formatterRefs.add(i);
		}
	}
	if (lineIndex < formatterOffsets.length/2)
			formatterOffsets[lineIndex*2+1] = num;
	
	return num;
}

private static Vector tempFormatters;
private static Range range;
//===================================================================
public Vector getTextFormatter(Point onControl,Vector dest)
//===================================================================
{
	if (dest == null) {
		if (tempFormatters == null) tempFormatters = new Vector();
		dest = tempFormatters;
	}
	dest.clear();
	Point where = getCharAt(onControl);
	if (where != null) { //Not on any character, possibly on an image?
		DisplayLine dl = getLine(where.y);
		for (int i = 0;;i++){
			TextFormatter hs = getTextFormatterFor(i,where.y);
			if (hs == null) break;
			if (hs.image != null) continue;
			if (range == null) range = new Range(0,0);
			if (hs.getCharRange(where.y,dl,range) != null)
				if (where.x >= range.first && where.x <= range.last){
					dest.add(hs);
					if (hs.linkedTo != null) dest.add(hs.linkedTo);
				}
		}
	}
	//See if on any images.
	int h = getLineHeight();
	int py = (onControl.y-spacing+getTopLine()*h)/h;
	if (py >= getNumLines()) return dest;
	FormattedTextSpecs f;
	int [] got = (f = getTextPositions(py,getLine(py))).calculatedPositions;
	for (int i = 0;;i++){
		TextFormatter hs = getTextFormatterFor(i,py);
		if (hs == null) break;
		if (hs.image != null && hs.isOnImage(this,onControl.x-spacing+getLeftPosition(),f)){
			dest.add(hs);
			if (hs.linkedTo != null) dest.add(hs.linkedTo);
		}
	}
	return dest;
}
//===================================================================
public TextFormatter getTextFormatter(Point onControl)
//===================================================================
{
	Vector v = getTextFormatter(onControl,null);
	if (v.size() == 0) return null;
	else return (TextFormatter)v.get(0);
}

//-------------------------------------------------------------------
protected void applySpecialFormat(int formatIndex,int lineIndex,DisplayLine theLine,FormattedTextSpecs format)
//-------------------------------------------------------------------
{
	TextFormatter hs = getTextFormatterFor(formatIndex,lineIndex);
	if (hs == null) return;
	hs.applySpecialFormat(this,lineIndex,theLine,format);
}
//-------------------------------------------------------------------
protected void drawSpecialFormat(int formatIndex,int lineIndex,DisplayLine theLine,FormattedTextSpecs format,Graphics g,Color background)
//-------------------------------------------------------------------
{
	TextFormatter hs = getTextFormatterFor(formatIndex,lineIndex);
	if (hs == null) return;
	hs.drawSpecialFormat(this,lineIndex,theLine,format,g,background);
}

Hashtable imageFormatters = new Hashtable();

//===================================================================
public void refresh(IImage image,int options)
//===================================================================
{
	if (!Gui.requestPaint(this)) return;
	TextFormatter [] found = (TextFormatter [])imageFormatters.get(image);
	if (found != null){
		for (int i = 0; i<found.length; i++)
			found[i].repaint(this,null);
	}
}
//===================================================================
public void stopAniImages()
//===================================================================
{
	for (Enumeration it = imageFormatters.keys(); it.hasMoreElements();){
		Object obj = it.nextElement();
		if (obj instanceof OnScreenImage)
			((OnScreenImage)obj).changeRefresher(null,this);
	}
}
//===================================================================
public void formClosing()
//===================================================================
{
	stopAniImages();
	super.formClosing();
}
//===================================================================
public void imageSet(TextFormatter formatter)
//===================================================================
{
	if (formatter.image instanceof OnScreenImage){
		OnScreenImage ai = (OnScreenImage)formatter.image;
		TextFormatter [] was = (TextFormatter [])imageFormatters.get(ai);
		TextFormatter [] now = new TextFormatter[was == null ? 1 : was.length+1];
		now[0] = formatter;
		if (was != null) System.arraycopy(was,0,now,1,was.length);
		imageFormatters.put(ai, now);
		//ai.displayControl = this;
		ai.setRefresher(this);
		refresh(ai,0);
	}
}
//===================================================================
public TextFormatter addTextFormatter(TextFormatter formatter)
//===================================================================
{
	formatters.add(formatter);
	if (formatter.image instanceof OnScreenImage) imageSet(formatter);
	return formatter;
}
//===================================================================
public TextFormatter addTextFormatter(int lineIndex,int characterIndex,int length,TextFormatter dest)
//===================================================================
{
	if (dest == null) dest = new TextFormatter(lineIndex,characterIndex,length);
	else {
		dest.startLine = lineIndex;
		dest.startCharacter = characterIndex;
		dest.endCharacter = characterIndex+length-1;
		dest.numLines = 1;
	}
	return addTextFormatter(dest);
}
//===================================================================
public TextFormatter addTextFormatter(int lineIndex,int characterIndex,int length,int fontFlags,int lineFlags)
//===================================================================
{
	return addTextFormatter(new TextFormatter(lineIndex,characterIndex,length,fontFlags,lineFlags));
}
//===================================================================
public TextFormatter addTextFormatter(int lineIndex,int numLines,int fontFlags,int lineFlags)
//===================================================================
{
	return addTextFormatter(new TextFormatter(lineIndex,numLines,fontFlags,lineFlags));
}

//===================================================================
public void clearTextFormatters()
//===================================================================
{
	formatters.clear();
	stopAniImages();
	imageFormatters.clear();
}
/*
//===================================================================
public void removeTextFormatter(TextFormatter hs)
//===================================================================
{
	formatters.remove(hs);
}
*/
//===================================================================
public Iterator getTextFormatters()
//===================================================================
{
	return formatters.iterator();
}
//===================================================================
public TextFormatter addTextFormatter(int indexInFullText,int length,TextFormatter dest)
//===================================================================
{
	Dimension d = getIndexLocation(indexInFullText,null);
	if (d == null) return null;
	return addTextFormatter(d.height,d.width,length,dest);
}

//------------------------------------------------------------------
protected void splitLines(int width)
//------------------------------------------------------------------
{
	super.splitLines(width);
	postEvent(new ControlEvent(LINES_SPLIT,this));
}

private int[] formatterOffsets;
private IntArray formatterRefs;
/**
* This tells the TextDisplay that formatters have been set and so it can optimize
* itself based on this.
**/
//===================================================================
public void formattersSet()
//===================================================================
{
	formatterOffsets = new int[numLines*2];
	formatterRefs = new IntArray();
	formatterRefs.add(0);
}
//-------------------------------------------------------------------
protected void getColors(boolean hasFocus,int flags)
//-------------------------------------------------------------------
{
	super.getColors(hasFocus,flags);
	colors[0] = getForeground();
	colors[1] = pageColor;
	colors[2] = colors[1];
	colors[3] = Color.DarkBlue;//colors[0];
}

protected TextFormatter mouseOver;

//-------------------------------------------------------------------
protected void mouseMovedOnOff(TextFormatter tf,boolean movedOn){}
//-------------------------------------------------------------------

//===================================================================
public void onPenEvent(PenEvent ev)
//===================================================================
{
	if (ev.type == ev.PEN_MOVE){
		Vector v = getTextFormatter(new Point(ev.x,ev.y),null);
		//ewe.sys.Vm.debug(v.toString());
		for (int i = 0; i<v.size(); i++){
			TextFormatter hs = (TextFormatter)v.get(i);
				if (hs.cursor != 0) {
					setCursor(hs.cursor);
					if (mouseOver != hs) {
						if (mouseOver != null) mouseMovedOnOff(mouseOver,false);
						mouseMovedOnOff(mouseOver = hs,true);
					}
					return;
				}
		}
		if (mouseOver != null) mouseMovedOnOff(mouseOver,false);
		mouseOver = null;
	}else if (ev.type == ev.PEN_MOVED_OFF){
		if (mouseOver != null) mouseMovedOnOff(mouseOver,false);
		mouseOver = null;
	}
	super.onPenEvent(ev);
}


/**
 * This tells the display to go to the specified anchor.
 * @param anchorName The name of the anchor.
 * @return true if the anchor is in the current document. False otherwise.
 */
//===================================================================
public boolean goToAnchor(String anchorName)
//===================================================================
{
	String lookFor = anchorName.toUpperCase();
	if (lookFor.startsWith("#")) lookFor = lookFor.substring(1);
	for (Iterator it = getTextFormatters(); it.hasNext();){
		Object got = it.next();
		if (got instanceof HotSpot){
			HotSpot an = (HotSpot)got;
			String anchor = an.data == null ? null : an.data.toString().toUpperCase();
			if (anchor == null || anchor.length() < 2) continue;
			if (anchor.charAt(0) == '!' && anchor.substring(1).equals(lookFor)){
				goToLine(an.startLine);
				return true;
			}
		}
	}
	return false;
}

//-------------------------------------------------------------------
protected boolean hotspotPressed(HotSpot hs,Point where)
//-------------------------------------------------------------------
{
	postEvent(new HotSpotEvent(HotSpotEvent.PRESSED,hs));
	if (hs.data != null && hs.data.toString().startsWith("#")){
		markHistory();
		boolean ret = goToAnchor(hs.data.toString());
		if (!ret) deleteLastHistory();
		return ret;
	}
	return false;
}
/**
 * Scroll to the specified line. An entry in the "goBack" history will be made with the
 * current position.
 * @param line The line to go to.
 * @return true if scrolled, false otherwise.
 */
//===================================================================
public boolean goToLine(int line)
//===================================================================
{
	//markHistory();
	scrollTo(line,false);
	return true;
}
/**
 * Clear the goBack history.
 */
//===================================================================
public void clearHistory()
//===================================================================
{
	states.clear();
}
/**
 * Mark the current location in the goBack history.
 */
//===================================================================
public void markHistory()
//===================================================================
{
	states.add(getState());	
}
Vector states = new Vector();
//===================================================================
public void deleteLastHistory()
//===================================================================
{
	int sz = states.size();
	if (sz != 0) states.removeElementAt(sz-1);
	if (states.size() == 0) 
		markHistory();
}
/**
* This goes back one place in the saved states for the HtmlDisplay.
**/
//===================================================================
public void goBack()
//===================================================================
{
	int sz = states.size();
	if (sz == 0) scrollTo(0,false);
	else{
		setState(states.get(sz-1));
		states.removeElementAt(sz-1);
	}
	if (states.size() == 0) 
		markHistory();
}

/**
* This is a type of Control event. It is sent after the text has been split into separate lines
* by the TextDisplay. It can be used to trigger the setting up of the TextFormatters.
**/
public static final int LINES_SPLIT = 310;

//##################################################################
public class HotSpotEvent extends ControlEvent{
//##################################################################


public HotSpot hotSpot;

//-------------------------------------------------------------------
HotSpotEvent(int type,HotSpot spot)
//-------------------------------------------------------------------
{
	super(type,TextDisplay.this);
	this.hotSpot = spot;
}

//##################################################################
}
//##################################################################

//===================================================================
public void penPressed(Point where)
//===================================================================
{
	Point p = where;
	Vector v = getTextFormatter(where,null);
	//ewe.sys.Vm.debug("-----------------");
	//ewe.sys.Vm.debug(v.toString());
	for (int i = 0; i<v.size(); i++){
		TextFormatter hs = (TextFormatter)v.get(i);
		if (hs instanceof HotSpot)
			if (hotspotPressed((HotSpot)hs,p)){
				clearSelection();
				break;
			}
	}
	super.penPressed(where);
}

//===================================================================
public Object getToolTip(int x,int y)
//===================================================================
{
	Vector v = getTextFormatter(new Point(x,y),null);
	for (int i = 0; i<v.size(); i++){
		TextFormatter hs = (TextFormatter)v.get(i);
		Object tt = hs.getToolTip();
		if (tt != null) return tt;
	}
	return super.getToolTip(x,y);
}
/**
* Call this to indicate that something about the display has changed. By
* default it simply does a repaintNow(), but HtmlDisplay will update its background images etc.
**/
//===================================================================
public void displayPropertiesChanged()
//===================================================================
{
	repaintNow();
}

/*
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	TextDisplay td; 
	f.addLast(new ScrollBarPanel(td = new TextDisplay())).setPreferredSize(240,320);
	td.font = new Font("Times New Roman",Font.PLAIN,20);
	td.wrapToScreenSize = false;
	td.setText(
"Copyright (c) 2001 Michael L Brereton  All rights reserved.\n"+
"\n"+
"This software is furnished under the Gnu General Public License, Version 2, June 1991,\n"+
"and may be used only in accordance with the terms of that license. This source code\n"+
"must be distributed with a copy of this license. This software and documentation, \n"+
"and its copyrights are owned by Michael L Brereton and are protected by copyright law.\n"+
"\n"+
"If this notice is followed by a Wabasoft Copyright notice, then this software\n"+
"is a modified version of the original as provided by Wabasoft. Wabasoft also \n"+
"retains all rights as stipulated in the Gnu General Public License. These modifications\n"+
"were made to the Version 1.0 source code release of Waba, throughout 2000 and up to May \n"+
"2001.\n"+
"\n"+
"THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED \"AS IS\" WITHOUT WARRANTY\n"+
"AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,\n"+
"OR AGAINST INFRINGEMENT. MICHAEL L BRERETON ASSUMES NO RESPONSIBILITY FOR THE USE OR\n"+
"INABILITY TO USE THIS SOFTWARE. MICHAEL L BRERETON SHALL NOT BE LIABLE FOR INDIRECT,\n"+
"SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.\n"+
"\n"+
"MICHAEL L BRERETON SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,\n"+
"MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM\n"+
"ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF\n"+
"YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY MICHAEL L BRERETON.\n"
	);
	TextFormatter tf;
	//TextFormatter tf = td.addTextFormatter(3,4,TextFormatter.FONT_CHANGE|Font.BOLD,TextFormatter.LINE_ADD_FIRST_LINE_HEADER);
	//tf.leftMarginShift = 20;
	//tf.data = tf.DIAMOND;
	tf = td.addTextFormatter(8,3,10,TextFormatter.FONT_CHANGE|Font.ITALIC|Font.BOLD,0);
	tf.numLines = 4;
	tf.endCharacter = 1;
	tf.cursor = ewe.sys.Vm.HAND_CURSOR;
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################


