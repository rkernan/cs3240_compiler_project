package generator.parser;

import global.Token;

import java.util.ArrayList;

public class LL1_Token {
	private Token<LL1_TokenType> token;
	private ArrayList<Terminal> FirstSet;
	
	/**
	 * 
	 * @param token
	 */
	public LL1_Token(Token<LL1_TokenType> token){
		this.token = token;
		this.FirstSet = new ArrayList<Terminal>();
	}
	
	/**
	 * 
	 * @param tok
	 */
	public void addToFirstSet(Terminal tok){
		FirstSet.add(tok);	
	}
	
	/**
	 * 
	 * @return
	 */
	public Token<LL1_TokenType> getToken() {
		return token;
	}
	
	/**
	 * 
	 * @param token
	 */
	public void setToken(Token<LL1_TokenType> token) {
		this.token = token;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Terminal> getFirstSet() {
		return FirstSet;
	}

	/**
	 * 
	 * @param firstSet
	 */
	public void setFirstSet(ArrayList<Terminal> firstSet) {
		FirstSet = firstSet;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LL1_Token other = (LL1_Token) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.getValue().equals(other.token.getValue()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		
		return "" + token.getValue();
	}

	
	
}
