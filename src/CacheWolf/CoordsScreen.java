    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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

import CacheWolf.imp.SpiderGC;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.ProjectedPoint;
import CacheWolf.navi.TransformCoordinates;
import ewe.fx.Dimension;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.MessageBox;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;

/**
 *	Class for entering coordinates<br>
 *	Class IDs 1400 and 600 (same as calc panel and preferences screen)<br>
 */


public class CoordsScreen extends Form {

	mCheckBox chkDMM, chkDMS, chkDD, chkCustom;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	mChoice localCooSystem;
	mChoice chcNS, chcEW;
	mInput inpNSDeg, inpNSm, inpNSs, inpEWDeg, inpEWm, inpEWs;
	mInput inpUTMZone, inpUTMNorthing, inpUTMEasting;
	mInput inpText;
	mButton btnCancel, btnApply, btnCopy, btnPaste, btnParse, btnGps, btnClear, btnSearch;
	InputScreen inpScreen;

	CWPoint coordInp = new CWPoint();
	CellPanel topLinePanel = new CellPanel();
	CellPanel mainPanel = new CellPanel();
	int exitKeys[]={75009};
	int currFormat;

	boolean bNSDeg = false;
	boolean bNSm = false;
	boolean	bNSs = false;
	boolean bEWDeg = false;
	boolean bEWm = false;
	boolean bEWs = false;
	boolean bUTMNorthing = false;
	boolean bUTMEasting = false;
	

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
		//topLinePanel.addNext(chkUTM =new mCheckBox("UTM"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		topLinePanel.addNext(chkCustom =new mCheckBox(""),CellConstants.DONTSTRETCH, CellConstants.WEST);

		String[] ls = TransformCoordinates.getProjectedSystemNames();
		topLinePanel.addLast(localCooSystem = new mChoice(ls, 0),CellConstants.DONTSTRETCH, CellConstants.WEST);

