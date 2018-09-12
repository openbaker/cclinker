/**
 * Retrieve configuration variables
 */
package backend.manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * @author schiend
 *
 */
public class ConfigManager {

	private static String CONF_LINK = ".";
	private static String CONF_SERVICE = "service";
	private static String CONF_MANAGER = "manager";
	private static String CONF_REGEXP = "regexp";
	private static String CONF_FILE = "file";
	
	// to construct arrays in the props file
	private static String ARRAY_DEL = ",";
	private static String ARRAY_VALUE = ":";
	
	// Singleton
	private static ConfigManager instance;
	
	private String config_path = "config/cclinker.cfg";
	private Properties props;
	
	private ConfigManager() {
		// load config
		this.loadConfig();
	}
	
	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static ConfigManager getInstance() {
        if(instance == null){
        		instance = new ConfigManager();
        }
        
        return instance;
	}
	
	/**
	 * @return the props
	 */
	private Properties getProps() {
		return props;
	}
	
	/**
	 * @param props the props to set
	 */
	private void setProps(Properties props) {
		this.props = props;
	}
	
	/**
	 * Search for property key
	 * 
	 * @param key
	 * @return
	 */
	private String getProp(String key) {
		// look for key
		String value = this.getProps().getProperty(key, new String());
		
		// return value of key
		return value;
	}

	/**
	 * Extract map from prop
	 * 
	 * @param prop
	 * @return
	 */
	private Map<String, String> extractMapFromProp(String prop) {
		Map<String, String> paramMap = new HashMap<>();
		
		// load params as map with defaults
		String[] params = prop.split(ARRAY_DEL);
		
		for (String param: params) {
			// split param
			String[] keyValue = param.split(ARRAY_VALUE);
			String key = keyValue[0];
			String value = new String();
			
			if (keyValue.length > 1) {
				value = keyValue[1];
			}
	        
			paramMap.put(key, value);
	    }
		
		return paramMap;
	}
	
	/**
	 * Extract list from prop
	 * 
	 * @param prop
	 * @return
	 */
	private List<String> extractListFromProp(String prop) {
		List<String> paramList = new ArrayList<>();
		
		// load params as map with defaults
		String[] params = prop.split(ARRAY_DEL);
		
		// add to param list
		for (String param: params) {
			paramList.add(param);
	    }
		
		return paramList;
	}
	
