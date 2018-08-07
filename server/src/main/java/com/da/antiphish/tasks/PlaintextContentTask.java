package com.da.antiphish.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.lists.ContentlistEntry;
import com.da.antiphish.types.Category;

/**
 * This task checks if the plaintext of the website contains suspicious keywords/contents from content list 
 * (phishing_content.json). Returns the highest confidence of all found entries in the list as score.
 *  
 * @author Marco Madritsch
 */
public class PlaintextContentTask extends Task {
	private Document htmlDoc;
	private Map<String, ContentlistEntry> contentlist;
	
	/**
	* Constructor which gets the Document object of a website and the plaintext content list of the system.
	* @param htmlDoc		the Document object of the website to be analysed
	* @param contentlist	the list of suspicious plaintext contents of the system
	*/
	public PlaintextContentTask(Document htmlDoc, Map<String, ContentlistEntry> contentlist) {
		this.LOGGER = LoggerFactory.getLogger(PlaintextContentTask.class);
		this.category = Category.CONTENT;
		this.htmlDoc = htmlDoc;
		this.contentlist = contentlist;
	}
	
	/**
	* Checks if the plaintext of the website contains suspicious keywords/contents from content list or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null && contentlist != null) {
			try {
				// get plain text of website
				String plaintext = HtmlHelper.extractPlaintext(htmlDoc);
				
				// split plain text
				String[] words = plaintext.split("[\\W_]+");
				
				// filter words of plaintext which are in content list
				List<ContentlistEntry> matchingContent = new ArrayList<ContentlistEntry>();
				for(int i = 0; i < words.length; i++) {
					if(contentlist.containsKey(words[i].toLowerCase())) {
						matchingContent.add(contentlist.get(words[i].toLowerCase()));
					}
				}
				
				if(matchingContent.size() > 0) {
					LOGGER.debug("Found " + matchingContent.size() + " keyword(s)/phrase(s) in plaintext");
					
					// get entry with maximum confidence of all matches
					ContentlistEntry maxConfidenceEntry = matchingContent.stream().parallel().max((content1, content2) 
							-> Double.compare(content1.getConfidence(), content2.getConfidence())).get();
					
					LOGGER.debug("Entry with max. confidence: " + maxConfidenceEntry.getText() + ", " + maxConfidenceEntry.getConfidence());
					return new TaskResult(this.getClass().getSimpleName(), category, maxConfidenceEntry.getConfidence());
				} else {
					LOGGER.debug("Did not find any keywords/phrases in plaintext");
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				}	
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing plaintext");
			}
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
