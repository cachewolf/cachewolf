package CacheWolf.view.pda;

import CacheWolf.database.TravelbugJourney;
import CacheWolf.model.TravelBugJourneyScreenModel;
import ewe.graphics.AniImage;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Form;

public class PDATravelbugJourneyScreen extends PDAList {
    private static final String LINE = "Line";

    private static final String NEXT_PAGE = "NextPage";

    private static final String PREV_PAGE = "PrevPage";

    private static final String MENUE = "Menue";

    TravelBugJourneyScreenModel model;

    private final int linesOnScreen = 7;

    /**
     * The six visible entries in the List
     */

    public PDATravelbugJourneyScreen(TravelBugJourneyScreenModel travelbugModel) {
	addListener(this);
	setTitle("TravelBugs");

	model = travelbugModel;

	// backgroundImage = new Image("bug_vga.gif");
	for (int i = 0; i < model.allTravelbugJourneys.size(); i++) {
	    model.shownTravelbugJourneys.add(model.allTravelbugJourneys.getTBJourney(i));
	}
	createShowSet();
	setupTBButtons();
    }

    public void onControlEvent(ControlEvent ev) {
	if (ev instanceof ControlEvent) {
	    switch (ev.type) {
	    case ControlEvent.PRESSED:
		if (ev.action.equals(NEXT_PAGE) && model.shownTravelbugJourneys.size() > firstLine + linesOnScreen) {
		    firstLine += linesOnScreen;
		    setupTBButtons();
		} else if (ev.action.equals(PREV_PAGE) && firstLine > 0) {
		    firstLine -= linesOnScreen;
		    if (firstLine < 0) {
			firstLine = 0;
		    }
		    setupTBButtons();
		} else if (ev.action.startsWith(LINE)) {
		    int line = ev.action.charAt(LINE.length()) - '0';
		    TravelbugJourney tbJourney = (TravelbugJourney) model.shownTravelbugJourneys.get(line + firstLine);
		    Form form = new PDATravelbugDetailPanel(tbJourney, this);
		    form.setPreferredSize(800, 600);
		    form.execute();
		    setupTBButtons();
		} else if (ev.action.equals(MENUE)) {
		    Form form = new PDATravelbugMenuPanel(this);
		    form.setPreferredSize(800, 600);
		    int execute = form.execute();
		    if (execute == 1) {
			exit(0);
		    }
		    setupTBButtons();
		}
		break;
	    default:
		super.onControlEvent(ev);
	    }
	}
    }

    protected PDAListButton createListButton(int i) {
	return new PDATravelBugJourneyButton("", LINE + i);
    }

    public void setupTBButtons() {
	if (model == null) {
	    return;
	}
	for (int i = 0; i < linesOnScreen; i++) {
	    if (i + firstLine < model.shownTravelbugJourneys.size()) {
		TravelbugJourney tbJourney = (TravelbugJourney) model.shownTravelbugJourneys.get(i + firstLine);
		String tbName = tbJourney.getTb().getName();
		listButtons[i].text = tbName;
		listButtons[i].fromText = tbJourney.getFromWaypoint() + '/' + tbJourney.getFromProfile();
		listButtons[i].fromLogged = tbJourney.getFromLogged();
		listButtons[i].toText = tbJourney.getToWaypoint() + '/' + tbJourney.getToProfile();
		listButtons[i].toLogged = tbJourney.getToLogged();
		listButtons[i].image = new AniImage("bug_vga.gif");
		listButtons[i].modify(ControlConstants.Disabled, 1);
	    } else {
		listButtons[i].text = listButtons[i].fromText = listButtons[i].toText = "";
		listButtons[i].toLogged = listButtons[i].fromLogged = true;
		listButtons[i].image = null;
		listButtons[i].modify(ControlConstants.Disabled, 0);
	    }
	    listButtons[i].repaint();
	}
    }

    public void toggleOnlyLogged() {
	model.toggleOnlyLogged();
	firstLine = 0;
	setupTBButtons();
    }

    public void createShowSet() {
	firstLine = 0;
	model.createShowSet();
    }
}