	/**
	 * load config
	 */
	private void loadConfig() {
		// create properties object
		this.setProps(new Properties());
		
		// load properties
		try {
			this.getProps().load(new FileInputStream(this.config_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get service property
	 * 
	 * @return
	 */
	private String getServiceProp(String key) {
		return this.getProp(CONF_SERVICE + CONF_LINK + key);
	}
	
	/**
	 * Get manager property
	 * 
	 * @return
	 */
	private String getManagerProp(String key) {
		return this.getProp(CONF_MANAGER + CONF_LINK + key);
	}
	
	/**
	 * Get regexp property
	 * 
	 * @return
	 */
	private String getRegexpProp(String key) {
		return this.getProp(CONF_REGEXP + CONF_LINK + key);
	}
	
	/**
	 * Get file property
	 * 
	 * @return
	 */
	private String getFileProp(String key) {
		return this.getProp(CONF_FILE + CONF_LINK + key);
	}
	
	/**
	 * Return pubmed base scheme
	 * 
	 * @return
	 */
	public String getServicePubmedBaseScheme() {
		return this.getServiceProp("pubmed_base_scheme");
	}
	
	/**
	 * Return pubmed base url
	 * 
	 * @return
	 */
	public String getServicePubmedBaseURL() {
		return this.getServiceProp("pubmed_base_url");
	}
	
	/**
	 * Return pubmed base path
	 * 
	 * @return
	 */
	public String getServicePubmedBasePath() {
		return this.getServiceProp("pubmed_base_path");
	}
	
	/**
	 * Return pubmed search path
	 * 
	 * @return
	 */
	public String getServicePubmedSearchPath() {
		return this.getServiceProp("pubmed_search_path");
	}
	
	/**
	 * Return pubmed fetch path
	 * 
	 * @return
	 */
	public String getServicePubmedFetchPath() {
		return this.getServiceProp("pubmed_fetch_path");
	}
	
	/**
	 * Return pubmed search params
	 * 
	 * @return
	 */
	public Map<String, String> getServicePubmedSearchParams() {
		Map<String, String> paramPairs = this.extractMapFromProp(
				this.getServiceProp("pubmed_search_params"));
		
		// add standard parameters
		paramPairs.replace("retmax",
				String.valueOf(ConfigManager.getInstance().getServicePubmedRetmax()));
		paramPairs.replace("api_key",
				ConfigManager.getInstance().getServicePubmedAPIKey());
	
		return paramPairs;
	}
	
	/**
	 * Return pubmed fetch params
	 * 
	 * @return
	 */
	public Map<String, String> getServicePubmedFetchParams() {
		Map<String, String> paramPairs = this.extractMapFromProp(
				this.getServiceProp("pubmed_fetch_params"));
		
		// add standard parameters
		paramPairs.replace("retmax",
				String.valueOf(ConfigManager.getInstance().getServicePubmedRetmax()));
		paramPairs.replace("api_key",
				ConfigManager.getInstance().getServicePubmedAPIKey());
	
		return paramPairs;
	}
	
	/**
	 * Return pubmed retmax
	 * 
	 * @return
	 */
	public int getServicePubmedRetmax() {
		return Integer.parseInt(this.getServiceProp("pubmed_retmax"));
	}
	
	/**
	 * Return pubmed API key
	 * 
	 * @return
	 */
	public String getServicePubmedAPIKey() {
		return this.getServiceProp("pubmed_api_key");
	}
	
	/**
	 * Return pubmed delay
	 * 
	 * @return
	 */
	public int getServicePubmedDelay() {
		return Integer.parseInt(this.getServiceProp("pubmed_delay"));
	}
	
	/**
	 * Return pubmed search file path
	 * 
	 * @return
	 */
	public String getServicePubmedSearchFp() {
		return this.getServiceProp("pubmed_search_fp");
	}
	
	/**
	 * Return pubmed search file path
	 * 
	 * @return
	 */
	public int getServicePubmedSearchIdMax() {
		return Integer.parseInt(this.getServiceProp("pubmed_search_id_max"));
	}
	
	/**
	 * Return pubmed date pattern
	 * 
	 * @return
	 */
	public String getServicePubmedDatePattern() {
		return this.getServiceProp("pubmed_date_pattern");
	}
	
	/**
	 * Return umls base scheme
	 * 
	 * @return
	 */
	public String getServiceUMLSBaseScheme() {
		return this.getServiceProp("umls_base_scheme");
	}
	
	/**
	 * Return umls base url
	 * 
	 * @return
	 */
	public String getServiceUMLSServiceRawURL() {
		return this.getServiceProp("umls_service_raw_url");
	}
	
	/**
	 * Return umls tgt url
	 * 
	 * @return
	 */
	public String getServiceUMLSTgtURL() {
		return this.getServiceProp("umls_tgt_url");
	}
	
	/**
	 * Return umls rest url
	 * 
	 * @return
	 */
	public String getServiceUMLSRestURL() {
		return this.getServiceProp("umls_rest_url");
	}
	
	/**
	 * Return umls tgt path
	 * 
	 * @return
	 */
	public String getServiceUMLSTgtPath() {
		return this.getServiceProp("umls_tgt_path");
	}
	
	/**
	 * Return umls rest path
	 * 
	 * @return
	 */
	public String getServiceUMLSRestPath() {
		return this.getServiceProp("umls_rest_path");
	}
	
	/**
	 * Return umls api key
	 * 
	 * @return
	 */
	public String getServiceUMLSAPIKey() {
		return this.getServiceProp("umls_api_key");
	}
	
	/**
	 * Return umls search path
	 * 
	 * @return
	 */
	public String getServiceUMLSSearchPath() {
		return this.getServiceProp("umls_search_path");
	}
	
	/**
	 * Return UMLS tgt params
	 * 
	 * @return
	 */
	public Map<String, String> getServiceUMLSTgtParams() {
		Map<String, String> paramPairs = this.extractMapFromProp(
				this.getServiceProp("umls_tgt_params"));
		
		return paramPairs;
	}
	
	/**
	 * Return UMLS search params
	 * 
	 * @return
	 */
	public Map<String, String> getServiceUMLSSearchParams() {
		Map<String, String> paramPairs = this.extractMapFromProp(
				this.getServiceProp("umls_search_params"));
		
		return paramPairs;
	}
	
	/**
	 * Return umls tgt ticket expires
	 * 
	 * @return
	 */
	public int getServiceUMLSTgtTicketExpires() {
		return Integer.parseInt(this.getServiceProp("umls_tgt_ticket_expires"));
	}
	
	/**
	 * Return umls delay
	 * 
	 * @return
	 */
	public int getServiceUMLSDelay() {
		return Integer.parseInt(this.getServiceProp("umls_delay"));
	}
	
	/**
	 * Return umls ticket path
	 * 
	 * @return
	 */
	public String getServiceUMLSTicketPath() {
		return this.getServiceProp("umls_ticket_path");
	}
	
	/**
	 * Return umls version
	 * 
	 * @return
	 */
	public String getServiceUMLSVersion() {
		return this.getServiceProp("umls_version");
	}
	
	/**
	 * Return included sources
	 * 
	 * @return
	 */
	public List<String> getServiceUMLSIncludedSources() {
		return this.extractListFromProp(this.getServiceProp("umls_included_sources"));
	}
	
	/**
	 * Return preferred source
	 * 
	 * @return
	 */
	public String getServiceUMLSPreferredSource() {
		return this.getServiceProp("umls_preferred_source");
	}
	
	/**
	 * Return result limit
	 * 
	 * @return
	 */
	public int getServiceUMLSResultLimit() {
		return Integer.parseInt(this.getServiceProp("umls_result_limit"));
	}
	
	/**
	 * Return synonym tag
	 * 
	 * @return
	 */
	public String getServiceUMLSSynonymTag() {
		return this.getServiceProp("umls_synonym_tag");
	}
	
	/**
	 * Return preferred term tag
	 * 
	 * @return
	 */
	public String getServiceUMLSPreferredTermTag() {
		return this.getServiceProp("umls_preferred_term_tag");
	}
	
	/**
	 * Return Stanford NLP annotators
	 * 
	 * @return
	 */
	public String getServiceStanfordNLPAnnotators() {
		return this.getServiceProp("stanford_nlp_annotators");
	}
	
	/**
	 * Return Stanford NLP sentence splitter
	 * 
	 * @return
	 */
	public String getServiceStanfordNLPssplit() {
		return this.getServiceProp("stanford_nlp_ssplit");
	}
	
	/**
	 * Return Stanford NLP dependency parsing
	 * 
	 * @return
	 */
	public String getServiceStanfordNLPdepparse() {
		return this.getServiceProp("stanford_nlp_depparse");
	}
	
	/**
	 * Return Stanford NLP algorithm
	 * 
	 * @return
	 */
	public String getServiceStanfordNLPAlgorithm() {
		return this.getServiceProp("stanford_nlp_algorithm");
	}
	
	/**
	 * Return Stanford NLP process fp
	 * 
	 * @return
	 */
	public String getServiceStanfordNLPProcessFP() {
		return this.getServiceProp("stanford_nlp_process_fp");
	}
	
	/**
	 * Return Stanford NLP depparse fp
	 * 
	 * @return
	 */
	public String getServiceStanfordNLPDepparseFP() {
		return this.getServiceProp("stanford_nlp_depparse_fp");
	}
	
	/**
	 * Return Stanford NLP pos fp
	 * 
	 * @return
	 */
	public String getServiceStanfordNLPPosFP() {
		return this.getServiceProp("stanford_nlp_pos_fp");
	}
	
	/**
	 * Return http url encoding delay
	 * 
	 * @return
	 */
	public String getManagerHttpURLEncoding() {
		return this.getManagerProp("http_url_encoding");
	}
	
	/**
	 * Return http post default
	 * 
	 * @return
	 */
	public String getManagerHttpHttppostDefault() {
		return this.getManagerProp("http_httppost_default");
	}
	
	/**
	 * Return http post json
	 * 
	 * @return
	 */
	public String getManagerHttpHttppostJson() {
		return this.getManagerProp("http_httppost_json");
	}
	
	/**
	 * Return http get json
	 * 
	 * @return
	 */
	public String getManagerHttpHttpgetJson() {
		return this.getManagerProp("http_httpget_json");
	}
	
	/**
	 * Return regexp get file name in filepath
	 * 
	 * @return
	 */
	public String getRegexpFileNameInFP() {
		return this.getRegexpProp("file_name_in_fp");
	}
	
	/**
	 * Return file csv delimeter
	 * 
	 * @return
	 */
	public String getFileCSVDelimeter() {
		return this.getFileProp("csv_del");
	}
}