		chkDD.setGroup(chkFormat); chkDD.exitKeys=exitKeys;
		chkDMM.setGroup(chkFormat);chkDMM.exitKeys=exitKeys;
		chkDMS.setGroup(chkFormat);chkDMS.exitKeys=exitKeys;
		chkCustom.setGroup(chkFormat);chkCustom.exitKeys=exitKeys;
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
			mainPanel.addNext(new mLabel(MyLocale.getMsg(1401,"Northing")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.SOUTHWEST));
			mainPanel.addLast(btnSearch = new mButton(MyLocale.getMsg(1414,"Search")),CellConstants.HSTRETCH, (CellConstants.HFILL));
		}

		mainPanel.addNext(inpUTMZone = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addNext(inpUTMEasting = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addNext(inpUTMNorthing = new mInput(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		mainPanel.addLast(btnGps = new mButton("GPS"),CellConstants.HSTRETCH, (CellConstants.HFILL));

		//	mainPanel.addLast(new mLabel(MyLocale.getMsg(1405,"To load coordinates from GC, enter GCxxxxx below")),CellConstants.HSTRETCH, (CellConstants.HFILL)).setTag(SPAN,new Dimension(4,1));
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
		//inpEWDeg.wantReturn=false; inpEWm.wantReturn=false; inpEWs.wantReturn=false; inpUTMNorthing.wantReturn=false;

		switch (format){
		case TransformCoordinates.DD:
			enable(chcNS); enable(inpNSDeg); disable(inpNSm); disable(inpNSs);
			enable(chcEW); enable(inpEWDeg); disable(inpEWm); disable(inpEWs);
			//inpEWDeg.wantReturn=true;
			disable(inpUTMZone); disable(inpUTMNorthing); disable(inpUTMEasting);
			break;
		case TransformCoordinates.DMM: 	
			enable(chcNS); enable(inpNSDeg); enable(inpNSm); disable(inpNSs);
			enable(chcEW); enable(inpEWDeg); enable(inpEWm); disable(inpEWs);
			//inpEWm.wantReturn=true;
			disable(inpUTMZone); disable(inpUTMNorthing); disable(inpUTMEasting);
			break;
		case TransformCoordinates.DMS: 	
			enable(chcNS); enable(inpNSDeg); enable(inpNSm); enable(inpNSs);
			enable(chcEW); enable(inpEWDeg); enable(inpEWm); enable(inpEWs);
			//inpEWs.wantReturn=true;
			disable(inpUTMZone); disable(inpUTMNorthing); disable(inpUTMEasting);
			break;
		default: 	
			disable(chcNS); disable(inpNSDeg); disable(inpNSm); disable(inpNSs);
			disable(chcEW); disable(inpEWDeg); disable(inpEWm); disable(inpEWs);
			if (TransformCoordinates.localSystems[localCooSystem.getInt()].zoneSeperatly) enable(inpUTMZone);
			else disable(inpUTMZone); 
			enable(inpUTMNorthing); enable(inpUTMEasting);
			//inpUTMNorthing.wantReturn=true;
			break;
		}

		this.stretchLastColumn = true;
		this.stretchLastRow = true;
		this.repaintNow();
	}

	private void enable(Control c) {c.modify(ControlConstants.TakesKeyFocus,ControlConstants.Disabled); }
	private void disable(Control c) {c.modify(ControlConstants.Disabled,ControlConstants.TakesKeyFocus); }

	public void readFields(CWPoint coords){
		String NS, EW;
		if (localSystemToformatSel(currFormat) >= formatSelToLocalSystem.length) {
			if (TransformCoordinates.getLocalSystem(currFormat).zoneSeperatly)
				coords.set(inpUTMNorthing.getText(), inpUTMEasting.getText(), inpUTMZone.getText(), currFormat); 
			else
				coords.set(inpUTMNorthing.getText(), inpUTMEasting.getText(), currFormat);			
		}
		else {
			NS = chcNS.getInt()== 0?"N":"S";
			EW = chcEW.getInt()== 0?"E":"W";
			coords.set(NS, inpNSDeg.getText(), inpNSm.getText(), inpNSs.getText(),
					EW, inpEWDeg.getText(), inpEWm.getText(), inpEWs.getText(),
					currFormat);
		}
		int formatsel = combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt());
		currFormat = getLocalSystem(formatsel);
		return;
	}
	public void setFields(CWPoint coords, int format) {
		int formatsel = localSystemToformatSel(format); 
		if ( formatsel >= formatSelToLocalSystem.length){ // projected point = neither dd, dd° mm.mm nor dd° mm' ss.s"
			if (coords.isValid()){
				localCooSystem.setInt(formatsel - formatSelToLocalSystem.length);
				ProjectedPoint pp = TransformCoordinates.wgs84ToLocalsystem(coords, format);
				inpText.setText(pp.toHumanReadableString());
				inpUTMNorthing.setText(Common.DoubleToString(pp.getNorthing(),0,0));
				inpUTMEasting.setText(Common.DoubleToString(pp.getEasting(),0,0));
				if (TransformCoordinates.getLocalSystem(format).zoneSeperatly) inpUTMZone.setText(pp.getZoneString()); 
				else inpUTMZone.setText("");
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
		chkFormat.selectIndex(java.lang.Math.min(localSystemToformatSel(format), formatSelToLocalSystem.length));
		inpText.setText(coords.toString(format));
		currFormat = format;
		activateFields(format);
	}

	public CWPoint getCoords(){
		return coordInp;
	}


	
	public void onEvent(Event ev){

		// Ensure that the Enter key moves to the appropriate field
		// for Checkboxes and Choice controls this is done via the exitKeys
		// For input fields we use the wantReturn field

		if(ev instanceof ControlEvent && ev.type == ControlEvent.EXITED){
			if (((ControlEvent)ev).target==chkDD || ((ControlEvent)ev).target==chkDMM ||
					((ControlEvent)ev).target==chkDMS) Gui.takeFocus(chcNS,ControlConstants.ByKeyboard);	
			if (((ControlEvent)ev).target==chkCustom) Gui.takeFocus(inpUTMEasting,ControlConstants.ByKeyboard);
			if (((ControlEvent)ev).target==chcNS) Gui.takeFocus(inpNSDeg,ControlConstants.ByKeyboard);
			if (((ControlEvent)ev).target==chcEW) Gui.takeFocus(inpEWDeg,ControlConstants.ByKeyboard);
		}
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (((ControlEvent)ev).target==inpEWDeg || ((ControlEvent)ev).target==inpEWm ||
					((ControlEvent)ev).target==inpEWs || ((ControlEvent)ev).target==inpUTMNorthing) Gui.takeFocus(btnApply,ControlConstants.ByKeyboard);	
			
			
			if (ev.target == chkFormat || ev.target == localCooSystem){
				if (ev.target == localCooSystem) chkFormat.selectIndex(3);
				readFields(coordInp);
				setFields(coordInp, currFormat);
				this.repaintNow();
			}

			if (ev.target == btnCancel){
				this.close(IDCANCEL);
			}

			if (ev.target == btnApply){
				currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
				readFields(coordInp);
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
				readFields(coordInp); // TODO was anderes als Gauß-Krüger unterstützen
				Vm.setClipboardText(coordInp.toString(currFormat));
			}

			if (ev.target == btnParse){
				// try to parse coords
				CWPoint coord;
				String inp=inpText.getText().trim().toUpperCase();
				if (inp.startsWith("GC")) {
					SpiderGC spider = new SpiderGC(Global.getPref(), Global.getProfile());
					coord = new CWPoint(spider.getCacheCoordinates(inp));
				} else {	
					coord = new CWPoint(inp);
				}
				if (!coord.isValid()){
					MessageBox tmpMB = new MessageBox(MyLocale.getMsg(321,"Error"), MyLocale.getMsg(4111,"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"), FormBase.OKB);
					tmpMB.exec();
				}else {
					currFormat =  getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
					setFields(coord,currFormat);
					this.repaintNow();
				}
			}

			if (ev.target == btnGps){
				Navigate nav=Global.mainTab.nav;
				if (nav.gpsPos.isValid()){
					CWPoint coord = nav.gpsPos;
					currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
					setFields(coord,currFormat);
				}
			}

			if (ev.target == btnClear){
				CWPoint coord = new CWPoint(91,361);
				currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
				setFields(coord,currFormat);
			}
			
			if (ev.target == btnSearch) {
				GeoCodeGui s = new GeoCodeGui(); 
				int ok = s.execute();
				if (ok == FormBase.IDOK) {
					currFormat = getLocalSystem(combineToFormatSel(chkFormat.getSelectedIndex(), localCooSystem.getInt()));
					setFields(s.coordInp,currFormat);
				}
			}
		}
		super.onEvent(ev);
	}
	

	private static final int[] formatSelToLocalSystem = {
		TransformCoordinates.DD,
		TransformCoordinates.DMM,
		TransformCoordinates.DMS,
	};

	public int localSystemToformatSel(int cwpointformat) {
		for (int i=0; i < formatSelToLocalSystem.length; i++) 
			if (formatSelToLocalSystem[i] == cwpointformat) return i;
		for (int i=0; i < TransformCoordinates.localSystems.length; i++) 
			if (TransformCoordinates.localSystems[i].code == cwpointformat) return i + formatSelToLocalSystem.length;
		
		throw new IllegalArgumentException("CoordScreen.CWPointformatToformatSel: cwpointformat " + cwpointformat + "not supported");
	}

	public static final int getLocalSystem(int formatsel) { // be carefull: this method is also used by CalcPanel
		if (formatsel < formatSelToLocalSystem.length) return formatSelToLocalSystem[formatsel];
		return TransformCoordinates.localSystems[formatsel - formatSelToLocalSystem.length].code;
	}
	
	public static final int combineToFormatSel(int radiobuttonindex, int choiceindex) {
		int ret = radiobuttonindex;
		if (ret == formatSelToLocalSystem.length) ret += choiceindex;
		return ret;
	}

}



