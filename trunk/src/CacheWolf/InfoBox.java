package CacheWolf;
import ewe.ui.*;

public class InfoBox extends Form{
	TextMessage msgArea;
	mCheckBox mCB;
	TextMessage warnings;
	public boolean mCB_state = false;
	mButton mB = new mButton("OK");
	mButton mC = new mButton("Cancel");
	public mInput feedback = new mInput();
	public final static int CHECKBOX = 1;
	public final static int INPUT = 2;
	public final static int DISPLAY_ONLY = 3;
	public final static int PROGRESS_WITH_WARNINGS = 4;
	private int type = 0;
	/** This variable is set to true if the user closed the Info window by
	 *  clicking the "close" button. It can be used to check if a lengthy task needs to be
	 *  aborted (i.e. spidering)
	 */
	public boolean isClosed=false;

	public InfoBox(String title, String info){
		this(title, info, DISPLAY_ONLY);

		/*this.setPreferredSize(170,50);
		this.title = title;
		msgArea = new MessageArea(""), CellConstants.STRETCH, CellConstants.FILL)
		this.addLast(scP = new ScrollBarPanel(msgArea));
		msgArea.setText(info);
		mB.setHotKey(0, IKeys.ACTION);
		mB.setHotKey(0, IKeys.ENTER);
		//mB.set(Control.Invisible, true);
		//this.addLast(mB, CellConstants.STRETCH, CellConstants.FILL);
		 */
	}

	public String getInput(){
		return feedback.getText();
	}

	public void addText(String t) {
		msgArea.setText(msgArea.text + t);
		this.repaintNow();
	}

	public InfoBox(String title, String info, int ty){
		this(title, info, ty, true);
//		this.setPreferredSize(170, 50);
		relayout(false);
	}

	public InfoBox(String title, String info, int ty, boolean autoWrap) {
		type = ty;
//		this.setPreferredSize(150,50);
		// Resize InfoBox with Fontsize
		Preferences pref=Global.getPref();
		int fs = pref.fontSize;
		int sw = MyLocale.getScreenWidth();
		int psx; int psy;
		psx=170;psy=50;
		if((fs > 11) && (sw >= 200)){psx=200;psy=70;}
		if((fs > 16) && (sw >= 250)){psx=250;psy=90;}
		if((fs > 21) && (sw >= 300)){psx=300;psy=110;}
		if((fs > 24) && (sw >= 350)){psx=350;psy=130;}
		this.setPreferredSize(psx, psy);
		this.title = title;
		switch (type) {
		case CHECKBOX: 
			mCB = new mCheckBox(info);
			this.addLast(mCB, CellConstants.STRETCH, CellConstants.FILL);
			break;
		case INPUT:
			mLabel mL = new mLabel(info);
			this.addNext(mL, CellConstants.STRETCH, CellConstants.FILL);
			this.addLast(feedback, CellConstants.STRETCH, CellConstants.FILL);
			break;
		case DISPLAY_ONLY:
			msgArea = new TextMessage(info);
			msgArea.autoWrap = autoWrap;
			msgArea.alignment = CellConstants.CENTER;
			msgArea.anchor = CellConstants.CENTER;
			this.addLast(msgArea.getScrollablePanel(), CellConstants.STRETCH, CellConstants.FILL);
			break;
		case PROGRESS_WITH_WARNINGS:
			msgArea = new TextMessage(info);
			msgArea.autoWrap = autoWrap;
			msgArea.alignment = CellConstants.CENTER;
			msgArea.anchor = CellConstants.CENTER;
			msgArea.setPreferredSize(psx-20, psy);
			this.addLast(msgArea.getScrollablePanel(), CellConstants.HEXPAND | CellConstants.HGROW, CellConstants.HEXPAND | CellConstants.HGROW);
			warnings = new TextMessage("");
			warnings.autoWrap = autoWrap;
			this.addLast(warnings.getScrollablePanel(), CellConstants.HEXPAND | CellConstants.VEXPAND |CellConstants.VGROW, CellConstants.HEXPAND | CellConstants.VEXPAND |CellConstants.VGROW);
			mB.set(ControlConstants.Disabled, true);
			mB.setPreferredSize(40, 20);
			addLast(mB, CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
			break;
		}
		mC.setHotKey(0, IKeys.ESCAPE);
		mB.setHotKey(0, IKeys.ACTION);
		mB.setHotKey(0, IKeys.ENTER);
		if (ty == CHECKBOX || ty == INPUT) {
			this.addNext(mC, CellConstants.STRETCH, CellConstants.FILL);
			this.addLast(mB, CellConstants.STRETCH, CellConstants.FILL);
		}
	}

	public void setInfo(String info){
		msgArea.setText(info);
		this.repaintNow();
	}
	
	public void setInfoHeight(int heighti) {
		msgArea.setPreferredSize(getPreferredSize(null).width, heighti);
	}
	public void setInfoWidth(int widthi) {
		msgArea.setPreferredSize(widthi,getPreferredSize(null).height);
	}
	public String getInfo(){
		return msgArea.getText();
	}

	public void addWarning (String w) {
		warnings.setText(warnings.text + w);
	}
	public void addOkButton() { //unfortunately this doesn't work
		mB.set(ControlConstants.Disabled, false);
		//addNext(mB);
		//relayout(true);
		//mB.set(Control.Invisible, false);
		this.repaintNow();
	}

	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if(ev.target == mB){
				if(type == CHECKBOX) mCB_state = mCB.getState();
				this.close(FormBase.IDOK);
			}
			if(ev.target == mC){
				this.close(FormBase.IDCANCEL);
			}
		}
		super.onEvent(ev);
	}
	
	protected boolean canExit(int exitCode) {
		isClosed=true;
		return true;
	}
	
}