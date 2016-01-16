package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.Keyword;
import twg2.parser.documentParser.DocumentFragmentText;

public enum CsKeyword {
	// NOTE: these must be in alphabetical order for Inst array binary searches to work
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


	public static final CsKeyword.Inst check = new CsKeyword.Inst();

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




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-14
	 */
	public static class Inst implements Keyword {
		public final String[] keywords;
		private final CsKeyword[] values;
		private final String[] primitives;
		private final String[] types;


		{
			List<String> typesList = new ArrayList<>();
			CsKeyword[] keywordEnums = CsKeyword.values();

			values = keywordEnums;

			keywords = new String[keywordEnums.length];

			for(int i = 0, size = keywordEnums.length; i < size; i++) {
				keywords[i] = keywordEnums[i].srcName;
				if(keywordEnums[i].isType) {
					typesList.add(keywordEnums[i].srcName);
				}
			}

			//Arrays.sort(keywords);

			types = typesList.toArray(new String[typesList.size()]);
			Arrays.sort(types);

			// from: https://msdn.microsoft.com/en-us/library/system.type.isprimitive%28v=vs.110%29.aspx
			// IntPtr and UIntPtr aren't keywords, so they are string literals here
			primitives = new String[] { BOOL.srcName, BYTE.srcName, SBYTE.srcName, SHORT.srcName, USHORT.srcName, INT.srcName, UINT.srcName, LONG.srcName, ULONG.srcName,
					"IntPtr", "UIntPtr",
					CHAR.srcName, FLOAT.srcName, DOUBLE.srcName };
			Arrays.sort(primitives);
		}


		public CsKeyword toKeyword(String str) {
			CsKeyword resType = tryToKeyword(str);
			if(resType == null) {
				throw new IllegalArgumentException("'" + str + "' is not a valid C# keyword");
			}
			return resType;
		}


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
			return Arrays.binarySearch(types, str) > -1;
		}


		/**
		 * @param str
		 * @return true for any string which is a type keyword (i.e. {@link #isType(String)}) or any non-keyword strings,
		 * returns false for any other keywords 
		 */
		@Override
		public boolean isDataTypeKeyword(String str) {
			return Arrays.binarySearch(types, str) > -1;
		}


		@Override
		public boolean isBlockKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD &&
					(CsKeyword.CLASS.getSrcName().equals(node.getText()) || CsKeyword.INTERFACE.getSrcName().equals(node.getText()) || CsKeyword.NAMESPACE.getSrcName().equals(node.getText())));
		}


		@Override
		public boolean isClassModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
			String text = null;
			return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD &&
					(CsKeyword.PUBLIC.getSrcName().equals((text = node.getText())) || CsKeyword.PROTECTED.getSrcName().equals(text) || CsKeyword.INTERNAL.getSrcName().equals(text) || CsKeyword.PRIVATE.getSrcName().equals(text) ||
					CsKeyword.ABSTRACT.getSrcName().equals(text) || CsKeyword.SEALED.getSrcName().equals(text) || CsKeyword.STATIC.getSrcName().equals(text)));
		}

	}

}
