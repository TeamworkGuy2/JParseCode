package codeParser;

import lombok.Getter;

/**
 * @author TeamworkGuy2
 * @since 2015-5-28
 */
public enum CodeFragmentType {
	DOCUMENT(true),
	COMMENT(false),
	STRING(false),
	IDENTIFIER(false),
	KEYWORD(false),
	OPERATOR(false),
	SEPARATOR(false),
	BLOCK(true),
	NAMESPACE_LIKE(true),
	CLASS_LIKE(true),
	FUNCTION_LIKE(true);


	@Getter private final boolean compound;


	private CodeFragmentType(boolean compound) {
		this.compound = compound;
	}

}
