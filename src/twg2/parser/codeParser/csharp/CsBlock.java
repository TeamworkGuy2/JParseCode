package twg2.parser.codeParser.csharp;

import twg2.dataUtil.dataUtils.EnumError;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.BlockUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public enum CsBlock implements BlockType {
	CLASS(true),
	ENUM(true),
	INTERFACE(false),
	NAMESPACE(false),
	STRUCT(false);


	final boolean isClass;

	private CsBlock(boolean isClass) {
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


	public boolean canContainImports() {
		return this == NAMESPACE;
	}


	@Override
	public boolean canContainFields() {
		return this == CLASS || this == ENUM || this == INTERFACE;
	}


	@Override
	public boolean canContainMethods() {
		return this == CLASS || this == ENUM || this == INTERFACE;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-09-03
	 */
	public static class CsBlockUtil implements BlockUtil<CsBlock, CsKeyword> {

		@Override
		public CsBlock tryToBlock(CsKeyword keyword) {
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
				throw EnumError.unknownValue(keyword, CsKeyword.class);
			}
		}

	}

}
