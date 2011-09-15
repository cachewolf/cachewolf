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
import ewe.fx.Color;
import ewe.fx.DrawnIcon;
import ewe.fx.Insets;
import ewe.fx.Point;
import ewe.reflect.FieldTransfer;
import ewe.sys.Convert;
import ewe.sys.Locale;
import ewe.sys.Time;
import ewe.sys.TimeOfDay;
import ewe.sys.Vm;
import ewe.ui.CardPanel;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlEvent;
import ewe.ui.Editor;
import ewe.ui.InputPanelTableModel;
import ewe.ui.MultiPanel;
import ewe.ui.TableCellAttributes;
import ewe.ui.TableControl;
import ewe.ui.TableEvent;
import ewe.ui.TableModel;
import ewe.ui.UIConstants;
import ewe.ui.mButton;
import ewe.util.Vector;
import ewe.util.mString;


public class DateTimeChooser extends Editor {

	MultiPanel panels = new CardPanel();
	
	public int year;
	public String monthName;
	public int month;
	public int day;
	public int hour;
	public int minute;
	public String time;
	Time dateSet;
	
	public boolean autoAdvance = true;
	public boolean didAll = false;
	
	TableControl dayChooser, monthChooser, yearChooser,timeChooser;
	public Locale locale = Vm.getLocale();
	
	Control dayDisplay;
	Control monthDisplay;
	Control yearDisplay;
	Control timeDisplay;
	//Control minuteDisplay;

	Time getDate() {
		Time t = (Time)dateSet.getCopy();
		t.day = day;
		t.month = month;
		t.year = year;
		t.hour = hour;
		t.minute = minute;
		t.update();
		return t;
	}
	
	void addTable(TableControl tc,TableModel tm,String pName) {
		tc.setTableModel(tm);
		tc.setClickMode(true);
		panels.addItem(tc,pName,null);
		//tc.addListener(this);
	}
	
	boolean added = false;
	Control addTopData(CellPanel cp,String field) {
		Control dl = new mButton();//DumbLabel(1,10);
		cp.addNext(addField(dl,field),HSTRETCH,HFILL);
		dl.addListener(this);
		//dl.anchor = CENTER;
		dl.modify(DrawFlat,0);
		dl.borderStyle = BDR_OUTLINE|BF_TOP|BF_RIGHT|BF_SQUARE;
		//if (!added) dl.borderStyle |= BF_LEFT;
		added = true;
		dl.borderColor = Color.Black;
		return dl;
	}
	
	public String firstPanel = "monthName";
	
	public void reset(Time t) {
		setDate(t);
		didAll = false;
		newDate();
		panels.select(firstPanel);
	}
	
	public CellPanel addTopSection(CellPanel addTo,Control cp) {
		int IconSize;
		if (Vm.isMobile() && MyLocale.getScreenWidth() >= 400)
			IconSize = 30;
		else
			IconSize = 30;
		addTo.modify(DrawFlat,0);
		addTo.defaultTags.set(INSETS,new Insets(0,0,0,0));
		mButton b = new mButton();
		b.borderStyle = BDR_OUTLINE|BF_LEFT|BF_TOP|BF_RIGHT|BF_SQUARE;
		b.image = new DrawnIcon(DrawnIcon.CROSS,IconSize,IconSize,new Color(0x80,0,0));
		addTo.addNext(addField(b,"reject")).setCell(DONTSTRETCH);
		addTo.addNext(cp,HSTRETCH,HFILL);
		b = new mButton();
		b.borderStyle = BDR_OUTLINE|BF_TOP|BF_RIGHT|BF_SQUARE;
		b.image = new DrawnIcon(DrawnIcon.TICK,IconSize,IconSize,new Color(0,0x80,0));
		addTo.addNext(addField(b,"accept")).setCell(DONTSTRETCH);
		return addTo;
	}
	
	CardPanel cards = new CardPanel();
	
