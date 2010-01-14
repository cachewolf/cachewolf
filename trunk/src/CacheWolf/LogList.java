package CacheWolf;

import ewe.util.Vector;

public class LogList {
	/** The Vector containing the Log objects 
	 * The list is always sorted in descending order */
	private Vector logList=new Vector(10);
	private static final StringBuffer buffer = new StringBuffer();

	/** Construct an empty Log list */
	public LogList() { // Public constructor
	}
	
	/** Get the Log at a certain position in the list */
	public Log getLog(int i) {
		return (Log) logList.elementAt(i);
	}
	
	/** Return the size of the list */
	public int size() {
		return logList.size();
	}
	
	/** Clear the Log list */
	public void clear() {
		logList.clear();
	}

	/** Add a Log to the list */
	public void add(Log log) {
		if (log.getIcon()!=null) logList.add(log); // Don't add invalid logs
	}
	
	/** Remove a Log from the list */
	public void remove(int i) {
		logList.removeElementAt(i);
	}

	/** Replace a Log in the list */
	public void replace(int i, Log log) {
		logList.set(i, log);
	}
	
	/** Merge a log into the list at the appropriate position
	 * @param newLog
	 * @return the position where the log was placed or -1 if it is already in the list
	 */ 
	 
	public int merge(Log newLog) {
		String newDate=newLog.getDate();
		int size=size();
		int i;
		for (i=0; i<size; i++) {
			 int comp=newDate.compareTo(((Log) logList.elementAt(i)).getDate());
			 if (comp>0) {
				 logList.insertElementAt(newLog, i);
				 return i;
			 }
			 if (comp==0) break;
		}
		// Now i points to the first log with same date as the new log or i==size()
		if (i==size) {
			add(newLog);
			return size;
		}
		int firstLog=i;
		// Check whether we have any logs with same date by same user
		String newLogger=newLog.getLogger();
		String newIcon=newLog.getIcon();
		while (i<size &&  newDate.equals(((Log) logList.elementAt(i)).getDate())) {
			Log log=(Log) logList.elementAt(i);
			if (log.getLogger().equals(newLogger) &&
				log.getIcon().equals(newIcon)) {
				// Has the log message changed vs. the one we have in cache.xml?
				if (!log.getMessage().equals(newLog.getMessage())) {
					replace(i,newLog);
					return i;
				} else
					return -1; // Log already in list
			}
			i++;
		}
		if (i==size) {
			add(newLog);
			return i;
		} else {
			logList.insertElementAt(newLog, firstLog);
			return firstLog;
		}
	}

	 /**
	  *  Count the number of not-found logs
	  */
	public byte countNotFoundLogs() {
		byte countNoFoundLogs = 0;
		int currentLog = 0;
		String currentIcon;
		while(countNoFoundLogs < size() && countNoFoundLogs < 5){
			currentIcon = getLog(currentLog).getIcon();
			if(currentIcon.startsWith("icon_sad")) {
				countNoFoundLogs++;
			}else if (currentIcon.startsWith("icon_smile") || currentIcon.startsWith("icon_camera") || currentIcon.startsWith("icon_attended") || currentIcon.startsWith("icon_rsvp")) {
				break;
			}
			currentLog++;
		}
		return countNoFoundLogs;
	}

	 /** only valid after calling calcRecommendations() */
	 int numRecommended = 0;
	 /** only valid after calling calcRecommendations() */
	 int foundsSinceRecommendation = 0;
	 /** only valid after calling calcRecommendations() */
	 int recommendationRating = 0;
	 
	 /**
	  * call this to 
	  *
	  */
	 public void calcRecommendations() {
		 numRecommended = 0;
		 foundsSinceRecommendation = 0;
		 Log l;
		 int s = size();
		 int i;
		 for (i= 0 ; i < s; i++){
			 l = getLog(i);
			 if (l.getDate().compareTo("2007-01-14") < 0) break; // this is the date when the recommendation system was introdueced in opencaching.de see: http://www.geoclub.de/viewtopic.php?t=14901&highlight=formel
			 if (l.isRecomended()) numRecommended++;
			 if (l.isFoundLog()) foundsSinceRecommendation++; 
		 }
		 recommendationRating = getScore(numRecommended, foundsSinceRecommendation);
	 }
	 
	 public static int getScore(int numrecommends, int numfoundlogs) {
		 return Math.round((((float)numrecommends * (float)numrecommends +1f ) / (numfoundlogs / 10f +1f))*100f);
	 }
	 
	 /**
	  * Returns a simple concatenation of all Log texts of the list. Intended for text search in 
	  * Logs.
	 * @return All log messages
	 */
	public String allMessages() {
		 buffer.setLength(0);
		 for (int i=0; i<logList.size(); i++) {
			 buffer.append(((Log)logList.get(i)).getMessage());
		 }
		 return buffer.toString();
	 }

	/**
	 * trim down number of log to maximum number user wants to keep in database
	 * @return number of removed logs
	 */
	public int purgeLogs() {
		int maxKeep = Global.getPref().maxLogsToKeep;
		boolean keepOwn = Global.getPref().alwaysKeepOwnLogs;
		int purgedLogs = 0;
		for (int i = logList.size(); i > maxKeep; i--) {
			if (!(keepOwn && getLog(i-1).isOwnLog())) {
				this.remove(i-1);
				purgedLogs++;
			}
		}
		return purgedLogs;
	}
	
}
