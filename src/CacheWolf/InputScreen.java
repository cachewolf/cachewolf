package CacheWolf;

import ewe.ui.*;
import ewe.fx.Font;

/**
*	Class for entering coordinates<br>
*	Class IDs 1400 and 600 (same as calc panel and preferences screen)<br>
*/


public class InputScreen extends Form {

	String sText = "";
	mButton btn9, btn8, btn7, btn6, btn5, btn4, btn3, btn2, btn1, btn0, btnPoint, btnOk, btnDEL, btnDELAll;
	mInput inpText = new mInput();
	CellPanel mainPanel = new CellPanel();
	mLabel lblValueShow = new mLabel("0.0");
	mLabel lblTextShow = new mLabel("Info");


	public InputScreen(mInput inpAnz, String sAnz)
	{
		inpText = inpAnz;
		lblValueShow.setText(inpText.getText());
		lblTextShow.setText(sAnz);
		//inpText.setText(sText);
		InitInputScreen();
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
		}
		else{
			// small screens: fixed size
			if (sh>240)
				this.setPreferredSize(240,260);
			else
				this.setPreferredSize(240,240);
		}

		this.setTitle("InputConsole");
		mainPanel.addLast(lblValueShow,CellConstants.DONTSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		mainPanel.addLast(lblTextShow,CellConstants.DONTSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn7 = new mButton(" 7 ");
		mainPanel.addNext(btn7,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn8 = new mButton(" 8 ");
		mainPanel.addNext(btn8,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn9 = new mButton(" 9 ");
		mainPanel.addNext(btn9,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnDEL = new mButton("DEL");
		mainPanel.addLast(btnDEL,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn4 = new mButton(" 4 ");
		mainPanel.addNext(btn4,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn5 = new mButton(" 5 ");
		mainPanel.addNext(btn5,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn6 = new mButton(" 6 ");
		mainPanel.addNext(btn6,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnDELAll = new mButton("DEL All");
		mainPanel.addLast(btnDELAll,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn1 = new mButton(" 1 ");
		mainPanel.addNext(btn1,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn2 = new mButton(" 2 ");
		mainPanel.addNext(btn2,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn3 = new mButton(" 3 ");
		mainPanel.addLast(btn3,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btn0 = new mButton(" 0 ");
		mainPanel.addNext(btn0,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnPoint = new mButton(" . ");
		mainPanel.addNext(btnPoint,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnOk = new mButton("OK");
		mainPanel.addLast(btnOk,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));

		this.addLast(mainPanel,CellConstants.HSTRETCH | CellConstants.VSTRETCH, (CellConstants.HFILL| CellConstants.VFILL));
		btnOk.takeFocus(ControlConstants.ByRequest);

		int inpFontSize = ( 6 * pref.fontSize ) / 2;
		Font inpNewFont = new Font("Helvetica", Font.PLAIN, inpFontSize );
		lblValueShow.setFont(inpNewFont);

		int btnFontSize = ( 3 * pref.fontSize ) / 2;
		Font btnNewFont = new Font("Helvetica", Font.PLAIN, btnFontSize );
		lblTextShow.setFont(btnNewFont);
		btnPoint.setFont(btnNewFont);
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
		btnOk.setFont(btnNewFont);
		btnDEL.setFont(btnNewFont);
		btnDELAll.setFont(btnNewFont);

	}

	public void onEvent(Event ev){
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btn1){
			   sText = lblValueShow.getText();
			   sText += "1";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn2){
			   sText = lblValueShow.getText();
			   sText += "2";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn3){
			   sText = lblValueShow.getText();
			   sText += "3";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn4){
			   sText = lblValueShow.getText();
			   sText += "4";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn5){
			   sText = lblValueShow.getText();
			   sText += "5";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn6){
			   sText = lblValueShow.getText();
			   sText += "6";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn7){
			   sText = lblValueShow.getText();
			   sText += "7";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn8){
			   sText = lblValueShow.getText();
			   sText += "8";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn9){
			   sText = lblValueShow.getText();
			   sText += "9";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btn0){
			   sText = lblValueShow.getText();
			   sText += "0";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btnPoint){
			   sText = lblValueShow.getText();
			   sText += ".";
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btnOk ){
				inpText.setText(lblValueShow.getText());
				this.close(0);
			}

			if (ev.target == btnDEL){
			   sText = lblValueShow.getText();
			   if(sText != "")
				sText = sText.substring(0, sText.length()-1);
			   //inpText.setText(sText);
			   lblValueShow.setText(sText);
			}

			if (ev.target == btnDELAll){
			   //inpText.setText("");
				lblValueShow.setText("");
			}
		}
	}

}
