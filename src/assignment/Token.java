package assignment;
import java.net.URL;
import java.util.*;

public class Token {
	
	String word;
	Set<URL> links;
	
	//default constructor;
	Token(){
	}
	
	//constructor with parameter
	Token(String word){
		this.word = word;
	}
	
	//constructor with 2 parameters
	Token(String word, Set<URL> links){
		this.word = word;
		this.links = links;
	}
	
	//returns Token that has links value that is either an intersection or union of links...
	//...contained within current links and operand.links
	public Token performOperation(Token operator, Token operand) {
		Set<URL> newLinks = new HashSet<URL>();
		if(operator.word.equals("&")) {
			Set<URL> operandLinks = operand.links;
			for(URL link: links) {
				if(operandLinks.contains(link)) {
					newLinks.add(link);
				}
			}
		}
		else {
			Set<URL> operandLinks = operand.links;
			newLinks.addAll(links);
			newLinks.addAll(operandLinks);
		}
		links = newLinks;
		return this;
	}	
}