package twg2.parser.tokenizers;

import java.util.ArrayList;
import java.util.List;

import twg2.parser.fragment.CodeTokenType;
import twg2.parser.fragment.TextToken;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.ranges.IntRangeSearcherMutableImpl;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.TreeTraverse;
import twg2.treeLike.parameters.IndexedTreeTraverseParameters;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-09-07
 */
public class CodeTreeToSource {


	public static String toSource(SimpleTree<? extends TextToken<TextFragmentRef, CodeTokenType>> tree, char[] src, int srcOff, int srcLen, boolean includeParsedFragments) {
		StringBuilder sb = new StringBuilder();
		toSource(tree, src, srcOff, srcLen, includeParsedFragments, sb);
		return sb.toString();
	}


	public static void toSource(SimpleTree<? extends TextToken<TextFragmentRef, CodeTokenType>> tree, char[] src, int srcOff, int srcLen, boolean includeParsedFragments, StringBuilder dst) {
		IntRangeSearcherMutableImpl fragments = new IntRangeSearcherMutableImpl(false, false, true);
		List<TextFragmentRef> frags = new ArrayList<>();

		TreeTraverse.Indexed.traverse(IndexedTreeTraverseParameters.allNodes(tree, TreeTraversalOrder.PRE_ORDER, (node) -> node.getChildren().size() > 0, (node) -> node.getChildren())
			.setConsumerIndexed((tokenNode, idx, size, depth, parent) -> {
				TextToken<TextFragmentRef, CodeTokenType> token = tokenNode.getData();
				TextFragmentRef textFrag = token.getToken();
				frags.add(textFrag);
				if(!token.getTokenType().isCompound()) {
					fragments.addRange(textFrag.getOffsetStart(), textFrag.getOffsetEnd() - 1);
				}
			})
		);

		if(includeParsedFragments) {
			for(int i = 0, size = fragments.size(); i < size; i++) {
				dst.append(src, fragments.getLowerBound(i), fragments.getUpperBound(i) + 1);
			}
		}
		else {
			int size = fragments.size();
			dst.append(src, srcOff, size > 0 ? fragments.getLowerBound(0) : (srcOff + srcLen));
			for(int i = 0; i < size - 1; i++) {
				dst.append(src, fragments.getUpperBound(i) + 1, fragments.getLowerBound(i + 1));
			}
			if(size > 0) {
				dst.append(src, fragments.getUpperBound(size - 1) + 1, srcOff + srcLen);
			}
		}
	}

}
