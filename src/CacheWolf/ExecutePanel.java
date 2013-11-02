package CacheWolf;

import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.IKeys;
import ewe.ui.mButton;

public class ExecutePanel extends CellPanel {
	public final static int LEFT = 1, MIDDLE = 2, RIGHT = 4, CANCEL = LEFT, APPLY = RIGHT; // flags
	public mButton leftButton, middleButton, rightButton;
	public mButton cancelButton, applyButton;
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
