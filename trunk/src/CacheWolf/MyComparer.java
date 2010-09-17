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
import ewe.util.Comparer;
import ewe.util.Vector;

/**
*	This class handles the sorting for most of the sorting tasks. If a cache is 
*	to be displayed in the table or not is handled in the table model
*	@see MyTableModel
*	@see DistComparer
*/
public class MyComparer implements Comparer{
	Vector cacheDB;
	
	public MyComparer(CacheDB cacheDB, int colToCompare, int visibleSize){
		//visibleSize=Global.mainTab.tbP.myMod.numRows;
		if (visibleSize<2) return;
		for (int i=visibleSize; i<cacheDB.size(); i++) {
			CacheHolder ch=cacheDB.get(i);
			ch.sort="\uFFFF";
		}
		if (colToCompare==1) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=String.valueOf(ch.getType());
			}
		} else if (colToCompare==2) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=String.valueOf(ch.getHard());
			}
		} else if (colToCompare==3) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=String.valueOf(ch.getTerrain());
			}
		} else if (colToCompare==4) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=ch.getWayPoint().toUpperCase();
			}
		} else if (colToCompare==5) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=ch.getCacheName().trim().toLowerCase();
			}
		} else if (colToCompare==6) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=ch.getLatLon();
			}
		} else if (colToCompare==7) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=ch.getCacheOwner().toLowerCase();
			}
		} else if (colToCompare==8) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=ch.getDateHidden();
			}
		} else if (colToCompare==9) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=ch.getCacheStatus();
			}
		} else if (colToCompare==10) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				// CHECK Is the formatting correctly done?
				if (ch.kilom==-1.0) {
					ch.sort="\uFFFF";
				}
				else {
					ch.sort = MyLocale.formatDouble(ch.kilom*1000, "000000000000");
				}
			}
		} else if (colToCompare==11) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				if (ch.getBearing().equals("?")) {
					ch.sort="\uFFFF";
				}
				else {
					ch.sort=ch.getBearing();
				}			
			}
			
		} else if (colToCompare==12) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				ch.sort=Integer.toString(ch.getCacheSize());
			}
		} else if (colToCompare==13) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				if ( ch.isOC() ) {
  				ch.sort=MyLocale.formatLong(ch.getNumRecommended(),"00000");
        } else {
				  int gcVote = ch.getNumRecommended();
				  if ( gcVote < 100 ) {
				    ch.sort=MyLocale.formatLong(gcVote,"00") + "00000000";
          } else {
            int votes = gcVote / 100;
            gcVote = gcVote - 100 * votes;
            ch.sort = MyLocale.formatLong(gcVote,"00") + MyLocale.formatLong(votes,"00000000");
          }
        }
			}			
		} else if (colToCompare==14) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch=cacheDB.get(i);
				if (ch.isOC())
					ch.sort=MyLocale.formatLong(ch.recommendationScore,"00000");
				else
					ch.sort="\uFFFF";
			}			
		} else if (colToCompare==15) {
		    for (int i=0; i<visibleSize; i++) {
		        CacheHolder ch=cacheDB.get(i);
		        if (ch.hasSolver()) {
		            ch.sort="1";
		        } else {
		            ch.sort="2";
		        }
		    }
		} else if (colToCompare==16) {
		    for (int i=0; i<visibleSize; i++) {
		        CacheHolder ch=cacheDB.get(i);
		        if (ch.hasNote()) {
		            ch.sort="1";
		        } else {
		            ch.sort="2";
		        }
		    }
		} else if (colToCompare==17) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch =  cacheDB.get(i);
				ch.sort=MyLocale.formatLong(ch.addiWpts.size(),"000");
			}
		} else if (colToCompare==18) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch =  cacheDB.get(i);
				ch.sort=MyLocale.formatLong(ch.getNoFindLogs(),"000");
			}
		} else if (colToCompare==19) {
			for (int i=0; i<visibleSize; i++) {
				CacheHolder ch =  cacheDB.get(i);
				ch.sort=ch.getLastSync();
			}
		}
 	}
	
	public int compare(Object o1, Object o2){
		CacheHolder oo1 = (CacheHolder)o1;
		CacheHolder oo2 = (CacheHolder)o2;
		return oo1.sort.compareTo(oo2.sort);
	}
}
