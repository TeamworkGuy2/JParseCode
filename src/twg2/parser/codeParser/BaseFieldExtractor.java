package twg2.parser.codeParser;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.baseAst.tools.AstUtil;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.condition.AstParser;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.block.IntermBlock;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.type.TypeSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class BaseFieldExtractor implements AstParser<List<IntermFieldSig>> {

	static enum State {
		INIT,
		FINDING_DATA_TYPE,
		FINDING_NAME,
		FOUND_NAME_CHECK,
		COMPLETE,
		FAILED;
	}


	Keyword keyword;
	IntermBlock<? extends CompoundBlock> parentBlock;
	AstParser<List<AnnotationSig>> annotationParser;
	List<IntermFieldSig> fields = new ArrayList<>();
	TypeSig.Simple fieldTypeSig;
	String fieldName;
	AstParser<TypeSig.Simple> typeParser;
	State state = State.INIT;
	String langName;
	String name;


	public BaseFieldExtractor(String langName, Keyword keyword, IntermBlock<? extends CompoundBlock> parentBlock,
			AstParser<TypeSig.Simple> typeParser, AstParser<List<AnnotationSig>> annotationParser) {
		this.langName = langName;
		this.name = langName + " field";
		this.keyword = keyword;
		this.parentBlock = parentBlock;
		this.typeParser = typeParser;
		this.annotationParser = annotationParser;
	}


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}

		if(state == State.INIT && BaseDataTypeExtractor.isPossiblyType(keyword, tokenNode, false)) {
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
				fields.add(new IntermFieldSig(fieldName, NameUtil.newFqName(parentBlock.getDeclaration().getFullName(), fieldName), fieldTypeSig, annotations));
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
	public BaseFieldExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public BaseFieldExtractor copy() {
		val copy = new BaseFieldExtractor(this.langName, this.keyword, this.parentBlock, this.typeParser.copy(), this.annotationParser.copy());
		return copy;
	}


	// package-private
	void reset() {
		fields.clear();
	}

}
