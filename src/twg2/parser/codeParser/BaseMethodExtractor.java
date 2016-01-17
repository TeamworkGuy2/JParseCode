package twg2.parser.codeParser;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.block.IntermBlock;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.intermAst.type.TypeSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class BaseMethodExtractor implements AstParser<List<IntermMethodSig.SimpleImpl>> {

	static enum State {
		INIT,
		FINDING_RETURN_TYPE,
		FINDING_NAME,
		FINDING_PARAMS,
		COMPLETE,
		FAILED;
	}


	Keyword keyword;
	IntermBlock<? extends CompoundBlock> parentBlock;
	AstParser<List<AnnotationSig>> annotationParser;
	String methodName;
	TypeSig.Simple returnTypeSig;
	List<IntermMethodSig.SimpleImpl> methods = new ArrayList<>();
	AstParser<TypeSig.Simple> typeParser;
	State state = State.INIT;
	String langName;
	String name;


	@Override
	public String name() {
		return name;
	}


	/**
	 * @param parentBlock
	 * @param annotationParser this annotation parser should be being run external from this instance.  When this instance finds a method signature,
	 * the annotation parser should already contain results (i.e. {@link AstParser#getParserResult()}) for the method's annotations
	 */
	public BaseMethodExtractor(String langName, Keyword keyword, IntermBlock<? extends CompoundBlock> parentBlock,
			AstParser<TypeSig.Simple> typeParser, AstParser<List<AnnotationSig>> annotationParser) {
		this.langName = langName;
		this.name = langName + " method signature";
		this.keyword = keyword;
		this.methods = new ArrayList<>();
		this.parentBlock = parentBlock;
		this.typeParser = typeParser;
		this.annotationParser = annotationParser;
	}


	@Override
	public boolean acceptNext(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}

		if(state == State.INIT && BaseDataTypeExtractor.isPossiblyType(keyword, tokenNode, true)) {
			state = State.FINDING_RETURN_TYPE;
			updateAndCheckTypeParser(tokenNode);
			return state == State.FINDING_NAME;
		}
		else if(state == State.FINDING_RETURN_TYPE) {
			val res = updateAndCheckTypeParser(tokenNode);
			// TODO required because type parser has to look ahead
			if(state == State.FINDING_NAME) {
				// copied from below
				if(AstFragType.isIdentifier(tokenNode.getData())) {
					methodName = tokenNode.getData().getText();
					state = State.FINDING_PARAMS;
					return true;
				}
				state = State.FAILED;
			}
			return res;
		}
		else if(state == State.FINDING_NAME) {
			if(AstFragType.isIdentifier(tokenNode.getData())) {
				methodName = tokenNode.getData().getText();
				state = State.FINDING_PARAMS;
				return true;
			}
			state = State.FAILED;
		}
		else if(state == State.FINDING_PARAMS) {
			if(AstFragType.isBlock(tokenNode.getData(), "(")) {
				state = State.COMPLETE;
				val annotations = new ArrayList<>(annotationParser.getParserResult());
				annotationParser.recycle();

				val params = BaseMethodParametersParser.extractParamsFromSignature(tokenNode);
				methods.add(new IntermMethodSig.SimpleImpl(methodName, NameUtil.newFqName(parentBlock.getDeclaration().getFullName(), methodName), params, returnTypeSig, annotations));
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
			returnTypeSig = typeParser.getParserResult();

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
	public List<IntermMethodSig.SimpleImpl> getParserResult() {
		return methods;
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
	public BaseMethodExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public BaseMethodExtractor copy() {
		val copy = new BaseMethodExtractor(this.langName, this.keyword, this.parentBlock, this.typeParser.copy(), this.annotationParser.copy());
		return copy;
	}


	// package-private
	void reset() {
		methods.clear();
		annotationParser = annotationParser.recycle();
		state = State.INIT;
	}

}
