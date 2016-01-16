package twg2.parser.documentParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import twg2.collections.tuple.Tuples;
import twg2.collections.util.dataStructures.PairList;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.text.CharMultiConditionParser;
import twg2.parser.text.CharParserFactory;
import twg2.parser.textFragment.TextConsumer;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textFragment.TextTransformer;
import twg2.parser.textParser.TextParser;
import twg2.ranges.IntRangeSearcherMutableImpl;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.TreeTraverse;
import twg2.treeLike.parameters.IndexedTreeTraverseParameters;
import twg2.treeLike.simpleTree.SimpleTree;
import twg2.treeLike.simpleTree.SimpleTreeImpl;

/**
 * @author TeamworkGuy2
 * @since 2015-5-27
 */
public class DocumentParser<T> {
	private PairList<CharParserFactory, TextTransformer<T>> parsers;


	public DocumentParser() {
		this.parsers = new PairList<>();
	}


	public void addFragmentParser(CharParserFactory fragmentParser, TextTransformer<T> handler) {
		this.parsers.add(fragmentParser, handler);
	}


	/** Consumes a {@link TextParser}, pass the text through this document parser's list of {@link TextTransformer TextTransformers}
	 * @param input the TextParser to read text from
	 * @param root the root element to use for the returned {@link SimpleTree}
	 * @param isParent determine if a document fragment is possibly a parent fragment
	 * @param isInside determines if a document fragment contains another document fragment
	 * @return a {@link SimpleTree} containing tokens parsed from the input
	 */
	public <D extends DocumentFragment<S, T>, S> SimpleTree<D> parseDocument(TextParser input, D root, BiFunction<T, TextFragmentRef.Impl, D> docFragConstructor, Function<D, Boolean> isParent, IsParentChild<D> isInside) {
		SimpleTreeImpl<D> tree = new SimpleTreeImpl<>(root);

		List<Entry<CharParserFactory, TextConsumer>> conditions = new ArrayList<>();

		for(int i = 0, size = parsers.size(); i < size; i++) {
			TextTransformer<T> transformer = parsers.getValue(i);

			conditions.add(Tuples.of(parsers.getKey(i), (text, off, len, lineStart, columnStart, lineEnd, columnEnd) -> {
				T elemType = transformer.apply(text, off, len);
				TextFragmentRef.Impl textFragment = new TextFragmentRef.Impl(off, off + len, lineStart, columnStart, lineEnd, columnEnd);

				D docFrag = docFragConstructor.apply(elemType, textFragment);

				if(isParent.apply(docFrag)) {
					List<SimpleTreeImpl<D>> subChildren = new ArrayList<>();
					getChildrenInRange(tree.getChildrenRaw(), docFrag, isInside, subChildren);
					removeChildren(tree, subChildren);

					// add after checking for children, so that this fragment does not include itself as one of it's children
					SimpleTreeImpl<D> subTree = tree.addChild(docFrag);
					for(int ii = 0, sizeI = subChildren.size(); ii < sizeI; ii++) {
						subTree.addChildTree(subChildren.get(ii));
					}
				}
				else {
					// add after checking for children, so that this fragment does not include itself as one of it's children
					tree.addChild(docFrag);
				}
			}));
		}

		CharMultiConditionParser parser = new CharMultiConditionParser(conditions);

		while(input.hasNext()) {
			char ch = input.nextChar();
			parser.acceptNext(ch, input);
		}

		return tree;
	}


	public static <D extends DocumentFragment<S, T>, S, T> List<SimpleTreeImpl<D>> getChildrenInRange(List<SimpleTreeImpl<D>> src,
			D parent, IsParentChild<D> isInside, List<SimpleTreeImpl<D>> dstToAddTo) {
		for(int i = 0, size = src.size(); i < size; i++) {
			SimpleTreeImpl<D> child = src.get(i);
			if(isInside.test(parent, child.getData())) {
				dstToAddTo.add(child);
			}
		}
		return dstToAddTo;
	}


	public static <D extends DocumentFragment<S, T>, S, T> void removeChildren(SimpleTreeImpl<D> tree, List<SimpleTreeImpl<D>> children) {
		for(int i = 0, size = children.size(); i < size; i++) {
			SimpleTree<D> child = children.get(i);
			boolean res = tree.removeChild(child);
			if(res == false) {
				throw new IllegalStateException("could not remove child '" + child + "' from tree '" + tree + "'");
			}
		}
	}


	public static String toSource(SimpleTree<? extends DocumentFragment<TextFragmentRef, CodeFragmentType>> tree, String src, boolean includeParsedFragments) {
		StringBuilder sb = new StringBuilder();
		toSource(tree, src, includeParsedFragments, sb);
		return sb.toString();
	}


	public static void toSource(SimpleTree<? extends DocumentFragment<TextFragmentRef, CodeFragmentType>> tree, String src, boolean includeParsedFragments, StringBuilder dst) {
		IntRangeSearcherMutableImpl fragSegments = new IntRangeSearcherMutableImpl(false, false, true);
		List<TextFragmentRef> frags = new ArrayList<>();

		TreeTraverse.Indexed.traverse(IndexedTreeTraverseParameters.allNodes(tree, TreeTraversalOrder.PRE_ORDER, (node) -> node.getChildren().size() > 0, (node) -> node.getChildren())
			.setConsumerIndexed((tokenNode, idx, size, depth, parent) -> {
				DocumentFragment<TextFragmentRef, CodeFragmentType> token = tokenNode.getData();
				TextFragmentRef textFrag = token.getTextFragment();
				frags.add(textFrag);
				if(!token.getFragmentType().isCompound()) {
					fragSegments.addRange(textFrag.getOffsetStart(), textFrag.getOffsetEnd() - 1);
				}
			})
		);

		if(includeParsedFragments) {
			for(int i = 0, size = fragSegments.size(); i < size; i++) {
				dst.append(src.substring(fragSegments.getLowerBound(i), fragSegments.getUpperBound(i) + 1));
			}
		}
		else {
			int size = fragSegments.size();
			dst.append(src.substring(0, size > 0 ? fragSegments.getLowerBound(0) : src.length()));
			for(int i = 0; i < size - 1; i++) {
				dst.append(src.substring(fragSegments.getUpperBound(i) + 1, fragSegments.getLowerBound(i + 1)));
			}
			if(size > 0) {
				dst.append(src.substring(fragSegments.getUpperBound(size - 1) + 1, src.length()));
			}
		}
	}

}
