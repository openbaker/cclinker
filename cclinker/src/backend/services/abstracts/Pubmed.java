/**
 * 
 */
package backend.services.abstracts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import backend.manager.ConfigManager;
import backend.manager.HttpManager;
import backend.services.AbstractService;

/**
 * @author schiend
 *
 */
public class Pubmed implements AbstractService{
	
	public Pubmed() {
		// init pubmed methods
	}
	
	/**
	 * Fetch abstracts with terms
	 * 
	 * @param termsList
	 * @param params
	 * @return
	 */
	public static int[] getAbstractsWithTermsList(List<List<String>> termList, Map<String, String> params) {
		int numAbstracts = 0;
		
		// create search string with term
		String searchString = combineSearchStrings(termList);
		
		String searchURL = buildSearchURL(searchString, params);
		
		System.out.println(">> SEARCH URL: " + searchURL);
		
		// search for abstracts with term
		JSONObject searchDoc = HttpManager.getJSONfromURL(
				buildSearchURL(searchString, params), HttpManager.TYPE_JSON_GET);
		
		// get webenv and result count
		String resultWebenv = ((JSONObject) searchDoc.get("esearchresult")).getString("webenv");
		int resultCount = ((JSONObject) searchDoc.get("esearchresult")).getNumber("count").intValue();
		
		System.out.println(">> GOT: " + resultCount);
		
		// create ID for search
		int curDate = Integer.parseInt(
				new SimpleDateFormat("yyMMdd").format(Calendar.getInstance().getTime()));
		int searchID = createNewSearchID();
		
		// create new file
		File file = new File(getSearchIDFile(curDate, searchID));
		FileWriter fw;
		
		// adjust params for retstart
		if (params == null) {
			params = new HashMap<>();
		}
		
		if (params.containsKey("retstart")) {
			params.replace("retstart", Integer.toString(0));
		} else {
			params.put("retstart", Integer.toString(0));
		}
		
		// fetch all abstracts
		try {
			fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder sb = new StringBuilder();
			
			// get abstracts in chunks
			for (int i = 0; i < resultCount; i += ConfigManager.getInstance().getServicePubmedRetmax()) {
				// wait
				Thread.sleep(ConfigManager.getInstance().getServicePubmedDelay());
				
				// fetch abstracts
				params.replace("retstart", Integer.toString(i));
				Document fetchDoc = HttpManager.getXMLfromURL(buildFetchURL(resultWebenv, params));
				
				// get abstracts
				NodeList abstracts = fetchDoc.getElementsByTagName("PubmedArticle");
				
				System.out.println(">> PROCESS: " + abstracts.getLength());
				
				// go through results
				for (int j = 0; j < abstracts.getLength(); j++) {
					// get current node
					Element curAbstract = (Element) abstracts.item(j);
					
					// get pmid
					int pmid = Integer.parseInt(
							curAbstract.getElementsByTagName("PMID").item(0).getTextContent());
					
					// get abstract
					NodeList abstractNode = curAbstract.getElementsByTagName("Abstract");
					String text = new String();
					
					if (abstractNode != null && abstractNode.getLength() > 0) {
						NodeList abstractTextNode =
								((Element) abstractNode.item(0)).getElementsByTagName("AbstractText");
						if (abstractTextNode != null && abstractTextNode.getLength() > 0) {
							text = abstractTextNode.item(0).getTextContent();
						}
					}
					
					// write to results
					sb.append(pmid);
					sb.append(System.lineSeparator());
					sb.append(text);
					sb.append(System.lineSeparator());
					sb.append(System.lineSeparator());
					
					numAbstracts++;
				}
				
				// save to file
				bw.write(sb.toString());
			}
		
			// store in file
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// return search id
		int[] searchRef = {curDate, searchID};
		
		return searchRef;
	}
	
	/**
	 * Fetch abstracts with terms
	 * 
	 * @param terms
	 * @param params
	 * @return
	 */
	public static int getAbstractsWithTerms(List<String> terms, Map<String, String> params) {
		// create a dummy list with one entry
		List<List<String>> termsList = new ArrayList<>();
		
		termsList.add(terms);
		
		return getAbstractsWithTermsList(termsList, params);
	}
	
	/**
	 * @see getAbstractsWithTerms(List<String> terms, Map<String, String> params)
	 * 
	 * @param term
	 * @param minDate
	 * @param maxDate
	 * @return
	 */
	public static int getAbstractsWithTerm(String term, Map<String, String> params) {
		return getAbstractsWithTerms(Arrays.asList(term), params);
	}
	
	/**
	 * Create new search ID
	 * 
	 * @return
	 */
	private static int createNewSearchID() {
		// generate random number
		int rnd = new Random().nextInt(
				ConfigManager.getInstance().getServicePubmedSearchIdMax());
		
		// TODO generate an autoincrement for each day
		
		return rnd;
	}
	
	/**
	 * Return search file from ID
	 * 
	 * @param date
	 * @param id
	 * @return
	 */
	public static String getSearchIDFile(int date, int id) {
		return ConfigManager.getInstance().getServicePubmedSearchFp()
				+ Integer.toString(date) + Integer.toString(id);
	}

	public static String createSearchString(String term) {
		return createSearchString(Arrays.asList(term).iterator());
	}
	
	public static String createSearchString(List<String> terms) {
		return createSearchString(terms.iterator());
	}
	
	public static String createSearchString(Iterator<String> terms) {
		String searchString = new String();

		// go through terms and combine to a query
		searchString = "(";

		// combine terms by (X OR Y OR Z)
		while(terms.hasNext()) {
			searchString += "\"" + terms.next() + "\"";
			
			// add logical operator if there is another term
			if (terms.hasNext()) {
				searchString += " " + AbstractService.logicalOperatorOR + " ";
			}
		}
		
		searchString += ")";
		
		return searchString;
	}

	/**
	 * Combine terms to search string
	 * 
	 * @param terms
	 * @param logicalOperator
	 * @return
	 */
	private static String combineSearchStrings(List<List<String>> termsList, String logicalOperator) {
		String searchString = new String();
		
		Iterator<List<String>> termsIt = termsList.iterator();
		
		// go through term lists
		while(termsIt.hasNext()) {
			// create a search string with terms
			searchString += createSearchString(termsIt.next().iterator());
			
			// add logical operator if there is another term list
			if (termsIt.hasNext()) {
				searchString += " " + logicalOperator + " ";
			}
		}
		
		return searchString;
	}
	
	/**
	 * @see combineSearchStrings(Iterator<List<String>> terms, String logicalOperator)
	 * 
	 * @param terms
	 * @return
	 */
	private static String combineSearchStrings(List<List<String>> termsList) {
		return combineSearchStrings(termsList, AbstractService.logicalOperatorAND);
	}
	
	/**
	 * Build search URL
	 * 
	 * @param terms
	 * @param retstart
	 * @return
	 */
	public static String buildSearchURL(String searchString, Map<String, String> paramsToAdd) {
		String searchURL = new String();
		
		// get params
		Map<String, String> params =
				ConfigManager.getInstance().getServicePubmedSearchParams();
		
		// replace params
		params.replace("term", searchString);
		
		// prepare params
		prepareParams(params, paramsToAdd);
		
		// build URL
		searchURL = HttpManager.buildURL(
						ConfigManager.getInstance().getServicePubmedBaseScheme(),
						ConfigManager.getInstance().getServicePubmedBaseURL(),
						ConfigManager.getInstance().getServicePubmedBasePath()
						+ ConfigManager.getInstance().getServicePubmedSearchPath(),
						params,
						false); // do not encode otherwise the search string will not work
		
		return searchURL;
	}
	
	/**
	 * Build search URL
	 * 
	 * @param terms
	 * @return
	 */
	public static String buildSearchURL(String searchString) {
		return buildSearchURL(searchString, null);
	}
	
	/**
	 * Build fetch URL
	 * 
	 * @param searchWebenv
	 * @param retstart
	 * @return
	 */
	public static String buildFetchURL(String searchWebenv, Map<String, String> paramsToAdd) {
		String searchURL = new String();
		
		// get params
		Map<String, String> params =
				ConfigManager.getInstance().getServicePubmedFetchParams();
		
		// replace params
		params.replace("webenv", searchWebenv);
		
		// prepare params
		prepareParams(params, paramsToAdd);
		
		// build URL
		searchURL = HttpManager.buildURL(
						ConfigManager.getInstance().getServicePubmedBaseScheme(),
						ConfigManager.getInstance().getServicePubmedBaseURL(),
						ConfigManager.getInstance().getServicePubmedBasePath()
						+ ConfigManager.getInstance().getServicePubmedFetchPath(),
						params);
		
		return searchURL;
	}
	
	/**
	 * Prepare params map
	 * 
	 * @param paramsToAdd
	 * @return
	 */
	private static Map<String, String> prepareParams(
			Map<String, String> params, Map<String, String> paramsToAdd) {
		if (paramsToAdd != null) {
			// restart
			if (paramsToAdd.containsKey("retstart")) {
				params.replace("retstart", paramsToAdd.get("retstart"));
			}
			
			// date
			if (paramsToAdd.containsKey("mindate")) {
				params.replace("mindate", paramsToAdd.get("mindate"));
			}
			
			if (paramsToAdd.containsKey("maxdate")) {
				params.replace("maxdate", paramsToAdd.get("maxdate"));
			}
		}
		
		return params;
	}
	
	/**
	 * Convert date
	 * 
	 * @param date
	 * @return
	 */
	public static String convertDate(Date date) {
		return new SimpleDateFormat(ConfigManager.getInstance().getServicePubmedDatePattern()).format(date);
	}
	
	/**
	 * Build data params
	 * 
	 * @param mindate
	 * @param maxdate
	 * @return
	 */
	public static Map<String, String> buildDateParams(Date mindate, Date maxdate) {
		// build params
		Map<String, String> params = new HashMap<>();
		
		// set min and max
		params.put("mindate", Pubmed.convertDate(mindate));
		params.put("maxdate", Pubmed.convertDate(maxdate));
		
		return params;
	}

}
