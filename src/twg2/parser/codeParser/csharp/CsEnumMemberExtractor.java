package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.val;
import twg2.arrays.ArrayUtil;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.stateMachine.AstMemberInClassParserReusable;
import twg2.parser.stateMachine.AstParser;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-09-04
 */
public class CsEnumMemberExtractor extends AstMemberInClassParserReusable<CsEnumMemberExtractor.State, List<FieldDef>> {

	static enum State {
		INIT,
		FOUND_NAME,
		FOUND_ASSIGNMENT_SYMBOL,
		EXPECTING_ANOTHER_MEMBER,
		COMPLETE,
		FAILED;
	}


	// from: https://msdn.microsoft.com/en-us/library/sbbt4032.aspx
	private static final String[] validEnumTypes = new String[] { CsKeyword.BYTE.srcName, CsKeyword.SBYTE.srcName, CsKeyword.SHORT.srcName, CsKeyword.USHORT.srcName,
			CsKeyword.INT.srcName, CsKeyword.UINT.srcName, CsKeyword.LONG.srcName, CsKeyword.ULONG.srcName };

	KeywordUtil<? extends AccessModifier> keywordUtil;
	AstParser<List<String>> commentParser;
	List<FieldDef> enumMembers = new ArrayList<>();
	TypeSig.TypeSigSimpleBase enumType; // the enum's base type, can be any of 'validEnumTypes', default: int
	String nextMemberName;
	List<String> nextMemberComments = null;


	public CsEnumMemberExtractor(KeywordUtil<? extends AccessModifier> keywordUtil, BlockAst<? extends BlockType> parentBlock, AstParser<List<String>> commentParser) {
		super(CodeLanguageOptions.C_SHARP.displayName(), "enum member", parentBlock, State.COMPLETE, State.FAILED);
		this.keywordUtil = keywordUtil;
		this.commentParser = commentParser;
		this.state = State.INIT;

		// determine the enum's base type
		val enumExtends = parentBlock.getDeclaration().getExtendImplementSimpleNames();
		if(enumExtends == null || enumExtends.isEmpty()) {
			enumType = new TypeSig.TypeSigSimpleBase(CsKeyword.INT.toSrc(), 0, false, true);
		}
		else {
			if(enumExtends.size() > 1) {
				throw new RuntimeException("C# enums cannot extend/implement more than one class/interface, enum '" + NameUtil.joinFqName(parentBlock.getDeclaration().getFullName()) + "' extends " + enumExtends.get(0));
			}
			if(ArrayUtil.indexOf(validEnumTypes, enumExtends.get(0)) < 0) {
				throw new RuntimeException("C# enums must extend an integer based data type, enum '" + NameUtil.joinFqName(parentBlock.getDeclaration().getFullName()) + "' extends " + enumExtends.get(0));
			}
			enumType = new TypeSig.TypeSigSimpleBase(enumExtends.get(0), 0, false, true);
		}
	}


	/** Parse a iterator of AST nodes possibly representing an enum, example:<br>
	 * <pre><code>enum CoolTypes {
	 *   MY_ENUM = 2,
	 *   OTHER_ENUM = 3,
	 *   ...;
	 * }
	 * </code></pre>
	 * @param tokenNode
	 * @return parsed enum members
	 */
	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		CodeToken tokenData = tokenNode.getData();
		if(tokenData.getTokenType() == CodeTokenType.COMMENT) {
			return true;
		}
		if(AstFragType.isBlock(tokenData, "[")) {
			return true;
		}
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}

		if(state == State.INIT || state == State.EXPECTING_ANOTHER_MEMBER) {
			if(!AstFragType.isIdentifier(tokenNode.getData())) {
				state = State.FAILED;
				return false;
			}
			nextMemberName = tokenNode.getData().getText();
			// minimum viable enum
			addEnumMember(nextMemberName, null);
			state = State.FOUND_NAME;
			return true;
		}
		else if(state == State.FOUND_NAME) {
			// if a '=' symbol is found, the enum has a custom value (i.e. 'MY_ENUM = 2, ...;')
			if(AstFragType.isOperator(tokenData, CsOperator.ASSIGNMENT)) {
				// remove the minimum viable enum that was added when the previous identifier node was found, this is going to be a full enum with a value
				val minimumEnum = enumMembers.remove(enumMembers.size() - 1);
				nextMemberComments = minimumEnum.getComments();
				state = State.FOUND_ASSIGNMENT_SYMBOL;
				return true;
			}
			// previous enum member was only a name and the next node is the next member name (i.e. 'MY_ENUM, ...;')
			else if(AstFragType.isIdentifier(tokenData)) {
				nextMemberName = tokenNode.getData().getText();
				// minimum viable enum
				addEnumMember(nextMemberName, null);
				state = State.FOUND_NAME;
				return true;
			}
			// the end of the enum
			else {
				state = State.FAILED;
				return false;
			}
		}
		else if(state == State.FOUND_ASSIGNMENT_SYMBOL) {
			addEnumMember(nextMemberName, tokenNode);
			state = State.EXPECTING_ANOTHER_MEMBER;
			return true;
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
	public CsEnumMemberExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public AstParser<List<FieldDef>> copy() {
		val copy = new CsEnumMemberExtractor(keywordUtil, parentBlock, commentParser.copy());
		return copy;
	}


	// package-private
	void reset() {
		enumMembers.clear();
	}


	private void addEnumMember(String memberName, SimpleTree<CodeToken> tokenNode) {
		val comments = (nextMemberComments != null ? nextMemberComments : new ArrayList<>(commentParser.getParserResult()));
		val field = new FieldDef(memberName, NameUtil.newFqName(parentBlock.getDeclaration().getFullName(), memberName), enumType,
				Arrays.asList(CsKeyword.PUBLIC), Collections.<AnnotationSig>emptyList(), comments, tokenNode);
		nextMemberComments = null;
		commentParser.recycle();
		enumMembers.add(field);
	}

}
