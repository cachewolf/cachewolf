package CacheWolf;

import ewe.ui.*;
import ewe.ui.formatted.TextDisplay;
import ewe.util.Vector;
import ewe.fx.Dimension;
import ewe.sys.*;

/**
*	Class to create the panel to do calculation with waypoints<br>
*	Also allows for creation of a custom waypoint.<br>
*	Class ID 1400
*/

/**
 * Wrapper class to pass bearing and distance
 */
class BearingDistance {
	public double degrees;
	public double distance;
	
	public BearingDistance(){
		this.degrees = 0;
		this.distance = 0;
	}
	
	public BearingDistance(double degrees, double distance) {
		this.degrees = degrees;
		this.distance = distance;
	}
}

public class CalcPanel extends CellPanel {

	mCheckBox chkDMM, chkDMS, chkDD, chkUTM;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	mChoice chcNS, chcEW, chcDistUnit;
	mInput inpNSDeg, inpNSm, inpNSs, inpEWDeg, inpEWm, inpEWs;
	mInput inpUTMZone, inpUTMNorthing, inpUTMEasting;
	mInput inpBearing, inpDistance, inpText;
	TextDisplay txtOutput;
	mButton btnCalc, btnClear, btnSave, btnGoto, btnParse;
	BearingDistance bd = new BearingDistance();
	CWPoint coordInp = new CWPoint();
	CWPoint coordOut = new CWPoint();
	// Needed for creation of new waypoint
	Vector cacheDB;
	MainTab mainT;
	DetailsPanel detP;
	Preferences pref;
	Profile profile;
	// different panels to avoid spanning
	CellPanel TopP = new CellPanel();
	CellPanel BottomP = new CellPanel();

	
	int currFormat;

	
	public CalcPanel()
	{
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

		//Input for UTM
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

		//inpBearing and direction, unit for inpDistance
		BottomP.addNext(new mLabel(MyLocale.getMsg(1403,"Bearing")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addLast(new mLabel(MyLocale.getMsg(1404,"Distance")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addNext(inpBearing = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addNext(inpDistance = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addLast(chcDistUnit = new mChoice(new String[]{"m", "km"},0),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		chcDistUnit.setInt(0);
		
		// Buttons for calc and save
		BottomP.addNext(btnCalc = new mButton("Calc"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addNext(btnClear = new mButton("Clear"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addNext(btnGoto = new mButton("Goto"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addLast(btnSave = new mButton(MyLocale.getMsg(311,"Create Waypoint")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		
		// Output 
		txtOutput = new TextDisplay();
		ScrollBarPanel sbp = new ScrollBarPanel(txtOutput);
		BottomP.addLast(sbp.setTag(Control.SPAN, new Dimension(4,1)),CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));
		
		//add Panels
		this.addLast(TopP,CellConstants.DONTSTRETCH, CellConstants.WEST).setTag(SPAN,new Dimension(4,1));
		this.addLast(BottomP,CellConstants.VSTRETCH, CellConstants.VFILL|CellConstants.WEST).setTag(SPAN,new Dimension(4,1));
		
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
	public void readFields(CWPoint coords, BearingDistance degKm, int format){
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

		degKm.degrees = Common.parseDouble(inpBearing.getText());
		if (chcDistUnit.getInt() == 0)
			// meter
			degKm.distance = Common.parseDouble(inpDistance.getText()) / 1000;
		else 
			// kilometer
			degKm.distance = Common.parseDouble(inpDistance.getText());
		return;
	}
	
	public void setFields(CacheHolder ch, MainTab mt,DetailsPanel dp, Preferences p, Profile prof){
		pref = p;
		profile=prof;
		mainT = mt;
		detP = dp;
		cacheDB = profile.cacheDB;
		
		currFormat = CWPoint.DMM;
		if (ch.LatLon.length()== 0) coordInp.set(0,0);
		else coordInp.set(ch.LatLon, CWPoint.CW);
		setFields(coordInp, CWPoint.DMM);
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
			
			inpNSDeg.setText(coords.getLatDeg(format));
			inpNSm.setText(coords.getLatMin(format));
			inpNSs.setText(coords.getLatSec(format));
			
			inpEWDeg.setText(coords.getLonDeg(format));
			inpEWm.setText(coords.getLonMin(format));
			inpEWs.setText(coords.getLonSec(format));
		}
		chkFormat.selectIndex(format);
		inpText.setText(coords.toString(format));
		inpBearing.setText("0");
		inpDistance.setText("0");
		chcDistUnit.setInt(1);
	}


	public void onEvent(Event ev){

		//Vm.debug(ev.toString());

		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == chkFormat){
				readFields(coordInp,bd, currFormat);
				currFormat = chkFormat.getSelectedIndex();
				setFields(coordInp, currFormat);
				activateFields(currFormat);
				this.repaintNow();
			}

			if (ev.target == btnCalc){
				readFields(coordInp, bd, currFormat);
				currFormat = chkFormat.getSelectedIndex();
				coordOut = coordInp.project(bd.degrees, bd.distance);
				txtOutput.appendText(coordOut.toString(currFormat)+ "\n",true);
			}
			if (ev.target == btnClear){
				txtOutput.setText("");
			}
			if (ev.target == btnSave){
				CacheHolder ch = new CacheHolder();
				readFields(coordInp, bd, currFormat);
				currFormat = chkFormat.getSelectedIndex();
				coordOut = coordInp.project(bd.degrees, bd.distance);
				ch.LatLon = coordOut.toString();
				detP.newWaypoint(ch, mainT, pref, profile);
			}
			
			if (ev.target == btnGoto){
				readFields(coordInp, bd, currFormat);
				currFormat = chkFormat.getSelectedIndex();
				coordOut = coordInp.project(bd.degrees, bd.distance);
				mainT.gotoPoint(coordOut.toString());
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
