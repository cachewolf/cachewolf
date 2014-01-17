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

import CacheWolf.database.CWPoint;
import CacheWolf.navi.Navigate;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.utils.Common;
import ewe.fx.Color;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.Pen;
import ewe.fx.Rect;
import ewe.ui.ButtonObject;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.SingleContainer;
import ewe.ui.mButton;
import ewe.ui.mLabel;

/**
 * Class for entering coordinates<br>
 * Class IDs 1400 and 600 (same as calc panel and preferences screen)<br>
 */

public class CoordsPDAInput extends Form {

    private static StringBuffer strBufCoords = new StringBuffer(30);
    private static StringBuffer strBufDistanc = new StringBuffer(10);
    private static StringBuffer strBufBear = new StringBuffer(3);

    static final Color ColorEdit = new Color(0, 255, 0);// green
    static final Color ColorPos = new Color(255, 0, 0);// red
    static final Color ColorNormal = new Color(192, 192, 192);// grey

    private static final int POS_NDD = 2, LEN_NDD = 3;
    private static final int POS_NMM = 6, LEN_NMM = 3;
    private static final int POS_NSSS = 9, LEN_NSSS = 3;
    private static final int POS_EDDD = 15, LEN_EDDD = 4;
    private static final int POS_EMM = 20, LEN_EMM = 3;
    private static final int POS_ESSS = 23, LEN_ESSS = 3;
    private int iPosition = POS_NDD;
    private int iPosBear = 0, iPosDist = 0;

    int currFormatSel;
    CWPoint coordsInput = new CWPoint();
    CWPoint coordsBack = new CWPoint();
    CWPoint coordsBear = new CWPoint();

    BearingDistance bd = new BearingDistance();
    private boolean bBearingPanelOnTop = false;
    // different panels to avoid spanning
    private CellPanel MainP = new CellPanel();
    private CellPanel TopP = new CellPanel();
    private SingleContainer TopSP = new SingleContainer();
    private CellPanel BottomP = new CellPanel();
    private CellPanel ExpertP = new CellPanel();
    private CellPanel BearP = new CellPanel();
    // all Buttons
    private mButton btnNorth, btnWest;
    private mButtonPos btnNorthDD, btnEastDDD;
    private mButtonPos btnNorthMM, btnEastMM;
    private mButtonPos btnNorthSSS, btnEastSSS;
    private mButton btn9, btn8, btn7, btn6, btn5, btn4, btn3, btn2, btn1, btn0, btnOk, btnEsc;
    private mButton btnBear, btnGPS, btnExpert;
    private mLabel lblDistanc = new mLabel(MyLocale.getMsg(1404, "Distance"));
    private mLabel lblBearing = new mLabel(MyLocale.getMsg(1403, "Bearing"));
    private mButtonPos btnBearing, btnDistanc;
    private mLabel lblDist = new mLabel("m");
    private mLabel lblBear = new mLabel("°");
    private int[] iPointPos = { 0, 0, 0, 0, 0, 0 };

    private boolean allowInvalid = false;

    public CoordsPDAInput(int FormSelect, boolean allowInvalidCoords) {
	allowInvalid = allowInvalidCoords;

	currFormatSel = FormSelect;
	InitInputScreen();
	//	setTextButton(sCoords);
    }

    public CoordsPDAInput(int FormSelect) {
	currFormatSel = FormSelect;
	InitInputScreen();
	//	setTextButton(sCoords);
    }

