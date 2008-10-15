package cachewolf;
import eve.ui.*;
import eve.ui.event.*;
import eve.fx.*;
import eve.sys.*;
import eve.ui.filechooser.*;

/**
*	This class displays a form that the user uses to set the filter criteria.
*	Class ID=700
*/
public class FilterScreen extends Form{
	private static final Color COLOR_FILTERINACTIVE=new Color(190,190,190);
	private static final Color COLOR_FILTERACTIVE=new Color(0,255,0);
	private static final Color COLOR_FILTERALL=new Color(255,0,0); // Red

	private Button btnCancel, btnApply,
					btnBearing,btnTypes,btnAttributes,btnRatings,btnContainer,btnAddi, btnSelect,btnDeselect,btnCacheAttributes;
	//private Button btnSearch;
	private Choice chcDist, chcDiff, chcTerr, chcAttrib;
	private CheckBox chkFound, chkNotFound, chkTrad, chkVirtual, chkEvent, chkEarth, chkMega,
					  chkOwned, chkNotOwned, chkMulti, chkLetter, chkWebcam, chkMystery, chkLocless,
	                  chkCustom,chkParking,	chkStage, chkQuestion, chkFinal, chkTrailhead, chkReference,
					  chkMicro,chkSmall,chkRegular,chkLarge,chkVeryLarge,chkOther,chkCito,
	                  chkArchived,chkNotArchived, chkAvailable,chkNotAvailable,
					  chkNW, chkNNW , chkN , chkNNE, chkNE, chkENE, chkE, chkESE, chkSE, chkSSE, chkS,
					  chkSSW, chkSW, chkWSW, chkW, chkWNW,chkWherigo;

	private Input inpDist, inpTerr, inpDiff;

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

