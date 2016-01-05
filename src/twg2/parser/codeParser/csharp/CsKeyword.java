package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum CsKeyword {
	ABSTRACT("abstract"),
	AS("as"),
	BASE("base"),
	BOOL("bool", true),
	BREAK("break"),
	BYTE("byte", true),
	CASE("case"),
	CATCH("catch"),
	CHAR("char", true),
	CHECKED("checked"),
	CLASS("class"),
	CONST("const"),
	CONTINUE("continue"),
	DECIMAL("decimal", true),
	DEFAULT("default"),
	DELEGATE("delegate"),
	DO("do"),
	DOUBLE("double", true),
	ELSE("else"),
	ENUM("enum"),
	EVENT("event"),
	EXPLICIT("explicit"),
	EXTERN("extern"),
	FALSE("false"),
	FINALLY("finally"),
	FIXED("fixed"),
	FLOAT("float", true),
	FOR("for"),
	FOREACH("foreach"),
	GOTO("goto"),
	IF("if"),
	IMPLICIT("implicit"),
	IN("in"),
	INT("int", true),
	INTERFACE("interface"),
	INTERNAL("internal"),
	IS("is"),
	LOCK("lock"),
	LONG("long", true),
	NAMESPACE("namespace"),
	NEW("new"),
	NULL("null"),
	OBJECT("object", true),
	OPERATOR("operator"),
	OUT("out"),
	OVERRIDE("override"),
	PARAMS("params"),
	PRIVATE("private"),
	PROTECTED("protected"),
	PUBLIC("public"),
	READONLY("readonly"),
	REF("ref"),
	RETURN("return"),
	SBYTE("sbyte", true),
	SEALED("sealed"),
	SHORT("short", true),
	SIZEOF("sizeof"),
	STACKALLOC("stackalloc"),
	STATIC("static"),
	STRING("string", true),
	STRUCT("struct"),
	SWITCH("switch"),
	THIS("this"),
	THROW("throw"),
	TRUE("true"),
	TRY("try"),
	TYPEOF("typeof"),
	UINT("uint", true),
	ULONG("ulong", true),
	UNCHECKED("unchecked"),
	UNSAFE("unsafe"),
	USHORT("ushort", true),
	USING("using"),
	VIRTUAL("virtual"),
	VOID("void"),
	VOLATILE("volatile"),
	WHILE("while");


	public static final String[] keywords;
	private static final String[] primitives;
	private static final String[] types;


	static {
		List<String> typesList = new ArrayList<>();
		CsKeyword[] keywordEnums = CsKeyword.values();

		keywords = new String[keywordEnums.length];

		for(int i = 0, size = keywordEnums.length; i < size; i++) {
			keywords[i] = keywordEnums[i].srcName;
			if(keywordEnums[i].isType) {
				typesList.add(keywordEnums[i].srcName);
			}
		}

		Arrays.sort(keywords);

		types = typesList.toArray(new String[typesList.size()]);
		Arrays.sort(types);

		// from: https://msdn.microsoft.com/en-us/library/system.type.isprimitive%28v=vs.110%29.aspx
		// IntPtr and UIntPtr aren't keywords, so they are string literals here
		primitives = new String[] { BOOL.srcName, BYTE.srcName, SBYTE.srcName, SHORT.srcName, USHORT.srcName, INT.srcName, UINT.srcName, LONG.srcName, ULONG.srcName,
				"IntPtr", "UIntPtr",
				CHAR.srcName, FLOAT.srcName, DOUBLE.srcName };
		Arrays.sort(primitives);
	}


	public final String srcName;
	public final boolean isType;


	CsKeyword(String name) {
		this.srcName = name;
		this.isType = false;
	}


	CsKeyword(String name, boolean isType) {
		this.srcName = name;
		this.isType = isType;
	}


	public String getSrcName() {
		return srcName;
	}


	public boolean isType() {
		return isType;
	}


	public static CsKeyword toKeyword(String str) {
		CsKeyword resType = tryToKeyword(str);
		if(resType == null) {
			throw new IllegalArgumentException("'" + str + "' is not a valid C# keyword");
		}
		return resType;
	}


	public static CsKeyword tryToKeyword(String str) {
		int idx = Arrays.binarySearch(keywords, str);
		return idx > -1 ? CsKeyword.values()[idx] : null;
	}


	public static boolean isKeyword(String str) {
		return Arrays.binarySearch(keywords, str) > -1;
	}


	public static boolean isPrimitive(String str) {
		return Arrays.binarySearch(primitives, str) > -1;
	}


	public static boolean isType(String str) {
		return Arrays.binarySearch(types, str) > -1;
	}


	/**
	 * @param str
	 * @return true for any string which is a type keyword (i.e. {@link #isType(String)}) or any non-keyword strings,
	 * returns false for any other keywords 
	 */
	public static boolean isDataTypeKeyword(String str) {
		return Arrays.binarySearch(types, str) > -1;
	}

}