    private void InitInputScreen() {
	int sw = MyLocale.getScreenWidth();
	int sh = MyLocale.getScreenHeight();
	int fs = Preferences.itself().fontSize;
	int psx;
	int psy;
	if ((sw > 300) && (sh > 300)) {
	    // larger screens: size according to fontsize
	    psx = 240;
	    psy = 260;
	    if (fs > 12) {
		psx = 300;
		psy = 330;
	    }
	    if (fs > 17) {
		psx = 400;
		psy = 340;
	    }
	    if (fs > 23) {
		psx = 500;
		psy = 350;
	    }
	    this.setPreferredSize(psx, psy);
	    MainP.setPreferredSize(psx, psy);
	} else {
	    // small screens: fixed size
	    if (sh > 240) {
		this.setPreferredSize(240, 260);
		MainP.setPreferredSize(240, 260);
	    } else {
		this.setPreferredSize(240, 240);
		MainP.setPreferredSize(240, 240);
	    }
	}

	this.setTitle("InputConsole");

	addLast(MainP, CellConstants.STRETCH, (CellConstants.FILL));
	MainP.addLast(TopSP, CellConstants.STRETCH, (CellConstants.FILL));
	MainP.addLast(BottomP, CellConstants.STRETCH, (CellConstants.FILL));
	MainP.addLast(ExpertP, CellConstants.STRETCH, (CellConstants.FILL));
	btnNorth = new mButton("N");
	btnNorth.borderStyle = BDR_NOBORDER;
	TopP.addNext(btnNorth, CellConstants.STRETCH, (CellConstants.FILL));
	btnNorthDD = new mButtonPos("DD°");
	btnNorthDD.borderStyle = BDR_NOBORDER;
	TopP.addNext(btnNorthDD, CellConstants.STRETCH, (CellConstants.FILL));
	btnNorthMM = new mButtonPos("MM.");
	btnNorthMM.borderStyle = BDR_NOBORDER;
	TopP.addNext(btnNorthMM, CellConstants.STRETCH, (CellConstants.FILL));
	btnNorthSSS = new mButtonPos("SSS");
	btnNorthSSS.borderStyle = BDR_NOBORDER;
	TopP.addLast(btnNorthSSS, CellConstants.STRETCH, (CellConstants.FILL));

	btnWest = new mButton("E");
	btnWest.borderStyle = BDR_NOBORDER;
	TopP.addNext(btnWest, CellConstants.STRETCH, (CellConstants.FILL));
	btnEastDDD = new mButtonPos("DDD°");
	btnEastDDD.borderStyle = BDR_NOBORDER;
	TopP.addNext(btnEastDDD, CellConstants.STRETCH, (CellConstants.FILL));
	btnEastMM = new mButtonPos("MM.");
	btnEastMM.borderStyle = BDR_NOBORDER;
	TopP.addNext(btnEastMM, CellConstants.STRETCH, (CellConstants.FILL));
	btnEastSSS = new mButtonPos("SSS");
	btnEastSSS.borderStyle = BDR_NOBORDER;
	TopP.addLast(btnEastSSS, CellConstants.STRETCH, (CellConstants.FILL));
	TopSP.setControl(TopP);

	btn7 = new mButton("  7  ");
	BottomP.addNext(btn7, CellConstants.STRETCH, (CellConstants.FILL));
	btn8 = new mButton(" 8 ");
	BottomP.addNext(btn8, CellConstants.STRETCH, (CellConstants.FILL));
	btn9 = new mButton(" 9 ");
	BottomP.addLast(btn9, CellConstants.STRETCH, (CellConstants.FILL));
	btn4 = new mButton("  4  ");
	BottomP.addNext(btn4, CellConstants.STRETCH, (CellConstants.FILL));
	btn5 = new mButton(" 5 ");
	BottomP.addNext(btn5, CellConstants.STRETCH, (CellConstants.FILL));
	btn6 = new mButton(" 6 ");
	BottomP.addLast(btn6, CellConstants.STRETCH, (CellConstants.FILL));
	btn1 = new mButton("  1  ");
	BottomP.addNext(btn1, CellConstants.STRETCH, (CellConstants.FILL));
	btn2 = new mButton(" 2 ");
	BottomP.addNext(btn2, CellConstants.STRETCH, (CellConstants.FILL));
	btn3 = new mButton(" 3 ");
	BottomP.addLast(btn3, CellConstants.STRETCH, (CellConstants.FILL));
	btn0 = new mButton("     0     ");
	BottomP.addNext(btn0, CellConstants.STRETCH, (CellConstants.FILL));
	btnGPS = new mButton("   GPS   ");
	BottomP.addNext(btnGPS, CellConstants.STRETCH, (CellConstants.FILL));
	btnBear = new mButton(MyLocale.getMsg(1415, "bearing"));
	BottomP.addLast(btnBear, CellConstants.STRETCH, (CellConstants.FILL));
	btnExpert = new mButton(" Expert");
	ExpertP.addNext(btnExpert, CellConstants.STRETCH, (CellConstants.FILL));
	btnEsc = new mButton("  Abbr   ");
	ExpertP.addNext(btnEsc, CellConstants.STRETCH, (CellConstants.FILL));
	btnOk = new mButton("   OK   ");
	ExpertP.addLast(btnOk, CellConstants.STRETCH, (CellConstants.FILL));

	//bearing Pannel
	BearP.addNext(lblDistanc, CellConstants.STRETCH, (CellConstants.FILL));
	btnDistanc = new mButtonPos("        0");
	btnDistanc.borderStyle = BDR_NOBORDER;
	BearP.addNext(btnDistanc, CellConstants.STRETCH, (CellConstants.FILL));
	BearP.addLast(lblDist, CellConstants.STRETCH, (CellConstants.FILL));
	BearP.addNext(lblBearing, CellConstants.STRETCH, (CellConstants.FILL));
	btnBearing = new mButtonPos("        0");
	btnBearing.borderStyle = BDR_NOBORDER;
	BearP.addNext(btnBearing, CellConstants.STRETCH, (CellConstants.FILL));
	BearP.addLast(lblBear, CellConstants.STRETCH, (CellConstants.FILL));

	int inpFontSize = (4 * Preferences.itself().fontSize) / 2;
	Font inpNewFont = new Font("Helvetica", Font.PLAIN, inpFontSize);
	lblDistanc.setFont(inpNewFont);
	lblBearing.setFont(inpNewFont);
	btnDistanc.setFont(inpNewFont);
	btnBearing.setFont(inpNewFont);
	btnNorth.setFont(inpNewFont);
	btnWest.setFont(inpNewFont);
	btnNorthDD.setFont(inpNewFont);
	btnEastDDD.setFont(inpNewFont);
	btnNorthMM.setFont(inpNewFont);
	btnEastMM.setFont(inpNewFont);
	btnNorthSSS.setFont(inpNewFont);
	btnEastSSS.setFont(inpNewFont);
	lblBear.setFont(inpNewFont);
	lblDist.setFont(inpNewFont);

	// int btnFontSize = ( 3 * pref.fontSize ) / 2;
	Font btnNewFont = inpNewFont; // new Font("Helvetica", Font.PLAIN, btnFontSize );
	btn9.setFont(btnNewFont);
	btn8.setFont(btnNewFont);
	btn7.setFont(btnNewFont);
	btn6.setFont(btnNewFont);
	btn5.setFont(btnNewFont);
	btn4.setFont(btnNewFont);
	btn3.setFont(btnNewFont);
	btn2.setFont(btnNewFont);
	btn1.setFont(btnNewFont);
	btn0.setFont(btnNewFont);
	btnEsc.setFont(btnNewFont);
	btnOk.setFont(inpNewFont);
	btnExpert.setFont(btnNewFont);
	btnBear.setFont(btnNewFont);
	btnGPS.setFont(btnNewFont);

    }

