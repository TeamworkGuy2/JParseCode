package twg2.parser.codeParser.java;

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

/**
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public enum JavaKeyword implements AccessModifier {
	// NOTE: these must be in alphabetical order for Inst array binary searches to work
	ABSTRACT("abstract", Flag.METHOD_MOD | Flag.CLASS_MOD),
	ASSERT("assert"),
	BOOLEAN("boolean", Flag.IS_TYPE),
	BREAK("break"),
	BYTE("byte", Flag.IS_TYPE),
	CASE("case"),
	CATCH("catch"),
	CHAR("char", Flag.IS_TYPE),
	CLASS("class", Flag.BLOCK_MOD),
	CONST("const"),
	CONTINUE("continue"),
	DEFAULT("default"),
	DO("do"),
	DOUBLE("double", Flag.IS_TYPE),
	ELSE("else"),
	ENUM("enum", Flag.BLOCK_MOD),
	EXTENDS("extends"),
	FINAL("final", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	FINALLY("finally"),
	FLOAT("float", Flag.IS_TYPE),
	FOR("for"),
	GOTO("goto"),
	IF("if"),
	IMPLEMENTS("implements"),
	IMPORT("import"),
	INSTANCEOF("instanceof"),
	INT("int", Flag.IS_TYPE),
	INTERFACE("interface", Flag.BLOCK_MOD),
	LONG("long", Flag.IS_TYPE),
	NATIVE("native", Flag.METHOD_MOD),
	NEW("new"),
	PACKAGE("package"),
	PRIVATE("private", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	PROTECTED("protected", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	PUBLIC("public", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	RETURN("return"),
	SHORT("short", Flag.IS_TYPE),
	STATIC("static", Flag.FIELD_MOD | Flag.METHOD_MOD | Flag.CLASS_MOD),
	STRICTFP("strictfp", Flag.METHOD_MOD | Flag.CLASS_MOD),
	SUPER("super"),
	SWITCH("switch"),
	SYNCHRONIZED("synchronized", Flag.METHOD_MOD),
	THIS("this"),
	THROW("throw"),
	THROWS("throws"),
	TRANSIENT("transient", Flag.FIELD_MOD),
	TRY("try"),
	VOID("void"),
	VOLATILE("volatile", Flag.FIELD_MOD),
	WHILE("while");


	public static final Inst check = new JavaKeyword.Inst();

	public final String srcName;
	public final boolean isType;
	public final boolean isClassModifier;
	public final boolean isFieldModifier;
	public final boolean isMethodModifier;
	public final boolean isBlockModifier;
	public final boolean isOperator;
	public final boolean isTypeLiteral;


	JavaKeyword(String name) {
		this.srcName = name;
		this.isType = false;
		this.isClassModifier = false;
		this.isFieldModifier = false;
		this.isMethodModifier = false;
		this.isBlockModifier = false;
		this.isOperator = false;
		this.isTypeLiteral = false;
	}


	JavaKeyword(String name, int typeFlags) {
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




	@Accessors(fluent = true)
	public static class Inst implements KeywordUtil<JavaKeyword> {
		public final String[] keywords;
		private final JavaKeyword[] values;
		private final String[] primitives;
		@Getter private final CodeFragmentEnumSubSet<JavaKeyword> types;
		@Getter private final CodeFragmentEnumSubSet<JavaKeyword> classModifiers;
		@Getter private final CodeFragmentEnumSubSet<JavaKeyword> fieldModifiers;
		@Getter private final CodeFragmentEnumSubSet<JavaKeyword> methodModifiers;
		@Getter private final CodeFragmentEnumSubSet<JavaKeyword> blockModifiers;
		@Getter private final CodeFragmentEnumSubSet<JavaKeyword> operators;
		@Getter private final CodeFragmentEnumSubSet<JavaKeyword> typeLiterals;


		{
			this.values = JavaKeyword.values();
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

			// from: http://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.2
			primitives = new String[] { BOOLEAN.srcName, CHAR.srcName, BYTE.srcName, SHORT.srcName, INT.srcName, LONG.srcName, FLOAT.srcName, DOUBLE.srcName, LONG.srcName };
			Arrays.sort(primitives);
		}


		@Override
		public JavaKeyword toKeyword(String str) {
			JavaKeyword resType = tryToKeyword(str);
			if(resType == null) {
				throw new IllegalArgumentException("'" + str + "' is not a valid Java keyword");
			}
			return resType;
		}


		@Override
		public JavaKeyword tryToKeyword(String str) {
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
	 * @since 2016-2-18
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
