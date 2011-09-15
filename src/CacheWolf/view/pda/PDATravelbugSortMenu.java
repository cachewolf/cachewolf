package CacheWolf.view.pda;

import CacheWolf.MyLocale;
import ewe.fx.Rect;
import ewe.ui.Window;

public class PDATravelbugSortMenu extends PDAMenu {
	private static final String NAME = "name";
	private static final String FROM_WP = "from_wp";
	private static final String FROM_DATE = "from_date";
	private static final String TO_WP = "to_wp";
	private static final String TO_DATE = "to_date";
	private static final String TRACK_NR = "track_nw";
	private static final String EXIT = "exit";
	public int sortColumn = -1;
	public boolean ascending = true;

	public PDATravelbugSortMenu() {
		super();
		PDAMenuButton button = new PDAMenuButton(MyLocale.getMsg(6028, "Name"), NAME);
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("^^", NAME + "_UP");
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("vv", NAME + "_DOWN");
		addLast(button, HSTRETCH, HFILL);

		button = new PDAMenuButton(MyLocale.getMsg(6005, "From Wpt"), FROM_WP);
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("^^", FROM_WP + "_UP");
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("vv", FROM_WP + "_DOWN");
		addLast(button, HSTRETCH, HFILL);

		button = new PDAMenuButton(MyLocale.getMsg(6006, "From Date"), FROM_DATE);
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("^^", FROM_DATE + "_UP");
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("vv", FROM_DATE + "_DOWN");
		addLast(button, HSTRETCH, HFILL);

		button = new PDAMenuButton(MyLocale.getMsg(6009, "To Wpt"), TO_WP);
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("^^", TO_WP + "_UP");
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("vv", TO_WP + "_DOWN");
		addLast(button, HSTRETCH, HFILL);

		button = new PDAMenuButton(MyLocale.getMsg(6010, "To Date"), TO_DATE);
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("^^", TO_DATE + "_UP");
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("vv", TO_DATE + "_DOWN");
		addLast(button, HSTRETCH, HFILL);

		button = new PDAMenuButton(MyLocale.getMsg(6062, "Track-No"), TRACK_NR);
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("^^", TRACK_NR + "_UP");
		addNext(button, HSTRETCH, HFILL);
		button = new PDAMenuButton("vv", TRACK_NR + "_DOWN");
		addLast(button, HSTRETCH, HFILL);

		button = new PDAMenuButton(MyLocale.getMsg(6057, "Back"), EXIT);
		addLast(button);

		Rect s = (Rect) Window.getGuiInfo(Window.INFO_SCREEN_RECT, null, new Rect(), 0);
		setPreferredSize(s.width, s.height);
	}

	public void actionPerformed(String actionCommand) {
		if (actionCommand.startsWith(NAME)) {
			sortColumn = 1;
		} else if (actionCommand.startsWith(FROM_WP)) {
			sortColumn = 5;
		} else if (actionCommand.startsWith(FROM_DATE)) {
			sortColumn = 6;
		} else if (actionCommand.startsWith(TO_WP)) {
			sortColumn = 9;
		} else if (actionCommand.startsWith(TO_DATE)) {
			sortColumn = 10;
		} else if (actionCommand.startsWith(TRACK_NR)) {
			sortColumn = 2;
		}
		ascending = actionCommand.endsWith("DOWN");
		exit(0);
	}
}
