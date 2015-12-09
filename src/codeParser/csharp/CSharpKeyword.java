package codeParser.csharp;

import java.util.Arrays;

public enum CSharpKeyword {
	ABSTRACT("abstract"),
	AS("as"),
	BASE("base"),
	BOOL("bool"),
	BREAK("break"),
	BYTE("byte"),
	CASE("case"),
	CATCH("catch"),
	CHAR("char"),
	CHECKED("checked"),
	CLASS("class"),
	CONST("const"),
	CONTINUE("continue"),
	DECIMAL("decimal"),
	DEFAULT("default"),
	DELEGATE("delegate"),
	DO("do"),
	DOUBLE("double"),
	ELSE("else"),
	ENUM("enum"),
	EVENT("event"),
	EXPLICIT("explicit"),
	EXTERN("extern"),
	FALSE("false"),
	FINALLY("finally"),
	FIXED("fixed"),
	FLOAT("float"),
	FOR("for"),
	FOREACH("foreach"),
	GOTO("goto"),
	IF("if"),
	IMPLICIT("implicit"),
	IN("in"),
	INT("int"),
	INTERFACE("interface"),
	INTERNAL("internal"),
	IS("is"),
	LOCK("lock"),
	LONG("long"),
	NAMESPACE("namespace"),
	NEW("new"),
	NULL("null"),
	OBJECT("object"),
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
	SBYTE("sbyte"),
	SEALED("sealed"),
	SHORT("short"),
	SIZEOF("sizeof"),
	STACKALLOC("stackalloc"),
	STATIC("static"),
	STRING("string"),
	STRUCT("struct"),
	SWITCH("switch"),
	THIS("this"),
	THROW("throw"),
	TRUE("true"),
	TRY("try"),
	TYPEOF("typeof"),
	UINT("uint"),
	ULONG("ulong"),
	UNCHECKED("unchecked"),
	UNSAFE("unsafe"),
	USHORT("ushort"),
	USING("using"),
	VIRTUAL("virtual"),
	VOID("void"),
	VOLATILE("volatile"),
	WHILE("while");


	public static final String[] keywords;


	static {
		CSharpKeyword[] keywordEnums = CSharpKeyword.values();
		keywords = new String[keywordEnums.length];
		for(int i = 0, size = keywordEnums.length; i < size; i++) {
			keywords[i] = keywordEnums[i].srcName;
		}
		Arrays.sort(keywords);
	}


	public final String srcName;


	CSharpKeyword(String name) {
		this.srcName = name;
	}


	public String getSrcName() {
		return srcName;
	}


	public static CSharpKeyword toKeyword(String str) {
		CSharpKeyword resType = tryToKeyword(str);
		if(resType == null) {
			throw new IllegalArgumentException("'" + str + "' is not a valid C# keyword");
		}
		return resType;
	}


	public static CSharpKeyword tryToKeyword(String str) {
		int idx = Arrays.binarySearch(keywords, str);
		return idx > -1 ? CSharpKeyword.values()[idx] : null;
	}


	public static boolean isKeyword(String str) {
		return Arrays.binarySearch(keywords, str) > -1;
	}

}
