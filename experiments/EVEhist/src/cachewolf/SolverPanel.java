package cachewolf;


import eve.ui.*;
import eve.io.*;
import eve.fx.*;
import java.util.*;
import eve.sys.*;
import eve.ui.data.InputBox;
import eve.ui.event.ControlEvent;
import eve.ui.event.DataChangeEvent;

/**
* Class to create the solver panel. Calls the parser and tokeniser and handles
*	the parser results.
*	@see Parser
*	@see Tokenizer
*/
public class SolverPanel extends CellPanel{
	public TextPad mText; // Accessed by Parser error
	private Button mBtSolve;
	//private Button btnLoad, btnSave, btnSaveAs;
	private Button btnWolfLang;
	private OutputPanel mOutput;
	private Parser parser = null; // Lazy initialisation to speed up loading
	private Vector msgFIFO = null; // Lazy initialisation to speed up loading
	private Menu mnuContextOutp,mnuContextInp;
	private String originalInstructions="";
	private Button btnDegRad;

	public boolean isDirty() {
		return !originalInstructions.equals(getInstructions());
	}

	public String getInstructions() {
		return mText.getText();
	}
	public void setInstructions(String text) {
		originalInstructions=text;
		mText.setText(text);
		mText.repaint();
	}

	public void clearOutput() {
		mOutput.setText("");
	}

	Panel programPanel, outputPanel;

	private String getSolverDegMode() {
		return Global.getPref().solverDegMode ? "DEG" : "RAD";
	}

	public void showSolverMode() {
		btnDegRad.setText(getSolverDegMode());
		btnDegRad.repaint();
	}

	public SolverPanel (){
		SplittablePanel split = new SplittablePanel(PanelSplitter.VERTICAL);

		programPanel = split.getNextPanel();
		outputPanel = split.getNextPanel();
		split.setSplitter(PanelSplitter.AFTER|PanelSplitter.HIDDEN,PanelSplitter.BEFORE|PanelSplitter.HIDDEN,0);

		programPanel.addLast(new MyScrollBarPanel(mText = new InputPanel())).setTag(TAG_SPAN, new Dimension(2,1));
		Panel pnlStatButtons=new Panel();
		pnlStatButtons.addNext(btnDegRad=new Button(getSolverDegMode()),CellConstants.DONTSTRETCH,CellConstants.DONTFILL);
		btnDegRad.backGround=Color.Sand;
		btnDegRad.borderStyle=btnDegRad.borderWidth=0;
		Panel pnlButtons=new Panel();
		pnlButtons.addNext(mBtSolve= new Button(MyLocale.getMsg(1735,"Solve!")),CellConstants.HSTRETCH, CellConstants.HFILL);
		pnlButtons.addLast(btnWolfLang= new Button(MyLocale.getMsg(118,"WolfLanguage")),CellConstants.HSTRETCH, CellConstants.HFILL);
		pnlButtons.equalWidths=true;
		pnlStatButtons.addLast(pnlButtons,CellConstants.HSTRETCH,CellConstants.HFILL);
		programPanel.addLast(pnlStatButtons,HSTRETCH,HFILL);
		/*programPanel.addNext(btnLoad= new Button(MyLocale.getMsg(1736,"Load")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		programPanel.addNext(btnSave= new Button(MyLocale.getMsg(1737,"Save")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		programPanel.addLast(btnSaveAs= new Button(MyLocale.getMsg(1738,"SaveAs")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		*/
		outputPanel.addLast(new MyScrollBarPanel(mOutput = new OutputPanel()));
		this.addLast(split);
	}

	private void execDirectCommand() {
		InpScreen boxInp=new InpScreen(MyLocale.getMsg(1733,"Input command"));
		boxInp.input(parent.getFrame(),"",100); //,MyLocale.getScreenWidth()*4/5);
		String s=boxInp.getInput();
		if (s.equals("")) return;
		processCommand(s);
	}

