package com.da.antiphish.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.lists.MetalistEntry;
import com.da.antiphish.types.Category;

/**
 * This task checks if the meta tags contain suspicious keywords/contents from the meta list (meta_content.json) or not.
 * Returns the highest confidence of all found entries in the list as score.
 *  
 * @author Marco Madritsch
 */
public class MetaTagsTask extends Task {
	private Document htmlDoc;
	private Map<String, MetalistEntry> metalist;
	
	/**
	* Constructor which gets the Document object of a website and the meta list of the system.
	* @param htmlDoc	the Document object of the website to be analysed
	* @param metalist	the list of suspicious meta contents of the system
	*/
	public MetaTagsTask(Document htmlDoc, Map<String, MetalistEntry> metalist) {
		this.LOGGER = LoggerFactory.getLogger(MetaTagsTask.class);
		this.category = Category.METADATA;
		this.htmlDoc = htmlDoc;
		this.metalist = metalist;
	}
	
	/**
	* Checks if the meta tags contain suspicious keywords/contents from the meta list or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null && metalist != null) {
			try {
				// get all meta tags of website
				Elements metaTags = HtmlHelper.extractMetaTags(htmlDoc);
				
				// check for null
				if(metaTags == null) {
					LOGGER.warn("Extraction of meta tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// split meta tags text
				String[] words = metaTags.toString().split("[\\W_]+");
				
				// filter words of meta tags which are in content list
				List<MetalistEntry> matchingContent = new ArrayList<MetalistEntry>();
				for(int i = 0; i < words.length; i++) {
					if(metalist.containsKey(words[i].toLowerCase())) {
						matchingContent.add(metalist.get(words[i].toLowerCase()));
					}
				}
				
				if(matchingContent.size() > 0) {
					LOGGER.debug("Found " + matchingContent.size() + " keyword(s)/phrase(s) in meta tags");
					
					// get entry with maximum confidence of all matches
					MetalistEntry maxConfidenceEntry = matchingContent.stream().max((content1, content2) 
							-> Double.compare(content1.getConfidence(), content2.getConfidence())).get();
					
					return new TaskResult(this.getClass().getSimpleName(), category, maxConfidenceEntry.getConfidence());
				} else {
					LOGGER.debug("Did not find any keywords/phrases in meta tags");
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing meta tags");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
