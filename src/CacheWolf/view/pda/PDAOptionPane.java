package CacheWolf.view.pda;

import CacheWolf.Preferences;
import ewe.fx.Color;
import ewe.fx.Font;
import ewe.ui.ControlEvent;
import ewe.ui.Form;
import ewe.ui.Frame;
import ewe.ui.Gui;
import ewe.ui.mLabel;

public class PDAOptionPane extends Form {
	public static final int CANCEL = 0;
	public static final int OK = 1;
	private static final String OK_STR = "OK";
	private static final String CANCEL_STR = "CANCEL";

	private int result = CANCEL;

	public static int showConfirmDialog(Frame parent, String title, String message) {
		PDAOptionPane pane = new PDAOptionPane();
		pane.title = title;
		pane.backGround=new Color(255,128,128);
		Font tmpFont = new Font("Helvetica", Font.BOLD, Preferences.getPrefObject().fontSize * 2);
		mLabel messageLabel = new mLabel(message);
		messageLabel.font=tmpFont;
		pane.addLast(messageLabel, HFILL, HSTRETCH);
		PDAMenuButton button = new PDAMenuButton("OK", OK_STR);
		pane.addNext(button, FILL, STRETCH);
		button = new PDAMenuButton("Abbruch", CANCEL_STR);
		pane.addLast(button, FILL, STRETCH);
		pane.setLocation(0, 0);
		pane.execute(null, Gui.FILL_FRAME);
		return pane.result;
	}

	public void onControlEvent(ControlEvent event) {
		switch (event.type) {
		case ControlEvent.PRESSED:
			String action = event.action;
			if (action.equals(OK_STR)) {
				result = OK;
				exit(0);
			} else if (action.equals(CANCEL_STR)) {
				exit(0);
			}
		}
	}
}