    private void processCommand(String s) {
		if (parser==null) {
			parser=new Parser(); // Lazy initialisation
			msgFIFO=new Vector();
		} else
			msgFIFO.clear();
		parser.parse(s, msgFIFO);
		String msgStr = "";
		for(int i = 0; i < msgFIFO.size(); i++){
			msgStr = msgStr + msgFIFO.get(i) + "\n";
		}
		mOutput.appendText(msgStr,true);
    }

	public void onEvent(Event ev){
		if (ev instanceof DataChangeEvent) Global.mainTab.cacheDirty=true;
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if(ev.target == mBtSolve){
				processCommand(mText.getText());
			}
			if (ev.target==btnWolfLang) {
				InfoHtmlScreen is = new InfoHtmlScreen(File.getProgramDirectory() + "/" + "wolflang.html", MyLocale.getMsg(118,"WolfLanguage"), true);
				is.execute(parent.getFrame(), Gui.CENTER_FRAME);
			}
			if (ev.target==btnDegRad) {
				Global.getPref().solverDegMode=!Global.getPref().solverDegMode;
				btnDegRad.setText(getSolverDegMode());
			}
		}
	}

//############################################################################
//  InputScreen
//############################################################################

	private class InpScreen extends InputBox {
		InpScreen(String title) {super(title); }
		String getInput() { return getInputValue();}
	}

//############################################################################
//  InputPanel
//############################################################################

	private class InputPanel extends TextPad {
		MenuItem mnuCopyAndComment;
		String text2copy;

		InputPanel() {
			mnuContextInp=getClipboardMenu(new Menu(new MenuItem[]{ mnuCopyAndComment=new MenuItem(MyLocale.getMsg(1740,"Copy as comment")) },""));
		}
		public void penRightReleased(Point p){
			showMenu(p);
		}
		public void penHeld(Point p){
			showMenu(p);
		}

		private void showMenu(Point p) {
			text2copy=eve.sys.Vm.getClipboardText();
			// Disable the copy&comment menu item if clipboard is empty
			if (text2copy==null || text2copy.length()==0)
				((MenuItem)mnuContextInp.items.elementAt(0)).modifiers|=MenuItem.MODIFIER_DISABLED;
			else
				((MenuItem)mnuContextInp.items.elementAt(0)).modifiers&=~MenuItem.MODIFIER_DISABLED;
			setMenu(mnuContextInp);
			doShowMenu(p);
		}
		public void popupMenuEvent(Object selectedItem){
			if (selectedItem==mnuCopyAndComment)
				copyAndComment();
			else
				super.popupMenuEvent(selectedItem);
		}

		public void  penDoubleClicked(Point where) {
			execDirectCommand();
		}

		/** Take the clipboard, prefix each line with a # and insert it
		 * into the input panel at the cursor. Thus descriptions can easily be
		 * copied from the description to the solver.
		 */
		private void copyAndComment() {
			StringBuffer sb=new StringBuffer(text2copy.length()*3/2);
			sb.append('#');
			for (int i=0; i<text2copy.length(); i++) {
				sb.append(text2copy.charAt(i));
				if (text2copy.charAt(i)==0x0A) sb.append('#');
			}
			clipboardTransfer(sb.toString(),false,false);
		}
	}

//############################################################################
//  OutputPanel
//############################################################################
	private class OutputPanel extends TextPad {
		MenuItem mnuClr;
		OutputPanel() {
			this.modify(Control.NotEditable,0);
			//this.modifiers=this.modifiers|WantHoldDown;
			mnuContextOutp=getClipboardMenu(new Menu(new MenuItem[]{ mnuClr=new MenuItem(MyLocale.getMsg(1734,"Clear output")) },""));
		}
		public void penRightReleased(Point p){
			setMenu(mnuContextOutp);
			doShowMenu(p); // direct call (not through doMenu) is neccesary because it will exclude the whole table
		}
		public void penHeld(Point p){
			setMenu(mnuContextOutp);
			doShowMenu(p);
		}
		public void popupMenuEvent(Object selectedItem){
			if (selectedItem==mnuClr)
				this.setText("");
			else
				super.popupMenuEvent(selectedItem);
		}
	}


}
