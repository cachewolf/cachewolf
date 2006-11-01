package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;

/**
*	Class to create the panel that holds hints and logs.
*	It holds a method to cryt and decrypt hints.
*	Two buttons allow for navigation through the logs. 5 logs are displayed at
*   together. This was implemented to allow for better performance on the
*	PocketPC.
*	Class ID=400
*/
public class HintLogPanel extends CellPanel{
	int crntLogPosition = 0;
	CacheHolder cache;
	
	mTextPad hint = new mTextPad();
	//mTextPad logs = new mTextPad();
	HtmlDisplay logs = new HtmlDisplay();
	mButton decodeButton = new mButton("Decode");
	mButton moreBt = new mButton(">>");
	mButton prevBt = new mButton("<<");
	public HintLogPanel(){
		//SplittablePanel split = new SplittablePanel(PanelSplitter.VERTICAL);
		//CellPanel hintpane = split.getNextPanel();
		//CellPanel logpane = split.getNextPanel();
		
		ScrollBarPanel sbphint = new ScrollBarPanel(hint);
		this.addLast(sbphint,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		this.addLast(decodeButton,CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		
		ScrollBarPanel sbplog = new ScrollBarPanel(logs, ScrollBarPanel.NeverShowHorizontalScrollers);
		this.addLast(sbplog,CellConstants.STRETCH, CellConstants.FILL);
		this.addNext(prevBt,CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		this.addLast(moreBt,CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		hint.modify(Control.NotEditable,0);
		//this.addLast(split);
	}
	
	public void setText(CacheHolder cache){
		this.cache = cache;
		Vm.showWait(true);
		if(!cache.Hints.equals("null")) hint.setText(cache.Hints);
		String dummy = new String();
		int counter = 0;
		for(int i = 0; i<cache.CacheLogs.size(); i++){
			dummy += (String)cache.CacheLogs.get(i);
			counter++;
			if(counter >= 5 || counter >= cache.CacheLogs.size()) break;
		}
		crntLogPosition = 0;
		logs.setHtml(dummy);
		moreBt.modify(0,Control.Disabled);
		prevBt.modify(0,Control.Disabled);
		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
			Vm.setSIP(0);
		}
		Vm.showWait(false);
		////Vm.debug("In log: " + cache.CacheLogs);
	}
	
	/**
	* Method that handles user input on this panel.
	* It handles decryption of hints and navigation through
	* the logs (always 5 at a time). Navigation of logs is required
	* for performance reasons on the pocketpc.
	*/
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			int minLogs = java.lang.Math.min(5, cache.CacheLogs.size());
			if(ev.target == moreBt){
				prevBt.modify(0,Control.Disabled);
				prevBt.repaintNow();
				crntLogPosition += minLogs;
				if(crntLogPosition >= cache.CacheLogs.size()) {
					//crntLogPosition = cache.CacheLogs.size()-5;
					crntLogPosition = cache.CacheLogs.size()- minLogs;
					moreBt.modify(Control.Disabled,0);
					moreBt.repaintNow();
				}
				String dummy = new String();
				int counter = 0;
				for(int i = crntLogPosition; i<cache.CacheLogs.size(); i++){
					dummy += (String)cache.CacheLogs.get(i);
					counter++;
					if(counter >= minLogs) break;
				}
				logs.setHtml(dummy);
			} // = moreBt
			if(ev.target == prevBt){
				moreBt.modify(0,Control.Disabled);
				moreBt.repaintNow();
				String dummy = new String();
				crntLogPosition -= minLogs;
				if(crntLogPosition <= 0) {
					prevBt.modify(Control.Disabled,0);
					prevBt.repaintNow();
					crntLogPosition = 0;
				}
				int counter = 0;
				for(int i = crntLogPosition; i<cache.CacheLogs.size(); i++){
					dummy += (String)cache.CacheLogs.get(i);
					counter++;
					if(counter >= minLogs) break;
				}
				logs.setHtml(dummy);
			}
			if(ev.target == decodeButton){
				hint.setText(Common.rot13(hint.getText()));
			}
		}
	}
}
