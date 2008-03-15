package CacheWolf;

import com.stevesoft.ewe_pat.Regex;

import ewe.ui.*;
import ewe.fx.*;
import ewe.sys.*;
import ewe.util.Vector;

/**
*	This class shows the long description on a cache.
*/
public class DescriptionPanel extends CellPanel{
	HtmlDisplay disp = new HtmlDisplay();
	mButton btnPlus, btnMinus;
	CacheHolderDetail currCache;
	
	CellPanel buttonP = new CellPanel();
	CellPanel descP = new CellPanel();
	
	private String desc;

	public DescriptionPanel(){
		buttonP.addNext(btnPlus = new mButton("+"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		buttonP.addLast(btnMinus = new mButton("-"),CellConstants.HSTRETCH, (CellConstants.HFILL));
		ScrollBarPanel sbp = new MyScrollBarPanel(disp, 0);
		//sbp.setScrollBarSize(40,40, 20);
		descP.addLast(sbp);
		this.addLast(descP);
		this.addLast(buttonP,CellConstants.HSTRETCH,CellConstants.HFILL);
		clear();
	}
	
	/**
	*	Set the text to display. Text should be HTML formated.
	*/
	String description = null;
	public void setText(CacheHolderDetail cache){
		boolean isHtml=cache.is_HTML;
		if (currCache == cache) return;
		int scrollto = 0;
		if (cache.hasSameMainCache(currCache)) scrollto = disp.getTopLine();
		if (cache == null) desc = "";
		else {
			if (cache.isAddiWpt()) {
				isHtml=true;
				if (cache.LongDescription != null && cache.LongDescription.length() > 0)
					 desc = cache.LongDescription + "<hr>\n"+cache.mainCache.getCacheDetails(true).LongDescription;
				else desc = cache.mainCache.getCacheDetails(true).LongDescription;
			} else // not an addi-wpt
				desc = cache.LongDescription;
		}
		if (!desc.equals(description)) {
			//disp.getDecoderProperties().setBoolean("allowImages",true);
			Vm.showWait(true); 
			if (isHtml) {
				if (Global.getPref().descShowImg) {
					Vector Images;
					if (cache.isAddiWpt()) {
						Images = cache.mainCache.getCacheDetails(true).Images;
					} else {
						Images = cache.Images;						
					}					
					StringBuffer s=new StringBuffer(desc.length()+Images.size()*100);
					int start=0;
					int pos;
					int imageNo=0;
					Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
					if (Images.getCount() > 0) {
						while (start>=0 && (pos=desc.indexOf("<img",start))>0) {
							s.append(desc.substring(start,pos));
							imgRex.searchFrom(desc,pos);
							String imgUrl=imgRex.stringMatched(1);
							//Vm.debug("imgUrl "+imgUrl);
							if (imgUrl.lastIndexOf('.')>0 && imgUrl.toLowerCase().startsWith("http")) {
								String imgType = (imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase()+"    ").substring(0,4).trim();
								// If we have an image which we stored when spidering, we can display it
								if(!imgType.startsWith(".com") && !imgType.startsWith(".php") && !imgType.startsWith(".exe")){
									s.append("<img src=\""+
											//Global.getProfile().dataDir+
											Images.get(imageNo)+"\">");
									imageNo++;
								}
							}
							start=desc.indexOf(">",pos);
							if (start>=0) start++;
							if (imageNo >= Images.getCount())break;
						}
					}
					if (start>=0) s.append(desc.substring(start));
					desc=s.toString();
				}
				//disp.setHtml(desc);
				disp.startHtml();
				disp.getDecoderProperties().set("documentroot",Global.getProfile().dataDir);
				disp.addHtml(desc,new ewe.sys.Handle());
				disp.endHtml();
				
			}
			else
				disp.setPlainText(desc);
			disp.scrollTo(scrollto,false);
			description = desc;
			Vm.showWait(false);
		}
		currCache = cache;
	}
	
	public void clear() {
		disp.setPlainText("loading ...");
	}
	
	private void redraw() {
		int currLine;

		Vm.showWait(true);
		currLine = disp.getTopLine();
		if (currCache.is_HTML)	disp.setHtml(desc);
		else				disp.setPlainText(currCache.LongDescription);
		disp.scrollTo(currLine,false);
		Vm.showWait(false);
	}
	
	/**
	 * Eventhandler
	 */
	public void onEvent(Event ev){
		
		if(ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
			if (ev.target == btnPlus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() + 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				redraw();
			}

			if (ev.target == btnMinus){
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null, currFont.getSize() - 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				redraw();
			}
		}
		super.onEvent(ev);
	}

}
