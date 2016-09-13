package twg2.parser.fragment;

import java.util.function.BiPredicate;

import lombok.val;
import twg2.arrays.ArrayUtil;
import twg2.parser.codeParser.Operator;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-10
 */
public class AstFragType {


	/** Check if a {@link DocumentFragment} has a fragment type equal to {@code type1}
	 */
	public static final boolean isType(DocumentFragment<?, CodeFragmentType> node, CodeFragmentType type1) {
		return node != null && (node.getFragmentType() == type1);
	}


	/** Check if a {@link DocumentFragment} has a fragment type equal to {@code type1 OR type2}
	 */
	public static final boolean isType(DocumentFragment<?, CodeFragmentType> node, CodeFragmentType type1, CodeFragmentType type2) {
		return node != null && (node.getFragmentType() == type1 || node.getFragmentType() == type2);
	}


	/** Check if a {@link DocumentFragment} has a fragment type equal to {@code type1 OR type2 type3}
	 */
	public static final boolean isType(DocumentFragment<?, CodeFragmentType> node, CodeFragmentType type1, CodeFragmentType type2, CodeFragmentType type3) {
		return node != null && (node.getFragmentType() == type1 || node.getFragmentType() == type2 || node.getFragmentType() == type3);
	}


	/** Check if a {@link DocumentFragment} has a fragment type equal to any of {@code types}
	 */
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


	public static final boolean isOperator(CodeFragment node, Operator op) {
		return node != null && node.getFragmentType() == CodeFragmentType.OPERATOR && op.toSrc().equals(node.getText());
	}


	public static final boolean isOptionalTypeMarker(CodeFragment node) {
		return node != null && (node.getFragmentType() == CodeFragmentType.OPERATOR && "?".equals(node.getText()));
	}


	public static final boolean isSeparator(CodeFragment node, String separator) {
		return node != null && (node.getFragmentType() == CodeFragmentType.SEPARATOR && separator.equals(node.getText()));
	}


	public static final boolean isIdentifier(CodeFragment node) {
		return node != null && (node.getFragmentType() == CodeFragmentType.IDENTIFIER);
	}


	public static final boolean isIdentifierOrKeyword(CodeFragment node) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD || node.getFragmentType() == CodeFragmentType.IDENTIFIER);
	}


	public static final boolean isKeyword(CodeFragment node) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD);
	}


	public static final boolean isBlock(CodeFragment node, String blockSymbol) {
		return node != null && node.getFragmentType().isCompound() && node.getText().startsWith(blockSymbol);
	}


	// TODO unused
	public static final boolean blockContainsOnly(SimpleTree<CodeFragment> block, BiPredicate<CodeFragment, CodeFragmentType> cond, boolean emptyTreeValid, CodeFragmentType... optionalAllows) {
		if(block == null) {
			return emptyTreeValid;
		}
		if(optionalAllows == null) {
			optionalAllows = new CodeFragmentType[0];
		}
		val childs = block.getChildren();
		if(childs.size() == 0) {
			return false;
		}

		for(val child : childs) {
			val frag = child.getData();
			if(ArrayUtil.indexOf(optionalAllows, frag.getFragmentType()) < 0 && !cond.test(frag, frag.getFragmentType())) {
				return false;
			}
		}
		return true;
	}

}
