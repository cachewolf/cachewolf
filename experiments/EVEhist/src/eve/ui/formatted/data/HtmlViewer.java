
package eve.ui.formatted.data;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import eve.data.PropertyList;
import eve.fx.Dimension;
import eve.fx.Font;
import eve.fx.Point;
import eve.io.AsciiCodec;
import eve.io.File;
import eve.io.StreamUtils;
import eve.io.TextCodec;
import eve.net.Link;
import eve.net.URL;
import eve.sys.Handle;
import eve.sys.Task;
import eve.sys.TimeOut;
import eve.sys.Vm;
import eve.ui.Application;
import eve.ui.CardPanel;
import eve.ui.CellPanel;
import eve.ui.Control;
import eve.ui.Gui;
import eve.ui.InputStack;
import eve.ui.Label;
import eve.ui.MessageBox;
import eve.ui.ProgressBarForm;
import eve.ui.ProgressDisplay;
import eve.ui.ScrollBarPanel;
import eve.ui.Window;
import eve.ui.data.AppForm;
import eve.ui.data.Editor;
import eve.ui.data.FontChooser;
import eve.ui.filechooser.FileChooser;
import eve.ui.formatted.FormattedTextMaker;
import eve.ui.formatted.HotSpot;
import eve.ui.formatted.HtmlDisplay;
import eve.ui.formatted.TextFormatter;
import eve.util.ByteArray;
import eve.util.CharArray;
import eve.util.mString;
/**
This is a useful Form that you can use to display HTML files. It consists
of an HtmlDisplay along with optional controls.<p>
The 
**/
//##################################################################
public class HtmlViewer extends AppForm {
//##################################################################

HtmlDisplay display;
//public FileSaver saver = new FileSaver();

public PropertyList htmlProperties = new PropertyList();
public FontChooser fontChooser;
/**
* Specifies if images should be displayed - true by default.
**/
public boolean showImages = true;
/**
* Specifies if animated images should be allowed - true on some systems by default.
**/
public boolean animatedImages = true;
/**
* A display option for the constructor.
**/
public final static int DISPLAY_NO_STATUS_BAR = 0x1;
/**
* A display option for the constructor.
**/
public final static int DISPLAY_NO_TOOL_BUTTONS = 0x2;
/**
* A display option for the constructor.
**/
public final static int DISPLAY_NO_TABS = 0x4;
/**
* A display option for the constructor.
**/
public final static int DISPLAY_NO_OPEN = 0x8;
public String status = "";
protected ProgressDisplay progress;
protected Control stopButton;
protected CardPanel statusCard;

//##################################################################
protected class HtmlViewerDisplay extends HtmlDisplay{
//##################################################################



//-------------------------------------------------------------------
HtmlViewerDisplay()
//-------------------------------------------------------------------
{
	super(20,60);
}
//-------------------------------------------------------------------
protected boolean hotspotPressed(HotSpot hs,Point where)
//-------------------------------------------------------------------
{
	if (super.hotspotPressed(hs,where)) return true;
	return HtmlViewer.this.hotspotPressed(hs,where);
}
//-------------------------------------------------------------------
protected void mouseMovedOnOff(TextFormatter tf,boolean movedOn)
//-------------------------------------------------------------------
{
	if (movedOn) status = mString.toString(tf.data);
	else status = "";
	toControls("status");
}
//===================================================================
public Object getState()
//===================================================================
{
	Object [] ret = new Object[2];
	ret[0] = currentURL;
	ret[1] = super.getState();
	return ret;
}
//===================================================================
public boolean setState(Object state)
//===================================================================
{
	Object [] st = (Object [])state;
	if (st[0] == null) return false;
	if (!st[0].equals(currentURL)){
		try{
			doOpen((String)st[0]).waitOn(Handle.Success);
			return super.setState(st[1]);
		}catch(Exception e){
			return false;
		}
	}else return super.setState(st[1]);
}
//##################################################################
}
//##################################################################

/**
* This is the codec that will be used to decode text. If it is null then an
* AsciiCodec will be used.
**/
public TextCodec codec;
/*
//===================================================================
public TextCodec getCodec()
//===================================================================
{
}
*/
/**
 * Get a codec to decode a Web document. The properties of the document
 * as provided by the Web Server are in the specified PropertyList parameter.
 * @param pl the properties for the document.
 * @return a non-null Codec for decoding the document. By default this returns
 * the value of the "codec" field. If that is null an AsciiCodec is returned.
 */
//===================================================================
public TextCodec getCodec(PropertyList pl)
//===================================================================
{
	TextCodec c = codec;
	if (c == null) c = new AsciiCodec();
	return c;
}

/**
 * This returns a String ONLY if the String is the name of a VALID URL resource. This could
 * be a file name.
 * @param root The current document root.
 * @param document The document being referred to.
 * @return the URL as a String if it is valid and can be retrieved by the Viewer.
 */
//-------------------------------------------------------------------
protected String toFullURL(String root,String document)
//-------------------------------------------------------------------
{
	File file = null;
	if (document.indexOf(':') != -1 || document.indexOf('/') == 0) file = getNewFile(document);
	else file = root.length() == 0 ? getNewFile(document) : getNewFile(root).getChild(document);
	if (!file.canRead()) return null;
	return file.getFullPath();
}
//-------------------------------------------------------------------
protected Handle openDocument(String url,PropertyList pl)
//-------------------------------------------------------------------
{
	try{
		File file = getNewFile(url);
		if (file.canRead()){
			String root = file.getParent();
			pl.set("documentRoot",root);
		}
		String nu = url.toLowerCase();
		for (int i = 0; i<images.length; i++)
			if (nu.endsWith(images[i])){
				String toOpen = "<head><title>"+url+"</title></head><body><img src=\""+
				//file.getFileExt()
				(file.canRead() ? file.getFileExt() : url)
				+"\"><br></body>";
				return new Handle(Handle.Succeeded,toOpen);
			}
		return StreamUtils.readAllBytes(file.toReadableStream(),null);
	}catch(Exception e){
		return new Handle(Handle.Failed,null);
	}
}
//-------------------------------------------------------------------
protected boolean hotspotPressed(HotSpot hs,Point where)
//-------------------------------------------------------------------
{
	if (hs.data == null) return false;
	String href = hs.data.toString();
	if (href == null) return false;
	display.markHistory();
	doOpen(href);
	return true;
}
protected Handle loading;
//-------------------------------------------------------------------
protected void noLoading()
//-------------------------------------------------------------------
{
	if (stopButton != null){
		stopButton.modify(Disabled,0);
		stopButton.repaintNow();
	}
	if (statusCard != null) statusCard.select("status");
}
//-------------------------------------------------------------------
protected void startingLoad(Handle h)
//-------------------------------------------------------------------
{
	if (statusCard != null) statusCard.select("progress");
	if (progress != null) progress.setTask(h,"Loading");
	if (stopButton != null){
		stopButton.modify(0,Disabled);
		stopButton.repaintNow();
	}
}
//-------------------------------------------------------------------
protected void endedLoad(Handle h)
//-------------------------------------------------------------------
{
	if (loading == h) noLoading();
}

String currentURL;

//===================================================================
public String getCurrentURL()
//===================================================================
{
	return currentURL;
}

//-------------------------------------------------------------------
protected void newDocumentLoaded(String url){currentURL = url;}
//-------------------------------------------------------------------

/**
 * Tell the viewer to load and display the URL. 
 * @param url The url which can be an http:// url or a file name. It can also be relative to
 * the current document.
 * @return A Handle that can be used to monitor the progress of the display.
 */
//===================================================================
public Handle setHtml(URL url)
//===================================================================
{
	return setHtml(url,null,null);	
}
/**
 * Tell the viewer to load and display the Link. A Link can be a URL in text form or it can
 * be a file name or an anchor ('#') to a bookmark in the current document.
 * @param link The link which can be an "http://" url or a file name. It can also be relative to
 * the current document.
 * @return A Handle that can be used to monitor the progress of the display.
*/
//===================================================================
public Handle setHtml(Link link)
//===================================================================
{
	return setHtml(link,null,null);
}
/**
 * Set the Html text directly.
 * @param htmlText An object representing htmlText. This can be:
	<ul>
	<li>A URL holding the URL you want the browser to fetch.
	<li>A Link holding the URL or file that you want the browser to fetch.
	<li>A String holding the HTML text.
	<li>An eve.util.ByteArray holding the text encoded HTML text.
	<li>A byte array (byte[]) holding the text encoded HTML.
	<li>An InputStream object from which the text can be read.
	<li>An eve.io.File object from which the text can be read.
	</ul>
 * @param urlToDisplay The URL to display for the data.
 * @param bookmark An optional bookmark in the file to go to.
 * @return A Handle that can be used to monitor the progress of the display.
 */
//===================================================================
public Handle setHtml(Object htmlText,String urlToDisplay,String bookmark)
//===================================================================
{
	String uu = urlToDisplay;
	String aa = bookmark;
	if (htmlText instanceof URL){
		htmlText = new Link(htmlText.toString());
	}
	if (htmlText instanceof Link){
		String href = htmlText.toString();
		String file = mString.leftOf(href,'#');
		aa = mString.rightOf(href,'#');
		if (file.length() == 0) return new Handle(Handle.Failed,null);
		uu = toFullURL(htmlProperties.getString("documentRoot",""),file);
		if (uu == null) return new Handle(Handle.Failed,null);
	}
	final String url = uu;
	final String anchor = aa;
	//
	FormattedTextMaker fm = findCached(url);
	//
	cancelLoading(true);
	if (fm != null){
		setHtml(fm);
		htmlProperties.set("documentRoot",display.getDecoderProperties().getString("documentRoot","/"));
		htmlProperties.set("document",url);
		if (anchor != null && anchor.length() > 0) display.goToAnchor(anchor);
		newDocumentLoaded(url);
		return new Handle(Handle.Succeeded,fm);
	}
	final PropertyList pl = new PropertyList();
	pl.set("document",url);

	final Handle h = htmlText instanceof Link ? openDocument(url,pl) : new Handle(Handle.Succeeded,htmlText);
	return new Task(){
		protected void doRun(){
			Handle handle = this;
			try{
				handle.doing = "Loading...";
				startingLoad(loading = handle);
				handle.setProgress(0);
				if (!waitOnSuccess(h,TimeOut.Forever,true)){
					if ((handle.check() & handle.Aborted) == 0){
						new MessageBox("Load Error","Could not load document.\nError: "+h.getErrorText("Unknown"),MBOK).execute();
						//if (handle.error != null) new ReportException(handle.error).execute();
					}
					return;
				}
				Object toDecode = h.returnValue;
				handle.setProgress(0);
				decodeHtml(toDecode,url,anchor,this,pl);
			}finally{
				endedLoad(handle);
				loading = null;
			}
		}
	}.start();
}

/**
 * Setup the htmlProperties based on selected options.
 */
//===================================================================
public void setupProperties()
//===================================================================
{
	htmlProperties.setBoolean("allowAnimatedImages",animatedImages);
	htmlProperties.setBoolean("allowImages",showImages);
}
/**
* Use this to cancel a load/decode operation. If you intend to immediately start another
* load using doOpen() then set startingNewLoad to true.
**/
//===================================================================
public void cancelLoading(boolean startingNewLoad)
//===================================================================
{
	if (loading == null) return;
	loading.stop(0);
	loading = null;
	if (progress != null) progress.clearTask();
	if (!startingNewLoad) noLoading();
}

//-------------------------------------------------------------------
void decodeHtml(Object what,String url,String anchor,Task t,PropertyList pl)
//-------------------------------------------------------------------
{
	Handle handle = t;
	if (anchor == null) anchor = "";
	try{
		String html = null;
		boolean closeStream = false;
		while(true){
			if (what instanceof String) html = (String)what;
			else if (what instanceof ByteArray){
				ByteArray ba = (ByteArray)what;
				CharArray ca = getCodec(pl).decodeText(ba.data,0,ba.length,true,null);
				html = new String(ca.data,0,ca.length);
			}else if (what instanceof byte []){
				byte [] d = (byte [])what;
				CharArray ca = getCodec(pl).decodeText(d,0,d.length,true,null);
				html = new String(ca.data,0,ca.length);
			}else if (what instanceof InputStream){
				ByteArray got = StreamUtils.readAllBytes(null,(InputStream)what,null);
				if (closeStream) ((InputStream)what).close();
				what = got;
				continue;
			}else if (what instanceof File){
				what = ((File)what).toReadableStream();
				closeStream = true;
				continue;
			}
			if (html == null) throw new IOException("Cannot decode HTML");
			else break;
		}
		handle.doing = "Decoding...";
		handle.setProgress(0);
		setupProperties();
		htmlProperties.set(pl);
		addPropertiesTo(htmlProperties);
		String loadedDocument = htmlProperties.getString("document",url);
		FormattedTextMaker fm = setHtml(html,htmlProperties,handle);
		if (handle.shouldStop) throw new IOException();
		handle.returnValue = fm;
		cacheOpened(loadedDocument,fm);
		if (anchor.length() > 0) 
			display.goToAnchor(anchor);
		
		newDocumentLoaded(loadedDocument);
		handle.progress = 0;
		handle.doing = "";
		handle.set(Handle.Succeeded);
	}catch(Exception e){
		handle.set(Handle.Failed);
	}
}
/**
* Open the file relative to the current document root, or as a new document/file.
* @param fileOrUrl.
* @return A Handle with which the progress of the load can be monitored.
*/
//===================================================================
public Handle doOpen(String fileOrUrl)
//===================================================================
{
		return setHtml(new Link(fileOrUrl),null,null);
	/*
	//
	String href = fileOrUrl;
	final String file = ewe.util.mString.leftOf(href,'#');
	final String anchor = ewe.util.mString.rightOf(href,'#');
	if (file.length() == 0) return new Handle(Handle.Failed,null);
	//
	final String url = toFullURL(htmlProperties.getString("documentRoot",""),file);
	if (url == null) return  new Handle(Handle.Failed,null);
	//
	FormattedTextMaker fm = findCached(url);
	//
	cancelLoading(true);
	if (fm != null){
		setHtml(fm);
		htmlProperties.set("documentRoot",display.getDecoderProperties().getString("documentRoot","/"));
		htmlProperties.set("document",url);
		if (anchor.length() > 0) display.goToAnchor(anchor);
		newDocumentLoaded(url);
		return new Handle(Handle.Succeeded,fm);
	}
	final PropertyList pl = new PropertyList();
	pl.set("document",url);
	final Handle h = openDocument(url,pl);
	return new TaskObject(){
		protected void doRun(){
			handle.doing = "Loading...";
			startingLoad(loading = handle);
			try{
				if (!waitOnSuccess(h,true)) {
					if ((handle.check() & handle.Aborted) == 0)
						new MessageBox("Load Error","Could not load document.\nError: "+h.getErrorText("Unknown"),MBOK).execute();
					return;
				}
				try{
					handle.progress = 0;
					handle.doing = "Decoding...";
					String html = null;
					if (h.returnValue instanceof String) html = (String)h.returnValue;
					if (h.returnValue instanceof ByteArray){
						ByteArray ba = (ByteArray)h.returnValue;
						CharArray ca = getCodec().decodeText(ba.data,0,ba.length,true,null);
						html = new String(ca.data,0,ca.length);
					}
					setupProperties();
					htmlProperties.set(pl);
					addPropertiesTo(htmlProperties);
					String loadedDocument = htmlProperties.getString("document",url);
					FormattedTextMaker fm = setHtml(html,htmlProperties,handle);
					if (handle.shouldStop) throw new IOException();
					handle.returnValue = fm;
					cacheOpened(loadedDocument,fm);
					if (anchor.length() > 0) 
						display.goToAnchor(anchor);
					newDocumentLoaded(loadedDocument);
					handle.progress = 0;
					handle.doing = "";
					handle.set(Handle.Succeeded);
				}catch(IOException e){
					handle.set(Handle.Failed);
				}
			}finally{
				endedLoad(handle);
				loading = null;
			}
		}		
	}.startTask();
	//return h;
	*/
}
/**
 * Create the viewer with full tools.
 * @param displayOptions This can be any of the : DISPLAY_XXX options ORed together.
 */
//===================================================================
public HtmlViewer()
//===================================================================
{
	this(null,0);
}
/**
 * Create the viewer the specified display options.
 * @param displayOptions This can be any of the : DISPLAY_XXX options ORed together.
 */
//===================================================================
public HtmlViewer(int displayOptions)
//===================================================================
{
	this(null,displayOptions);
}

//-------------------------------------------------------------------
protected HtmlDisplay createDisplay()
//-------------------------------------------------------------------
{
	return new HtmlViewerDisplay();
}
/**
 * Create the viewer with a particular HtmlDisplay and using the specified display options.
 * @param display An HtmlDisplay to use (can be null).
 * @param displayOptions This can be any of: DISPLAY_NO_STATUS_BAR, DISPLAY_NO_TOOL_BUTTONS, DISPLAY_NO_TABS OR'ed together.
 */
//===================================================================
public HtmlViewer(HtmlDisplay display,int displayOptions)
//===================================================================
{
	super((displayOptions & DISPLAY_NO_TABS) == 0,(displayOptions & DISPLAY_NO_TABS) == 0);
	//
	//String plat = Vm.getPlatform().toUpperCase();
	//if (plat.equals("UNIX") || plat.equals("LINUX")) animatedImages = false;
	Dimension sz = Gui.getScreenSize();
	if (!Gui.screenIs(Gui.WIDE_SCREEN)) htmlProperties.set("maxImageSize",new Dimension(sz.width/2,sz.width/2));
	int opts = Vm.getParameter(Vm.VM_FLAGS);
	if ((opts & Vm.VM_FLAG_SLOW_MACHINE) != 0)
		htmlProperties.setBoolean("allowAnimatedImages",animatedImages = false);
	if (display == null) display = createDisplay();
	acceptsDroppedFiles = true;
	this.display = display;

	title = "HTML Viewer";
	ScrollBarPanel sbp = new ScrollBarPanel(display);
	//
	sbp.setOptions(sbp.Permanent);
	data.addLast(sbp);
	Vector t = new Vector();
	
	//pl.setBoolean("allowAnimatedImages",false);
	//pl.set("maxImageSize",new ewe.fx.Dimension(50,50));
	
	if (tabs != null){
		fontChooser = new FontChooser(false);
		CellPanel tl = new CellPanel();
		fontChooser.fromFont(display.getFont());
		tl.addLast(addField(fontChooser.getEditor(0),"fontChooser")).setCell(HSHRINK);
		tabs.addCard(tl,"Font",null).iconize("eve/fontsmall.png");
		
		InputStack is = new InputStack(); is.columns = 2;
		addField(is.addCheckBox("Images"),"showImages");
		addField(is.addCheckBox("Animated Images"),"animatedImages");
		tabs.addCard(is,"Options",null).iconize("eve/optionssmall.png");
	}
	
	if ((displayOptions & DISPLAY_NO_TOOL_BUTTONS) == 0){
		if ((displayOptions & DISPLAY_NO_OPEN) == 0)
			setupStandardFileCommands(this,SHOW_OPEN_BUTTON/*|SHOW_EXIT_BUTTON*/,tools,"HTML File");
		addToolButton("back",loadImage("eve/leftarrowsmall.png"),"Go Back",true);
		addToolButton("reload",loadImage("eve/reloadsmall.png"),"Reload",true);
		stopButton = addToolButton("stop",stop,"Stop",true);
		stopButton.modify(Disabled,0);
		tools.modifyAll(NoFocus|MouseSensitive,TakesKeyFocus,true);
	}
	
	if ((displayOptions & DISPLAY_NO_STATUS_BAR) == 0){
		statusCard = new CardPanel();

		statusCard.borderWidth = 3;
		statusCard.borderStyle = EDGE_SUNKEN;
		data.addLast(statusCard).setCell(HSTRETCH);
		statusCard.addItem(addField(new Label(" "),"status"),"status",null);
	//
		ProgressBarForm pbf = new ProgressBarForm();
		pbf.showMainTask = false;
		pbf.showSubTask = true;
		pbf.showTaskInBar = true;
		pbf.exitOnCompletion = false;
		pbf.bar.showPercent = false;
		pbf.bar.incompleteColor = getBackground();
		progress = pbf;
		CellPanel cp2 = new CellPanel();
		cp2.addNext(pbf).setControl(DONTFILL|WEST);
		statusCard.addItem(cp2,"progress",null);
	}
}

//===================================================================
File getNewFile(String path)
//===================================================================
{
	return File.getNewFile(path);
}

//===================================================================
void htmlSet()
//===================================================================
{
	title = display.headerData.getString("title","untitled");
	Window w = getWindow();
	if (w != null) w.setTitle(title);
	htmlProperties.set(display.getDecoderProperties());
}
//===================================================================
public void setHtml(FormattedTextMaker html)
//===================================================================
{
	display.setHtml(html,null);
	htmlSet();
}
//===================================================================
public FormattedTextMaker setHtml(String html,PropertyList props,Handle h)
//===================================================================
{
	if (html == null) return null;
	addPropertiesTo(props);
	FormattedTextMaker ftm = display.setHtml(html,props,h);
	htmlSet();
	return ftm;
}

static Hashtable opened = new Hashtable();

//-------------------------------------------------------------------
protected void addPropertiesTo(PropertyList pl)
//-------------------------------------------------------------------
{
	
}
//===================================================================
public FormattedTextMaker open(String data,String fileName)
//===================================================================
{
	Gui.showWait(this,true);
	try{
		String root = getNewFile(fileName).getParent();
		if (root == null) root = "";
		root = File.removeTrailingSlash(root)+"/";
		htmlProperties.set("documentRoot",root);
		//htmlProperties.set("maxImageSize",new ewe.fx.Dimension(150,150));
		return setHtml(data,htmlProperties,new Handle());
	}finally{
		Gui.showWait(this,false);
	}
}
public static String [] images = {".jpeg",".jpg",".png",".bmp",".gif"};

//===================================================================
public void open(FormattedTextMaker maker)
//===================================================================
{
	setHtml(maker);
}
//===================================================================
public FormattedTextMaker findCached(String name)
//===================================================================
{
	return (FormattedTextMaker)opened.get(name);
}
//===================================================================
public FormattedTextMaker cacheOpened(String name,FormattedTextMaker maker)
//===================================================================
{
	opened.put(name,maker);
	return maker;
}
//===================================================================
public void removeCached(String name)
//===================================================================
{
	opened.remove(name);
}
//===================================================================
public FormattedTextMaker open(String name)
//===================================================================
{
	String fe = File.getFileExt(name);
	try{
		String nu = name.toLowerCase();
		for (int i = 0; i<images.length; i++)
			if (nu.endsWith(images[i])){
				String toOpen = "<head><title>name</title></head><body><img src=\""+fe+"\"><br></body>";
				return open(toOpen,name);
			}
		FormattedTextMaker ftm = findCached(name);
		if (ftm != null){
			open(ftm);
			return ftm;
		}
		/*
		String got = saver.openText(name,getFrame());
		if (nu.endsWith(".txt") && got != null){
			display.setPlainText(got);
			return null;
		}
		if (got != null) 
			return cacheOpened(name,open(got,name));
		*/
	}catch(Exception e){
		
	}
		return null;
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("Exit")){
		exit(0);
	}else if (fieldName.equals("Open")){
		open();
	}else if (fieldName.equals("back")){
		goBack();
	}else if (fieldName.equals("stop")){
		cancelLoading(false);
	}else if (fieldName.equals("reload")){
		reload();
	}else
		super.action(fieldName,ed);
}


