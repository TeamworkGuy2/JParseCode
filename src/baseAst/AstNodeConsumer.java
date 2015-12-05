package baseAst;

import java.util.List;

import twg2.treeLike.simpleTree.SimpleTree;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@FunctionalInterface
public interface AstNodeConsumer<T> {

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
	public void accept(SimpleTree<DocumentFragmentText<T>> node, DocumentFragmentText<T> nodeFrag,
			int idx, int size, int depth, List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings,
			SimpleTree<DocumentFragmentText<T>> parentNode, DocumentFragmentText<T> parentNodeFrag);

}
