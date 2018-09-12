/**
 * 
 */
package backend.manager;

/**
 * Main class organising requests made by the user.
 */

/**
 * @author schiend
 *
 */
public class RequestManager {
	
	// Singleton
	private static RequestManager instance = null;
	
	// Services
	
	public RequestManager() {
		// dummy
	}
	
	/**
	 * Singleton return
	 * 
	 * @return
	 */
	public static RequestManager getInstance() {
		if (instance == null) {
			instance = new RequestManager();
		}
		
		return instance;
	}
	
	/**
	 * Get abstracts for term
	 * 
	 * @param term
	 * @return
	 */
	public boolean getAbstractsForTerm(String term) {
		// get abstracts for term
		
	}
	
	/**
	 * Normalise term and get abstracts
	 * 
	 * @param term
	 * @return
	 */
	public boolean getAbstractsForNormalisedTerm(String term) {
		// normalise string
		
		// get abstracts for term
		this.getAbstractsForTerm(term);
	}
	

	
	public boolean getAbstractsForSynonyms(String term) {
		// get synonyms for term
		
		
		// get abstracts for term
		this.getAbstractsForTerm(term);
	}
	
}
