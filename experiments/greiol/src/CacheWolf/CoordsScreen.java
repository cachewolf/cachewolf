package CacheWolf;

import CacheWolf.navi.Navigate;
import ewe.ui.*;
import ewe.fx.Dimension;
import ewe.sys.*;

/**
*	Class for entering coordinates<br>
*	Class IDs 1400 and 600 (same as calc panel and preferences screen)<br>
*/


public class CoordsScreen extends Form {

	mCheckBox chkDMM, chkDMS, chkDD, chkUTM, chkGK;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	mChoice chcNS, chcEW;
	mInput inpNSDeg, inpNSm, inpNSs, inpEWDeg, inpEWm, inpEWs;
	mInput inpUTMZone, inpUTMNorthing, inpUTMEasting;
	mInput inpText;
	mButton btnCancel, btnApply, btnCopy, btnPaste, btnParse, btnGps, btnClear;
	CWPoint coordInp = new CWPoint();
	CellPanel topLinePanel = new CellPanel();
	CellPanel mainPanel = new CellPanel();
	int exitKeys[]={75009};
	int currFormat;
	
	private boolean allowInvalid = false;
	
	public CoordsScreen(boolean allowInvalidCoords)
	{
		allowInvalid = allowInvalidCoords;
		
		InitCoordsScreen();		
	}
	
	public CoordsScreen()
	{
		InitCoordsScreen();		
	}
	
