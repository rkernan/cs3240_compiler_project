package generator.parser;

import global.Token;

import java.text.ParseException;
import java.util.ArrayList;

/** PaseTable.java
 * 
 */
public class ParseTable {
	
	private ArrayList<LL1_Rule> rule_list;
	private ArrayList<Terminal> terminals;
	private ArrayList<NonTerminal> non_terminals;
	private LL1_Rule[][] table;
	
	/**
	 * creates the parse table, column id, and row id, then generates the parse table
	 * @param rule_list list of rules to generate the table from
	 * @param terminals identifiers for the row of the table
	 * @param non_terminals identifiers for the column of the table
	 */
	public ParseTable(ArrayList<LL1_Rule> rule_list, ArrayList<Terminal> terminals, ArrayList<NonTerminal> non_terminals) throws ParseException {
		this.rule_list = rule_list;
		this.terminals = terminals;
		this.non_terminals = non_terminals;
		this.table = new LL1_Rule[this.non_terminals.size()][this.terminals.size()];
		this.build_table();
	}
	
	/**
	 * builds the parse table
	 * @throws ParseException if parse table generation fails (i.e. language isn't LL(1)) 
	 */
	public void build_table() throws ParseException {
		for(int i = 0; i < this.rule_list.size(); i++) {
			NonTerminal non_term = this.rule_list.get(i).getNonTerm();
			//find column position
			int nt_pos = -1;
			for(int j = 0; j < this.non_terminals.size(); j++) {
				if(this.non_terminals.get(j).equals(non_term)) {
					nt_pos = j;
					break;
				}
			}
			//find row position
			ArrayList<Terminal> first = this.rule_list.get(i).getFirstSet();
			//for all first in non_term
			for(int j = 0; j < first.size(); j++) {
				//if EPSILON
				if(first.get(j).getToken().getValue().equals("EPSILON")) {
					ArrayList<Terminal> follow = non_term.getFollowSet();
					//for all follow in non term
					for(int k = 0; k < follow.size(); k++) {
						//add rule to table position
						for(int l = 0; l < this.terminals.size(); l++) {
							//find the column position
							if(this.terminals.get(l).equals(follow.get(k)) /*&& table[nt_pos][l] == null*/) {
								if(table[nt_pos][l] != null) {
									throw new ParseException("Specification ERROR: Specification grammar is not LL(1)", 0);
								}
								//add it
								this.table[nt_pos][l] = this.rule_list.get(i);
							}
						}
					}
				}
				else {
					//add rule to table position
					for(int k = 0; k < this.terminals.size(); k++) {
						//find column position
						if(this.terminals.get(k).equals(first.get(j)) /*&& table[nt_pos][k] == null*/) {
							if(table[nt_pos][k] != null) {
								throw new ParseException("Specification ERROR: Specification grammar is not LL(1)", 0);
							}
							//add it
							this.table[nt_pos][k] = this.rule_list.get(i);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Terminal> getTerminals() {
		return this.terminals;
	}
	
	/**
	 * generates string representation of the parse table
	 * @return string representation of the parse table
	 */
	public String toString() {
		String result = new String();
		for(int i = 0; i < this.table.length; i++) {
			result += "non-terminal: " + this.non_terminals.get(i).toString() + "\n";
			for(int j = 0; j < this.table[i].length; j++) {
				if(this.table[i][j] == null) {
					//do nothing
				}
				else {
					result += "\trule: \"" + this.table[i][j].toString() + "\", on: " + this.terminals.get(j).toString() + "\n";
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param non_terminal
	 * @param terminal
	 * @return
	 */
	public boolean hasProduction(LL1_Token non_terminal, Token<String> terminal) {
		boolean exists = false;
		//get row
		int row;
		for(row = 0; row < this.non_terminals.size(); row++) {
			if(this.non_terminals.get(row).equals(non_terminal)) {
				break;
			}
		}
		//get column
		int col;
		for(col = 0; col < this.terminals.size(); col++) {
			if(this.terminals.get(col).getToken().getValue().equals(terminal.getType())) {
				break;
			}
		}
		//check position
		if(this.table[row][col] != null) {
			exists = true;
		}
		return exists;
	}
	
	/**
	 * 
	 * @param non_terminal
	 * @param terminal
	 * @return
	 */
	public LL1_Rule getProduction(LL1_Token non_terminal, Token<String> terminal) {
		//get row
		int row;
		for(row = 0; row < this.non_terminals.size(); row++) {
			if(this.non_terminals.get(row).equals(non_terminal)) {
				break;
			}
		}
		//get column
		int col;
		for(col = 0; col < this.terminals.size(); col++) {
			if(this.terminals.get(col).getToken().getValue().equals(terminal.getType())) {
				break;
			}
		}
		return this.table[row][col];
	}
}
