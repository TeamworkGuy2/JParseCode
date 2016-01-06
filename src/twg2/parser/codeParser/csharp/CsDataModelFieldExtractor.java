package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.baseAst.tools.AstUtil;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.condition.AstParserCondition;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.block.IntermBlock;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.type.TypeSig;
import twg2.parser.output.JsonWritableSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class CsDataModelFieldExtractor implements AstParserCondition<List<IntermFieldSig>> {

	static enum State {
		INIT,
		FINDING_DATA_TYPE,
		FINDING_NAME,
		FOUND_NAME_CHECK,
		COMPLETE,
		FAILED;
	}


	IntermBlock<? extends JsonWritableSig, ? extends CompoundBlock> parentBlock;
	CsAnnotationExtractor annotationParser;
	List<IntermFieldSig> fields = new ArrayList<>();
	TypeSig.Simple fieldTypeSig;
	String fieldName;
	CsDataTypeExtractor typeParser = new CsDataTypeExtractor(false);
	State state = State.INIT;


	public CsDataModelFieldExtractor(IntermBlock<? extends JsonWritableSig, ? extends CompoundBlock> parentBlock, CsAnnotationExtractor annotationParser) {
		this.parentBlock = parentBlock;
		this.annotationParser = annotationParser;
	}


	@Override
	public boolean acceptNext(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}

		if(state == State.INIT && CsDataTypeExtractor.isPossiblyType(tokenNode, false)) {
			state = State.FINDING_DATA_TYPE;
			val res = updateAndCheckTypeParser(tokenNode);
			return res;
		}
		else if(state == State.FINDING_DATA_TYPE) {
			val res = updateAndCheckTypeParser(tokenNode);
			// TODO because the type parser has to look ahead for now, but may not consume the look ahead token while also completing based on a look ahead
			if(!res && state == State.FINDING_NAME) {
				// copied from below
				if(AstFragType.isIdentifier(tokenNode.getData())) {
					fieldName = tokenNode.getData().getText();
					state = State.FOUND_NAME_CHECK;
					return true;
				}
				state = State.FAILED;
			}
			return res;
		}
		else if(state == State.FINDING_NAME) {
			if(AstFragType.isIdentifier(tokenNode.getData())) {
				fieldName = tokenNode.getData().getText();
				state = State.FOUND_NAME_CHECK;
				return true;
			}
			state = State.FAILED;
		}
		else if(state == State.FOUND_NAME_CHECK) {
			if((tokenNode == null || tokenNode.getData().getFragmentType() != CodeFragmentType.BLOCK ||
					AstUtil.blockContainsOnly(tokenNode, (node, type) -> type == CodeFragmentType.IDENTIFIER && ("get".equals(node.getText()) || "set".equals(node.getText())), true, CodeFragmentType.SEPARATOR, CodeFragmentType.COMMENT))) {
				state = State.COMPLETE;
				val annotations = new ArrayList<>(annotationParser.getParserResult());
				annotationParser.recycle();
				fields.add(new IntermFieldSig(fieldName, NameUtil.newFqName(parentBlock.getDeclaration().getFullyQualifyingName(), fieldName), fieldTypeSig, annotations));
				return true;
			}
			state = State.FAILED;
		}
		return false;
	}


	private boolean updateAndCheckTypeParser(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		boolean res = typeParser.acceptNext(tokenNode);
		boolean complete = typeParser.isComplete();
		boolean failed = typeParser.isFailed();
		if(complete) {
			fieldTypeSig = typeParser.getParserResult();
			typeParser = typeParser.recycle();
			state = State.FINDING_NAME;
		}
		else if(failed) {
			typeParser = typeParser.recycle();
			state = State.FAILED;
		}
		return res;
	}


	@Override
	public List<IntermFieldSig> getParserResult() {
		return fields;
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
	public CsDataModelFieldExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CsDataModelFieldExtractor copy() {
		val copy = new CsDataModelFieldExtractor(this.parentBlock, this.annotationParser);
		return copy;
	}


	// package-private
	void reset() {
		fields.clear();
	}

}
