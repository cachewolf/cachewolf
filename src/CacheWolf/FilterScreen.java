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
    	
	mButton btCancel, btApply,btRoute,btArea;
	
	mChoice distChc, difChc, terrChc, lastChc;
	mCheckBox foundChk, notFoundChk, tradChk, virtualChk, eventChk, earthChk, megaChk;
	mCheckBox ownedChk, notOwnedChk, multiChk, letterChk, webcamChk, mysteryChk,addiWptChk, loclessChk;
	mInput distIn, lastIn, foundIn, terrIn, difIn;
	Vector cacheDB;
	mCheckBox archivedChk,notArchivedChk, availableChk,notAvailableChk;
	mCheckBox NW, NNW , N , NNE, NE;
	mCheckBox ENE, E, ESE, SE, SSE, S;
	mCheckBox SSW, SW, WSW, W, WNW, chkDeselect,chkSelect;
	String dir = new String();
	
	CellPanel content = new CellPanel();
	CellPanel pnlBearDist=new CellPanel();
	CellPanel pnlAttributes=new CellPanel();
	CellPanel pnlRatings=new CellPanel();
	CellPanel pnlCacheTypes=new CellPanel();
	CellPanel pnlContainer=new CellPanel();
	CellPanel pnlSearch=new CellPanel();
	CellPanel roseP = new CellPanel();
	CardPanel cp=new CardPanel();
	CellPanel pnlButtons=new CellPanel();
	mButton btnBearing,btnTypes,btnAttributes,btnRatings,btnContainer,btnSearch;

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
		pnlBearDist.addNext(distChc = new mChoice(new String[]{"<=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlBearDist.addLast(distIn = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlBearDist.addLast(new mLabel(""));
		roseP.addNext(NW = new mCheckBox("NW"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(NNW = new mCheckBox("NNW"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(N = new mCheckBox("N"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addLast(NNE = new mCheckBox("NNE"),CellConstants.HSTRETCH, CellConstants.FILL);
		
		roseP.addNext(NE = new mCheckBox("NE"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(ENE = new mCheckBox("ENE"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(E = new mCheckBox("E "),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addLast(ESE = new mCheckBox("ESE"),CellConstants.HSTRETCH, CellConstants.FILL);

		roseP.addNext(SE = new mCheckBox("SE"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(SSE = new mCheckBox("SSE"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(S = new mCheckBox("S"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addLast(SSW = new mCheckBox("SSW"),CellConstants.HSTRETCH, CellConstants.FILL);

		roseP.addNext(SW = new mCheckBox("SW"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(WSW = new mCheckBox("WSW"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(W = new mCheckBox("W "),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addLast(WNW = new mCheckBox("WNW"),CellConstants.HSTRETCH, CellConstants.FILL);
		roseP.addNext(chkDeselect=new mCheckBox(MyLocale.getMsg(716,"Deselect all")));
		chkDeselect.setTag(SPAN,new Dimension(2,1));
		roseP.addLast(chkSelect=new mCheckBox(MyLocale.getMsg(717,"Select all")));
		pnlBearDist.addLast(roseP, CellConstants.STRETCH,CellConstants.FILL);
		
		//////////////////////////
		// Panel 2 - Cache attributes
		//////////////////////////
		addTitle(pnlAttributes,MyLocale.getMsg(720,"Attributes"));
		mLabel lblTitleAtt; 
		pnlAttributes.addLast(lblTitleAtt=new mLabel(MyLocale.getMsg(715,"Show all caches with status:")),HSTRETCH,FILL);
		lblTitleAtt.setTag(SPAN,new Dimension(2,1));
		pnlAttributes.addNext(archivedChk = new mCheckBox(MyLocale.getMsg(710,"Archived")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(notArchivedChk = new mCheckBox("Nicht archiviert"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(availableChk = new mCheckBox("Suchbar"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(notAvailableChk = new mCheckBox(MyLocale.getMsg(711,"Not available")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(foundChk = new mCheckBox(MyLocale.getMsg(703,"Found")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(notFoundChk = new mCheckBox("Noch nicht gefunden"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlAttributes.addNext(ownedChk = new mCheckBox(MyLocale.getMsg(707,"Owned")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlAttributes.addLast(notOwnedChk = new mCheckBox("Anderer Besitzer"), CellConstants.DONTSTRETCH, CellConstants.FILL);

		//////////////////////////
		// Panel 3 - Cache ratings
		//////////////////////////
		addTitle(pnlRatings,MyLocale.getMsg(718,"Cache ratings"));
		pnlRatings.addNext(new mLabel(MyLocale.getMsg(702,"Difficulty: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlRatings.addNext(difChc = new mChoice(new String[]{"<=","=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//pnlRatings.addLast(difIn = new mChoice(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlRatings.addLast(difIn = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlRatings.addNext(new mLabel("Terrain: "),CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlRatings.addNext(terrChc = new mChoice(new String[]{"<=", "=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		//pnlRatings.addLast(terrIn = new mChoice(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		pnlRatings.addLast(terrIn = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);

		//////////////////////////
		// Panel 4 - Cache types
		//////////////////////////
		
		addTitle(pnlCacheTypes,MyLocale.getMsg(719,"Cache types"));
		pnlCacheTypes.addNext(addImg("2.png"));
		pnlCacheTypes.addNext(tradChk = new mCheckBox("Traditonal"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("3.png"));
		pnlCacheTypes.addLast(multiChk = new mCheckBox("Multi"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("4.png"));
		pnlCacheTypes.addNext(virtualChk = new mCheckBox("Virtual"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("5.png"));
		pnlCacheTypes.addLast(letterChk = new mCheckBox("Letterbox"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("6.png"));
		pnlCacheTypes.addNext(eventChk = new mCheckBox("Event"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("11.png"));
		pnlCacheTypes.addLast(webcamChk = new mCheckBox("Webcam"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("8.png"));
		pnlCacheTypes.addNext(mysteryChk = new mCheckBox("Mystery"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("137.png"));
		pnlCacheTypes.addLast(earthChk = new mCheckBox("Earth"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("12.png"));
		pnlCacheTypes.addNext(loclessChk = new mCheckBox("Locationless"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("453.png"));
		pnlCacheTypes.addLast(megaChk = new mCheckBox("Mega-Ev."), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		pnlCacheTypes.addNext(addImg("110.png"));
		pnlCacheTypes.addLast(addiWptChk = new mCheckBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		pnlCacheTypes.addLast(new mLabel(""));

		//////////////////////////
		// Panel 5 - Cache container
		//////////////////////////
		addTitle(pnlContainer,MyLocale.getMsg(720,"Cache container"));
		pnlContainer.addLast(new mLabel("To be implemented"));
		
		//////////////////////////
		// Panel 6 - Search
		//////////////////////////
		addTitle(pnlSearch,"Search");
		pnlSearch.addLast(new mLabel("To be implemented"));
		
		lastChc = new mChoice(new String[]{MyLocale.getMsg(703,"Found"), MyLocale.getMsg(704,"Not found")},0);
		lastIn = new mInput();
		foundIn = new mInput();
/*		content.addNext(lastChc,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addNext(new mLabel(MyLocale.getMsg(705," last ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		content.addLast(lastIn,CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		content.addNext(new mLabel(MyLocale.getMsg(706,"Found by: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		content.addLast(foundIn,CellConstants.DONTSTRETCH, CellConstants.FILL);
*/		
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
		pnlButtons.addLast(btnContainer=new mButton(MyLocale.getMsg(724,"Container"))); btnContainer.modify(Disabled,0);
		pnlButtons.addLast(btnSearch=new mButton("Search")); btnSearch.modify(Disabled,0);
		addNext(pnlButtons,HSTRETCH,FILL);

		cp.addItem(pnlBearDist,"Bear",null);
		cp.addItem(pnlAttributes,"Att",null);
		cp.addItem(pnlRatings,"DT",null); 
		cp.addItem(pnlCacheTypes,"Type",null);
		cp.addItem(pnlContainer,"Size",null);
		cp.addItem(pnlSearch,"Search",null);
		addLast(cp);

		CellPanel btPanel = new CellPanel();
		btPanel.addNext(btCancel = new mButton(MyLocale.getMsg(708,"Cancel")),CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addNext(btApply = new mButton(MyLocale.getMsg(709,"Apply")),CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addLast(btRoute = new mButton("Route"),CellConstants.STRETCH, CellConstants.FILL);
		addLast(btPanel.setTag(Control.SPAN, new Dimension(3,1)), CellConstants.STRETCH, CellConstants.FILL);
		setPreferredSize(240,240);
		
	}
	
	
	public void setData(Vector DB, String d){
		dir = d;
		cacheDB = DB;
		Profile prof=Global.getProfile();

		//////////////////////////
		// Panel 1 - Bearing & Distance
		//////////////////////////
		if (prof.filterDist.length()>1) {
			if (prof.filterDist.charAt(0)=='<')
				distChc.select(0);
			else
				distChc.select(1);
			distIn.setText(prof.filterDist.substring(1));
		} else {
			distChc.select(0);
			distIn.setText("");
		}
		// If filter has been corrupted, pad it to 11 characters
		String fltRose=prof.filterRose;
		if (fltRose.length()<16) { 
			fltRose=(fltRose+"1111111111111111").substring(0,16); 
			prof.filterRose=fltRose; 
		}  
		NW.state   = fltRose.charAt(0) == '1';
		NNW.state  = fltRose.charAt(1) == '1';
		N.state    = fltRose.charAt(2) == '1';
		NNE.state  = fltRose.charAt(3) == '1';
		
		NE.state   = fltRose.charAt(4) == '1';
		ENE.state  = fltRose.charAt(5) == '1';
		E.state    = fltRose.charAt(6) == '1';
		ESE.state  = fltRose.charAt(7) == '1';

		SE.state   = fltRose.charAt(8) == '1';
		SSE.state  = fltRose.charAt(9) == '1';
		S.state    = fltRose.charAt(10) == '1';
		SSW.state  = fltRose.charAt(11) == '1';

		SW.state   = fltRose.charAt(12) == '1';
		WSW.state  = fltRose.charAt(13) == '1';
		W.state    = fltRose.charAt(14) == '1';
		WNW.state  = fltRose.charAt(15) == '1';
		
		//////////////////////////
		// Panel 2 - Cache attributes
		//////////////////////////
		String fltVar=prof.filterVar;
		if (fltVar.length()<8) { 
			fltVar=(fltVar+"11111111").substring(0,8); 
			prof.filterVar=fltVar; 
		}  
		archivedChk.state      = fltVar.charAt(0) == '1';
		availableChk.state     = fltVar.charAt(1) == '1';
		foundChk.state         = fltVar.charAt(2) == '1';
		ownedChk.state         = fltVar.charAt(3) == '1';
		notArchivedChk.state   = fltVar.charAt(4) == '1';
		notAvailableChk.state  = fltVar.charAt(5) == '1';
		notFoundChk.state      = fltVar.charAt(6) == '1';
		notOwnedChk.state      = fltVar.charAt(7) == '1';

		//////////////////////////
		// Panel 3 - Cache ratings
		//////////////////////////
		if (prof.filterDiff.length()>1) {
			if (prof.filterDiff.charAt(0)=='<')
				difChc.select(0);
			else if (prof.filterDiff.charAt(0)=='=')
				difChc.select(1);
			else
				difChc.select(2);
			difIn.setText(prof.filterDiff.substring(1));
		} else {
			difChc.select(0);
			difIn.setText("");
		}

		if (prof.filterTerr.length()>1) {
			if (prof.filterTerr.charAt(0)=='<')
				terrChc.select(0);
			else if (prof.filterTerr.charAt(0)=='=')
				terrChc.select(1);
			else
				terrChc.select(2);
			terrIn.setText(prof.filterTerr.substring(1));
		} else {
			terrChc.select(0);
			terrIn.setText("");
		}


		//////////////////////////
		// Panel 4 - Cache types
		//////////////////////////
		
		// If filter has been corrupted, pad it to 11 characters
		String fltType=prof.filterType;
		if (fltType.length()<11) { 
			fltType=(fltType+"11111111111").substring(0,11); 
			prof.filterType=fltType; 
		}  
		tradChk.state    = fltType.charAt(0) == '1';
		multiChk.state   = fltType.charAt(1) == '1';
		virtualChk.state = fltType.charAt(2) == '1';
		letterChk.state  = fltType.charAt(3) == '1';
		eventChk.state   = fltType.charAt(4) == '1';
		webcamChk.state  = fltType.charAt(5) == '1';
		mysteryChk.state = fltType.charAt(6) == '1';
		earthChk.state   = fltType.charAt(7) == '1';
		loclessChk.state = fltType.charAt(8) == '1';
		megaChk.state    = fltType.charAt(9) == '1';
		addiWptChk.state = fltType.charAt(10) == '1';

		//////////////////////////
		// Panel 5 - Cache container
		//////////////////////////
		
		//////////////////////////
		// Panel 6 - Search
		//////////////////////////
		
		// Adjust colors of buttons depending on which filters are active
		setColors();
	}
	
	// Set the colors of the filter buttons according to which filters are active
	private void setColors() {
		// Panel 1 - Bearing & Distance
		if (distIn.getText().length()>0 || 
			  !(NW.getState() && NNW.getState() && N.getState() && NNE.getState() &&
				NE.getState() && ENE.getState() && E.getState() && ESE.getState() &&
				SE.getState() && SSE.getState() && S.getState() && SSW.getState() &&
				SW.getState() && WSW.getState() && W.getState() && WNW.getState()))
			btnBearing.backGround=COLOR_FILTERACTIVE;
		else
			btnBearing.backGround=COLOR_FILTERINACTIVE;
		btnBearing.repaint();
		
		// Panel 2 - Cache attributes
		if (!( archivedChk.getState()    && availableChk.getState()    && foundChk.getState()    && ownedChk.getState() &&
			   notArchivedChk.getState() && notAvailableChk.getState() && notFoundChk.getState() && notOwnedChk.getState()))
			btnAttributes.backGround=COLOR_FILTERACTIVE;
		else
			btnAttributes.backGround=COLOR_FILTERINACTIVE;
		btnAttributes.repaint();
		
		// Panel 3 - Cache ratings
		if (difIn.getText().length()>0 || terrIn.getText().length()>0)
			btnRatings.backGround=COLOR_FILTERACTIVE;
		else
			btnRatings.backGround=COLOR_FILTERINACTIVE;
		btnRatings.repaint();

		// Panel 4 - Cache types
		if (!(tradChk.getState() && multiChk.getState() && 	virtualChk.getState() && letterChk.getState() &&
		      eventChk.getState() && webcamChk.getState() && mysteryChk.getState() && earthChk.getState() &&
		      loclessChk.getState() && megaChk.getState() && !addiWptChk.getState())) 
			btnTypes.backGround=COLOR_FILTERACTIVE;
		else
			btnTypes.backGround=COLOR_FILTERINACTIVE;
		btnTypes.repaint();

		// Panel 5 - Cache container

		// Panel 6 - Search
		
	}
	
	/**
	*	React to the users input, create a filter and set the variable of the filter.
	*	@see Filter
	*/
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btCancel){
				this.close(0);
			}
			else if (ev.target == btRoute){
				
				File datei;
				FileChooser fc = new FileChooser(FileChooser.OPEN, dir);
				fc.setTitle(MyLocale.getMsg(712,"Select route file"));
				if(fc.execute() != FormBase.IDCANCEL) {
					datei = fc.getChosenFile();
					InfoBox inf = new InfoBox("Distance?", "Dist:", InfoBox.INPUT);
					inf.execute();
					Vm.showWait(true);
					Filter flt = new Filter();
					flt.routeFile = datei;
					flt.doFilterRoute(cacheDB, dir, Convert.toDouble(inf.feedback.getText()));
				}
				Vm.showWait(false);
				this.close(0);
				
			}
			else if (ev.target == btApply){
				Vm.showWait(true);
				//Save filter required
				Filter flt = new Filter();
				flt.foundByMe = foundChk.getState();
				flt.notFoundByMe = notFoundChk.getState();
				flt.ownedByMe = ownedChk.getState();
				flt.notOwnedByMe = notOwnedChk.getState();
				
				flt.dist = distIn.getText();
				flt.diff = difIn.getText();
				flt.terr = terrIn.getText();
				flt.days = lastIn.getText();
				flt.by = foundIn.getText();
				flt.available = availableChk.getState();
				flt.notAvailable = notAvailableChk.getState();
				flt.archived = archivedChk.getState();
				flt.notArchived = notArchivedChk.getState();
				String filterType;
				String filterVar;
				String filterRose;
				filterVar = (archivedChk.getState()    ? "1" : "0") +
							(availableChk.getState()   ? "1" : "0") +
							(foundChk.getState()       ? "1" : "0") +
							(ownedChk.getState()       ? "1" : "0") +
							(notArchivedChk.getState() ? "1" : "0") +
							(notAvailableChk.getState()? "1" : "0") +
							(notFoundChk.getState()    ? "1" : "0") +
							(notOwnedChk.getState()    ? "1" : "0");							
							
				int typeMatchPattern = 0;
				if(tradChk.getState()) typeMatchPattern |= Filter.TRADITIONAL;
				if(multiChk.getState()) typeMatchPattern |= Filter.MULTI;
				if(virtualChk.getState()) typeMatchPattern |= Filter.VIRTUAL;
				if(letterChk.getState()) typeMatchPattern |= Filter.LETTER;
				if(eventChk.getState()) typeMatchPattern |= Filter.EVENT;
				if(webcamChk.getState()) typeMatchPattern |= Filter.WEBCAM;
				if(mysteryChk.getState()) typeMatchPattern |= Filter.MYSTERY;
				if(loclessChk.getState()) typeMatchPattern |= Filter.LOCLESS;
				if(addiWptChk.getState()) typeMatchPattern |= Filter.ADDIWPT;
				if(earthChk.getState()) typeMatchPattern |= Filter.EARTH;
				if(megaChk.getState()) typeMatchPattern |= Filter.MEGA;
				flt.typeMatchPattern = typeMatchPattern;
				filterType = 	(tradChk.getState()    ? "1" : "0") +
								(multiChk.getState()   ? "1" : "0") +
								(virtualChk.getState() ? "1" : "0") +
								(letterChk.getState()  ? "1" : "0") +
								(eventChk.getState()   ? "1" : "0") + 
								(webcamChk.getState()  ? "1" : "0") +
								(mysteryChk.getState() ? "1" : "0") +
								(earthChk.getState()   ? "1" : "0") +
								(loclessChk.getState() ? "1" : "0") +
								(megaChk.getState()    ? "1" : "0") +
								(addiWptChk.getState() ? "1" : "0");
				
				int roseMatchPattern = 0;
				if(N.getState())   roseMatchPattern |= Filter.N;
				if(NNE.getState()) roseMatchPattern |= Filter.NNE;
				if(NE.getState())  roseMatchPattern |= Filter.NE;
				if(ENE.getState()) roseMatchPattern |= Filter.ENE;
				if(E.getState())   roseMatchPattern |= Filter.E;
				if(ESE.getState()) roseMatchPattern |= Filter.ESE;
				if(SE.getState())  roseMatchPattern |= Filter.SE;
				if(SSE.getState()) roseMatchPattern |= Filter.SSE;
				if(SSW.getState()) roseMatchPattern |= Filter.SSW;
				if(SW.getState())  roseMatchPattern |= Filter.SW;
				if(WSW.getState()) roseMatchPattern |= Filter.WSW;
				if(W.getState())   roseMatchPattern |= Filter.W;
				if(WNW.getState()) roseMatchPattern |= Filter.WNW;
				if(NW.getState())  roseMatchPattern |= Filter.NW;
				if(NNW.getState()) roseMatchPattern |= Filter.NNW;
				if(S.getState())   roseMatchPattern |= Filter.S;
				flt.roseMatchPattern = roseMatchPattern;
				filterRose = (NW.getState()  ? "1":"0")+
							 (NNW.getState() ? "1":"0")+
							 (N.getState()   ? "1":"0")+
							 (NNE.getState() ? "1":"0")+
							 (NE.getState()  ? "1":"0")+
							 (ENE.getState() ? "1":"0")+
							 (E.getState()   ? "1":"0")+
							 (ESE.getState() ? "1":"0")+
							 (SE.getState()  ? "1":"0")+
							 (SSE.getState() ? "1":"0")+
							 (S.getState()   ? "1":"0")+
							 (SSW.getState() ? "1":"0")+
							 (SW.getState()  ? "1":"0")+
							 (WSW.getState() ? "1":"0")+
							 (W.getState()   ? "1":"0")+
							 (WNW.getState() ? "1":"0");
				
				Profile pfl = Global.getProfile();
				if(distChc.selectedIndex == 0) { 
					flt.distdirec = Filter.SMALLER;
					pfl.filterDist="<"+flt.dist;
				} else { 
					flt.distdirec = Filter.GREATER;
					pfl.filterDist=">"+flt.dist;
				}
					
				if(difChc.selectedIndex == 0) { 
					flt.diffdirec = Filter.SMALLER;
					pfl.filterDiff="<"+flt.diff;
				} else if(difChc.selectedIndex == 1) { 
					flt.diffdirec = Filter.EQUAL;
					pfl.filterDiff="="+flt.diff;
				} else {	
					flt.diffdirec = Filter.GREATER;
					pfl.filterDiff=">"+flt.diff;
				}	
					
				if(terrChc.selectedIndex == 0) { 
					flt.terrdirec = Filter.SMALLER;
					pfl.filterTerr="<"+flt.terr;
				} else if(terrChc.selectedIndex == 1){ 
					flt.terrdirec = Filter.EQUAL;
					pfl.filterTerr="="+flt.terr;
				} else { 
					flt.terrdirec = Filter.GREATER;
					pfl.filterTerr=">"+flt.terr;
				}
				
				if(lastChc.selectedIndex == 0) 
					flt.daysdirec = Filter.FOUND;
				else 
					flt.daysdirec = Filter.NOTFOUND;

				// Need to think about saving it here. If yes, we also need to save filter when we clear it.
				// Maybe better to auto-save index upon exit
				//InfoBox infB = new InfoBox("Info",MyLocale.getMsg(713,"Saving filter"));
				//infB.exec();
				pfl.filterRose = filterRose;
				pfl.filterType = filterType;
				pfl.filterVar = filterVar;
				//pfl.saveIndex(Global.getPref());
				//infB.close(0);
				flt.doFilter(cacheDB, dir);
				Global.mainTab.tbP.tc.scrollToVisible(0,0);
				Vm.showWait(false);
				//Tabelle neu zeichnen lassen!
				this.close(0);
			}
			else if (ev.target == btnBearing) cp.select(0);
			else if (ev.target == btnAttributes) cp.select(1);
			else if (ev.target == btnRatings)cp.select(2);
			else if (ev.target == btnTypes)cp.select(3);
			else if (ev.target == btnContainer)cp.select(4);
			else if (ev.target == btnSearch)cp.select(5);
			else if (ev.target == chkDeselect) {
				NW.state= NNW.state  = N.state    = NNE.state  = 
				NE.state   = ENE.state  = E.state    = ESE.state  = 
				SE.state   = SSE.state  = S.state    = SSW.state  = 
				SW.state   = WSW.state  = W.state    = WNW.state = false; 
				chkDeselect.state=false;
				repaint();
					
			} else if (ev.target == chkSelect) {
				NW.state= NNW.state  = N.state    = NNE.state  = 
				NE.state   = ENE.state  = E.state    = ESE.state  = 
				SE.state   = SSE.state  = S.state    = SSW.state  = 
				SW.state   = WSW.state  = W.state    = WNW.state = true; 
				chkSelect.state=false;
				repaint();	
			}
		}
		if (ev instanceof DataChangeEvent )	setColors();

	}
}
