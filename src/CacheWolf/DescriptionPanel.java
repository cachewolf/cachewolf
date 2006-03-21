package CacheWolf;

import ewe.ui.*;
import ewe.io.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;

/**
*	This class shows the long description on a cache.
*/
public class DescriptionPanel extends CellPanel{
	mTextPad myPad = new mTextPad();
	HtmlDisplay disp = new HtmlDisplay();
	mButton btnPlus, btnMinus;
	
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
	public void setText(CacheHolder cache){
		Vm.showWait(true);
		if (cache.is_HTML)	disp.setHtml(cache.LongDescription);
		else				disp.setPlainText(cache.LongDescription);
		Vm.showWait(false);
		//myPad.setText(cache.LongDescription);
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
			}
			if (ev.target == btnMinus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() - 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
			}
		}
		super.onEvent(ev);
	}

}
