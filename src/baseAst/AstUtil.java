package baseAst;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import lombok.val;
import twg2.treeLike.IndexedSubtreeConsumer;
import twg2.treeLike.SubtreeConsumer;
import twg2.treeLike.SubtreeTransformer;
import twg2.treeLike.TreeTransform;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.TreeTraverse;
import twg2.treeLike.parameters.IndexedTreeTraverseParameters;
import twg2.treeLike.simpleTree.SimpleTree;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragment;
import documentParser.DocumentFragmentRef;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class AstUtil {

	public static final void forEach(SimpleTree<DocumentFragmentText<CodeFragmentType>> tree, IndexedSubtreeConsumer<SimpleTree<DocumentFragmentText<CodeFragmentType>>> indexedSubTreeConsumer) {
		TreeTraverse.Indexed.traverse(IndexedTreeTraverseParameters.allNodes(tree, TreeTraversalOrder.PRE_ORDER,
				(node) -> node.getChildren().size() > 0,
				(node) -> node.getChildren())
			.setConsumerIndexed(indexedSubTreeConsumer));
	}


	public static final void forEach(SimpleTree<DocumentFragmentText<CodeFragmentType>> tree, AstNodeConsumer<CodeFragmentType> indexedSubTreeConsumer) {
		TreeTraverse.Indexed.traverse(IndexedTreeTraverseParameters.allNodes(tree, TreeTraversalOrder.PRE_ORDER,
				(node) -> node.getChildren().size() > 0,
				(node) -> node.getChildren())
			.setConsumerIndexed((node, idx, size, depth, parent) -> {
				List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings = parent != null ? parent.getChildren() : Collections.emptyList();
				indexedSubTreeConsumer.accept(node, node.getData(), idx, size, depth, siblings, parent, parent != null ? parent.getData() : null);
			}));
	}


	public static final SimpleTree<DocumentFragmentText<CodeFragmentType>> transformTreeRefToText(String src, SimpleTree<? extends DocumentFragmentRef<CodeFragmentType>> tree) {
		DocumentFragmentText<CodeFragmentType> rootData = tree.getData() != null ? new DocumentFragmentText<CodeFragmentType>(tree.getData().getFragmentType(), tree.getData().getTextFragment(), tree.getData().getTextFragment().getText(src).toString()) : null;
		return transform(tree, rootData, (branch, transformedParent, originalParent) -> {
			return branch != null ? new DocumentFragmentText<CodeFragmentType>(branch.getFragmentType(), branch.getTextFragment(), branch.getTextFragment().getText(src).toString()) : null;
		}, (branch, depth, parentBranch) -> {
		});
	}


	public static final <S, D> SimpleTree<D> transform(SimpleTree<S> src, D rootDataTransformed, SubtreeTransformer<S, D> transformer, SubtreeConsumer<D> consumer) {
		SimpleTree<D> dstTree = TreeTransform.transformSimpleTree(src, rootDataTransformed, transformer, consumer, null, null);
		return dstTree;
	}


	/** Get the {@code subOff} sibling away from the node at index {@code off}, not counting {@link CodeFragmentType#COMMENT} or {@link CodeFragmentType#STRING} siblings.<br>
	 * For example, given a list {@code [IDENTIFIER, STRING, COMMENT, KEYWORD, COMMENT]}:<br>
	 * Searching for sibling {@code 2} from {@code off = 0}, returns null because there is only 1 non COMMENT/STRING sibling after the first sibling<br>
	 * Searching for sibling {@code -1} from {@code off = 3}, returns IDENTIFIER because the first non COMMENT/STRING sibling preceding KEYWORD at offset 3 is the first sibling, IDENTIFIER.
	 * @param children the list of siblings
	 * @param off the offset of the current node
	 * @param subOff the offset of the sibling to find
	 * @return a sibling matching the above described criteria
	 */
	public static final DocumentFragmentText<CodeFragmentType> getSiblingData(List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> children, int off, int subOff) {
		val res = querySibling(children, off, subOff, null);
		return res != null ? res.getData() : null;
	}


	public static final SimpleTree<DocumentFragmentText<CodeFragmentType>> getSibling(List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> children, int off, int subOff) {
		return querySibling(children, off, subOff, null);
	}


	public static final SimpleTree<DocumentFragmentText<CodeFragmentType>> queryPrevSibling(List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> children, int off, Predicate<DocumentFragmentText<CodeFragmentType>> cond) {
		return querySibling(children, off, Integer.MIN_VALUE, cond);
	}


	public static final SimpleTree<DocumentFragmentText<CodeFragmentType>> queryNextSibling(List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> children, int off, Predicate<DocumentFragmentText<CodeFragmentType>> cond) {
		return querySibling(children, off, Integer.MAX_VALUE, cond);
	}


	public static final SimpleTree<DocumentFragmentText<CodeFragmentType>> querySibling(List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> children, int off, int subOff, Predicate<DocumentFragmentText<CodeFragmentType>> cond) {
		if(subOff == 0) {
			return getIdx(children, off);
		}

		int incr = subOff > 0 ? 1 : -1;
		int found = 0;
		for(int i = off + incr, size = children.size(); i < size && i > -1; i+=incr) {
			val curType = children.get(i).getData().getFragmentType();
			// only increment the found count when a valid element is encountered, skipping invalid elements
			if(curType != CodeFragmentType.COMMENT && curType != CodeFragmentType.STRING && (cond == null || cond.test(children.get(i).getData()))) {
				found += incr;
				if(Math.abs(found) >= Math.abs(subOff)) {
					break;
				}
			}
		}

		return Math.abs(found) >= Math.abs(subOff) ? getIdx(children, off + found) : null;
	}


	public static final DocumentFragmentText<CodeFragmentType> queryParent(SimpleTree<DocumentFragmentText<CodeFragmentType>> node, int maxDepth, Predicate<DocumentFragmentText<CodeFragmentType>> cond) {
		SimpleTree<DocumentFragmentText<CodeFragmentType>> parent = node.getParent();
		int depth = 1;
		while(parent != null && !cond.test(parent.getData()) && depth <= maxDepth) {
			parent = parent.getParent();
			depth++;
		}
		return parent != null && depth <= maxDepth ? parent.getData() : null;
	}


	public static final boolean blockContainsOnly(SimpleTree<DocumentFragmentText<CodeFragmentType>> block, BiPredicate<DocumentFragmentText<CodeFragmentType>, CodeFragmentType> cond, boolean emptyTreeValid) {
		if(block == null) {
			return emptyTreeValid;
		}

		for(val child : block.getChildren()) {
			val frag = child.getData();
			if(!cond.test(frag, frag.getFragmentType())) {
				return false;
			}
		}
		return true;
	}


	private static final <U> SimpleTree<U> getIdx(List<SimpleTree<U>> list, int i) {
		return i < list.size() && i > -1 ? list.get(i) : null;
	}


	//private static final <U> U getIdxData(List<SimpleTree<U>> list, int i) {
	//	return i < list.size() && i > -1 ? list.get(i).getData() : null;
	//}


	public static final boolean isType(DocumentFragment<?, CodeFragmentType> node, CodeFragmentType type1) {
		return node != null && (node.getFragmentType() == type1);
	}


	public static final boolean isType(DocumentFragment<?, CodeFragmentType> node, CodeFragmentType type1, CodeFragmentType type2) {
		return node != null && (node.getFragmentType() == type1 || node.getFragmentType() == type2);
	}


	public static final boolean isType(DocumentFragment<?, CodeFragmentType> node, CodeFragmentType type1, CodeFragmentType type2, CodeFragmentType type3) {
		return node != null && (node.getFragmentType() == type1 || node.getFragmentType() == type2 || node.getFragmentType() == type3);
	}


	public static final boolean isType(DocumentFragment<?, CodeFragmentType> node, CodeFragmentType... types) {
		if(node == null) {
			return false;
		}
		CodeFragmentType nodeType = node.getFragmentType();
		for(CodeFragmentType type : types) {
			if(nodeType != type) {
				return false;
			}
		}
		return true;
	}


}