	// A subclassed checkbox with a "third" state (=grey background).
	// If all addi wpts are false or all addi wpts are true, the background is white
	// If the addi wpt filter is a mixture of true/false, the bg is grey
	// Thus the addi filter can be set in one of two ways: Using the single checkbox with all the other
	// attributes, or using the multiple checkboxes for each addi waypoint type
	private class myChkBox extends CheckBox {
		Color bgColor=Color.White;
		myChkBox(String s) {super(s); }
		public void doPaintSquare(Graphics g) {
			int h = height;
			g.setColor(bgColor);
			int sp = 2*boxWidth/15;
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
				Pen oldPen = g.getPen(Pen.getCached());
				g.changePen(c,Pen.SOLID,sp);
//				Pen oldpen = g.setPen(new Pen(c,Pen.SOLID,2));
				g.drawLine(bx+4,by+boxWidth-5,bx+boxWidth-5,by+4);
				g.drawLine(bx+4,by+boxWidth-5,bx+4,by+boxWidth-10);
//				g.drawLine(bx+3,by+3,bx+boxWidth-5,by+boxWidth-5);
//				g.drawLine(bx+3,by+boxWidth-5,bx+boxWidth-5,by+3);
				g.set(oldPen);
				oldPen.cache();
			}
			g.draw3DRect(new Rect(bx,by,boxWidth,boxWidth),	GuiStyle.checkboxEdge,true,null,Color.DarkGray);
		}
	}
	private myChkBox addiWptChk;

	private Button addImg(String imgName) {
		Button mb=new Button(new Picture(imgName)); mb.borderWidth=0; mb.modify(NotEditable|PreferredSizeOnly,0);
		return mb;
	}
	private void addTitle(CellPanel c, String title) {
		Label lblTitle;
		c.addLast(lblTitle=new Label(title),HSTRETCH,FILL|CENTER);
		lblTitle.backGround=new Color(127,127,127);
		lblTitle.foreGround=Color.White;
		lblTitle.setTag(TAG_INSETS,new Insets(2,0,4,0));
	}

	public FilterScreen() {
		this.title = MyLocale.getMsg(700,"Set Filter");

		//////////////////////////
		// Panel 1 - Bearing & Distance
		//////////////////////////
		addTitle(pnlBearDist,MyLocale.getMsg(714,"Bearings & Distance"));
		pnlBearDist.addNext(new Label(MyLocale.getMsg(701,"Distance: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlBearDist.addNext(chcDist = new Choice(new String[]{"<=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlBearDist.addLast(inpDist = new Input(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlBearDist.addLast(new Label(""));
		pnlRose.addNext(chkNW = new CheckBox("NW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkNNW = new CheckBox("NNW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkN = new CheckBox("N"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addLast(chkNNE = new CheckBox("NNE"),CellConstants.HSTRETCH, CellConstants.FILL);

		pnlRose.addNext(chkNE = new CheckBox("NE"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkENE = new CheckBox("ENE"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkE = new CheckBox("E "),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addLast(chkESE = new CheckBox("ESE"),CellConstants.HSTRETCH, CellConstants.FILL);

		pnlRose.addNext(chkSE = new CheckBox("SE"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkSSE = new CheckBox("SSE"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkS = new CheckBox("S"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addLast(chkSSW = new CheckBox("SSW"),CellConstants.HSTRETCH, CellConstants.FILL);

		pnlRose.addNext(chkSW = new CheckBox("SW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkWSW = new CheckBox("WSW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(chkW = new CheckBox("W "),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addLast(chkWNW = new CheckBox("WNW"),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlRose.addNext(btnDeselect=new Button(MyLocale.getMsg(716,"Deselect all")),CellConstants.HSTRETCH, CellConstants.FILL);
		btnDeselect.setTag(TAG_SPAN,new Dimension(2,1));
		pnlRose.addLast(btnSelect=new Button(MyLocale.getMsg(717,"Select all")),CellConstants.HSTRETCH, CellConstants.FILL);
		pnlBearDist.addLast(pnlRose, CellConstants.STRETCH,CellConstants.FILL);

		//////////////////////////
		// Panel 2 - Cache attributes
		//////////////////////////
		addTitle(pnlAttributes,MyLocale.getMsg(720,"Status"));
		Label lblTitleAtt;
		pnlAttributes.addLast(lblTitleAtt=new Label(MyLocale.getMsg(715,"Show all caches with status:")),HSTRETCH,FILL);
		lblTitleAtt.setTag(TAG_SPAN,new Dimension(2,1));
		pnlAttributes.addNext(chkArchived = new CheckBox(MyLocale.getMsg(710,"Archived")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotArchived = new CheckBox(MyLocale.getMsg(729,"Nicht archiviert")), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlAttributes.addNext(chkAvailable = new CheckBox(MyLocale.getMsg(730,"Suchbar")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotAvailable = new CheckBox(MyLocale.getMsg(711,"Not available")), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlAttributes.addNext(chkFound = new CheckBox(MyLocale.getMsg(703,"Found")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotFound = new CheckBox(MyLocale.getMsg(731,"Noch nicht gefunden")), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlAttributes.addNext(chkOwned = new CheckBox(MyLocale.getMsg(707,"Owned")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(chkNotOwned = new CheckBox(MyLocale.getMsg(732,"Anderer Besitzer")), CellConstants.DONTSTRETCH, CellConstants.FILL);

		//////////////////////////
		// Panel 3 - Cache ratings
		//////////////////////////
		addTitle(pnlRatings,MyLocale.getMsg(718,"Cache ratings"));
		pnlRatings.addNext(new Label(MyLocale.getMsg(702,"Difficulty: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlRatings.addNext(chcDiff = new Choice(new String[]{"<=","=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//pnlRatings.addLast(difIn = new Choice(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlRatings.addLast(inpDiff = new Input(),CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlRatings.addNext(new Label("Terrain: "),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlRatings.addNext(chcTerr = new Choice(new String[]{"<=", "=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//pnlRatings.addLast(terrIn = new Choice(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlRatings.addLast(inpTerr = new Input(),CellConstants.DONTSTRETCH, CellConstants.FILL);

		//////////////////////////
		// Panel 4 - Cache types
		//////////////////////////

		addTitle(pnlCacheTypes,MyLocale.getMsg(719,"Cache types"));
		pnlCacheTypes.addNext(addImg("2.png"));
		pnlCacheTypes.addNext(chkTrad = new CheckBox("Traditonal"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("3.png"));
		pnlCacheTypes.addLast(chkMulti = new CheckBox("Multi"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("4.png"));
		pnlCacheTypes.addNext(chkVirtual = new CheckBox("Virtual"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("5.png"));
		pnlCacheTypes.addLast(chkLetter = new CheckBox("Letterbox"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("6.png"));
		pnlCacheTypes.addNext(chkEvent = new CheckBox("Event"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("11.png"));
		pnlCacheTypes.addLast(chkWebcam = new CheckBox("Webcam"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("8.png"));
		pnlCacheTypes.addNext(chkMystery = new CheckBox("Mystery"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("137.png"));
		pnlCacheTypes.addLast(chkEarth = new CheckBox("Earth"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("12.png"));
		pnlCacheTypes.addNext(chkLocless = new CheckBox("Locationless"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("453.png"));
		pnlCacheTypes.addLast(chkMega = new CheckBox("Mega-Ev."), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("13.png"));
		pnlCacheTypes.addNext(chkCito = new CheckBox("Cito-Ev."), CellConstants.DONTSTRETCH, CellConstants.FILL);

		//pnlCacheTypes.addLast(addiWptChk = new CheckBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		//pnlCacheTypes.addLast(new Label(""));
		pnlCacheTypes.addNext(addImg("0.png"));
		pnlCacheTypes.addLast(chkCustom = new CheckBox("Custom"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("1858.png"));
		pnlCacheTypes.addNext(chkWherigo = new myChkBox("WherIGo"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		pnlCacheTypes.addNext(addImg("110.png"));
		pnlCacheTypes.addNext(addiWptChk = new myChkBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		//addiWptChk.modify(0,NotAnEditor);
		//////////////////////////
		// Panel 5 - Addi waypoints
		//////////////////////////
		addTitle(pnlAddi,MyLocale.getMsg(726,"Additional waypoints"));
		pnlAddi.addNext(addImg("pkg.png"));
		pnlAddi.addNext(chkParking = new CheckBox("Parking"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("stage.png"));
		pnlAddi.addLast(chkStage = new CheckBox("Stage"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("puzzle.png"));
		pnlAddi.addNext(chkQuestion = new CheckBox("Question"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("flag.png"));
		pnlAddi.addLast(chkFinal = new CheckBox("Final"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("trailhead.png"));
		pnlAddi.addNext(chkTrailhead = new CheckBox("Trailhead"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAddi.addNext(addImg("waypoint.png"));
		pnlAddi.addLast(chkReference = new CheckBox("Reference"), CellConstants.DONTSTRETCH, CellConstants.FILL);
        pnlAddi.addLast(new Label(""),VSTRETCH,FILL);

		//////////////////////////
		// Panel 6 - Cache container
		//////////////////////////
		addTitle(pnlContainer,MyLocale.getMsg(727,"Cache container"));
		pnlContainer.addLast(chkMicro=new CheckBox("Micro"));
		pnlContainer.addLast(chkSmall=new CheckBox("Small"));
		pnlContainer.addLast(chkRegular=new CheckBox("Regular"));
		pnlContainer.addLast(chkLarge=new CheckBox("Large"));
		pnlContainer.addLast(chkVeryLarge=new CheckBox("Very Large"));
		pnlContainer.addLast(chkOther=new CheckBox("Other"));

		//////////////////////////
		// Panel 7 - Search
		//////////////////////////
		addTitle(pnlSearch,"Search");
		pnlSearch.addLast(new Label("To be implemented"));


		//////////////////////////
		// Panel 8 - Cache attributes
		//////////////////////////

		if (MyLocale.getScreenHeight()>240) addTitle(pnlCacheAttributes,MyLocale.getMsg(737,"Attributes"));
		pnlCacheAttributes.addNext(new Label(MyLocale.getMsg(739,"Filter on")+":"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlCacheAttributes.addLast(chcAttrib = new Choice(new String[]{MyLocale.getMsg(740,"all"), MyLocale.getMsg(741,"one"), MyLocale.getMsg(742,"none")},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		attV=new AttributesSelector();
		pnlCacheAttributes.addLast(attV, CellConstants.STRETCH|CellConstants.LEFT, CellConstants.STRETCH);
		attV.setSelectionMasks( 0l, 0l );

		CellPanel frmScreen=new CellPanel();
		Label lblInfo;
		frmScreen.addLast(lblInfo=new Label(MyLocale.getMsg(725,"Note: Filters are additive, active filter=green"))).setTag(TAG_SPAN,new Dimension(2,1));
		lblInfo.setTag(TAG_INSETS,new Insets(0,0,2,0));
		frmScreen.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_BOTTOM;
		this.addLast(frmScreen,HSTRETCH,HFILL);

		CellPanel pnlButtons=new CellPanel();
		pnlButtons.addLast(new Label("Filter"));
		pnlButtons.addLast(btnBearing=new Button(MyLocale.getMsg(721,"Bearing")));
		pnlButtons.addLast(btnAttributes=new Button(MyLocale.getMsg(720,"Attributes")));
		pnlButtons.addLast(btnRatings=new Button(MyLocale.getMsg(722,"Ratings")));
		pnlButtons.addLast(btnTypes=new Button(MyLocale.getMsg(723,"Types")));
		pnlButtons.addLast(btnAddi=new Button(MyLocale.getMsg(733,"Add. Wpt")));
		pnlButtons.addLast(btnContainer=new Button(MyLocale.getMsg(724,"Container")));
		pnlButtons.addLast(btnCacheAttributes=new Button(MyLocale.getMsg(738,"Attributes")));
		// Search ist für 0.9n noch deaktiviert
		//pnlButtons.addLast(btnSearch=new Button("Search")); btnSearch.modify(Disabled,0);
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

		CellPanel btPanel = new CellPanel();
		btPanel.addNext(btnCancel = new Button(MyLocale.getMsg(708,"Cancel")),CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addLast(btnApply = new Button(MyLocale.getMsg(709,"Apply")),CellConstants.STRETCH, CellConstants.FILL);
		//nbtPanel.addLast(btnRoute = new Button("Route"),CellConstants.STRETCH, CellConstants.FILL);
		addLast(btPanel.setTag(Control.TAG_SPAN, new Dimension(3,1)), CellConstants.STRETCH, CellConstants.FILL);

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

		//////////////////////////
		// Panel 8 - Cache attributes
		//////////////////////////
		attV.setSelectionMasks( prof.filterAttrYes, prof.filterAttrNo );
		chcAttrib.select(prof.filterAttrChoice);

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
		if ( attV.selectionMaskYes == 0l && attV.selectionMaskNo == 0l)
			btnCacheAttributes.backGround=COLOR_FILTERINACTIVE;
		else
			btnCacheAttributes.backGround=COLOR_FILTERACTIVE;
		btnCacheAttributes.repaint();


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
			else if (ev.target == btnApply){
				Form.showWait();
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
								(chkReference.state ? "1" : "0")+
								(chkCito.state ? "1" : "0")+
								(chkWherigo.state ? "1" : "0");
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
				pfl.filterAttrYes = attV.selectionMaskYes;
				pfl.filterAttrNo = attV.selectionMaskNo;
				pfl.filterAttrChoice = chcAttrib.selectedIndex;
				Filter flt = new Filter();
				flt.setFilter();
				flt.doFilter();
				Global.mainTab.tbP.tControl.scrollToVisible(0,0);
				Form.cancelWait();
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
//			else if (ev.target == btnSearch)cp.select(6);
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
			setColors();
		}

	}

}
