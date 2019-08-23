package assignment;

import java.util.*;
import java.net.*;
import org.attoparser.simple.*;

/**
 * A markup handler which is called by the Attoparser markup parser as it parses the input;
 * responsible for building the actual web index.
 *
 */
public class CrawlingMarkupHandler extends AbstractSimpleMarkupHandler {
    
	private WebIndex index;
	private URL currentURL;
	private List<String> newURLs;
	private int place;
	
	//default constructor
    public CrawlingMarkupHandler() {
    		index = new WebIndex();
    		newURLs = new ArrayList<String>();
    		place = 0;
    }
    
    //setter
    public void setCurrentURL(URL currentURL) {
    		this.currentURL = currentURL;
    }
    
    /**
    * This method returns the complete index that has been crawled thus far when called.
    */
    public Index getIndex() {
        return index;
    }

    /**
    * This method returns any new URLs found to the Crawler; upon being called, the set of new URLs
    * should be cleared.
    */
    public List<URL> newURLs() {
        List<URL> newURLs = new ArrayList<URL>();
        for(int i = 0; i < this.newURLs.size(); i++) {
        		try {
                newURLs.add(new URL(this.newURLs.get(i)));
            } catch (MalformedURLException e) {
                // Throw this one out!
                System.err.printf("Error: URL '%s' was malformed and will be ignored!%n", this.newURLs.get(i));
            }
        }
        this.newURLs.clear();
        return newURLs; 
    }

    /**
    * These are some of the methods from AbstractSimpleMarkupHandler.
    * All of its method implementations are NoOps, so we've added some things
    * to do; please remove all the extra printing before you turn in your code.
    *
    * Note: each of these methods defines a line and col param, but you probably
    * don't need those values. You can look at the documentation for the
    * superclass to see all of the handler methods.
    */

    /**
    * Called when the parser first starts reading a document.
    * @param startTimeNanos  the current time (in nanoseconds) when parsing starts
    * @param line            the line of the document where parsing starts
    * @param col             the column of the document where parsing starts
    */
    public void handleDocumentStart(long startTimeNanos, int line, int col) {
    		place = 0;
    		index.allURLs.add(currentURL);
    }

    /**
    * Called when the parser finishes reading a document.
    * @param endTimeNanos    the current time (in nanoseconds) when parsing ends
    * @param totalTimeNanos  the difference between current times at the start
    *                        and end of parsing
    * @param line            the line of the document where parsing ends
    * @param col             the column of the document where the parsing ends
    */
    public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col) {
    }
    
    //file path B.S.
    private String getcurrentFilePath() {
		String filePath = currentURL.toString().substring(0, currentURL.toString().lastIndexOf('/') + 1);
		return filePath;
    }
    
    /**
    * Called at the start of any tag.
    * @param elementName the element name (such as "div")
    * @param attributes  the element attributes map, or null if it has no attributes
    * @param line        the line in the document where this elements appears
    * @param col         the column in the document where this element appears
    */
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
    	URL tempURL = currentURL;
    	if(elementName.equalsIgnoreCase("a")) {
    		if(attributes.get("href")!=null || attributes.get("HREF") != null || attributes.get("Href")!=null) {
    			String value = "";
    			if(attributes.containsKey("href")) {
    				value = attributes.get("href");
    			}
    			else if(attributes.containsKey("Href")) {
    				value = attributes.get("Href");
    			}
    			else {
    				value = attributes.get("HREF");
    			}
    			//ignore rest of file path B.S.
    			String currentFilePath = getcurrentFilePath();
    			while(value.contains("../")) {
    				try {
    					value = value.substring(value.indexOf('/')+1);
    					currentFilePath = currentFilePath.substring(0, currentFilePath.lastIndexOf('/'));
    					currentURL = new URL(currentFilePath);
    					currentFilePath = getcurrentFilePath();
    				} catch (MalformedURLException e) {
    					// Throw this one out!
    					System.err.printf("Error: URL '%s' was malformed and will be ignored!%n", getcurrentFilePath());
    				}
    			}
			newURLs.add(currentFilePath+value);	
    		}	
    	}
    	currentURL = tempURL;
    }

    /**
    * Called at the end of any tag.
    * @param elementName the element name (such as "div").
    * @param line        the line in the document where this elements appears.
    * @param col         the column in the document where this element appears.
    */
    public void handleCloseElement(String elementName, int line, int col) {
    }

    /**
    * Called whenever characters are found inside a tag. Note that the parser is not
    * required to return all characters in the tag in a single chunk. Whitespace is
    * also returned as characters.
    * @param ch      buffer containint characters; do not modify this buffer
    * @param start   location of 1st character in ch
    * @param length  number of characters in ch
    */
    public void handleText(char ch[], int start, int length, int line, int col) {
    	String word = "";
        for(int i = start; i < start + length; i++) {
        		if(Character.isLetter(ch[i]) || Character.isDigit(ch[i])) {
        			word += ch[i];
        			//if word is at end of tag
        			if(i==start+length-1) {
        				place++;
        				index.add(word.toLowerCase(), currentURL, place);
        			}
        		}
        		else {
        			if(!word.equals("")) {
    					place++;
    					index.add(word.toLowerCase(), currentURL, place);
        				word = "";
    				}
        		}
        }        
    }
}