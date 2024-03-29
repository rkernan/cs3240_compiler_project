package generator.regex;
/**
 * Token types for use when scanning a Grammar.
 * 
 * @author Robert Kernan
 *
 */

/**
 * TokenType.java
 * Token types for use when scanning a grammar specification.
 */
public enum Regex_TokenType {
	EOF,
	EOL,
	LITERAL,
	DEFINED,
	UNION,
	KLEENE,
	PLUS,
	DOT,
	CARET,
	DASH,
	LBRACKET,
	RBRACKET,
	LPAREN,
	RPAREN,
	IN
}

