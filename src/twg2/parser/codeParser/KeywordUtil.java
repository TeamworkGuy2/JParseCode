package twg2.parser.codeParser;

import twg2.parser.baseAst.AccessModifier;
import twg2.parser.codeParser.tools.CodeFragmentEnumSubSet;

/**
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public interface KeywordUtil<T_KEYWORD extends AccessModifier> {

	public boolean isKeyword(String str);

	public boolean isPrimitive(String str);

	public boolean isType(String str);

	/**
	 * @return true for any string which is a type keyword (i.e. {@link #isType(String)}) or any non-keyword strings,
	 * returns false for any other keywords 
	 */
	public boolean isDataTypeKeyword(String str);

	/** Checks for block identifying keywords (i.e. 'namespace', 'module', 'class', 'interface')
	 */
	public CodeFragmentEnumSubSet<T_KEYWORD> blockModifiers();

	/** Checks for class/interface block modifier keywords (i.e. 'abstract', 'static', 'final', 'sealed')
	 */
	public CodeFragmentEnumSubSet<T_KEYWORD> classModifiers();

	/** Checks for field modifier keywords (i.e. 'volatile', 'readonly', 'static', 'private')
	 */
	public CodeFragmentEnumSubSet<T_KEYWORD> fieldModifiers();

	/** Checks for method modifier keywords (i.e. 'synchronized', 'static', 'final', 'protected')
	 */
	public CodeFragmentEnumSubSet<T_KEYWORD> methodModifiers();

	/** Checks for operator keywords (i.e. 'As', 'Is', 'instanceof')
	 */
	public CodeFragmentEnumSubSet<T_KEYWORD> operators();

	/** Checks for type literal keywords (i.e. 'true', 'false', 'null')
	 */
	public CodeFragmentEnumSubSet<T_KEYWORD> typeLiterals();

}
