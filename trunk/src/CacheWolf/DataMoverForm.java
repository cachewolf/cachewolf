package CacheWolf;

import ewe.ui.CheckBoxGroup;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mLabel;

public class DataMoverForm extends Form {
	mCheckBox ticked, visible, tickedVisible;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	mButton yesButton;
	mButton noButton;
	mLabel firstLine;
	
	public DataMoverForm ()
	{
		title = MyLocale.getMsg(144,"Warning");
		ticked = new mCheckBox (MyLocale.getMsg(254, "All visible"));
		ticked.setGroup(chkFormat);
		visible = new mCheckBox (MyLocale.getMsg(255, "All ticked"));
		visible.setGroup(chkFormat);
		tickedVisible = new mCheckBox (MyLocale.getMsg(256, "All visible and ticked"));
		tickedVisible.setGroup(chkFormat);
		firstLine = new mLabel ("");
		firstLine.anchor = mLabel.CENTER;
		addLast (firstLine);
		addLast (visible);
		addLast (ticked);
		addLast (tickedVisible);
		mLabel continueQuestion =new mLabel (MyLocale.getMsg(259, "Do You really want to continue?"));
		continueQuestion.anchor = mLabel.CENTER;
		addLast (continueQuestion);
		doButtons(Form.YESB|Form.CANCELB);
		setModefromPref();
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
	
	public void setTickedText(String value) {
		ticked.text = value;
	}

	public void setVisibleText(String value) {
		visible.text = value;
	}

	public void setTickedVisibleText(String value) {
		tickedVisible.text = value;
	}
	
	public void setFirstLineText (String value){
		firstLine.text = value;
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
