package twg2.parser.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import lombok.Getter;
import lombok.val;
import twg2.parser.codeParser.AccessModifier;
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
			T_KEYWORD extends AccessModifier,
			T_LANG extends CodeLanguage,
			T_OP extends Operator,
			T_AST_UTIL extends AstUtil<T_BLOCK, T_KEYWORD>,
			T_OP_UTIL extends OperatorUtil<T_OP>,
			T_AST_EXTRACTOR extends AstExtractor<T_BLOCK>
			> implements CodeLanguage {
		final String displayName;
		@Getter final BlockUtil<T_BLOCK, T_KEYWORD> blockUtil;
		@Getter final T_AST_UTIL astUtil;
		@Getter final KeywordUtil<T_KEYWORD> keywordUtil;
		@Getter final T_OP_UTIL operatorUtil;
		@Getter final Function<ParseInput, CodeFileSrc<T_LANG>> parser;
		@Getter final T_AST_EXTRACTOR extractor;
		@Getter final List<String> fileExtensions;


		// package-private
		@SuppressWarnings("unchecked")
		CodeLanguageImpl(String displayName, BlockUtil<T_BLOCK, T_KEYWORD> blockUtil, AstUtil<? extends T_BLOCK, ? extends T_KEYWORD> astUtil, KeywordUtil<? extends T_KEYWORD> keywordUtil, T_OP_UTIL operatorUtil,
				Function<ParseInput, CodeFileSrc<T_LANG>> parser, T_AST_EXTRACTOR extractor, List<String> fileExtensions) {
			this.displayName = displayName;
			this.parser = parser;
			this.fileExtensions = new ArrayList<>(fileExtensions);
			this.blockUtil = blockUtil;
			this.astUtil = (T_AST_UTIL)astUtil;
			this.keywordUtil = (KeywordUtil<T_KEYWORD>)keywordUtil;
			this.operatorUtil = operatorUtil;
			this.extractor = extractor;
		}


		@Override
		public String displayName() {
			return displayName;
		}

	}


	public static class CSharp extends CodeLanguageImpl<CsBlock, CsKeyword, CSharp, CsOperator, CsAstUtil, CsOperator.Inst, AstExtractor<CsBlock>> {

		CSharp(String displayName, CsBlockUtil blockUtil, CsAstUtil astUtil, KeywordUtil<CsKeyword> keywordUtil, CsOperator.Inst operatorUtil,
				Function<ParseInput, CodeFileSrc<CSharp>> parser, AstExtractor<CsBlock> extractor, List<String> fileExtensions) {
			super(displayName, blockUtil, astUtil, keywordUtil, operatorUtil, parser, extractor, fileExtensions);
		}

	}


	public static class Java extends CodeLanguageImpl<JavaBlock, JavaKeyword, Java, JavaOperator, JavaAstUtil, JavaOperator.Inst, AstExtractor<JavaBlock>> {

		Java(String displayName, JavaBlockUtil blockUtil, JavaAstUtil astUtil, KeywordUtil<JavaKeyword> keywordUtil, JavaOperator.Inst operatorUtil,
				Function<ParseInput, CodeFileSrc<Java>> parser, AstExtractor<JavaBlock> extractor, List<String> fileExtensions) {
			super(displayName, blockUtil, astUtil, keywordUtil, operatorUtil, parser, extractor, fileExtensions);
		}

	}


	public static final CSharp C_SHARP = new CSharp("C#", new CsBlockUtil(), new CsAstUtil(), CsKeyword.check, CsOperator.check,
			CodeTokenizerBuilder.createTokenizerWithTimer(() -> CsFileTokenizer.createFileParser().build()), new CsBlockParser(), Arrays.asList("cs"));

	public static final Java JAVA = new Java("Java", new JavaBlockUtil(), new JavaAstUtil(), JavaKeyword.check, JavaOperator.check,
			CodeTokenizerBuilder.createTokenizerWithTimer(() -> JavaFileTokenizer.createFileParser().build()), new JavaBlockParser(), Arrays.asList("java"));

	public static final CodeLanguageImpl<BlockType, AccessModifier, CodeLanguage, Operator, AstUtil<BlockType, AccessModifier>, OperatorUtil<Operator>, AstExtractor<BlockType>> JAVASCRIPT = new CodeLanguageImpl<>("Javascript", null, null, null, null, null, null, Arrays.asList("js", "ts"));

	private static CopyOnWriteArrayList<CodeLanguage> values;


	static {
		values = new CopyOnWriteArrayList<>();
		values.add(JAVA);
		values.add(JAVASCRIPT);
		values.add(C_SHARP);
	}


	/**
	 * @return a list of all registered languages
	 */
	public static List<CodeLanguage> getLanguagesCopy() {
		return new ArrayList<>(values);
	}


	/** Register a new language (while will be added to the list returned by {@link CodeLanguageOptions#getLanguagesCopy()}.<br>
	 * NOTE: thread safe
	 * @param displayName the common name of the language
	 * @param astUtil the utility used to parse and convert specific features of this language to the common {@code baseAst} representation used by this project
	 * @param parser the parser builder for this language, call it with {@link ParseInput} parameters and get back a parsed {@link CodeFileSrc}.
	 * NOTE: this function should be thread-safe or should have no side effects
	 * @param fileExtensions a list of file extensions associated with this language
	 * @return a new {@link CodeLanguage} instance
	 */
	public static <
			_T_BLOCK extends BlockType,
			_T_KEYWORD extends AccessModifier,
			_T_LANG extends CodeLanguage,
			_T_OP extends Operator,
			_T_AST_UTIL extends AstUtil<_T_BLOCK, _T_KEYWORD>,
			_T_OP_UTIL extends OperatorUtil<_T_OP>,
			_T_AST_EXTRACTOR extends AstExtractor<_T_BLOCK>
			> CodeLanguage registerCodeLanguage(String displayName, BlockUtil<_T_BLOCK, _T_KEYWORD> block, _T_AST_UTIL astUtil, KeywordUtil<? extends _T_KEYWORD> keywordUtil,
					_T_OP_UTIL operatorUtil, Function<ParseInput, CodeFileSrc<_T_LANG>> parser, _T_AST_EXTRACTOR extractor, List<String> fileExtensions) {

		CodeLanguageImpl<_T_BLOCK, _T_KEYWORD, _T_LANG, _T_OP, _T_AST_UTIL, _T_OP_UTIL, _T_AST_EXTRACTOR> inst =
				new CodeLanguageImpl<>(displayName, block, astUtil, keywordUtil, operatorUtil, parser, extractor, fileExtensions);
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


	private static final <T_BLOCK extends BlockType, T_KEYWORD> void _registerNewLanguage(CodeLanguage inst) {
		values.add(inst);
	}

}
