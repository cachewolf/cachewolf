package CacheWolf;

import ewe.ui.*;
import ewe.sys.*;


/**
*	This form displays the list of profiles for a user to choose from,
*	when CacheWolf starts up.
*	ClassID = 1300
*/
public class ProfilesForm extends Form{
	mButton cancel, prof1,prof2,prof3,prof4;
	
	/**
	*	Constructor to create the form. It requires that the preferences
	*	have been loaded so that the list of available profiles may be passed
	*	on to this form.
	*/
	public ProfilesForm(String[] profiles){
		Locale l = Vm.getLocale();
		LocalResource lr = l.getLocalResource("cachewolf.Languages",true);
		this.setPreferredSize(200,100);
		this.title = (String)lr.get(1301,"Select Profile:");
		this.addLast(prof1 = new mButton(profiles[0]));
		this.addLast(prof2 = new mButton(profiles[1]));
		this.addLast(prof3 = new mButton(profiles[2]));
		this.addLast(prof4 = new mButton(profiles[3]));
		this.addLast(cancel = new mButton((String)lr.get(1300,"Last Setting")));
	}
	
	/**
	*	The event handler to react to a users selection.
	*	A return value is created and passed back to the calling form
	*	while it closes itself.
	*/
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == cancel){
				this.close(0);
			}
			if(ev.target == prof1) this.close(1);
			if(ev.target == prof2) this.close(2);
			if(ev.target == prof3) this.close(3);
			if(ev.target == prof4) this.close(4);
		}
	}
}
