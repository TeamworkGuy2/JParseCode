package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.Operator;
import twg2.parser.codeParser.OperatorUtil;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.AstTypeChecker;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.stateMachine.AstMemberInClassParserReusable;
import twg2.parser.stateMachine.AstParser;
import twg2.parser.stateMachine.Consume;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class FieldExtractor extends AstMemberInClassParserReusable<FieldExtractor.State, List<FieldDef>> {

	static enum State {
		INIT,
		FINDING_ACCESS_MODIFIERS,
		FINDING_DATA_TYPE,
		FINDING_NAME,
		FINDING_GET_SET, // only applies to C#
		FINDING_INITIALIZER_ASSIGNMENT,
		FOUND_INITIALIZER_ASSIGNMENT,
		COMPLETE,
		FAILED;
	}


	KeywordUtil<? extends Keyword> keywordUtil;
	OperatorUtil<? extends Operator> operatorUtil;
	AstParser<List<AnnotationSig>> annotationParser;
	AstParser<List<String>> commentParser;
	AstParser<TypeSig.TypeSigSimple> typeParser;
	List<Keyword> accessModifiers = new ArrayList<>();
	List<FieldDef> fields = new ArrayList<>();
	TypeSig.TypeSigSimple fieldTypeSig;
	String fieldName;
	SimpleTree<CodeToken> property;
	List<SimpleTree<CodeToken>> initializer;
	AstTypeChecker<?> typeChecker;


	public FieldExtractor(String langName, KeywordUtil<? extends Keyword> keywordUtil, OperatorUtil<? extends Operator> operatorUtil, BlockAst<? extends BlockType> parentBlock,
			AstParser<TypeSig.TypeSigSimple> typeParser, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser, AstTypeChecker<?> typeChecker) {
		super(langName, "field", parentBlock, State.COMPLETE, State.FAILED);
		this.keywordUtil = keywordUtil;
		this.operatorUtil = operatorUtil;
		this.typeParser = typeParser;
		this.annotationParser = annotationParser;
		this.commentParser = commentParser;
		this.typeChecker = typeChecker;
		this.state = State.INIT;
	}


	@Override
	public boolean isComplete() {
		return this.state == State.FINDING_GET_SET || this.state == State.FINDING_INITIALIZER_ASSIGNMENT || this.state == State.FOUND_INITIALIZER_ASSIGNMENT || this.state == State.COMPLETE;
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}
		Consume res = null;

		if(state == State.INIT) {
			res = findingFieldInitial(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_ACCESS_MODIFIERS) {
			res = findingAccessModifiers(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_DATA_TYPE) {
			res = findingDataType(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_NAME) {
			res = findingName(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_GET_SET) {
			res = findingGetSet(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_INITIALIZER_ASSIGNMENT) {
			res = findingInitializerAssignment(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FOUND_INITIALIZER_ASSIGNMENT) {
			res = findingInitializer(tokenNode);
			if(res.isAccept()) { return true; }
		}
		return false;
	}


	@Override
	public void blockComplete() {
		if(isComplete() && this.state != State.COMPLETE) {
			// if a valid field has been parsed but not yet added, finish adding it when the block ends
			// (i.e. if the required parts of the field appeared at the end of a block and this parser hasn't finished the field in case there are more optional tokens)
			finishCreateField(null);
		}
	}


	private Consume findingFieldInitial(SimpleTree<CodeToken> tokenNode) {
		if(keywordUtil.fieldModifiers().is(tokenNode.getData())) {
			state = State.FINDING_ACCESS_MODIFIERS;
			return findingAccessModifiers(tokenNode);
		}
		if(TypeExtractor.isPossiblyType(keywordUtil, tokenNode, false)) {
			state = State.FINDING_DATA_TYPE;
			return updateAndCheckTypeParser(tokenNode);
		}
		return Consume.REJECTED;
	}


	private Consume findingAccessModifiers(SimpleTree<CodeToken> tokenNode) {
		Keyword accessMod = AccessModifierExtractor.parseAccessModifier(keywordUtil, tokenNode);
		if(accessMod != null) {
			this.accessModifiers.add(accessMod);
			return Consume.ACCEPTED;
		}
		else {
			state = State.FINDING_DATA_TYPE;
			var res2 = findingDataType(tokenNode);
			if(res2 == Consume.REJECTED) {
				accessModifiers.clear();
				if(initializer != null) initializer.clear();
				state = State.FAILED;
			}
			return res2;
		}
	}


	private Consume findingDataType(SimpleTree<CodeToken> tokenNode) {
		var res = updateAndCheckTypeParser(tokenNode);
		// TODO because the type parser has to look ahead for now, but may not consume the look ahead token while also completing based on a look ahead
		if(res == Consume.REJECTED && state == State.FINDING_NAME) {
			var res2 = findingName(tokenNode);
			if(res2.isAccept()) { return res2; }
		}
		return res;
	}


	private Consume updateAndCheckTypeParser(SimpleTree<CodeToken> tokenNode) {
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
			accessModifiers.clear();
			if(initializer != null) initializer.clear();
			state = State.FAILED;
		}
		return res ? Consume.ACCEPTED : Consume.REJECTED;
	}


	private Consume findingName(SimpleTree<CodeToken> tokenNode) {
		if(AstFragType.isIdentifier(tokenNode.getData())) {
			fieldName = tokenNode.getData().getText();
			state = State.FINDING_GET_SET;
			return Consume.ACCEPTED;
		}
		accessModifiers.clear();
		if(initializer != null) initializer.clear();
		state = State.FAILED;
		return Consume.REJECTED;
	}


	private Consume findingGetSet(SimpleTree<CodeToken> tokenNode) {
		// if a block is found, the field is likely a C# Property (i.e. 'int prop { get; set; }')
		if(typeChecker.isFieldBlock(tokenNode)) {
			property = tokenNode;
			state = State.FINDING_INITIALIZER_ASSIGNMENT;
			return Consume.ACCEPTED;
		}
		// property get/set is optional, move to the next state
		else {
			state = State.FINDING_INITIALIZER_ASSIGNMENT;
			return findingInitializerAssignment(tokenNode);
		}
	}


	private Consume findingInitializerAssignment(SimpleTree<CodeToken> tokenNode) {
		// if '=' symbol is found, the field has an initial value (i.e. 'field = 2;')
		if(operatorUtil.assignmentOperators().is(tokenNode.getData())) {
			state = State.FOUND_INITIALIZER_ASSIGNMENT;
			return Consume.ACCEPTED;
		}
		// initial value assignment is optional, so if not found, assume the field is complete
		else {
			var res2 = finishCreateField(tokenNode);
			// the token was not used and might be the part of the next field (only applies to languages where a field can end without a separator ';', e.g. C# '{ get; set; }' block at the end of a property)
			acceptNext(tokenNode);
			return res2;
		}
	}


	private Consume findingInitializer(SimpleTree<CodeToken> tokenNode) {
		if(initializer == null) {
			initializer = new ArrayList<>();
		}
		// if '=' symbol is found, the field has an initial value (i.e. 'field = 2;')
		if(!AstFragType.isSeparator(tokenNode.getData(), ";")) {
			initializer.add(tokenNode);
			return Consume.ACCEPTED;
		}
		else {
			var res2 = finishCreateField(tokenNode);
			// the token was not used and might be the part of the next field (only applies to languages where a field can end without a separator ';', e.g. C# '{ get; set; }' block at the end of a property)
			acceptNext(tokenNode);
			return res2;
		}
	}


	private Consume finishCreateField(SimpleTree<CodeToken> tokenNode) {
		// don't want to accidentially consume the beginning of a method signature, so if a block (other than an annotation '[...]' block is next, then it might be a method so bail)
		if(tokenNode == null || tokenNode.getData().getTokenType() != CodeTokenType.BLOCK || AstFragType.isBlock(tokenNode.getData(), '[')) {
			state = State.COMPLETE;
			var annotations = new ArrayList<>(annotationParser.getParserResult());
			annotationParser.recycle();

			var comments = new ArrayList<>(commentParser.getParserResult());
			commentParser.recycle();

			var accessMods = new ArrayList<>(accessModifiers);

			var initializers = initializer != null ? new ArrayList<>(initializer) : null;

			fields.add(new FieldDef(fieldName, NameUtil.newFqName(parentBlock.declaration.getFullName(), fieldName), fieldTypeSig, accessMods, annotations, comments, initializers));
			accessModifiers.clear();
			if(initializer != null) initializer.clear();
			return Consume.ACCEPTED;
		}
		accessModifiers.clear();
		if(initializer != null) initializer.clear();
		state = State.FAILED;
		return Consume.REJECTED;
	}


	@Override
	public List<FieldDef> getParserResult() {
		return fields;
	}


	@Override
	public FieldExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public FieldExtractor copy() {
		return new FieldExtractor(this.langName, this.keywordUtil, this.operatorUtil, this.parentBlock, this.typeParser.copy(), this.annotationParser.copy(), this.commentParser.copy(), this.typeChecker);
	}


	// package-private
	void reset() {
		this.fields.clear();
		this.accessModifiers.clear();
		if(this.initializer != null) this.initializer.clear();
		this.typeParser = typeParser.recycle();
		this.annotationParser = annotationParser.recycle();
	}

}
