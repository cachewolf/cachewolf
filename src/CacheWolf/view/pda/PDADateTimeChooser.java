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
package CacheWolf.view.pda;

import CacheWolf.utils.MyLocale;
import ewe.fx.Color;
import ewe.fx.Font;
import ewe.fx.Rect;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Form;
import ewe.ui.Gui;
import ewe.ui.Panel;
import ewe.ui.mButton;
import ewe.ui.mLabel;

public class PDADateTimeChooser extends Form  {

	private int year; 
	private int month; 
	private int day; 

	private int hour; 
	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	private int minute; 

	private	mLabel lbDay;
	private mLabel lbMonth;
	private mLabel lbYear;
	private mLabel lbHour;
	private mLabel lbMinute;
	private mLabel lbSep;
	
	private mButton btDayUp;
	private mButton btMonthUp;
	private mButton btYearUp;
	private mButton btHourUp;
	private mButton btMinuteUp;

	private mButton btDayDown;
	private mButton btMonthDown;
	private mButton btYearDown;
	private mButton btHourDown;
	private mButton btMinuteDown;

	private mButton btSet;
	private mButton btTime;
	private mButton btCalendar;
	private mButton btCancel;

	public PDADateTimeChooser (){
		int screenWidth = MyLocale.getScreenWidth();
		String string = "SET TIME CANCEL";
		int fontsize =  screenWidth/string.length();
		Rect size = Gui.getSize(getFontMetrics(), string, 5,0);
		while (size.width < screenWidth) {
			fontsize += 5;
			font = new Font(getFont().getName(), Font.BOLD,fontsize);
			size = Gui.getSize(getFontMetrics(), string, 5, 0);
		}

		backGround = Color.White;

		lbDay = new mLabel("");
		lbDay.anchor=mLabel.CENTER;
		lbMonth = new mLabel ("");
		lbMonth.anchor=mLabel.CENTER;
		lbYear = new mLabel ("");
		lbYear.anchor=mLabel.CENTER;
		lbHour = new mLabel ("HH");
		lbHour.anchor=mLabel.CENTER;
		lbSep = new mLabel(":");
		lbMinute = new mLabel ("MM");
		lbMinute.anchor=mLabel.CENTER;

		btDayUp = new mButton (" ^^ ");btDayUp.backGround=Color.LightBlue;
		btMonthUp = new mButton (" ^^ ");btMonthUp.backGround=Color.LightBlue;
		btYearUp = new mButton ("  ^^  ");btYearUp.backGround=Color.LightBlue;
		btHourUp = new mButton ("  ^^  ");btHourUp.backGround=Color.LightBlue;
		btMinuteUp = new mButton ("  ^^  ");btMinuteUp.backGround=Color.LightBlue;

		btDayDown = new mButton ("vv");btDayDown.backGround=Color.LightBlue;
		btMonthDown = new mButton ("vv");btMonthDown.backGround=Color.LightBlue;
		btYearDown = new mButton (" vv ");btYearDown.backGround=Color.LightBlue;
		btHourDown = new mButton (" vv ");btHourDown.backGround=Color.LightBlue;
		btMinuteDown = new mButton (" vv ");btMinuteDown.backGround=Color.LightBlue;
		
		btSet = new mButton ("Set");btSet.backGround=Color.Sand;
		btTime = new mButton ("Time");btTime.backGround=Color.Sand;
		btCalendar  = new mButton("Cal");btCalendar.backGround=Color.Sand;
		btCancel = new mButton ("Cancel");btCancel.backGround=Color.Sand;

		layoutCalendar();
		
		addListener(this);
	}

	
	private void layoutCalendar() {
		removeAll();
		addNext (lbDay,HSTRETCH,HFILL);
		addNext (lbMonth,HSTRETCH,HFILL);
		addLast (lbYear,HSTRETCH,HFILL);

		addNext (btDayUp,HSTRETCH,HFILL);
		addNext (btMonthUp,HSTRETCH,HFILL);
		addLast (btYearUp,HSTRETCH,HFILL);

		addNext (btDayDown,HSTRETCH,HFILL);
		addNext (btMonthDown,HSTRETCH,HFILL);
		addLast (btYearDown,HSTRETCH,HFILL);
		
		addNext (btSet, HSTRETCH,CENTER|HFILL);
		addNext (btTime, HSTRETCH,CENTER|HFILL);
		addLast (btCancel, HSTRETCH,CENTER|HFILL);
	}
	
