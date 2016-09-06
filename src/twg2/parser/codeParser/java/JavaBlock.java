package twg2.parser.codeParser.java;

import twg2.dataUtil.dataUtils.EnumError;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.BlockUtil;

/**
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public enum JavaBlock implements BlockType {
	CLASS(true),
	ENUM(true),
	INTERFACE(false);


	final boolean isClass;

	private JavaBlock(boolean isClass) {
		this.isClass = isClass;
	}


	@Override
	public boolean isEnum() {
		return this == ENUM;
	}


	@Override
	public boolean isInterface() {
		return this == INTERFACE;
	}


	public final boolean canContainImports() {
		return false;
	}


	@Override
	public final boolean canContainFields() {
		return this == CLASS || this == ENUM || this == INTERFACE;
	}


	@Override
	public final boolean canContainMethods() {
		return this == CLASS || this == ENUM || this == INTERFACE;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-09-03
	 */
	public static class JavaBlockUtil implements BlockUtil<JavaBlock, JavaKeyword> {

		@Override
		public final JavaBlock parseKeyword(JavaKeyword keyword) {
			JavaBlock blockType = tryParseKeyword(keyword);
			if(blockType == null) {
				throw new IllegalArgumentException("Java keyword '" + keyword + "' is not a valid compound block type");
			}
			return blockType;
		}


		@Override
		public final JavaBlock tryParseKeyword(JavaKeyword keyword) {
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

}
