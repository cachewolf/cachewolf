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
	mInput feedback = new mInput();
	public static int CHECKBOX = 1;
	public static int INPUT = 2;
	private int type = 0;
		
	public InfoBox(String title, String info){
		this.setPreferredSize(150,50);
		this.title = title;
		this.addLast(msgArea = new MessageArea(""), this.STRETCH, this.FILL);
		msgArea.setText(info);
	}
	
	public InfoBox(String title, String info, int ty){
		type = ty;
		this.setPreferredSize(150,50);
		this.title = title;
		if(type == CHECKBOX){
			mCB = new mCheckBox(info);
			this.addLast(mCB, this.STRETCH, this.FILL);
		}
		if(type == INPUT){
			mLabel mL = new mLabel(info);
			this.addNext(mL, this.STRETCH, this.FILL);
			this.addLast(feedback, this.STRETCH, this.FILL);
		}
		this.addLast(mB, this.STRETCH, this.FILL);
	}
	
	public void setInfo(String info){
		msgArea.setText(info);
		this.repaintNow();
	}
	
	public void onEvent(Event ev){
		if(ev.target == mB){
			if(type == CHECKBOX) mCB_state = mCB.getState();
			this.close(0);
		}
		super.onEvent(ev);
	}
}