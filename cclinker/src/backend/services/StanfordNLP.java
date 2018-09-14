/**
 * 
 */
package backend.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import backend.manager.ConfigManager;
import edu.stanford.nlp.io.EncodingPrintWriter.out;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

/**
 * @author dominiks
 *
 */
public class StanfordNLP {

	public static int TYPE_PROCESS = 0;
	public static int TYPE_POS = 1;
	public static int TYPE_DEPPARSE = 2;
	
	/**
	 * Split sentences from abstracts
	 * 
	 * @param keywordsList
	 * @param filepath
	 * @return
	 */
	public static int splitAbstractSentencesWithKeywordsList(
			List<List<String>> keywordsList, String filepath, boolean saveOnlyMatches) {
		int numAbstracts = 0;
		
		// load abstract file
		BufferedReader br;

		// prepare csv file
		File file = new File(buildFileName(filepath, TYPE_PROCESS));
		if (file.exists()) {
			file.delete();
		}
		
		try {
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			// prepare file handler
			br = new BufferedReader(new FileReader(filepath));
					
			// for abstract
		    StringBuilder abstractText = new StringBuilder();
		    int pmid = 0;
		    
		    // for csv
			StringBuilder csvText = new StringBuilder();
		    
		    String line = new String();
		    
		    while ((line = br.readLine()) != null) {
		    	// extract PMID and abstract text
	    		if (StringUtils.isNumeric(line.trim())) {
		    		pmid = Integer.parseInt(line);
		    		
		    	} else if (line.trim().isEmpty() != true) {
		    		if (pmid > 0) {
			    		abstractText.append(line);
			    		abstractText.append(System.lineSeparator());
		    		}
		    	}
		    	
	    		boolean abstractComplete = (pmid > 0 && abstractText.length() > 0);
	    		boolean lastLine = (line.trim().isEmpty() != true);
	    		
	    		if (abstractComplete && lastLine) {
		    		// split sentences
		    		CoreDocument document = splitSentences(abstractText.toString());
		    		
		    		// go through sentences and store in csv
		    		int sentenceCount = 0;
		    		for (CoreSentence sentence: document.sentences()) {
		    			int listMatches = 0;
		    			boolean allMatched = false;
		    			
		    			// does the sentence contain any of the keywords?
		    			for (List<String> keywords: keywordsList) {
		    				int matches = 0;
		    				
			    			for (String keyword: keywords) {
			    				matches += (Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE)
			    						.matcher(sentence.text()).find() ? 1 : 0);
			    			}
			    			
			    			listMatches += (matches > 0) ? 1 : 0;
		    			}
		    			
		    			// all lists matched?
		    			allMatched = (listMatches == keywordsList.size());
		    			
		    			if (saveOnlyMatches == false || allMatched == true) {
			    			// save sentence to csv
			    			csvText.append(pmid);
			    			csvText.append(ConfigManager.getInstance().getFileCSVDelimeter());
			    			csvText.append(sentenceCount++);
			    			csvText.append(ConfigManager.getInstance().getFileCSVDelimeter());
			    			csvText.append((allMatched) ? 1 : 0);
			    			csvText.append(ConfigManager.getInstance().getFileCSVDelimeter());
			    			csvText.append("\"" + sentence.text() + "\"");
			    			csvText.append(System.lineSeparator());
		    			}
		    		}
		    		
		    		// write to file
		    		bw.write(csvText.toString());
		    		
		    		// next abstract
		    		pmid = 0;
		    		abstractText = new StringBuilder();
		    		csvText = new StringBuilder();
		    		
		    		numAbstracts++;
	    		}
		    }
		    
		    // close reader
		    br.close();
		    
		    // close writer
		    bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return numAbstracts;
	}
	
	/**
	 * Split sentences from abstracts
	 * 
	 * @param keywords
	 * @param filepath
	 * @return
	 */
	public static int splitAbstractSentencesWithKeywords(
			List<String> keywords, String filepath, boolean saveOnlyMatches) {
		// create a dummy list with one entry
		List<List<String>> keywordsList = new ArrayList<>();
		
		keywordsList.add(keywords);
		
		return splitAbstractSentencesWithKeywordsList(keywordsList, filepath, saveOnlyMatches);
	}
	
	/**
	 * @see splitAbstractSentencesWithKeywords(List<String> keywords, String filepath, boolean saveOnlyMatches)
	 * 
	 * @param keywords
	 * @param filepath
	 * @param saveOnlyMatches
	 * @return
	 */
	public static int splitAbstractSentencesWithKeywords(
			List<String> keywords, String filepath) {
		return splitAbstractSentencesWithKeywords(keywords, filepath, true);
	}
	
	/**
	 * Get filepath based on type
	 * 
	 * @param type
	 * @return
	 */
	private static String getFilepath(int type) {
		String filepath = new String();
		
		// which filepath?
		if (type == TYPE_PROCESS) {
			filepath = ConfigManager.getInstance().getServiceStanfordNLPProcessFP();
		} else if (type == TYPE_POS) {
			filepath = ConfigManager.getInstance().getServiceStanfordNLPPosFP();
		} else if (type == TYPE_DEPPARSE) {
			filepath = ConfigManager.getInstance().getServiceStanfordNLPDepparseFP();
		}
		
		return filepath;
	}
	
	/**
	 * Build filename from other service
	 * 
	 * @param filePath
	 * @param type
	 * @return
	 */
	public static String buildFileName(String serviceFilepath, int type) {
		String filename = new String();
		String filepath = new String();
		
		// extract filename
		Matcher match = 
				Pattern.compile(ConfigManager.getInstance().getRegexpFileNameInFP())
				.matcher(serviceFilepath);
		
		// build filepath
		if (match.find()) {
				filename = match.group(0);
				filename += ".csv";
				
				filepath = getFilepath(type);
					
				filepath += filename;
		}
				
		return filepath;
	}
	
