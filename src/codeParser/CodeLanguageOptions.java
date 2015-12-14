package codeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import lombok.Getter;
import lombok.val;
import baseAst.LanguageAstUtil;
import baseAst.csharp.CsAstUtil;
import codeParser.csharp.CsClassParser;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
public class CodeLanguageOptions<T_LANG extends CodeLanguage, T_AST_UTIL extends LanguageAstUtil> implements CodeLanguage {

	public static class CSharp extends CodeLanguageOptions<CSharp, CsAstUtil> {
		CSharp(String displayName, CsAstUtil astUtil, Function<ParseInput, CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CSharp>> parser, String... fileExtensions) {
			super(displayName, astUtil, parser, fileExtensions);
		}

	}


	public static final CodeLanguageOptions<CodeLanguage, LanguageAstUtil> JAVA = new CodeLanguageOptions<>("Java", null, null, "java");
	public static final CodeLanguageOptions<CodeLanguage, LanguageAstUtil> JAVASCRIPT = new CodeLanguageOptions<>("Javascript", null, null, "js", "ts");
	public static final CSharp C_SHARP = new CSharp("C#", new CsAstUtil(), CsClassParser::parse, "cs");
	public static final CodeLanguageOptions<CodeLanguage, LanguageAstUtil> CSS = new CodeLanguageOptions<>("CSS", (LanguageAstUtil)null, null, "css");
	public static final CodeLanguageOptions<CodeLanguage, LanguageAstUtil> XML = new CodeLanguageOptions<>("XML", null, null, "html", "xml");

	private static CopyOnWriteArrayList<CodeLanguage> values;


	static {
		values = new CopyOnWriteArrayList<>();
		values.add(JAVA);
		values.add(JAVASCRIPT);
		values.add(C_SHARP);
		values.add(CSS);
		values.add(XML);
	}


	final String displayName;
	@Getter final Function<ParseInput, CodeFileSrc<DocumentFragmentText<CodeFragmentType>, T_LANG>> parser;
	@Getter final List<String> fileExtensions;
	@Getter final T_AST_UTIL astUtil;


	// package-private
	CodeLanguageOptions(String displayName, T_AST_UTIL astUtil,
			Function<ParseInput, CodeFileSrc<DocumentFragmentText<CodeFragmentType>, T_LANG>> parser, String... fileExtensions) {
		this.displayName = displayName;
		this.parser = parser;
		this.fileExtensions = new ArrayList<>(Arrays.asList(fileExtensions));
		this.astUtil = astUtil;
	}


	@Override
	public String displayName() {
		return displayName;
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
	public static <_T_LANG extends CodeLanguage, _T_AST_UTIL extends LanguageAstUtil> CodeLanguage registerCodeLanguage(String displayName, _T_AST_UTIL astUtil,
			Function<ParseInput, CodeFileSrc<DocumentFragmentText<CodeFragmentType>, _T_LANG>> parser, String... fileExtensions) {
		val inst = new CodeLanguageOptions<>(displayName, astUtil, parser, fileExtensions);
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


	private static final void _registerNewLanguage(CodeLanguageOptions<? extends CodeLanguage, ? extends LanguageAstUtil> inst) {
		values.add(inst);
	}

}
