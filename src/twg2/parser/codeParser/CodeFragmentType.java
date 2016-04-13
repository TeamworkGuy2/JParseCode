package twg2.parser.codeParser;

import lombok.Getter;

/**
 * An enumeration of common source code tokens across supported languages
 * supported by this parsing library
 * 
 * @author TeamworkGuy2
 * @since 2015-5-28
 */
public enum CodeFragmentType {
	DOCUMENT(true),
	/** multi or single line */
	COMMENT(false),
	/** chars surrounded by quotes */
	STRING(false),
	/** a float, int, hexadecimal, octal, binary, or other numeric literal */
	NUMBER(false),
	/** sequence of chars forming a valid name/keyword/identifier */
	IDENTIFIER(false),
	/** sequence of chars forming a keyword, commonly a subset of 'IDENTIFIER' */
	KEYWORD(false),
	/** chars like '+', '-', '=' */
	OPERATOR(false),
	/** chars like ';', ',' */
	SEPARATOR(false),
	/** chars surrounded by parenthesis, can contain nested blocks */
	BLOCK(true);

	@Getter
	private final boolean compound;

	private CodeFragmentType(boolean compound) {
		this.compound = compound;
	}

}
