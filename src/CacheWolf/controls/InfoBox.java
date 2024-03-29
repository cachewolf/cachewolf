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

import CacheWolf.Preferences;
import CacheWolf.utils.MyLocale;
import ewe.fx.Dimension;
import ewe.ui.*;

public class InfoBox extends Form {

    public final static int CHECKBOX = 1;
    public final static int INPUT = 2;
    public final static int DISPLAY_ONLY = 3;
    public final static int PROGRESS_WITH_WARNINGS = 4;

    private TextMessage msgArea;
    private TextMessage warnings;
    private mCheckBox checkBox;
    private mInput feedback = new mInput();
    private ExecutePanel executePanel;

    private int type = 0;

    private boolean isClosed = false;

    public InfoBox(String title, String info) {
        this(title, info, DISPLAY_ONLY);
    }

    public InfoBox(String title, String info, int type) {
        this(title, info, type, true);
    }

    public InfoBox(String title, Control ctrl, int w, int h) {
        this.setPreferredSize(w, h);
        this.addLast(new MyScrollBarPanel((ScrollClient) ctrl, ScrollablePanel.NeverShowHorizontalScrollers), STRETCH, FILL);
        this.title = title;
        this.type = DISPLAY_ONLY;
        // relayout(false);
    }

    public InfoBox(String title, String info, int type, boolean autoWrap) {

        Preferences.itself().setSubWindowSize(this);

        switch (type) {
            case CHECKBOX:
                checkBox = new mCheckBox(info);
                this.addLast(checkBox, STRETCH, FILL);
                executePanel = new ExecutePanel(this);
                break;
            case INPUT:
                mLabel mL = new mLabel(info);
                this.addLast(mL, STRETCH, FILL);
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
                warnings = new TextMessage("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n");
                warnings.autoWrap = autoWrap;
                lowerPanel.addLast(warnings.getScrollablePanel());
                this.addLast(splittablePanel);
                executePanel = new ExecutePanel(this, FormBase.CANCELB);
		Dimension pf = (Dimension) getTag(PREFERREDSIZE, new Dimension (10,10));
		upperPanel.setMinimumSize(pf.width, Math.min(50, pf.height / 3));
                lowerPanel.setMinimumSize(pf.width, Math.min(100, pf.height / 3));
                break;
        }
        this.title = title;
        this.type = type;
        //relayout(false);
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

    /**
     * The user can close the Info window to abort operation
     * Use this to check.
     */
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