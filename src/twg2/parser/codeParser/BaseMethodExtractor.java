package twg2.parser.codeParser;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.method.MethodSig;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.baseAst.AccessModifier;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class BaseMethodExtractor implements AstParser<List<MethodSig.SimpleImpl>> {

	static enum State {
		INIT,
		FINDING_ACCESS_MODIFIERS,
		FINDING_RETURN_TYPE,
		FINDING_NAME,
		FINDING_PARAMS,
		COMPLETE,
		FAILED;
	}


	KeywordUtil keywordUtil;
	BlockAst<? extends CompoundBlock> parentBlock;
	AstParser<List<AnnotationSig>> annotationParser;
	AstParser<List<String>> commentParser;
	AstParser<TypeSig.Simple> typeParser;
	List<AccessModifier> accessModifiers = new ArrayList<>();
	String methodName;
	TypeSig.Simple returnTypeSig;
	List<MethodSig.SimpleImpl> methods = new ArrayList<>();
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
	public BaseMethodExtractor(String langName, KeywordUtil keywordUtil, BlockAst<? extends CompoundBlock> parentBlock,
			AstParser<TypeSig.Simple> typeParser, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		this.langName = langName;
		this.name = langName + " method signature";
		this.parentBlock = parentBlock;
		this.keywordUtil = keywordUtil;
		this.methods = new ArrayList<>();
		this.typeParser = typeParser;
		this.annotationParser = annotationParser;
		this.commentParser = commentParser;
	}


	@Override
	public boolean acceptNext(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}
		Consume res = null;

		if(state == State.INIT) {
			if(keywordUtil.isMethodModifierKeyword(tokenNode.getData())) {
				state = State.FINDING_ACCESS_MODIFIERS;
				res = findingAccessModifiers(tokenNode);
				if(res.isAccept()) { return true; }
			}
			if(BaseDataTypeExtractor.isPossiblyType(keywordUtil, tokenNode, true)) {
				state = State.FINDING_RETURN_TYPE;
				res = updateAndCheckTypeParser(tokenNode);
				if(res.isAccept()) { return true; }
			}
		}
		else if(state == State.FINDING_ACCESS_MODIFIERS) {
			res = findingAccessModifiers(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_RETURN_TYPE) {
			res = findingReturnType(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_NAME) {
			res = findingName(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_PARAMS) {
			res = findingParams(tokenNode);
			if(res.isAccept()) { return true; }
		}
		return false;
	}


	private Consume updateAndCheckTypeParser(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
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
			accessModifiers.clear();
			state = State.FAILED;
		}
		return res ? Consume.ACCEPTED : Consume.REJECTED;
	}


	private Consume findingAccessModifiers(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		AccessModifier accessMod = BaseAccessModifierExtractor.readAccessModifier(keywordUtil, tokenNode);
		if(accessMod != null) {
			this.accessModifiers.add(accessMod);
			return Consume.ACCEPTED;
		}
		else {
			state = State.FINDING_RETURN_TYPE;
			val res2 = findingReturnType(tokenNode);
			if(res2 == Consume.REJECTED) {
				accessModifiers.clear();
				state = State.FAILED;
			}
			return res2;
		}
	}


	private Consume findingReturnType(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		val res = updateAndCheckTypeParser(tokenNode);
		// TODO required because type parser has to look ahead
		if(state == State.FINDING_NAME) {
			val res2 = findingName(tokenNode);
			if(res2.isAccept()) { return res2; }
		}
		return res;
	}


	private Consume findingName(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(AstFragType.isIdentifier(tokenNode.getData())) {
			methodName = tokenNode.getData().getText();
			state = State.FINDING_PARAMS;
			return Consume.ACCEPTED;
		}
		accessModifiers.clear();
		state = State.FAILED;
		return Consume.REJECTED;
	}


	private Consume findingParams(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(AstFragType.isBlock(tokenNode.getData(), "(")) {
			state = State.COMPLETE;
			val annotations = new ArrayList<>(annotationParser.getParserResult());
			annotationParser.recycle();

			val comments = new ArrayList<>(commentParser.getParserResult());
			commentParser.recycle();

			val params = BaseMethodParametersParser.extractParamsFromSignature(tokenNode);
			val accessMods = new ArrayList<>(accessModifiers);

			methods.add(new MethodSig.SimpleImpl(methodName, NameUtil.newFqName(parentBlock.getDeclaration().getFullName(), methodName), params, returnTypeSig, accessMods, annotations, comments));
			accessModifiers.clear();
			return Consume.ACCEPTED;
		}
		accessModifiers.clear();
		state = State.FAILED;
		return Consume.REJECTED;
	}


	@Override
	public List<MethodSig.SimpleImpl> getParserResult() {
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
		val copy = new BaseMethodExtractor(this.langName, this.keywordUtil, this.parentBlock, this.typeParser.copy(), this.annotationParser.copy(), this.commentParser.copy());
		return copy;
	}


	// package-private
	void reset() {
		this.methods.clear();
		this.accessModifiers.clear();
		this.typeParser = typeParser.recycle();
		this.annotationParser = annotationParser.recycle();
		this.state = State.INIT;
	}

}
