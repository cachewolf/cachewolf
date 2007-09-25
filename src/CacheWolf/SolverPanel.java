package CacheWolf;


import ewe.ui.*;
import ewe.io.*;
import ewe.filechooser.FileChooser;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;

/**
* Class to create the solver panel. Calls the parser and tokeniser and handles
*	the parser results.
*	@see Parser
*	@see Tokenizer
*/
public class SolverPanel extends CellPanel{
	mButton mBtSolve;
	mButton btnLoad, btnSave, btnSaveAs;
	mTextPad mText;
	OutputPanel mOutput;
	Preferences pref;
	Profile profile;
	String currFile;
	Tokenizer tokeniser = new Tokenizer();
	Parser parser = new Parser();
	Vector msgFIFO = new Vector();
	Menu mnuContext;
	private String originalInstructions="";
	
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
	
	
	private class OutputPanel extends mTextPad {
		MenuItem mnuClr;
		OutputPanel() {
			this.modify(Control.NotEditable,0);
			//this.modifiers=this.modifiers|WantHoldDown; 
			setMenu(mnuContext=getClipboardMenu(new Menu(new MenuItem[]{ mnuClr=new MenuItem(MyLocale.getMsg(1734,"Clear output")) },"")));
		} 
		public void penRightReleased(Point p){
			setMenu(mnuContext);
			doShowMenu(p); // direct call (not through doMenu) is neccesary because it will exclude the whole table
		}
		public void penHeld(Point p){
			setMenu(mnuContext);
			doShowMenu(p);
		}
		public void popupMenuEvent(Object selectedItem){
			if (selectedItem==mnuClr) 
				this.setText("");
			else 
				super.popupMenuEvent(selectedItem);
		}
	}
	private class InputPanel extends mTextPad {

		public void  penDoubleClicked(Point where) {
			execDirectCommand();
		}
	}
	private class InpScreen extends InputBox {
		InpScreen(String title) {super(title); }
		String getInput() { return getInputValue();}
	}
	CellPanel programPanel, outputPanel;
	
	public SolverPanel (Preferences p, Profile prof){
		pref = p;
		profile = prof;
		SplittablePanel split = new SplittablePanel(PanelSplitter.VERTICAL);

		programPanel = split.getNextPanel();
		outputPanel = split.getNextPanel();
		split.setSplitter(PanelSplitter.AFTER|PanelSplitter.HIDDEN,PanelSplitter.BEFORE|PanelSplitter.HIDDEN,0);

		programPanel.addLast(new ScrollBarPanel(mText = new InputPanel()));
		programPanel.addNext(mBtSolve= new mButton(MyLocale.getMsg(1735,"Solve!")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		/*programPanel.addNext(btnLoad= new mButton(MyLocale.getMsg(1736,"Load")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		programPanel.addNext(btnSave= new mButton(MyLocale.getMsg(1737,"Save")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		programPanel.addLast(btnSaveAs= new mButton(MyLocale.getMsg(1738,"SaveAs")),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		*/
		outputPanel.addLast(new ScrollBarPanel(mOutput = new OutputPanel()));

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
		msgFIFO.clear();
		tokeniser.tokenizeSource(s, msgFIFO); // Tokeniser sets message if an error occurred
		if (msgFIFO.size()==0) parser.parse(tokeniser.TokenStack, msgFIFO);
		String msgStr = "";
		for(int i = 0; i < msgFIFO.size(); i++){
			msgStr = msgStr + msgFIFO.get(i) + "\n";
		}
		mOutput.appendText(msgStr,true);
    }
	
	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if(ev.target == mBtSolve){
				processCommand(mText.getText());
			}
/*			if(ev.target == btnLoad){
				FileChooser fc = new FileChooser(FileChooser.OPEN, profile.dataDir);
				
				fc.addMask(currCh.wayPoint + ".wl");
				fc.addMask("*.wl");
				fc.setTitle("Select File");
				if(fc.execute() != FileChooser.IDCANCEL){
					currFile = fc.getChosen();
					mText.setText("");
					try {
						InputStreamReader inp = new InputStreamReader( new FileInputStream(currFile));
						mText.setText(inp.readAll());
						inp.close();

					} catch (Exception e) {
						Vm.debug("Error reading file " + e.toString());
					}
				}
			}
			if((ev.target == btnSave) && (currFile != null)){
				try {
					OutputStreamWriter outp = new OutputStreamWriter( new FileOutputStream(currFile));
					outp.write(mText.getText());
					outp.close();
				} catch (Exception e) {
					Vm.debug("Error writing file ");
				}
			}
			if((ev.target == btnSaveAs)||((ev.target == btnSave) && (currFile == null))){
				FileChooser fc = new FileChooser(FileChooser.SAVE, profile.dataDir);
				fc.addMask(currCh.wayPoint + ".wl");
				fc.addMask("*.wl");
				fc.setTitle("Select File");
				if(fc.execute() != FileChooser.IDCANCEL){
					File saveFile = fc.getChosenFile();
					currFile = fc.getChosen();
					try {
						OutputStreamWriter outp = new OutputStreamWriter( new FileOutputStream(saveFile));
						outp.write(mText.getText());
						outp.close();
					} catch (Exception e) {
						Vm.debug("Error writing file ");
					}
				}
			}
*/			
		}
	}
	
}
