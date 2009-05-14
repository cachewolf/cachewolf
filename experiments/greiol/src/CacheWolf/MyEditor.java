package CacheWolf;

import ewe.fx.Color;
import ewe.fx.Insets;
import ewe.ui.*;

/**
 * Descendant from ewe.ui.Editor to allow more flexibility when needed
 * @author engywuck
 */
public class MyEditor extends Editor {

	// Constraint used to align buttons of MyEditor
	public int buttonConstraints = CellConstants.CENTER;

	/** 
	 * Mainly overwritten of ewe.ui.Editor, except for the placement constraints for
	 * the buttons which allow for variable buttonConstraints.
	 */
	protected void checkButtons() {
		if (buttons != null) {
			if (buttons.size() != 0) {
				if (Gui.isSmartPhone && getSoftKeyBarFor(null) == null) {
					buttonsToSoftKeyBar(buttons, (no != null && cancel != null) ? "No/Cancel"
							: "Actions", BUTTONS_TO_SOFT_KEY_FIRST_BUTTON_SEPARATE);
				} else {
					CellPanel p = new CellPanel();
					p.defaultTags.set(INSETS, new Insets(0, 1, 0, 1));
					p.modify(AlwaysEnabled | NotAnEditor, 0); // Just in case a dialog pops up
																// with global disabling.
					for (int i = 0; i < buttons.size(); i++) {
						p.addNext((Control) buttons.get(i));
						if ((buttonsPerRow > 0) && (((i + 1) % buttonsPerRow) == 0))
							p.endRow();
					}
					p.endRow();
					CellPanel p2 = buttonsPanel = new CellPanel();
					p.defaultTags.set(INSETS, new Insets(2, 2, 2, 2));
					//
					// Here is difference from ewe.ui.Editor: CENTER -> buttonConstraints
					//
					p2.addLast(p).setControl(buttonConstraints);// p2.borderStyle =
																// Graphics.EDGE_SUNKEN;
				}
			}
		}
		if (!hasExitButton()) {
			if (Gui.isSmartPhone) {
				if (getSoftKeyBarFor(null) == null) {
					SoftKeyBar sk = makeSoftKeys();
					sk.setKey(1, "Close|" + EXIT_IDCANCEL, close, null);
				}
			} else {
				titleOK = new mButton(close);// getButton("OK");
				titleOK.backGround = Color.DarkBlue;
				((mButton) titleOK).insideColor = getBackground();
			}
		}
		if (titleOK != null)
			titleOK.modify(AlwaysEnabled | NotAnEditor, 0);
		if (titleCancel != null)
			titleCancel.modify(AlwaysEnabled | NotAnEditor, 0);
	}

}
