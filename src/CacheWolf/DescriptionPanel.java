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
	
	public DescriptionPanel(){
		ScrollBarPanel sbp = new ScrollBarPanel(disp, ScrollBarPanel.NeverShowHorizontalScrollers);
		this.addLast(sbp);
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
}
