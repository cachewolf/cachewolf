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
import CacheWolf.navi.Metrics;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Insets;
import ewe.fx.Pen;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.io.File;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.ButtonObject;
import ewe.ui.CardPanel;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.DataChangeEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Frame;
import ewe.ui.InputBox;
import ewe.ui.MessageBox;
import ewe.ui.Panel;
import ewe.ui.UIConstants;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mComboBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;

/**
*	This class displays a form that the user uses to set the filter criteria.
*	Class ID=700
*/
public class FilterScreen extends Form{
	private static final Color COLOR_FILTERINACTIVE=new Color(190,190,190);
	private static final Color COLOR_FILTERACTIVE=new Color(0,255,0);
	private static final Color COLOR_FILTERALL=new Color(255,0,0); // Red
    	
	private mButton btnCancel, btnApply, btnRoute, btnSaveFlt, btnDelFlt,
					btnBearing, btnTypes, btnAttributes, btnRatings, btnContainer, btnSearch,
					btnAddi, btnSelect, btnDeselect, btnCacheAttributes;
	
	private mChoice chcDist, chcDiff, chcTerr, chcAttrib;
	private mCheckBox chkFound, chkNotFound, chkTrad, chkVirtual, chkEvent, chkEarth, chkMega,
					  chkOwned, chkNotOwned, chkMulti, chkLetter, chkWebcam, chkMystery, chkLocless,
	                  chkCustom, chkParking, chkStage, chkQuestion, chkFinal, chkTrailhead, chkReference,
					  chkMicro,chkSmall,chkRegular,chkLarge,chkVeryLarge,chkOther,chkCito,
	                  chkArchived, chkNotArchived, chkAvailable, chkNotAvailable,
					  chkNW, chkNNW , chkN , chkNNE, chkNE, chkENE, chkE, chkESE, chkSE, chkSSE, chkS,
					  chkSSW, chkSW, chkWSW, chkW, chkWNW, chkWherigo, chkNoCoord;
	private mComboBox chcStatus;
	private mChoice   fltList;
	private mCheckBox chkUseRegexp;
	
	private mInput inpDist, inpTerr, inpDiff;

	AttributesSelector attV;
	
	private CellPanel pnlBearDist=new CellPanel();
	private CellPanel pnlAttributes=new CellPanel();
	private CellPanel pnlRatings=new CellPanel();
	private CellPanel pnlCacheTypes=new CellPanel();
	private CellPanel pnlContainer=new CellPanel();
	private CellPanel pnlSearch=new CellPanel();
	private CellPanel pnlRose = new CellPanel();
	private CellPanel pnlAddi=new CellPanel();
	private CellPanel pnlCacheAttributes=new CellPanel();
	private CardPanel cp=new CardPanel();
	
	// ID of last filter selected from the filter list
	private String currentFilterID = ""; 
	// Flag, true if filters have been changed, added or deleted. Leads to saving of pref.xml
	private boolean savedFiltersChanged= false;

	// A subclassed checkbox with a "third" state (=grey background).
	// If all addi wpts are false or all addi wpts are true, the background is white
	// If the addi wpt filter is a mixture of true/false, the bg is grey
	// Thus the addi filter can be set in one of two ways: Using the single checkbox with all the other
	// attributes, or using the multiple checkboxes for each addi waypoint type
	private class myChkBox extends mCheckBox {
		Color bgColor=Color.White;
		myChkBox(String s) {super(s); }
		public void doPaintSquare(Graphics g) {
			int h = height;
			g.setColor(bgColor);
			int bx = text.length() == 0 ? 0 : 2;
			int by = text.length() == 0 ? 0 : (h-boxWidth)/2+1;
			g.fillRect(bx+2,by+2,boxWidth-4,boxWidth-4);
			if (state || pressState){
				Color c = Color.LightGray;
				if (!pressState){
					if (!state) 
						c=bgColor;
					else 
						c=Color.Black;
				}
				Pen oldpen = g.setPen(new Pen(c,Pen.SOLID,2));
				g.drawLine(bx+4,by+boxWidth-5,bx+boxWidth-5,by+4);
				g.drawLine(bx+4,by+boxWidth-5,bx+4,by+boxWidth-10);
//				g.drawLine(bx+3,by+3,bx+boxWidth-5,by+boxWidth-5);
//				g.drawLine(bx+3,by+boxWidth-5,bx+boxWidth-5,by+3);
				g.setPen(oldpen);
			}
			g.draw3DRect(new Rect(bx,by,boxWidth,boxWidth),	ButtonObject.checkboxEdge,true,null,Color.DarkGray);
		}		
	}
	private myChkBox addiWptChk;
	
	private mButton addImg(Image pImage) {
		mButton mb = new mButton(pImage);
		mb.borderWidth=0; 
		mb.modify(NotEditable|PreferredSizeOnly,0);
		return mb;
	}
	private void addTitle(CellPanel c, String pTitle) {
		mLabel lblTitle;
		c.addLast(lblTitle=new mLabel(pTitle),HSTRETCH,FILL|CENTER);
		lblTitle.backGround=new Color(127,127,127); 
		lblTitle.foreGround=Color.White; 
		lblTitle.setTag(INSETS,new Insets(2,0,4,0));
	}
	
