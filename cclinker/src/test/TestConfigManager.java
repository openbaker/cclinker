/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import backend.manager.ConfigManager;

/**
 * @author schiend
 *
 */
class TestConfigManager {

	@Test
	void getServicePubmedSearchParams() {
		Map<String, String> paramMap = new HashMap<>();
		Map<String, String> propMap = new HashMap<>();
		
		// build map
		paramMap.put("db", "pubmed");
		paramMap.put("term", "");
		paramMap.put("usehistory", "y");
		paramMap.put("retstart", "");
		paramMap.put("retmax", "");
		paramMap.put("api_key", "");
		
		// get from config
		propMap = ConfigManager.getInstance().getServicePubmedSearchParams();
		
		// compare
		assertEquals(paramMap, propMap);
	}

}
