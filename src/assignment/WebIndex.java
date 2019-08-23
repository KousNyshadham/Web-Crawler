package assignment;
import java.util.*;
import java.net.*;
/**
 * A web-index which efficiently stores information about pages. Serialization is done automatically
 * via the superclass "Index" and Java's Serializable interface.
 *
 */
public class WebIndex extends Index {
    /**
     * Needed for Serialization (provided by Index) - don't remove this!
     */
    private static final long serialVersionUID = 1L;
    
    HashMap<String, HashMap<URL, HashSet<Integer>>> index = new HashMap<String, HashMap<URL, HashSet<Integer>>>();
    HashSet<URL> allURLs = new HashSet<URL>();
    
    //adds to index entry with Word: word, URL: url, and Place: place
    public void add(String word, URL url, int place) {
    		if(index.containsKey(word)) {
    			if(index.get(word).containsKey(url)) {
    				index.get(word).get(url).add(place);
    			}
    			else {
    				HashSet<Integer> places = new HashSet<Integer>();
    				places.add(place);
    				index.get(word).put(url, places);
    			}
    		}
    		else {
    			HashSet<Integer> places = new HashSet<Integer>();
    			places.add(place);
    			HashMap<URL, HashSet<Integer>> urlToLocation = new HashMap<URL, HashSet<Integer>>();
    			urlToLocation.put(url, places);
    			index.put(word, urlToLocation);
    		}
    }
    
    //checks whether word is contained within index data structure
    public boolean contains(String word){
    		return (index.containsKey(word));
    }
   
    //generates links for given word
    //word can be a word, !word, or "word1 ... wordn"
    public Set<URL> links(String word){
    		Set<URL> links = new HashSet<URL>();
    		//!word finds urls that have word and then traverses through allURLs and adds all...
    		//... that are not contained within set of urls that have word
    		if(word.charAt(0) == '!') {
    			word = word.substring(1);
    			if(!contains(word)) {
    				return allURLs;
    			}
    			else {
    				Set<URL> temp = index.get(word).keySet();
    				for(URL url : allURLs) {
    					if(!temp.contains(url)) {
    						links.add(url);
    					}
    				}
    				return links;
    			}
    		}
    		//phrase query url retrieval
    		else if(word.charAt(0) == '"') {
    			word = word.substring(1, word.length()-1);
    			String[] words = word.split(" ");
    			Set<URL> newLinks = new HashSet<URL>();
    			//test that all words in phrase query are contained within index
    			for(int i = 0 ; i < words.length; i++) {
    				if(!contains(words[i])) {
    					return newLinks;
    				}
    			}
    			//urls that have all the words in phrase
    			Set<URL> intersection = index.get(words[0]).keySet();
    			for(int i = 1; i < words.length; i++) {
    				intersection.retainAll(index.get(words[i]).keySet());
    			}
    			if(intersection.size() == 0) {
    				return newLinks;
    			}
    			for(URL url: intersection) {
    				//locations of first word in nth url in intersected urls
    				HashSet<Integer> locations = index.get(words[0]).get(url);
    				for(int i = 1; i < words.length; i++) {
    					//find locations of next word in nth url and..
    					//intersect with each value increased......
    					//by one the locations of previous word in nth url
    					HashSet<Integer> temp = new HashSet<Integer>();
    					for(Integer val : locations) {
    						temp.add(val+1);
    					}
    					locations = index.get(words[i]).get(url);
    					locations.retainAll(temp);
    				}
    				if(locations.size() != 0) {
    					newLinks.add(url);
    				}
    			}
    			return newLinks;
    		}
    		//word query 
    		else {
    			if(!contains(word)) {
        			return new HashSet<URL>();
        		}
    			else {
    				return index.get(word).keySet();
    			}
    		}     	
    }
}