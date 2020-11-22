package twg2.parser.codeParser;

import twg2.parser.language.CodeLanguage;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public interface AccessModifierParser<T_ACCESS_MODIFIER extends Keyword, T_BLOCK extends BlockType> {

	public CodeLanguage getLanguage();

	public T_ACCESS_MODIFIER defaultAccessModifier(String src, T_BLOCK currentBlock, T_BLOCK parentBlock);

	public T_ACCESS_MODIFIER defaultAccessModifier(T_ACCESS_MODIFIER access, T_BLOCK currentBlock, T_BLOCK parentBlock);

	public T_ACCESS_MODIFIER tryParseFromSrc(String src);


	public default T_ACCESS_MODIFIER parseFromSrc(String src) {
		T_ACCESS_MODIFIER access = tryParseFromSrc(src);
		if(access == null) {
			throw new IllegalArgumentException("'" + src + "' is not recognized as a '" + getLanguage().displayName() + "' access modifier");
		}
		return access;
	}

}
