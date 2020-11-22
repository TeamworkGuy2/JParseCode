package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.Operator;
import twg2.parser.codeParser.OperatorUtil;
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
		FINDING_TYPE_PARAMS,
		FINDING_RETURN_TYPE,
		FINDING_NAME,
		FINDING_PARAMS,
		COMPLETE,
		FAILED;
	}


	KeywordUtil<? extends Keyword> keywordUtil;
	OperatorUtil<? extends Operator> operatorUtil;
	AstParser<List<AnnotationSig>> annotationParser;
	AstParser<List<String>> commentParser;
	AstParser<TypeSig.TypeSigSimple> typeParser;
	List<TypeSig.TypeSigSimple> typeParameters = new ArrayList<>();
	List<Keyword> accessModifiers = new ArrayList<>();
	String methodName;
	TypeSig.TypeSigSimple returnTypeSig;
	List<MethodSigSimple> methods = new ArrayList<>();


	/**
	 * @param parentBlock
	 * @param annotationParser this annotation parser should be being run external from this instance.  When this instance finds a method signature,
	 * the annotation parser should already contain results (i.e. {@link AstParser#getParserResult()}) for the method's annotations
	 */
	public MethodExtractor(String langName, KeywordUtil<? extends Keyword> keywordUtil, OperatorUtil<? extends Operator> operatorUtil, BlockAst<? extends BlockType> parentBlock,
			AstParser<TypeSig.TypeSigSimple> typeParser, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		super(langName, "method signature", parentBlock, State.COMPLETE, State.FAILED);
		this.keywordUtil = keywordUtil;
		this.operatorUtil = operatorUtil;
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
			if(AstFragType.isBlock(tokenNode.getData(), '<')) {
				state = State.FINDING_TYPE_PARAMS;
				res = findingTypeParams(tokenNode);
				if(res.isAccept()) { return true; }
			}
			if(TypeExtractor.isPossiblyType(keywordUtil, tokenNode, true)) {
				state = State.FINDING_RETURN_TYPE;
				res = updateReturnTypeParser(tokenNode);
				if(res.isAccept()) { return true; }
			}
		}
		else if(state == State.FINDING_ACCESS_MODIFIERS) {
			res = findingAccessModifiers(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_TYPE_PARAMS) {
			res = findingTypeParams(tokenNode);
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


	private Consume updateReturnTypeParser(SimpleTree<CodeToken> tokenNode) {
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
			typeParameters.clear();
			accessModifiers.clear();
			state = State.FAILED;
		}
		return res ? Consume.ACCEPTED : Consume.REJECTED;
	}


	private Consume findingAccessModifiers(SimpleTree<CodeToken> tokenNode) {
		var accessMod = AccessModifierExtractor.parseAccessModifier(keywordUtil, tokenNode);
		if(accessMod != null) {
			this.accessModifiers.add(accessMod);
			return Consume.ACCEPTED;
		}
		else {
			state = State.FINDING_TYPE_PARAMS;
			var res2 = findingTypeParams(tokenNode);
			if(res2 == Consume.REJECTED) {
				accessModifiers.clear();
				state = State.FAILED;
			}
			return res2;
		}
	}


	private Consume findingTypeParams(SimpleTree<CodeToken> tokenNode) {
		if(AstFragType.isBlock(tokenNode.getData(), '<')) {
			var genericTypes = TypeExtractor.extractGenericTypes(tokenNode.getData().getText(), keywordUtil);
			typeParameters.addAll(genericTypes.getParams());
			return Consume.ACCEPTED;
		}
		else {
			state = State.FINDING_RETURN_TYPE;
			var res2 = findingReturnType(tokenNode);
			if(res2 == Consume.REJECTED) {
				typeParameters.clear();
				accessModifiers.clear();
				state = State.FAILED;
			}
			return res2;
		}
	}


	private Consume findingReturnType(SimpleTree<CodeToken> tokenNode) {
		var res = updateReturnTypeParser(tokenNode);
		// required because type parser has to look ahead
		if(state == State.FINDING_NAME) {
			var res2 = findingName(tokenNode);
			if(res2.isAccept()) { return res2; }
		}
		return res;
	}


	private Consume findingName(SimpleTree<CodeToken> tokenNode) {
		if(AstFragType.isIdentifier(tokenNode.getData())) {
			methodName = tokenNode.getData().getText();
			if(methodName.endsWith(">")) {
				var genericTypes = TypeExtractor.extractGenericTypes(methodName, keywordUtil);
				methodName = genericTypes.getTypeName();
				typeParameters.addAll(genericTypes.getParams());
			}
			state = State.FINDING_PARAMS;
			return Consume.ACCEPTED;
		}
		typeParameters.clear();
		accessModifiers.clear();
		state = State.FAILED;
		return Consume.REJECTED;
	}


	private Consume findingParams(SimpleTree<CodeToken> tokenNode) {
		if(AstFragType.isBlock(tokenNode.getData(), '(')) {
			state = State.COMPLETE;
			var annotations = new ArrayList<>(annotationParser.getParserResult());
			annotationParser.recycle();

			var comments = new ArrayList<>(commentParser.getParserResult());
			commentParser.recycle();

			var params = MethodParametersParser.extractParamsFromSignature(keywordUtil, operatorUtil, annotationParser, tokenNode);
			var typeParams = new ArrayList<>(typeParameters);
			var accessMods = new ArrayList<>(accessModifiers);
			annotationParser.recycle();

			methods.add(new MethodSigSimple(methodName, NameUtil.newFqName(parentBlock.declaration.getFullName(), methodName), params, returnTypeSig, accessMods, typeParams, annotations, comments));
			typeParameters.clear();
			accessModifiers.clear();
			return Consume.ACCEPTED;
		}
		typeParameters.clear();
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
		return new MethodExtractor(this.langName, this.keywordUtil, this.operatorUtil, this.parentBlock, this.typeParser.copy(), this.annotationParser.copy(), this.commentParser.copy());
	}


	// package-private
	void reset() {
		this.methods.clear();
		this.typeParameters.clear();
		this.accessModifiers.clear();
		this.typeParser = typeParser.recycle();
		this.annotationParser = annotationParser.recycle();
		this.state = State.INIT;
	}

}
