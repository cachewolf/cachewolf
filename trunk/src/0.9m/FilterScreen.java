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
	mCheckBox foundChk, tradChk, virtualChk, eventChk;
	mCheckBox ownedChk, multiChk, letterChk, webcamChk, mysteryChk, loclessChk;
	mInput distIn, difIn, terrIn, lastIn, foundIn;
	Vector cacheDB;
	mCheckBox archivedChk, notAvailableChk;
	mCheckBox NW, NNW , N , NNE, NE;
	mCheckBox ENE, E, ESE, SE, SSE, S;
	mCheckBox SSW, SW, WSW, W, WNW;
	String dir = new String();
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	
	public FilterScreen(Vector DB, String d){
		dir = d;
		cacheDB = DB;
		//Filter einlesen
		
		//Werte setzen
		this.title = (String)lr.get(700,"Set Filter");
		
		this.addNext(new mLabel((String)lr.get(701,"Distance: ")),this.DONTSTRETCH, this.FILL);
		this.addNext(distChc = new mChoice(new String[]{"<=", ">="},0),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(distIn = new mInput(),this.DONTSTRETCH, this.FILL);
		
		this.addNext(new mLabel((String)lr.get(702,"Difficulty: ")),this.DONTSTRETCH, this.FILL);
		this.addNext(difChc = new mChoice(new String[]{"<=", ">="},0),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(difIn = new mInput(),this.DONTSTRETCH, this.FILL);
		
		this.addNext(new mLabel("Terrain: "),this.DONTSTRETCH, this.FILL);
		this.addNext(terrChc = new mChoice(new String[]{"<=", ">="},0),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addLast(terrIn = new mInput(),this.DONTSTRETCH, this.FILL);
		
		this.addNext(lastChc = new mChoice(new String[]{(String)lr.get(703,"Found"), (String)lr.get(704,"Not found")},0),this.DONTSTRETCH, (this.DONTFILL|this.WEST));
		this.addNext(new mLabel((String)lr.get(705," last ")),this.DONTSTRETCH, this.FILL);
		this.addLast(lastIn = new mInput(),this.DONTSTRETCH, this.FILL);
		
		this.addNext(new mLabel((String)lr.get(706,"Found by: ")),this.DONTSTRETCH, this.FILL);
		this.addLast(foundIn = new mInput(),this.DONTSTRETCH, this.FILL);
		
		this.addNext(archivedChk = new mCheckBox((String)lr.get(710,"Archived")), this.DONTSTRETCH, this.FILL);
		this.addLast(notAvailableChk = new mCheckBox((String)lr.get(711,"Not available")), this.DONTSTRETCH, this.FILL);
		
		this.addNext(foundChk = new mCheckBox((String)lr.get(703,"Found")), this.DONTSTRETCH, this.FILL);
		this.addLast(ownedChk = new mCheckBox((String)lr.get(707,"Owned")), this.DONTSTRETCH, this.FILL);
		
		CellPanel ctype = new CellPanel();
		ctype.addNext(tradChk = new mCheckBox("Traditonal"), this.DONTSTRETCH, this.FILL);
		ctype.addNext(multiChk = new mCheckBox("Multi"), this.DONTSTRETCH, this.FILL);
		ctype.addLast(virtualChk = new mCheckBox("Virtual"), this.DONTSTRETCH, this.FILL);
		
		ctype.addNext(letterChk = new mCheckBox("Letterbox"), this.DONTSTRETCH, this.FILL);
		ctype.addNext(eventChk = new mCheckBox("Event"), this.DONTSTRETCH, this.FILL);
		ctype.addLast(webcamChk = new mCheckBox("Webcam"), this.DONTSTRETCH, this.FILL);
		
		ctype.addNext(mysteryChk = new mCheckBox("Mystery"), this.DONTSTRETCH, this.FILL);
		ctype.addLast(loclessChk = new mCheckBox("Locationless"), this.DONTSTRETCH, this.FILL);
		
		this.addLast(ctype, this.STRETCH,this.FILL);
		
		CellPanel roseP = new CellPanel();
		//Image img = new Image("rose.png");
		//ImageControl ic = new ImageControl(img);
		mLabel ic = new mLabel("");
		roseP.addNext(NW = new mCheckBox("NW"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addNext(NNW = new mCheckBox("NNW"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addNext(N = new mCheckBox("N"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addLast(NNE = new mCheckBox("NNE"),roseP.DONTSTRETCH, roseP.FILL);
		
		roseP.addNext(NE = new mCheckBox("NE"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addNext(WNW = new mCheckBox("WNW"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addNext(ENE = new mCheckBox("ENE"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addLast(W = new mCheckBox("W "),roseP.DONTSTRETCH, roseP.FILL);
		
		roseP.addNext(E = new mCheckBox("E "),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addNext(WSW = new mCheckBox("WSW"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addNext(ESE = new mCheckBox("ESE"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addLast(SW = new mCheckBox("SW"),roseP.DONTSTRETCH, roseP.FILL);
		
		roseP.addNext(SSW = new mCheckBox("SSW"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addNext(S = new mCheckBox("S"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addNext(SSE = new mCheckBox("SSE"),roseP.DONTSTRETCH, roseP.FILL);
		roseP.addLast(SE = new mCheckBox("SE"),roseP.DONTSTRETCH, roseP.FILL);
		
		this.addLast(roseP, this.STRETCH,this.FILL);
		
		CellPanel btPanel = new CellPanel();
		btPanel.addNext(btCancel = new mButton((String)lr.get(708,"Cancel")),btPanel.STRETCH, btPanel.FILL);
		btPanel.addNext(btApply = new mButton((String)lr.get(709,"Apply")),btPanel.STRETCH, btPanel.FILL);
		btPanel.addLast(btRoute = new mButton("Route"),btPanel.STRETCH, btPanel.FILL);
		this.addLast(btPanel.setTag(Control.SPAN, new Dimension(3,1)), this.STRETCH, this.FILL);
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
				if(fc.execute() != fc.IDCANCEL) {
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
				String typ = new String();
				if(tradChk.getState()) typ = "|2|";
				if(multiChk.getState()) typ = typ + "|3|";
				if(virtualChk.getState()) typ = typ + "|4|";
				if(letterChk.getState()) typ = typ + "|5|";
				if(eventChk.getState()) typ = typ + "|6|";
				if(webcamChk.getState()) typ = typ + "|11|";
				if(mysteryChk.getState()) typ = typ + "|8|";
				if(loclessChk.getState()) typ = typ + "|12|";
				flt.type = typ;
				
				String bear = new String();
				if(N.getState()) bear = "|N|";
				if(NNE.getState()) bear = bear + "|NNE|";
				if(NE.getState()) bear = bear + "|NE|";
				if(ENE.getState()) bear = bear + "|ENE|";
				if(E.getState()) bear = bear + "|E|";
				if(ESE.getState()) bear = bear + "|ESE|";
				if(SE.getState()) bear = bear + "|SE|";
				if(SSE.getState()) bear = bear + "|SSE|";
				if(SSW.getState()) bear = bear + "|SSW|";
				if(SW.getState()) bear = bear + "|SW|";
				if(WSW.getState()) bear = bear + "|WSW|";
				if(W.getState()) bear = bear + "|W|";
				if(WNW.getState()) bear = bear + "|WNW|";
				if(NW.getState()) bear = bear + "|NW|";
				if(NNW.getState()) bear = bear + "|NNW|";
				flt.bearing = bear;
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
