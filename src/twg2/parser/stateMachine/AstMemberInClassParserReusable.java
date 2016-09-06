package twg2.parser.stateMachine;

import twg2.ast.interm.block.BlockAst;
import twg2.parser.codeParser.BlockType;

/**
 * @author TeamworkGuy2
 * @since 2016-09-04
 * @param <T_STATE> the state transition type/enum used by this parser to track its state
 * @param <T_RESULT> the type of result object that parsed data is store in
 */
public abstract class AstMemberInClassParserReusable<T_STATE, T_RESULT> extends AstParserReusableBase<T_STATE, T_RESULT> {
	protected String langName;
	protected BlockAst<? extends BlockType> parentBlock;


	public AstMemberInClassParserReusable(String langName, String memberName, BlockAst<? extends BlockType> parentBlock, T_STATE completed, T_STATE failed) {
		super(langName + " " + memberName, completed, failed);
		this.langName = langName;
		this.parentBlock = parentBlock;
	}

}
