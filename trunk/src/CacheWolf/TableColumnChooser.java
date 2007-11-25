package CacheWolf;

import ewe.fx.*;
import ewe.sys.Convert;
import ewe.sys.VMApp;
import ewe.sys.VMApps;
import ewe.sys.Vm;
import ewe.ui.*;
import ewe.util.*;

public class TableColumnChooser extends CellPanel {

	String [] colNames;
	Vector shownCols=new Vector(14);
	Vector hiddenCols=new Vector(14);
	private mButton btnDown,btnUp,btnLeft,btnRight;
	private myList lstShown,lstHidden;
	
	/**
	 * 
	 * @param colNames String array of ALL column names
	 * @param selectedCols The selected columns separated by ,
	 */
	public TableColumnChooser(String [] colNames, String selectedCols) {
        this.colNames=colNames;
		addNext(new mLabel(MyLocale.getMsg(6050,"Show column")));
        addNext(new mLabel(""));
        addLast(new mLabel(MyLocale.getMsg(6051,"Don't show column")));
        
        addNext(new ScrollBarPanel(lstShown=new myList(6,shownCols),ScrollBarPanel.AlwaysShowVerticalScrollers));
        CellPanel cpMid=new CellPanel();
        cpMid.addLast(new mLabel(""));
        mImage imgRight=new mImage("ewe/rightarrowsmall.bmp");imgRight.transparentColor=Color.White;
        mImage imgLeft=new mImage("ewe/leftarrowsmall.bmp");imgLeft.transparentColor=Color.White;
        cpMid.addLast(btnRight=new mButton(imgRight));
        cpMid.addLast(new mLabel(""));
        cpMid.addLast(btnLeft=new mButton(imgLeft));
        cpMid.addLast(new mLabel(""));
        addNext(cpMid,VSTRETCH,VFILL);
        addLast(new ScrollBarPanel(lstHidden=new myList(6,hiddenCols),ScrollBarPanel.AlwaysShowVerticalScrollers));
        
        CellPanel pnlButtons=new CellPanel();
		mImage imgDown=new mImage("ewe/downarrowsmall.bmp"); imgDown.transparentColor=Color.White;
		mImage imgUp=new mImage("ewe/uparrowsmall.bmp"); imgUp.transparentColor=Color.White;
        pnlButtons.addNext(btnDown=new mButton(imgDown),HSTRETCH,HFILL); btnDown.modify(Disabled,0);
		pnlButtons.addLast(btnUp=new mButton(imgUp),HSTRETCH,HFILL); btnUp.modify(Disabled,0);
        addNext(pnlButtons);
        addNext(new mLabel(""));
        addLast(new mLabel(""));
        
        // Set up
        for (int i=0; i<colNames.length; i++) hiddenCols.add(colNames[i]);
        StringTokenizer st=new StringTokenizer(selectedCols,",");
        int iCol;
        while (st.hasMoreTokens()) {
        	iCol=Common.parseInt(st.nextToken());
        	if (iCol>=0 && iCol<colNames.length) {
        		shownCols.add(colNames[iCol]);
        		hiddenCols.remove(colNames[iCol]);
        	}
        }
        changeUpDownButtonStatus();
	}

	public String getSelectedCols() {
		StringBuffer sb=new StringBuffer(40);
		for (int i=0; i<lstShown.items.size(); i++) {
			String colName=(String)lstShown.items.elementAt(i);
			for (int j=0; j<colNames.length; j++) {
				if (colName.equals(colNames[j])) {
					if (sb.length()!=0) sb.append(',');
					sb.append(j);
					break;
				}
			}
		}
		return sb.toString();
	}
	