	private void layoutTime() {
		removeAll ();
		Panel p = new CellPanel();
		p.addNext (lbHour, HSTRETCH,HFILL);
		p.addNext (lbSep,DONTSTRETCH,DONTFILL);
		p.addLast (lbMinute, HSTRETCH,CENTER);
		p.addNext (btHourUp, HSTRETCH,HFILL);
		p.addNext(new mLabel(""));
		p.addLast (btMinuteUp, HSTRETCH,HFILL);
		p.addNext (btHourDown, HSTRETCH,HFILL);
		p.addNext(new mLabel(""));
		p.addLast (btMinuteDown, HSTRETCH,HFILL);
		
		addLast (p, HSTRETCH,HFILL);

		Panel p1 = new CellPanel();
		p1.addNext(btSet, HSTRETCH,CENTER|HFILL);
		p1.addNext(btCalendar, HSTRETCH,CENTER|HFILL);
		p1.addLast(btCancel, HSTRETCH,CENTER|HFILL);
		addLast (p1, HSTRETCH,CENTER|HFILL);
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public String getTime() {
		return Convert.toString(year) + "-" + MyLocale.formatLong(month, "00") + "-"
		+ MyLocale.formatLong(day, "00") + " " + MyLocale.formatLong(hour, "00") +
		":"+MyLocale.formatLong(minute, "00");
	}

	public int getYear() {
		return year;
	}

	public void reset(Time t) {
		year = t.year;
		month = t.month;
		day = t.day;
		hour = t.hour;
		minute=t.minute;

		lbDay.setText(Integer.toString(day));
		lbMonth.setText(Integer.toString(month));
		lbYear.setText(Integer.toString(year));
		lbHour.setText(Integer.toString(hour));
		lbMinute.setText(MyLocale.formatLong(minute, "00"));
	}
	
	public void onControlEvent(ControlEvent ev) {
		switch (ev.type) {
		case ControlEvent.PRESSED:
			if (ev.target == btDayUp && day < 31) {
				day++;
				lbDay.setText(Integer.toString(day));
			}
			else if (ev.target == btDayDown && day > 1){
				day--;
				lbDay.setText(Integer.toString(day));
			}
			
			else if (ev.target == btMonthUp && month < 12){
				month++;
				lbMonth.setText(Integer.toString(month));
			}
			else if (ev.target == btMonthDown && month > 1){
				month--;
				lbMonth.setText(Integer.toString(month));
			}

			else if (ev.target == btYearUp){
				year++;
				lbYear.setText(Integer.toString(year));
			}
			else if (ev.target == btYearDown){
				year--;
				lbYear.setText(Integer.toString(year));
			}
			
			else if (ev.target == btHourUp && hour < 23){
				hour++;
				lbHour.setText(Integer.toString(hour));
			}
			else if (ev.target == btHourDown && hour > 0){
				hour--;
				lbHour.setText(Integer.toString(hour));
			}
			else if (ev.target == btMinuteUp && minute < 59){
				minute++;
				lbMinute.setText(MyLocale.formatLong(minute, "00"));
			}
			else if (ev.target == btMinuteDown && minute > 0){
				minute--;
				lbMinute.setText(MyLocale.formatLong(minute, "00"));
			}

			else if (ev.target == btSet){
				exit(IDOK);
			}
			else if (ev.target == btTime){
				Gui.flashMessage("Uhrzeit anzeigen", 1000, this, 0);
				made=false;
				layoutTime();
				make(true);
				relayoutMe(true);
				reShow(0,0,width,height);
			}
			else if (ev.target == btCalendar){
				Gui.flashMessage("Calendar anzeigen!", 1000, this, 0);
				made=false;
				layoutCalendar();
				make(true);
				relayoutMe(true);
				reShow(0,0,width,height);				
			}
			else if (ev.target == btCancel){
				exit(IDNO);
			}
			
		}
		super.onControlEvent(ev);
	}
}
