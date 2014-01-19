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
package CacheWolf;

import CacheWolf.controls.InfoScreen;
import CacheWolf.controls.MyScrollBarPanel;
import CacheWolf.database.CacheHolder;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Point;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.InputBox;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.PanelSplitter;
import ewe.ui.SplittablePanel;
import ewe.ui.mButton;
import ewe.ui.mTextPad;
import ewe.util.Vector;

/**
 * Class to create the solver panel.
 * Calls the parser and tokeniser and handles the parser results.
 * 
 * @see Parser
 * @see Tokenizer
 */
public class SolverPanel extends CellPanel {
    private mButton mBtSolve;
    private mButton btnWolfLang;
    private mTextPad mText;
    private OutputPanel mOutput;
    private Tokenizer tokeniser = new Tokenizer();
    private Parser parser = new Parser();
    private Vector msgFIFO = new Vector();
    private Menu mnuContext;
    private String originalInstructions = "";
    private mButton btnDegRad;
    private CacheHolder ch;

    public SolverPanel() {
	CellPanel pnlStatButtons = new CellPanel();
	pnlStatButtons.addNext(btnDegRad = GuiImageBroker.getButton("DEG", "degrad"), CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
	showSolverMode();
	btnDegRad.backGround = Color.Sand;
	// btnDegRad.borderStyle = btnDegRad.borderWidth = 0;
	CellPanel pnlButtons = new CellPanel();
	pnlButtons.addNext(mBtSolve = GuiImageBroker.getButton(MyLocale.getMsg(1735, "Solve!"), "solver"), CellConstants.HSTRETCH, CellConstants.HFILL);
	pnlButtons.addLast(btnWolfLang = GuiImageBroker.getButton(MyLocale.getMsg(118, "WolfLanguage"), "wolflanguage"), CellConstants.HSTRETCH, CellConstants.HFILL);
	pnlButtons.equalWidths = true;
	pnlStatButtons.addLast(pnlButtons, CellConstants.HSTRETCH, CellConstants.HFILL);

	if (!Preferences.itself().tabsAtTop)
	    this.addLast(pnlStatButtons, HSTRETCH, HFILL).setTag(SPAN, new Dimension(3, 1));

	SplittablePanel split = new SplittablePanel(PanelSplitter.VERTICAL);
	MyLocale.setSplitterSize(split);

	programPanel = split.getNextPanel();
	outputPanel = split.getNextPanel();
	split.setSplitter(PanelSplitter.AFTER | PanelSplitter.HIDDEN, PanelSplitter.BEFORE | PanelSplitter.HIDDEN, 0);

	programPanel.addLast(new MyScrollBarPanel(mText = new InputPanel())).setTag(SPAN, new Dimension(2, 1));

	outputPanel.addLast(new MyScrollBarPanel(mOutput = new OutputPanel()));

	this.addLast(split);

	if (Preferences.itself().tabsAtTop)
	    this.addLast(pnlStatButtons, HSTRETCH, HFILL).setTag(SPAN, new Dimension(3, 1));

    }

    public boolean isDirty() {
	return !originalInstructions.equals(getInstructions());
    }

    public String getInstructions() {
	return mText.getText();
    }

    /**
     * Sets the instructions in the solver panel. The last remembered cache of the solver panel
     * is not changed.
     * 
     * @param text
     *            The instructions to set.
     */
    public void setInstructions(String text) {
	originalInstructions = text;
	mText.setText(text);
	mText.repaint();
    }

    public void setText(String what) {
	mText.setText(what);
    }

    public void appendText(String what, boolean moveToEnd) {
	mText.appendText(what, moveToEnd);
    }

    public void setSelectionRange(int startChar, int startLine, int endChar, int endLine) {
	if (MainTab.itself.solverPanel.mText.setSelectionRange(startChar, startLine, endChar, endLine))
	    MainTab.itself.solverPanel.mText.repaintNow();
    }

    public void cls() {
	mOutput.setText("");
    }

    /**
     * Sets the instructions of the solver panel to the solver code in the given cache.
     * The current cache is remembered. If the instructions are set for an other cache,
     * then the output panel is cleared if the cache objects do not belong to each other.
     * 
     * @param pCh
     *            The cache who's solver code is used
     */
    public void setInstructions(CacheHolder pCh) {
	if (pCh != null) {
	    this.setInstructions(pCh.getCacheDetails(false).getSolver());
	    if (!pCh.hasSameMainCache(ch)) {
		this.clearOutput();
	    }
	} else {
	    this.setInstructions("");
	}
	ch = pCh;
    }

    private class OutputPanel extends mTextPad {
	MenuItem mnuClr;

	OutputPanel() {
	    this.modify(ControlConstants.NotEditable, 0);
	    //this.modifiers=this.modifiers|WantHoldDown; 
	    setMenu(mnuContext = getClipboardMenu(new Menu(new MenuItem[] { mnuClr = new MenuItem(MyLocale.getMsg(1734, "Clear output")) }, "")));
	}

	public void penRightReleased(Point p) {
	    setMenu(mnuContext);
	    doShowMenu(p); // direct call (not through doMenu) is neccesary because it will exclude the whole table
	}

	public void penHeld(Point p) {
	    setMenu(mnuContext);
	    doShowMenu(p);
	}

	public void popupMenuEvent(Object selectedItem) {
	    if (selectedItem == mnuClr)
		this.setText("");
	    else
		super.popupMenuEvent(selectedItem);
	}
    }

    private class InputPanel extends mTextPad {

	public void penDoubleClicked(Point where) {
	    execDirectCommand();
	}
    }

    private class InpScreen extends InputBox {
	InpScreen(String title) {
	    super(title);
	}

	String getInput() {
	    return getInputValue();
	}
    }

    CellPanel programPanel, outputPanel;

    public void showSolverMode() {
	GuiImageBroker.setButtonText(btnDegRad, Preferences.itself().solverDegMode ? "DEG" : "RAD");
    }

    private void execDirectCommand() {
	InpScreen boxInp = new InpScreen(MyLocale.getMsg(1733, "Input command"));
	boxInp.input(parent.getFrame(), "", 100); //,MyLocale.getScreenWidth()*4/5);
	String s = boxInp.getInput();
	if (s.equals("") || (boxInp.exitValue == FormBase.IDCANCEL))
	    return;
	processCommand(s);
    }

    private void processCommand(String s) {
	msgFIFO.clear();
	tokeniser.tokenizeSource(s, msgFIFO); // Tokeniser sets message if an error occurred
	if (msgFIFO.size() == 0)
	    parser.parse(tokeniser.TokenStack, msgFIFO);
	String msgStr = "";
	for (int i = 0; i < msgFIFO.size(); i++) {
	    msgStr = msgStr + msgFIFO.get(i) + "\n";
	}
	mOutput.appendText(msgStr, true);
    }

    /**
     * Clears the output panel
     */
    void clearOutput() {
	mOutput.setText("");
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    if (ev.target == mBtSolve) {
		saveChanges();
		processCommand(mText.getText());
	    }
	    if (ev.target == btnWolfLang) {
		InfoScreen is = new InfoScreen(MyLocale.getLocalizedFile("wolflang.html"), MyLocale.getMsg(118, "WolfLanguage"), true);
		is.execute(parent.getFrame(), Gui.CENTER_FRAME);
	    }
	    if (ev.target == btnDegRad) {
		Preferences.itself().solverDegMode = !Preferences.itself().solverDegMode;
		showSolverMode();
	    }
	}
    }

    /**
     * Saves solver content to file if it is changed. Saving is always done in the main
     * cache (if it is an addi waypoint).
     */
    private void saveChanges() {
	if (isDirty()) {
	    CacheHolder cacheToUpdate;
	    if (ch.mainCache == null) {
		cacheToUpdate = ch;
	    } else {
		cacheToUpdate = ch.mainCache;
	    }
	    boolean oldHasSolver = cacheToUpdate.hasSolver();
	    cacheToUpdate.getCacheDetails(false).setSolver(getInstructions());
	    if (oldHasSolver != cacheToUpdate.hasSolver()) {
		MainTab.itself.tablePanel.myTableControl.update(true);
	    }
	    cacheToUpdate.save();
	    originalInstructions = getInstructions();
	}
    }

}