	/** Enable the up/down buttons only if at least 2 caches are in the list */
	private void changeUpDownButtonStatus() {
		btnUp.modify(0,Disabled);
		if (lstShown.items.size()<2 || lstShown.getSelectedIndex(0)==0) btnUp.modify(Disabled,0);
		btnDown.modify(0,Disabled);
		if (lstShown.items.size()<2 || lstShown.getSelectedIndex(0)==lstShown.items.size()-1) btnDown.modify(Disabled,0);
		btnUp.repaintNow();
		btnDown.repaintNow();
	}

	
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type==ControlEvent.PRESSED) {
			if (ev.target==btnUp) {
				lstShown.moveUp();
			} else if (ev.target==btnDown) {
				lstShown.moveDown();
			} else if (ev.target==btnRight) {
				// Need to leave at least one item in shown list
				if (lstShown.items.size()>1)
					lstShown.moveItem(lstHidden,lstShown.getSelectedIndex(0));
			} else if (ev.target==btnLeft) {
				lstHidden.moveItem(lstShown,lstHidden.getSelectedIndex(0));
			}
			changeUpDownButtonStatus();
		} else if (ev instanceof ListEvent && ev.target==lstShown)
			changeUpDownButtonStatus();
		super.onEvent(ev);
	}
	
	private class myList extends mList {

		//public Vector items;
		int idx;
		
		myList(int rows, Vector elements) {
			super(rows,1,false);
			//this.items=elements;
			items=elements;
		}

		// Move selected element down by one
    	public void moveDown() {
			idx=getSelectedIndex(0);
    		if (idx>=0) {
	    		String s=(String) items.elementAt(idx);
				items.del(idx);
	    		items.insertElementAt(s,idx+1);
	    		select(idx+1);
	    		repaint();
    		}
    	}
    	
    	// Move selected element up by one
    	public void moveUp() {
			idx=getSelectedIndex(0);
    		if (idx>=0) {
				String s=(String) items.elementAt(idx);
				items.del(idx);
	    		items.insertElementAt(s,idx-1);
	    		select(idx-1);
	    		repaint();
    		}
    	}
    	
    	
		public void startDragging(DragContext dc) {
			 idx=getSelectedIndex(0);
			 // Can only drag if we have a valid index (at least on element in list)
			 // Also if we drag from lstShown, we must leave at least one item in list
			 if (idx>=0 && idx<items.size() && (this!=lstShown || items.size()>1)) {
				 IconAndText imgDrag=new IconAndText();
				 imgDrag.addColumn(items.elementAt(idx));
				 dc.dragData=dc.startImageDrag(imgDrag,new Point(8,8),this);
			 }
		}
	
		public void dragged(DragContext dc) {
			if (dc.dragData!=null) dc.imageDrag();
		}
		 
		public void stopDragging(DragContext dc) {
			 if (dc.dragData==null) return;
			 dc.stopImageDrag(true);
			 Point p = Gui.getPosInParent(this,getWindow());
			 p.x += dc.curPoint.x;
			 p.y += dc.curPoint.y;
			 Control dest = getWindow().findChild(p.x,p.y);
		     if (dest instanceof myList) { 
		    	 moveItem((myList)dest,idx);
		    	 changeUpDownButtonStatus();
		     }
		 }
		
		public void moveItem(myList dst, int srcIdx) {
			 if(srcIdx<0) return;
	    	 String colToMove=(String) items.elementAt(srcIdx);
	    	 items.del(srcIdx);
	    	 ((myList)dst).items.add(colToMove);
	    	 repaint();
	    	 dst.repaint();
	    	 if (srcIdx>=items.size()) select(items.size()-1);
		}
	
	} // myList
	
	/**
	 * Converts a comma delimited string into an integer array.
	 * Each value is checked and has to be between min and max, If not it is
	 * replaced with default
	 * @param minSize TODO
	 */ 
	public static int[] str2Array(String configString, int min, int max, int def, int minSize) {
		Vector strConfigVector=new Vector(18);
		SubString.split(configString,',',strConfigVector);
		int i;
		int nElem=strConfigVector.size();
		int []res=new int[nElem>minSize?nElem:minSize];
		for (i=0; i<nElem; i++) {
			res[i]=Common.parseInt((String)strConfigVector.elementAt(i));
			if (res[i]<min || res[i]>max) res[i]=def;
		}
		for (i=nElem+1; i<minSize; i++) res[i]=def;
		return res;
	}
	
}
