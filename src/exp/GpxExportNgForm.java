package exp;

import ewe.ui.*;

/**
 * GUI for GpxExporterNg with checkboxes for the options 
 *
 */
public class GpxExportNgForm extends Form {
	private CheckBoxGroup cbgExportType;
	private mCheckBox cbCompact, cbPqLike, cbMyFinds, cbCustomIcons, cbSeperateFiles, cbSendToGarmin, cbSmartId;
	private mButton btnOk, btnCancel;
	
	/**
	 * set up the form / dialog
	 */
	public GpxExportNgForm() {
		//TODO: get defaults from profile
		
		this.setTitle("GPX Export");
		
		cbgExportType = new CheckBoxGroup();
		
		cbCompact = new mCheckBox("Compact");
		cbCompact.setGroup(cbgExportType);
		
		cbPqLike = new mCheckBox("PQ like");
		cbPqLike.setGroup(cbgExportType);
		
		cbMyFinds = new mCheckBox("MyFinds");
		cbMyFinds.setGroup(cbgExportType);
		
		cbgExportType.setText("Compact");
		
		cbCustomIcons = new mCheckBox("custom icons");
		
		cbSeperateFiles = new mCheckBox("one file per type");
				
		cbSendToGarmin = new mCheckBox("send to Garmin GPSr");
		cbSendToGarmin.modify(Control.Disabled, 0); // not yet
		
		cbSmartId = new mCheckBox("use smart IDs");
		
		btnOk = new mButton("OK");
		btnCancel = new mButton("Cancel");
		
		addNext(cbCustomIcons);
		addLast(cbCompact);
		addNext(cbSeperateFiles);
		addLast(cbPqLike);
		addNext(cbSendToGarmin);
		addLast(cbMyFinds);
		addLast(cbSmartId);

		addButton(btnOk);
		addButton(btnCancel);
	}
	
	/**
	 * react to GUI events and toogle access to the checkboxes according to radio button settings
	 * pass everything else to <code>super()</code>
	 */
	public void onEvent(Event ev){
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
						
			if (ev.target == cbgExportType) {
				if (cbgExportType.getSelected() == cbCompact) {
					if (cbCustomIcons.change(0,Control.Disabled)) cbCustomIcons.repaint();
					if (cbSeperateFiles.change(0,Control.Disabled)) cbSeperateFiles.repaint();
//					if (cbSendToGarmin.change(0,Control.Disabled)) cbSendToGarmin.repaint();
					if (cbSmartId.change(0,Control.Disabled)) cbSmartId.repaint();
				} else if (cbgExportType.getSelected() == cbPqLike) {
					cbSeperateFiles.setState(false);
					if (cbCustomIcons.change(0,Control.Disabled)) cbCustomIcons.repaint();
					if (cbSeperateFiles.change(Control.Disabled,0)) cbSeperateFiles.repaint();
//					if (cbSendToGarmin.change(0,Control.Disabled)) cbSendToGarmin.repaint();
					if (cbSmartId.change(0,Control.Disabled)) cbSmartId.repaint();
				} else if (cbgExportType.getSelected() == cbMyFinds) {
					cbCustomIcons.setState(false);
					cbSeperateFiles.setState(false);
					cbSendToGarmin.setState(false);
					cbSmartId.setState(false);
					if (cbCustomIcons.change(Control.Disabled,0)) cbCustomIcons.repaint();
					if (cbSeperateFiles.change(Control.Disabled,0)) cbSeperateFiles.repaint();
//					if (cbSendToGarmin.change(Control.Disabled,0)) cbSendToGarmin.repaint();
					if (cbSmartId.change(Control.Disabled,0)) cbSmartId.repaint();
				}
			} else if (ev.target == btnOk) {
				close(1);
			} else if (ev.target == btnCancel) {
				close(-1);
			}
		}
		super.onEvent(ev);
	}
	
	/**
	 * get the export type the user selected
	 * @return index of selected option in checkboxgroup
	 * @see GpxExportNg
	 */
	public int getExportType() {
		return cbgExportType.getSelectedIndex();
	}
	
	/**
	 * check if the user wants smart IDs
	 * @return true for smart IDs, false otherwise
	 */
	public boolean getSmartIds() {
		return cbSmartId.state;
	}
	
	/**
	 * check if user wants to send output straight to a Garmin GPSr
	 * @return true for GPSr transfer, false otherwise
	 */
	public boolean getSendToGarmin() {
		return cbSendToGarmin.state;
	}
	
	/**
	 * check if user wants custom icons
	 * @return true if user wants custom icons, false otherwise
	 */
	public boolean getCustomIcons() {
		return cbCustomIcons.state;
	}
	
	/**
	 * check if user wants separate files (POI loader)
	 * @return true for separate files, false for single file
	 */
	public boolean getSeparateFiles() {
		return cbSeperateFiles.state;
	}
}
