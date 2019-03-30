package twg2.parser.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.AstUtil;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.BlockUtil;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.Operator;
import twg2.parser.codeParser.OperatorUtil;
import twg2.parser.codeParser.csharp.CsAstUtil;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsBlock.CsBlockUtil;
import twg2.parser.codeParser.csharp.CsBlockParser;
import twg2.parser.codeParser.csharp.CsFileTokenizer;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.codeParser.csharp.CsOperator;
import twg2.parser.codeParser.java.JavaAstUtil;
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaBlock.JavaBlockUtil;
import twg2.parser.codeParser.java.JavaBlockParser;
import twg2.parser.codeParser.java.JavaFileTokenizer;
import twg2.parser.codeParser.java.JavaKeyword;
import twg2.parser.codeParser.java.JavaOperator;
import twg2.parser.tokenizers.CodeTokenizerBuilder;
import twg2.parser.workflow.CodeFileSrc;
import twg2.parser.workflow.ParseInput;

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
	public static class CodeLanguageImpl<
			T_BLOCK extends BlockType,
			T_KEYWORD extends Keyword,
			T_LANG extends CodeLanguage,
			T_OP extends Operator,
			T_AST_UTIL extends AstUtil<T_BLOCK, T_KEYWORD>,
			T_OP_UTIL extends OperatorUtil<T_OP>,
			T_AST_EXTRACTOR extends AstExtractor<T_BLOCK>
			> implements CodeLanguage {
		final String displayName;
		final BlockUtil<T_BLOCK, T_KEYWORD> blockUtil;
		final T_AST_UTIL astUtil;
		final KeywordUtil<T_KEYWORD> keywordUtil;
		final T_OP_UTIL operatorUtil;
		final Function<ParseInput, CodeFileSrc> parser;
		final T_AST_EXTRACTOR extractor;
		final List<String> fileExtensions;

		public BlockUtil<T_BLOCK, T_KEYWORD> getBlockUtil() { return blockUtil; }

		@Override public T_AST_UTIL getAstUtil() { return astUtil; }

		@Override public KeywordUtil<T_KEYWORD> getKeywordUtil() { return keywordUtil; }

		@Override public T_OP_UTIL getOperatorUtil() { return operatorUtil; }

		@Override public Function<ParseInput, CodeFileSrc> getParser() { return parser; }

		@Override public T_AST_EXTRACTOR getExtractor() { return extractor; }

		@Override public List<String> getFileExtensions() { return fileExtensions; }


		/** Create a new code language instance.
		 * @param displayName the common name of the language
		 * @param astUtil the utility used to parse and convert specific features of this language to the common {@code baseAst} representation used by this project
		 * @param parser the parser builder for this language, call it with {@link ParseInput} parameters and get back a parsed {@link CodeFileSrc}.
		 * NOTE: this function should be thread-safe or should have no side effects
		 * @param fileExtensions a list of file extensions associated with this language
		 * @return a new {@link CodeLanguage} instance
		 */
		// package-private
		@SuppressWarnings("unchecked")
		CodeLanguageImpl(
				String displayName,
				BlockUtil<T_BLOCK, T_KEYWORD> blockUtil,
				AstUtil<? extends T_BLOCK, ? extends T_KEYWORD> astUtil,
				KeywordUtil<? extends T_KEYWORD> keywordUtil,
				T_OP_UTIL operatorUtil,
				Function<ParseInput, CodeFileSrc> parser,
				T_AST_EXTRACTOR extractor,
				List<String> fileExtensions
		) {
			this.displayName = displayName;
			this.blockUtil = blockUtil;
			this.astUtil = (T_AST_UTIL)astUtil;
			this.keywordUtil = (KeywordUtil<T_KEYWORD>)keywordUtil;
			this.operatorUtil = operatorUtil;
			this.parser = parser;
			this.extractor = extractor;
			this.fileExtensions = new ArrayList<>(fileExtensions);
		}


		@Override
		public String displayName() {
			return displayName;
		}

	}


	public static class CSharp extends CodeLanguageImpl<CsBlock, CsKeyword, CSharp, CsOperator, CsAstUtil, CsOperator.Inst, AstExtractor<CsBlock>> {

		CSharp(
				String displayName,
				CsBlockUtil blockUtil,
				CsAstUtil astUtil,
				KeywordUtil<CsKeyword> keywordUtil,
				CsOperator.Inst operatorUtil,
				Function<ParseInput, CodeFileSrc> parser,
				AstExtractor<CsBlock> extractor,
				List<String> fileExtensions
		) {
			super(displayName, blockUtil, astUtil, keywordUtil, operatorUtil, parser, extractor, fileExtensions);
		}

	}


	public static class Java extends CodeLanguageImpl<JavaBlock, JavaKeyword, Java, JavaOperator, JavaAstUtil, JavaOperator.Inst, AstExtractor<JavaBlock>> {

		Java(
				String displayName,
				JavaBlockUtil blockUtil,
				JavaAstUtil astUtil,
				KeywordUtil<JavaKeyword> keywordUtil,
				JavaOperator.Inst operatorUtil,
				Function<ParseInput, CodeFileSrc> parser,
				AstExtractor<JavaBlock> extractor,
				List<String> fileExtensions
		) {
			super(displayName, blockUtil, astUtil, keywordUtil, operatorUtil, parser, extractor, fileExtensions);
		}

	}


	private static CopyOnWriteArrayList<CodeLanguage> values = new CopyOnWriteArrayList<>();


	public static final CSharp C_SHARP = registerCodeLanguage(
		new CSharp("C#", new CsBlockUtil(), new CsAstUtil(), CsKeyword.check, CsOperator.check,
			CodeTokenizerBuilder.createTokenizerWithTimer(() -> CsFileTokenizer.createFileParser().build()), new CsBlockParser(), Arrays.asList("cs"))
	);

	public static final Java JAVA = registerCodeLanguage(
		new Java("Java", new JavaBlockUtil(), new JavaAstUtil(), JavaKeyword.check, JavaOperator.check,
			CodeTokenizerBuilder.createTokenizerWithTimer(() -> JavaFileTokenizer.createFileParser().build()), new JavaBlockParser(), Arrays.asList("java"))
	);

	public static final CodeLanguageImpl<BlockType, Keyword, CodeLanguage, Operator, AstUtil<BlockType, Keyword>, OperatorUtil<Operator>, AstExtractor<BlockType>> JAVASCRIPT = registerCodeLanguage(
		new CodeLanguageImpl<>("Javascript", null, null, null, null, null, null, Arrays.asList("js", "ts"))
	);


	/**
	 * @return a list of all registered languages
	 */
	public static List<CodeLanguage> getLanguagesCopy() {
		return new ArrayList<>(values);
	}


	public static CodeLanguage fromFileExtension(String fileExtension) throws IllegalArgumentException {
		CodeLanguage lang = tryFromFileExtension(fileExtension);
		if(lang == null) {
			throw new IllegalArgumentException("unsupported file extension '" + fileExtension + "' for parsing");
		}
		return lang;
	}


	public static CodeLanguage tryFromFileExtension(String fileExtension) {
		if(fileExtension.charAt(0) == '.') {
			fileExtension = fileExtension.substring(1);
		}

		for(CodeLanguage lang : CodeLanguageOptions.values) {
			if(lang.getFileExtensions().indexOf(fileExtension) > -1) {
				return lang;
			}
		}
		return null;
	}



	/** Register a new language (while will be added to the list returned by {@link CodeLanguageOptions#getLanguagesCopy()}.<br>
	 * NOTE: thread safe
	 * @param inst the code language the register
	 * @return the input {@code inst} unmodified
	 */
	private static final <T extends CodeLanguage> T registerCodeLanguage(T inst) {
		values.add(inst);
		return inst;
	}

}
