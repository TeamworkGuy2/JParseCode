package twg2.parser.fragment;

import twg2.parser.documentParser.CodeFragment;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-3
 */
public interface AstTypeChecker<T_KEYWORD> {

	/** Check if a code fragment is a block of code following a possible field definition.<br>
	 * Best understood by example, a C# property like <code>{ get; set; }</code><br>
	 * or <code>{ get { return _a; }; set { _a = value; }; }</code>
	 * @param tokenNode the document code fragment
	 * @return true if the token represents a field block, false if not
	 */
	public boolean isFieldBlock(SimpleTree<CodeFragment> tokenNode);

	public boolean isKeyword(CodeFragment node, T_KEYWORD keyword1);

	public boolean isKeyword(CodeFragment node, T_KEYWORD keyword1, T_KEYWORD keyword2);

	public boolean isKeyword(CodeFragment node, T_KEYWORD keyword1, T_KEYWORD keyword2, T_KEYWORD keyword3);

}
