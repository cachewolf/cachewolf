package CacheWolf;

import ewe.ui.*;
import ewe.fx.Dimension;
import ewe.sys.*;

/**
*	Class for entering coordinates<br>
*	Class IDs 1400 and 600 (same as calc panel and preferences screen)<br>
*/


public class CoordsScreen extends Form {

	mCheckBox chkDMM, chkDMS, chkDD, chkUTM;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	mChoice chcNS, chcEW;
	mInput inpNSDeg, inpNSm, inpNSs, inpEWDeg, inpEWm, inpEWs;
	mInput inpUTMZone, inpUTMNorthing, inpUTMEasting;
	mInput inpText;
	mButton btnCancel, btnApply, btnCopy, btnPaste, btnParse;
	CWPoint coordInp = new CWPoint();
	CellPanel TopP = new CellPanel();
	CellPanel BottomP = new CellPanel();

	
	int currFormat;
	
	public CoordsScreen()
	{
		this.setTitle("");
		//Radiobuttons for format
		TopP.addNext(chkDD =new mCheckBox("d.d°"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkDMM =new mCheckBox("d°m.m\'"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkDMS =new mCheckBox("d°m\'s\""),CellConstants.DONTSTRETCH,CellConstants.WEST);
		TopP.addLast(chkUTM =new mCheckBox("UTM"),CellConstants.DONTSTRETCH, CellConstants.WEST);

		chkDD.setGroup(chkFormat);
		chkDMM.setGroup(chkFormat);
		chkDMS.setGroup(chkFormat);
		chkUTM.setGroup(chkFormat);

		// Input for degrees
		TopP.addNext(chcNS = new mChoice(new String[]{"N", "S"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chcNS.setInt(0);
		TopP.addNext(inpNSDeg = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		TopP.addNext(inpNSm = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		TopP.addLast(inpNSs = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
		TopP.addNext(chcEW = new mChoice(new String[]{"E", "W"},0),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		chcEW.setInt(0);
		TopP.addNext(inpEWDeg = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		TopP.addNext(inpEWm = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		TopP.addLast(inpEWs = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		// Input for UTM
		TopP.addNext(new mLabel(MyLocale.getMsg(1400,"Zone")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		TopP.addNext(new mLabel(MyLocale.getMsg(1402,"Easting")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		TopP.addLast(new mLabel(MyLocale.getMsg(1401,"Northing")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		TopP.addNext(inpUTMZone = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		TopP.addNext(inpUTMEasting = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		TopP.addLast(inpUTMNorthing = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		// Input for free Text
		TopP.addNext(inpText = new mInput(),CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		inpText.setTag(SPAN,new Dimension(3,1));
		TopP.addLast(btnParse = new mButton(MyLocale.getMsg(619,"Parse")),CellConstants.HSTRETCH, (CellConstants.HFILL));

		
		// Buttons for cancel and apply, copy and paste
		TopP.addNext(btnCancel = new mButton(MyLocale.getMsg(614,"Cancel")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnCancel.setTag(SPAN,new Dimension(4,1));
		TopP.addNext(btnApply = new mButton(MyLocale.getMsg(615,"Apply")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnApply.setTag(SPAN,new Dimension(4,1));
		TopP.addNext(btnPaste = new mButton(MyLocale.getMsg(617,"Paste")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnParse.setTag(SPAN,new Dimension(4,1));
		TopP.addLast(btnCopy = new mButton(MyLocale.getMsg(618,"Copy")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		//btnCopy.setTag(SPAN,new Dimension(4,1));


		
		//add Panels
		this.addLast(TopP,CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(SPAN,new Dimension(4,1));
//		this.addLast(BottomP,CellConstants.VSTRETCH, CellConstants.VFILL|CellConstants.WEST).setTag(SPAN,new Dimension(4,1));
		
	}
	
	public void activateFields(int format){
		// first enable all fields
		this.modifyAll(0,ControlConstants.Disabled);
		
		switch (format){
		case CWPoint.DD: 	inpNSm.modify(ControlConstants.Disabled,0);inpEWm.modify(ControlConstants.Disabled,0);
				 	inpNSs.modify(ControlConstants.Disabled,0);inpEWs.modify(ControlConstants.Disabled,0);
		case CWPoint.CW:
		case CWPoint.DMM: 	inpNSs.modify(ControlConstants.Disabled,0);inpEWs.modify(ControlConstants.Disabled,0);
		case CWPoint.DMS: 	inpUTMZone.modify(ControlConstants.Disabled,0);
					inpUTMNorthing.modify(ControlConstants.Disabled,0);
					inpUTMEasting.modify(ControlConstants.Disabled,0);
					break;
		case CWPoint.UTM: 	inpNSDeg.modify(ControlConstants.Disabled,0);inpEWDeg.modify(ControlConstants.Disabled,0);
					inpNSm.modify(ControlConstants.Disabled,0);inpEWm.modify(ControlConstants.Disabled,0);
	 				inpNSs.modify(ControlConstants.Disabled,0);inpEWs.modify(ControlConstants.Disabled,0);
	 				chcNS.modify(ControlConstants.Disabled,0);
	 				chcEW.modify(ControlConstants.Disabled,0);
	 				break;
		}
		
		this.stretchLastColumn = true;
		this.stretchLastRow = true;
		this.repaintNow();
	}

	public void readFields(CWPoint coords, int format){
		String NS, EW;
		if (format == CWPoint.UTM)
			coords.set(inpUTMZone.getText(), 
					   inpUTMNorthing.getText(), inpUTMEasting.getText());
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

		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
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
				this.close(IDOK);
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
				CWPoint coord = new CWPoint(inpText.getText());
				if (coord.latDec == 0 && coord.lonDec == 0){
					MessageBox tmpMB = new MessageBox(MyLocale.getMsg(312,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), MessageBox.OKB);
					tmpMB.exec();
				}else {
					currFormat = chkFormat.getSelectedIndex();
					setFields(coord,currFormat);
					activateFields(currFormat);
					this.repaintNow();
				}
			}
				
				
		}
		super.onEvent(ev);
	}

	
}
