package eveWorkArounds;

import eve.sys.Time;

public class TimeWorkArounds extends Time {
	public void parse(String dateValue,String dateFormat) throws IllegalArgumentException {
		if (dateFormat.equals("yyyyMMddHHmmss")) {
			StringBuffer tmp = new StringBuffer(dateValue.length()+5);
			tmp.append(dateValue.substring(0, 4));
			tmp.append(" ").append(dateValue.substring(4, 6));
			tmp.append(" ").append(dateValue.substring(6, 8));
			tmp.append(" ").append(dateValue.substring(8, 10));
			tmp.append(" ").append(dateValue.substring(10,12));
			tmp.append(" ").append(dateValue.substring(12,14));
			super.parse(tmp.toString(), dateFormat);
		} else {
			super.parse(dateValue, dateFormat);
		}
	}

}
