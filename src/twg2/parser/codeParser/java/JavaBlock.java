package twg2.parser.codeParser.java;

import twg2.dataUtil.dataUtils.EnumError;
import twg2.parser.baseAst.CompoundBlock;

/**
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public enum JavaBlock implements CompoundBlock {
	CLASS(true, false),
	ENUM(true, false),
	INTERFACE(false, true);


	final boolean isClass;
	final boolean isInterface;

	private JavaBlock(boolean isClass, boolean isInterface) {
		this.isClass = isClass;
		this.isInterface = isInterface;
	}


	@Override
	public boolean isInterface() {
		return isInterface;
	}


	public final boolean canContainImports() {
		return false;
	}


	@Override
	public final boolean canContainFields() {
		return this == CLASS || this == INTERFACE;
	}


	@Override
	public final boolean canContainMethods() {
		return this == CLASS || this == INTERFACE;
	}


	public static final JavaBlock fromKeyword(JavaKeyword keyword) {
		JavaBlock blockType = tryFromKeyword(keyword);
		if(blockType == null) {
			throw new IllegalArgumentException("Java keyword '" + keyword + "' is not a valid compound block type");
		}
		return blockType;
	}


	public static final JavaBlock tryFromKeyword(JavaKeyword keyword) {
		switch(keyword) {
		case CLASS:
			return JavaBlock.CLASS;
		case INTERFACE:
			return JavaBlock.INTERFACE;
		case ENUM:
			return JavaBlock.ENUM;
		default:
			throw EnumError.unknownValue(keyword, JavaKeyword.class);
		}
	}

}
