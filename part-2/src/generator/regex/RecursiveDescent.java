package generator.regex;

import java.util.ArrayList;
import java.util.Stack;
import java.text.ParseException;

import generator.regex.Regex_Lexer;

import global.Options;

/**
 * RecursiveDescent.java
 * A recursive descent algorithm that builds an nfa for a given input 
 */

public class RecursiveDescent {

	private Stack<NFA> stack;
	//already defined identifiers
	private ArrayList<NFA_Identifier> defined;
	//stream to parse
	private Regex_Lexer lexer;
	//flag to scope back in
	private boolean scope_back;
	///flag to differentiate char classes
	private boolean char_class;

	/**
	 * setup parser with given input stream and defined identifiers
	 * @param lexer input stream to use
	 * @param defined list of defined identifiers (char classes and regular expressions)
	 */
	public RecursiveDescent(Regex_Lexer lexer, ArrayList<NFA_Identifier> defined) {
		this.lexer = lexer;
		this.defined = defined;
		this.stack = new Stack<NFA>();
		this.scope_back = false;
		this.char_class = true;
	}
	
	/**
	 * initialize the recursive descent
	 * @return nfa generated by recursive descent
	 * @throws ParseException thrown by regEx function
	 */
	public NFA_Identifier descend() throws ParseException {
		stack.push(new NFA());
		regEx();
		NFA new_nfa = stack.pop();
		new_nfa.finalize();
		NFA_Identifier result = new NFA_Identifier(null, new_nfa, char_class);
		return result;
	}
	
	/**
	 * <regEx> ->  <rexp> 
	 * @throws ParseException thrown by rexp function
	 */
	private void regEx() throws ParseException {
		rexp();
	}
	
	/**
	 * <rexp> -> <rexp1> <rexp$>
	 * @throws ParseException thrown by rexp1 function
	 */
	private void rexp() throws ParseException {
		rexp1();
		rexp$();
	}
	
	/**
	 * <rexp1> -> <rexp2> <rexp1$>
	 * @throws ParseException thrown by rexp2 function
	 */
	private void rexp1() throws ParseException {
		rexp2();
		rexp1$();
	}
	
	/**
	 * <rexp$> -> UNION <rexp1> <rexp$>  |  E 
	 * @throws ParseException thrown  by rexp1 function
	 */
	private void rexp$() throws ParseException {
		TokenType type = lexer.peekNextToken().getType();
		if (type == TokenType.UNION){
			
			if(Options.DEBUG) {
				System.out.println("   [RDescent] UNION found... not in char class");
			}
			
			char_class = false;
			lexer.getNextToken();//consume UNION
			
			NFA t2 = stack.pop();
			
			if(Options.DEBUG) {
				System.out.println("   [RDescent] adding alternation...");
			}
			
			t2.addAlternation();
			stack.push(t2);
			
			rexp1();
			rexp$();
		}
		else
			return;
	}
	
	/**
	 * <rexp1$> -> <rexp2> <rexp1$>  |  E 
	 * @throws ParseException thrown by rexp2 function
	 */
	private void rexp1$() throws ParseException {
		TokenType type = lexer.peekNextToken().getType();
		if(type == TokenType.LPAREN || type == TokenType.DOT || type == TokenType.LBRACKET || type == TokenType.DEFINED) {
			rexp2();
			rexp1$();
		}
		else if(type == TokenType.LITERAL) {
			//make sure it's an RE_CHAR
			boolean valid = check_valid(lexer.peekNextToken(), RE_CHAR);
			if(!valid) {
				return;
			}
			
			rexp2();
			rexp1$();
		}
		else
			return;
	}
	