	public FilterScreen() {
		this.title = MyLocale.getMsg(700,"Set Filter");

		//////////////////////////
		// Panel 1 - Bearing & Distance
		//////////////////////////
		addTitle(pnlBearDist,MyLocale.getMsg(714,"Bearings & Distance"));
		pnlBearDist.addNext(new mLabel(MyLocale.getMsg(701,"Distance: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlBearDist.addNext(chcDist = new mChoice(new String[]{"<=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlBearDist.addLast(inpDist = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlBearDist.addLast(new mLabel(""));
		pnlBearDist.addLast(chkNoCoord = new mCheckBox(MyLocale.getMsg(743,"No coordinates")),CellConstants.HSTRETCH, CellConstants.FILL);
		
		pnlRose.addNext(chkNW = new mCheckBox("NW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkNNW = new mCheckBox("NNW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkN = new mCheckBox("N"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addLast(chkNNE = new mCheckBox("NNE"),CellConstants.HSTRETCH, CellConstants.FILL);
		
		pnlRose.addNext(chkNE = new mCheckBox("NE"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkENE = new mCheckBox("ENE"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkE = new mCheckBox("E "),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addLast(chkESE = new mCheckBox("ESE"),CellConstants.HSTRETCH, CellConstants.FILL);

		pnlRose.addNext(chkSE = new mCheckBox("SE"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkSSE = new mCheckBox("SSE"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkS = new mCheckBox("S"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addLast(chkSSW = new mCheckBox("SSW"),CellConstants.HSTRETCH, CellConstants.FILL);

		pnlRose.addNext(chkSW = new mCheckBox("SW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkWSW = new mCheckBox("WSW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkW = new mCheckBox("W "),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addLast(chkWNW = new mCheckBox("WNW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(btnDeselect=new mButton(MyLocale.getMsg(716,"Deselect all")),CellConstants.HSTRETCH, CellConstants.FILL);
		btnDeselect.setTag(SPAN,new Dimension(2,1));
		pnlRose.addLast(btnSelect=new mButton(MyLocale.getMsg(717,"Select all")),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlBearDist.addLast(pnlRose, CellConstants.STRETCH,CellConstants.FILL);
		
		//////////////////////////
		// Panel 2 - Cache attributes
		//////////////////////////
		addTitle(pnlAttributes,MyLocale.getMsg(720,"Status"));
		mLabel lblTitleAtt; 
		pnlAttributes.addLast(lblTitleAtt=new mLabel(MyLocale.getMsg(715,"Show all caches with status:")),HSTRETCH,FILL);
		lblTitleAtt.setTag(SPAN,new Dimension(2,1));
		pnlAttributes.addNext(chkArchived = new mCheckBox(MyLocale.getMsg(710,"Archived")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotArchived = new mCheckBox(MyLocale.getMsg(729,"Nicht archiviert")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(chkAvailable = new mCheckBox(MyLocale.getMsg(730,"Suchbar")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotAvailable = new mCheckBox(MyLocale.getMsg(711,"Not available")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(chkFound = new mCheckBox(MyLocale.getMsg(703,"Found")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotFound = new mCheckBox(MyLocale.getMsg(731,"Noch nicht gefunden")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(chkOwned = new mCheckBox(MyLocale.getMsg(707,"Owned")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotOwned = new mCheckBox(MyLocale.getMsg(732,"Anderer Besitzer")), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlAttributes.addNext(new mLabel(MyLocale.getMsg(307,"Status:")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlAttributes.addLast(chcStatus = new mComboBox(CacheHolder.GetGuiLogTypes(),0),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		pnlAttributes.addLast(chkUseRegexp = new mCheckBox(MyLocale.getMsg(299,"Regular expression")));
		
		//////////////////////////
		// Panel 3 - Cache ratings
		//////////////////////////
		addTitle(pnlRatings,MyLocale.getMsg(718,"Cache ratings"));
		pnlRatings.addNext(new mLabel(MyLocale.getMsg(702,"Difficulty: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlRatings.addNext(chcDiff = new mChoice(new String[]{"<=","=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//pnlRatings.addLast(difIn = new mChoice(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlRatings.addLast(inpDiff = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlRatings.addNext(new mLabel("Terrain: "),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlRatings.addNext(chcTerr = new mChoice(new String[]{"<=", "=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//pnlRatings.addLast(terrIn = new mChoice(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlRatings.addLast(inpTerr = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);

		//////////////////////////
		// Panel 4 - Cache types
		//////////////////////////
		
		addTitle(pnlCacheTypes,MyLocale.getMsg(719,"Cache types"));

		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_TRADITIONAL)));
		pnlCacheTypes.addNext(chkTrad = new mCheckBox("Traditonal"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_MULTI)));
		pnlCacheTypes.addLast(chkMulti = new mCheckBox("Multi"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_VIRTUAL)));
		pnlCacheTypes.addNext(chkVirtual = new mCheckBox("Virtual"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_LETTERBOX)));
		pnlCacheTypes.addLast(chkLetter = new mCheckBox("Letterbox"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_EVENT)));
		pnlCacheTypes.addNext(chkEvent = new mCheckBox("Event"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_WEBCAM)));
		pnlCacheTypes.addLast(chkWebcam = new mCheckBox("Webcam"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_UNKNOWN)));
		pnlCacheTypes.addNext(chkMystery = new mCheckBox("Mystery"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_EARTH)));
		pnlCacheTypes.addLast(chkEarth = new mCheckBox("Earth"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_LOCATIONLESS)));
		pnlCacheTypes.addNext(chkLocless = new mCheckBox("Locationless"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_MEGA_EVENT)));
		pnlCacheTypes.addLast(chkMega = new mCheckBox("Mega-Ev."), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_CITO)));
		pnlCacheTypes.addNext(chkCito = new mCheckBox("Cito-Ev."), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		//pnlCacheTypes.addLast(addiWptChk = new mCheckBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		//pnlCacheTypes.addLast(new mLabel(""));
		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_CUSTOM)));
		pnlCacheTypes.addLast(chkCustom = new mCheckBox("Custom"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_WHEREIGO)));
		pnlCacheTypes.addNext(chkWherigo = new myChkBox("WherIGo"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_DRIVE_IN)));
		pnlCacheTypes.addNext(addiWptChk = new myChkBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		//addiWptChk.modify(0,NotAnEditor);
		//////////////////////////
		// Panel 5 - Addi waypoints
		//////////////////////////
		addTitle(pnlAddi,MyLocale.getMsg(726,"Additional waypoints"));
		pnlAddi.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_PARKING)));
		pnlAddi.addNext(chkParking = new mCheckBox("Parking"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_STAGE)));
		pnlAddi.addLast(chkStage = new mCheckBox("Stage"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_QUESTION)));
		pnlAddi.addNext(chkQuestion = new mCheckBox("Question"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_FINAL)));
		pnlAddi.addLast(chkFinal = new mCheckBox("Final"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_TRAILHEAD)));
		pnlAddi.addNext(chkTrailhead = new mCheckBox("Trailhead"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg(CacheType.getTypeImage(CacheType.CW_TYPE_REFERENCE)));
		pnlAddi.addLast(chkReference = new mCheckBox("Reference"), CellConstants.DONTSTRETCH, CellConstants.FILL);
        pnlAddi.addLast(new mLabel(""),VSTRETCH,FILL);		

		//////////////////////////
		// Panel 6 - Cache container
		//////////////////////////
		addTitle(pnlContainer,MyLocale.getMsg(727,"Cache container"));
		pnlContainer.addLast(chkMicro=new mCheckBox("Micro"));
		pnlContainer.addLast(chkSmall=new mCheckBox("Small"));
		pnlContainer.addLast(chkRegular=new mCheckBox("Regular"));
		pnlContainer.addLast(chkLarge=new mCheckBox("Large"));
		pnlContainer.addLast(chkVeryLarge=new mCheckBox("Very Large"));
		pnlContainer.addLast(chkOther=new mCheckBox("Other"));
		
		//////////////////////////
		// Panel 7 - Search
		//////////////////////////
		addTitle(pnlSearch,"Search");
		pnlSearch.addLast(new mLabel("To be implemented"));


		//////////////////////////
		// Panel 8 - Cache attributes
		//////////////////////////
		
		if (MyLocale.getScreenHeight()>240) addTitle(pnlCacheAttributes,MyLocale.getMsg(737,"Attributes"));
		pnlCacheAttributes.addNext(new mLabel(MyLocale.getMsg(739,"Filter on")+":"), CellConstants.DONTSTRETCH, CellConstants.LEFT);
		pnlCacheAttributes.addLast(chcAttrib = new mChoice(new String[]{MyLocale.getMsg(740,"all"), MyLocale.getMsg(741,"one"), MyLocale.getMsg(742,"none")},0),CellConstants.DONTSTRETCH, CellConstants.LEFT);
		pnlCacheAttributes.addLast(attV=new AttributesSelector(), DONTSTRETCH, CENTER|TOP);
		long[] ini = {0l,0l,0l,0l};
		attV.setSelectionMasks(ini);
		
		Frame frmScreen=new Frame();
		mLabel lblInfo; 
		frmScreen.addLast(lblInfo=new mLabel(MyLocale.getMsg(725,"Note: Filters are additive, active filter=green"))).setTag(SPAN,new Dimension(2,1));
		lblInfo.setTag(INSETS,new Insets(0,0,2,0));
		frmScreen.borderStyle=UIConstants.BDR_RAISEDOUTER|UIConstants.BDR_SUNKENINNER|UIConstants.BF_BOTTOM;
		this.addLast(frmScreen,HSTRETCH,HFILL);
		

		CellPanel pnlButtons=new CellPanel();
		pnlButtons.addLast(new mLabel("Filter"));
		pnlButtons.addLast(btnBearing=new mButton(MyLocale.getMsg(721,"Bearing")));
		pnlButtons.addLast(btnAttributes=new mButton(MyLocale.getMsg(720,"Attributes")));
		pnlButtons.addLast(btnRatings=new mButton(MyLocale.getMsg(722,"Ratings")));
		pnlButtons.addLast(btnTypes=new mButton(MyLocale.getMsg(723,"Types"))); 
		pnlButtons.addLast(btnAddi=new mButton(MyLocale.getMsg(733,"Add. Wpt"))); 
		pnlButtons.addLast(btnContainer=new mButton(MyLocale.getMsg(724,"Container")));
		pnlButtons.addLast(btnCacheAttributes=new mButton(MyLocale.getMsg(738,"Attributes")));
		// Search ist für 0.9n noch deaktiviert
		//pnlButtons.addLast(btnSearch=new mButton("Search")); btnSearch.modify(Disabled,0);
		addNext(pnlButtons,HSTRETCH,FILL);

		cp.addItem(pnlBearDist,"Bear",null);
		cp.addItem(pnlAttributes,"Att",null);
		cp.addItem(pnlRatings,"DT",null); 
		cp.addItem(pnlCacheTypes,"Type",null);
		cp.addItem(pnlAddi,"Addi",null);
		cp.addItem(pnlContainer,"Size",null);
		cp.addItem(pnlSearch,"Search",null);
		cp.addItem(pnlCacheAttributes,"Attr",null);
		addLast(cp,VSTRETCH,FILL);

		CellPanel savDelPanel = new CellPanel(); // Panel for "save" and "delete" button
		savDelPanel.equalWidths = true;
		mImage savImg=new mImage("clsave.png"); savImg.transparentColor=new Color(255,0,0);
		savDelPanel.addNext(btnSaveFlt = new mButton(savImg),STRETCH,FILL);
		savDelPanel.addLast(btnDelFlt = new mButton(new mImage("trash.png")),STRETCH,FILL);
		Panel fltListPanel = new Panel();
		fltListPanel.addLast(fltList = new mChoice());
		fltListPanel.addLast(savDelPanel);
		CellPanel btPanel = new CellPanel();
		btPanel.equalWidths = true;
		btPanel.addNext(fltListPanel,CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addNext(btnCancel = new mButton(MyLocale.getMsg(708,"Cancel")),CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addLast(btnApply = new mButton(MyLocale.getMsg(709,"Apply")),CellConstants.STRETCH, CellConstants.FILL);
//		btPanel.addLast(btnRoute = new mButton("Route"),CellConstants.STRETCH, CellConstants.FILL);
		addLast(btPanel.setTag(CellConstants.SPAN, new Dimension(3,1)), CellConstants.STRETCH, CellConstants.FILL);
		
		int sw = MyLocale.getScreenWidth(); int sh = MyLocale.getScreenHeight(); 
		Preferences pref = Global.getPref();int fs = pref.fontSize;
		int psx; int psy;
		if((sw>300) && (sh>300)){
			// larger screens: size according to fontsize
			psx=240;psy=260;
			if(fs > 12){psx=300;psy=330;}
			if(fs > 17){psx=400;psy=340;}
			if(fs > 23){psx=500;psy=350;}
			setPreferredSize(psx,psy);
		}
		else{
			// small screens: fixed size
			if (sh>240)
				setPreferredSize(240,260);
			else
				setPreferredSize(240,240);
		}
		cp.select(3);
		
		// Populating the comboBox of saved filters
		buildFilterList();
	}

    public void resizeTo(int width, int height) {
		attV.changeIapSize(width, height);
		this.relayout(true);
		super.resizeTo(width,height);
	}
	
	public void setData(FilterData data){

		//////////////////////////
		// Panel 1 - Bearing & Distance
		//////////////////////////
		if (data.getFilterDist().length()>1) {
			if (data.getFilterDist().charAt(0)=='L')
				chcDist.select(0);
			else
				chcDist.select(1);
			String dist = data.getFilterDist().substring(1);
			if (Global.getPref().metricSystem == Metrics.IMPERIAL) {
				double distValue = java.lang.Double.valueOf(dist).doubleValue();
				double newDistValue = Metrics.convertUnit(distValue, Metrics.KILOMETER, Metrics.MILES);
				dist = String.valueOf(newDistValue);
			}
			inpDist.setText(dist);
		} else {
			chcDist.select(0);
			inpDist.setText("");
		}
		chkNoCoord.state = data.getFilterNoCoord();
		String fltRose=data.getFilterRose();
		chkNW.state   = fltRose.charAt(0) == '1';
		chkNNW.state  = fltRose.charAt(1) == '1';
		chkN.state    = fltRose.charAt(2) == '1';
		chkNNE.state  = fltRose.charAt(3) == '1';
		
		chkNE.state   = fltRose.charAt(4) == '1';
		chkENE.state  = fltRose.charAt(5) == '1';
		chkE.state    = fltRose.charAt(6) == '1';
		chkESE.state  = fltRose.charAt(7) == '1';

		chkSE.state   = fltRose.charAt(8) == '1';
		chkSSE.state  = fltRose.charAt(9) == '1';
		chkS.state    = fltRose.charAt(10) == '1';
		chkSSW.state  = fltRose.charAt(11) == '1';

		chkSW.state   = fltRose.charAt(12) == '1';
		chkWSW.state  = fltRose.charAt(13) == '1';
		chkW.state    = fltRose.charAt(14) == '1';
		chkWNW.state  = fltRose.charAt(15) == '1';
		
		//////////////////////////
		// Panel 2 - Cache attributes
		//////////////////////////
		String fltVar=data.getFilterVar();
		chkArchived.state      = fltVar.charAt(0) == '1';
		chkAvailable.state     = fltVar.charAt(1) == '1';
		chkFound.state         = fltVar.charAt(2) == '1';
		chkOwned.state         = fltVar.charAt(3) == '1';
		chkNotArchived.state   = fltVar.charAt(4) == '1';
		chkNotAvailable.state  = fltVar.charAt(5) == '1';
		chkNotFound.state      = fltVar.charAt(6) == '1';
		chkNotOwned.state      = fltVar.charAt(7) == '1';
		chcStatus.setText(data.getFilterStatus());
		chkUseRegexp.setState(data.useRegexp());

		//////////////////////////
		// Panel 3 - Cache ratings
		//////////////////////////
		if (data.getFilterDiff().length()>1) {
			if (data.getFilterDiff().charAt(0)=='L')
				chcDiff.select(0);
			else if (data.getFilterDiff().charAt(0)=='=')
				chcDiff.select(1);
			else
				chcDiff.select(2);
			inpDiff.setText(data.getFilterDiff().substring(1));
		} else {
			chcDiff.select(0);
			inpDiff.setText("");
		}

		if (data.getFilterTerr().length()>1) {
			if (data.getFilterTerr().charAt(0)=='L')
				chcTerr.select(0);
			else if (data.getFilterTerr().charAt(0)=='=')
				chcTerr.select(1);
			else
				chcTerr.select(2);
			inpTerr.setText(data.getFilterTerr().substring(1));
		} else {
			chcTerr.select(0);
			inpTerr.setText("");
		}


		//////////////////////////
		// Panel 4 - Cache types
		//////////////////////////
		
		String fltType=data.getFilterType();
		chkTrad.state       = fltType.charAt(0) == '1';
		chkMulti.state      = fltType.charAt(1) == '1';
		chkVirtual.state    = fltType.charAt(2) == '1';
		chkLetter.state     = fltType.charAt(3) == '1';
		chkEvent.state      = fltType.charAt(4) == '1';
		chkWebcam.state     = fltType.charAt(5) == '1';
		chkMystery.state    = fltType.charAt(6) == '1';
		chkEarth.state      = fltType.charAt(7) == '1';
		chkLocless.state    = fltType.charAt(8) == '1';
		chkMega.state       = fltType.charAt(9) == '1';
		chkCustom.state     = fltType.charAt(10) == '1';
		chkCito.state       = fltType.charAt(17) == '1';
		chkWherigo.state    = fltType.charAt(18) == '1';
		
		// Note addiWptState is set by setColors
		
		//////////////////////////
		// Panel 5 - Additional waypoints
		//////////////////////////
		chkParking.state    = fltType.charAt(11) == '1';	
		chkStage.state      = fltType.charAt(12) == '1'; 
		chkQuestion.state   = fltType.charAt(13) == '1'; 
		chkFinal.state      = fltType.charAt(14) == '1'; 
		chkTrailhead.state  = fltType.charAt(15) == '1'; 
		chkReference.state  = fltType.charAt(16) == '1';
		addiWptChk.state= !fltType.substring(11,17).equals("000000");

		//////////////////////////
		// Panel 6 - Cache container
		//////////////////////////
		String fltSize=data.getFilterSize();
		chkMicro.state      = fltSize.charAt(0) == '1';
		chkSmall.state      = fltSize.charAt(1) == '1';
		chkRegular.state    = fltSize.charAt(2) == '1';
		chkLarge.state      = fltSize.charAt(3) == '1';
		chkVeryLarge.state  = fltSize.charAt(4) == '1';
		chkOther.state      = fltSize.charAt(5) == '1';
		
		//////////////////////////
		// Panel 7 - Search
		//////////////////////////

		//////////////////////////
		// Panel 8 - Cache attributes
		//////////////////////////
		attV.setSelectionMasks( data.getFilterAttr());
		chcAttrib.select(data.getFilterAttrChoice());
		
		// Adjust colors of buttons depending on which filters are active
		setColors();
	}
	
	// Set the colors of the filter buttons according to which filters are active
	private void setColors() {
		// Panel 1 - Bearing & Distance
		if (inpDist.getText().length()>0 || 
			  !(chkNW.state && chkNNW.state && chkN.state && chkNNE.state &&
				chkNE.state && chkENE.state && chkE.state && chkESE.state &&
				chkSE.state && chkSSE.state && chkS.state && chkSSW.state &&
				chkSW.state && chkWSW.state && chkW.state && chkWNW.state && chkNoCoord.state))
			btnBearing.backGround=COLOR_FILTERACTIVE;
		else
			btnBearing.backGround=COLOR_FILTERINACTIVE;
		if (  !(chkNW.state || chkNNW.state || chkN.state || chkNNE.state ||
				chkNE.state || chkENE.state || chkE.state || chkESE.state ||
				chkSE.state || chkSSE.state || chkS.state || chkSSW.state ||
				chkSW.state || chkWSW.state || chkW.state || chkWNW.state || chkNoCoord.state))
			btnBearing.backGround=COLOR_FILTERALL;
		btnBearing.repaint();
		
		// Panel 2 - Cache attributes
		if (!( chkArchived.state    && chkAvailable.state    && chkFound.state    && chkOwned.state &&
			   chkNotArchived.state && chkNotAvailable.state && chkNotFound.state && chkNotOwned.state &&
			   chcStatus.getText().equals("")))
			btnAttributes.backGround=COLOR_FILTERACTIVE;
		else
			btnAttributes.backGround=COLOR_FILTERINACTIVE;
		if ((chkArchived.state==false && chkNotArchived.state==false) || 
			    (chkAvailable.state==false && chkNotAvailable.state==false) ||
			    (chkFound.state==false && chkNotFound.state==false) ||
			    (chkOwned.state==false && chkNotOwned.state==false)) 
			btnAttributes.backGround=COLOR_FILTERALL;
		btnAttributes.repaint();
		
		// Panel 3 - Cache ratings
		if (inpDiff.getText().length()>0 || inpTerr.getText().length()>0)
			btnRatings.backGround=COLOR_FILTERACTIVE;
		else
			btnRatings.backGround=COLOR_FILTERINACTIVE;
		btnRatings.repaint();

		// Panel 5 - Addi Waypoints
		if (chkParking.state || chkStage.state || chkQuestion.state || 
			chkFinal.state || chkTrailhead.state || chkReference.state ) { // At least one tick
			btnAddi.backGround=COLOR_FILTERACTIVE;
			addiWptChk.state=true;
			if (chkParking.state && chkStage.state &&  chkQuestion.state && 
				chkFinal.state && chkTrailhead.state && chkReference.state ) { // All ticked?
				addiWptChk.bgColor=Color.White;
				btnAddi.backGround=COLOR_FILTERINACTIVE;
			} else {	
				addiWptChk.bgColor=Color.LightGray;
			}
		} else { // All not ticked
			btnAddi.backGround=COLOR_FILTERACTIVE;
			addiWptChk.bgColor=Color.White;
			addiWptChk.state=false;
		}
		btnAddi.repaint();

		// Panel 4 - Cache types
		boolean allAddis=(chkParking.state && chkStage.state &&  chkQuestion.state && 
		chkFinal.state && chkTrailhead.state && chkReference.state) ;
		if (!(chkTrad.state && chkMulti.state && 	chkVirtual.state && chkLetter.state &&
		      chkEvent.state && chkWebcam.state && chkMystery.state && chkEarth.state &&
		      chkLocless.state && chkMega.state && chkCito.state && chkWherigo.state && chkCustom.state && allAddis) ) 
			btnTypes.backGround=COLOR_FILTERACTIVE;
		else
			btnTypes.backGround=COLOR_FILTERINACTIVE;
		if (!(chkTrad.state || chkMulti.state || 	chkVirtual.state || chkLetter.state ||
			      chkEvent.state || chkWebcam.state || chkMystery.state || chkEarth.state ||
			      chkLocless.state || chkMega.state || chkCustom.state || chkParking.state 
			      || chkStage.state || chkQuestion.state || 
					chkFinal.state || chkTrailhead.state || chkCito.state || chkWherigo.state || chkReference.state )) 
			btnTypes.backGround=COLOR_FILTERALL;
		btnTypes.repaint();

			
		// Panel 6 - Cache container
		if (!(chkMicro.state && chkSmall.state && chkRegular.state && 
			  chkLarge.state && chkVeryLarge.state && chkOther.state))
			  btnContainer.backGround=COLOR_FILTERACTIVE;
		else
			  btnContainer.backGround=COLOR_FILTERINACTIVE;
		if (!(chkMicro.state || chkSmall.state || chkRegular.state || 
				  chkLarge.state || chkVeryLarge.state || chkOther.state))
			  btnContainer.backGround=COLOR_FILTERALL;
		btnContainer.repaint();
			
		// Panel 7 - Search

		// Panel 8 - Cache attributes
		if (attV.isSetSelectionMask()) 
			btnCacheAttributes.backGround=COLOR_FILTERACTIVE;
		else
			btnCacheAttributes.backGround=COLOR_FILTERINACTIVE;
		btnCacheAttributes.repaint();

		
	}
	
	/**
	*	React to the users input, create a filter and set the variable of the filter.
	*	@see Filter
	*/
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnCancel){
				if (savedFiltersChanged) {
					Global.getPref().savePreferences();
					savedFiltersChanged = false;
				}
				fltList.select(-1);
				currentFilterID = "";
				this.close(0);
			}
			else if (ev.target == btnRoute){
				
				File datei;
				FileChooser fc = new FileChooser(FileChooserBase.OPEN, Global.getProfile().dataDir);
				fc.setTitle(MyLocale.getMsg(712,"Select route file"));
				if(fc.execute() != FormBase.IDCANCEL) {
					datei = fc.getChosenFile();
					InfoBox inf = new InfoBox("Distance?", "Dist:", InfoBox.INPUT);
					inf.execute();
					Vm.showWait(true);
					Filter flt = new Filter();
					flt.doFilterRoute(datei, Convert.toDouble(inf.feedback.getText()));
				}
				Vm.showWait(false);
				fltList.select(-1);
				currentFilterID = "";
				this.close(0);
				
			}
			else if (ev.target == btnApply){
				Vm.showWait(true);

				FilterData data = getDataFromScreen();
				Global.getProfile().setCurrentFilter(data);

				Filter flt = new Filter();
				flt.setFilter();
				flt.doFilter();
				if (savedFiltersChanged) {
					Global.getPref().savePreferences();
					savedFiltersChanged = false;
				}
				Global.mainTab.tbP.tc.scrollToVisible(0,0);
				Vm.showWait(false);
				fltList.select(-1);
				currentFilterID = "";
				this.close(0);
			} else if (ev.target == btnSaveFlt) {
				String ID = fltList.getText();
				FilterData data = getDataFromScreen();
				MessageBox mBox;
				InputBox inp = new InputBox("ID");
				String newID = inp.input(ID, 20);
				if (newID != null && !newID.equals("")) {
					if (Global.getPref().hasFilter(newID)) {
						mBox = new MessageBox(MyLocale.getMsg(221,"Overwrite Filter?"), MyLocale.getMsg(222,"The filter already exists. Overwrite it?") , FormBase.IDYES |FormBase.IDNO);
						if (mBox.execute() == FormBase.IDYES){
							Global.getPref().addFilter(newID, data);
							savedFiltersChanged = true;
							buildFilterList();
						} 
					} else {
						Global.getPref().addFilter(newID, data);
						savedFiltersChanged = true;
						buildFilterList();
					}	
				}	
			} else if (ev.target == btnDelFlt) {
				String ID = fltList.getText();
				if (!ID.equals("")) {
					FilterData data = Global.getPref().getFilter(ID);
					// We only need to delete anything, if there is already a filter of the id
					// in the list box. If not, just delete the text in the box.
					if (data != null) {
						MessageBox mBox = new MessageBox(MyLocale.getMsg(223,"Delete filter?"), ID+MyLocale.getMsg(224," - Delete this filter?") , FormBase.IDYES |FormBase.IDNO);
						if (mBox.execute() == FormBase.IDYES){
							Global.getPref().removeFilter(ID);
							fltList.setText("");
							savedFiltersChanged = true;
							this.buildFilterList();
						}							
					} else {
						fltList.setText("");
					}
				}
			} else if (ev.target == addiWptChk) { // Set all addi filters to value of main addi filter
				chkParking.setState(addiWptChk.state);
				chkStage.setState(addiWptChk.state);
				chkQuestion.setState(addiWptChk.state);
				chkFinal.setState(addiWptChk.state);
				chkTrailhead.setState(addiWptChk.state);
				chkReference.setState(addiWptChk.state);
				addiWptChk.bgColor=Color.White;
				addiWptChk.repaint();
			} else if (ev.target == btnBearing) cp.select(0);
			else if (ev.target == btnAttributes) cp.select(1);
			else if (ev.target == btnRatings)cp.select(2);
			else if (ev.target == btnTypes)cp.select(3);
			else if (ev.target == btnAddi)cp.select(4);
			else if (ev.target == btnContainer)cp.select(5);
			else if (ev.target == btnSearch)cp.select(6);
			else if (ev.target == btnCacheAttributes)cp.select(7);
			else if (ev.target == btnDeselect) {
				chkNW.state= chkNNW.state  = chkN.state    = chkNNE.state  = 
				chkNE.state   = chkENE.state  = chkE.state    = chkESE.state  = 
				chkSE.state   = chkSSE.state  = chkS.state    = chkSSW.state  = 
				chkSW.state   = chkWSW.state  = chkW.state    = chkWNW.state = false; 
				setColors();
				repaint();
					
			} else if (ev.target == btnSelect) {
				chkNW.state= chkNNW.state  = chkN.state    = chkNNE.state  = 
				chkNE.state   = chkENE.state  = chkE.state    = chkESE.state  = 
				chkSE.state   = chkSSE.state  = chkS.state    = chkSSW.state  = 
				chkSW.state   = chkWSW.state  = chkW.state    = chkWNW.state = true; 
				setColors();
				repaint();	
			}
		}
		if (ev instanceof DataChangeEvent )	{ 
			if (ev.target == fltList) {
				if (!currentFilterID.equals(fltList.getText())) {
					FilterData data = Global.getPref().getFilter(fltList.getText());
					if (data!=null) {
						currentFilterID = fltList.getText();
						this.setData(data);
						this.repaintNow();
					}
				}
			}
			setColors();
		}	
	}
	
	/**
	 * Populating the list of available filters in the comboBox from memory, so that the comboBox
	 * reflects the filters that are currenty in memory.
	 */
	private void buildFilterList() {
		while (fltList.itemsSize()>0) {
			fltList.deleteItem(0);
		}
		fltList.addItems(Global.getPref().getFilterIDs());
		fltList.updateItems();
	}
	
	/**
     * Examines the filter screen and creates a FilterData object that represents the data
     * entered in the screen.
     */
    private FilterData getDataFromScreen() {
    	FilterData data = new FilterData();
    	data.setFilterVar((chkArchived.state    ? "1" : "0") +
	    			(chkAvailable.state   ? "1" : "0") +
	    			(chkFound.state       ? "1" : "0") +
	    			(chkOwned.state       ? "1" : "0") +
	    			(chkNotArchived.state ? "1" : "0") +
	    			(chkNotAvailable.state? "1" : "0") +
	    			(chkNotFound.state    ? "1" : "0") +
	    			(chkNotOwned.state    ? "1" : "0"));							
	    data.setFilterType((chkTrad.state    ? "1" : "0") +
	    				(chkMulti.state   ? "1" : "0") +
	    				(chkVirtual.state ? "1" : "0") +
	    				(chkLetter.state  ? "1" : "0") +
	    				(chkEvent.state   ? "1" : "0") + 
	    				(chkWebcam.state  ? "1" : "0") +
	    				(chkMystery.state ? "1" : "0") +
	    				(chkEarth.state   ? "1" : "0") +
	    				(chkLocless.state ? "1" : "0") +
	    				(chkMega.state    ? "1" : "0") +
	    				(chkCustom.state  ? "1" : "0") +
	    				(chkParking.state ? "1" : "0") +
	    				(chkStage.state   ? "1" : "0") +
	    				(chkQuestion.state? "1" : "0") +
	    				(chkFinal.state   ? "1" : "0") +
	    				(chkTrailhead.state ? "1" : "0") +
	    				(chkReference.state ? "1" : "0")+
	    				(chkCito.state ? "1" : "0")+
	    				(chkWherigo.state ? "1" : "0"));
	    data.setFilterRose((chkNW.state  ? "1":"0")+
	    			 (chkNNW.state ? "1":"0")+
	    			 (chkN.state   ? "1":"0")+
	    			 (chkNNE.state ? "1":"0")+
	    			 (chkNE.state  ? "1":"0")+
	    			 (chkENE.state ? "1":"0")+
	    			 (chkE.state   ? "1":"0")+
	    			 (chkESE.state ? "1":"0")+
	    			 (chkSE.state  ? "1":"0")+
	    			 (chkSSE.state ? "1":"0")+
	    			 (chkS.state   ? "1":"0")+
	    			 (chkSSW.state ? "1":"0")+
	    			 (chkSW.state  ? "1":"0")+
	    			 (chkWSW.state ? "1":"0")+
	    			 (chkW.state   ? "1":"0")+
	    			 (chkWNW.state ? "1":"0"));
	    data.setFilterSize((chkMicro.state ? "1" : "0")+
	    			(chkSmall.state ? "1" : "0")+
	    			(chkRegular.state ? "1" : "0")+
	    			(chkLarge.state ? "1" : "0")+
	    			(chkVeryLarge.state ? "1" : "0")+
	    			(chkOther.state ? "1" : "0"));
	    
	    // Distance: If Metric system is set to imperial units,
	    //           then the entered value is meant to be miles,
	    //           otherwise it's kilometer.
	    double distValue = java.lang.Double.NaN;
	    String rawDistance = inpDist.getText().replace(',', '.');
	    String newDistance = rawDistance; // initial Value;
	    if (! rawDistance.trim().equals("")) {
	    	distValue = java.lang.Double.valueOf(rawDistance).doubleValue();
	    	if (Global.getPref().metricSystem == Metrics.IMPERIAL){
	    		newDistance = String.valueOf(Metrics.convertUnit(distValue, Metrics.MILES, Metrics.KILOMETER));
	    	}
	    }
	    if(chcDist.selectedIndex == 0) { 
	    	data.setFilterDist("L"+newDistance);
	    } else { 
	    	data.setFilterDist("G"+newDistance);
	    }
	    	
	    if(chcDiff.selectedIndex == 0) { 
	    	data.setFilterDiff("L"+inpDiff.getText());
	    } else if(chcDiff.selectedIndex == 1) { 
	    	data.setFilterDiff("="+inpDiff.getText());
	    } else {	
	    	data.setFilterDiff("G"+inpDiff.getText());
	    }	
	    	
	    if(chcTerr.selectedIndex == 0) { 
	    	data.setFilterTerr("L"+inpTerr.getText());
	    } else if(chcTerr.selectedIndex == 1){ 
	    	data.setFilterTerr("="+inpTerr.getText());
	    } else { 
	    	data.setFilterTerr("G"+inpTerr.getText());
	    }
	    data.setFilterAttr(attV.getSelectionMasks());
	    data.setFilterAttrChoice(chcAttrib.selectedIndex);
	    data.setFilterStatus(chcStatus.getText());
	    data.setUseRegexp(chkUseRegexp.getState());
	    data.setFilterNoCoord(chkNoCoord.getState());
	    return data;
    }

}
