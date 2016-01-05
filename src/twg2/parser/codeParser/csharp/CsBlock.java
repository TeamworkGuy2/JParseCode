package twg2.parser.codeParser.csharp;

import twg2.parser.baseAst.CompoundBlock;
import dataUtils.EnumUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public enum CsBlock implements CompoundBlock {
	CLASS(false, false),
	ENUM(false, false),
	INTERFACE(false, true),
	NAMESPACE(false, false),
	STRUCT(false, false);


	final boolean isClass;
	final boolean isInterface;

	private CsBlock(boolean isClass, boolean isInterface) {
		this.isClass = isClass;
		this.isInterface = isInterface;
	}


	@Override
	public boolean isInterface() {
		return isInterface;
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
