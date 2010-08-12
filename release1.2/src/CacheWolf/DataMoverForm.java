package CacheWolf;

import ewe.ui.CellConstants;
import ewe.ui.CheckBoxGroup;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.mCheckBox;
import ewe.ui.mLabel;

public class DataMoverForm extends Form {
	private mCheckBox ticked, visible, tickedVisible;
	private CheckBoxGroup chkFormat = new CheckBoxGroup();
	private mLabel firstLine;
	
	public DataMoverForm (String tickedText, String visibleText, String tickedVisibleText, String firstLineText)
	{
		title = MyLocale.getMsg(144,"Warning");
		ticked = new mCheckBox (MyLocale.getMsg(254, "All visible"));
		ticked.setGroup(chkFormat);
		visible = new mCheckBox (MyLocale.getMsg(255, "All ticked"));
		visible.setGroup(chkFormat);
		tickedVisible = new mCheckBox (MyLocale.getMsg(256, "All visible and ticked"));
		tickedVisible.setGroup(chkFormat);
		firstLine = new mLabel ("");
		firstLine.anchor = CellConstants.CENTER;
		addLast (firstLine);
		addLast (visible);
		addLast (ticked);
		addLast (tickedVisible);
		mLabel continueQuestion =new mLabel (MyLocale.getMsg(259, "Do You really want to continue?"));
		continueQuestion.anchor = CellConstants.CENTER;
		addLast (continueQuestion);
		doButtons(FormBase.YESB|FormBase.CANCELB);
		setModefromPref();
		ticked.text = tickedText;
		visible.text = visibleText;
		tickedVisible.text = tickedVisibleText;
		firstLine.text = firstLineText;
	}

	/**
	 * Gets the last mode from the preferences
	 */
	private void setModefromPref (){
		Preferences prefObject = Preferences.getPrefObject();
		switch (prefObject.processorMode){
		case 1:
			ticked.setState(true);
			break;
		case 2:
			tickedVisible.setState(true);
			break;
		case 0:
			visible.setState(true);
			break;
		}
	}
	public void onEvent(Event ev) {
		if (ev.target == yes || ev.target == no){
			Preferences.getPrefObject().processorMode = getMode();
		}
		super.onEvent(ev);
	}
	
	public int getMode (){
		if (visible.getState()){
			return 0;
		}
		else if (ticked.getState()){
			return 1;
		}
		else if (tickedVisible.getState()){
			return 2;
		}
		else{
			throw new IllegalStateException ("No radiobutton selected");
		}
	}
}
