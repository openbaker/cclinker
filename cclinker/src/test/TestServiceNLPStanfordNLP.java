/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import backend.services.Pubmed;
import backend.services.StanfordNLP;

/**
 * @author dominiks
 *
 */
class TestServiceNLPStanfordNLP {
	
	/**
	 * Test method for {@link backend.services.nlp.StanfordNLP#splitAbstractSentences(List<String> keywords, String filePath)}.
	 */
	void testSplitAbstractSentences() {
		// define term
		String term = "dendritic cells";
		
		// define keywords
		//List<String> keywords = UMLS.getSynonymsOfTerm(
		//		term, true, Arrays.asList("DC", "DCs"));
		
		List<String> keywords = Arrays.asList(
				"Dendritic Cell",
				"Dendritic Cells",
				"cell dendritic",
				"cell dendritics",
				"cells dendritic",
				"dendritic cell",
				"dendritic cells",
				"DC", "DCs");
		
		// define pubmed result file
		String fileName = Pubmed.getSearchIDFile(180911, 1234567890);
		
		// split abstract sentences
		int numAbstracts = StanfordNLP.splitAbstractSentencesWithKeywords(keywords, fileName);
		
		assertEquals(10, numAbstracts);
	}
	
	/**
	 * Test to extract concepts from retrieved pubmed sentences
	 */
	@Test
	void testExtractConceptsFromSentences() {
		List<String> cellList = Arrays.asList(
				"Dendritic Cell",
				"Dendritic Cells",
				"cell dendritic",
				"cell dendritics",
				"cells dendritic",
				"dendritic cell",
				"dendritic cells",
				"DC", "DCs");
		List<String> key1 = Arrays.asList(
				"resting");
		List<String> key2 = Arrays.asList(
				"activated");
		List<String> key3 = Arrays.asList(
				"mature");
		List<String> key4 = Arrays.asList(
				"phagocytic",
				"phagocytosis");
		
		// get dendritic cells and associated terms
		List<List<String>> searchTerms = new ArrayList<>();
		searchTerms.add(cellList);
		searchTerms.add(key2);
		
		/*
		int[] id = Pubmed.getAbstractsWithTermsList(searchTerms,
				Pubmed.buildDateParams(
						new GregorianCalendar(2017, 1, 1).getTime(),
						new GregorianCalendar(2017, 12, 31).getTime()
						));
		*/
		
		int[] id = {180914, 655497487};
		
		// define pubmed result file
		String fileName = Pubmed.getSearchIDFile(id[0], id[1]);
		
		// split abstract sentences
		int numAbstracts = StanfordNLP.splitAbstractSentencesWithKeywordsList(
				searchTerms, fileName, true);
		
		String fpDepparse = StanfordNLP.buildFileName(
				Pubmed.getSearchIDFile(id[0], id[1]), StanfordNLP.TYPE_DEPPARSE);
		
		// extract concepts from sentences
		StanfordNLP.extractConceptsFromSentences(id, cellList);
	}

}
