package twg2.parser.baseAst.java;

import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.AccessModifierParser;
import twg2.parser.baseAst.AstTypeChecker;
import twg2.parser.baseAst.AstUtil;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaKeyword;
import twg2.parser.documentParser.DocumentFragmentText;
import dataUtils.EnumUtil;

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
	public AccessModifierEnum defaultAccessModifier(String src, JavaBlock currentBlock, JavaBlock parentBlock) {
		AccessModifierEnum access = tryFromLanguageSrc(src);
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
					throw EnumUtil.unknownValue(parentBlock, JavaBlock.class);
				}
			case INTERFACE:
				switch(parentBlock) {
				case CLASS: // class { interface }
				case ENUM: // enum { interface }
					return AccessModifierEnum.NAMESPACE_LOCAL;
				case INTERFACE: // interface { interface }
					return AccessModifierEnum.PUBLIC;
				default:
					throw EnumUtil.unknownValue(parentBlock, JavaBlock.class);
				}
			case ENUM:
				switch(parentBlock) {
				case CLASS: // class { enum }
				case ENUM: // enum { enum }
					return AccessModifierEnum.NAMESPACE_LOCAL;
				case INTERFACE: // interface { enum }
					return AccessModifierEnum.PUBLIC;
				default:
					throw EnumUtil.unknownValue(parentBlock, JavaBlock.class);
				}
			default:
				throw EnumUtil.unknownValue(currentBlock, JavaBlock.class);
		}
	}


	@Override
	public final AccessModifierEnum tryFromLanguageSrc(String src) {
		if("public".equals(src)) {
			return AccessModifierEnum.PUBLIC;
		}
		if("private".equals(src)) {
			return AccessModifierEnum.PRIVATE;
		}
		if("protected".equals(src)) {
			return AccessModifierEnum.NAMESPACE_OR_INHERITANCE_LOCAL;
		}
		if("".equals(src)) {
			return AccessModifierEnum.NAMESPACE_LOCAL;
		}
		return null;
	}


	@Override
	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, JavaKeyword keyword1) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD && keyword1.getSrcName().equals(node.getText()));
	}


	@Override
	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, JavaKeyword keyword1, JavaKeyword keyword2) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD && (keyword1.getSrcName().equals(node.getText()) || keyword2.getSrcName().equals(node.getText())));
	}


	@Override
	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, JavaKeyword keyword1, JavaKeyword keyword2, JavaKeyword keyword3) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD && (keyword1.getSrcName().equals(node.getText()) || keyword2.getSrcName().equals(node.getText()) || keyword3.getSrcName().equals(node.getText())));
	}

}
