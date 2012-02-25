package CacheWolf.view.pda;

public class PDATravelBugJourneyButton extends PDAListButton {

	public PDATravelBugJourneyButton(String newText, String newAction) {
		super(newText, newAction);
	}

	protected void createButtonObject() {
		buttonObject = new PDATravelBugJourneyScreenButtonObject(this);
	}
}