	/**
	 * <rexp2> -> (<rexp>) <rexp2Tail>  | RE_CHAR <rexp2Tail> | <rexp3>
	 * @throws ParseException if an literal is not a valid RE_CHAR
	 */
	private void rexp2() throws ParseException {
		TokenType type = lexer.peekNextToken().getType();
		if(type == TokenType.LPAREN){
			
			if(Options.DEBUG) {
				System.out.println("   [RDescent] LPAREN found... not in char class");
				System.out.println("   [RDescent] Scoping out...");
			}
			
			char_class = false;
			stack.push(new NFA());
			lexer.getNextToken();//consume LPAREN
			rexp();
			lexer.getNextToken();//consume RPAREN
			
			if(Options.DEBUG) {
				System.out.println("   [RDescent] Scoping in...");
			}
			
			scope_back = true;
			rexp2Tail();
		}
		else if(type == TokenType.LITERAL) {
			
			if(Options.DEBUG) {
				System.out.println("   [RDescent] LITERAL found... not in char class");
			}
			
			char_class = false;
			
			//make sure it's valid
			boolean valid = check_valid(lexer.peekNextToken(), RE_CHAR);
			if(!valid) {
				throw new ParseException("ERROR: invalid token: " + lexer.peekNextToken().getValue() +
						", line: " + lexer.getLine() + ", position: " + this.lexer.getPosition(), this.lexer.getLine());
			}
			
			Token token = lexer.getNextToken();//consume LITERAL
			NFA t2 = stack.pop();
			
			char trans_val = token.getValue().charAt(0);
			if(trans_val == '\\') {
				trans_val = token.getValue().charAt(1);
			}
			
			if(Options.DEBUG) {
				System.out.println("   [RDescent] adding concatenation (literal)...");
			}
			
			t2.addConcatenation(trans_val);
			stack.push(t2);
			
			rexp2Tail();
		}
		else{
			rexp3();
		}
	}
	
	/**
	 * <rexp2Tail> -> * | + |  E
	 */
	private void rexp2Tail() {
		TokenType type = lexer.peekNextToken().getType();
		if(type == TokenType.KLEENE){
			
			if(Options.DEBUG) {
				System.out.println("   [RDescent] KLEENE found... not in char class");
			}
			
			char_class = false;
			lexer.getNextToken();
			if(scope_back) {
				scope_back = false;
				NFA t2 = stack.pop();
				t2.finalize();
				
				if(Options.DEBUG) {
					System.out.println("   [RDescent] adding repitition (*) global...");
				}
				
				t2.addRepetitionKleeneGlobal();
				//scope in
				NFA t1 = stack.pop();
				t1.concatenate(t2);
				stack.push(t1);
			}
			else {
				NFA t2 = stack.pop();
				
				if(Options.DEBUG) {
					System.out.println("   [RDescent] adding repitition (*)...");
				}
				
				t2.addRepetitionKleene();
				//put back onto stack
				stack.push(t2);
			}
		}
		else if(type == TokenType.PLUS){
			
			if(Options.DEBUG) {
				System.out.println("   [RDescent] PLUS found... not in char class");
			}
			
			char_class = false;
			lexer.getNextToken();
			if(scope_back) {
				scope_back = false;
				NFA t2 = stack.pop();
				t2.finalize();
				
				if(Options.DEBUG) {
					System.out.println("   [RDescent] adding repitition (+) global...");
				}
				
				t2.addRepetitionPlusGlobal();
				//scope in
				NFA t1 = stack.pop();
				t1.concatenate(t2);
				stack.push(t1);
			}
			else {
				NFA t2 = stack.pop();
				
				if(Options.DEBUG) {
					System.out.println("   [RDescent] adding repitition (+)...");
				}
				
				t2.addRepetitionPlus();
				//put back onto stack
				stack.push(t2);
			}
		}
		else {
			if(scope_back) {
				scope_back = false;
				//scope back in
				NFA t2 = stack.pop();
				NFA t1 = stack.pop();
				t1.concatenate(t2);
				stack.push(t1);
			}
			
			return;
		}
	}
	
	/**
	 * <rexp3> -> <charClass>  |  E 
	 * @throws ParseException thrown by charClass function
	 */
	private void rexp3() throws ParseException {
		TokenType type = lexer.peekNextToken().getType();
		if(type == TokenType.DOT || type == TokenType.LBRACKET || type == TokenType.DEFINED){
			charClass();
		}
		else
			return;
	}
	
	/**
	 * <charClass> ->  .  |  [ <charClass1>  | <definedClass>
	 * @throws ParseException thrown by definedClass function
	 */
	private void charClass() throws ParseException{
		TokenType type = lexer.peekNextToken().getType();
		
		if(type == TokenType.DOT){
			lexer.getNextToken();//consume DOT
			
			NFA t2 = stack.pop();
			//make list from dot_char (bad...)
			ArrayList<Character> dot_char = new ArrayList<Character>();
			for(int i = 0; i < DOT_CHAR.length; i++) {
				dot_char.add(DOT_CHAR[i]);
			}
			t2.addConcatenation(dot_char);
			stack.push(t2);
		}
		else if(type == TokenType.LBRACKET){
			lexer.getNextToken();//consume LBRACKET
			charClass1();
		}
		else  {
			Token defined = lexer.getNextToken();
			definedClass(defined, false);
		}
	}
	
