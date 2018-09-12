/**
 * 
 */
package backend.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import backend.manager.ConfigManager;
import backend.services.LookupService;
import gov.nih.nlm.uts.webservice.AtomDTO;
import gov.nih.nlm.uts.webservice.DefinitionDTO;
import gov.nih.nlm.uts.webservice.Psf;
import gov.nih.nlm.uts.webservice.UiLabel;
import gov.nih.nlm.uts.webservice.UtsFault_Exception;
import gov.nih.nlm.uts.webservice.UtsWsContentController;
import gov.nih.nlm.uts.webservice.UtsWsContentControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsFinderController;
import gov.nih.nlm.uts.webservice.UtsWsFinderControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsHistoryController;
import gov.nih.nlm.uts.webservice.UtsWsHistoryControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsMetadataController;
import gov.nih.nlm.uts.webservice.UtsWsMetadataControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsSecurityController;
import gov.nih.nlm.uts.webservice.UtsWsSecurityControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsSemanticNetworkController;
import gov.nih.nlm.uts.webservice.UtsWsSemanticNetworkControllerImplService;

/**
 * @author schiend
 *
 */
public class UMLS implements LookupService {
	
	// find terms
	private static int FIND_CONCEPTS = 0;
	private static int FIND_ATOMS = 1;
	
	// get atoms
	private static int GET_CONCEPT_ATOMS = 1;
	
	/**
	 * get UMLS security controller
	 * 
	 * @return
	 */
	private static UtsWsSecurityController getUtsWsSecurityController() {
		UtsWsSecurityController utsSecurityService;
		utsSecurityService = (new UtsWsSecurityControllerImplService())
				.getUtsWsSecurityControllerImplPort();
		
		return utsSecurityService;
	}
	
	/**
	 * get UMLS content controller
	 * 
	 * @return
	 */
	private static UtsWsContentController getUtsWsContentController() {
		UtsWsContentController utsContentService;
		utsContentService = (new UtsWsContentControllerImplService())
				.getUtsWsContentControllerImplPort();
		
		return utsContentService;
	}
	
	/**
	 * get UMLS finder controller
	 * 
	 * @return
	 */
	private static UtsWsFinderController getUtsWsFinderController() {
		UtsWsFinderController utsFinderService;
		utsFinderService = (new UtsWsFinderControllerImplService())
				.getUtsWsFinderControllerImplPort();
		
		return utsFinderService;
	}
	
	/**
	 * get UMLS metadata controller
	 * 
	 * @return
	 */
	private static UtsWsMetadataController getUtsWsMetadataController() {
		UtsWsMetadataController utsMetadataService;
		utsMetadataService = (new UtsWsMetadataControllerImplService())
				.getUtsWsMetadataControllerImplPort();
		
		return utsMetadataService;
	}
	
	/**
	 * get UMLS semantic network controller
	 * 
	 * @return
	 */
	private static UtsWsSemanticNetworkController getUtsWsSemanticNetworkController() {
		UtsWsSemanticNetworkController utsSemanticNetworkService;
		utsSemanticNetworkService = (new UtsWsSemanticNetworkControllerImplService())
				.getUtsWsSemanticNetworkControllerImplPort();
		
		return utsSemanticNetworkService;
	}
	
	/**
	 * get UMLS history controller
	 * 
	 * @return
	 */
	private static UtsWsHistoryController getUtsWsHistoryController() {
		UtsWsHistoryController utsHistoryService;
		utsHistoryService = (new UtsWsHistoryControllerImplService())
				.getUtsWsHistoryControllerImplPort();
		
		return utsHistoryService;
	}
	
	/**
	 * Get normalised atom
	 * 
	 * @param code
	 * @return
	 */
	public static AtomDTO getNormalisedAtomByCode(String code) {
		AtomDTO atom = null;
		
		try {
			atom = getUtsWsContentController().getDefaultPreferredAtom(
					getUMLSUseTicket(),
					getCurrentUMLSVersion(), code,
					ConfigManager.getInstance().getServiceUMLSPreferredSource());
		} catch (UtsFault_Exception e) {
			e.printStackTrace();
		}
		
		return atom;
	}
	
	/**
	 * Get normalised Term
	 * 
	 * @param code
	 * @return
	 */
	public static String getNormalisedTermByCode(String code) {
		String normTerm = new String();
		
		AtomDTO atom = getNormalisedAtomByCode(code);
		
		// get normalised name
		if (atom != null) {
			normTerm = atom.getTermString().getName();
		}
		
		return normTerm;
	}
	
