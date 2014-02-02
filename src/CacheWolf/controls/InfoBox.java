/*
GNU General Public License
CacheWolf is a software for PocketPC, Win and Linux that
enables paperless caching.
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://www.cachewolf.de/ for more information.
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package CacheWolf.controls;

import CacheWolf.utils.MyLocale;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.PanelSplitter;
import ewe.ui.SplittablePanel;
import ewe.ui.TextMessage;
import ewe.ui.mCheckBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;

public class InfoBox extends Form {

    public final static int CHECKBOX = 1;
    public final static int INPUT = 2;
    public final static int DISPLAY_ONLY = 3;
    public final static int PROGRESS_WITH_WARNINGS = 4;

    private static int preferredWidth;
    private static int preferredHeight;

    private TextMessage msgArea;
    private TextMessage warnings;
    private mCheckBox checkBox;
    private mInput feedback = new mInput();
    private ExecutePanel executePanel;

    private int type = 0;

    /**
     * This variable is set to true (by canExit()), if the user closed the Info window by clicking the "close" button.
     * It can be used to check if a lengthy task needs to be aborted (i.e. spidering)
     */
    private boolean isClosed = false;

    public static void init(int fontSize, boolean useBigIcons) {
	// InfoBox Size
	int psx = fontSize * 16;
	int psy = fontSize * 12;
	if (useBigIcons) {
	    psx = Math.min(psx + 48, MyLocale.getScreenWidth());
	    psy = Math.min(psy + 16, MyLocale.getScreenHeight());
	} else {
	    psx = Math.min(psx, MyLocale.getScreenWidth());
	    psy = Math.min(psy, MyLocale.getScreenHeight());
	}
	preferredWidth = psx;
	preferredHeight = psy;
    }

    public InfoBox(String title, String info) {
	this(title, info, DISPLAY_ONLY);
    }

    public InfoBox(String title, String info, int type) {
	this(title, info, type, true);
    }

    public InfoBox(String title, String info, int type, boolean autoWrap) {

	if (preferredWidth > 0 && preferredHeight > 0)
	    this.setPreferredSize(preferredWidth, preferredHeight);

	switch (type) {
	case CHECKBOX:
	    checkBox = new mCheckBox(info);
	    this.addLast(checkBox, STRETCH, FILL);
	    executePanel = new ExecutePanel(this);
	    break;
	case INPUT:
	    mLabel mL = new mLabel(info);
	    this.addNext(mL, STRETCH, FILL);
	    this.addLast(feedback, STRETCH, FILL);
	    executePanel = new ExecutePanel(this);
	    break;
	case DISPLAY_ONLY:
	    msgArea = new TextMessage(info);
	    msgArea.autoWrap = autoWrap;
	    msgArea.alignment = CENTER;
	    msgArea.anchor = CENTER;
	    this.addLast(msgArea.getScrollablePanel(), STRETCH, FILL);
	    break;
	case PROGRESS_WITH_WARNINGS:
	    SplittablePanel splittablePanel = new SplittablePanel(PanelSplitter.VERTICAL);
	    splittablePanel.theSplitter.thickness = 8;
	    CellPanel upperPanel = splittablePanel.getNextPanel();
	    CellPanel lowerPanel = splittablePanel.getNextPanel();
	    splittablePanel.setSplitter(PanelSplitter.AFTER | PanelSplitter.MIN_SIZE, PanelSplitter.BEFORE | PanelSplitter.MIN_SIZE, PanelSplitter.OPENED);
	    MyLocale.setSplitterSize(splittablePanel);
	    msgArea = new TextMessage("");
	    msgArea.autoWrap = autoWrap;
	    msgArea.alignment = CENTER;
	    msgArea.anchor = CENTER;
	    upperPanel.addLast(msgArea.getScrollablePanel());
	    warnings = new TextMessage("");
	    warnings.autoWrap = autoWrap;
	    lowerPanel.addLast(warnings.getScrollablePanel());
	    this.addLast(splittablePanel);
	    executePanel = new ExecutePanel(this, FormBase.CANCELB);
	    upperPanel.setMinimumSize(preferredWidth, preferredHeight / 4);
	    lowerPanel.setMinimumSize(preferredWidth, preferredHeight / 4);
	    break;
	}
	this.title = title;
	this.type = type;
	relayout(false);
    }

    public final int wait(int doButtons)
    //===================================================================
    {
	if (type == DISPLAY_ONLY) {
	    if (executePanel == null)
		executePanel = new ExecutePanel(this, doButtons);
	}
	exec();
	return waitUntilClosed();
    }

    public String getInfo() {
	return msgArea.getText();
    }

    public void setInfo(String info) {
	msgArea.setText(info);
	this.repaintNow();
    }

    public void addInfo(String t) {
	msgArea.setText(t + "\n" + msgArea.text);
	this.repaintNow();
    }

    public String getInput() {
	return feedback.getText();
    }

    public void setInput(String value) {
	feedback.setText(value);
    }

    public void setInputPassword(String value) {
	feedback.setText(value);
	feedback.isPassword = true;
    }

    public void addWarning(String w) {
	warnings.setText(w + "\n" + warnings.text);
    }

    public boolean getCheckBoxState() {
	return checkBox.getState();
    }

    public void setCheckBoxState(boolean to) {
	checkBox.setState(to);
    }

    public void showButton(int button) {
	executePanel.show(button);
    }

    public void hideButton(int button) {
	executePanel.hide(button);
    }

    public void enableButton(int button) {
	executePanel.enable(button);
    }

    public void disableButton(int button) {
	executePanel.disable(button);
    }

    public void setButtonText(String text, int button) {
	executePanel.setText(text, button);
    }

    public boolean isClosed() {
	return isClosed;
    }

    // Overrides
    protected boolean canExit(int exitCode) {
	isClosed = true;
	return true;
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == executePanel.applyButton) {
		isClosed = true;
		this.close(FormBase.IDOK);
	    } else if (ev.target == executePanel.cancelButton) {
		isClosed = true;
		this.close(FormBase.IDCANCEL);
	    } else if (ev.target == executePanel.refuseButton) {
		isClosed = true;
		this.close(FormBase.IDNO);
	    }
	}
	super.onEvent(ev);
    }

}