	/**
	 * <charClass1> ->  <charSetList> | <excludeSet>
	 * @throws ParseException thrown by excludeSet function
	 * @throws ParseException thrown by charSetList function
	 */
	private void charClass1() throws ParseException {
		TokenType type = lexer.peekNextToken().getType();
		ArrayList<Character> range;
		if(type == TokenType.CARET){
			range = excludeSet(new ArrayList<Character>());
		}
		else{
			range = charSetList(new ArrayList<Character>());
		}
		
		if(Options.DEBUG) {
			for(int i = 0; i < range.size(); i++) {
				System.out.println("   [RDescent] adding to range: " + range.get(i));
			}
		}
		
		//add range to nfa
		NFA t2 = stack.pop();
		
		if(Options.DEBUG) {
			System.out.println("   [RDescent] adding concatenation (range)...");
		}
		
		t2.addConcatenation(range);
		
		stack.push(t2);
	}
	
	/**
	 * <charSetList> ->  <charSet> <charSetList> |  E 
	 * @param range the range that has been build so far
	 * @return the range to include
	 * @throws ParseException thrown by charSet function
	 */
	private ArrayList<Character> charSetList(ArrayList<Character> range) throws ParseException{
		TokenType type = lexer.peekNextToken().getType();
		if(type == TokenType.LITERAL || type == TokenType.DOT){
			//make sure the literal is a CLS_CHAR
			boolean valid = check_valid(lexer.peekNextToken(), CLS_CHAR);
			if(!valid) {
				return range;
			}
			range = charSet(range);
			return charSetList(range);
		}
		else
			return range;
	}
	
	/**
	 * <charSet> -> CLS_CHAR <charSetTail> 
	 * @param range the range that has been build so far
	 * @return the range to include
	 * @throws ParseException if range value isn't in CLS_CHAR
	 */
	private ArrayList<Character> charSet(ArrayList<Character> range) throws ParseException {
		Token start = lexer.getNextToken();//consume LITERAL
		
		if(!check_valid(start, CLS_CHAR)) {
			throw new ParseException("ERROR: Token not a valid CLS_CHAR: " + start.getValue() +
					", line: " + this.lexer.getLine() + ", position: " + this.lexer.getPosition(), this.lexer.getLine());
		}
		
		return charSetTail(start, range);
	}
	
	/**
	 * <charSetTail> -> - CLS_CHAR | E
	 * @param start starting character for the range
	 * @param range the range that has been build so far
	 * @return the range to include
	 * @throws ParseException if range value isn't in CLS_CHAR
	 */
	private ArrayList<Character> charSetTail(Token start, ArrayList<Character> range) throws ParseException {
		TokenType type = lexer.peekNextToken().getType();
		if(type == TokenType.DASH){
			lexer.getNextToken();//consume DASH
			Token end = lexer.getNextToken();
			
			if(!check_valid(end, CLS_CHAR)) {
				throw new ParseException("ERROR: Token not a valid CLS_CHAR: " + end.getValue() + 
						", line: " + this.lexer.getLine() + ", position: " + this.lexer.getPosition(), this.lexer.getLine());
			}
			//make set from range
			int start_index;
			int end_index;
			if(start.getValue().charAt(0) == '\\') {
				start_index = ((int)start.getValue().charAt(1)) - 32;
			}
			else {
				start_index = ((int)start.getValue().charAt(0)) - 32;
			}
			if(end.getValue().charAt(0) == '\\') {
				end_index = ((int)end.getValue().charAt(1)) -32;
			}
			else {
				end_index = ((int)end.getValue().charAt(0)) - 32;
			}
			int current_index = start_index;
			while(current_index <= end_index) {
				range.add(((char)(current_index + 32)));
				current_index++;
			}
			return range;
		}
		else {
			if(start.getValue().charAt(0) == '\\') {
				range.add(start.getValue().charAt(1));
			}
			else {
				range.add(start.getValue().charAt(0));
			}
			return range;
		}
	}
	
