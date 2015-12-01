package codeParser.csharp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import parser.textFragment.TextFragmentRef;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.TreeTraverse;
import twg2.treeLike.parameters.IndexedTreeTraverseParameters;
import twg2.treeLike.simpleTree.SimpleTree;
import lombok.val;
import codeParser.CodeFile;
import codeParser.CodeFragmentType;
import codeParser.ParseInput;
import codeRepresentation.method.MethodSig;
import documentParser.DocumentFragment;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CSharpDirtyInterfaceExtractor {

	public static List<MethodSig> parse(ParseInput params) {
		val parsedFile = CSharpClassParser.parse(params);
		val methodDefs = extractInterfaceMethods(parsedFile);
		
		return methodDefs;
	}


	public static List<MethodSig> extractInterfaceMethods(CodeFile parsedFile) {
		List<MethodSig> methods = new ArrayList<>();
		val tree = parsedFile.getDoc();
		val src = parsedFile.getSrc();

		TreeTraverse.Indexed.traverse(IndexedTreeTraverseParameters.allNodes(tree, TreeTraversalOrder.PRE_ORDER, (node) -> node.getChildren().size() > 0, (node) -> node.getChildren())
			.setConsumerIndexed((tokenNode, idx, size, depth, parent) -> {
				List<SimpleTree<DocumentFragment<CodeFragmentType>>> children = parent != null ? parent.getChildren() : null;
				DocumentFragment<CodeFragmentType> token = tokenNode.getData();
				TextFragmentRef textFrag = token.getTextFragment();
				String text = textFrag.getText(src).toString();
				if(children != null && token.getFragmentType().isCompound() && text.indexOf('(') == 0) {
					DocumentFragment<CodeFragmentType> prev1 = getType(children, idx, -1);
					DocumentFragment<CodeFragmentType> prev2 = getType(children, idx, -2);
					DocumentFragment<CodeFragmentType> parentFrag = queryParent(tokenNode, 1, (frag) -> {
						return frag.getFragmentType() == CodeFragmentType.BLOCK && frag.getTextFragment().getText(src).toString().indexOf('{') == 0;
					});
					if(parentFrag != null) {
						methods.add(new MethodSig(prev2.getTextFragment().getText(src).toString(), prev1.getTextFragment().getText(src).toString(), token.getTextFragment().getText(src).toString()));
					}
				}
			})
		);

		return methods;
	}


	static final DocumentFragment<CodeFragmentType> getType(List<SimpleTree<DocumentFragment<CodeFragmentType>>> children, int off, int subOff) {
		return querySibling(children, off, subOff, null);
	}


	static final DocumentFragment<CodeFragmentType> queryPrevSibling(List<SimpleTree<DocumentFragment<CodeFragmentType>>> children, int off, Predicate<DocumentFragment<CodeFragmentType>> cond) {
		return querySibling(children, off, Integer.MIN_VALUE, cond);
	}


	static final DocumentFragment<CodeFragmentType> queryNextSibling(List<SimpleTree<DocumentFragment<CodeFragmentType>>> children, int off, Predicate<DocumentFragment<CodeFragmentType>> cond) {
		return querySibling(children, off, Integer.MAX_VALUE, cond);
	}


	static final DocumentFragment<CodeFragmentType> querySibling(List<SimpleTree<DocumentFragment<CodeFragmentType>>> children, int off, int subOff, Predicate<DocumentFragment<CodeFragmentType>> cond) {
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

		return getIdx(children, off + found);
	}


	static final DocumentFragment<CodeFragmentType> queryParent(SimpleTree<DocumentFragment<CodeFragmentType>> node, int maxDepth, Predicate<DocumentFragment<CodeFragmentType>> cond) {
		SimpleTree<DocumentFragment<CodeFragmentType>> parent = node.getParent();
		int depth = 1;
		while(parent != null && !cond.test(parent.getData()) && depth <= maxDepth) {
			parent = parent.getParent();
			depth++;
		}
		return parent != null && depth <= maxDepth ? parent.getData() : null;
	}


	private static final <U> U getIdx(List<SimpleTree<U>> list, int i) {
		return i < list.size() && i > -1 ? list.get(i).getData() : null;
	}

}