	private void InitCoordsScreen()
	{
		this.setTitle("");
		//Radiobuttons for format
		topLinePanel.addNext(chkDD =new mCheckBox("d.d°"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		topLinePanel.addNext(chkDMM =new mCheckBox("d°m.m\'"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		topLinePanel.addNext(chkDMS =new mCheckBox("d°m\'s\""),CellConstants.DONTSTRETCH,CellConstants.WEST);
		topLinePanel.addNext(chkUTM =new mCheckBox("UTM"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		topLinePanel.addLast(chkGK =new mCheckBox("GK"),CellConstants.DONTSTRETCH, CellConstants.WEST);

		chkDD.setGroup(chkFormat); chkDD.exitKeys=exitKeys;
		chkDMM.setGroup(chkFormat);chkDMM.exitKeys=exitKeys;
		chkDMS.setGroup(chkFormat);chkDMS.exitKeys=exitKeys;
		chkUTM.setGroup(chkFormat);chkUTM.exitKeys=exitKeys;
		chkGK.setGroup(chkFormat);chkGK.exitKeys=exitKeys;
		
		this.addLast(topLinePanel,CellConstants.DONTSTRETCH, CellConstants.WEST);

		// Input for degrees
		mainPanel.addNext(chcNS = new mChoice(new String[]{"N", "S"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chcNS.setInt(0);
		mainPanel.addNext(inpNSDeg = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addNext(inpNSm = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addLast(inpNSs = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
		mainPanel.addNext(chcEW = new mChoice(new String[]{"E", "W"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chcEW.setInt(0);
		mainPanel.addNext(inpEWDeg = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addNext(inpEWm = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addLast(inpEWs = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		// Input for UTM
		if (allowInvalid){
			mainPanel.addNext(new mLabel(MyLocale.getMsg(1400,"Zone")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.SOUTHWEST));
			mainPanel.addNext(new mLabel(MyLocale.getMsg(1402,"Easting")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.SOUTHWEST));
			mainPanel.addNext(new mLabel(MyLocale.getMsg(1401,"Northing")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.SOUTHWEST));
			mainPanel.addLast(btnClear = new mButton(MyLocale.getMsg(1413,"Clear")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		} else {
			mainPanel.addNext(new mLabel(MyLocale.getMsg(1400,"Zone")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.SOUTHWEST));
			mainPanel.addNext(new mLabel(MyLocale.getMsg(1402,"Easting")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.SOUTHWEST));
			mainPanel.addLast(new mLabel(MyLocale.getMsg(1401,"Northing")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.SOUTHWEST));
		}
		
		mainPanel.addNext(inpUTMZone = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addNext(inpUTMEasting = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addNext(inpUTMNorthing = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addLast(btnGps = new mButton("GPS"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		
		mainPanel.addLast(new mLabel(MyLocale.getMsg(1405,"To load coordinates from GC, enter GCxxxxx below")),CellConstants.HSTRETCH, (CellConstants.HFILL)).setTag(SPAN,new Dimension(4,1));
			// Input for free Text
		mainPanel.addNext(inpText = new mInput(),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		inpText.toolTip=MyLocale.getMsg(1406,"Enter coordinates in any format or GCxxxxx");
		inpText.setTag(SPAN,new Dimension(3,1));
		mainPanel.addLast(btnParse = new mButton(MyLocale.getMsg(619,"Parse")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		
		// Buttons for cancel and apply, copy and paste
		btnCancel = new mButton(MyLocale.getMsg(614,"Cancel"));
		btnCancel.setHotKey(0, IKeys.ESCAPE);
		mainPanel.addNext(btnCancel,CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnCancel.setTag(SPAN,new Dimension(4,1));
		mainPanel.addNext(btnApply = new mButton(MyLocale.getMsg(615,"Apply")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnApply.setTag(SPAN,new Dimension(4,1));
		mainPanel.addNext(btnPaste = new mButton(MyLocale.getMsg(617,"Paste")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnParse.setTag(SPAN,new Dimension(4,1));
		mainPanel.addLast(btnCopy = new mButton(MyLocale.getMsg(618,"Copy")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnCopy.setTag(SPAN,new Dimension(4,1));
		chcNS.exitKeys=exitKeys; chcEW.exitKeys=exitKeys;
		//add Panels
		this.addLast(mainPanel,CellConstants.DONTSTRETCH, CellConstants.WEST);
		chcNS.takeFocus(ControlConstants.ByKeyboard);
	}
	
	public void activateFields(int format){
		inpEWDeg.wantReturn=false; inpEWm.wantReturn=false; inpEWs.wantReturn=false; inpUTMNorthing.wantReturn=false;
		switch (format){
			case CWPoint.DD:
				enable(chcNS); enable(inpNSDeg); disable(inpNSm); disable(inpNSs);
				enable(chcEW); enable(inpEWDeg); disable(inpEWm); disable(inpEWs);
				inpEWDeg.wantReturn=true;
				disable(inpUTMZone); disable(inpUTMNorthing); disable(inpUTMEasting);
				break;
			case CWPoint.CW:
			case CWPoint.DMM: 	
				enable(chcNS); enable(inpNSDeg); enable(inpNSm); disable(inpNSs);
				enable(chcEW); enable(inpEWDeg); enable(inpEWm); disable(inpEWs);
				inpEWm.wantReturn=true;
				disable(inpUTMZone); disable(inpUTMNorthing); disable(inpUTMEasting);
				break;
			case CWPoint.DMS: 	
				enable(chcNS); enable(inpNSDeg); enable(inpNSm); enable(inpNSs);
				enable(chcEW); enable(inpEWDeg); enable(inpEWm); enable(inpEWs);
				inpEWs.wantReturn=true;
				disable(inpUTMZone); disable(inpUTMNorthing); disable(inpUTMEasting);
				break;
			case CWPoint.UTM: 	
				disable(chcNS); disable(inpNSDeg); disable(inpNSm); disable(inpNSs);
				disable(chcEW); disable(inpEWDeg); disable(inpEWm); disable(inpEWs);
				enable(inpUTMZone); enable(inpUTMNorthing); enable(inpUTMEasting);
				inpUTMNorthing.wantReturn=true;
	 			break;
			case CWPoint.GK: 	
				disable(chcNS); disable(inpNSDeg); disable(inpNSm); disable(inpNSs);
				disable(chcEW); disable(inpEWDeg); disable(inpEWm); disable(inpEWs);
				disable(inpUTMZone); enable(inpUTMNorthing); enable(inpUTMEasting);
				inpUTMNorthing.wantReturn=true;
	 			break;
		}
		
		this.stretchLastColumn = true;
		this.stretchLastRow = true;
		this.repaintNow();
	}

	private void enable(Control c) {c.modify(ControlConstants.TakesKeyFocus,ControlConstants.Disabled); }
	private void disable(Control c) {c.modify(ControlConstants.Disabled,ControlConstants.TakesKeyFocus); }
	
	public void readFields(CWPoint coords, int format){
		String NS, EW;
		if (format == CWPoint.UTM)
			coords.set(inpUTMZone.getText(), 
					   inpUTMNorthing.getText(), inpUTMEasting.getText());
		else if (format == CWPoint.GK) {
			coords.set(inpUTMEasting.getText(), inpUTMNorthing.getText());			
		}
		else {
			NS = chcNS.getInt()== 0?"N":"S";
			EW = chcEW.getInt()== 0?"E":"W";
			coords.set(NS, inpNSDeg.getText(), inpNSm.getText(), inpNSs.getText(),
							 EW, inpEWDeg.getText(), inpEWm.getText(), inpEWs.getText(),
							 format);
		}

		return;
	}
	public void setFields(CWPoint coords, int format) {
		if (format == CWPoint.CW) format = CWPoint.DMM;
		if (format == CWPoint.UTM){
			inpUTMZone.setText(coords.getUTMZone());
			inpUTMNorthing.setText(coords.getUTMNorthing());
			inpUTMEasting.setText((coords.getUTMEasting()));
		}
		else if (format == CWPoint.GK){
			inpUTMZone.setText("");
			if (coords.isValid()){
				inpUTMNorthing.setText(coords.getGKNorthing(0));
				inpUTMEasting.setText((coords.getGKEasting(0)));				
			}
			else {
				inpUTMNorthing.setText("0");
				inpUTMEasting.setText("0");
			}
		}
		else {
			chcNS.setInt(coords.getNSLetter().equals("N")?0:1);
			chcEW.setInt(coords.getEWLetter().equals("E")?0:1);
			
			inpNSDeg.setText(STRreplace.replace(coords.getLatDeg(format),"-",""));
			inpNSm.setText(coords.getLatMin(format));
			inpNSs.setText(coords.getLatSec(format));
			
			inpEWDeg.setText(STRreplace.replace(coords.getLonDeg(format),"-",""));
			inpEWm.setText(coords.getLonMin(format));
			inpEWs.setText(coords.getLonSec(format));
		}
		chkFormat.selectIndex(format);
		inpText.setText(coords.toString(format));
		currFormat = format;
		activateFields(format);
	}

	public CWPoint getCoords(){
		return coordInp;
	}
	

	public void onEvent(Event ev){

		//Vm.debug(ev.toString());
		// Ensure that the Enter key moves to the appropriate field
		// for Checkboxes and Choice controls this is done via the exitKeys
		// For input fields we use the wantReturn field
		if(ev instanceof ControlEvent && ev.type == ControlEvent.EXITED){
			if (((ControlEvent)ev).target==chkDD || ((ControlEvent)ev).target==chkDMM ||
			    ((ControlEvent)ev).target==chkDMS) Gui.takeFocus(chcNS,ControlConstants.ByKeyboard);	
			if (((ControlEvent)ev).target==chkUTM) Gui.takeFocus(inpUTMZone,ControlConstants.ByKeyboard);
			if (((ControlEvent)ev).target==chkGK) Gui.takeFocus(inpUTMEasting,ControlConstants.ByKeyboard);
			if (((ControlEvent)ev).target==chcNS) Gui.takeFocus(inpNSDeg,ControlConstants.ByKeyboard);
			if (((ControlEvent)ev).target==chcEW) Gui.takeFocus(inpEWDeg,ControlConstants.ByKeyboard);
		}
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (((ControlEvent)ev).target==inpEWDeg || ((ControlEvent)ev).target==inpEWm ||
					((ControlEvent)ev).target==inpEWs || ((ControlEvent)ev).target==inpUTMNorthing) Gui.takeFocus(btnApply,ControlConstants.ByKeyboard);	
			if (ev.target == chkFormat){
				readFields(coordInp, currFormat);
				currFormat = chkFormat.getSelectedIndex();
				setFields(coordInp, currFormat);
				activateFields(currFormat);
				this.repaintNow();
			}

			if (ev.target == btnCancel){
				this.close(IDCANCEL);
			}

			if (ev.target == btnApply){
				currFormat = chkFormat.getSelectedIndex();
				readFields(coordInp, currFormat);
				if (coordInp.isValid()) this.close(IDOK);
				else {
					if	( allowInvalid ) {
						if ((new MessageBox(MyLocale.getMsg(144,"Warnung"),MyLocale.getMsg(1412,"Coordinates invalid. Apply anyway?"),FormBase.DEFOKB|FormBase.NOB)).execute() == FormBase.IDOK ) {
							this.close(IDOK);						
						}
					} else {
						(new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(1411,"Please enter valid coordinates"), FormBase.OKB)).execute();						
					}
				}
			}
			
			if (ev.target == btnPaste){
				inpText.setText(Vm.getClipboardText(""));
			}
			
			if (ev.target == btnCopy){
				currFormat = chkFormat.getSelectedIndex();
				readFields(coordInp, currFormat);
				Vm.setClipboardText(coordInp.toString(chkFormat.getSelectedIndex()));
			}

			if (ev.target == btnParse){
				// try to parse coords
				CWPoint coord;
				String inp=inpText.getText().trim().toUpperCase();
				if (inp.startsWith("GC")) {
					SpiderGC spider = new SpiderGC(Global.getPref(), Global.getProfile(), false);
					coord = new CWPoint(spider.getCacheCoordinates(inp));
				} else {	
					coord = new CWPoint(inp);
				}
				if (coord.latDec == -91 && coord.lonDec == -361){
					MessageBox tmpMB = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), FormBase.OKB);
					tmpMB.exec();
				}else {
					currFormat = chkFormat.getSelectedIndex();
					setFields(coord,currFormat);
					activateFields(currFormat);
					this.repaintNow();
				}
			}
			
			if (ev.target == btnGps){
				Navigate nav=Global.mainTab.nav;
				if (nav.gpsPos.isValid()){
					CWPoint coord = nav.gpsPos;
					currFormat = chkFormat.getSelectedIndex();
					setFields(coord,currFormat);
					activateFields(currFormat);
				}
			}
			
			if (ev.target == btnClear){
				CWPoint coord = new CWPoint(91,361);
				currFormat = chkFormat.getSelectedIndex();
				setFields(coord,currFormat);
				activateFields(currFormat);
			}
		}
		super.onEvent(ev);
	}

	
}
