package baseAst.csharp;

import baseAst.CompoundBlock;
import codeParser.csharp.CsKeyword;
import dataUtils.EnumUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public enum CsBlock implements CompoundBlock {
	CLASS,
	ENUM,
	INTERFACE,
	NAMESPACE,
	STRUCT;


	private CsBlock() {
	}


	public final boolean canContainImports() {
		return this == NAMESPACE;
	}


	@Override
	public final boolean canContainFields() {
		return this == CLASS || this == INTERFACE;
	}


	@Override
	public final boolean canContainMethods() {
		return this == CLASS || this == INTERFACE;
	}


	public static final CsBlock fromKeyword(CsKeyword keyword) {
		CsBlock blockType = tryFromKeyword(keyword);
		if(blockType == null) {
			throw new IllegalArgumentException("C# keyword '" + keyword + "' is not a valid compound block type");
		}
		return blockType;
	}


	public static final CsBlock tryFromKeyword(CsKeyword keyword) {
		switch(keyword) {
		case CLASS:
			return CsBlock.CLASS;
		case INTERFACE:
			return CsBlock.INTERFACE;
		case ENUM:
			return CsBlock.ENUM;
		case STRUCT:
			return CsBlock.STRUCT;
		case NAMESPACE:
			return CsBlock.NAMESPACE;
		default:
			throw EnumUtil.unknownValue(keyword, CsKeyword.class);
		}
	}

}
