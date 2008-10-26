package cachewolf;

import eve.ui.*;
import eve.ui.formatted.TextDisplay;

import cachewolf.utils.Common;


import eve.fx.Dimension;
import eve.fx.FontMetrics;
import eve.sys.Event;
import eve.ui.event.ControlEvent;

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

	CheckBox chkDMM, chkDMS, chkDD, chkUTM, chkGK;
	CheckBoxGroup chkFormat = new CheckBoxGroup();
	Choice chcDistUnit;
	Input inpBearing, inpDistance;
	TextDisplay txtOutput;
	Button btnCalc, btnClear, btnSave, btnGoto;
	BearingDistance bd = new BearingDistance();
	CWPoint coordInp = new CWPoint();
	CWPoint coordOut = new CWPoint();
	// Needed for creation of new waypoint
	MainTab mainT;
	// different panels to avoid spanning
	CellPanel TopP = new CellPanel();
	CellPanel BottomP = new CellPanel();
	String lastWaypoint = "";

	int currFormat;
	Button btnChangeLatLon;

	public CalcPanel()	{
		mainT = Global.mainTab;

		TopP.addNext(chkDD =new CheckBox("d.d°"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkDMM =new CheckBox("d°m.m\'"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkDMS =new CheckBox("d°m\'s\""),CellConstants.DONTSTRETCH,CellConstants.WEST);
		TopP.addNext(chkUTM =new CheckBox("UTM"),CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addLast(chkGK =new CheckBox("GK"),CellConstants.DONTSTRETCH, CellConstants.WEST);

		chkDD.setGroup(chkFormat);
		chkDMM.setGroup(chkFormat);
		chkDMS.setGroup(chkFormat);
		chkUTM.setGroup(chkFormat);
		chkGK.setGroup(chkFormat);
		chkFormat.setInt(CWPoint.DMM);
		btnChangeLatLon=new Button();
		TopP.addLast(btnChangeLatLon,CellConstants.HSTRETCH, (CellConstants.HFILL|CellConstants.WEST));
		//inpBearing and direction, unit for inpDistance
		BottomP.addNext(new Label(MyLocale.getMsg(1403,"Bearing")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addLast(new Label(MyLocale.getMsg(1404,"Distance")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addNext(inpBearing = new Input(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		inpBearing.setText("0");
		BottomP.addNext(inpDistance = new Input(),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		inpDistance.setText("0");
		// Check for narrow screen and reduce width of fields to avoid horizontal scroll panel
		if (MyLocale.getScreenWidth()<=240) {
			FontMetrics fm = getFontMetrics(inpBearing.getFont());
			inpBearing.setPreferredSize(fm.getTextWidth("99999999"),fm.getHeight()*4/3);
			inpDistance.setPreferredSize(fm.getTextWidth("99999999"),fm.getHeight()*4/3);
		}
		BottomP.addLast(chcDistUnit = new Choice(new String[]{"m", "km",MyLocale.getMsg(1407,"steps"), MyLocale.getMsg(1408,"feet"), MyLocale.getMsg(1409,"yards"), MyLocale.getMsg(1410,"miles")},0),CellConstants.DONTSTRETCH, (CellConstants.HFILL|CellConstants.WEST)).setTag(CellConstants.TAG_INSETS,new eve.fx.Insets(0,2,0,0));
		chcDistUnit.setInt(0);

		// Buttons for calc and save
		BottomP.addNext(btnCalc = new Button("Calc"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addNext(btnClear = new Button("Clear"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addNext(btnGoto = new Button("Goto"),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		BottomP.addLast(btnSave = new Button(MyLocale.getMsg(311,"Create Waypoint")),CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));

		// Output
		txtOutput = new TextDisplay(3,1); // Need to limit size for small screens
		ScrollBarPanel sbp = new MyScrollBarPanel(txtOutput);
		BottomP.addLast(sbp.setTag(Control.TAG_SPAN, new Dimension(4,1)),CellConstants.STRETCH, (CellConstants.FILL|CellConstants.WEST));

		//add Panels
		this.addLast(TopP,CellConstants.HSTRETCH, CellConstants.WEST);//.setTag(TAG_SPAN,new Dimension(4,1));
		this.addLast(BottomP,CellConstants.VSTRETCH, CellConstants.VFILL|CellConstants.WEST); //.setTag(TAG_SPAN,new Dimension(4,1));

	}

	public void readFields(CWPoint coords, BearingDistance degKm, int format){
		coords.set(btnChangeLatLon.getText());
		currFormat = chkFormat.getSelectedIndex();
		degKm.degrees = Common.parseDouble(inpBearing.getText());

		double rawDistance = Common.parseDouble(inpDistance.getText());
		switch ( chcDistUnit.getInt() ) {
		case 0:
			// meter
			degKm.distance = rawDistance / 1000.0;
			break;
		case 1:
			// kilometer
			degKm.distance = rawDistance;
			break;
		case 2:
			// steps
			degKm.distance = rawDistance * 0.00063;
			break;
		case 3:
			// feet
			degKm.distance = rawDistance * 0.0003048;
			break;
		case 4:
			// yards
			degKm.distance = rawDistance * 0.0009144;
			break;
		case 5:
			// miles
			degKm.distance = rawDistance * 1.609344;
			break;
		default:
			// meter
			degKm.distance = rawDistance / 1000.0;
			break;
		}
		return;
	}

	// ch must be not null
	public void setFields(CacheHolder ch){
        if ( !ch.wayPoint.equalsIgnoreCase(lastWaypoint) ) {
            lastWaypoint = ch.wayPoint;
            if (ch.pos.isValid()) {
                    inpBearing.setText("0");
                    inpDistance.setText("0");

                    currFormat = CWPoint.DMM;
                    if (ch.latLon.length()== 0) coordInp.set(0,0);
                    else coordInp.set(ch.latLon, CWPoint.CW);
                    setFields(coordInp, CWPoint.DMM);
            }
    }
	}


	public void setFields(CWPoint coords, int format) {
		if (format == CWPoint.CW) format = CWPoint.DMM;
		btnChangeLatLon.setText(coords.toString(format));
		chkFormat.selectIndex(format);
	}


	public void onEvent(Event ev){

		//Vm.debug(ev.toString());
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == chkFormat){
				readFields(coordInp,bd, currFormat);
				setFields(coordInp, currFormat);
				this.repaintNow();
			}

			if (ev.target == btnCalc){
				readFields(coordInp, bd, currFormat);
				coordOut = coordInp.project(bd.degrees, bd.distance);
				txtOutput.appendText(coordOut.toString(currFormat)+ "\n",true);
			}
			if (ev.target == btnClear){
				txtOutput.setText("");
			}
			if (ev.target == btnSave){
				CacheHolder ch = new CacheHolder();
				readFields(coordInp, bd, currFormat);
				coordOut = coordInp.project(bd.degrees, bd.distance);
				ch.latLon = coordOut.toString();
				ch.pos.set(coordOut);
				ch.type = 51; // see CacheType.GC_AW_STAGE_OF_MULTI // TODO unfertig
				mainT.newWaypoint(ch);
			}

			if (ev.target == btnGoto){
				readFields(coordInp, bd, currFormat);
				coordOut = coordInp.project(bd.degrees, bd.distance);
				mainT.gotoPoint(coordOut);
			}
			if (ev.target == btnChangeLatLon){
				CoordsScreen cs = new CoordsScreen();
				readFields(coordInp, bd, currFormat);
				cs.setFields(coordInp,currFormat);
				if (cs.execute()== CoordsScreen.IDOK){
					btnChangeLatLon.setText(cs.getCoords().toString(currFormat));
					coordInp.set(cs.getCoords());
				}
			}

		}
		super.onEvent(ev);
	}
}
