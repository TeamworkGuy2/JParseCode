package twg2.parser.codeParser.csharp;

import java.util.Arrays;

import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;
import twg2.arrays.ArrayUtil;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.tools.CodeFragmentEnumSubSet;
import twg2.parser.codeParser.tools.EnumSplitter;
import twg2.parser.fragment.CodeFragmentType;

public enum CsKeyword implements AccessModifier {
	// NOTE: these must be in alphabetical order for Inst array binary searches to work
	// TODO ASYNC("async", CsKeyword.METHOD_MOD),
	ABSTRACT("abstract", Flag.METHOD_MOD | Flag.CLASS_MOD),
	AS("as", Flag.OPERATOR_MOD),
	BASE("base"),
	BOOL("bool", Flag.IS_TYPE),
	BREAK("break"),
	BYTE("byte", Flag.IS_TYPE),
	CASE("case"),
	CATCH("catch"),
	CHAR("char", Flag.IS_TYPE),
	CHECKED("checked"),
	CLASS("class", Flag.BLOCK_MOD),
	CONST("const"),
	CONTINUE("continue"),
	DECIMAL("decimal", Flag.IS_TYPE),
	DEFAULT("default"),
	DELEGATE("delegate"),
	DO("do"),
	DOUBLE("double", Flag.IS_TYPE),
	ELSE("else"),
	ENUM("enum", Flag.BLOCK_MOD),
	EVENT("event"),
	EXPLICIT("explicit"),
	EXTERN("extern", Flag.METHOD_MOD),
	FALSE("false", Flag.TYPE_LITERAL),
	FINALLY("finally"),
	FIXED("fixed"),
	FLOAT("float", Flag.IS_TYPE),
	FOR("for"),
	FOREACH("foreach"),
	GOTO("goto"),
	IF("if"),
	IMPLICIT("implicit"),
	IN("in"),
	INT("int", Flag.IS_TYPE),
	INTERFACE("interface", Flag.BLOCK_MOD),
	INTERNAL("internal", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	IS("is", Flag.OPERATOR_MOD),
	LOCK("lock"),
	LONG("long", Flag.IS_TYPE),
	NAMESPACE("namespace", Flag.BLOCK_MOD),
	NEW("new", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	NULL("null", Flag.TYPE_LITERAL),
	OBJECT("object", Flag.IS_TYPE),
	OPERATOR("operator"),
	OUT("out"),
	OVERRIDE("override", Flag.METHOD_MOD),
	PARAMS("params"),
	PRIVATE("private", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	PROTECTED("protected", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	PUBLIC("public", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	READONLY("readonly", Flag.FIELD_MOD),
	REF("ref"),
	RETURN("return"),
	SBYTE("sbyte", Flag.IS_TYPE),
	SEALED("sealed", Flag.METHOD_MOD | Flag.CLASS_MOD),
	SHORT("short", Flag.IS_TYPE),
	SIZEOF("sizeof"),
	STACKALLOC("stackalloc"),
	STATIC("static", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	STRING("string", Flag.IS_TYPE),
	STRUCT("struct"),
	SWITCH("switch"),
	THIS("this"),
	THROW("throw"),
	TRUE("true", Flag.TYPE_LITERAL),
	TRY("try"),
	TYPEOF("typeof"),
	UINT("uint", Flag.IS_TYPE),
	ULONG("ulong", Flag.IS_TYPE),
	UNCHECKED("unchecked"),
	UNSAFE("unsafe"),
	USHORT("ushort", Flag.IS_TYPE),
	USING("using"),
	VIRTUAL("virtual", Flag.METHOD_MOD),
	VOID("void"),
	VOLATILE("volatile", Flag.FIELD_MOD),
	WHILE("while");


	public static final CsKeyword.Inst check = new CsKeyword.Inst();

	public final String srcName;
	public final boolean isType;
	public final boolean isClassModifier;
	public final boolean isFieldModifier;
	public final boolean isMethodModifier;
	public final boolean isBlockModifier;
	public final boolean isOperator;
	public final boolean isTypeLiteral;


	CsKeyword(String name) {
		this.srcName = name;
		this.isType = false;
		this.isClassModifier = false;
		this.isFieldModifier = false;
		this.isMethodModifier = false;
		this.isBlockModifier = false;
		this.isOperator = false;
		this.isTypeLiteral = false;
	}


	CsKeyword(String name, int typeFlags) {
		this.srcName = name;
		this.isType = (typeFlags & Flag.IS_TYPE) == Flag.IS_TYPE;
		this.isClassModifier = (typeFlags & Flag.CLASS_MOD) == Flag.CLASS_MOD;
		this.isFieldModifier = (typeFlags & Flag.FIELD_MOD) == Flag.FIELD_MOD;
		this.isMethodModifier = (typeFlags & Flag.METHOD_MOD) == Flag.METHOD_MOD;
		this.isBlockModifier = (typeFlags & Flag.BLOCK_MOD) == Flag.BLOCK_MOD;
		this.isOperator = (typeFlags & Flag.OPERATOR_MOD) == Flag.OPERATOR_MOD;
		this.isTypeLiteral = (typeFlags & Flag.TYPE_LITERAL) == Flag.TYPE_LITERAL;
	}


	@Override
	public String toSrc() {
		return srcName;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-14
	 */
	@Accessors(fluent = true)
	public static class Inst implements KeywordUtil<CsKeyword> {
		public final String[] keywords;
		private final CsKeyword[] values;
		private final String[] primitives;
		@Getter private final CodeFragmentEnumSubSet<CsKeyword> types;
		@Getter private final CodeFragmentEnumSubSet<CsKeyword> classModifiers;
		@Getter private final CodeFragmentEnumSubSet<CsKeyword> fieldModifiers;
		@Getter private final CodeFragmentEnumSubSet<CsKeyword> methodModifiers;
		@Getter private final CodeFragmentEnumSubSet<CsKeyword> blockModifiers;
		@Getter private final CodeFragmentEnumSubSet<CsKeyword> operators;
		@Getter private final CodeFragmentEnumSubSet<CsKeyword> typeLiterals;


		{
			this.values = CsKeyword.values();
			val enumData = EnumSplitter.split(this.values, (e) -> e.srcName,
				(e) -> e.isType,
				(e) -> e.isClassModifier,
				(e) -> e.isFieldModifier,
				(e) -> e.isMethodModifier,
				(e) -> e.isBlockModifier,
				(e) -> e.isOperator,
				(e) -> e.isTypeLiteral
			);
			this.keywords = enumData.getKey();

			int i = 0;
			val enumSets = ArrayUtil.map(enumData.getValue(), CodeFragmentEnumSubSet.class, (es) -> new CodeFragmentEnumSubSet<>(CodeFragmentType.KEYWORD, es));
			types = enumSets[i++];
			classModifiers = enumSets[i++];
			fieldModifiers = enumSets[i++];
			methodModifiers = enumSets[i++];
			blockModifiers = enumSets[i++];
			operators = enumSets[i++];
			typeLiterals = enumSets[i++];

			// from: https://msdn.microsoft.com/en-us/library/system.type.isprimitive%28v=vs.110%29.aspx
			// IntPtr and UIntPtr aren't keywords, so they are string literals here
			primitives = new String[] { BOOL.srcName, BYTE.srcName, SBYTE.srcName, SHORT.srcName, USHORT.srcName, INT.srcName, UINT.srcName, LONG.srcName, ULONG.srcName,
					"IntPtr", "UIntPtr",
					CHAR.srcName, FLOAT.srcName, DOUBLE.srcName };
			Arrays.sort(primitives);
		}


		@Override
		public CsKeyword toKeyword(String str) {
			CsKeyword resType = tryToKeyword(str);
			if(resType == null) {
				throw new IllegalArgumentException("'" + str + "' is not a valid C# keyword");
			}
			return resType;
		}


		@Override
		public CsKeyword tryToKeyword(String str) {
			int idx = Arrays.binarySearch(keywords, str);
			return idx > -1 ? values[idx] : null;
		}


		@Override
		public boolean isKeyword(String str) {
			return Arrays.binarySearch(keywords, str) > -1;
		}


		@Override
		public boolean isPrimitive(String str) {
			return Arrays.binarySearch(primitives, str) > -1;
		}


		@Override
		public boolean isType(String str) {
			return types.find(str) != null;
		}


		/**
		 * @param str
		 * @return true for any string which is a type keyword (i.e. {@link #isType(String)}) or any non-keyword strings,
		 * returns false for any other keywords 
		 */
		@Override
		public boolean isDataTypeKeyword(String str) {
			return types.find(str) != null;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-2-20
	 */
	static class Flag {
		static final int IS_TYPE = 1;
		static final int CLASS_MOD = 2;
		static final int FIELD_MOD = 4;
		static final int METHOD_MOD = 8;
		static final int BLOCK_MOD = 16;
		static final int OPERATOR_MOD = 32;
		static final int TYPE_LITERAL = 64;
	}

}
