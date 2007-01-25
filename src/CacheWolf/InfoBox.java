package CacheWolf;
import ewe.ui.*;
import ewe.sys.*;

public class InfoBox extends Form{
	Locale l = Vm.getLocale();
	LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
	MessageArea msgArea;
	mCheckBox mCB;
	public boolean mCB_state = false;
	mButton mB = new mButton("OK");
	mButton mC = new mButton("Cancel");
	mInput feedback = new mInput();
	public final static int CHECKBOX = 1;
	public final static int INPUT = 2;
	private int type = 0;
		
	public InfoBox(String title, String info){
		this.setPreferredSize(170,50);
		this.title = title;
		this.addLast(msgArea = new MessageArea(""), CellConstants.STRETCH, CellConstants.FILL);
		msgArea.setText(info);
		mB.setHotKey(0, IKeys.ACTION);
		mB.setHotKey(0, IKeys.ENTER);
		//mB.set(Control.Invisible, true);
		//this.addLast(mB, CellConstants.STRETCH, CellConstants.FILL);

	}
	
	public String getInput(){
		return feedback.getText();
	}
	
	public void addText(String t) {
		msgArea.setText(msgArea.text + t);
		this.repaintNow();
	}
	
	public InfoBox(String title, String info, int ty){
		type = ty;
		this.setPreferredSize(150,50);
		this.title = title;
		if(type == CHECKBOX){
			mCB = new mCheckBox(info);
			this.addLast(mCB, CellConstants.STRETCH, CellConstants.FILL);
		}
		if(type == INPUT){
			mLabel mL = new mLabel(info);
			this.addNext(mL, CellConstants.STRETCH, CellConstants.FILL);
			this.addLast(feedback, CellConstants.STRETCH, CellConstants.FILL);
		}
		mC.setHotKey(0, IKeys.ESCAPE);
		this.addNext(mC, CellConstants.STRETCH, CellConstants.FILL);
		mB.setHotKey(0, IKeys.ACTION);
		mB.setHotKey(0, IKeys.ENTER);
		this.addLast(mB, CellConstants.STRETCH, CellConstants.FILL);
	}
	
	public void setInfo(String info){
		msgArea.setText(info);
		this.repaintNow();
	}
	
/*	public void addOkButton() { unfortunately this doesn't work
		//mB.set(Control.Invisible, false);
		this.repaintNow();
	}
*/
	public void onEvent(Event ev){
		if(ev.target == mB){
			if(type == CHECKBOX) mCB_state = mCB.getState();
			this.close(Form.IDOK);
		}
		if(ev.target == mC){
			this.close(Form.IDCANCEL);
		}
		super.onEvent(ev);
	}
}