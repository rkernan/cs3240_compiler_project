package generator.parser;

import global.Token;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * LL1.java
 * LL1 parser capable of processing input text
 */
public class LL1 {
	
	private static ArrayList<LL1_Rule> ruleList = new ArrayList<LL1_Rule>();
	private static ArrayList<Terminal> termList = new ArrayList<Terminal>();
	private static boolean changeFlag = true;
	
	public static void main(String[] args) throws FileNotFoundException{
		Grammar_Lexer lex = new Grammar_Lexer("minire-specification.txt");
		
		//add all terminals to termList
		while(lex.peekNextToken().getType() != LL1_TokenType.EOL){
			termList.add(new Terminal(lex.getNextToken()));
		}
		
		//turn all non-terminals into a rule and add their tokens to the rule
		while(lex.peekNextToken().getType() != LL1_TokenType.EOF){

			LL1_Rule currRule = new LL1_Rule(lex.getNextToken());
			ruleList.add(currRule);
			while(lex.peekNextToken().getType() != LL1_TokenType.EOL){
				currRule.addToTNTList(new NonTerminal(lex.getNextToken()));
			}
		}
		
		
		
	}
	/**
	 * FOR all nonterminals A DO First(A) := {};                      
       WHILE there are changes to any First(A) do                     
	       FOR each production choice A --> X1X2...Xn DO              
		     k:= 1 ; Continue := true ;
		     WHILE Continue = true AND k <= n DO
			   add First(Xk)-{epsilon} to First(A);
			   IF epsilon is not in First(Xk) THEN Continue := false ;
			     k := k + 1 ;
		     IF Continue = true THEN add epsilon to First(A) ;

	 */
	private static void First(){
		LL1_Rule curRule = ruleList.get(0);
		Token<LL1_TokenType> curTerm = curRule.getNonTerm();
		  while(changeFlag = true){
		      changeFlag = false;
			  for(int i = 0; i < ruleList.size(); i++){
		          int k = 1;
		          int n = curRule.getTNTList().size();
		          boolean Continue = true;
		          curRule = ruleList.get(i);
		          curTerm = curRule.getNonTerm();
		          
		          while( Continue = true && k <= n){
		              ArrayList<> tokenList = curTerm.getTokenList();
		              Array kFirstList = tokenList[k].getFirstList();
		              curTerm.getFirstList.addList(kFirstList);
		              if(!kList.contains(epsilon){
		                  Continue = false;
		              }
		              k++;
		          }
		          if(Continue){
		              curTerm.getFirstList.add(epislon);
		          }
		      }
		  }
		 
	}
	
/*	}*/
	/**
	 * Follow(start-symbol) := {$} ;
       FOR all nonterminals A != start-symbol DO Follow(A) := {} ;
       WHILE there are changes to any Follow sets DO
	       FOR each production A --> X1X2...Xn DO
		     FOR EACH Xi that is a nonterminal DO
			   add First(Xi+1Xi+2...Xn) - {epsilon} to Follow(Xi)
			   (* Note: if i=n, then Xi+1Xi+2...Xn = epsilon *)
			   IF epsilon is in First(Xi+1Xi+2...Xn) THEN
			     add Follow(A) to Follow(Xi
	 */
	/* public void follow(){
	 *     nonTerminalList
	   }*/
}


/* Algorithm for computing First(A) for all nonterminals A
 * Pseudocode from book
 * 


**********
* Simplified algorithm of above in the absense of epsilon-productions
* We will have to use the first one, but this one makes it easier to understand
* the basic algorithm
* 

FOR all nonterminals A DO First(A) := {};
WHILE there are changes to any First(A) DO
	FOR each production choice A --> X1X2...Xn DO
		add First(X1) to First(A);


*/


/* Algorithm for the computation of Follow sets
 * Pseudocode from book



*/