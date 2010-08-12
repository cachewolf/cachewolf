package CacheWolf;

import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollClient;
import ewe.ui.ScrollablePanel;

/**
 * Always use this class instead of ewe.ui.ScrollBarPanel
 * as it will change it's size automatically with
 * the font size, which is entered in the preferences dialog
 * 
 * @author pfeffer
 */

public class MyScrollBarPanel extends ScrollBarPanel {
	public MyScrollBarPanel(ScrollClient client,int options) {
		super(client, options);
		int s = java.lang.Math.round(Global.getPref().fontSize / 11f * 15f); // standard fontsize = 1, standard bottum size = 15
		setScrollBarSize(s, s, s);
	}
	public MyScrollBarPanel(ScrollClient client)
	{
		this(client,0);
	}
	public ScrollablePanel getScrollablePanel()
	{
		return new MyScrollBarPanel(getScrollClient());
	}
}