	/**
	 * Get normalised Term
	 * 
	 * @param term
	 * @return
	 */
	public static AtomDTO getNormalisedAtomByTerm(String term) {
		// find concepts
		List<UiLabel> results = UMLS.findConcepts(term);
		
		// take first hit
		String firstUi = results.get(0).getUi();
		
		// get normalised term
		return getNormalisedAtomByCode(firstUi);
	}
	
	/**
	 * Get normalised Term
	 * 
	 * @param term
	 * @return
	 */
	public static String getNormalisedTermByTerm(String term) {
		String normTerm = new String();
		
		// get normalised atom
		AtomDTO atom = getNormalisedAtomByTerm(term);
		
		// get normalised name
		if (atom != null) {
			normTerm = atom.getTermString().getName();
		}
		
		return normTerm;
	}

	/**
	 * Get synonyms of code
	 * 
	 * @param code
	 * @param includePT
	 * @return
	 */
	public static List<AtomDTO> getSynonymsOfCode(String code, boolean includePT) {
		List<AtomDTO> synonymAtoms = new ArrayList<>();
		
		// get concept atoms
		List<AtomDTO> atoms = getConceptAtoms(code);
		
		// filter for synonyms
		for (AtomDTO atom: atoms) {
			boolean isSY = atom.getTermType().equals(ConfigManager.getInstance().getServiceUMLSSynonymTag());
			boolean isPT = atom.getTermType().equals(ConfigManager.getInstance().getServiceUMLSPreferredTermTag());
			
			if (isSY || (includePT == true && isPT)) {
				synonymAtoms.add(atom);
			}
		}
		
		return synonymAtoms;
	}
	
	/**
	 * Get synonyms of code
	 * 
	 * @param code
	 * @return
	 */
	public static List<AtomDTO> getSynonymsOfCode(String code) {
		List<AtomDTO> synonymAtoms = new ArrayList<>();
		
		// get concept atoms
		List<AtomDTO> atoms = getConceptAtoms(code);
		
		// filter for synonyms
		for (AtomDTO atom: atoms) {
			System.out.println(">> Term type: " + atom.getTermType());
			
			if (atom.getTermType().equals(ConfigManager.getInstance().getServiceUMLSSynonymTag())) {
				synonymAtoms.add(atom);
			}
		}
		
		return synonymAtoms;
	}
	
	/**
	 * Get synonym atoms of term
	 * 
	 * @param term
	 * @param includePT
	 * @return
	 */
	public static List<AtomDTO> getSynonymAtomsOfTerm(String term, boolean includePT) {
		// get normalised atom for term
		AtomDTO atom = getNormalisedAtomByTerm(term);
		
		// get synonyms of atom
		List<AtomDTO> atoms = getSynonymsOfCode(atom.getConcept().getUi(), includePT);
		
		return atoms;
	}
	
	/**
	 * Get synonym atoms of term
	 * 
	 * @param term
	 * @return
	 */
	public static List<AtomDTO> getSynonymAtomsOfTerm(String term) {
		return getSynonymAtomsOfTerm(term, false);
	}
	
	/**
	 * Get synonyms of term
	 * 
	 * @param term
	 * @param includePT
	 * @param createAbbreviation
	 * @return
	 */
	public static List<String> getSynonymsOfTerm(String term, boolean includePT, List<String> abbreviations) {
		List<String> synonyms = new ArrayList<>();
		
		// get synonym atoms
		List<AtomDTO> atoms = getSynonymAtomsOfTerm(term, includePT);
		
		// go through atoms get strings
		for (AtomDTO atom: atoms) {
			synonyms.add(atom.getTermString().getName());
		}
		
		// create simplified abbreviation
		if (abbreviations != null) {
			synonyms.addAll(abbreviations);
		}
		
		return synonyms;
	}
	
	/**
	 * @see getSynonymsOfTerm(String term, boolean includePT, List<String> abbreviations)
	 * 
	 * @param term
	 * @param includePT
	 * @return
	 */
	public static List<String> getSynonymsOfTerm(String term, boolean includePT) {
		return getSynonymsOfTerm(term, includePT, null);
	}
	