/**
 * Go to previous document/location. Acts as if the go back button had been pressed.
 */
//===================================================================
public void goBack()
//===================================================================
{
	display.goBack();
}
/**
 * Open a new file. Acts as if the open button had been pressed.
 */
//===================================================================
public void open()
//===================================================================
{
	FileChooser fc = new FileChooser();
	fc.title = "Open Web Document";
	fc.addMask("*.html;*.htm - HTML Documents");
	fc.addMask(fc.allFilesMask);
	fc.persistentHistoryKey = "Ewesoft-HtmlViewer";
	if (fc.execute() == IDCANCEL) return;
	doOpen(fc.getChosenFile().getFullPath());
	//String data = saver.open(null,getFrame());
	//if (data != null) open(data,saver.lastSaved);
}
/**
* Reload the current document. Acts as if the reload button had been pressed.
**/
//===================================================================
public void reload()
//===================================================================
{
	cancelLoading(false);
	if (currentURL != null){
		removeCached(currentURL);
		doOpen(currentURL);
	}
}
//===================================================================
public void fieldChanged(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("fontChooser")){
		display.font = fontChooser.toFont();
		update();
	}if (fieldName.equals("animatedImages") || fieldName.equals("showImages")){
		setupProperties();
	}
}
//===================================================================
public void update()
//===================================================================
{
		display.update();
		display.repaintNow();
}

//===================================================================
public void filesDropped(String[] fileName)
//===================================================================
{
	doOpen(fileName[0]);
}
/**
 * Set the display font.
 */
//===================================================================
public void setDisplayFont(Font f)
//===================================================================
{
	display.font = f;
	fontChooser.fromFont(display.font);
}

//===================================================================
public static HtmlViewer openAndDisplay(String url,int displayOptions,boolean execute)
//===================================================================
{
	HtmlViewer np = new HtmlViewer(null,displayOptions);
	if (url != null) {
		np.doOpen(url);
	}
	if (execute) np.exec();
	else np.show();
	return np;
}
//===================================================================
public static void main(String args[])
//===================================================================
{
	Application.startApplication(args);
	String toOpen = null;
	if (args.length != 0) toOpen = args[0];
	HtmlViewer np = openAndDisplay(toOpen,0,false);
	np.exitSystemOnClose = true;
}

//##################################################################
}
//##################################################################

