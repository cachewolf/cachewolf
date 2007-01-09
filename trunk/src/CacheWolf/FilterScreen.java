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
		content.addLast(notAvailableChk = new mCheckBox((String)lr.get(711,"Not available")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		content.addNext(foundChk = new mCheckBox((String)lr.get(703,"Found")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		content.addLast(ownedChk = new mCheckBox((String)lr.get(707,"Owned")), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		CellPanel ctype = new CellPanel();
		
		ctype.addLast(new mLabel("__________"));
		ctype.addNext(tradChk = new mCheckBox("Traditonal"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ctype.addNext(multiChk = new mCheckBox("Multi"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ctype.addLast(virtualChk = new mCheckBox("Virtual"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		ctype.addNext(letterChk = new mCheckBox("Letterbox"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ctype.addNext(eventChk = new mCheckBox("Event"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ctype.addLast(webcamChk = new mCheckBox("Webcam"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		ctype.addNext(mysteryChk = new mCheckBox("Mystery"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ctype.addNext(earthChk = new mCheckBox("Earth"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ctype.addLast(loclessChk = new mCheckBox("Locationless"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		ctype.addNext(megaChk = new mCheckBox("Mega-Ev."), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ctype.addLast(addiWptChk = new mCheckBox("Add. Wpt"), CellConstants.DONTSTRETCH, CellConstants.FILL);
		ctype.addLast(new mLabel("__________"));
		content.addLast(ctype, CellConstants.STRETCH,CellConstants.FILL);
		
		CellPanel roseP = new CellPanel();
		//Image img = new Image("rose.png");
		//ImageControl ic = new ImageControl(img);
		//mLabel ic = new mLabel("");
		roseP.addNext(NW = new mCheckBox("NW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addNext(NNW = new mCheckBox("NNW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addNext(N = new mCheckBox("N"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addLast(NNE = new mCheckBox("NNE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		roseP.addNext(NE = new mCheckBox("NE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addNext(WNW = new mCheckBox("WNW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addNext(ENE = new mCheckBox("ENE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addLast(W = new mCheckBox("W "),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		roseP.addNext(E = new mCheckBox("E "),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addNext(WSW = new mCheckBox("WSW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addNext(ESE = new mCheckBox("ESE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addLast(SW = new mCheckBox("SW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
		roseP.addNext(SSW = new mCheckBox("SSW"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addNext(S = new mCheckBox("S"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addNext(SSE = new mCheckBox("SSE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		roseP.addLast(SE = new mCheckBox("SE"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		
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
