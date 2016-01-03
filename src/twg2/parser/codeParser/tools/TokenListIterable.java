package twg2.parser.codeParser.tools;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class TokenListIterable implements Iterable<SimpleTree<DocumentFragmentText<CodeFragmentType>>> {

	static class TokenSupplier implements Supplier<SimpleTree<DocumentFragmentText<CodeFragmentType>>> {
		Iterator<SimpleTree<DocumentFragmentText<CodeFragmentType>>> childIter;
		Predicate<? super DocumentFragmentText<? super CodeFragmentType>> cond;
		int i;

		public TokenSupplier(Iterable<? extends SimpleTree<? extends DocumentFragmentText<? extends CodeFragmentType>>> childs) {
			this(childs, null);
		}


		public TokenSupplier(Iterable<? extends SimpleTree<? extends DocumentFragmentText<? extends CodeFragmentType>>> childs, Predicate<? super DocumentFragmentText<? super CodeFragmentType>> cond) {
			@SuppressWarnings("unchecked")
			Iterator<SimpleTree<DocumentFragmentText<CodeFragmentType>>> iter = (Iterator<SimpleTree<DocumentFragmentText<CodeFragmentType>>>) childs.iterator();
			this.childIter = iter;
			this.cond = cond;
			this.i = 0;
		}


		@Override
		public SimpleTree<DocumentFragmentText<CodeFragmentType>> get() {
			SimpleTree<DocumentFragmentText<CodeFragmentType>> child = null;
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
	private EnhancedListBuilderIterator<SimpleTree<DocumentFragmentText<CodeFragmentType>>> iter;

	public TokenListIterable(Iterable<? extends SimpleTree<? extends DocumentFragmentText<? extends CodeFragmentType>>> childs) {
		this.tokenStream = new TokenSupplier(childs);
		this.iter = new EnhancedListBuilderIterator<>(tokenStream);
	}


	/** Always returns the same instance
	 */
	@Override
	public EnhancedListBuilderIterator<SimpleTree<DocumentFragmentText<CodeFragmentType>>> iterator() {
		return iter;
	}

}
