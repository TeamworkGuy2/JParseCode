package twg2.parser.stateMachine;

import twg2.parser.condition.TokenParser;
import twg2.parser.fragment.CodeToken;
import twg2.treeLike.simpleTree.SimpleTree;

/** Base interface for AST transformers/parsers.
 * Implementations accept {@link SimpleTree}{@code <}{@link CodeToken}{@code >} AST nodes.
 * Implementations by design are supposed to accept these AST nodes until a failure or completion state is reached and then return a result based on AST nodes feed to them
 * @author TeamworkGuy2
 * @since 2015-12-12
 * @param <T_RESULT> the type of result object that parsed data is store in
 */
public interface AstParser<T_RESULT> extends TokenParser<SimpleTree<CodeToken>, T_RESULT> {

	@Override
	public AstParser<T_RESULT> copy();


	@Override
	public default AstParser<T_RESULT> recycle() {
		throw new UnsupportedOperationException("AstParser recycling not supported");
	}


	@Override
	public default AstParser<T_RESULT> copyOrReuse() {
		AstParser<T_RESULT> filter = null;
		if(this.canRecycle()) {
			filter = this.recycle();
		}
		else {
			filter = this.copy();
		}
		return filter;
	}


	/** This function should be called when a block completes and no more tokens are going to be passed to {@link #acceptNext(Object)}
	 */
	public default void blockComplete() {
		// default implementation
	}

}
