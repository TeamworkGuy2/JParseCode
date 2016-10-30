package twg2.parser.codeParser;

import java.util.List;

import twg2.parser.fragment.TextFragmentRefToken;
import twg2.treeLike.IndexedSubtreeConsumer;
import twg2.treeLike.simpleTree.SimpleTree;

/** Interface which adds additional parameters to {@link IndexedSubtreeConsumer} designed to consume a {@link SimpleTree} of {@link TextFragmentRefToken}.
 * New parameters are:
 * <ul>
 *   <li>node - the current tree node</li>
 *   <li>(new) nodeFrag - the current tree node's data</li>
 *   <li>idx - the current tree node's index within its parent tree's children list</li>
 *   <li>size - the total number of siblings, including this node</li>
 *   <li>depth - the tree depth of the current node (0 == root, 1 == a child of the root, etc.)</li>
 *   <li>(new) siblings - a list of the current node's parent's children (i.e. all of the current node's siblings) (NOTE: this list is immutable and contains the current node)</li>
 *   <li>parentNode (nullable) - the current tree node's parent</li>
 *   <li>(new) parentNodeFrag (nullable) - the current tree node's parent's data</li>
 * </ul>
 * @author TeamworkGuy2
 * @since 2015-12-9
 */
@FunctionalInterface
public interface AstNodePredicate<T> {

	/**
	 * @param node the current tree node
	 * @param nodeFrag the current node data
	 * @param idx the current node's index in {@code siblings}
	 * @param size the number of siblings
	 * @param depth the current node's depth in the tree
	 * @param siblings the current node's siblings
	 * @param parentNode the parent tree node of the current node
	 * @param parentNodeFrag the parent tree node's data
	 */
	public boolean test(SimpleTree<TextFragmentRefToken<T>> node, TextFragmentRefToken<T> nodeFrag,
			int idx, int size, int depth, List<SimpleTree<TextFragmentRefToken<T>>> siblings,
			SimpleTree<TextFragmentRefToken<T>> parentNode, TextFragmentRefToken<T> parentNodeFrag);

}