    private void disable(Control c) {
	c.modify(ControlConstants.Disabled, ControlConstants.TakesKeyFocus);
    }

    private void enable(Control c) {
	c.modify(ControlConstants.TakesKeyFocus, ControlConstants.Disabled);
    }

    private void setFocusCoords() {
	iPosition = POS_NMM;
	checkPosition();
    }

    private void changeTextButton() {
	btnNorthDD.setText(strBufCoords.substring(POS_NDD, LEN_NDD + POS_NDD), iPointPos[0]);
	btnNorthMM.setText(strBufCoords.substring(POS_NMM, LEN_NMM + POS_NMM), iPointPos[1]);
	btnNorthSSS.setText(strBufCoords.substring(POS_NSSS, LEN_NSSS + POS_NSSS), iPointPos[2]);
	btnEastDDD.setText(strBufCoords.substring(POS_EDDD, LEN_EDDD + POS_EDDD), iPointPos[3]);
	btnEastMM.setText(strBufCoords.substring(POS_EMM, LEN_EMM + POS_EMM), iPointPos[4]);
	btnEastSSS.setText(strBufCoords.substring(POS_ESSS, LEN_ESSS + POS_ESSS), iPointPos[5]);
	this.repaint();
	//disable(btnBear);
    }

    private void changeTextButtonBearing() {
	btnDistanc.setText(strBufDistanc.toString(), iPosDist);
	btnBearing.setText(strBufBear.toString(), iPosBear);
	this.repaint();
    }

