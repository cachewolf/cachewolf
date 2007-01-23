package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;

/**
*	Class to create the panel that holds hints and logs.
*	It holds a method to cryt and decrypt hints.
*	Two buttons allow for navigation through the logs. 5 logs are displayed at
*   together. This was implemented to allow for better performance on the
*	PocketPC. This number can be changed in the preferences.
*	Class ID=400
*/
public class HintLogPanel extends CellPanel{
	int crntLogPosition = 0;
	CacheHolder cache;
	private final int DEFAULT_STRINGBUFFER_SIZE=8000;
	mTextPad hint = new mTextPad();
	//mTextPad logs = new mTextPad();
	HtmlDisplay logs = new HtmlDisplay();
	mButton decodeButton = new mButton("Decode");
	mButton moreBt = new mButton(">>");
	mButton prevBt = new mButton("<<");
	public HintLogPanel(){
		SplittablePanel split = new SplittablePanel(PanelSplitter.VERTICAL);
		CellPanel hintpane = split.getNextPanel();
		CellPanel logpane = split.getNextPanel();
		split.setSplitter(PanelSplitter.AFTER|PanelSplitter.HIDDEN,PanelSplitter.BEFORE|PanelSplitter.HIDDEN,0);
		int initialHintHeight=Global.getPref().initialHintHeight;
		if (initialHintHeight<0 || initialHintHeight>1000) initialHintHeight=Global.getPref().DEFAULT_INITIAL_HINT_HEIGHT;
		hintpane.setPreferredSize(100,initialHintHeight); 
		ScrollBarPanel sbphint = new ScrollBarPanel(hint);
		hintpane.addLast(sbphint,CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
		hintpane.addNext(prevBt,CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		hintpane.addNext(decodeButton,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		decodeButton.setMinimumSize(MyLocale.getScreenWidth()*2/3,10);
		hintpane.addLast(moreBt,CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.EAST));
		
		ScrollBarPanel sbplog = new ScrollBarPanel((ScrollClient)logs, ScrollBarPanel.NeverShowHorizontalScrollers);
		logpane.addLast(sbplog,CellConstants.STRETCH, CellConstants.FILL);
		hint.modify(Control.NotEditable,0);
		this.addLast(split);
	}
	
	public void setText(CacheHolder cache){
		this.cache = cache;
		if(!cache.Hints.equals("null")) 
			hint.setText(cache.Hints);
		else
			hint.setText("");
		crntLogPosition = 0;
		setLogs(0);
		moreBt.modify(0,Control.Disabled);
		prevBt.modify(0,Control.Disabled);
//		if (Gui.screenIs(Gui.PDA_SCREEN) && Vm.isMobile()) {
//			Vm.setSIP(0);
//		}
		////Vm.debug("In log: " + cache.CacheLogs);
	}
	
	void setLogs(int crntLogPosition) {
		Vm.showWait(true);
		StringBuffer dummy = new StringBuffer(DEFAULT_STRINGBUFFER_SIZE);
		int counter = 0;
		int nLogs=cache.CacheLogs.size();
		int logsPerPage=Global.getPref().logsPerPage;
		for(int i = crntLogPosition; i<nLogs; i++){
			dummy.append((String)cache.CacheLogs.get(i));
			dummy.append("</br>");
			if(++counter >= logsPerPage) break;
		}
		logs.setHtml(dummy.toString());
		Vm.showWait(false);
	}
	/**
	* Method that handles user input on this panel.
	* It handles decryption of hints and navigation through
	* the logs (always 5 at a time). Navigation of logs is required
	* for performance reasons on the pocketpc.
	*/
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			int minLogs = java.lang.Math.min(Global.getPref().logsPerPage, cache.CacheLogs.size());
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
				setLogs(crntLogPosition);
			} // = moreBt
			if(ev.target == prevBt){
				moreBt.modify(0,Control.Disabled);
				moreBt.repaintNow();
				crntLogPosition -= minLogs;
				if(crntLogPosition <= 0) {
					prevBt.modify(Control.Disabled,0);
					prevBt.repaintNow();
					crntLogPosition = 0;
				}
				setLogs(crntLogPosition);
			}
			if(ev.target == decodeButton){
				hint.setText(Common.rot13(hint.getText()));
			}
		}
	}
}
