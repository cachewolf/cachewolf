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
	public static int CHECKBOX = 1;
	public static int INPUT = 2;
	private int type = 0;
		
	public InfoBox(String title, String info){
		this.setPreferredSize(170,50);
		this.title = title;
		this.addLast(msgArea = new MessageArea(""), CellConstants.STRETCH, CellConstants.FILL);
		msgArea.setText(info);
	}
	
	public String getInput(){
		return feedback.getText();
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
		this.addNext(mC, CellConstants.STRETCH, CellConstants.FILL);
		this.addLast(mB, CellConstants.STRETCH, CellConstants.FILL);
	}
	
	public void setInfo(String info){
		msgArea.setText(info);
		this.repaintNow();
	}
	
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