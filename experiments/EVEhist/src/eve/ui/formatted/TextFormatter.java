package eve.ui.formatted;
import eve.data.DataObject;
import eve.fx.Color;
import eve.fx.DisplayLine;
import eve.fx.Font;
import eve.fx.FontMetrics;
import eve.fx.FormattedTextSpecs;
import eve.fx.Graphics;
import eve.fx.IImage;
import eve.fx.Pen;
import eve.fx.PixelBuffer;
import eve.fx.Rect;
import eve.ui.Control;
import eve.ui.TextPad;
import eve.util.Range;

/**
* A TextFormatter is used to format all or some of the text of a single line of an TextPad
* display.
**/
	//##################################################################
	public class TextFormatter extends DataObject{
	//##################################################################
	/**
	* The first line of text covered by the formatter - where 0 indicates the first line of
	* the display.
	**/
	public int startLine;
	/**
	* The number of lines covered by the formatter. - if it is greater than 1, then this is
	* a block level formatter and the "character" and "length" members are not used. 
	**/
	public int numLines;
	/**
	* The first character in the line being covered by the formatter. If this is -1 then it is
	* assumed that this formatter will not have any effect on the displayed text, but is really only used
	* as some sort of flag or marker for the line or lines
	**/
	public int startCharacter;
	/**
	* The index of the last character in the last line being covered by the formatter. If this is -1 then it
	* is assumed that the formatter is applied to ALL characters in ALL the lines covered by
	* the formatter.
	**/
	public int endCharacter;
	/**
	* If this is not null then the text will be redrawn in this color <b>if</b> the FONT_CHANGE
	* flag is set in fontFlags.
	**/
	public Color color = null;
	/**
	* This is a fontFlag. Other fontFlags could be Font.BOLD, Font.UNDERLINE and Font.ITALIC.
	**/
	public static final int FONT_CHANGE = 0x8000;
	public static final int FONT_FREEZE = 0x10000;
	/**
	* This can be a combination of FONT_CHANGE along with Font.BOLD, Font.UNDERLINE and Font.ITALIC.
	**/
	public int fontFlags = 0;
	/**
	* This can be a combination of the LINE_XXX values.
	**/
	public int lineFlags = 0;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_CENTERED = 0x1;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_RIGHT_ALIGNED = 0x2;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_RESPLIT = 0x4;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_ADD_FIRST_LINE_HEADER = 0x8;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_LEFT_MARGIN_SHIFT_IS_CHARACTERS = 0x10;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_HEADER_RIGHT_ALIGN = 0x20;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_DRAW_RULE = 0x40;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_ALIGN_NOW = 0x80;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_CANCEL_ALIGNMENTS = 0x100;
	/**
	* This is a possible lineFlag.
	**/
	public static final int LINE_BACKGROUND_COLOR_CHANGE = 0x200;
	
	public Color backgroundColor;
	
	/**
	 * Returns if this need to be applied at the start or during the line.
	 */
	//===================================================================
	public boolean applyBefore()
	//===================================================================
	{
		//if (true) return true;
		if ((lineFlags & (LINE_BACKGROUND_COLOR_CHANGE|LINE_DRAW_RULE|LINE_ADD_FIRST_LINE_HEADER)) != 0)	return true;
		if (leftMarginShift != 0) return true;
		if (image != null) return true;
		if ((fontFlags & FONT_CHANGE) != 0) return true;
		return false;
	}
	/**
	 * Returns if this need to be applied at the end of the line.
	 */
	//===================================================================
	public boolean applyAfter()
	//===================================================================
	{
		//if (true) return false;
		if ((lineFlags & (LINE_CANCEL_ALIGNMENTS|LINE_RIGHT_ALIGNED|LINE_CENTERED)) != 0) return true;
		return false;
	}
	//===================================================================
	public TextFormatter getEndFormatter(TextFormatter dest)
	//===================================================================
	{
		if (dest == null) dest = new TextFormatter();
		dest.lineFlags = (lineFlags & (LINE_CANCEL_ALIGNMENTS|LINE_RIGHT_ALIGNED|LINE_CENTERED))|LINE_ALIGN_NOW;
		return dest;
	}
	//===================================================================
	public TextFormatter getStartFormatter(TextFormatter dest)
	//===================================================================
	{
		if (dest == null) dest = new TextFormatter();
		dest.lineFlags = lineFlags;
		//lineFlags | LINE_ALIGN_NOW;
		return dest;
	}	
	public Object data;
	public Object toolTip = null;
	public int cursor = 0;
	public int leftMarginShift;
	public int resplitWidthChange;
	public String label;
	//
	// Used for displaying images.
	//
	public IImage image;
	/**
	* The line that the image is actually on.
	**/
	public int imageLine;
	/**
	* The character that comes after the image.
	**/
	public int imageChar;
	public int imageX;
	public int imageY;
	public int hSpace = 0, vSpace = 0, borderWidth = 0;
	
	public TextFormatter linkedTo;
	
	static IImage UnknownImage, BrokenImage;
	
	public boolean notUsed;
	
	//===================================================================
	public static IImage getUnknownImage()
	//===================================================================
	{
 		if (UnknownImage == null) 
			UnknownImage = Control.loadImage("eve/imagesmall.png");
		return UnknownImage;
	}
	//===================================================================
	public static IImage getBrokenImage()
	//===================================================================
	{
		if (BrokenImage == null){
			PixelBuffer pb = new PixelBuffer(getUnknownImage());
			Graphics g = pb.getDrawingBuffer(null,null,1.0);
			g.changePen(Color.Red,Pen.SOLID,2);
			g.drawLine(2,2,pb.getWidth()-4,pb.getHeight()-4);
			g.drawLine(2,pb.getHeight()-4,pb.getWidth()-4,2);
			pb.putDrawingBuffer(pb.PUT_BLEND);
			BrokenImage = pb.toPicture();
			pb.free();
		}
		return BrokenImage;
	}
	//===================================================================
	public String rangeToString()
	//===================================================================
	{
		return endCharacter == -1 ? startLine+"->"+(startLine+numLines-1) : "("+startLine+","+startCharacter+")->"+"("+(startLine+numLines-1)+","+endCharacter+")";
	}
	//===================================================================
	public String toString()
	//===================================================================
	{
		return label == null ?  rangeToString() : label;
	}
	/**
	* The amount to change the font size by (FONT_CHANGE must be set).
	**/
	public int fontSizeChange = 0;
	/**
	* The amount to change the font to (FONT_CHANGE must be set).
	**/
	public int fontSize = 0;
	/**
	* The name of the new font to use (FONT_CHANGE must be set).
	**/
	public String fontName = null;
	/**
	* If you set this to a Font, the fontFlags, fontSizeChange and fontName values are ignored
	* and this is used instead.
	**/
	public Font newFont;
	/**
	* The ID of the group this formatter belongs to.
	**/
	public int groupID;
	/**
	* The index of this formatter in the group this formatter belongs to.
	**/
	public int groupIndex;
	//===================================================================
	public TextFormatter()
	//===================================================================
	{
	}
	//===================================================================
	public TextFormatter(int line,int character,int length)
	//===================================================================
	{
		this.startLine = line;
		this.startCharacter = character;
		this.endCharacter = character+length-1;		
		this.numLines = 1;
	}
	//===================================================================
	public TextFormatter(int line,int character,int length,int fontFlags,int lineFlags)
	//===================================================================
	{
		this.startLine = line;
		this.startCharacter = character;
		this.endCharacter = character+length-1;		
		this.fontFlags = fontFlags;
		this.lineFlags = lineFlags;
		this.numLines = 1;
	}
	//===================================================================
	public TextFormatter(int startLine,int numLines,int fontFlags,int lineFlags)
	//===================================================================
	{
		this.startLine = startLine;
		this.numLines = numLines;
		this.startCharacter = 0;
		this.endCharacter = -1;
		this.fontFlags = fontFlags;
		this.lineFlags = lineFlags;
	}
	//===================================================================
	public Object getToolTip()
	//===================================================================
	{
		return toolTip;
	}


//===================================================================
public Font getFont(Font baseFont,int useSize)
//===================================================================
{
	if ((fontFlags & FONT_CHANGE) == 0) return baseFont;
	Font f = baseFont;
	int ff = fontFlags & (Font.ITALIC|Font.BOLD|Font.PLAIN|Font.UNDERLINE);
	int fs = useSize == 0 ? (fontSize == 0 ? f.getSize() : fontSize)+fontSizeChange : useSize;
	return new Font(fontName == null ? f.getName() : fontName,f.getStyle()|ff,fs);
}

//-------------------------------------------------------------------
protected FontMetrics getFontMetrics(TextPad pad,FontMetrics baseFont)
//-------------------------------------------------------------------
{
	if (baseFont == null) baseFont = pad.getFontMetrics();
	int ln = pad.getLineHeight();
	for (int fs = 0;; fs--){
		Font f = getFont(baseFont.getFont(),fs);
		FontMetrics fm = baseFont.getNewFor(f);
		if (fontSizeChange == 0){
			if (fm.getCharWidth('X') <= baseFont.getCharWidth('X')) return fm;
		}else{
			if (fm.getHeight() <= ln) return fm;
		}
		fs = f.getSize();
		if (fs <= 5) return fm;
	}
}
/*
//-------------------------------------------------------------------
protected FontMetrics getFontMetrics(FormattedTextSpecs format)
//-------------------------------------------------------------------
{
	Font f = getFont(format.metrics.getFont());
	return format.metrics.getNewFor(f);
}
*/
//===================================================================
public Range getCharRange(int lineIndex,DisplayLine theLine,Range dest)
//===================================================================
{
	if (notUsed) {
		return null;
	}
	if (startCharacter == -1 || numLines == 0) return null;
	if (dest == null) dest = new Range(0,0);
	int ll = theLine.length();
	dest.first = startCharacter;
 	dest.last = endCharacter;
	if (lineIndex == startLine){ // This is the first line in the sequence.
		if (numLines != 1) dest.last = ll-1;
	}else if (lineIndex == startLine+numLines-1){ // This is the last line in the sequence.
		dest.first = 0;
	}else if (lineIndex > startLine && lineIndex < startLine+numLines-1){ // This must be between the first and lastLines.
		dest.first = 0;
		dest.last = ll-1;
	}else
		return null;
	if (endCharacter == -1){ //Covers the entire line for all lines.
		dest.first = 0;
		dest.last = ll-1;
	}
	if (dest.last > ll-1) dest.last = ll-1;
	return dest;
}
private static Range range = new Range(0,0);
//private static int [] myPos;
//-------------------------------------------------------------------
protected void applySpecialFormat(TextPad source,int lineIndex,DisplayLine theLine,FormattedTextSpecs format)
//-------------------------------------------------------------------
{
	try{
		if (getCharRange(lineIndex,theLine,range) == null) {
			return;
		}
		
		if (image == null){
			int character = range.first, length = range.last-range.first+1;
			if (length < 0) return;
			//
			int [] ft = format.calculatedPositions;
			FontMetrics fm = null;
			if ((fontFlags & FONT_CHANGE) != 0){
				String part = length == 0 ? "" : theLine.substring(character,character+length);
				fm = getFontMetrics(source,format.metrics);
				int [] got = fm.getFormattedTextPositions(part,format,null);
				format.changeAndAdjustPositions(got,character,length);
			}
			//
			if (lineFlags == 0 && image == null && leftMarginShift == 0) return;
			//
			int ll = theLine.length();
			int fullWidth = ll == 0 ? 0 : format.calculatedPositions[ll-1];
			fullWidth += format.firstCharPosition;
			//
			//ewe.sys.Vm.debug(format.lineFlags+"");
			if ((format.lineFlags & format.LINE_FLAG_CANCEL_ALIGNMENTS) == 0){
				if ((lineFlags & (LINE_CENTERED|LINE_ALIGN_NOW)) == (LINE_CENTERED|LINE_ALIGN_NOW)){
					format.leftMargin = 
					//((source.getAvailableWidth()-source.leftMargin-source.rightMargin-fullWidth)/2)+source.leftMargin;
					((format.displayLineWidth-fullWidth-format.leftMargin-format.rightMargin)/2)+format.leftMargin;
				}else if ((lineFlags & (LINE_RIGHT_ALIGNED|LINE_ALIGN_NOW)) == (LINE_RIGHT_ALIGNED|LINE_ALIGN_NOW))
					format.leftMargin = format.displayLineWidth-fullWidth-format.rightMargin;//+format.leftMargin;
			}
			
			if ((lineFlags & LINE_LEFT_MARGIN_SHIFT_IS_CHARACTERS) != 0){
				if (fm == null) fm = getFontMetrics(source,format.metrics);
				format.leftMargin += leftMarginShift*fm.getCharWidth('X');
			}else{
				format.leftMargin += leftMarginShift;
			}
			if ((lineFlags & (LINE_CANCEL_ALIGNMENTS|LINE_ALIGN_NOW)) == (LINE_CANCEL_ALIGNMENTS|LINE_ALIGN_NOW))
				format.lineFlags |= format.LINE_FLAG_CANCEL_ALIGNMENTS;
			if ((lineFlags & LINE_BACKGROUND_COLOR_CHANGE) != 0) 
				format.backgroundColor = backgroundColor;
			//
		}else { //if (image != null){
			FormattedTextSpecs f = format;
		/*
	 		if (imageLine > lineIndex){
				DisplayLine nl = (DisplayLine)DisplayLine.getNext(theLine,imageLine-lineIndex);
				f = source.getTextPositions(imageLine,nl,new FormattedTextSpecs(),myPos);
			}else if (imageLine < lineIndex){
				DisplayLine pl = (DisplayLine)DisplayLine.getPrev(theLine,lineIndex-imageLine);
				f = source.getTextPositions(imageLine,pl,new FormattedTextSpecs(),myPos);
			}
			if (f != format) myPos = f.calculatedPositions;
		*/
			//
			int allocatedWidth = image.getWidth()+(hSpace+borderWidth)*2;
			if (imageChar == -1){ //Indicates a left or right aligned image.
				if (imageX == 0) // Left aligned
					f.leftMargin += allocatedWidth;//f.insertSpace(0,allocatedWidth);
				else
					f.rightMargin += allocatedWidth;
				//f.extraSpaceUsed += allocatedWidth;
			}else
				if (imageLine == lineIndex){
					f.insertSpace(imageChar,allocatedWidth);
					f.extraSpaceUsed += allocatedWidth;
				}

			//
		}
		//
		if (format.leftMargin < 0) format.leftMargin = 0;
	}catch(Exception e){
		
	}
}

//===================================================================
public static final Object SOLID_CIRCLE = new Object();
public static final Object CIRCLE = new Object();
public static final Object DIAMOND = new Object();
//===================================================================

//===================================================================
public boolean isOnImage(TextPad source,int xPosition,FormattedTextSpecs specs)
//===================================================================
{
	if (imageChar == -1){ //Left or right aligned image.
				int ix = (imageX < 0) ? 
					source.getAvailableWidth()-specs.rightMargin+hSpace:
					source.leftMargin+hSpace;
				return xPosition >= ix && xPosition <= ix+borderWidth*2+image.getWidth(); 
	}else{
		int ix = imageX+hSpace;
		return xPosition >= ix && xPosition <= ix+borderWidth*2+image.getWidth(); 
	}
}
//-------------------------------------------------------------------
protected void drawSpecialFormat(TextPad source,int lineIndex,DisplayLine theLine,FormattedTextSpecs format,Graphics g,Color background)
//-------------------------------------------------------------------
{
	try{
	if (getCharRange(lineIndex,theLine,range) == null) return;
	if (image == null){
		int character = range.first, length = range.last-range.first+1;
		if (length < 0) return;
		//
		FontMetrics fm = null;
		if ((fontFlags & FONT_CHANGE) != 0){
			Color was = g.getColor(Color.getCached());
			fm = getFontMetrics(source,format.metrics);
			Font f = fm.getFont();
			//g.setColor(background);
			//g.fillRect(format.leftMargin+(character == 0 ? 0 : format.calculatedPositions[character-1]),0,format.widthOf(character,length),format.displayLineHeight);
			//if (color == null) color = was;
			Color cc = background.equals(source.pageColor) ? color : source.pageColor;
			if (cc == null) cc = was;
			g.setColor(cc);
			g.setFont(f);
			g.drawFormattedText(format.charsToDraw,character,length,format.leftMargin+format.firstCharPosition,0,format);
			char ctd = '\t';
			for (int i = character; i<character+length; i++)
				format.charsToDraw[i] = ctd;
			g.setColor(was);
			was.cache();
		}
		if (((lineFlags & LINE_ADD_FIRST_LINE_HEADER) != 0) && (lineIndex == startLine) && (data != null)){
			String str = data.toString();
			int blockWidth = format.displayLineHeight/3;
			if (fm == null) fm = getFontMetrics(source,format.metrics);
			int charSpace = fm.getCharWidth('X');
			int ls = leftMarginShift;
			if ((lineFlags & LINE_LEFT_MARGIN_SHIFT_IS_CHARACTERS) != 0)
				ls = leftMarginShift*charSpace;
			if ((lineFlags & LINE_HEADER_RIGHT_ALIGN) != 0){
				if (data instanceof String) {
					int full = charSpace+fm.getTextWidth(str);
					g.drawText(str,format.leftMargin-full,0);
				}else if (data == SOLID_CIRCLE) {
					g.fillEllipse(format.leftMargin-blockWidth-charSpace,(format.displayLineHeight-blockWidth)/2,blockWidth,blockWidth);
				}
			}else{
				if (data instanceof String)
					g.drawText(str,format.leftMargin-ls,0);
				else if (data == SOLID_CIRCLE)
					g.fillEllipse(format.leftMargin-ls,(format.displayLineHeight-blockWidth)/2,blockWidth,blockWidth);
				else if (data == CIRCLE)
					g.drawEllipse(format.leftMargin-ls,(format.displayLineHeight-blockWidth)/2,blockWidth,blockWidth);
				else if (data == DIAMOND)
					g.drawDiamond(new Rect(format.leftMargin-ls,(format.displayLineHeight-blockWidth)/2,blockWidth,blockWidth),g.All);
			}
		}
		if ((lineFlags & LINE_DRAW_RULE) != 0){
			g.drawLine(source.leftMargin,format.displayLineHeight/2-1,format.displayLineWidth-source.leftMargin,format.displayLineHeight/2-1);
			g.drawLine(source.leftMargin,format.displayLineHeight/2+1,format.displayLineWidth-source.leftMargin,format.displayLineHeight/2+1);
		}
	}else {//if (image != null){
		FormattedTextSpecs f = format;
 		if (imageLine > lineIndex){
			DisplayLine nl = (DisplayLine)DisplayLine.getNext(theLine,imageLine-lineIndex);
			//ewe.sys.Vm.debug(nl.line);
			f = source.getTextPositions(imageLine,nl,new FormattedTextSpecs(),new int[0],true);
		}else if (imageLine < lineIndex){
			DisplayLine pl = (DisplayLine)DisplayLine.getPrev(theLine,lineIndex-imageLine);
			//ewe.sys.Vm.debug(pl.line);
			f = source.getTextPositions(imageLine,pl,new FormattedTextSpecs(),new int[0],true);
		}//else ewe.sys.Vm.debug("Yes, this is the line");
		//if (f != format) myPos = f.calculatedPositions;
		int y = (lineIndex-startLine)*source.getLineHeight();
		int allocatedWidth = image.getWidth()+(hSpace+borderWidth)*2;
		if (imageChar > -1){
			imageX = imageChar == 0 ? 0 : f.calculatedPositions[imageChar-1];
			imageX += f.leftMargin+f.firstCharPosition-allocatedWidth;
			image.draw(g,imageX+hSpace+borderWidth,-y+imageY+vSpace+borderWidth,0);
		}else{
			//ewe.sys.Vm.debug("Line: "+lineIndex+", of: "+numLines);
			if (imageX < 0)
				image.draw(g,source.getAvailableWidth()-f.rightMargin+hSpace+borderWidth,-y+imageY+vSpace+borderWidth,0);
			else
				image.draw(g,source.leftMargin+hSpace+borderWidth/*-hSpace-borderWidth-image.getWidth()*/,-y+imageY+vSpace+borderWidth,0);
		}
		//ewe.sys.Vm.debug(numLines+", Range: "+range);
		//image.draw(g,format.leftMargin,0,
	}
	}catch(Exception e){
		//e.printStackTrace();
	}
}

//-------------------------------------------------------------------
protected void replace(TextPad pad, DisplayLine firstLine, int numLines, DisplayLine newLines)
//-------------------------------------------------------------------
{
	pad.replaceDisplayLines(newLines,firstLine,(DisplayLine)firstLine.getNext(firstLine,numLines));
}
//-------------------------------------------------------------------
protected DisplayLine resplit(TextPad pad, DisplayLine firstLine, int numLines,int forWidth,String newText)
//-------------------------------------------------------------------
{
	FontMetrics fm = pad.getFontMetrics();
	DisplayLine dl = pad.resplit(firstLine,numLines,forWidth,null,getFontMetrics(pad,null),newText);
	return dl;
}
/*
//-------------------------------------------------------------------
protected DisplayLine resplit(TextPad pad, DisplayLine firstLine, int numLines,int lineIndex,DisplayLine.WidthProvider wp,String newText)
//-------------------------------------------------------------------
{
	String text = newText != null ? newText : firstLine.concatenate(firstLine,0,numLines);
	FontMetrics fm = pad.getFontMetrics();
	DisplayLine dl = pad.splitLines(text,0,
		fm.getNewFor(getFont(fm.getFont())),null,wp);
	pad.replaceDisplayLines(dl,firstLine,(DisplayLine)firstLine.getNext(firstLine,numLines-1));
	return dl;
}
*/
/**
* This gives the TextFormatter the chance to actually alter the lines being displayed.
**/
//===================================================================
public DisplayLine creatingFor(TextPad pad, DisplayLine firstLine, int numLines,int lineIndex)
//===================================================================
{
	if (((lineFlags & LINE_RESPLIT) != 0)){
		int size = firstLine.displayWidth-leftMarginShift;//pad.getDim(null).width-(pad.spacing*2)-pad.leftMargin-leftMarginShift-pad.rightMargin+resplitWidthChange;
		//ewe.sys.Vm.debug("Resplit: "+size);
		return resplit(pad,firstLine,numLines,size,null);
	}//else if (image != null){
		//DisplayLine f = pad.addDisplayLine("",firstLine);
		//pad.addDisplayLine("",(DisplayLine)firstLine.next);
		//return f;
	//}
	return firstLine;
}
//===================================================================
public void repaint(TextPad pad, Graphics gr)
//===================================================================
{
	int first = pad.getTopLine();
	int max = first+pad.getScreenRows()+1;
	for (int i = startLine; i<startLine+numLines; i++){
		if (i < first || i >= max) continue;
		pad.paintLine(gr,i);
	}
}

//##################################################################
}
//##################################################################