	/**
	 * <excludeSet> -> ^ <charSet>] IN <excludeSetTail>
	 * @param range the range that has been build so far
	 * @return the range to include
	 * @throws ParseException thrown by charSet function
	 */
	private ArrayList<Character> excludeSet(ArrayList<Character> range) throws ParseException {
		lexer.getNextToken();//consume CARET
		ArrayList<Character> exclude = charSet(new ArrayList<Character>());
		lexer.getNextToken();//consume RBRACKET
		lexer.getNextToken();//consume IN
		ArrayList<Character> in = excludeSetTail();
		
		for(int i = 0; i < in.size(); i++) {
			if(!exclude.contains(in.get(i))) {
				range.add(in.get(i));
			}
		}
		
		return range;
	} 
	
	/**
	 * <excludeSetTail> -> [<charSet>]  | <definedClass>
	 * @return the range to exclude 
	 * @throws ParseException thrown by charSet function
	 * @throws ParseException thrown by definedClass function
	 */
	private ArrayList<Character> excludeSetTail() throws ParseException{
		TokenType type = lexer.peekNextToken().getType();
		if(type == TokenType.LBRACKET){
			lexer.getNextToken();//consume LBRACKET
			ArrayList<Character> range = charSet(new ArrayList<Character>());
			lexer.getNextToken();//consume RBRACKET
			return range;
		}
		else{
			Token token = lexer.getNextToken();
			ArrayList<Character> range = definedClass(token, true);
			return range;
		}
	}
	
	/**
	 * includes or excludes a defined class
	 * @param token class name to include or exclude
	 * @param exclude true: excluding the range, false: including it
	 * @return the range
	 * @throws ParseException when character class doesn't exist
	 * @throws ParseException when character class isn't valid
	 */
	private ArrayList<Character> definedClass(Token token, boolean exclude) throws ParseException {
		
		NFA_Identifier phony = new NFA_Identifier(token.getValue(), null, false);
		int index = this.defined.indexOf(phony);
		//make sure it exists
		if(index == -1) {
			throw new ParseException("ERROR: char class doesn't exist: " + phony.getName()+
					", line: " + this.lexer.getLine() + ", position: " + this.lexer.getPosition(), this.lexer.getLine());
		}
		NFA_Identifier defined_nfa = this.defined.get(index);
		
		if(exclude) {
			if(!defined_nfa.getCharClass()) {
				throw new ParseException("ERROR: exclusion may only be used on a char class, invalid class: " + phony.getName() +
						", line: " + this.lexer.getLine() + ", position: " + this.lexer.getPosition(), this.lexer.getLine());
			}
			
			NFA temp = defined_nfa.getNFA();
			
			ArrayList<NFA.State.Transition> trans = temp.get(1).getTransitions();
			
			ArrayList<Character> set = new ArrayList<Character>();
			for(int i = 0; i < trans.size(); i++) {
				if(trans.get(i).getLetter() != NFA.EPSILON) {
					set.add(trans.get(i).getLetter());
				}
			}
			return set;
		}
		else {
			NFA top = stack.pop();
			top.concatenate(defined_nfa.getNFA());
			stack.push(top);
			return null;
		}
	}
	
	/**
	 * check if the given token is contained in the given set
	 * @param token symbol to check validity of
	 * @param set set to check validity with
	 * @return true: token if valid, false: token is invalid
	 */
	private boolean check_valid(Token token, String[] set) {
		for(int i = 0; i < set.length; i++) {
			if(set[i].equals(token.getValue())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * RE_CHAR set
	 * set of valid characters for use in a regular expression (as literals)
	 */
	private static final String[] RE_CHAR = {
			"\\ ", "!", "\\\"", "#", "$", "%", "&", "\\\'", "\\(", "\\)", "\\*", "\\+", ",", "-", "\\.", "/",
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "\\?", 
			"@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", 
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\\[", "\\\\", "\\]", "^", "_", 
			"`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", 
			"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "\\|", "}", "~"		
	};
	
	/**
	 * CLS_CHAR set
	 * set of valid characters for use in a character class (when defining a range)
	 */
	private static final String[] CLS_CHAR = {
			" ", "!", "\"", "#", "$", "%", "&", "\'", "(", ")", "*", "+", ",", "\\-", ".", "/",
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", 
			"@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", 
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\\[", "\\\\", "\\]", "\\^", "_", 
			"`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", 
			"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "|", "}", "~"		
	};
	
	/**
	 * DOT_CHAR set
	 * set of characters represented by a non-escaped DOT (".") literal 
	 */
	public static final char[] DOT_CHAR = {
			'\\', '*', '+', '?', '|', '[', ']', '(', ')', '.', '\'', '\"'
	};
}

