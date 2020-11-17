package CacheWolf.controls;

import CacheWolf.Preferences;
import ewe.ui.*;

public class InputPanel extends Form {

    ExecutePanel executePanel;
    mInput inputText;

    public InputPanel(String title, String label, String defaultValue) {
        Preferences.itself().setSubWindowSize(this);
        this.title = title;
        this.addNext(new mLabel(label), DONTSTRETCH, LEFT);
        this.addLast(inputText = new mInput(defaultValue));
        executePanel = new ExecutePanel(this);
    }

    public void toolTip(String toolTip) {
        inputText.toolTip = toolTip;
    }

    public String input() {
        return inputText.getText();
    }

    public void onEvent(Event ev) {
        if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
            if (ev.target == executePanel.cancelButton)

            {
                this.close(IDCANCEL);
            }

            if (ev.target == executePanel.applyButton)

            {
                this.close(IDOK);
            }
        }
    }

}
