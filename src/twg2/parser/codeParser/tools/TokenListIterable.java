package twg2.parser.codeParser.tools;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.documentParser.CodeFragment;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class TokenListIterable implements Iterable<SimpleTree<CodeFragment>> {

	static class TokenSupplier implements Supplier<SimpleTree<CodeFragment>> {
		Iterator<SimpleTree<CodeFragment>> childIter;
		Predicate<? super CodeFragment> cond;
		int i;

		public TokenSupplier(Iterable<? extends SimpleTree<? extends CodeFragment>> childs) {
			this(childs, null);
		}


		public TokenSupplier(Iterable<? extends SimpleTree<? extends CodeFragment>> childs, Predicate<? super CodeFragment> cond) {
			@SuppressWarnings("unchecked")
			Iterator<SimpleTree<CodeFragment>> iter = (Iterator<SimpleTree<CodeFragment>>) childs.iterator();
			this.childIter = iter;
			this.cond = cond;
			this.i = 0;
		}


		@Override
		public SimpleTree<CodeFragment> get() {
			SimpleTree<CodeFragment> child = null;
			CodeFragmentType curType = null;
			// only increment the found count when a valid element is encountered, skipping invalid elements
			do {
				if(!childIter.hasNext()) {
					return null;
				}
				child = childIter.next();
				curType = child.getData().getFragmentType();
				i++;
			} while(curType == CodeFragmentType.COMMENT || curType == CodeFragmentType.STRING || (cond != null && !cond.test(child.getData())));
			return child;
		}

	}




	private TokenSupplier tokenStream;
	private EnhancedListBuilderIterator<SimpleTree<CodeFragment>> iter;

	public TokenListIterable(Iterable<? extends SimpleTree<? extends CodeFragment>> childs) {
		this.tokenStream = new TokenSupplier(childs);
		this.iter = new EnhancedListBuilderIterator<>(tokenStream);
	}


	/** Always returns the same instance
	 */
	@Override
	public EnhancedListBuilderIterator<SimpleTree<CodeFragment>> iterator() {
		return iter;
	}

}
