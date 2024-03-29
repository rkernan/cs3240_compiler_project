package generator.parser;

import java.util.ArrayList;

/**
 * 
 *
 */
public class LL1_Rule {
    private NonTerminal nonTerm;
    private ArrayList<LL1_Token> TNTList;
    private ArrayList<Terminal> FirstSet;
    
    /**
     * 
     * @param nonTerm
     */
    public LL1_Rule(NonTerminal nonTerm){
            this.nonTerm = nonTerm;
            this.FirstSet = new ArrayList<Terminal>();
            TNTList = new ArrayList<LL1_Token>();
    }

    /**
     * 
     * @param tok
     */
    public void addToTNTList(LL1_Token tok){
            TNTList.add(tok);       
    }
    
    /**
     * 
     * @return
     */
    public NonTerminal getNonTerm() {
            return nonTerm;
    }

    /**
     * 
     * @param nonTerm
     */
    public void setNonTerm(NonTerminal nonTerm) {
            this.nonTerm = nonTerm;
    }

    /**
     * 
     * @return
     */
    public ArrayList<LL1_Token> getTNTList() {
            return TNTList;
    }

    /**
     * 
     * @param tNTList
     */
    public void setTNTList(ArrayList<LL1_Token> tNTList) {
            TNTList = tNTList;
    }
    
    /**
     * 
     * @return
     */
    @Override
    public String toString() {
            String retString = "" + nonTerm.getToken().getValue() + " -> ";
            for(int i = 0; i < TNTList.size(); i++){
                    retString += TNTList.get(i).getToken().getValue();
                    retString += " ";
            }
            return retString;
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
}
