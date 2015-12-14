package parser.condition;

import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-12
 * @param <T_RESULT> the type of result object that parsed data is store in
 */
public interface AstParserCondition<T_RESULT> extends TokenParserCondition<SimpleTree<DocumentFragmentText<CodeFragmentType>>, T_RESULT> {

	@Override
	public AstParserCondition<T_RESULT> copy();


	@Override
	public default AstParserCondition<T_RESULT> recycle() {
		throw new UnsupportedOperationException("AstParserCondition recycling not supported");
	}


	@Override
	public default AstParserCondition<T_RESULT> copyOrReuse() {
		AstParserCondition<T_RESULT> filter = null;
		if(this.canRecycle()) {
			filter = this.recycle();
		}
		else {
			filter = this.copy();
		}
		return filter;
	}

}
