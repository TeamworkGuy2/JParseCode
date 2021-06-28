package twg2.parser.codeParser.java;

import java.util.List;

import twg2.dataUtil.dataUtils.EnumError;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.AccessModifierParser;
import twg2.parser.codeParser.AstUtil;
import twg2.parser.fragment.AstTypeChecker;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguage;
import twg2.parser.language.CodeLanguageOptions;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public class JavaAstUtil implements AccessModifierParser<AccessModifierEnum, JavaBlock>, AstTypeChecker<JavaKeyword>, AstUtil<JavaBlock, JavaKeyword> {

	@Override
	public CodeLanguage getLanguage() {
		return CodeLanguageOptions.JAVA;
	}


	@Override
	public JavaAstUtil getAccessModifierParser() {
		return this;
	}


	@Override
	public AstTypeChecker<JavaKeyword> getChecker() {
		return this;
	}


	@Override
	public AccessModifierEnum defaultAccessModifier(List<String> accessModifiers, JavaBlock currentBlock, JavaBlock parentBlock) {
		AccessModifierEnum access = tryParseFromSrc(accessModifiers);
		return defaultAccessModifier(access, currentBlock, parentBlock);
	}


	@Override
	public final AccessModifierEnum defaultAccessModifier(AccessModifierEnum access, JavaBlock currentBlock, JavaBlock parentBlock) {
		if(access != null) {
			return access;
		}

		if(parentBlock == null) {
			return AccessModifierEnum.NAMESPACE_LOCAL;
		}

		switch(currentBlock) {
			case CLASS:
				switch(parentBlock) {
				case CLASS: // class { class }
				case ENUM: // enum { class }
					return AccessModifierEnum.NAMESPACE_LOCAL;
				case INTERFACE: // interface { class }
					return AccessModifierEnum.PUBLIC;
				default:
					throw EnumError.unknownValue(parentBlock, JavaBlock.class);
				}
			case INTERFACE:
				switch(parentBlock) {
				case CLASS: // class { interface }
				case ENUM: // enum { interface }
					return AccessModifierEnum.NAMESPACE_LOCAL;
				case INTERFACE: // interface { interface }
					return AccessModifierEnum.PUBLIC;
				default:
					throw EnumError.unknownValue(parentBlock, JavaBlock.class);
				}
			case ENUM:
				switch(parentBlock) {
				case CLASS: // class { enum }
				case ENUM: // enum { enum }
					return AccessModifierEnum.NAMESPACE_LOCAL;
				case INTERFACE: // interface { enum }
					return AccessModifierEnum.PUBLIC;
				default:
					throw EnumError.unknownValue(parentBlock, JavaBlock.class);
				}
			default:
				throw EnumError.unknownValue(currentBlock, JavaBlock.class);
		}
	}


	// TODO how should we handle other class modifiers like 'abstract': https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.1.1
	@Override
	public final AccessModifierEnum tryParseFromSrc(List<String> accessModifiers) {
		if(accessModifiers == null) {
			return null;
		}
		if(accessModifiers.contains("public")) {
			return AccessModifierEnum.PUBLIC;
		}
		if(accessModifiers.contains("private")) {
			return AccessModifierEnum.PRIVATE;
		}
		if(accessModifiers.contains("protected")) {
			return AccessModifierEnum.NAMESPACE_OR_INHERITANCE_LOCAL;
		}
		return null;
	}


	@Override
	public boolean isKeyword(CodeToken node, JavaKeyword keyword1) {
		return node != null && (node.getTokenType() == CodeTokenType.KEYWORD && keyword1.toSrc().equals(node.getText()));
	}


	@Override
	public boolean isKeyword(CodeToken node, JavaKeyword keyword1, JavaKeyword keyword2) {
		return node != null && (node.getTokenType() == CodeTokenType.KEYWORD && (keyword1.toSrc().equals(node.getText()) || keyword2.toSrc().equals(node.getText())));
	}


	@Override
	public boolean isKeyword(CodeToken node, JavaKeyword keyword1, JavaKeyword keyword2, JavaKeyword keyword3) {
		return node != null && (node.getTokenType() == CodeTokenType.KEYWORD && (keyword1.toSrc().equals(node.getText()) || keyword2.toSrc().equals(node.getText()) || keyword3.toSrc().equals(node.getText())));
	}


	/** Java does not have a field block
	 */
	@Override
	public boolean isFieldBlock(SimpleTree<CodeToken> tokenNode) {
		return false;
	}

}
