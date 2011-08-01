package CacheWolf.view;

public interface ITravelbugScreen {

	/**
	 * Shows the form
	 * @return
	 */
	int execute();

	/**
	 * Returns the selected entry if any else <code>null</code>
	 * @return
	 */
	int getSelectedItem();

}