	/**
	 * @see getSynonymsOfTerm(String term, boolean includePT, List<String> abbreviations)
	 * 
	 * @param term
	 * @return
	 */
	public static List<String> getSynonymsOfTerm(String term) {
		return getSynonymsOfTerm(term, false, null);
	}

	/**
	 * Get code definition
	 * 
	 * @param term
	 * @return
	 */
	public static String getCodeDefinition(String code) {
		String result = new String();
		
		// get definitions
		List<DefinitionDTO> definitions = new ArrayList<DefinitionDTO>();
		try {
			definitions = getUtsWsContentController().getConceptDefinitions(
					getUMLSUseTicket(),
					getCurrentUMLSVersion(), code, buildPsf());
			
			// find definition from preferred source
			for (DefinitionDTO definition: definitions) {
				if (result.isEmpty() && definition.getRootSource().equals(
						ConfigManager.getInstance().getServiceUMLSPreferredSource())) {
					result = definition.getValue();
				}
			}
		} catch (UtsFault_Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Get term definition
	 * 
	 * @param term
	 * @return
	 */
	public static String getTermDefinition(String term) {
		// get normalised atom and code
		AtomDTO atom = getNormalisedAtomByTerm(term);
		String code = atom.getConcept().getUi();
		
		return getCodeDefinition(code);
	}
	
	/**
	 * Find term
	 * 
	 * @param term
	 * @param searchType
	 * @param limit
	 * @return
	 */
	public static List<UiLabel> findTerm(String term, int searchType, int limit,
			List<String> includedSources) {
		List<UiLabel> results = new ArrayList<>();
		List<UiLabel> searchResults = new ArrayList<>();
				
		Psf psf = buildPsf(includedSources);
		int pageNum = 1;
		
		// get current release
		String currentRelease = getCurrentUMLSVersion();
		
		// set limit to results
		boolean limitReached = false;
		        
		// go through pages
	    do {
	    	try {
	    		psf.setPageNum(pageNum);
	    		
	    		// get results
	    		if (searchType == FIND_CONCEPTS) {
					searchResults =
							getUtsWsFinderController().findConcepts(
									getUMLSUseTicket(),
									currentRelease, "atom", term, "words", psf);
	    		} else if (searchType == FIND_ATOMS) {
	    			searchResults =
							getUtsWsFinderController().findAtoms(
									getUMLSUseTicket(),
									currentRelease, "atom", term, "words", psf);
	    		} 
				
				// save results
		        for (UiLabel result: searchResults) {
		        	// has the limit been reached?
		        	if (limit <= 0 || (limit > 0 && limit > results.size())) {
		        		results.add(result);
		        	} else {
		        		limitReached = true;
		        	}
				}
			} catch (UtsFault_Exception e) {
				e.printStackTrace();
			}
	        
	        pageNum++;
	    } while (searchResults.size() > 0 && limitReached == false);
	    
	    return results;
	}
	
	/**
	 * Find concepts
	 * 
	 * @param term
	 * @param limit
	 * @return
	 */
	public static List<UiLabel> findConcepts(String term, int limit) {
		return findTerm(term, FIND_CONCEPTS, limit, null);
	}
	
	/**
	 * Find concepts
	 * 
	 * @param term
	 * @return
	 */
	public static List<UiLabel> findConcepts(String term) {
		return findTerm(term, FIND_CONCEPTS,
				ConfigManager.getInstance().getServiceUMLSResultLimit(), null);
	}
	
	/**
	 * Find atoms
	 * 
	 * @param term
	 * @param limit
	 * @return
	 */
	public static List<UiLabel> findAtoms(String term, int limit) {
		return findTerm(term, FIND_ATOMS, limit, null);
	}
	
	/**
	 * Find atoms
	 * 
	 * @param term
	 * @param limit
	 * @param includedSources
	 * @return
	 */
	public static List<UiLabel> findAtoms(String term, int limit, List<String> includedSources) {
		return findTerm(term, FIND_ATOMS, limit, includedSources);
	}
	
	/**
	 * Get concept atoms
	 * 
	 * @param code
	 * @param searchType
	 * @param includedSources
	 * @return
	 */
	public static List<AtomDTO> getAtoms(String code, int searchType,
			List<String> includedSources) {
		List<AtomDTO> results = new ArrayList<>();
		
		Psf psf = buildPsf(includedSources);

		// get current release
		String currentRelease = getCurrentUMLSVersion();
		        
    	try {
    		// get results
    		if (searchType == GET_CONCEPT_ATOMS) {
				results =
						getUtsWsContentController().getConceptAtoms(
								getUMLSUseTicket(),
								currentRelease, code, psf);
    		}
		} catch (UtsFault_Exception e) {
			e.printStackTrace();
		}
	    
	    return results;
	}
	
	/**
	 * Get concept atoms
	 * 
	 * @param code
	 * @param includedSources
	 * @return
	 */
	public static List<AtomDTO> getConceptAtoms(String code) {
		return getAtoms(code, GET_CONCEPT_ATOMS, null);
	}
	
	/**
	 * Build Paging, sorting and filtering
	 * 
	 * @param includedSources
	 * @return
	 */
	private static Psf buildPsf(List<String> includedSources) {
		Psf psf = new Psf();
		//exclude suppressible + obsolete term matches
		psf.setIncludeObsolete(false);
		psf.setIncludeSuppressible(false);
		
		// set included source
		List<String> sourcesToInclude = new ArrayList<>();
		if (includedSources != null) {
			sourcesToInclude = includedSources;
		} else {
			sourcesToInclude = ConfigManager.getInstance().getServiceUMLSIncludedSources();
		}
		
		for (String source: sourcesToInclude) {
			psf.getIncludedSources().add(source);
		}
		
		return psf;
	}
	
	/**
	 * Build PSF
	 * 
	 * @return
	 */
	private static Psf buildPsf() {
		return buildPsf(null);
	}
	
	/**
	 * Get current UMLS version
	 * 
	 * @return
	 */
	public static String getCurrentUMLSVersion() {
		String currentRelease = new String();
		
		try {
			currentRelease = getUtsWsMetadataController().getCurrentUMLSVersion(
					getUMLSUseTicket());
		} catch (UtsFault_Exception e) {
			e.printStackTrace();
		}
		
		return currentRelease;
	}
	
	/**
	 * Get TGT ticket
	 * 
	 * @return
	 */
	public static String getUMLSTgtTicket() {
		String ticket = null;
		
		// has TGT ticket expired?
		File tgtFile = new File(ConfigManager.getInstance().getServiceUMLSTicketPath());
		
		// how many hours since the ticket was requested?
		int hours = (int) TimeUnit.MILLISECONDS.toHours(
				System.currentTimeMillis() - tgtFile.lastModified());
		
		// has the ticket expired?
		if (hours < ConfigManager.getInstance().getServiceUMLSTgtTicketExpires()) {
			// load previous ticket
			try {
				BufferedReader reader;
				reader = new BufferedReader(
						new FileReader(tgtFile.getAbsoluteFile()));
				
				ticket = reader.readLine(); // ignore first row
				
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// get a new ticket
			try {
				ticket = getUtsWsSecurityController().getProxyGrantTicketWithApiKey(
								ConfigManager.getInstance().getServiceUMLSAPIKey());
			} catch (UtsFault_Exception e) {
				e.printStackTrace();
			}
		}
		
		return ticket;
	}
	
	/**
	 * Get use ticket
	 * 
	 * @return
	 */
	public static String getUMLSUseTicket(String tgtTicket) {
		String ticket = new String();
		
		try {
			// get tgt ticket?
			if (tgtTicket == null) {
				tgtTicket = getUMLSTgtTicket();
			}
			
			// get ticket
			ticket = getUtsWsSecurityController().getProxyTicket(
					tgtTicket, ConfigManager.getInstance().getServiceUMLSServiceRawURL());
		} catch (UtsFault_Exception e) {
			e.printStackTrace();
		}
		
		return ticket;
	}
	
	/**
	 * @see getUMLSUseTicket(String tgtTicket)
	 * 
	 * @return
	 */
	public static String getUMLSUseTicket() {
		return getUMLSUseTicket(null);
	}
	
	/**
	 * Validate use ticket
	 * 
	 * @param useTicket
	 * @return
	 */
	public static boolean validateUMLSUseTicket(String useTicket) {
		boolean validated = false;
		
		try {
			// validate ticket
			String validateTicket = getUtsWsSecurityController().validateProxyTicket(
					useTicket, ConfigManager.getInstance().getServiceUMLSServiceRawURL());
			
			// check against API key
			if (validateTicket.equals(ConfigManager.getInstance().getServiceUMLSAPIKey())) {
				validated = true;
			}
		} catch (UtsFault_Exception e) {
			e.printStackTrace();
		}
		
		return validated;
	}

}