    public void setCoords(CWPoint coords) {
	setCoords(coords, true);
    }

    public void setCoords(CWPoint coords, boolean setFocus) {
	StringBuffer strBufTemp = new StringBuffer(30);
	strBufTemp.delete(0, strBufTemp.length());
	strBufCoords.delete(0, strBufCoords.length());

	if (coords.isValid()) {
	    strBufTemp.append(coords.toString(TransformCoordinates.CW));
	    strBufCoords.append(strBufTemp.toString());
	    coordsInput = coords;
	} else {
	    strBufCoords.append("N 91° 00.000 E 361° 00.000");
	    coordsInput = coords;
	}
	if (setFocus) {
	    setFocusCoords();
	}
    }

    private void setTextBuffer(char cValue) {
	if (bBearingPanelOnTop) {
	    if (iPosBear == 4) {
		if (strBufBear.length() < 3)
		    strBufBear.insert(strBufBear.length(), cValue);
		changeTextButtonBearing();
	    } else {
		strBufDistanc.insert(strBufDistanc.length(), cValue);
		changeTextButtonBearing();
	    }

	} else {
	    //'°' und '.' filtern
	    if (iPosition == POS_NMM - 2 || iPosition == POS_EMM - 2 || iPosition == POS_ESSS - 1 || iPosition == POS_NSSS - 1 || iPosition == POS_ESSS - 1 || iPosition == POS_EDDD - 3)
		iPosition++;

	    if (iPosition == POS_NMM - 1 || iPosition == POS_EMM - 1 || iPosition == POS_EDDD - 2)
		iPosition++;

	    if (iPosition == POS_EDDD - 1)
		iPosition++;

	    if (iPosition >= strBufCoords.length()) {
		//iPosition = strBufCoords.length();
		iPosition = POS_NDD;
		//return;
	    }
	    strBufCoords.setCharAt(iPosition, cValue);

	    if (iPosition == POS_NSSS + 2)
		iPosition = POS_EMM - 1;
	    iPosition++;
	    checkPosition();
	}
    }

    private void checkPosition() {
	iPointPos[0] = 0;
	iPointPos[1] = 0;
	iPointPos[2] = 0;
	iPointPos[3] = 0;
	iPointPos[4] = 0;
	iPointPos[5] = 0;
	switch (iPosition) {
	//NorthDD
	case 26:
	case 2:
	    iPointPos[0] = 1;
	    break;
	case 3:
	    iPointPos[0] = 2;
	    break;
	//NorthMM
	case 4:
	case 5:
	case 6:
	    iPointPos[1] = 1;
	    break;
	case 7:
	    iPointPos[1] = 2;
	    break;
	//NorthSSS
	case 8:
	case 9:
	    iPointPos[2] = 1;
	    break;
	case 10:
	    iPointPos[2] = 2;
	    break;
	case 11:
	    iPointPos[2] = 3;
	    break;

	//EastDDD
	case 12:
	case 13:
	case 14:
	case 15:
	    iPointPos[3] = 1;
	    break;
	case 16:
	    iPointPos[3] = 2;
	    break;
	case 17:
	    iPointPos[3] = 3;
	    break;
	//EastMM
	case 18:
	case 19:
	case 20:
	    iPointPos[4] = 1;
	    break;
	case 21:
	    iPointPos[4] = 2;
	    break;
	//EastSSS
	case 22:
	case 23:
	    iPointPos[5] = 1;
	    break;
	case 24:
	    iPointPos[5] = 2;
	    break;
	case 25:
	    iPointPos[5] = 3;
	    break;
	}
	changeTextButton();
    }

