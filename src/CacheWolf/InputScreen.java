package CacheWolf;


import CacheWolf.navi.Navigate;
import CacheWolf.navi.TransformCoordinates;
import ewe.fx.Font;
import ewe.fx.Color;
import ewe.ui.*;


/**
*	Class for entering coordinates<br>
*	Class IDs 1400 and 600 (same as calc panel and preferences screen)<br>
*/


public class InputScreen extends Form {

	private static StringBuffer strBufCoords = new StringBuffer(30);
	private static StringBuffer strBufDistanc = new StringBuffer(10);
	private static StringBuffer strBufBear = new StringBuffer(3);


	private int iPosition;
	private static final int POS_NDD=2, LEN_NDD=3;
	private static final int POS_NMM=6, LEN_NMM=3;
	private static final int POS_NSSS=9, LEN_NSSS=3;
	private static final int POS_EDDD=15, LEN_EDDD=4;
	private static final int POS_EMM=20, LEN_EMM=3;
	private static final int POS_ESSS=23, LEN_ESSS=3;

	public Navigate myNavigation;
	int currFormatSel;
	CWPoint CoordsBack = new CWPoint();
	CWPoint CoordsBear = new CWPoint();

	BearingDistance bd = new BearingDistance();
	private boolean bBearingPanelOnTop = false;

	static Color ColorEdit = new Color(0,255,0);// green
	static Color ColorNormal = new Color(192,192,192);// grey
	// different panels to avoid spanning
	private CellPanel MainP = new CellPanel();
	private CellPanel TopP = new CellPanel();
	private SingleContainer TopSP = new SingleContainer();
	private CellPanel BottomP = new CellPanel();
	private CellPanel ExpertP = new CellPanel();
	private CellPanel BearP = new CellPanel();
	// all Buttons
	private mButton btnNorth, btnWest;
	private mButton btnNorthDD, btnEastDDD;
	private mButton btnNorthMM, btnEastMM;
	private mButton btnNorthSSS, btnEastSSS;
	private mButton btn9, btn8, btn7, btn6, btn5, btn4, btn3, btn2, btn1, btn0, btnOk, btnEsc;
	private mButton btnBear, btnGPS , btnExpert;
	private mLabel lblDistanc = new mLabel(MyLocale.getMsg(1404,"Distance"));
	private mLabel lblBearing = new mLabel(MyLocale.getMsg(1403,"Bearing"));
	private mButton btnBearing, btnDistanc;
	private mLabel lblDist = new mLabel("m");
	private mLabel lblBear = new mLabel("°");


	public InputScreen(Navigate nav, int FormSelect)
	{
		myNavigation = nav;
		currFormatSel = FormSelect;
		InitInputScreen();
	//	setTextButton(sCoords);
	}

