package CacheWolf;

import ewe.ui.*;
import ewe.fx.*;
import ewe.sys.*;

/**
*	This class shows the long description on a cache.
*/
public class DescriptionPanel extends CellPanel{
	HtmlDisplay disp = new HtmlDisplay();
	mButton btnPlus, btnMinus;
	CacheHolderDetail currCache;
	
	CellPanel buttonP = new CellPanel();
	CellPanel descP = new CellPanel();
	
	public DescriptionPanel(){
		buttonP.addNext(btnPlus = new mButton("+"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		buttonP.addLast(btnMinus = new mButton("-"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		ScrollBarPanel sbp = new ScrollBarPanel(disp, ScrollBarPanel.NeverShowHorizontalScrollers);
		descP.addLast(sbp);
		this.addLast(descP);
		this.addLast(buttonP,CellConstants.HSTRETCH,CellConstants.HFILL);

	}
	
	/**
	*	Set the text to display. Text should be HTML formated.
	*/
	String description = null;
	public void setText(CacheHolderDetail cache){
		if (currCache == cache) return;
		int scrollto = 0;
		if (cache.hasSameMainCache(currCache)) scrollto = disp.getTopLine();
		String desc;
		if (cache == null) desc = "";
		else {
			if (cache.isAddiWpt()) {
				if (cache.LongDescription != null && cache.LongDescription.length() > 0)
					 desc = cache.LongDescription + "<hr>\n"+cache.mainCache.getCacheDetails(true).LongDescription;
				else desc = cache.mainCache.getCacheDetails(true).LongDescription;

			} else // not an addi-wpt
				desc = cache.LongDescription;
		}
		if (!desc.equals(description)) {
			Vm.showWait(true); 
			if (cache.is_HTML)	disp.setHtml(desc);
			else				disp.setPlainText(desc);
			disp.scrollTo(scrollto,false);
			description = desc;
			Vm.showWait(false);
		}
		currCache = cache;
	}
	
	private void redraw() {
		int currLine;

		Vm.showWait(true);
		currLine = disp.getTopLine();
		if (currCache.is_HTML)	disp.setHtml(currCache.LongDescription);
		else				disp.setPlainText(currCache.LongDescription);
		disp.scrollTo(currLine,false);
		Vm.showWait(false);
	}
	
	/**
	 * Eventhandler
	 */
	public void onEvent(Event ev){
		
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnPlus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() + 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				redraw();
			}

			if (ev.target == btnMinus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() - 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				redraw();
			}
		}
		super.onEvent(ev);
	}

}
