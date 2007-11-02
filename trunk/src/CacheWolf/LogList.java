package CacheWolf;

import ewe.util.Vector;

public class LogList {
	/** The Vector containing the Log objects 
	 * The list is always sorted in descending order */
	private Vector logList=new Vector(10);

	/** Construct an empty Log list */
	public LogList() {
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
	 public int countNotFoundLogs() {
		int countNoFoundLogs = 0;
		while(countNoFoundLogs < size() && countNoFoundLogs < 5){
			if(getLog(countNoFoundLogs).getIcon().equals("icon_sad")) {
				countNoFoundLogs++;
			}else break;
		}
		return countNoFoundLogs;
	 }
	

}
