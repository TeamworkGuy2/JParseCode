package twg2.parser.codeParser.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.Keyword;
import twg2.parser.documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public enum JavaKeyword {
	// NOTE: these must be in alphabetical order for Inst array binary searches to work
	ABSTRACT("abstract"),
	ASSERT("assert"),
	BOOLEAN("boolean", true),
	BREAK("break"),
	BYTE("byte", true),
	CASE("case"),
	CATCH("catch"),
	CHAR("char", true),
	CLASS("class"),
	CONST("const"),
	CONTINUE("continue"),
	DEFAULT("default"),
	DO("do"),
	DOUBLE("double", true),
	ELSE("else"),
	ENUM("enum"),
	EXTENDS("extends"),
	FINAL("final"),
	FINALLY("finally"),
	FLOAT("float", true),
	FOR("for"),
	GOTO("goto"),
	IF("if"),
	IMPLEMENTS("implements"),
	IMPORT("import"),
	INSTANCEOF("instanceof"),
	INT("int", true),
	INTERFACE("interface", true),
	LONG("long", true),
	NATIVE("native"),
	NEW("new"),
	PACKAGE("package"),
	PRIVATE("private"),
	PROTECTED("protected"),
	PUBLIC("public"),
	RETURN("return"),
	SHORT("short", true),
	STATIC("static"),
	STRICTFP("strictfp"),
	SUPER("super"),
	SWITCH("switch"),
	SYNCHRONIZED("synchronized"),
	THIS("this"),
	THROW("throw"),
	THROWS("throws"),
	TRANSIENT("transient"),
	TRY("try"),
	VOID("void"),
	VOLATILE("volatile"),
	WHILE("while");


	public static final Inst check = new JavaKeyword.Inst();

	public final String srcName;
	public final boolean isType;


	JavaKeyword(String name) {
		this.srcName = name;
		this.isType = false;
	}


	JavaKeyword(String name, boolean isType) {
		this.srcName = name;
		this.isType = isType;
	}


	public String getSrcName() {
		return srcName;
	}


	public boolean isType() {
		return isType;
	}




	public static class Inst implements Keyword {
		public final String[] keywords;
		private final JavaKeyword[] values;
		private final String[] primitives;
		private final String[] types;


		{
			List<String> typesList = new ArrayList<>();
			JavaKeyword[] keywordEnums = JavaKeyword.values();

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

			// from: http://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.2
			primitives = new String[] { BOOLEAN.srcName, CHAR.srcName, BYTE.srcName, SHORT.srcName, INT.srcName, LONG.srcName, FLOAT.srcName, DOUBLE.srcName, LONG.srcName };
			Arrays.sort(primitives);
		}


		public JavaKeyword toKeyword(String str) {
			JavaKeyword resType = tryToKeyword(str);
			if(resType == null) {
				throw new IllegalArgumentException("'" + str + "' is not a valid Java keyword");
			}
			return resType;
		}


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
					(JavaKeyword.CLASS.getSrcName().equals(node.getText()) || JavaKeyword.INTERFACE.getSrcName().equals(node.getText())));
		}


		@Override
		public boolean isClassModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
			String text = null;
			return node != null && (node.getFragmentType() == CodeFragmentType.KEYWORD &&
					(JavaKeyword.PUBLIC.getSrcName().equals((text = node.getText())) || JavaKeyword.PROTECTED.getSrcName().equals(text) || JavaKeyword.PRIVATE.getSrcName().equals(text) ||
					JavaKeyword.ABSTRACT.getSrcName().equals(text) || JavaKeyword.STATIC.getSrcName().equals(text) || JavaKeyword.FINAL.getSrcName().equals(text) || JavaKeyword.STRICTFP.getSrcName().equals(text)));
		}

	}
	
}
