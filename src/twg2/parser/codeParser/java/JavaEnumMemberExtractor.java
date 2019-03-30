package twg2.parser.codeParser.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.stateMachine.AstMemberInClassParserReusable;
import twg2.parser.stateMachine.AstParser;
import twg2.parser.stateMachine.Consume;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-09-04
 */
public class JavaEnumMemberExtractor extends AstMemberInClassParserReusable<JavaEnumMemberExtractor.State, List<FieldDef>> {

	static enum State {
		INIT,
		FOUND_NAME,
		FOUND_ARGUMENTS,
		EXPECTING_ANOTHER_MEMBER,
		COMPLETE,
		FAILED;
	}


	KeywordUtil<? extends Keyword> keywordUtil;
	AstParser<List<String>> commentParser;
	List<FieldDef> enumMembers = new ArrayList<>();
	TypeSig.TypeSigSimple enumType; // the enum's base type, can be any of 'validEnumTypes', default: int
	String nextMemberName;
	List<String> nextMemberComments = null;
	boolean lastNodeWasAnnotationStart = false;


	public JavaEnumMemberExtractor(KeywordUtil<? extends Keyword> keywordUtil, BlockAst<? extends BlockType> parentBlock, AstParser<List<String>> commentParser) {
		super(CodeLanguageOptions.C_SHARP.displayName(), "enum member", parentBlock, State.COMPLETE, State.FAILED);
		this.keywordUtil = keywordUtil;
		this.commentParser = commentParser;
		this.state = State.INIT;

		var enumSig = parentBlock.declaration;
		if(enumSig.getParams().size() > 0) {
			enumType = new TypeSig.TypeSigSimpleGeneric(enumSig.getSimpleName(), enumSig.getParams(), 0, false, false);
		}
		else {
			enumType = new TypeSig.TypeSigSimpleBase(enumSig.getSimpleName(), 0, false, false);
		}
	}


	/** Parse a iterator of AST nodes possibly representing an enum, example:<br>
	 * <pre><code>enum CoolTypes {
	 *   MY_ENUM(2) { ... },
	 *   OTHER_ENUM(3),
	 *   ...;
	 * }
	 * </code></pre>
	 * @param tokenNode
	 * @return parsed enum members
	 */
	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		if(state == State.COMPLETE) {
			return false;
		}
		CodeToken tokenData = tokenNode.getData();
		if(tokenData.getTokenType() == CodeTokenType.COMMENT) {
			return true;
		}
		// TODO handle annotations
		if(AstFragType.isSeparator(tokenData, "@")) {
			lastNodeWasAnnotationStart = true;
			return true;
		}
		if(lastNodeWasAnnotationStart && AstFragType.isIdentifier(tokenData)) {
			return true;
		}
		lastNodeWasAnnotationStart = false;

		if(state == State.FAILED) {
			state = State.INIT;
		}

		if(state == State.INIT || state == State.EXPECTING_ANOTHER_MEMBER) {
			if(AstFragType.isSeparator(tokenData, ";")) {
				state = State.COMPLETE;
				return true;
			}
			else if(!AstFragType.isIdentifier(tokenNode.getData())) {
				state = State.FAILED;
				return false;
			}
			foundName(tokenNode);
			return true;
		}
		else if(state == State.FOUND_NAME) {
			// if a '()' argument block is found (i.e. 'MY_ENUM(2), ...;')
			if(AstFragType.isBlock(tokenData, "(")) {
				updateLastAddedEnumMember(tokenNode);
				state = State.FOUND_ARGUMENTS;
				return true;
			}
			// previous enum member was only a name and the next node is the next member name (i.e. 'MY_ENUM, ...;')
			else if(AstFragType.isIdentifier(tokenData)) {
				foundName(tokenNode);
				return true;
			}
			else if(AstFragType.isSeparator(tokenData, ";")) {
				state = State.COMPLETE;
				return true;
			}
			// the end of the enum
			else {
				state = State.FAILED;
				return false;
			}
		}
		else if(state == State.FOUND_ARGUMENTS) {
			if(AstFragType.isBlock(tokenData, "{")) {
				updateLastAddedEnumMember(tokenNode);
				state = State.EXPECTING_ANOTHER_MEMBER;
				return true;
			}
			// previous enum member was a name with arguments and the next node is the next member name (i.e. 'MY_ENUM, ...;')
			else if(AstFragType.isIdentifier(tokenData)) {
				foundName(tokenNode);
				return true;
			}
			else if(AstFragType.isSeparator(tokenData, ";")) {
				state = State.COMPLETE;
				return true;
			}
			else {
				state = State.EXPECTING_ANOTHER_MEMBER;
				return true;
			}
		}
		//val separator = iter.next();
		// TODO for future when comma ',' parsing is added
		//if(separator == null || AstFragType.isSeparator(separator.getData(), ";")) {
		//	throw new RuntimeException("expected comma or semicolon between or after enum constants, found " + separator + " after '" + name + "'");
		//}
		return false;
	}


	@Override
	public List<FieldDef> getParserResult() {
		return enumMembers;
	}


	@Override
	public JavaEnumMemberExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public AstParser<List<FieldDef>> copy() {
		return new JavaEnumMemberExtractor(keywordUtil, parentBlock, commentParser.recycle());
	}


	// package-private
	void reset() {
		enumMembers.clear();
	}


	private Consume foundName(SimpleTree<CodeToken> tokenNode) {
		nextMemberName = tokenNode.getData().getText();
		// minimum viable enum
		addEnumMember(nextMemberName, null);
		state = State.FOUND_NAME;
		return Consume.ACCEPTED;
	}


	private void updateLastAddedEnumMember(SimpleTree<CodeToken> tokenNode) {
		// remove the minimum viable enum OR partially complete enum with args that was added when the previous identifier node OR argument block was found, this is going to be a full enum with arguments OR a body block
		var partialEnum = enumMembers.remove(enumMembers.size() - 1);
		nextMemberComments = partialEnum.getComments();
		addEnumMember(nextMemberName, tokenNode); // TODO if this is the enum body block, include the previous initalizer (the arguments (...)) as well
	}


	private void addEnumMember(String memberName, SimpleTree<CodeToken> tokenNode) {
		var comments = (nextMemberComments != null ? nextMemberComments : new ArrayList<>(commentParser.getParserResult()));
		var field = new FieldDef(memberName, NameUtil.newFqName(parentBlock.declaration.getFullName(), memberName), enumType,
				Arrays.asList(CsKeyword.PUBLIC), Collections.emptyList(), comments, tokenNode);
		nextMemberComments = null;
		commentParser.recycle();
		enumMembers.add(field);
	}

}
