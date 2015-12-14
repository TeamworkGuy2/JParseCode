package codeParser.csharp;

import intermAst.type.TypeSig;
import lombok.val;
import parser.condition.AstParserCondition;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.util.AstFragType;
import baseAst.util.NameUtil;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-12
 */
public class CsDataTypeParser implements AstParserCondition<TypeSig> {

	static enum State {
		INIT,
		FOUND_TYPE_NAME,
		COMPLETE,
		FAILED;
	}


	private TypeSig type;
	private String typeName;
	private State state = State.INIT;
	private boolean allowVoid;
	private boolean prevNodeWasBlockId;


	/**
	 * @param allowVoid indicate whether 'void'/'Void' is a valid data type when parsing (true for method return types, but invalid for field/variable types)
	 */
	public CsDataTypeParser(boolean allowVoid) {
		this.allowVoid = allowVoid;
	}


	@Override
	public boolean acceptNext(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}

		if(state == State.INIT && !prevNodeWasBlockId) {
			// found type name
			if(isPossiblyType(tokenNode, allowVoid)) {
				state = State.FOUND_TYPE_NAME;
				typeName = tokenNode.getData().getText();
				prevNodeWasBlockId = AstFragType.isBlockKeyword(tokenNode.getData());
				return true;
			}
			state = State.INIT;
			prevNodeWasBlockId = AstFragType.isBlockKeyword(tokenNode.getData());
			return false;
		}
		else if(state == State.FOUND_TYPE_NAME) {
			boolean isNullable = false;
			// found optional type marker
			if(AstFragType.isOptionalTypeMarker(tokenNode.getData())) {
				isNullable = true;
			}
			this.state = State.COMPLETE;
			this.type = new TypeSig(NameUtil.splitFqName(typeName), isNullable);
			prevNodeWasBlockId = AstFragType.isBlockKeyword(tokenNode.getData());
			return isNullable;
		}
		state = State.INIT;
		prevNodeWasBlockId = AstFragType.isBlockKeyword(tokenNode.getData());
		return false;
	}


	@Override
	public TypeSig getParserResult() {
		return type;
	}


	@Override
	public boolean isComplete() {
		return state == State.COMPLETE;
	}


	@Override
	public boolean isFailed() {
		return state == State.FAILED;
	}


	@Override
	public boolean canRecycle() {
		return true;
	}


	@Override
	public CsDataTypeParser recycle() {
		reset();
		return this;
	}


	@Override
	public CsDataTypeParser copy() {
		val copy = new CsDataTypeParser(this.allowVoid);
		return copy;
	}


	// package-private
	void reset() {
		type = null;
		typeName = null;
		state = State.INIT;
	}


	/** Check if a tree node is the start of a data type
	 */
	public static boolean isPossiblyType(SimpleTree<DocumentFragmentText<CodeFragmentType>> node, boolean allowVoid) {
		val nodeData = node.getData();
		return AstFragType.isIdentifierOrKeyword(nodeData) && CsKeyword.isNotNonTypeKeyword(nodeData.getText()) || (allowVoid ? "void".equalsIgnoreCase(nodeData.getText()) : false);
	}

}
