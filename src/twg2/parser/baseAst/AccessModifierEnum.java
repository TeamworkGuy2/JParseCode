package twg2.parser.baseAst;

import twg2.parser.language.CodeLanguage;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public enum AccessModifierEnum implements AccessModifier {
	PUBLIC,
	NAMESPACE_LOCAL,
	INHERITANCE_LOCAL,
	NAMESPACE_OR_INHERITANCE_LOCAL,
	PRIVATE;


	@Override
	public String toSrc() {
		return this.name();
	}


	public static final AccessModifier fromLanguageSrc(String src, CodeLanguage lang) {
		AccessModifier access = tryFromLanguageSrc(src, lang);
		if(access == null) {
			throw new IllegalArgumentException("'" + src + "' is not recognized as a " + lang.displayName() + " access modifier");
		}
		return access;
	}


	public static final AccessModifier tryFromLanguageSrc(String src, CodeLanguage lang) {
		return lang.getAstUtil().getAccessModifierParser().tryFromLanguageSrc(src);
	}

}
