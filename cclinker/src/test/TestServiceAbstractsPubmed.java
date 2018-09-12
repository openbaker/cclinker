/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import backend.services.Pubmed;
import backend.services.UMLS;

/**
 * @author schiend
 *
 */
class TestServiceAbstractsPubmed {

	/**
	 * Test method for {@link backend.services.Pubmed#getAbstractsWithTerm(String)}.
	 */
	void testGetAbstractsWithTerm() {
		String term = "Dendritic cell fibroblast interaction";
		int expected = 142;
		
		int actual = Pubmed.getAbstractsWithTerm(term,
				Pubmed.buildDateParams(
						new GregorianCalendar(2007, 1, 1).getTime(),
						new GregorianCalendar(2017, 12, 31).getTime()
						));
		
		assertEquals(expected, actual);
	}
	
	/**
	 * Test method for {@link backend.services.Pubmed#getAbstractsWithTerm(String)}.
	 */
	@Test
	void testGetAbstractsWithSynonyms() {
		String term = "dendritic cells";
		int expected = 951;
		
		// get synonyms for term
		List<String> synonyms = UMLS.getSynonymsOfTerm(term);
		
		int actual = Pubmed.getAbstractsWithTerms(synonyms,
				Pubmed.buildDateParams(
						new GregorianCalendar(2017, 10, 1).getTime(),
						new GregorianCalendar(2017, 12, 31).getTime()
						));
		
		assertEquals(expected, actual);
	}

}
