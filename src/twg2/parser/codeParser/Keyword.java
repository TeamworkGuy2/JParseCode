package twg2.parser.codeParser;

import twg2.parser.documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public interface Keyword {

	public boolean isKeyword(String str);

	public boolean isPrimitive(String str);

	public boolean isType(String str);

	/**
	 * @param str
	 * @return true for any string which is a type keyword (i.e. {@link #isType(String)}) or any non-keyword strings,
	 * returns false for any other keywords 
	 */
	public boolean isDataTypeKeyword(String str);

	/** Checks for block identifying keywords (i.e. 'namespace', 'module', 'class', 'interface')
	 */
	public boolean isBlockKeyword(DocumentFragmentText<CodeFragmentType> node);

	/** Checks for class/interface block modifier keywords (i.e. 'abstract', 'static', 'final', 'sealed')
	 */
	public boolean isClassModifierKeyword(DocumentFragmentText<CodeFragmentType> node);

}
