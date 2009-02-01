package eve.ui.formatted;
import eve.fx.Color;
import eve.fx.gui.Cursor;

/**
* A HotSpot represents a Hyperlink to a location within this document (if the link starts with '#')
* or to another document. It is also used to represent a location within a document that another
* HotSpot can link to (in which case it is effectively invisible).<p>
* 
* The hyperlink that the HotSpot refers to is stored in the "data" member variable as a String.
* If this String starts with '!' then it is a book mark in the document that other hyperlinks
* will refer to. If it does not start with '!' then it is a clickable hyperlink to another
* location or document.
**/

	//##################################################################
	public class HotSpot extends TextFormatter{
	//##################################################################
	public static Color hotColor = new Color(0,0,255);
	{
		cursor = Cursor.HAND_CURSOR;
		color = hotColor;
	}
	
	//===================================================================
	public HotSpot()
	//===================================================================
	{
	}
	//===================================================================
	public HotSpot(int line,int character,int length)
	//===================================================================
	{
		super(line,character,length);
	}
		/**
	 * Returns if this need to be applied at the start or during the line.
	 */
	//===================================================================
	public boolean applyBefore()
	//===================================================================
	{
		return true;
	}
/*
	//===================================================================
	public Object getToolTip()
	//===================================================================
	{
		Object got = super.getToolTip();
		if (got != null) return got;
		return (cursor != 0) ? data.toString() : null;
	}
*/
	//##################################################################
	}
	//##################################################################

