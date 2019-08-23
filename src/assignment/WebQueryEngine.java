package assignment;
import java.net.URL;
import java.util.*;

/**
 * A query engine which holds an underlying web index and can answer textual queries with a
 * collection of relevant pages.
 *
 */
public class WebQueryEngine {
	WebIndex index;
	
	//default constructor
	public WebQueryEngine(){
		
	}
	
	//constructor with one parameter
	public WebQueryEngine(WebIndex index){
		this.index = index;
	}
	
    /**
     * Returns a WebQueryEngine that uses the given Index to constructe answers to queries.
     *
     * @param index The WebIndex this WebQueryEngine should use.
     * @return A WebQueryEngine ready to be queried.
     */
    public static WebQueryEngine fromIndex(WebIndex index) {
        return new WebQueryEngine(index);
    }

    /**
     * Returns a Collection of URLs (as Strings) of web pages satisfying the query expression.
     *
     * @param query A query expression.
     * @return A collection of web pages satisfying the query.
     */
    public Collection<Page> query(String query) {
    		boolean notValid = false;
    		for(int i = 0; i < query.length(); i++) {
    			char c = query.charAt(i);
    			if(!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '"' && c!= '!'
    					&& c != '&' && c != '|' && c != '(' && c!= ')' && c != ' ') {
    				notValid = true;
    			}
    		}
    		if(notValid) {
    			return new ArrayList<Page>();
    		}
    		try {
	    		ArrayList<String> elements = parseInfix(query);
	    		elements = fixInfix(elements);
	    		elements = shuntingYard(elements);
	    		ArrayList<Token> tokens = new ArrayList<Token>();
	    		for(String element : elements) {
	    			if(element.equals("&")) {
	    				Token token = new Token("&");
	    				tokens.add(token);
	    			}
	    			else if(element.equals("|")) {
	    				Token token = new Token("|");
	    				tokens.add(token);
	    			}
	    			else {
	    				Set<URL> links = index.links(element.toLowerCase());
	    				Token token = new Token(element.toLowerCase(), links);
	    				tokens.add(token);
	    			}
	    		}
	       	Set<URL> urls = evaluatePostfix(tokens);
	    		ArrayList<Page> pages = new ArrayList<Page>();
			for(URL url: urls) {
				pages.add(new Page(url));
			}
	    		return pages;
    		} catch (Exception e) {
    			return new ArrayList<Page>();
    		}
    }
    
    //evaluates postfix expression consisting of tokens
    public Set<URL> evaluatePostfix(ArrayList<Token> tokens){
    		Stack<Token> stack = new Stack<Token>();
    		for(Token token : tokens) {
    			if(token.word.equals("&") || token.word.equals("|")) {
    				Token token1 = stack.pop();
    				Token token2 = stack.pop();
    				token1 = token1.performOperation(token, token2);
    				stack.push(token1);
    			}
    			else {
    				stack.push(token);
    			}
    		}
    		Set<URL> links = stack.pop().links;
    		return links;
    }
    
    //converts query to list of tokens
    public ArrayList<String> parseInfix(String query) {
    		ArrayList<String> tokens = new ArrayList<String>();
    		for(int i = 0; i < query.length(); i++) {
    			char currentChar = query.charAt(i);
    			//starts at ! and creates a string token that goes all the way up until first nonalphanumeric character encountered
    			if(currentChar == '!') {
    				String token = query.charAt(i) + "";
    				i = i + 1;
    				while(i < query.length() && (Character.isAlphabetic(query.charAt(i)) || Character.isDigit(query.charAt(i)))) {
    					token += query.charAt(i);
    					i++;
    				}
    				i = i - 1;
    				tokens.add(token);
    			}
    			//starts at " and creates string token that goes all the way up until second "
    			else if(currentChar == '"') {
    				String token = query.charAt(i) + "";
    				i = i + 1;
    				char nextChar = query.charAt(i);
    				while(nextChar != '"') {
    					token += nextChar;
    					i++;
    					nextChar = query.charAt(i);
    				}
    				token += nextChar;
    				tokens.add(token);
    			}
    			//starts at alphanumeric character and goes all the way up until first nonalphaneumeric character encountered
    			else if(Character.isAlphabetic(currentChar) || Character.isDigit(currentChar)) {
    				String token = query.charAt(i) + "";
    				i = i + 1;
    				while(i < query.length() && (Character.isAlphabetic(query.charAt(i)) || Character.isDigit(query.charAt(i)))) {
    					token += query.charAt(i);
    					i++;
    				}
    				i = i - 1;
    				tokens.add(token);
    			}
    			//straightforward 
    			else if(currentChar == '(') {
    				tokens.add(query.charAt(i)+"");
    			}
    			else if(currentChar == ')') {
    				tokens.add(query.charAt(i)+"");
    			}
    			else if(currentChar == '&') {
    				tokens.add(query.charAt(i)+"");
    			}
    			else if(currentChar == '|') {
    				tokens.add(query.charAt(i)+"");
    			}
    		}
    		return tokens;
    }
    
    //given list of tokens after parseInfix...
    //add implicit ands
    public ArrayList<String> fixInfix(ArrayList<String> tokens){
    		ArrayList<String> newTokens = new ArrayList<String>();
    		for(int i = 0; i < tokens.size()-1; i++) {
    			if(!(tokens.get(i).equals("&") || tokens.get(i).equals("|")) && !(tokens.get(i+1).equals("&") || tokens.get(i+1).equals("|"))) {
    				if(!(tokens.get(i).equals("(") || tokens.get(i+1).equals(")"))) {
    					newTokens.add(tokens.get(i));
        				newTokens.add("&");
    				}
    				else {
    					newTokens.add(tokens.get(i));
    				}
    			}
    			else {
    				newTokens.add(tokens.get(i));
    			}
    		}
    		newTokens.add(tokens.get(tokens.size()-1));
    		return newTokens;
    }
    
    //converting infix to postfix using djikstra's shunting yard algorithm
    public ArrayList<String> shuntingYard(ArrayList<String> tokens){
    		Stack<String> operatorStack = new Stack<String>();
    		Queue<String> output = new LinkedList<String>();
    		ArrayList<String> newTokens = new ArrayList<String>();
    		for(int i = 0; i < tokens.size(); i++) {
    			if(tokens.get(i).equals("&") || tokens.get(i).equals("|")) {
    				String operator = tokens.get(i);
    				while(!operatorStack.isEmpty() && (operator.equals("|") && operatorStack.peek().equals("&"))) {
    					output.add(operatorStack.pop());
    				}
    				operatorStack.push(operator);
    			}
    			else if(tokens.get(i).equals("(")) {
    				operatorStack.push(tokens.get(i));
    			}
    			else if(tokens.get(i).equals(")")) {
    				while(!operatorStack.isEmpty() && !(operatorStack.peek().equals("("))) {
    					output.add(operatorStack.pop());
    				}
    				operatorStack.pop();
    			}
    			else {
    				output.add(tokens.get(i));
    			}
    		}
    		while(!operatorStack.isEmpty()) {
    			output.add(operatorStack.pop());
    		}
    		for(String token: output) {
    			newTokens.add(token);
    		}
    		return newTokens;
    }
}