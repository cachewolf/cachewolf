package CacheWolf;

import ewe.ui.CellConstants;
import ewe.ui.Control;
import ewe.ui.Frame;
import ewe.ui.InputBox;
import ewe.ui.mCheckBox;

/**
 * A SearchBox is a customized input box optimized for searching in CacheWolf. The actual
 * implementation is able to display a CheckBox which with label "in Notes/Description".

 * @author Engywuck
 */
public class SearchBox extends InputBox {

	protected mCheckBox useNoteDesc;
	protected mCheckBox useLogs;
	protected boolean buildingForm = false;

	/**
	 * Creates the search box with given title.
	 * @param title The titel of the box
	 */
	public SearchBox(String title) {
		super(title);
	}

	/**
	 * Displays the search Box and returns the String value entered, if OK is pressed, otherwise
	 * the value is null.
	 * @param initialValue Initial value to display in the search box
	 * @param checkUseNoteDesc Initial value for check box
	 * @param checkUseLogs Initial value for check box
	 * @param pWidth ?
	 * @return String to search for if ok is pressed and a string is entered, <code>null</code> otherwise.
	 */
	public String input(String initialValue, boolean checkUseNoteDesc, boolean checkUseLogs, int pWidth) {
		return this.input(null, initialValue, checkUseNoteDesc, checkUseLogs, pWidth);
	}
	
	protected String input(Frame pParent, String initialValue, boolean checkUseNoteDesc, boolean checkUseLogs, int pWidth) {
		String result;
		buildingForm = true;
		useNoteDesc = new mCheckBox(MyLocale.getMsg(218,"Also in description/notes"));//,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		useNoteDesc.setState(checkUseNoteDesc);		
		useLogs = new mCheckBox(MyLocale.getMsg(225,"Also in logs"));//,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		useLogs.setState(checkUseLogs);		
		result = super.input(pParent, initialValue, pWidth);
		return result;
    }
	
	/**
	 * Queries the check box to search in Notes and Description if it is checked or not.
	 * @return <code>True</code> if check box is checked, <code>false</code> if not.
	 */
	public boolean useNoteDesc() {
		boolean result = false;
		if (useNoteDesc != null) {
			result = useNoteDesc.getState();
		}
		return result;
	}

	/**
	 * Queries the check box to search in Logs if it is checked or not.
	 * @return <code>True</code> if check box is checked, <code>false</code> if not.
	 */
	public boolean useLogs() {
		boolean result = false;
		if (useLogs != null) {
			result = useLogs.getState();
		}
		return result;
	}
	
	public Control addLast(Control c) {
		// This method is a dirty hack, because in InputBox every thing, from creation of the 
		// controls to displaying it and returning the return value is done at once.
		// To be able to add other controls, I have to enhance the addLast(Control) method - not
		// nice but it works.
		Control result;
		if (!buildingForm) {
			result = super.addLast(c);
		} else {
			buildingForm = false;
			this.addControlsBeforeInput();
			result = super.addLast(c);
			this.addControlsAfterInput();
		}
		return result;
	}

	/**
	 * Called before creating the input box. Additional controls may be added here.
	 */
	private void addControlsBeforeInput() {
		// For future use 
	}

	/**
	 * Called after creating the input box. Additional controls may be added here.
	 */
	private void addControlsAfterInput() {
		if (useNoteDesc != null) {
			this.addLast(useNoteDesc,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}
		if (useLogs != null) {
			this.addLast(useLogs,CellConstants.DONTSTRETCH, (CellConstants.DONTFILL|CellConstants.WEST));
		}
    }


}
