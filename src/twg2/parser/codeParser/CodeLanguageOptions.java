package twg2.parser.codeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import lombok.Getter;
import lombok.val;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.AstUtil;
import twg2.parser.baseAst.csharp.CsAstUtil;
import twg2.parser.baseAst.java.JavaAstUtil;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsBlockParser;
import twg2.parser.codeParser.csharp.CsClassParser;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaBlockParser;
import twg2.parser.codeParser.java.JavaClassParser;
import twg2.parser.codeParser.java.JavaKeyword;

/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
public enum CodeLanguageOptions {
	;

	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-16
	 * @param <T_KEYWORD> the keyword enum containing this language's keywords
	 * @param <T_LANG> this language's {@link CodeLanguage} type
	 * @param <T_AST_UTIL> the {@link AstUtil} type for this language
	 * @param <T_AST_EXTRACTOR> {@link AstExtractor} type for this language
	 */
	public static class CodeLanguageImpl<T_KEYWORD, T_LANG extends CodeLanguage, T_AST_UTIL extends AstUtil<? extends CompoundBlock, T_KEYWORD>, T_AST_EXTRACTOR extends AstExtractor<? extends CompoundBlock>> implements CodeLanguage {
		final String displayName;
		@Getter final Function<ParseInput, CodeFileSrc<T_LANG>> parser;
		@Getter final List<String> fileExtensions;
		@Getter final T_AST_UTIL astUtil;
		@Getter final Keyword keyword;
		@Getter final T_AST_EXTRACTOR extractor;


		// package-private
		CodeLanguageImpl(String displayName, T_AST_UTIL astUtil, Keyword keyword, Function<ParseInput, CodeFileSrc<T_LANG>> parser, T_AST_EXTRACTOR extractor, String... fileExtensions) {
			this.displayName = displayName;
			this.parser = parser;
			this.fileExtensions = new ArrayList<>(Arrays.asList(fileExtensions));
			this.astUtil = astUtil;
			this.keyword = keyword;
			this.extractor = extractor;
		}


		@Override
		public String displayName() {
			return displayName;
		}

	}


	public static class CSharp extends CodeLanguageImpl<CsKeyword, CSharp, CsAstUtil, AstExtractor<CsBlock>> {
		CSharp(String displayName, CsAstUtil astUtil, Keyword keyword, Function<ParseInput, CodeFileSrc<CSharp>> parser, AstExtractor<CsBlock> extractor, String... fileExtensions) {
			super(displayName, astUtil, keyword, parser, extractor, fileExtensions);
		}

	}


	public static class Java extends CodeLanguageImpl<JavaKeyword, Java, JavaAstUtil, AstExtractor<JavaBlock>> {
		Java(String displayName, JavaAstUtil astUtil, Keyword keyword, Function<ParseInput, CodeFileSrc<Java>> parser, AstExtractor<JavaBlock> extractor, String... fileExtensions) {
			super(displayName, astUtil, keyword, parser, extractor, fileExtensions);
		}

	}


	public static final Java JAVA = new Java("Java", new JavaAstUtil(), JavaKeyword.check, JavaClassParser::parse, new JavaBlockParser(), "java");
	public static final CodeLanguageImpl<Object, CodeLanguage, AstUtil<CompoundBlock, Object>, AstExtractor<CompoundBlock>> JAVASCRIPT = new CodeLanguageImpl<>("Javascript", null, null, null, null, "js", "ts");
	public static final CSharp C_SHARP = new CSharp("C#", new CsAstUtil(), CsKeyword.check, CsClassParser::parse, new CsBlockParser(), "cs");
	public static final CodeLanguageImpl<Object, CodeLanguage, AstUtil<CompoundBlock, Object>, AstExtractor<CompoundBlock>> CSS = new CodeLanguageImpl<>("CSS", null, null, null, null, "css");
	public static final CodeLanguageImpl<Object, CodeLanguage, AstUtil<CompoundBlock, Object>, AstExtractor<CompoundBlock>> XML = new CodeLanguageImpl<>("XML", null, null, null, null, "html", "xml");

	private static CopyOnWriteArrayList<CodeLanguage> values;


	static {
		values = new CopyOnWriteArrayList<>();
		values.add(JAVA);
		values.add(JAVASCRIPT);
		values.add(C_SHARP);
		values.add(CSS);
		values.add(XML);
	}


	/**
	 * @return a list of all registered languages
	 */
	public static List<CodeLanguage> getLanguages() {
		return values;
	}


	/** Register a new language (while will be added to the list returned by {@link CodeLanguageOptions#getLanguages()}.<br>
	 * NOTE: thread safe
	 * @param displayName the common name of the language
	 * @param astUtil the utility used to parse and convert specific features of this language to the common {@code baseAst} representation used by this project
	 * @param parser the parser builder for this language, call it with {@link ParseInput} parameters and get back a parsed {@link CodeFileSrc}.
	 * NOTE: this function should be thread-safe or should have no side effects
	 * @param fileExtensions a list of file extensions associated with this language
	 * @return a new {@link CodeLanguage} instance
	 */
	public static <_T_BLOCK extends CompoundBlock, _T_KEYWORD, _T_LANG extends CodeLanguage, _T_AST_UTIL extends AstUtil<_T_BLOCK, _T_KEYWORD>, T_AST_EXTRACTOR extends AstExtractor<_T_BLOCK>> CodeLanguage registerCodeLanguage(
			String displayName, _T_AST_UTIL astUtil, Keyword keyword, Function<ParseInput, CodeFileSrc<_T_LANG>> parser, T_AST_EXTRACTOR extractor, String... fileExtensions) {
		val inst = new CodeLanguageImpl<>(displayName, astUtil, keyword, parser, extractor, fileExtensions);
		_registerNewLanguage(inst);
		return inst;
	}


	public static CodeLanguage fromFileExtension(String fileExtension) throws IllegalArgumentException {
		val lang = tryFromFileExtension(fileExtension);
		if(lang == null) {
			throw new IllegalArgumentException("unsupported file extension '" + fileExtension + "' for parsing");
		}
		return lang;
	}


	public static CodeLanguage tryFromFileExtension(String fileExtension) {
		if(fileExtension.charAt(0) == '.') {
			fileExtension = fileExtension.substring(1);
		}

		for(val lang : CodeLanguageOptions.values) {
			if(lang.getFileExtensions().indexOf(fileExtension) > -1) {
				return lang;
			}
		}
		return null;
	}


	private static final <T_BLOCK extends CompoundBlock, T_KEYWORD> void _registerNewLanguage(CodeLanguage inst) {
		values.add(inst);
	}

}