	public DateTimeChooser(Locale l) {
		if (l != null) locale = l;
		setDate(new Time());
		addLast(cards);
		CellPanel addTo = new CellPanel();
		cards.addItem(addTo,"full",null);
		
		CellPanel cp = new CellPanel();
		CellPanel top = new CellPanel();
		cp.equalWidths = true;
		firstPanel = "day";
		dayDisplay = addTopData(cp,"day");
		monthDisplay = addTopData(cp,"monthName");
		yearDisplay = addTopData(cp,"year");
		timeDisplay = addTopData(cp,"time");
		cp.endRow();
	
	
		addTopSection(top,cp);
		addTo.addLast(top).setCell(HSTRETCH);
		addTo.addLast((Control)panels);	
		
		addTable(dayChooser = new TableControl(),new dayChooserTableModel(locale),"day");
		addTable(monthChooser = new TableControl(),new monthChooserTableModel(locale),"monthName");
		addTable(yearChooser = new TableControl(),new yearChooserTableModel(),"year");
		addTable(timeChooser = new TableControl(),new timeChooserTableModel(),"time");
		
		// the following is already done in addTopSection?
/*	
		mButton b = new mButton();
		//b.borderStyle = BDR_OUTLINE|BF_LEFT|BF_TOP|BF_RIGHT|BF_SQUARE;
		b.image = new DrawnIcon(DrawnIcon.CROSS,10,10,new Color(0x80,0,0));
		addField(b,"reject");
		b.image = new DrawnIcon(DrawnIcon.CROSS,10,10,new Color(0x80,0,0));
		b = new mButton();
		b.image = new DrawnIcon(DrawnIcon.TICK,10,10,new Color(0,0x80,0));
		addField(b,"accept");
*/
		newDate();
		
	}
	
	public void fieldChanged(FieldTransfer ft,Editor ed) {
		newDate();
	}
	
	public void action(FieldTransfer ft,Editor ed) {
		String n = ft.fieldName;
		if (n.equals("day")||n.equals("monthName")||n.equals("year")||n.equals("time"))
			panels.select(n);
		if (n.equals("accept") || n.equals("entered"))
			exit(IDOK);
		if (n.equals("reject")) exit(IDCANCEL);
	}
	
	public void onControlEvent(ControlEvent ev) {
		if (ev instanceof TableEvent && ev.type == TableEvent.CELL_CLICKED){
			if (ev.target == dayChooser){
				day = Convert.toInt((String)((TableEvent)ev).cellData);
				newDate();
				toControls("day");
				if (autoAdvance && !didAll) {
					panels.select("monthName");
				}
			}else if (ev.target == monthChooser){
				month = (int)((ewe.sys.Long)(((TableEvent)ev).cellData)).value;
				newDate();
				monthName = locale.getString(Locale.SHORT_MONTH,month,0);
				toControls("monthName");
				if (autoAdvance && !didAll) {
					panels.select("year");
					didAll = true;
				}
			}else if (ev.target == yearChooser){
				String p = (String)((TableEvent)ev).cellData;
				int dec = year % 100;
				year -= dec;
				if (p.charAt(0) == 'C'){
					if (p.charAt(1) == '+') year = year+100+dec;
					else year = year-100+dec;
				}else{
					int val = Convert.toInt(p);
					if (val > 9) year = val*100;
					else {
						dec = dec*10+val;
						year += dec%100;
					}
				}
				newDate();
				toControls("year");
			}else if (ev.target == timeChooser){
				timeChooserTableModel tcm = (timeChooserTableModel)timeChooser.getTableModel();
				int newHour=tcm.getHourFor(((TableEvent)ev).row,((TableEvent)ev).col);
				if (newHour>-1) hour=newHour;
				int newMinute=tcm.getMinuteFor(((TableEvent)ev).row,((TableEvent)ev).col);
				if (newMinute>-1) minute=newMinute;
				time=MyLocale.formatLong(hour,"00")+":"+MyLocale.formatLong(minute,"00");
				tcm.set(hour,minute);
				timeChooser.repaintNow();
				newDate();
				toControls("time");
			}
		}
		if (ev.type == ControlEvent.CANCELLED) exit(IDCANCEL);
		else super.onControlEvent(ev);
	}
	
	
	public void setDate(Time t) {
		dateSet = t;
		if (!t.isValid()) t = new Time();
		Time.toString(t,t instanceof TimeOfDay ? t.getFormat() : locale.getString(Locale.SHORT_DATE_FORMAT,0,0),locale);
		day = t.day; month = t.month; year = t.year; hour=t.hour; minute=t.minute;
		time=MyLocale.formatLong(hour,"00")+":"+MyLocale.formatLong(minute,"00");
		monthName = locale.getString(Locale.SHORT_MONTH,t.month,0);
		toControls("day,month,year,monthName,time"); 
	}
	
