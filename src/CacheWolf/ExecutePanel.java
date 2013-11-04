package CacheWolf;

import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.Window;
import ewe.ui.mButton;

public class ExecutePanel extends CellPanel {
    public final static int LEFT = 1, MIDDLE = 2, RIGHT = 4, CANCEL = LEFT, APPLY = RIGHT; // flags
    public mButton leftButton, middleButton, rightButton;
    public mButton cancelButton, refuseButton, applyButton;
    private int flags;

    public ExecutePanel(CellPanel panel) {
	this.equalWidths = true;
	cancelButton = GuiImageBroker.getButton(MyLocale.getMsg(614, "Cancel"), "cancel");
	cancelButton.setHotKey(0, IKeys.ESCAPE);
	this.addNext(cancelButton);
	applyButton = GuiImageBroker.getButton(MyLocale.getMsg(615, "Apply"), "apply");
	applyButton.setHotKey(0, IKeys.ACTION);
	applyButton.setHotKey(0, IKeys.ENTER);
	this.addNext(applyButton);
	panel.addLast(this, DONTSTRETCH, FILL);
	leftButton = cancelButton;
	rightButton = applyButton;
	flags = LEFT & RIGHT;
    }

    /** Cancel buttons should come before OK buttons. I changed the meaning: first cancel, then ok */
    private static boolean reverse = (Gui.getGuiFlags() & Window.GUI_FLAG_REVERSE_OK_CANCEL) == 0;

    public ExecutePanel(CellPanel panel, int which) {
	this.equalWidths = true;
	/** */
	boolean first = !reverse;

	if ((which & (FormBase.YESB | FormBase.NOB)) == (FormBase.YESB | FormBase.NOB))
	    if ((which & (FormBase.CANCELB | FormBase.DEFCANCELB)) != 0)
		if (!first) {
		    cancelButton = addButton(FormBase.DEFCANCELB | FormBase.CANCELB, IKeys.ESCAPE);
		    refuseButton = addButton(FormBase.NOB, 'n');
		    applyButton = addButton(FormBase.YESB, IKeys.ENTER);
		}
	for (int i = 0; i < 2; i++) {
	    if (first) {
		if (applyButton == null && (which & FormBase.OKB) != 0)
		    applyButton = addButton(FormBase.OKB, 'o');
		if (applyButton == null && (which & FormBase.DEFOKB) != 0)
		    applyButton = addButton(FormBase.DEFOKB, IKeys.ENTER);
		if (applyButton == null && (which & FormBase.YESB) != 0)
		    applyButton = addButton(FormBase.YESB, reverse ? IKeys.ENTER : 'y');
	    } else {
		if (refuseButton == null && (which & FormBase.NOB) != 0)
		    refuseButton = addButton(FormBase.NOB, reverse ? IKeys.ESCAPE : 'n');
		if (cancelButton == null && (which & FormBase.CANCELB) != 0)
		    cancelButton = addButton(FormBase.CANCELB, 'c');
		if (cancelButton == null && (which & FormBase.DEFCANCELB) != 0)
		    cancelButton = addButton(FormBase.DEFCANCELB, IKeys.ESCAPE);
	    }
	    first = !first;
	}
	// if ((which & FormBase.CANCELB) == 0)
	// if (backButton == null) backButton = addButton(which,FormBase.BACKB,"<< Back",null,'b');
	/* */

	panel.addLast(this, DONTSTRETCH, FILL);
	flags = 0;
    }

    public ExecutePanel(CellPanel panel, String leftText, String middleText, String rightText, String leftIcon, String middleIcon, String rightIcon) {
	this.equalWidths = true;
	if (leftText != null) {
	    leftButton = GuiImageBroker.getButton(leftText, leftIcon);
	    this.addNext(leftButton);
	}
	if (middleText != null) {
	    middleButton = GuiImageBroker.getButton(middleText, middleIcon);
	    this.addNext(middleButton);
	}
	if (leftText != null) {
	    rightButton = GuiImageBroker.getButton(rightText, rightIcon);
	    this.addLast(rightButton);
	}
	panel.addLast(this, DONTSTRETCH, FILL);

    }

    private mButton addButton(int which, int iKey) {
	mButton btn = null;
	if ((which & FormBase.YESB) != 0)
	    btn = GuiImageBroker.getButton(MyLocale.getMsg(640, "Yes"), "ok");
	if ((which & FormBase.NOB) != 0)
	    btn = GuiImageBroker.getButton(MyLocale.getMsg(641, "No"), "no");
	if ((which & FormBase.CANCELB) != 0)
	    btn = GuiImageBroker.getButton(MyLocale.getMsg(614, "Cancel"), "cancel");
	if ((which & FormBase.OKB) != 0 || (which & FormBase.DEFOKB) != 0)
	    btn = GuiImageBroker.getButton(MyLocale.getMsg(1605, "OK"), "ok");
	btn.setHotKey(0, iKey);
	this.addNext(btn);
	return btn;
    }

    public void enable(int button) {
	if ((flags & button & LEFT) != 0) {
	    leftButton.set(ControlConstants.Disabled, false);
	}
	if ((flags & button & MIDDLE) != 0) {
	    middleButton.set(ControlConstants.Disabled, false);
	}
	if ((flags & button & RIGHT) == 0) {
	    rightButton.set(ControlConstants.Disabled, false);
	}
	repaint();
    }

    public void disable(int button) {
	if ((flags & button & LEFT) != 0)
	    leftButton.set(ControlConstants.Invisible, false);
	leftButton.set(ControlConstants.Disabled, true);
	if ((flags & button & MIDDLE) != 0)
	    middleButton.set(ControlConstants.Invisible, false);
	middleButton.set(ControlConstants.Disabled, true);
	if ((flags & button & RIGHT) == 0)
	    rightButton.set(ControlConstants.Invisible, false);
	rightButton.set(ControlConstants.Disabled, true);
	repaint();
    }

    public void show(int button) {
	if ((flags & button & LEFT) != 0)
	    leftButton.set(ControlConstants.Invisible, false);
	if ((flags & button & MIDDLE) != 0)
	    middleButton.set(ControlConstants.Invisible, false);
	if ((flags & button & RIGHT) == 0)
	    rightButton.set(ControlConstants.Invisible, false);
	repaint();
    }

    public void hide(int button) {
	if ((flags & button & LEFT) != 0)
	    leftButton.set(ControlConstants.Invisible, true);
	if ((flags & button & MIDDLE) != 0)
	    middleButton.set(ControlConstants.Invisible, true);
	if ((flags & button & RIGHT) == 0)
	    rightButton.set(ControlConstants.Invisible, true);
	repaint();
    }

}
