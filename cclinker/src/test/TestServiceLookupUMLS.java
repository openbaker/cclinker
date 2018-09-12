/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import backend.services.lookup.UMLS;
import gov.nih.nlm.uts.webservice.AtomDTO;
import gov.nih.nlm.uts.webservice.UiLabel;

/**
 * @author schiend
 *
 */
class TestServiceLookupUMLS {

	/**
	 * Test method for {@link backend.services.lookup.UMLS#findConcepts(String, int, String)}.
	 */
	void testFindConcepts() {
		String term = "dendritic-cell";
		int limit = 10;
		
		// call find concepts
		List<UiLabel> results = UMLS.findConcepts(term, limit);
		
		assertEquals(limit, results.size());
		
		// is the first "Dendritic cells"?
		assertEquals("C0011306", results.get(0).getUi());
	}
	
	/**
	 * Test method for {@link backend.services.lookup.UMLS#getConceptAtoms(String)}.
	 */
	void testGetConceptAtoms() {
		String code = "C0011306"; // Dendritic cell
		
		// get atoms
		List<AtomDTO> results = UMLS.getConceptAtoms(code);
		
		assertEquals(7, results.size());
	}
	
	/**
	 * Test method for {@link backend.services.lookup.UMLS#getNormalisedTermByCode(java.lang.String)}.
	 */
	void testGetNormalisedTermByCode() {
		String code = "C0011306";
		String expectedTerm = "Dendritic Cells";
		
		String actualTerm = UMLS.getNormalisedTermByCode(code);
		
		assertEquals(expectedTerm, actualTerm);
	}
	
	/**
	 * Test method for {@link backend.services.lookup.UMLS#getNormalisedTermByTerm(java.lang.String)}.
	 */
	void testGetNormalisedTermByTerm() {
		String term = "dendritic-cells";
		String expectedTerm = "Dendritic Cells";
		
		String actualTerm = UMLS.getNormalisedTermByTerm(term);
		
		assertEquals(expectedTerm, actualTerm);
	}

	/**
	 * Test method for {@link backend.services.lookup.UMLS#getSynonymsOfCode(java.lang.String)}.
	 */
	void testGetSynonymsOfCode() {
		String code = "C0011306";
		int expectedSynonyms = 5;
		
		List<AtomDTO> atoms = UMLS.getSynonymsOfCode(code);
		
		assertEquals(expectedSynonyms, atoms.size());
	}
	
	/**
	 * Test method for {@link backend.services.lookup.UMLS#getSynonymsOfTerm(java.lang.String)}.
	 */
	void testGetSynonymsOfTerm() {
		String term = "dendritic-cell";
		int expectedSynonyms = 5;
		
		List<AtomDTO> atoms = UMLS.getSynonymsOfTerm(term);
		
		assertEquals(expectedSynonyms, atoms.size());
	}

	/**
	 * Test method for {@link backend.services.lookup.UMLS#getTermDefinition(java.lang.String)}.
	 */
	@Test
	void testGetTermDefinition() {
		String term = "dendritic-cell";
		String expected = "Immunocompetent cells of the lymphoid and hemopoietic systems and skin.";
		
		// get definition
		String actual = UMLS.getTermDefinition(term);
		
		assertEquals(expected, actual.substring(0, expected.length()));
	}

	/**
	 * Test method for {@link backend.services.lookup.UMLS#getUMLSUseTicket(java.lang.String)}.
	 */
	void testGetUMLSUseTicket() {
		// TGT ticket
		String tgtTicket = UMLS.getUMLSTgtTicket();
		assertFalse(tgtTicket.isEmpty());
		
		// use ticket
		String useTicket = UMLS.getUMLSUseTicket(tgtTicket);
		assertFalse(useTicket.isEmpty());
		
		// validate ticket
		assertTrue(UMLS.validateUMLSUseTicket(useTicket));
	}

}
