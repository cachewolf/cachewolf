package CacheWolf;

import ewe.ui.*;

/**
 * Choose a travelbug to pick up or drop
 * @author salzkammergut
 */ 
public class TravelbugScreen extends Form {
	private myList disp;
	private mButton btCancel,btAccept;
	/** The index into the list of travelbugs indicating the selected bug */
	public int selectedItem=-1;
	
	/**
	 * A screen to choose a travelbug from a list of bugs
	 * @param tbl The list of travelbugs from which to choose
	 * @param title The title of the screen
	 * @param allowNew True if a travelbug not on the list can be selected
	 */
	TravelbugScreen(TravelbugList tbl, String title,boolean allowNew) {
		this.setTitle(title);
		this.setPreferredSize(240, -1);
		disp=new myList(tbl,allowNew);
		ScrollBarPanel sbp = new MyScrollBarPanel(disp, ScrollablePanel.NeverShowHorizontalScrollers);
		this.addLast(sbp);
		this.addNext(btCancel = new mButton(MyLocale.getMsg(614,"Cancel")),CellConstants.DONTSTRETCH, CellConstants.FILL);
		this.addLast(btAccept = new mButton("OK"),CellConstants.DONTSTRETCH, CellConstants.FILL);
		btAccept.modify(Disabled,0);
	}

	public void onEvent(Event ev){
        if (ev instanceof ListEvent && ev.type==MenuEvent.SELECTED) {
        	btAccept.modify(0,Disabled);
        	btAccept.repaint();
        }
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btCancel){
				this.close(0);
			}
			if (ev.target == btAccept){
				this.close(0);
				selectedItem=disp.getSelectedIndex(0);
			}
		}
	}

	private class myList extends SimpleList {
		private TravelbugList tbl;
		private boolean allowNew;
		private int size; 
		myList(TravelbugList tbl,boolean allowNew) {
			this.tbl=tbl;
			this.size=tbl.size();
			this.allowNew=allowNew;
		}
		
		public Object getObjectAt(int idx) {
			return getDisplayItem(idx);		
		}
		public int getItemCount() {
			return tbl.size()+ (allowNew?1:0);
		}
		public String getDisplayItem(int idx) {
			if (idx==size)
				return MyLocale.getMsg(6015,"*** OTHER ***");
			else if (tbl.getTB(idx).getName().indexOf("&#")<0)
				return tbl.getTB(idx).getName();
			else // If the name contains HTML entities, we need to convert it back
				return SafeXML.cleanback(tbl.getTB(idx).getName());
		}
	}



}
