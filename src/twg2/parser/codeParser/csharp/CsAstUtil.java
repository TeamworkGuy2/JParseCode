package twg2.parser.codeParser.csharp;

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
 * @since 2015-12-6
 */
public class CsAstUtil implements AccessModifierParser<AccessModifierEnum, CsBlock>, AstTypeChecker<CsKeyword>, AstUtil<CsBlock, CsKeyword> {

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
		AccessModifierEnum access = tryParseFromSrc(src);
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
					throw EnumError.unknownValue(parentBlock, CsBlock.class);
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
					throw EnumError.unknownValue(parentBlock, CsBlock.class);
				}
			case NAMESPACE: // NAMESPACE default true
				return AccessModifierEnum.PUBLIC;
			default:
				throw EnumError.unknownValue(currentBlock, CsBlock.class);
		}
	}


	@Override
	public final AccessModifierEnum tryParseFromSrc(String src) {
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
	public boolean isKeyword(CodeToken node, CsKeyword keyword1) {
		return node != null && (node.getTokenType() == CodeTokenType.KEYWORD && keyword1.toSrc().equals(node.getText()));
	}


	@Override
	public boolean isKeyword(CodeToken node, CsKeyword keyword1, CsKeyword keyword2) {
		return node != null && (node.getTokenType() == CodeTokenType.KEYWORD && (keyword1.toSrc().equals(node.getText()) || keyword2.toSrc().equals(node.getText())));
	}


	@Override
	public boolean isKeyword(CodeToken node, CsKeyword keyword1, CsKeyword keyword2, CsKeyword keyword3) {
		return node != null && (node.getTokenType() == CodeTokenType.KEYWORD && (keyword1.toSrc().equals(node.getText()) || keyword2.toSrc().equals(node.getText()) || keyword3.toSrc().equals(node.getText())));
	}


	/** Supports property blocks in the format:
	 * <pre><code>{
	 *   [ [access-modifier] get ( ; | {...} ) ]
	 *   [ [access-modifier] set ( ; | {...} ) ]
	 *}</code></pre>
	 */
	@Override
	public boolean isFieldBlock(SimpleTree<CodeToken> block) {
		if(block == null) { return true; }
		var childs = block.getChildren();
		// properties must have at-least one indexer (i.e. 'get' or 'set')
		if(childs.size() == 0) { return false; }

		var keywords = this.getLanguage().getKeywordUtil();

		boolean prevWasGetOrSet = false;
		for(int i = 0, size = childs.size(); i < size; i++) {
			var child = childs.get(i);
			var nextChild = i < size - 1 ? childs.get(i + 1) : null;
			var frag = child.getData();
			var fragType = frag.getTokenType();
			if(fragType == CodeTokenType.COMMENT) {
				continue;
			}
			boolean isGetOrSet = isGetOrSet(frag);
			boolean isAccessMod = keywords.fieldModifiers().is(frag);
			if(isGetOrSet ||
					(prevWasGetOrSet && (fragType == CodeTokenType.BLOCK || fragType == CodeTokenType.SEPARATOR)) ||
					(isAccessMod && nextChild != null && isGetOrSet(nextChild.getData()))) {
				// allow
			}
			else {
				return false;
			}
			prevWasGetOrSet = isGetOrSet;
		}
		return true;
	}


	private static boolean isGetOrSet(CodeToken frag) {
		return frag.getTokenType() == CodeTokenType.IDENTIFIER && ("get".equals(frag.getText()) || "set".equals(frag.getText()));
	}

}
