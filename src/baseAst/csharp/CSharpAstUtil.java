package baseAst.csharp;

import codeParser.CodeLanguage;
import codeParser.CodeLanguageOptions;
import baseAst.AccessModifierParser;
import baseAst.AccessModifierEnum;
import baseAst.LanguageAstUtil;
import dataUtils.EnumUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public class CSharpAstUtil implements AccessModifierParser<AccessModifierEnum, CSharpBlock>, LanguageAstUtil {


	@Override
	public CodeLanguage getLanguage() {
		return CodeLanguageOptions.C_SHARP;
	}


	@Override
	public CSharpAstUtil getAccessModifierParser() {
		return this;
	}


	@Override
	public AccessModifierEnum defaultAccessModifier(String src, CSharpBlock currentBlock, CSharpBlock parentBlock) {
		AccessModifierEnum access = tryFromLanguageSrc(src);
		return defaultAccessModifier(access, currentBlock, parentBlock);
	}


	@Override
	public final AccessModifierEnum defaultAccessModifier(AccessModifierEnum access, CSharpBlock currentBlock, CSharpBlock parentBlock) {
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
					throw EnumUtil.unknownValue(parentBlock, CSharpBlock.class);
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
					throw EnumUtil.unknownValue(parentBlock, CSharpBlock.class);
				}
			case NAMESPACE: // NAMESPACE default true
				return AccessModifierEnum.PUBLIC;
			default:
				throw EnumUtil.unknownValue(currentBlock, CSharpBlock.class);
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

}
