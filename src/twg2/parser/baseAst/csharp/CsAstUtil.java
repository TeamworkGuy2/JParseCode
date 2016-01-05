package twg2.parser.baseAst.csharp;

import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.AccessModifierParser;
import twg2.parser.baseAst.AstTypeChecker;
import twg2.parser.baseAst.LanguageAstUtil;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.documentParser.DocumentFragmentText;
import dataUtils.EnumUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public class CsAstUtil implements AccessModifierParser<AccessModifierEnum, CsBlock>, AstTypeChecker<CsKeyword>, LanguageAstUtil {

	@Override
	public CodeLanguage getLanguage() {
		return CodeLanguageOptions.C_SHARP;
	}


	@Override
	public CsAstUtil getAccessModifierParser() {
		return this;
	}


	@Override
	public AstTypeChecker<CsKeyword> getChecker() {
		return this;
	}


	@Override
	public AccessModifierEnum defaultAccessModifier(String src, CsBlock currentBlock, CsBlock parentBlock) {
		AccessModifierEnum access = tryFromLanguageSrc(src);
		return defaultAccessModifier(access, currentBlock, parentBlock);
	}


	@Override
	public final AccessModifierEnum defaultAccessModifier(AccessModifierEnum access, CsBlock currentBlock, CsBlock parentBlock) {
		if(access != null) {
			return access;
		}

		if(parentBlock == null) {
			return AccessModifierEnum.PUBLIC;
		}

		switch(currentBlock) {
			case CLASS:
				switch(parentBlock) {
				case CLASS: // class { class }
					return AccessModifierEnum.PRIVATE;
				case INTERFACE: // interface { class }
					return AccessModifierEnum.PUBLIC;
				case NAMESPACE: // namespace { class }
					return AccessModifierEnum.NAMESPACE_LOCAL;
				default:
					throw EnumUtil.unknownValue(parentBlock, CsBlock.class);
				}
			case INTERFACE:
				switch(parentBlock) {
				case CLASS: // class { interface }
					return AccessModifierEnum.PRIVATE;
				case INTERFACE: // interface { interface }
					return AccessModifierEnum.PUBLIC;
				case NAMESPACE: // namespace { interface }
					return AccessModifierEnum.NAMESPACE_LOCAL;
				default:
					throw EnumUtil.unknownValue(parentBlock, CsBlock.class);
				}
			case NAMESPACE: // NAMESPACE default true
				return AccessModifierEnum.PUBLIC;
			default:
				throw EnumUtil.unknownValue(currentBlock, CsBlock.class);
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
			return AccessModifierEnum.INHERITANCE_LOCAL;
		}
		if("internal".equals(src)) {
			return AccessModifierEnum.NAMESPACE_LOCAL;
		}
		if("protected internal".equals(src)) {
			return AccessModifierEnum.NAMESPACE_OR_INHERITANCE_LOCAL;
		}
		return null;
	}


	@Override
	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, CsKeyword keyword1) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD && keyword1.getSrcName().equals(node.getText()));
	}


	@Override
	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, CsKeyword keyword1, CsKeyword keyword2) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD && (keyword1.getSrcName().equals(node.getText()) || keyword2.getSrcName().equals(node.getText())));
	}


	@Override
	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, CsKeyword keyword1, CsKeyword keyword2, CsKeyword keyword3) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD && (keyword1.getSrcName().equals(node.getText()) || keyword2.getSrcName().equals(node.getText()) || keyword3.getSrcName().equals(node.getText())));
	}


	@Override
	public boolean isBlockKeyword(DocumentFragmentText<CodeFragmentType> node) {
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD &&
				(CsKeyword.CLASS.getSrcName().equals(node.getText()) || CsKeyword.INTERFACE.getSrcName().equals(node.getText()) || CsKeyword.NAMESPACE.getSrcName().equals(node.getText())));
	}


	@Override
	public boolean isClassModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
		String text = null;
		return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD &&
				(CsKeyword.PUBLIC.getSrcName().equals((text = node.getText())) || CsKeyword.PROTECTED.getSrcName().equals(text) || CsKeyword.INTERNAL.getSrcName().equals(text) || CsKeyword.PRIVATE.getSrcName().equals(text) ||
				CsKeyword.ABSTRACT.getSrcName().equals(text) || CsKeyword.SEALED.getSrcName().equals(text) || CsKeyword.STATIC.getSrcName().equals(text)));
	}

}
