package twg2.parser.codeParser.tools;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.treeLike.simpleTree.SimpleTree;

/** Create an iterator for AST nodes, which skips {@link CodeTokenType} COMMENT and STRING nodes.
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class TokenListIterable implements Iterable<SimpleTree<CodeToken>> {

	/** A {@link SimpleTree}{@code <}{@link CodeToken}{@code >} {@link Supplier}.  When given an {@link Iterable}, this creates
	 * a supplier which returns them in order, returns null once all the values have been iterated over
	 * @author TeamworkGuy2
	 * @since 2016-1-1
	 */
	static class TokenSupplier implements Supplier<SimpleTree<CodeToken>> {
		Iterator<SimpleTree<CodeToken>> childIter;
		Predicate<? super CodeToken> cond;
		int i;


		public TokenSupplier(Iterable<? extends SimpleTree<? extends CodeToken>> childs) {
			this(childs, null);
		}


		public TokenSupplier(Iterable<? extends SimpleTree<? extends CodeToken>> childs, Predicate<? super CodeToken> cond) {
			@SuppressWarnings("unchecked")
			Iterator<SimpleTree<CodeToken>> iter = (Iterator<SimpleTree<CodeToken>>) childs.iterator();
			this.childIter = iter;
			this.cond = cond;
			this.i = 0;
		}


		@Override
		public SimpleTree<CodeToken> get() {
			SimpleTree<CodeToken> child = null;
			CodeTokenType curType = null;
			// only increment the found count when a valid element is encountered, skipping invalid elements
			do {
				if(!childIter.hasNext()) {
					return null;
				}
				child = childIter.next();
				curType = child.getData().getTokenType();
				i++;
			} while(curType == CodeTokenType.COMMENT || curType == CodeTokenType.STRING || (cond != null && !cond.test(child.getData())));
			return child;
		}

	}




	private TokenSupplier tokenStream;
	private EnhancedListBuilderIterator<SimpleTree<CodeToken>> iter;

	public TokenListIterable(Iterable<? extends SimpleTree<? extends CodeToken>> childs) {
		this.tokenStream = new TokenSupplier(childs);
		this.iter = new EnhancedListBuilderIterator<>(tokenStream);
	}


	/** Always returns the same instance
	 */
	@Override
	public EnhancedListBuilderIterator<SimpleTree<CodeToken>> iterator() {
		return iter;
	}

}
