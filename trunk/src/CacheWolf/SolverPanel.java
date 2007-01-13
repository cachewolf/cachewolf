package CacheWolf;


import ewe.ui.*;
import ewe.io.*;
import ewe.filechooser.FileChooser;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;

/**
* Class to create the solver panel. Calls the parser and tokenizer and handles
*	the parser results.
*	@see Parser
*	@see Tokenizer
*/
public class SolverPanel extends CellPanel{
	mButton mBtSolve;
	mButton btnLoad, btnSave, btnSaveAs;
	mTextPad mText;
	Preferences pref;
	Profile profile;
	String currFile;
	CacheHolder currCh;
	Tokenizer tokenizer = new Tokenizer();
	Parser parser = new Parser();
	Vector msgFIFO = new Vector();
	
	
	public SolverPanel (Preferences p, Profile prof){
		pref = p;
		profile = prof;
		ScrollBarPanel sbp = new ScrollBarPanel(mText = new mTextPad());
		this.addLast(sbp);
		this.addNext(mBtSolve= new mButton("Solve!"),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		this.addNext(btnLoad= new mButton("Load"),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		this.addNext(btnSave= new mButton("Save"),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		this.addLast(btnSaveAs= new mButton("SaveAs"),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
	}
	
	public void setCh(CacheHolder ch) {
		currCh=ch;
	}

	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if(ev.target == mBtSolve){
				msgFIFO.clear();
				String src = mText.getText()+"\n";
				tokenizer.tokenizeSource(src, msgFIFO); // Tokenizer sets message if an error occurred
				if (msgFIFO.size()==0) parser.parse(tokenizer.TokenStack, msgFIFO);
				String msgStr = new String();
				for(int i = 0; i < msgFIFO.size(); i++){
					msgStr = msgStr + "# "+msgFIFO.get(i) + "\n";
				}
				src = src +"#----Output----------\n"+ msgStr + "#----------------------";
				mText.setText(src);
			}
			if(ev.target == btnLoad){
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
			
		}
	}
}
