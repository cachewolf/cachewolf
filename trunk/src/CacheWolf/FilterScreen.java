package CacheWolf;
import ewe.ui.*;
import ewe.io.*;
import ewe.graphics.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.filechooser.*;

/**
*	This class displays a form that the user uses to set the filter criteria.
*	Class ID=700
*/
public class FilterScreen extends Form{
	private static final Color COLOR_FILTERINACTIVE=new Color(190,190,190);
	private static final Color COLOR_FILTERACTIVE=new Color(0,255,0);
	private static final Color COLOR_FILTERALL=new Color(255,0,0); // Red
    	
	private mButton btnCancel, btnApply,btnRoute,
					btnBearing,btnTypes,btnAttributes,btnRatings,btnContainer,btnSearch,btnAddi;
	
	private mChoice chcDist, chcDiff, chcTerr;
	private mCheckBox chkFound, chkNotFound, chkTrad, chkVirtual, chkEvent, chkEarth, chkMega,
					  chkOwned, chkNotOwned, chkMulti, chkLetter, chkWebcam, chkMystery, chkLocless,
	                  chkCustom,chkParking,	chkStage, chkQuestion, chkFinal, chkTrailhead, chkReference,
					  chkMicro,chkSmall,chkRegular,chkLarge,chkVeryLarge,chkOther,
	                  chkArchived,chkNotArchived, chkAvailable,chkNotAvailable,
					  chkNW, chkNNW , chkN , chkNNE, chkNE, chkENE, chkE, chkESE, chkSE, chkSSE, chkS,
					  chkSSW, chkSW, chkWSW, chkW, chkWNW, chkDeselect,chkSelect;
	
	private mInput inpDist, inpTerr, inpDiff;
	
	private CellPanel pnlBearDist=new CellPanel();
	private CellPanel pnlAttributes=new CellPanel();
	private CellPanel pnlRatings=new CellPanel();
	private CellPanel pnlCacheTypes=new CellPanel();
	private CellPanel pnlContainer=new CellPanel();
	private CellPanel pnlSearch=new CellPanel();
	private CellPanel pnlRose = new CellPanel();
	private CellPanel pnlButtons=new CellPanel();
	private CellPanel pnlAddi=new CellPanel();
	private CardPanel cp=new CardPanel();

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
	
