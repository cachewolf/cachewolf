package CacheWolf;

import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollClient;

public class MyScrollBarPanel extends ScrollBarPanel {
	public MyScrollBarPanel(ScrollClient client,int options) {
		super(client, options);
		int s = java.lang.Math.round(((float)Global.getPref().fontSize) / 11f * 15f); // standard fontsize = 1, standard bottum size = 15
		setScrollBarSize(s, s, s);
	}
	public MyScrollBarPanel(ScrollClient client)
	{
		this(client,0);
	}

}
