package com.da.antiphish.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.da.antiphish.analyse.HtmlHelper;
import com.da.antiphish.lists.ScriptlistEntry;
import com.da.antiphish.types.Category;

/**
 * This task checks if the script tags of the website contain suspicious keywords/contents from the script list 
 * (script_content.json). Returns the highest confidence of all found entries in the list as score.
 *  
 * @author Marco Madritsch
 */
public class SuspiciousScriptContentTask extends Task {
	private Document htmlDoc;
	private Map<String, ScriptlistEntry> scriptlist;
	
	/**
	* Constructor which gets the Document object of a website and the script list of the system.
	* @param htmlDoc	the Document object of the website to be analysed
	* @param scriptlist	the list of suspicious script contents of the system
	*/
	public SuspiciousScriptContentTask(Document htmlDoc, Map<String, ScriptlistEntry> scriptlist) {
		this.LOGGER = LoggerFactory.getLogger(SuspiciousScriptContentTask.class);
		this.category = Category.STRUCTURE;
		this.htmlDoc = htmlDoc;
		this.scriptlist = scriptlist;
	}
	
	/**
	* Checks if the script tags of the website contain suspicious keywords/contents from the script list or not.
	* @return 	the TaskResult object including the name of the task, the category of the task and the calculated
	* 			score of the task
	*/
	@Override
	public TaskResult call() {
		if(htmlDoc != null && scriptlist != null) {
			try {
				
				// get all script tags of website
				Elements scriptTags = HtmlHelper.extractScriptTags(htmlDoc);
				
				// check for null
				if(scriptTags == null) {
					LOGGER.warn("Extraction of script tags returned null");
					return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
				}
				
				// split script tags content
				String[] commands = scriptTags.html().toString().replaceAll("\\s","").split("[=;]+");
				
				// filter commands of script tags which are in script list
				List<ScriptlistEntry> matchingContent = new ArrayList<ScriptlistEntry>();
				for(int i = 0; i < commands.length; i++) {
					if(scriptlist.containsKey(commands[i].toLowerCase())) {
						matchingContent.add(scriptlist.get(commands[i].toLowerCase()));
					}
				}
				
				if(matchingContent.size() > 0) {
					LOGGER.debug("Found " + matchingContent.size() + " suspicious script content(s) in script tags");
					
					// get entry with maximum confidence of all matches
					ScriptlistEntry maxConfidenceEntry = matchingContent.stream().max((content1, content2) 
							-> Double.compare(content1.getConfidence(), content2.getConfidence())).get();
					
					return new TaskResult(this.getClass().getSimpleName(), category, maxConfidenceEntry.getConfidence());
				} else {
					LOGGER.debug("Did not find suspicious script contents in script tags");
					return new TaskResult(this.getClass().getSimpleName(), category, 0.0);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
	        	LOGGER.warn(e.getClass().getSimpleName() + " while analysing script tags");
	        }
		}
		
		return new TaskResult(this.getClass().getSimpleName(), category, -1.0);
	}	
}
