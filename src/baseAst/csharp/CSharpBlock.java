package baseAst.csharp;

import baseAst.CompoundBlock;
import codeParser.csharp.CSharpKeyword;
import dataUtils.EnumUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public enum CSharpBlock implements CompoundBlock {
	CLASS,
	ENUM,
	INTERFACE,
	NAMESPACE,
	STRUCT;


	private CSharpBlock() {
	}


	@Override
	public final boolean canContainFields() {
		return this == CLASS || this == INTERFACE;
	}


	@Override
	public final boolean canContainMethods() {
		return this == CLASS || this == INTERFACE;
	}


	public static final CSharpBlock fromKeyword(CSharpKeyword keyword) {
		CSharpBlock blockType = tryFromKeyword(keyword);
		if(blockType == null) {
			throw new IllegalArgumentException("C# keyword '" + keyword + "' is not a valid compound block type");
		}
		return blockType;
	}


	public static final CSharpBlock tryFromKeyword(CSharpKeyword keyword) {
		switch(keyword) {
		case CLASS:
			return CSharpBlock.CLASS;
		case INTERFACE:
			return CSharpBlock.INTERFACE;
		case ENUM:
			return CSharpBlock.ENUM;
		case STRUCT:
			return CSharpBlock.STRUCT;
		case NAMESPACE:
			return CSharpBlock.NAMESPACE;
		default:
			throw EnumUtil.unknownValue(keyword, CSharpKeyword.class);
		}
	}

}