    /*
     * set lat and lon by using coordinates in "CacheWolf" format
     * @param coord  String of type N 49° 33.167 E 011° 21.608
     * @param format only CWPoint.CW is supported
     */
    public CWPoint getCoords() {
	coordsBack.set(strBufCoords.toString(), TransformCoordinates.CW);
	return coordsBack;
    }

    public void onEvent(Event ev) {
	if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
	    // Button "E"
	    if (ev.target == btnWest) {
		if (btnWest.getText().startsWith("E"))
		    btnWest.setText("W");
		else
		    btnWest.setText("E");
	    }

	    // Button "N"
	    if (ev.target == btnNorth) {
		if (btnNorth.getText().startsWith("N"))
		    btnNorth.setText("S");
		else
		    btnNorth.setText("N");
	    }

	    // Button "Bearing"
	    if (ev.target == btnBearing) {
		iPosDist = 0;
		iPosBear = 4;
		strBufBear.delete(0, strBufBear.length());
		changeTextButtonBearing();
	    }

	    // Button "Distanc"
	    if (ev.target == btnDistanc) {
		iPosDist = 4;
		iPosBear = 0;
		strBufDistanc.delete(0, strBufDistanc.length());
		changeTextButtonBearing();
	    }

	    // Button "North DD"
	    if (ev.target == btnNorthDD) {
		iPosition = POS_NDD;
		checkPosition();
	    }

	    // Button "North MM"
	    if (ev.target == btnNorthMM) {
		iPosition = POS_NMM;
		checkPosition();
	    }

	    // Button "North SSS"
	    if (ev.target == btnNorthSSS) {
		iPosition = POS_NSSS;
		checkPosition();
	    }

	    // Button "East DDD"
	    if (ev.target == btnEastDDD) {
		iPosition = POS_EDDD;
		checkPosition();
	    }

	    // Button "East MM"
	    if (ev.target == btnEastMM) {
		iPosition = POS_EMM;
		checkPosition();
	    }

	    // Button "East SSS"
	    if (ev.target == btnEastSSS) {
		iPosition = POS_ESSS;
		checkPosition();
	    }

	    // Button "1"
	    if (ev.target == btn1) {
		setTextBuffer('1');
	    }
	    // Button "2"
	    if (ev.target == btn2) {
		setTextBuffer('2');
	    }
	    // Button "3"
	    if (ev.target == btn3) {
		setTextBuffer('3');
	    }
	    // Button "4"
	    if (ev.target == btn4) {
		setTextBuffer('4');
	    }
	    // Button "5"
	    if (ev.target == btn5) {
		setTextBuffer('5');
	    }
	    // Button "6"
	    if (ev.target == btn6) {
		setTextBuffer('6');
	    }
	    // Button "7"
	    if (ev.target == btn7) {
		setTextBuffer('7');
	    }
	    // Button "8"
	    if (ev.target == btn8) {
		setTextBuffer('8');
	    }
	    // Button "9"
	    if (ev.target == btn9) {
		setTextBuffer('9');
	    }
	    // Button "0"
	    if (ev.target == btn0) {
		setTextBuffer('0');
	    }

	    // Button "OK"
	    if (ev.target == btnOk) {
		iPosition = 0;
		if (bBearingPanelOnTop) {
		    coordsBear = this.getCoords();
		    bd.degrees = Common.parseDouble(btnBearing.getText());
		    bd.distance = Common.parseDouble(btnDistanc.getText());
		    // only meters !!
		    setCoords(coordsBear.project(bd.degrees, bd.distance / 1000.0));
		    bBearingPanelOnTop = false;
		}
		this.close(IDOK);
	    }
	    // Button "ESC"
	    if (ev.target == btnEsc) {
		if (bBearingPanelOnTop) {
		    enable(btnExpert);
		    enable(btnGPS);
		    enable(btnBear);
		    TopSP.setControl(TopP);
		    bBearingPanelOnTop = false;
		    this.repaint();
		} else {
		    iPosition = 0;
		    this.close(0);
		}
	    }

	    // Button "Bearing"
	    if (ev.target == btnBear) {
		disable(btnExpert);
		disable(btnGPS);
		disable(btnBear);
		strBufBear.delete(0, strBufBear.length());
		strBufDistanc.delete(0, strBufDistanc.length());
		TopSP.setControl(BearP, true);
		bBearingPanelOnTop = true;
		this.repaint();

	    }

	    // Button "GPS"
	    if (ev.target == btnGPS) {
		Navigate nav = MainTab.itself.navigate;
		if (nav.gpsPos.isValid()) {
		    setCoords(nav.gpsPos);
		}
	    }

	    // Button "Expert"
	    if (ev.target == btnExpert) {
		CoordsInput cs = new CoordsInput(allowInvalid);
		setCoords(getCoords(), false);
		//if (CoordsInput.isValid())	cs.setFields(CoordsInput, CoordsInput.getLocalSystem(currFormatSel));
		if (coordsInput.isValid())
		    cs.setFields(coordsInput, currFormatSel);
		else
		    cs.setFields(new CWPoint(0, 0), CoordsInput.getLocalSystem(currFormatSel));
		if (cs.execute(null, CellConstants.TOP) == FormBase.IDOK) {
		    setCoords(cs.getCoords(), false);
		    this.close(IDOK);
		}

	    }

	}
	super.onEvent(ev);
    }

    class mButtonPos extends mButton {
	private int iPosition, iPosY = 5, iGap = 1;
	private FontMetrics tempFm;
	private int stringHeight;
	private int sWidth1, sWidth2, sWidth3, stringWidth;
	private int TextStart;

	mButtonPos(String sValue) {
	    this.setText(sValue);
	}

	public void setText(String sValue, int iPos) {
	    this.setText(sValue);
	    tempFm = this.getFontMetrics(font);
	    stringHeight = tempFm.getHeight();
	    if (sValue.length() >= 3) {
		sWidth1 = tempFm.getTextWidth(sValue.substring(0, 1));
		sWidth2 = tempFm.getTextWidth(sValue.substring(1, 2));
		sWidth3 = tempFm.getTextWidth(sValue.substring(2, 3));
		stringWidth = tempFm.getTextWidth(sValue);
		TextStart = (this.width - stringWidth) / 2;
	    }
	    iPosition = iPos;
	}

	public void doPaint(Graphics g, Rect area) {
	    //if(iPosition > 0) g.setColor(ColorEdit); else  g.setColor(ColorNormal);
	    super.doPaint(g, area);
	    g.setPen(new Pen(ColorPos, Pen.SOLID, 2));
	    //g.fillRect(area.x, area.y, area.height, area.width);
	    //g.draw3DRect(area, 1, false, ColorEdit, ColorEdit);

	    switch (iPosition) {
	    case 0:
		g.setPen(new Pen(ColorNormal, Pen.SOLID, 2));
		g.drawRect(0, 0, 0, 0);
		g.draw3DRect(area, ButtonObject.buttonEdge, true, null, ColorNormal);
		break;
	    case 1:
		g.draw3DRect(area, ButtonObject.buttonEdge, true, null, ColorEdit);
		g.drawRect(TextStart, iPosY, sWidth1 + 1, stringHeight);
		break;
	    case 2:
		g.draw3DRect(area, ButtonObject.buttonEdge, true, null, ColorEdit);
		g.drawRect(TextStart + sWidth2 + iGap, iPosY, sWidth2 + 1, stringHeight);
		break;
	    case 3:
		g.draw3DRect(area, ButtonObject.buttonEdge, true, null, ColorEdit);
		g.drawRect(TextStart + sWidth2 + sWidth3, iPosY, sWidth3 + 1, stringHeight);
		break;
	    default:
		g.draw3DRect(area, ButtonObject.buttonEdge, true, null, ColorEdit);
		break;
	    }

	}

    }

}
