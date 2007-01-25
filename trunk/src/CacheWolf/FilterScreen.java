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
	mButton btCancel, btApply,btRoute,btArea;
	
	mChoice distChc, difChc, terrChc, lastChc;
	mCheckBox foundChk, tradChk, virtualChk, eventChk, earthChk, megaChk;
	mCheckBox ownedChk, multiChk, letterChk, webcamChk, mysteryChk,addiWptChk, loclessChk;
	mInput distIn, difIn, terrIn, lastIn, foundIn;
	Vector cacheDB;
	mCheckBox archivedChk, notAvailableChk;
	mCheckBox NW, NNW , N , NNE, NE;
	mCheckBox ENE, E, ESE, SE, SSE, S;
	mCheckBox SSW, SW, WSW, W, WNW;
	String dir = new String();
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	CellPanel content = new CellPanel();
	ScrollBarPanel scp;
	
	public FilterScreen(Vector DB, String d){
		dir = d;
		cacheDB = DB;
		//Filter einlesen
		
		//Werte setzen
		this.title = (String)lr.get(700,"Set Filter");
		
		scp = new ScrollBarPanel(content);
		
		content.addNext(new mLabel((String)lr.get(701,"Distance: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		content.addNext(distChc = new mChoice(new String[]{"<=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addLast(distIn = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		content.addNext(new mLabel((String)lr.get(702,"Difficulty: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		content.addNext(difChc = new mChoice(new String[]{"<=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addLast(difIn = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		content.addNext(new mLabel("Terrain: "),CellConstants.DONTSTRETCH, CellConstants.FILL);
		content.addNext(terrChc = new mChoice(new String[]{"<=", ">="},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addLast(terrIn = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		content.addNext(lastChc = new mChoice(new String[]{(String)lr.get(703,"Found"), (String)lr.get(704,"Not found")},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		content.addNext(new mLabel((String)lr.get(705," last ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		content.addLast(lastIn = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		content.addNext(new mLabel((String)lr.get(706,"Found by: ")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		content.addLast(foundIn = new mInput(),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		content.addNext(archivedChk = new mCheckBox((String)lr.get(710,"Archived")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		archivedChk.state = (Global.getProfile().filterVar.charAt(0) == '1' ? true : false);
		content.addLast(notAvailableChk = new mCheckBox((String)lr.get(711,"Not available")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		notAvailableChk.state = (Global.getProfile().filterVar.charAt(1) == '1' ? true : false);
		
		content.addNext(foundChk = new mCheckBox((String)lr.get(703,"Found")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		foundChk.state = (Global.getProfile().filterVar.charAt(2) == '1' ? true : false);
		content.addLast(ownedChk = new mCheckBox((String)lr.get(707,"Owned")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ownedChk.state = (Global.getProfile().filterVar.charAt(3) == '1' ? true : false);
		
		CellPanel ctype = new CellPanel();
		
		ctype.addLast(new mLabel("__________"));
		ctype.addNext(tradChk = new mCheckBox("Traditonal"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		tradChk.state = (Global.getProfile().filterType.charAt(0) == '1' ? true : false);
		ctype.addNext(multiChk = new mCheckBox("Multi"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		multiChk.state = (Global.getProfile().filterType.charAt(1) == '1' ? true : false);
		ctype.addLast(virtualChk = new mCheckBox("Virtual"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		virtualChk.state = (Global.getProfile().filterType.charAt(2) == '1' ? true : false);
		
		ctype.addNext(letterChk = new mCheckBox("Letterbox"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		letterChk.state = (Global.getProfile().filterType.charAt(3) == '1' ? true : false);
		ctype.addNext(eventChk = new mCheckBox("Event"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		eventChk.state = (Global.getProfile().filterType.charAt(4) == '1' ? true : false);
		ctype.addLast(webcamChk = new mCheckBox("Webcam"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		webcamChk.state = (Global.getProfile().filterType.charAt(5) == '1' ? true : false);
		
		ctype.addNext(mysteryChk = new mCheckBox("Mystery"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		mysteryChk.state = (Global.getProfile().filterType.charAt(6) == '1' ? true : false);
		ctype.addNext(earthChk = new mCheckBox("Earth"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		earthChk.state = (Global.getProfile().filterType.charAt(7) == '1' ? true : false);
		ctype.addLast(loclessChk = new mCheckBox("Locationless"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		loclessChk.state = (Global.getProfile().filterType.charAt(8) == '1' ? true : false);
		
		ctype.addNext(megaChk = new mCheckBox("Mega-Ev."), CellConstants.DONTSTRETCH, CellConstants.FILL);
		megaChk.state = (Global.getProfile().filterType.charAt(9) == '1' ? true : false);
		ctype.addLast(addiWptChk = new mCheckBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		addiWptChk.state = (Global.getProfile().filterType.charAt(10) == '1' ? true : false);
		ctype.addLast(new mLabel("__________"));
		content.addLast(ctype, CellConstants.STRETCH,CellConstants.FILL);
		
		CellPanel roseP = new CellPanel();
		//Image img = new Image("rose.png");
		//ImageControl ic = new ImageControl(img);
		//mLabel ic = new mLabel("");
		roseP.addNext(NW = new mCheckBox("NW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		NW.state = (Global.getProfile().filterRose.charAt(0) == '1' ? true : false);
		roseP.addNext(NNW = new mCheckBox("NNW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		NNW.state = (Global.getProfile().filterRose.charAt(1) == '1' ? true : false);
		roseP.addNext(N = new mCheckBox("N"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		N.state = (Global.getProfile().filterRose.charAt(2) == '1' ? true : false);
		roseP.addLast(NNE = new mCheckBox("NNE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		NNE.state = (Global.getProfile().filterRose.charAt(3) == '1' ? true : false);
		
		roseP.addNext(NE = new mCheckBox("NE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		NE.state = (Global.getProfile().filterRose.charAt(4) == '1' ? true : false);
		roseP.addNext(WNW = new mCheckBox("WNW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		WNW.state = (Global.getProfile().filterRose.charAt(5) == '1' ? true : false);
		roseP.addNext(ENE = new mCheckBox("ENE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		ENE.state = (Global.getProfile().filterRose.charAt(6) == '1' ? true : false);
		roseP.addLast(W = new mCheckBox("W "),CellConstants.DONTSTRETCH, CellConstants.FILL);
		W.state = (Global.getProfile().filterRose.charAt(7) == '1' ? true : false);
		
		roseP.addNext(E = new mCheckBox("E "),CellConstants.DONTSTRETCH, CellConstants.FILL);
		E.state = (Global.getProfile().filterRose.charAt(8) == '1' ? true : false);
		roseP.addNext(WSW = new mCheckBox("WSW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		WSW.state = (Global.getProfile().filterRose.charAt(9) == '1' ? true : false);
		roseP.addNext(ESE = new mCheckBox("ESE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		ESE.state = (Global.getProfile().filterRose.charAt(10) == '1' ? true : false);
		roseP.addLast(SW = new mCheckBox("SW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		SW.state = (Global.getProfile().filterRose.charAt(11) == '1' ? true : false);
		
		roseP.addNext(SSW = new mCheckBox("SSW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		SSW.state = (Global.getProfile().filterRose.charAt(12) == '1' ? true : false);
		roseP.addNext(S = new mCheckBox("S"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		S.state = (Global.getProfile().filterRose.charAt(13) == '1' ? true : false);
		roseP.addNext(SSE = new mCheckBox("SSE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		SSE.state = (Global.getProfile().filterRose.charAt(14) == '1' ? true : false);
		roseP.addLast(SE = new mCheckBox("SE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		SE.state = (Global.getProfile().filterRose.charAt(15) == '1' ? true : false);
		
		content.addLast(roseP, CellConstants.STRETCH,CellConstants.FILL);
		CellPanel btPanel = new CellPanel();
		btPanel.addNext(btCancel = new mButton((String)lr.get(708,"Cancel")),CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addNext(btApply = new mButton((String)lr.get(709,"Apply")),CellConstants.STRETCH, CellConstants.FILL);
		btPanel.addLast(btRoute = new mButton("Route"),CellConstants.STRETCH, CellConstants.FILL);
		content.addLast(btPanel.setTag(Control.SPAN, new Dimension(3,1)), CellConstants.STRETCH, CellConstants.FILL);
		
		this.addLast(scp.getScrollablePanel(), CellConstants.STRETCH, CellConstants.FILL);
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
			if (ev.target == btRoute){
				
				File datei;
				FileChooser fc = new FileChooser(FileChooser.OPEN, dir);
				fc.setTitle((String)lr.get(712,"Select route file"));
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
			if (ev.target == btApply){
				Vm.showWait(true);
				//Save filter required
				Filter flt = new Filter();
				flt.foundByMe = foundChk.getState();
				flt.ownedByMe = ownedChk.getState();
				
				flt.dist = distIn.getText();
				flt.diff = difIn.getText();
				flt.terr = terrIn.getText();
				flt.days = lastIn.getText();
				flt.by = foundIn.getText();
				flt.notAvailable = notAvailableChk.getState();
				flt.archived = archivedChk.getState();
				String filterType = new String();
				String filterVar = new String();
				String filterRose = new String();
				filterVar = (archivedChk.getState() == true ? "1" : "0")+
							(notAvailableChk.getState() == true ? "1" : "0") +
							(foundChk.getState() == true ? "1" : "0") +
							(ownedChk.getState() == true ? "1" : "0");
							
							
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
				filterType = 	(tradChk.getState() == true ? "1" : "0") +
								(multiChk.getState() == true ? "1" : "0") +
								(virtualChk.getState() == true ? "1" : "0") +
								(letterChk.getState() == true ? "1" : "0") +
								(eventChk.getState() == true ? "1" : "0")+ 
								(webcamChk.getState() == true ? "1" : "0")+
								(mysteryChk.getState() == true ? "1" : "0")+
								(earthChk.getState() == true ? "1" : "0")+
								(loclessChk.getState() == true ? "1" : "0")+
								(megaChk.getState() == true ? "1" : "0")+
								(addiWptChk.getState() == true ? "1" : "0");
				
				int roseMatchPattern = 0;
				if(N.getState()) roseMatchPattern |= Filter.N;
				if(NNE.getState()) roseMatchPattern |= Filter.NNE;
				if(NE.getState()) roseMatchPattern |= Filter.NE;
				if(ENE.getState()) roseMatchPattern |= Filter.ENE;
				if(E.getState()) roseMatchPattern |= Filter.E;
				if(ESE.getState()) roseMatchPattern |= Filter.ESE;
				if(SE.getState()) roseMatchPattern |= Filter.SE;
				if(SSE.getState()) roseMatchPattern |= Filter.SSE;
				if(SSW.getState()) roseMatchPattern |= Filter.SSW;
				if(SW.getState()) roseMatchPattern |= Filter.SW;
				if(WSW.getState()) roseMatchPattern |= Filter.WSW;
				if(W.getState()) roseMatchPattern |= Filter.W;
				if(WNW.getState()) roseMatchPattern |= Filter.WNW;
				if(NW.getState()) roseMatchPattern |= Filter.NW;
				if(NNW.getState()) roseMatchPattern |= Filter.NNW;
				if(S.getState()) roseMatchPattern |= Filter.S;
				flt.roseMatchPattern = roseMatchPattern;
				filterRose = (N.getState() == true ? "1":"0")+
							 (NNE.getState() == true ? "1":"0")+
							 (NE.getState() == true ? "1":"0")+
							 (ENE.getState() == true ? "1":"0")+
							 (E.getState() == true ? "1":"0")+
							 (ESE.getState() == true ? "1":"0")+
							 (SE.getState() == true ? "1":"0")+
							 (SSE.getState() == true ? "1":"0")+
							 (SSW.getState() == true ? "1":"0")+
							 (SW.getState() == true ? "1":"0")+
							 (WSW.getState() == true ? "1":"0")+
							 (W.getState() == true ? "1":"0")+
							 (WNW.getState() == true ? "1":"0")+
							 (NW.getState() == true ? "1":"0")+
							 (NNW.getState() == true ? "1":"0")+
							 (S.getState() == true ? "1":"0");
				
				InfoBox infB = new InfoBox("Info",MyLocale.getMsg(713,"Saving filter"));
				infB.exec();
				Profile pfl = Global.getProfile();
				pfl.filterRose = filterRose;
				pfl.filterType = filterType;
				pfl.filterVar = filterVar;
				pfl.saveIndex(Global.getPref());
				infB.close(0);
				
				if(distChc.selectedIndex == 1) flt.distdirec = Filter.SMALLER;
				else flt.distdirec = Filter.GREATER;
				if(difChc.selectedIndex == 1) flt.diffdirec = Filter.SMALLER;
				else flt.diffdirec = Filter.GREATER;
				if(terrChc.selectedIndex == 1) flt.terrdirec = Filter.SMALLER;
				else flt.terrdirec = Filter.GREATER;
				if(lastChc.selectedIndex == 0) flt.daysdirec = Filter.FOUND;
				else flt.daysdirec = Filter.NOTFOUND;
				flt.doFilter(cacheDB, dir);
				Vm.showWait(false);
				//Tabelle neu zeichnen lassen!
				this.close(0);
			}
		}
	}
}