	public void newDate() {
		Time t = new Time(day,month,year); t.hour=hour; t.minute=minute;
		Time t2 = t;
		if (!t2.isValid()) t2 = new Time(day = 1,month,year);
		if (!t2.isValid()) t2 = new Time();
		if (t2 != t){
			setDate(t2);
		}
		dayChooserTableModel dcm = (dayChooserTableModel)dayChooser.getTableModel();
		dcm.set(day,month,year);
		monthChooserTableModel mcm = (monthChooserTableModel)monthChooser.getTableModel();
		mcm.set(day,month,year);
		timeChooserTableModel tcm = (timeChooserTableModel)timeChooser.getTableModel();
		tcm.set(hour,minute);
		//dayChooser.repaintNow();
		//monthChooser.repaintNow();
	}

	public int getDay() {		
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public String getTime() {
		return time;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minHeight;
	}
} // DateTimeChooser


//-------------------------------------------------------
class monthChooserTableModel extends InputPanelTableModel {
//-------------------------------------------------------
	int chosenMonth;
	Locale locale = Vm.getLocale();

	monthChooserTableModel(Locale locale) {
		this.locale = locale;
		numRows = 3;
		numCols = 4;
		hasColumnHeaders = false;
		hasRowHeaders = false;
		fillToEqualHeights = fillToEqualWidths = true;
	}
	
	void set(int day,int month,int year) {
		int old = chosenMonth;
		chosenMonth = month;
		if (old != chosenMonth) refreshMonth(old);
	}

	int getMonthFor(int row,int cell) {
		if (row == -1 || cell == -1) return 0;
		int idx = row*4+cell;
		return idx+1;
	}

	Point getCellFor(int month,Point dest) {
		month--;
		int row = (month/4)%3;
		int col = month%4;
		return Point.unNull(dest).set(col,row);
	}

	void refreshMonth(int month) {
		Point p = getCellFor(month,null);
		table.repaintCell(p.y,p.x);
	}

	public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta) {
		ta.flat = true;
		ta = super.getCellAttributes(row,col,isSelected,ta);
		ta.borderStyle = fixBorder(UIConstants.BDR_OUTLINE|UIConstants.BF_BOTTOM|UIConstants.BF_RIGHT,row,col,true);
		if (getMonthFor(row,col) == chosenMonth && !isSelected){
			ta.fillColor = new Color(0x80,0x80,0xff);
		}
		return ta;
	}

	public Object getCellText(int row,int col) {
		return locale.getString(Locale.SHORT_MONTH,getMonthFor(row,col),0);
	}

	public Object getCellData(int row,int col) {
		int month = getMonthFor(row,col);
		return new ewe.sys.Long().set(month);
	}
}

//-------------------------------------------------------
class dayChooserTableModel extends InputPanelTableModel {
//-------------------------------------------------------
	
	Vector days = new Vector();
	Locale locale = Vm.getLocale();
	int firstDayIndex = 0;
	int numDays = 28;
	
	int chosenDay = 0;
	
	
	void set(int day,int month,int year) {
		numDays = Time.numberOfDays(month,year);
		Time t = new Time(1,month,year);
		firstDayIndex = Time.indexOfDayInWeek(t.dayOfWeek,locale)-1;
		int oldDay = chosenDay;
		chosenDay = day;
		if (oldDay != chosenDay) refreshDay(oldDay);
	}
	
	int getDayFor(int row,int cell) {
		if (row == -1 || cell == -1) return 0;
		int idx = row*7+cell;
		if (idx >= firstDayIndex && idx < firstDayIndex+numDays) return idx-firstDayIndex+1;
		idx = idx+35;
		if (idx >= firstDayIndex && idx < firstDayIndex+numDays) return idx-firstDayIndex+1;
		return 0;
	}
	
	Point getCellFor(int day,Point dest) {
		day += firstDayIndex-1;
		int row = (day/7)%5;
		int col = day%7;
		return Point.unNull(dest).set(col,row);
	}
	
	void refreshDay(int day) {
		Point p = getCellFor(day,null);
		table.repaintCell(p.y,p.x);
	}
	