	private void InitInputScreen()
	{
		int sw = MyLocale.getScreenWidth(); int sh = MyLocale.getScreenHeight();
		Preferences pref = Global.getPref();int fs = pref.fontSize;
		int psx; int psy;
		if((sw>300) && (sh>300)){
			// larger screens: size according to fontsize
			psx=240;psy=260;
			if(fs > 12){psx=300;psy=330;}
			if(fs > 17){psx=400;psy=340;}
			if(fs > 23){psx=500;psy=350;}
			this.setPreferredSize(psx,psy);
			MainP.setPreferredSize(psx,psy);
		}
		else{
			// small screens: fixed size
			if (sh>240)
			{
				this.setPreferredSize(240,260);
				MainP.setPreferredSize(240,260);
			}
			else
			{
				this.setPreferredSize(240,240);
				MainP.setPreferredSize(240,240);
			}
		}


		this.setTitle("InputConsole");


		addLast(MainP,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		MainP.addLast(TopSP,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		MainP.addLast(BottomP,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		MainP.addLast(ExpertP,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnNorth = new mButton("N");
		btnNorth.borderStyle = BDR_NOBORDER;
		TopP.addNext(btnNorth,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnNorthDD = new mButton("DD°");
		btnNorthDD.borderStyle = BDR_NOBORDER;
		TopP.addNext(btnNorthDD,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnNorthMM = new mButton("MM.");
		btnNorthMM.borderStyle = BDR_NOBORDER;
		TopP.addNext(btnNorthMM,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnNorthSSS = new mButton("SSS");
		btnNorthSSS.borderStyle = BDR_NOBORDER;
		TopP.addLast(btnNorthSSS,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));

		btnWest = new mButton("E");
		btnWest.borderStyle = BDR_NOBORDER;
		TopP.addNext(btnWest,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnEastDDD = new mButton("DDD°");
		btnEastDDD.borderStyle = BDR_NOBORDER;
		TopP.addNext(btnEastDDD,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnEastMM = new mButton("MM.");
		btnEastMM.borderStyle = BDR_NOBORDER;
		TopP.addNext(btnEastMM,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnEastSSS = new mButton("SSS");
		btnEastSSS.borderStyle = BDR_NOBORDER;
		TopP.addLast(btnEastSSS,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		TopSP.setControl(TopP);

		btn7 = new mButton("  7  ");
		BottomP.addNext(btn7,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn8 = new mButton(" 8 ");
		BottomP.addNext(btn8,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn9 = new mButton(" 9 ");
		BottomP.addLast(btn9,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn4 = new mButton("  4  ");
		BottomP.addNext(btn4,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn5 = new mButton(" 5 ");
		BottomP.addNext(btn5,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn6 = new mButton(" 6 ");
		BottomP.addLast(btn6,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn1 = new mButton("  1  ");
		BottomP.addNext(btn1,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn2 = new mButton(" 2 ");
		BottomP.addNext(btn2,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn3 = new mButton(" 3 ");
		BottomP.addLast(btn3,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn0 = new mButton("  0  ");
		BottomP.addNext(btn0,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnEsc = new mButton("ESC");
		BottomP.addNext(btnEsc,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnOk = new mButton("OK ");
		BottomP.addLast(btnOk,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnBear = new mButton(MyLocale.getMsg(1415,"bearing"));
		ExpertP.addNext(btnBear,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnGPS = new mButton(" GPS ");
		ExpertP.addNext(btnGPS,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnExpert = new mButton("Expert");
		ExpertP.addLast(btnExpert,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		//this.addLast(mainPanel,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));

		//bearing Pannel
		BearP.addNext(lblDistanc,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnDistanc = new mButton("        0");
		btnDistanc.borderStyle = BDR_NOBORDER;
		BearP.addNext(btnDistanc,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		BearP.addLast(lblDist,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		BearP.addNext(lblBearing,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnBearing = new mButton("        0");
		btnBearing.borderStyle = BDR_NOBORDER;
		BearP.addNext(btnBearing,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		BearP.addLast(lblBear,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));

		int inpFontSize = ( 4 * pref.fontSize ) / 2;
		Font inpNewFont = new Font("Helvetica", Font.PLAIN, inpFontSize );
		lblDistanc.setFont(inpNewFont);    lblBearing.setFont(inpNewFont);
		btnDistanc.setFont(inpNewFont);    btnBearing.setFont(inpNewFont);
		btnNorth.setFont(inpNewFont);      btnWest.setFont(inpNewFont);
		btnNorthDD.setFont(inpNewFont);    btnEastDDD.setFont(inpNewFont);
		btnNorthMM.setFont(inpNewFont);    btnEastMM.setFont(inpNewFont);
		btnNorthSSS.setFont(inpNewFont);   btnEastSSS.setFont(inpNewFont);
		lblBear.setFont(inpNewFont);	   lblDist.setFont(inpNewFont);
		btnBear.setFont(inpNewFont);       btnExpert.setFont(inpNewFont);
		btnGPS.setFont(inpNewFont);


		int btnFontSize = ( 3 * pref.fontSize ) / 2;
		Font btnNewFont = new Font("Helvetica", Font.PLAIN, btnFontSize );
		btn9.setFont(btnNewFont); btn8.setFont(btnNewFont);   btn7.setFont(btnNewFont);
		btn6.setFont(btnNewFont); btn5.setFont(btnNewFont);   btn4.setFont(btnNewFont);
		btn3.setFont(btnNewFont); btn2.setFont(btnNewFont);   btn1.setFont(btnNewFont);
		btn0.setFont(btnNewFont); btnEsc.setFont(btnNewFont); btnOk.setFont(btnNewFont);

		btnOk.takeFocus(ControlConstants.ByKeyboard);


	}

	private void disable(Control c) {c.modify(ControlConstants.Disabled,ControlConstants.TakesKeyFocus); }

	private void changeTextButton()
	{
		btnNorthDD.setText(strBufCoords.substring(POS_NDD, LEN_NDD+POS_NDD));
		btnNorthMM.setText(strBufCoords.substring(POS_NMM, LEN_NMM+POS_NMM));
		btnNorthSSS.setText(strBufCoords.substring(POS_NSSS, LEN_NSSS+POS_NSSS));
		btnEastDDD.setText(strBufCoords.substring(POS_EDDD, LEN_EDDD+POS_EDDD));
		btnEastMM.setText(strBufCoords.substring(POS_EMM, LEN_EMM+POS_EMM));
		btnEastSSS.setText(strBufCoords.substring(POS_ESSS, LEN_ESSS+POS_ESSS));
		disable(btnBear);
	}

	private void changeTextButtonBearing()
	{
		btnDistanc.setText(strBufDistanc.toString());
		btnBearing.setText(strBufBear.toString());
	}


	public void setCcords(CWPoint coords) {
		StringBuffer strBufTemp =new StringBuffer(30);
		strBufTemp.delete(0, strBufTemp.length());
		strBufCoords.delete(0, strBufCoords.length());

		if (coords.isValid()){

				strBufTemp.append(coords.toString(TransformCoordinates.CW));

				strBufCoords.append(strBufTemp.toString());
				btnNorth.setText(strBufTemp.substring(0, 1));
				btnNorthDD.setText(strBufTemp.substring(2, 5));
				btnNorthMM.setText(strBufTemp.substring(6,9));
				btnNorthSSS.setText(strBufTemp.substring(9,12));

				btnWest.setText(strBufTemp.substring(13,14));
				btnEastDDD.setText(strBufTemp.substring(15,19));
				btnEastMM.setText(strBufTemp.substring(20,23));
				btnEastSSS.setText(strBufTemp.substring(23,26));
		}
	}

	private void setTextBuffer(char cValue)
	{
		if(bBearingPanelOnTop){
			if(btnBearing.backGround  == ColorEdit){
				if(strBufBear.length()  < 3) strBufBear.insert(strBufBear.length(), cValue);
			    changeTextButtonBearing();
			}else{
				strBufDistanc.insert(strBufDistanc.length(), cValue);
			    changeTextButtonBearing();
			}

		}else{
		//'°' und '.' filtern
			if(iPosition == POS_NMM-2
				|| iPosition == POS_EMM-2
				|| iPosition == POS_ESSS-1
				|| iPosition == POS_NSSS-1
				|| iPosition == POS_ESSS-1
				|| iPosition == POS_EDDD-3)	iPosition++;

			if(iPosition == POS_NMM-1
				|| iPosition == POS_EMM-1
				|| iPosition == POS_EDDD-2)	iPosition++;

			if(iPosition == POS_EDDD-1)	iPosition++;

			if(iPosition >= strBufCoords.length()){
				iPosition = strBufCoords.length();
				return;
			}
				strBufCoords.setCharAt(iPosition, cValue);
			    iPosition++;
			    changeTextButton();
			    checkPosition();

		}
	}

	private void checkPosition()
	{
		btnNorthDD.backGround  = btnNorthMM.backGround = btnNorthSSS.backGround = ColorNormal;
		btnEastDDD.backGround  = btnEastSSS.backGround = btnEastMM.backGround = ColorNormal;
		if(iPosition >= POS_NDD && iPosition < POS_NMM -2) btnNorthDD.backGround = ColorEdit;
		if(iPosition >= POS_NMM-2 && iPosition < POS_NSSS -1) btnNorthMM.backGround = ColorEdit;
		if(iPosition >= POS_NSSS-1 && iPosition < POS_EDDD-3) btnNorthSSS.backGround = ColorEdit;
		if(iPosition >= POS_EDDD-3 && iPosition < POS_EMM -2) btnEastDDD.backGround  = ColorEdit;
		if(iPosition >= POS_EMM-2 && iPosition < POS_ESSS -1) btnEastMM.backGround  = ColorEdit;
		if(iPosition >= POS_ESSS-1) btnEastSSS.backGround  = ColorEdit;
		this.repaint();
	}

	/*
	 * set lat and lon by using coordinates in "CacheWolf" format
	 * @param coord  String of type N 49° 33.167 E 011° 21.608
	 * @param format only CWPoint.CW is supported
	 */
	public CWPoint getCoords(){
		String CoordsNorth, CoordsEast;
		CoordsNorth = btnNorth.getText()+ " " + btnNorthDD.getText()+ " " + btnNorthMM.getText()+ btnNorthSSS.getText();
		CoordsEast = btnWest.getText()+ " " + btnEastDDD.getText()+ " " + btnEastMM.getText()+ btnEastSSS.getText();
		CoordsBack.set(CoordsNorth + " " + CoordsEast , TransformCoordinates.CW);
		return CoordsBack;
	}


	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			// Button "E"
			if (ev.target == btnWest){
				if(btnWest.getText() == "E")
					btnWest.setText("W");
				else
					btnWest.setText("E");
			}
			// Button "N"
			if (ev.target == btnNorth){
				if(btnNorth.getText() == "N")
					btnNorth.setText("S");
				else
					btnNorth.setText("N");
			}


			// Button "Bearing"
			if (ev.target == btnBearing){
				btnBearing.backGround  = ColorEdit;
				btnDistanc.backGround = ColorNormal;
				strBufBear.delete(0, strBufBear.length());
				changeTextButtonBearing();
				this.repaint();
			}

			// Button "Distanc"
			if (ev.target == btnDistanc){
				btnBearing.backGround  = ColorNormal;
				btnDistanc.backGround = ColorEdit;
				strBufDistanc.delete(0, strBufDistanc.length());
				changeTextButtonBearing();
				this.repaint();
			}


			// Button "North DD"
			if (ev.target == btnNorthDD){
				iPosition=POS_NDD;
				checkPosition();
			}

			// Button "North MM"
			if (ev.target == btnNorthMM){
				iPosition=POS_NMM;
				checkPosition();
			}

			// Button "North SSS"
			if (ev.target == btnNorthSSS){
				iPosition=POS_NSSS;
				checkPosition();
			}

			// Button "East DDD"
			if (ev.target == btnEastDDD){
				iPosition=POS_EDDD;
				checkPosition();
			}

			// Button "East MM"
			if (ev.target == btnEastMM){
				iPosition=POS_EMM;
				checkPosition();
			}

			// Button "East SSS"
			if (ev.target == btnEastSSS){
				iPosition=POS_ESSS;
				checkPosition();
			}

			// Button "1"
			if (ev.target == btn1){
				setTextBuffer('1');
			}
			// Button "2"
			if (ev.target == btn2){
				setTextBuffer('2');
			}
			// Button "3"
			if (ev.target == btn3){
				setTextBuffer('3');
			}
			// Button "4"
			if (ev.target == btn4){
				setTextBuffer('4');
			}
			// Button "5"
			if (ev.target == btn5){
				setTextBuffer('5');
			}
			// Button "6"
			if (ev.target == btn6){
				setTextBuffer('6');
			}
			// Button "7"
			if (ev.target == btn7){
				setTextBuffer('7');
			}
			// Button "8"
			if (ev.target == btn8){
				setTextBuffer('8');
			}
			// Button "9"
			if (ev.target == btn9){
				setTextBuffer('9');
			}
			// Button "0"
			if (ev.target == btn0){
				setTextBuffer('0');
			}

			// Button "OK"
			if (ev.target == btnOk ){
				iPosition = 0;
				if(bBearingPanelOnTop){
					CoordsBear = this.getCoords();
					bd.degrees = Common.parseDouble(btnBearing.getText());
					bd.distance = Common.parseDouble(btnDistanc.getText());
					// only meters !!
					setCcords(CoordsBear.project(bd.degrees, bd.distance/1000.0));
					bBearingPanelOnTop = false;
				}
				this.close(IDOK);
			}
			// Button "ESC"
			if (ev.target == btnEsc ){
				iPosition = 0;
				this.close(0);
			}

			// Button "Bearing"
			if (ev.target == btnBear ){
				    disable(btnExpert);
				    disable(btnGPS);
				    TopSP.setControl(BearP,true);
				    bBearingPanelOnTop = true;

				}

			// Button "GPS"
			if (ev.target == btnGPS){
				Navigate nav=Global.mainTab.nav;
				if (nav.gpsPos.isValid()){
					setCcords(nav.gpsPos);
				}
			}


			// Button "Expert"
			if (ev.target == btnExpert ){
				CoordsScreen cs = new CoordsScreen();
				if (myNavigation.destination.isValid())	cs.setFields(myNavigation.destination, CoordsScreen.getLocalSystem(currFormatSel));
				else cs.setFields(new CWPoint(0,0), CoordsScreen.getLocalSystem(currFormatSel));
				if (cs.execute(null, CellConstants.TOP) == FormBase.IDOK){
					setCcords(cs.getCoords());
					this.close(IDOK);
				}

			}

		}
	}

}
