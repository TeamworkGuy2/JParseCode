package twg2.parser.codeParser.java;

import java.util.Arrays;

import lombok.val;
import twg2.parser.baseAst.AccessModifier;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.tools.EnumSubSet;
import twg2.parser.documentParser.DocumentFragmentText;

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
	ENUM("enum"),
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


	JavaKeyword(String name) {
		this.srcName = name;
		this.isType = false;
		this.isClassModifier = false;
		this.isFieldModifier = false;
		this.isMethodModifier = false;
		this.isBlockModifier = false;
	}


	JavaKeyword(String name, int typeFlags) {
		this.srcName = name;
		this.isType = (typeFlags & Flag.IS_TYPE) == Flag.IS_TYPE;
		this.isClassModifier = (typeFlags & Flag.CLASS_MOD) == Flag.CLASS_MOD;
		this.isFieldModifier = (typeFlags & Flag.FIELD_MOD) == Flag.FIELD_MOD;
		this.isMethodModifier = (typeFlags & Flag.METHOD_MOD) == Flag.METHOD_MOD;
		this.isBlockModifier = (typeFlags & Flag.BLOCK_MOD) == Flag.BLOCK_MOD;
	}


	@Override
	public String toSrc() {
		return srcName;
	}




	public static class Inst implements KeywordUtil {
		public final String[] keywords;
		private final JavaKeyword[] values;
		private final String[] primitives;
		private final EnumSubSet<JavaKeyword> types;
		private final EnumSubSet<JavaKeyword> classMods;
		private final EnumSubSet<JavaKeyword> fieldMods;
		private final EnumSubSet<JavaKeyword> methodMods;
		private final EnumSubSet<JavaKeyword> blockMods;


		{
			JavaKeyword[] keywordEnums = JavaKeyword.values();
			EnumSubSet.Builder<JavaKeyword> typesSet = new EnumSubSet.Builder<>((e) -> e.isType, (e) -> e.srcName);
			EnumSubSet.Builder<JavaKeyword> classModsSet = new EnumSubSet.Builder<>((e) -> e.isClassModifier, (e) -> e.srcName);
			EnumSubSet.Builder<JavaKeyword> fieldModsSet = new EnumSubSet.Builder<>((e) -> e.isFieldModifier, (e) -> e.srcName);
			EnumSubSet.Builder<JavaKeyword> methodModsSet = new EnumSubSet.Builder<>((e) -> e.isMethodModifier, (e) -> e.srcName);
			EnumSubSet.Builder<JavaKeyword> blockModsSet = new EnumSubSet.Builder<>((e) -> e.isBlockModifier, (e) -> e.srcName);

			values = keywordEnums;

			keywords = new String[keywordEnums.length];

			for(int i = 0, size = keywordEnums.length; i < size; i++) {
				val enm = keywordEnums[i];
				keywords[i] = enm.srcName;

				typesSet.add(enm);
				classModsSet.add(enm);
				fieldModsSet.add(enm);
				methodModsSet.add(enm);
				blockModsSet.add(enm);
			}

			//Arrays.sort(keywords);

			types = typesSet.build();
			classMods = classModsSet.build();
			fieldMods = fieldModsSet.build();
			methodMods = methodModsSet.build();
			blockMods = blockModsSet.build();

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


		@Override
		public boolean isBlockKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return parseKeyword(node, blockMods) != null;
		}


		@Override
		public boolean isClassModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return parseKeyword(node, classMods) != null;
		}


		@Override
		public boolean isFieldModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return parseKeyword(node, fieldMods) != null;
		}


		@Override
		public boolean isMethodModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return parseKeyword(node, methodMods) != null;
		}


		@Override
		public AccessModifier parseBlockKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return parseKeyword(node, blockMods);
		}


		@Override
		public AccessModifier parseClassModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return parseKeyword(node, classMods);
		}


		@Override
		public AccessModifier parseFieldModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return parseKeyword(node, fieldMods);
		}


		@Override
		public AccessModifier parseMethodModifierKeyword(DocumentFragmentText<CodeFragmentType> node) {
			return parseKeyword(node, methodMods);
		}


		private static JavaKeyword parseKeyword(DocumentFragmentText<CodeFragmentType> node, EnumSubSet<JavaKeyword> enums) {
			if(node != null && node.getFragmentType() == CodeFragmentType.KEYWORD) {
				return enums.find(node.getText());
			}
			return null;
		}

	}
	
}



/**
 * @author TeamworkGuy2
 * @since 2016-2-18
 */
class Flag {
	static final int IS_TYPE = 1;
	static final int CLASS_MOD = 2;
	static final int FIELD_MOD = 4;
	static final int METHOD_MOD = 8;
	static final int BLOCK_MOD = 8;
}