	/**
	 * Build filename from id
	 * 
	 * @param id
	 * @param type
	 * @return
	 */
	public static String buildFileName(int[] id, int type) {
		String filename = new String();
		String filepath = new String();
		
		if (id.length == 2) {
			filename = Integer.toString(id[0]) + Integer.toString(id[1]);
			filename += ".csv";
			
			filepath = getFilepath(type);
				
			filepath += filename;
		}
				
		return filepath;
	}

	/**
	 * Split sentences
	 * 
	 * @param string
	 * @return
	 */
	private static CoreDocument splitSentences(String text) {
		// set properties for processing
		Properties props = new Properties();
	    props.setProperty("annotators", ConfigManager.getInstance().getServiceStanfordNLPssplit());
	    props.setProperty("coref.algorithm", ConfigManager.getInstance().getServiceStanfordNLPAlgorithm());
	    
	    // build pipeline
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    // create a document
	    CoreDocument document = new CoreDocument(text);
	    
	    // annnotate the document
	    pipeline.annotate(document);
	    
	    return document;
	}
	
	/**
	 * Extract concepts from sentences
	 * 
	 * @param int[]
	 */
	public static void extractConceptsFromSentences(int[] id, List<String> keywords) {
		// filter sentences for the ones having the keywords, [2] > 0

		// prepare input/output
		File inputFile = new File(buildFileName(id, TYPE_PROCESS));
		File outputFile = new File(buildFileName(id, TYPE_DEPPARSE));
		
		if (outputFile.exists()) {
			outputFile.delete();
		}
		
		// process text
		Properties props = new Properties();
	    props.setProperty("annotators", ConfigManager.getInstance().getServiceStanfordNLPdepparse());
	    props.setProperty("coref.algorithm", ConfigManager.getInstance().getServiceStanfordNLPAlgorithm());
	    
	    // build pipeline
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    // prepare to store nlp information
	    StringBuilder abstractText = new StringBuilder();
	    
	    // get realations to look for
	    List<String> relations = ConfigManager.getInstance().getServiceStanfordNLPKeywordRelations();
	    
	    // load CSV and go through sentences
	    Reader reader;
		try {
			reader = Files.newBufferedReader(Paths.get(
					buildFileName(id, TYPE_PROCESS)));
			
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
		    
			int count = 0;
		    for (CSVRecord csvRecord : csvParser) {
		    	// get sentence
		    	String sentenceToAnnotate = csvRecord.get(3);
		    	
			    // create a document
			    Annotation document = new Annotation(sentenceToAnnotate);
			    
			    // get dependencies from sentence
			    pipeline.annotate(document);
			    
			    // for each sentence you can get a list of collapsed dependencies as follows
			    for (CoreMap sentence: document.get(SentencesAnnotation.class)) {			    	
			    	// get dependencies
			    	SemanticGraph dependencies =
			    			sentence.get(BasicDependenciesAnnotation.class);
			    	
			    	// find positions of keywords
			    	List<IndexedWord> idxWords = findPositionsOfKeywords(
			    			dependencies, keywords);
			    	
			    	// print sentence
			    	System.out.println(sentence.toString());
			    	
			    	String associatedTerm = new String();
			    	
			    	// get governor of keywords
			    	for (IndexedWord idxWord: idxWords) {
			    		// get outgoing edges
			    		Iterator<SemanticGraphEdge> outgoing =
			    				dependencies.incomingEdgeIterator(idxWord);
			    		
			    		// go through edges
			    		while (outgoing.hasNext()) {
			    			SemanticGraphEdge curEdge = outgoing.next();
			    			
			    			System.out.println(
				    				">> " + curEdge.getDependent().word()
				    				+ "\t| " + curEdge.getDependent().index()
				    				+ "\t| " + curEdge.getGovernor().word()
				    				+ "\t| " + curEdge.getGovernor().index()
				    				+ "\t| " + curEdge.getRelation().toString()
				    				);
			    			
			    			// found a relevant term?
				    		if (relations.contains(curEdge.getRelation().toString())) {
				    			associatedTerm = curEdge.getGovernor().word();
				    		}
			    		}
			    	}
			    	
			    	System.out.println(">> Associated term: " + associatedTerm);
			    }
		    }
		    
		    // close parser
		    csvParser.close();
			
			// go through and save lemma into map with pos as key and lemma as value
			
			// save as csv for each pmid
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}
	
	/**
	 * Find positions of keywords in sentence
	 * 
	 * @param sentence
	 * @param keywords
	 * @return
	 */
	private static List<IndexedWord> findPositionsOfKeywords(
			SemanticGraph dependencies, List<String> keywords) {
		List<IndexedWord> idxWords = new ArrayList<>();
		
		// go through keywords
		for (String keyword: keywords) {
			// split keyword
			String[] words = keyword.split(" ");
			List<IndexedWord> tmpIdxWords = new ArrayList<>();
			
			// get indices
			for (String word: words) {
				IndexedWord idxWord = dependencies.getNodeByWordPattern(word);
				
				if (idxWord != null) {
					tmpIdxWords.add(idxWord);
				}
			}
			
			// are the indices in sequence?
			boolean seqIndices = true;
			for (int i = 0; i < tmpIdxWords.size() - 1; i++) {
				if (tmpIdxWords.get(i).index() + 1 
						!= tmpIdxWords.get(i + 1).index()) {
					seqIndices = false;
				}
			}
			
			if (seqIndices == true && tmpIdxWords.size() > 0) {
				idxWords.add(tmpIdxWords.get(0));
			}
		}
		
		return idxWords;
	}
}
