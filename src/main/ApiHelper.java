package main;

/**
 * Singleton, contains all methods and variables of the used API
 * @author Dennis Rünzler
 *
 */
public class ApiHelper {
	/**
	 * The Instance, should only exist once
	 */
	private static ApiHelper instance;
	/**
	 * Constructor should only be called by factory method 
	 */
	private ApiHelper() {
	}
	/**
	 * Factory Method. Constructor is private to ensure only this method is used to obtain instance
	 * @return The Singleton instance
	 */
	public static ApiHelper getInstance() {
		if (instance == null) {
			instance = new ApiHelper();	
		}
		return instance;
	}
}
