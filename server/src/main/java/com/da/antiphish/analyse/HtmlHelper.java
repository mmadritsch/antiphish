package com.da.antiphish.analyse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HtmlHelper is a helper class which contains only static method in order to manipulate HTML content.
 * 
 * @author Marco Madritsch
 *
 */
public final class HtmlHelper {
	private final static Logger LOGGER = LoggerFactory.getLogger(HtmlHelper.class);
	
	private HtmlHelper() {}
	
	/**
	* Requests the HTML content of the given URL.
	* @param url		the URL as a String
	* @return			the HTML content as a Jsoup Document
	*/
	public static Document getUrlContent(String url) {
		Document htmlDoc = null;
		
		try {
			htmlDoc = Jsoup.connect(url)
					.ignoreContentType(true)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0")   
					.timeout(2000) 
					.followRedirects(true)
					.get();
	        
		} catch (Exception e) {
			LOGGER.warn(e.getClass().getSimpleName() + " while requesting URL \"" + url + "\"");
		}
		
		return htmlDoc;
	}
	
	/**
	* Extracts all <input> tags from the given Jsoup Document.
	* @param htmlDoc	the HTML content as a Jsoup Document
	* @return			all <input> tags as Jsoup Elements (ArrayList<Element>)
	*/
	public static Elements extractInputTags(Document htmlDoc) {
		Elements inputTags = null;
		
		if(htmlDoc != null) {
			inputTags = htmlDoc.select("input");
			LOGGER.debug("Extracted " + inputTags.size() + " input tags");
		}
		
		return inputTags;
	 }
	
	/**
	* Extracts all <a> tags from the given Jsoup Document.
	* @param htmlDoc	the HTML content as a Jsoup Document
	* @return			all <a> tags as Jsoup Elements (ArrayList<Element>)
	*/
	public static Elements extractLinkTags(Document htmlDoc) {
		Elements linkTags = null;
		
		if(htmlDoc != null) {
			linkTags = htmlDoc.select("a");
			LOGGER.debug("Extracted " + linkTags.size() + " links");
		}
		
		return linkTags;
	 }
	
	/**
	* Extracts all <meta> tags from the given Jsoup Document.
	* @param htmlDoc	the HTML content as a Jsoup Document
	* @return			all <meta> tags as Jsoup Elements (ArrayList<Element>)
	*/
	public static Elements extractMetaTags(Document htmlDoc) {
		Elements metaTags = null;
		
		if(htmlDoc != null) {
			metaTags = htmlDoc.select("meta");
			LOGGER.debug("Extracted " + metaTags.size() + " meta tags");
		}
		
		return metaTags;
	 }
	
	/**
	* Extracts all <iframe> and <frame> tags from the given Jsoup Document.
	* @param htmlDoc	the HTML content as a Jsoup Document
	* @return			all <iframe> and <frame> tags as Jsoup Elements (ArrayList<Element>)
	*/
	public static Elements extractFrameTags(Document htmlDoc) {
		Elements frameTags = null;
		
		if(htmlDoc != null) {
			frameTags = htmlDoc.select("iframe,frame");
			LOGGER.debug("Extracted " + frameTags.size() + " frame tags");
		}
		
		return frameTags;
	 }
	
	/**
	* Extracts all <form> tags from the given Jsoup Document.
	* @param htmlDoc	the HTML content as a Jsoup Document
	* @return			all <form> tags as Jsoup Elements (ArrayList<Element>)
	*/
	public static Elements extractFormTags(Document htmlDoc) {
		Elements formTags = null;
		
		if(htmlDoc != null) {
			formTags = htmlDoc.select("form");
			LOGGER.debug("Extracted " + formTags.size() + " form tags");
		}
		
		return formTags;
	 }
	
	/**
	* Extracts all script tags from the given Jsoup Document.
	* @param htmlDoc	the HTML content as a Jsoup Document
	* @return			all script tags as Jsoup Elements (ArrayList<Element>)
	*/
	public static Elements extractScriptTags(Document htmlDoc) {
		Elements scriptTags = null;
		
		if(htmlDoc != null) {
			scriptTags = htmlDoc.select("script");
			LOGGER.debug("Extracted " + scriptTags.size() + " script tags");
		}
		
		return scriptTags;
	 }
	
	/**
	* Extracts only the plaintext of the given Jsoup Document.
	* @param htmlDoc	the HTML content as a Jsoup Document
	* @return			the plaintext as a string
	*/
	public static String extractPlaintext(Document htmlDoc) {
		String plaintext = "";
		
		if(htmlDoc != null) {
			plaintext = htmlDoc.text();
			LOGGER.debug("Extracted plaintext");
		}
		
		return plaintext;
	 }
}
