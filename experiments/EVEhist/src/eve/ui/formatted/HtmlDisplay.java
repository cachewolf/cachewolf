package eve.ui.formatted;
import eve.data.PropertyList;
import eve.fx.Color;
import eve.fx.IImage;
import eve.sys.Handle;
//##################################################################
public class HtmlDisplay extends TextDisplay  {
//##################################################################

{
	forcedActualWidth = 5000;
	spacing = 2;
}
private FormattedTextMaker maker;

public PropertyList headerData = new PropertyList();
public PropertyList bodyData = new PropertyList();
/*
public void repaintNow()
{
	if ((getModifiers(true) & Invisible) != 0) return;
	new Exception().printStackTrace();
	super.repaintNow();
}
*/
//===================================================================
public HtmlDisplay(){}
//===================================================================
public HtmlDisplay(int rows,int columns)
//===================================================================
{
	super(rows,columns);
}
/**
* Call this before setting properties for the HTML decoder.
**/
//===================================================================
public void startHtml()
//===================================================================
{
	if (maker != null) maker.removeFrom(this);
	maker = new FormattedTextMaker();
}
/**
* Make sure you call startHtml(), before calling this.
* After you do that you can then set properties for the decoder. These
* include:<dl>
* <dt>"documentRoot"<dd>The root of the document. Images with relative path
* names will be searched for relative to this path.
* </dl>
**/
//===================================================================
public PropertyList getDecoderProperties()
//===================================================================
{
	if (maker != null) return maker.properties;
	else return PropertyList.nullPropertyList;
}
//===================================================================
public void addHtml(String htmlText,Handle h)
//===================================================================
{
	maker.parseHtml(this,htmlText,h);
}
//===================================================================
public FormattedTextMaker endHtml()
//===================================================================
{
	maker.endHtml();
	return endMaker();
}

//-------------------------------------------------------------------
FormattedTextMaker endMaker()
//-------------------------------------------------------------------
{
	headerData = maker.headerData;
	bodyData = maker.bodyData;
	try{
		pageColor = (Color)PropertyList.getValue(bodyData,"background",Color.White);
		foreGround =(Color)PropertyList.getValue(bodyData,"foreground",Color.Black); 
		backgroundImage = (IImage)PropertyList.getValue(bodyData,"backgroundImage",null); 
	}catch(Exception e){}
	int was = modify(Invisible,0);
	maker.addTo(this);
	scrollTo(0,false);
	restore(was,Invisible);
	repaintDataNow();
	
	//clearHistory();
	//markHistory();
	return maker;
}
//===================================================================
public void displayPropertiesChanged()
//===================================================================
{
	try{
		pageColor = (Color)PropertyList.getValue(bodyData,"background",Color.White);
		foreGround =(Color)PropertyList.getValue(bodyData,"foreground",Color.Black); 
		backgroundImage = (IImage)PropertyList.getValue(bodyData,"backgroundImage",null); 
	}catch(Exception e){}
	super.displayPropertiesChanged();
}
//===================================================================
public void setHtml(String htmlText)
//===================================================================
{
	setHtml(htmlText,null,new Handle());
}
//===================================================================
public FormattedTextMaker setHtml(String htmlText,PropertyList properties,Handle h)
//===================================================================
{
	startHtml();
	if (properties != null) getDecoderProperties().set(properties);
	addHtml(htmlText,h);
	return endHtml();
}

//===================================================================
public void setHtml(FormattedTextMaker maker,PropertyList properties)
//===================================================================
{
	if (this.maker != null) this.maker.removeFrom(this);
	this.maker = maker;
	if (properties != null) getDecoderProperties().set(properties);
	endMaker();
}

//===================================================================
public void formClosing()
//===================================================================
{
	super.formClosing();
	if (maker != null) maker.removeFrom(this);
}
//===================================================================
public void setPlainText(String text)
//===================================================================
{
	if (maker != null) maker.removeFrom(this);
	setText(text);
	//markHistory();
}
//##################################################################
}
//##################################################################

