package CacheWolf.controls;

import CacheWolf.GuiImageBroker;
import CacheWolf.MyLocale;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.Window;
import ewe.ui.mButton;

public class ExecutePanel extends CellPanel {

    /** Cancel buttons should come before YES/OK buttons. I changed the meaning: first cancel, then ok */
    private static boolean reverse = (Gui.getGuiFlags() & Window.GUI_FLAG_REVERSE_OK_CANCEL) == 0;
    public mButton cancelButton, refuseButton, applyButton;
    private int flags; // bit for a used button FormBase.YESB, FormBase.NOB, FormBase.CANCELB

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
	flags = FormBase.CANCELB | FormBase.YESB;
    }

    public ExecutePanel(CellPanel panel, int which) {
	this.equalWidths = true;
	/** */
	boolean first = !reverse;
	flags = 0;
	if ((which & (FormBase.YESB | FormBase.NOB)) == (FormBase.YESB | FormBase.NOB))
	    if ((which & (FormBase.CANCELB | FormBase.DEFCANCELB)) != 0)
		if (!first) {
		    cancelButton = addButton(FormBase.DEFCANCELB | FormBase.CANCELB, IKeys.ESCAPE);
		    refuseButton = addButton(FormBase.NOB, 'n');
		    applyButton = addButton(FormBase.YESB, IKeys.ENTER);
		    flags = FormBase.CANCELB | FormBase.YESB | FormBase.NOB;
		}
	for (int i = 0; i < 2; i++) {
	    if (first) {
		if (applyButton == null && (which & FormBase.OKB) != 0) {
		    applyButton = addButton(FormBase.OKB, 'o');
		    flags = flags | FormBase.YESB;
		}
		if (applyButton == null && (which & FormBase.DEFOKB) != 0) {
		    applyButton = addButton(FormBase.DEFOKB, IKeys.ENTER);
		    flags = flags | FormBase.YESB;
		}
		if (applyButton == null && (which & FormBase.YESB) != 0) {
		    applyButton = addButton(FormBase.YESB, reverse ? IKeys.ENTER : 'y');
		    flags = flags | FormBase.YESB;
		}
	    } else {
		if (refuseButton == null && (which & FormBase.NOB) != 0) {
		    refuseButton = addButton(FormBase.NOB, reverse ? IKeys.ESCAPE : 'n');
		    flags = flags | FormBase.NOB;
		}
		if (cancelButton == null && (which & FormBase.CANCELB) != 0) {
		    cancelButton = addButton(FormBase.CANCELB, 'c');
		    flags = flags | FormBase.CANCELB;
		}
		if (cancelButton == null && (which & FormBase.DEFCANCELB) != 0) {
		    cancelButton = addButton(FormBase.DEFCANCELB, IKeys.ESCAPE);
		    flags = flags | FormBase.CANCELB;
		}
	    }
	    first = !first;
	}

	panel.addLast(this, DONTSTRETCH, FILL);
    }

    private mButton addButton(int which, int iKey) {
	mButton btn = null;
	if ((which & FormBase.YESB) != 0)
	    btn = GuiImageBroker.getButton(MyLocale.getMsg(640, "Yes"), "ok");
	if ((which & FormBase.NOB) != 0)
	    btn = GuiImageBroker.getButton(MyLocale.getMsg(641, "No"), "no");
	if ((which & FormBase.CANCELB) != 0 || (which & FormBase.DEFCANCELB) != 0)
	    btn = GuiImageBroker.getButton(MyLocale.getMsg(614, "Cancel"), "cancel");
	if ((which & FormBase.OKB) != 0 || (which & FormBase.DEFOKB) != 0)
	    btn = GuiImageBroker.getButton(MyLocale.getMsg(1605, "OK"), "ok");
	btn.setHotKey(0, iKey);
	this.addNext(btn);
	return btn;
    }

    public void enable(int button) {
	if ((flags & button & FormBase.CANCELB) != 0) {
	    cancelButton.set(ControlConstants.Disabled, false);
	}
	if ((flags & button & FormBase.NOB) != 0) {
	    refuseButton.set(ControlConstants.Disabled, false);
	}
	if ((flags & button & FormBase.YESB) != 0) {
	    applyButton.set(ControlConstants.Disabled, false);
	}
	repaint();
    }

    public void disable(int button) {
	if ((flags & button & FormBase.CANCELB) != 0) {
	    cancelButton.set(ControlConstants.Invisible, false);
	    cancelButton.set(ControlConstants.Disabled, true);
	}
	if ((flags & button & FormBase.NOB) != 0) {
	    refuseButton.set(ControlConstants.Invisible, false);
	    refuseButton.set(ControlConstants.Disabled, true);
	}
	if ((flags & button & FormBase.YESB) != 0) {
	    applyButton.set(ControlConstants.Invisible, false);
	    applyButton.set(ControlConstants.Disabled, true);
	}
	repaint();
    }

    public void show(int button) {
	if ((flags & button & FormBase.CANCELB) != 0)
	    cancelButton.set(ControlConstants.Invisible, false);
	if ((flags & button & FormBase.NOB) != 0)
	    refuseButton.set(ControlConstants.Invisible, false);
	if ((flags & button & FormBase.YESB) != 0)
	    applyButton.set(ControlConstants.Invisible, false);
	repaint();
    }

    public void hide(int button) {
	if ((flags & button & FormBase.CANCELB) != 0)
	    cancelButton.set(ControlConstants.Invisible, true);
	if ((flags & button & FormBase.NOB) != 0)
	    refuseButton.set(ControlConstants.Invisible, true);
	if ((flags & button & FormBase.YESB) != 0)
	    applyButton.set(ControlConstants.Invisible, true);
	repaint();
    }

    public void setText(String text, int button) {
	if ((flags & button & FormBase.CANCELB) != 0)
	    GuiImageBroker.setButtonText(cancelButton, text);
	if ((flags & button & FormBase.NOB) != 0)
	    GuiImageBroker.setButtonText(refuseButton, text);
	if ((flags & button & FormBase.YESB) != 0)
	    GuiImageBroker.setButtonText(applyButton, text);
    }
}
