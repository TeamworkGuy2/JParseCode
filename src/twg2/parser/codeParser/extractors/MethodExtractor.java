package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.stateMachine.AstMemberInClassParserReusable;
import twg2.parser.stateMachine.AstParser;
import twg2.parser.stateMachine.Consume;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class MethodExtractor extends AstMemberInClassParserReusable<MethodExtractor.State, List<MethodSigSimple>> {

	static enum State {
		INIT,
		FINDING_ACCESS_MODIFIERS,
		FINDING_RETURN_TYPE,
		FINDING_NAME,
		FINDING_PARAMS,
		COMPLETE,
		FAILED;
	}


	KeywordUtil<? extends AccessModifier> keywordUtil;
	AstParser<List<AnnotationSig>> annotationParser;
	AstParser<List<String>> commentParser;
	AstParser<TypeSig.TypeSigSimple> typeParser;
	List<AccessModifier> accessModifiers = new ArrayList<>();
	String methodName;
	TypeSig.TypeSigSimple returnTypeSig;
	List<MethodSigSimple> methods = new ArrayList<>();


	/**
	 * @param parentBlock
	 * @param annotationParser this annotation parser should be being run external from this instance.  When this instance finds a method signature,
	 * the annotation parser should already contain results (i.e. {@link AstParser#getParserResult()}) for the method's annotations
	 */
	public MethodExtractor(String langName, KeywordUtil<? extends AccessModifier> keywordUtil, BlockAst<? extends BlockType> parentBlock,
			AstParser<TypeSig.TypeSigSimple> typeParser, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		super(langName, "method signature", parentBlock, State.COMPLETE, State.FAILED);
		this.keywordUtil = keywordUtil;
		this.methods = new ArrayList<>();
		this.typeParser = typeParser;
		this.annotationParser = annotationParser;
		this.commentParser = commentParser;
		this.state = State.INIT;
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}
		Consume res = null;

		if(state == State.INIT) {
			if(keywordUtil.methodModifiers().is(tokenNode.getData())) {
				state = State.FINDING_ACCESS_MODIFIERS;
				res = findingAccessModifiers(tokenNode);
				if(res.isAccept()) { return true; }
			}
			if(DataTypeExtractor.isPossiblyType(keywordUtil, tokenNode, true)) {
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


	private Consume updateAndCheckTypeParser(SimpleTree<CodeToken> tokenNode) {
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


	private Consume findingAccessModifiers(SimpleTree<CodeToken> tokenNode) {
		val accessMod = AccessModifierExtractor.parseAccessModifier(keywordUtil, tokenNode);
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


	private Consume findingReturnType(SimpleTree<CodeToken> tokenNode) {
		val res = updateAndCheckTypeParser(tokenNode);
		// TODO required because type parser has to look ahead
		if(state == State.FINDING_NAME) {
			val res2 = findingName(tokenNode);
			if(res2.isAccept()) { return res2; }
		}
		return res;
	}


	private Consume findingName(SimpleTree<CodeToken> tokenNode) {
		if(AstFragType.isIdentifier(tokenNode.getData())) {
			methodName = tokenNode.getData().getText();
			state = State.FINDING_PARAMS;
			return Consume.ACCEPTED;
		}
		accessModifiers.clear();
		state = State.FAILED;
		return Consume.REJECTED;
	}


	private Consume findingParams(SimpleTree<CodeToken> tokenNode) {
		if(AstFragType.isBlock(tokenNode.getData(), "(")) {
			state = State.COMPLETE;
			val annotations = new ArrayList<>(annotationParser.getParserResult());
			annotationParser.recycle();

			val comments = new ArrayList<>(commentParser.getParserResult());
			commentParser.recycle();

			val params = MethodParametersParser.extractParamsFromSignature(keywordUtil, tokenNode);
			val accessMods = new ArrayList<>(accessModifiers);

			methods.add(new MethodSigSimple(methodName, NameUtil.newFqName(parentBlock.declaration.getFullName(), methodName), params, returnTypeSig, accessMods, annotations, comments));
			accessModifiers.clear();
			return Consume.ACCEPTED;
		}
		accessModifiers.clear();
		state = State.FAILED;
		return Consume.REJECTED;
	}


	@Override
	public List<MethodSigSimple> getParserResult() {
		return methods;
	}


	@Override
	public MethodExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public MethodExtractor copy() {
		val copy = new MethodExtractor(this.langName, this.keywordUtil, this.parentBlock, this.typeParser.copy(), this.annotationParser.copy(), this.commentParser.copy());
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
