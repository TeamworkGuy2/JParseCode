package twg2.parser.baseAst;

import twg2.parser.codeParser.CodeLanguage;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public interface AccessModifierParser<T_ACCESS_MODIFIER extends AccessModifier, T_BLOCK extends CompoundBlock> {

	public CodeLanguage getLanguage();

	public T_ACCESS_MODIFIER defaultAccessModifier(String src, T_BLOCK currentBlock, T_BLOCK parentBlock);

	public T_ACCESS_MODIFIER defaultAccessModifier(T_ACCESS_MODIFIER access, T_BLOCK currentBlock, T_BLOCK parentBlock);

	public T_ACCESS_MODIFIER tryFromLanguageSrc(String src);


	public default T_ACCESS_MODIFIER fromLanguageSrc(String src) {
		T_ACCESS_MODIFIER access = tryFromLanguageSrc(src);
		if(access == null) {
			throw new IllegalArgumentException("'" + src + "' is not recognized as a '" + getLanguage().displayName() + "' language access modifier");
		}
		return access;
	}

}
