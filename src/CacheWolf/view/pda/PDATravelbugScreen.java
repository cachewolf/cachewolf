package CacheWolf.view.pda;

import CacheWolf.MyLocale;
import CacheWolf.Travelbug;
import CacheWolf.TravelbugList;
import CacheWolf.view.ITravelbugScreen;
import ewe.ui.ControlEvent;
import ewe.util.Vector;

public class PDATravelbugScreen extends PDAList implements ITravelbugScreen{

	/** The index into the list of travelbugs indicating the selected bug */
	public int selectedItem=-1;
	
	/**
	 * A screen to choose a travelbug from a list of bugs
	 * @param tbl The list of travelbugs from which to choose
	 * @param title The title of the screen
	 * @param allowNew True if a travelbug not on the list can be selected
	 */
	public PDATravelbugScreen(TravelbugList tbl, String title, boolean allowNew) {
		super();
		model = new Vector();
		for (int i=0;i < tbl.size();i++){
			Travelbug tb = tbl.getTB(i);
			model.add(tb.getName());
		}
		if (allowNew){
			model.add(MyLocale.getMsg(6015,"*** OTHER ***"));
		}
		setupTBButtons();
}

	public void onControlEvent(ControlEvent ev) {
		if (ev instanceof ControlEvent) {
			switch (ev.type) {
			case ControlEvent.PRESSED:
				if (ev.action.equals(NEXT_PAGE) ||(ev.action.equals(PREV_PAGE))) {
					super.onControlEvent(ev);	
				} else if (ev.action.startsWith(LINE)) {
					selectedItem = ev.action.charAt(LINE.length()) - '0';
//					Object clickedItem =  model.get(line + firstLine);
//					Form form = new PDATravelbugDetailPanel(tbJourney, this);
//					form.setPreferredSize(800, 600);
//					form.execute();
//					setupTBButtons();
					exit(0);
				} else if (ev.action.equals(MENUE)) {
//					Form form = new PDATravelbugMenuPanel(this);
//					form.setPreferredSize(800, 600);
//					int execute = form.execute();
//					if (execute == 1){
//						exit(0);
//					}
					setupTBButtons();
				}
				break;
			default:
				super.onControlEvent(ev);
			}
		}
	}

	public int getSelectedItem() {
		return selectedItem;
	}
}