	private mButton addImg(String imgName) {
		mButton mb=new mButton(new mImage(imgName)); mb.borderWidth=0; mb.modify(NotEditable|PreferredSizeOnly,0);
		return mb;
	}
	private void addTitle(CellPanel c, String title) {
		mLabel lblTitle;
		c.addLast(lblTitle=new mLabel(title),HSTRETCH,FILL|CENTER);
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
		pnlRose.addNext(chkDeselect=new mCheckBox(MyLocale.getMsg(716,"Deselect all")));
		chkDeselect.setTag(SPAN,new Dimension(2,1));
		pnlRose.addLast(chkSelect=new mCheckBox(MyLocale.getMsg(717,"Select all")));
		pnlBearDist.addLast(pnlRose, CellConstants.STRETCH,CellConstants.FILL);
		
		//////////////////////////
		// Panel 2 - Cache attributes
		//////////////////////////
		addTitle(pnlAttributes,MyLocale.getMsg(720,"Attributes"));
		mLabel lblTitleAtt; 
		pnlAttributes.addLast(lblTitleAtt=new mLabel(MyLocale.getMsg(715,"Show all caches with status:")),HSTRETCH,FILL);
		lblTitleAtt.setTag(SPAN,new Dimension(2,1));
		pnlAttributes.addNext(chkArchived = new mCheckBox(MyLocale.getMsg(710,"Archived")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotArchived = new mCheckBox("Nicht archiviert"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(chkAvailable = new mCheckBox("Suchbar"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotAvailable = new mCheckBox(MyLocale.getMsg(711,"Not available")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(chkFound = new mCheckBox(MyLocale.getMsg(703,"Found")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotFound = new mCheckBox("Noch nicht gefunden"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(chkOwned = new mCheckBox(MyLocale.getMsg(707,"Owned")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotOwned = new mCheckBox("Anderer Besitzer"), CellConstants.DONTSTRETCH, CellConstants.FILL);

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
		pnlCacheTypes.addNext(addImg("2.png"));
		pnlCacheTypes.addNext(chkTrad = new mCheckBox("Traditonal"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("3.png"));
		pnlCacheTypes.addLast(chkMulti = new mCheckBox("Multi"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("4.png"));
		pnlCacheTypes.addNext(chkVirtual = new mCheckBox("Virtual"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("5.png"));
		pnlCacheTypes.addLast(chkLetter = new mCheckBox("Letterbox"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("6.png"));
		pnlCacheTypes.addNext(chkEvent = new mCheckBox("Event"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("11.png"));
		pnlCacheTypes.addLast(chkWebcam = new mCheckBox("Webcam"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("8.png"));
		pnlCacheTypes.addNext(chkMystery = new mCheckBox("Mystery"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("137.png"));
		pnlCacheTypes.addLast(chkEarth = new mCheckBox("Earth"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("12.png"));
		pnlCacheTypes.addNext(chkLocless = new mCheckBox("Locationless"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("453.png"));
		pnlCacheTypes.addLast(chkMega = new mCheckBox("Mega-Ev."), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		//pnlCacheTypes.addLast(addiWptChk = new mCheckBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		//pnlCacheTypes.addLast(new mLabel(""));
		pnlCacheTypes.addNext(addImg("0.png"));
		pnlCacheTypes.addNext(chkCustom = new mCheckBox("Custom"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("110.png"));
		pnlCacheTypes.addLast(addiWptChk = new myChkBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		//addiWptChk.modify(0,NotAnEditor);
		//////////////////////////
		// Panel 5 - Addi waypoints
		//////////////////////////
		addTitle(pnlAddi,MyLocale.getMsg(726,"Additional waypoints"));
		pnlAddi.addNext(addImg("pkg.png"));
		pnlAddi.addNext(chkParking = new mCheckBox("Parking"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("stage.png"));
		pnlAddi.addLast(chkStage = new mCheckBox("Stage"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("puzzle.png"));
		pnlAddi.addNext(chkQuestion = new mCheckBox("Question"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("flag.png"));
		pnlAddi.addLast(chkFinal = new mCheckBox("Final"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("trailhead.png"));
		pnlAddi.addNext(chkTrailhead = new mCheckBox("Trailhead"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("waypoint.png"));
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
		
		Frame frmScreen=new Frame();
		mLabel lblInfo; 
		frmScreen.addLast(lblInfo=new mLabel(MyLocale.getMsg(725,"Note: Filters are additive, active filter=green"))).setTag(SPAN,new Dimension(2,1));
		lblInfo.setTag(INSETS,new Insets(0,0,2,0));
		frmScreen.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
		this.addLast(frmScreen,HSTRETCH,HFILL);
		
		CellPanel pnlButtons=new CellPanel();
		pnlButtons.addLast(new mLabel("Filter"));
		pnlButtons.addLast(btnBearing=new mButton(MyLocale.getMsg(721,"Bearing")));
		pnlButtons.addLast(btnAttributes=new mButton(MyLocale.getMsg(720,"Attributes")));
		pnlButtons.addLast(btnRatings=new mButton(MyLocale.getMsg(722,"Ratings")));
		pnlButtons.addLast(btnTypes=new mButton(MyLocale.getMsg(723,"Types"))); 
		pnlButtons.addLast(btnAddi=new mButton("Addi Wpt")); 
		pnlButtons.addLast(btnContainer=new mButton(MyLocale.getMsg(724,"Container")));
		pnlButtons.addLast(btnSearch=new mButton("Search")); btnSearch.modify(Disabled,0);
		addNext(pnlButtons,HSTRETCH,FILL);

		cp.addItem(pnlBearDist,"Bear",null);
		cp.addItem(pnlAttributes,"Att",null);
		cp.addItem(pnlRatings,"DT",null); 
		cp.addItem(pnlCacheTypes,"Type",null);
		cp.addItem(pnlAddi,"Addi",null);
		cp.addItem(pnlContainer,"Size",null);
		cp.addItem(pnlSearch,"Search",null);
		addLast(cp);

		CellPanel btPanel = new CellPanel();
		btPanel.addNext(btnCancel = new mButton(MyLocale.getMsg(708,"Cancel")),CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addNext(btnApply = new mButton(MyLocale.getMsg(709,"Apply")),CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addLast(btnRoute = new mButton("Route"),CellConstants.STRETCH, CellConstants.FILL);
		addLast(btPanel.setTag(Control.SPAN, new Dimension(3,1)), CellConstants.STRETCH, CellConstants.FILL);
		setPreferredSize(240,240);
		
	}
	
	
	public void setData(){
		Profile prof=Global.getProfile();

		//////////////////////////
		// Panel 1 - Bearing & Distance
		//////////////////////////
		if (prof.filterDist.length()>1) {
			if (prof.filterDist.charAt(0)=='L')
				chcDist.select(0);
			else
				chcDist.select(1);
			inpDist.setText(prof.filterDist.substring(1));
		} else {
			chcDist.select(0);
			inpDist.setText("");
		}
		// If filter has been corrupted, pad it to 11 characters
		String fltRose=prof.filterRose;
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
		String fltVar=prof.filterVar;
		chkArchived.state      = fltVar.charAt(0) == '1';
		chkAvailable.state     = fltVar.charAt(1) == '1';
		chkFound.state         = fltVar.charAt(2) == '1';
		chkOwned.state         = fltVar.charAt(3) == '1';
		chkNotArchived.state   = fltVar.charAt(4) == '1';
		chkNotAvailable.state  = fltVar.charAt(5) == '1';
		chkNotFound.state      = fltVar.charAt(6) == '1';
		chkNotOwned.state      = fltVar.charAt(7) == '1';

		//////////////////////////
		// Panel 3 - Cache ratings
		//////////////////////////
		if (prof.filterDiff.length()>1) {
			if (prof.filterDiff.charAt(0)=='L')
				chcDiff.select(0);
			else if (prof.filterDiff.charAt(0)=='=')
				chcDiff.select(1);
			else
				chcDiff.select(2);
			inpDiff.setText(prof.filterDiff.substring(1));
		} else {
			chcDiff.select(0);
			inpDiff.setText("");
		}

		if (prof.filterTerr.length()>1) {
			if (prof.filterTerr.charAt(0)=='L')
				chcTerr.select(0);
			else if (prof.filterTerr.charAt(0)=='=')
				chcTerr.select(1);
			else
				chcTerr.select(2);
			inpTerr.setText(prof.filterTerr.substring(1));
		} else {
			chcTerr.select(0);
			inpTerr.setText("");
		}


		//////////////////////////
		// Panel 4 - Cache types
		//////////////////////////
		
		String fltType=prof.filterType;
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
		String fltSize=prof.filterSize;
		chkMicro.state      = fltSize.charAt(0) == '1';
		chkSmall.state      = fltSize.charAt(1) == '1';
		chkRegular.state    = fltSize.charAt(2) == '1';
		chkLarge.state      = fltSize.charAt(3) == '1';
		chkVeryLarge.state  = fltSize.charAt(4) == '1';
		chkOther.state      = fltSize.charAt(5) == '1';
		
		//////////////////////////
		// Panel 7 - Search
		//////////////////////////
		
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
				chkSW.state && chkWSW.state && chkW.state && chkWNW.state))
			btnBearing.backGround=COLOR_FILTERACTIVE;
		else
			btnBearing.backGround=COLOR_FILTERINACTIVE;
		if (  !(chkNW.state || chkNNW.state || chkN.state || chkNNE.state ||
				chkNE.state || chkENE.state || chkE.state || chkESE.state ||
				chkSE.state || chkSSE.state || chkS.state || chkSSW.state ||
				chkSW.state || chkWSW.state || chkW.state || chkWNW.state))
			btnBearing.backGround=COLOR_FILTERALL;
		btnBearing.repaint();
		
		// Panel 2 - Cache attributes
		if (!( chkArchived.state    && chkAvailable.state    && chkFound.state    && chkOwned.state &&
			   chkNotArchived.state && chkNotAvailable.state && chkNotFound.state && chkNotOwned.state))
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
			chkFinal.state || chkTrailhead.state || chkReference.state )
			btnAddi.backGround=COLOR_FILTERACTIVE;
		else
			btnAddi.backGround=COLOR_FILTERINACTIVE;
		btnAddi.repaint();

		// Panel 4 - Cache types
		if (!(chkTrad.state && chkMulti.state && 	chkVirtual.state && chkLetter.state &&
		      chkEvent.state && chkWebcam.state && chkMystery.state && chkEarth.state &&
		      chkLocless.state && chkMega.state && chkCustom.state) ) 
			btnTypes.backGround=COLOR_FILTERACTIVE;
		else
			btnTypes.backGround=COLOR_FILTERINACTIVE;
		if (!(chkTrad.state || chkMulti.state || 	chkVirtual.state || chkLetter.state ||
			      chkEvent.state || chkWebcam.state || chkMystery.state || chkEarth.state ||
			      chkLocless.state || chkMega.state || chkCustom.state || chkParking.state 
			      || chkStage.state || chkQuestion.state || 
					chkFinal.state || chkTrailhead.state || chkReference.state )) 
			btnTypes.backGround=COLOR_FILTERALL;
		btnTypes.repaint();

		// Panel 5 - Addi Waypoints
		if (chkParking.state || chkStage.state || chkQuestion.state || 
			chkFinal.state || chkTrailhead.state || chkReference.state ) { // At least one tick
			btnAddi.backGround=COLOR_FILTERACTIVE;
			addiWptChk.state=true;
			if (chkParking.state && chkStage.state &&  chkQuestion.state && 
				chkFinal.state && chkTrailhead.state && chkReference.state ) { // All ticked?
				addiWptChk.bgColor=Color.White;
			} else {	
				addiWptChk.bgColor=Color.LightGray;
			}
		} else { // All not ticked
			btnAddi.backGround=COLOR_FILTERINACTIVE;
			addiWptChk.bgColor=Color.White;
			addiWptChk.state=false;
		}
		btnAddi.repaint();
			
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
		
	}
	
	/**
	*	React to the users input, create a filter and set the variable of the filter.
	*	@see Filter
	*/
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnCancel){
				this.close(0);
			}
			else if (ev.target == btnRoute){
				
				File datei;
				FileChooser fc = new FileChooser(FileChooser.OPEN, Global.getProfile().dataDir);
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
				this.close(0);
				
			}
			else if (ev.target == btnApply){
				Vm.showWait(true);
				//Save filter required
				Profile pfl = Global.getProfile();
				pfl.filterVar = (chkArchived.state    ? "1" : "0") +
							(chkAvailable.state   ? "1" : "0") +
							(chkFound.state       ? "1" : "0") +
							(chkOwned.state       ? "1" : "0") +
							(chkNotArchived.state ? "1" : "0") +
							(chkNotAvailable.state? "1" : "0") +
							(chkNotFound.state    ? "1" : "0") +
							(chkNotOwned.state    ? "1" : "0");							
				pfl.filterType =(chkTrad.state    ? "1" : "0") +
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
								(chkReference.state ? "1" : "0");
				
				pfl.filterRose = (chkNW.state  ? "1":"0")+
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
							 (chkWNW.state ? "1":"0");
				pfl.filterSize =(chkMicro.state ? "1" : "0")+
							(chkSmall.state ? "1" : "0")+
							(chkRegular.state ? "1" : "0")+
							(chkLarge.state ? "1" : "0")+
							(chkVeryLarge.state ? "1" : "0")+
							(chkOther.state ? "1" : "0");
				
				if(chcDist.selectedIndex == 0) { 
					pfl.filterDist="L"+inpDist.getText();
				} else { 
					pfl.filterDist="G"+inpDist.getText();
				}
					
				if(chcDiff.selectedIndex == 0) { 
					pfl.filterDiff="L"+inpDiff.getText();
				} else if(chcDiff.selectedIndex == 1) { 
					pfl.filterDiff="="+inpDiff.getText();
				} else {	
					pfl.filterDiff="G"+inpDiff.getText();
				}	
					
				if(chcTerr.selectedIndex == 0) { 
					pfl.filterTerr="L"+inpTerr.getText();
				} else if(chcTerr.selectedIndex == 1){ 
					pfl.filterTerr="="+inpTerr.getText();
				} else { 
					pfl.filterTerr="G"+inpTerr.getText();
				}
/*				if(lastChc.selectedIndex == 0) 
					flt.daysdirec = Filter.FOUND;
				else 
					flt.daysdirec = Filter.NOTFOUND;
*/
				// Need to think about saving it here. If yes, we also need to save filter when we clear it.
				// Maybe better to auto-save index upon exit
				//InfoBox infB = new InfoBox("Info",MyLocale.getMsg(713,"Saving filter"));
				//infB.exec();
				//pfl.saveIndex(Global.getPref());
				//infB.close(0);
				Filter flt = new Filter();
				flt.setFilter();
				flt.doFilter();
				Global.mainTab.tbP.tc.scrollToVisible(0,0);
				Vm.showWait(false);
				//Tabelle neu zeichnen lassen!
				this.close(0);
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
			else if (ev.target == chkDeselect) {
				chkNW.state= chkNNW.state  = chkN.state    = chkNNE.state  = 
				chkNE.state   = chkENE.state  = chkE.state    = chkESE.state  = 
				chkSE.state   = chkSSE.state  = chkS.state    = chkSSW.state  = 
				chkSW.state   = chkWSW.state  = chkW.state    = chkWNW.state = false; 
				chkDeselect.state=false;
				repaint();
					
			} else if (ev.target == chkSelect) {
				chkNW.state= chkNNW.state  = chkN.state    = chkNNE.state  = 
				chkNE.state   = chkENE.state  = chkE.state    = chkESE.state  = 
				chkSE.state   = chkSSE.state  = chkS.state    = chkSSW.state  = 
				chkSW.state   = chkWSW.state  = chkW.state    = chkWNW.state = true; 
				chkSelect.state=false;
				repaint();	
			}
		}
		if (ev instanceof DataChangeEvent )	{ 
			setColors();
		}

	}

}
