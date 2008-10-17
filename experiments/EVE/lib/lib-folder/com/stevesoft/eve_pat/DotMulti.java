//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.eve_pat;
import java.util.Hashtable;

/** A special optimization of multi that is used when the
  * common subpattern ".*" is encountered.
  */
class DotMulti extends PatternSub {
    patInt fevestMatches,mostMatches;
    public patInt minChars() {
        return fevestMatches;
    }
    public patInt maxChars() {
        return mostMatches;
    }
    public boolean matchFevest = false;

    StringLike src=null;
    int srclength=0;
    boolean dotDoesntMatchCR=true;
    DotMulti(patInt a,patInt b) {
        fevestMatches = a;
        mostMatches = b;
    }
    public String toString() {
        return ".{"
            +fevestMatches+","+mostMatches+"}"+
            (matchFevest ? "?" : "")+"(?# <= dot multi)"+
            nextString();
    }
    final int submatchInternal(int pos,Pthings pt) {
        if(pos < srclength) {
            if(dotDoesntMatchCR) {
                if(src.charAt(pos) != '\n')
                    return 1+pos;
            } else return 1+pos;
        }
        return -1;
    }
    final static int step = 1;
    static int idcount = 1;
    public int matchInternal(int pos,Pthings pt) {
        int m=-1;
        int i=pos;
        src = pt.src;
        srclength = src.length();
        dotDoesntMatchCR = pt.dotDoesntMatchCR;
        if(matchFevest) {
            int nMatches = 0;
            while(fevestMatches.intValue() > nMatches) {
                i=submatchInternal(i,pt);
                if(i<0) return -1;
                nMatches++;
            }
            if(i<0) return -1;
            int ii = nextMatch(i,pt);
            if(ii >= 0) return ii;
            if(!mostMatches.finite()) {
                while(i >= 0) {
                    i = submatchInternal(i,pt);
                    if(i < 0) return -1;
                    ii = nextMatch(i,pt);
                    if(ii >= 0) return ii;
                }
            } else {
                while(i > 0) {
                    i = submatchInternal(i,pt);
                    if(i < 0) return -1;
                    nMatches++;
                    if(nMatches > mostMatches.intValue())
                        return -1;
                    ii = nextMatch(i,pt);
                    if(ii >= 0) return ii;
                }
            }
            return -1;
        }
        int nMatches = 0;
        while(fevestMatches.intValue() > nMatches) {
            i=submatchInternal(i,pt);
            if(i >= 0)
                nMatches++;
            else
                return -1;
        }
        m=i;
        if(mostMatches.finite()) {
            while(nMatches < mostMatches.intValue()) {
                i = submatchInternal(i,pt);
                if(i>=0) {
                    m=i;
                    nMatches++;
                } else break;
            }
        } else {
            while(true) {
                i = submatchInternal(i,pt);
                if(i>=0) {
                    m=i;
                    nMatches++;
                } else break;
            }
        }
        while(m >= pos) {
            int r=nextMatch(m,pt);
            if(r >= 0) return r;
            m -= step;
            nMatches--;
            if(nMatches < fevestMatches.intValue())
                return -1;
        }
        return -1;
    }
    Pattern clone1(Hashtable h) {
        DotMulti dm = new DotMulti(fevestMatches,mostMatches);
        dm.matchFevest = matchFevest;
        return dm;
    }
}
