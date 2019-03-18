package twg2.parser.codeParser;

import twg2.parser.language.CodeLanguage;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public enum AccessModifierEnum implements Keyword {
	PUBLIC,
	NAMESPACE_LOCAL,
	INHERITANCE_LOCAL,
	NAMESPACE_OR_INHERITANCE_LOCAL,
	PRIVATE;


	@Override
	public String toSrc() {
		return this.name();
	}


	public static final Keyword parseFromSrc(String src, CodeLanguage lang) {
		Keyword access = tryParseFromSrc(src, lang);
		if(access == null) {
			throw new IllegalArgumentException("'" + src + "' is not recognized as a " + lang.displayName() + " access modifier");
		}
		return access;
	}


	public static final Keyword tryParseFromSrc(String src, CodeLanguage lang) {
		return lang.getAstUtil().getAccessModifierParser().tryParseFromSrc(src);
	}

}
