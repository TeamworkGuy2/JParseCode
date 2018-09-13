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


	/** Check if a {@link TextToken} has a fragment type equal to {@code type1}
	 */
	public static final boolean isType(TextToken<?, CodeTokenType> node, CodeTokenType type1) {
		return node != null && (node.getTokenType() == type1);
	}


	/** Check if a {@link TextToken} has a fragment type equal to {@code type1 OR type2}
	 */
	public static final boolean isType(TextToken<?, CodeTokenType> node, CodeTokenType type1, CodeTokenType type2) {
		return node != null && (node.getTokenType() == type1 || node.getTokenType() == type2);
	}


	/** Check if a {@link TextToken} has a fragment type equal to {@code type1 OR type2 OR type3}
	 */
	public static final boolean isType(TextToken<?, CodeTokenType> node, CodeTokenType type1, CodeTokenType type2, CodeTokenType type3) {
		return node != null && (node.getTokenType() == type1 || node.getTokenType() == type2 || node.getTokenType() == type3);
	}


	/** Check if a {@link TextToken} has a fragment type equal to any of {@code types}
	 */
	public static final boolean isType(TextToken<?, CodeTokenType> node, CodeTokenType... types) {
		if(node == null) {
			return false;
		}
		CodeTokenType nodeType = node.getTokenType();
		for(CodeTokenType type : types) {
			if(nodeType != type) {
				return false;
			}
		}
		return true;
	}


	public static final boolean isOperator(CodeToken node, Operator op) {
		return node != null && node.getTokenType() == CodeTokenType.OPERATOR && op.toSrc().equals(node.getText());
	}


	public static final boolean isOptionalTypeMarker(CodeToken node) {
		return node != null && (node.getTokenType() == CodeTokenType.OPERATOR && "?".equals(node.getText()));
	}


	public static final boolean isSeparator(CodeToken node, String separator) {
		return node != null && (node.getTokenType() == CodeTokenType.SEPARATOR && separator.equals(node.getText()));
	}


	public static final boolean isIdentifier(CodeToken node) {
		return node != null && (node.getTokenType() == CodeTokenType.IDENTIFIER);
	}


	public static final boolean isIdentifierOrKeyword(CodeToken node) {
		return node != null && (node.getTokenType() == CodeTokenType.KEYWORD || node.getTokenType() == CodeTokenType.IDENTIFIER);
	}


	public static final boolean isKeyword(CodeToken node) {
		return node != null && (node.getTokenType() == CodeTokenType.KEYWORD);
	}


	public static final boolean isBlock(CodeToken node, String blockSymbol) {
		return node != null && node.getTokenType().isCompound() && node.getText().startsWith(blockSymbol);
	}


	// TODO unused
	public static final boolean blockContainsOnly(SimpleTree<CodeToken> block, BiPredicate<CodeToken, CodeTokenType> cond, boolean emptyTreeValid, CodeTokenType... optionalAllows) {
		if(block == null) {
			return emptyTreeValid;
		}
		if(optionalAllows == null) {
			optionalAllows = new CodeTokenType[0];
		}
		val childs = block.getChildren();
		if(childs.size() == 0) {
			return false;
		}

		for(val child : childs) {
			val frag = child.getData();
			if(ArrayUtil.indexOf(optionalAllows, frag.getTokenType()) < 0 && !cond.test(frag, frag.getTokenType())) {
				return false;
			}
		}
		return true;
	}

}