	public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta) {
		ta.flat = true;
		ta = super.getCellAttributes(row,col,isSelected,ta);
		ta.borderStyle = fixBorder(UIConstants.BDR_OUTLINE|UIConstants.BF_BOTTOM|UIConstants.BF_RIGHT,row,col,true);
		if (getDayFor(row,col) == chosenDay && !isSelected){
			ta.fillColor = new Color(0x80,0x80,0xff);
		}
		return ta;
	}
	
	dayChooserTableModel(Locale l) {
		this.locale = l;
		numRows = 5;
		numCols = 7;
		hasColumnHeaders = true;
		hasRowHeaders = false;
		for (int i = 1; i<=7; i++) days.add(l.getString(Locale.SHORT_DAY_OF_WEEK,i,0));
	
		fillToEqualHeights = fillToEqualWidths = true;
	}
	
	public boolean canSelect(int row,int col) {
		return (getDayFor(row,col) != 0);
	}
	
	public Object getCellData(int row,int col) {
		if (row == -1) return days.get(col);
		else {
			int val = getDayFor(row,col);
			if (val == 0) return null;
			return Convert.toString(val);
		}
	}
	
}


//-------------------------------------------------------
class yearChooserTableModel extends InputPanelTableModel {
//-------------------------------------------------------
	
	yearChooserTableModel() {
		numRows = 4;
		numCols = 4;
		hasColumnHeaders = false;
		hasRowHeaders = false;
	}
	String [] all = mString.split("19xx|7|8|9|20xx|4|5|6|21xx|1|2|3|18xx|0|C+|C-");

	public Object getCellText(int row,int col) {
		if (row >= 0 && col >= 0) 
			return all[col+row*4];
		return null;
	}
	
	public Object getCellData(int row,int col) {
		String str = (String)getCellText(row,col);
		if (str.length() > 2) str = str.substring(0,2);
		return str;
	}
	
	public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta) {
		ta.flat = true;
		ta = super.getCellAttributes(row,col,isSelected,ta);
		return ta;
	}

}

//-------------------------------------------------------
class timeChooserTableModel extends InputPanelTableModel {
//	-------------------------------------------------------
	int chosenHour=-2,chosenMinute=-2;
	
	timeChooserTableModel() {
		numRows = 6;
		numCols = 15;
		hasColumnHeaders = false;
		hasRowHeaders = false;
	}
	void set(int hour,int minute) {
		int old = chosenHour;
		chosenHour = hour;
		if (old != chosenHour) refreshHour(old);
		old = chosenMinute;
		chosenMinute = minute;
		if (old != chosenMinute) refreshMinute(old);
	}

	int getHourFor(int row,int cell) {
		if (row == -1 || cell == -1) return 0;
		if (cell>=4) return -1;
		return row*4+cell;
	}

	Point getCellForHour(int hour,Point dest) {
		int row = (hour/4)%6;
		int col = hour%4;
		return Point.unNull(dest).set(col,row);
	}

	void refreshHour(int hour) {
		Point p = getCellForHour(hour,null);
		table.repaintCell(p.y,p.x);
	}

	int getMinuteFor(int row,int cell) {
		if (row == -1 || cell == -1) return 0;
		if (cell<=4) return -1;
		return row*10+cell-5;
	}

	Point getCellForMinute(int minute,Point dest) {
		int row = (minute/10)%6;
		int col = minute%10;
		return Point.unNull(dest).set(col,row);
	}

	void refreshMinute(int minute) {
		Point p = getCellForMinute(minute,null);
		table.repaintCell(p.y,p.x);
	}


	public Object getCellText(int row,int col) {
		if (row >= 0 && col >= 0) 
			if (col<4) 
				return Convert.toString(row*4+col);
			else if (col==4) 
				return "";
			else 
				return  Convert.toString(row*10+col-5);
		return null;
	}

	public Object getCellData(int row,int col) {
		return getCellText(row,col);
	}

	public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta) {
		ta.flat = true;
		if (col==4) {
			ta = super.getCellAttributes(row,col,isSelected,ta);
			ta.fillColor=Color.LightGray;
		} else
			ta = super.getCellAttributes(row,col,isSelected,ta);
		if (getHourFor(row,col) == chosenHour && !isSelected){
			ta.fillColor = new Color(0x80,0x80,0xff);
		}
		if (getMinuteFor(row,col) == chosenMinute && !isSelected){
			ta.fillColor = new Color(0x80,0x80,0xff);
		}
		return ta;
	}

	public int calculateColWidth(int col) {
		if (col==4)
        	return 4;
        else if (col<4)
        	return super.calculateColWidth(col)+2;
        else	
        	return super.calculateColWidth(col);
	}
}




