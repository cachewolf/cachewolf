package cachewolf;


import eve.ui.*;
import eve.sys.*;
import eve.ui.event.ControlEvent;

/**
*	This form displays profile specific data.
*	It allows the copying of the current centre to the profile centre
*/
public class CenterScreen extends Form {

	private Button btnOK, btnCurrentCentre, btnProfileCentre, btnCur2Prof, btnProf2Cur;
	Preferences pref;
	Profile profile;
	CellPanel content = new CellPanel();

	/**
	*/
	public CenterScreen(Preferences p, Profile prof){
		super();
		pref=p;
		profile=prof;
		
    	resizable =  false;
		content.setText(MyLocale.getMsg(1115,"Centre"));
		content.borderStyle=CellPanel.BDR_RAISEDOUTER|CellPanel.BDR_SUNKENINNER|CellPanel.BF_RECT;
	    //defaultTags.set(this.TAG_INSETS,new Insets(2,2,2,2));		
		title = MyLocale.getMsg(1118,"Profile")+": "+profile.name;
		content.addNext(new Label(MyLocale.getMsg(1116,"Current")));
		content.addLast(btnCurrentCentre=new Button(pref.curCentrePt.toString()),HSTRETCH,HFILL|LEFT);
		content.addNext(new Label("      "),HSTRETCH,HFILL);
		content.addNext(btnCur2Prof=new Button("   v   "),DONTSTRETCH,DONTFILL|LEFT);
		content.addNext(new Label(MyLocale.getMsg(1117,"copy")));
		content.addLast(btnProf2Cur=new Button("   ^   "),DONTSTRETCH,DONTFILL|RIGHT);
		content.addNext(new Label(MyLocale.getMsg(1118,"Profile")));
		content.addLast(btnProfileCentre=new Button(profile.centre.toString()),HSTRETCH,HFILL|LEFT);
		addLast(content,HSTRETCH,HFILL);
		addLast(new Label(""),VSTRETCH,FILL);
		//addNext(btnCancel = new Button(MyLocale.getMsg(1604,"Cancel")),DONTSTRETCH,DONTFILL|LEFT);
		addLast(btnOK = new Button("OK"),DONTSTRETCH,HFILL|RIGHT);
	}
	
	/**
	*	The event handler to react to a users selection.
	*	A return value is created and passed back to the calling form
	*	while it closes itself.
	*/
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			/*if (ev.target == btnCancel){
				close(-1);
			}*/
			if (ev.target == btnOK){
				close(1);
			}
			if (ev.target == btnCurrentCentre){
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(pref.curCentrePt, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					pref.curCentrePt.set(cs.getCoords());
					btnCurrentCentre.setText(pref.curCentrePt.toString());
					Global.getProfile().updateBearingDistance();
				}
			}
			if (ev.target == btnProfileCentre){
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(profile.centre, CWPoint.CW);
				if (cs.execute()== CoordsScreen.IDOK){
					profile.centre.set(cs.getCoords());
					btnProfileCentre.setText(profile.centre.toString());
					profile.hasUnsavedChanges=true;
				}
			}
			if (ev.target == btnCur2Prof){
				profile.centre.set(pref.curCentrePt);
				btnProfileCentre.setText(profile.centre.toString());
				profile.hasUnsavedChanges=true;
			}
			if (ev.target == btnProf2Cur){
				pref.curCentrePt.set(profile.centre);
				btnCurrentCentre.setText(pref.curCentrePt.toString());
				Global.getProfile().updateBearingDistance();
			}
		}
		super.onEvent(ev);
	}

}
