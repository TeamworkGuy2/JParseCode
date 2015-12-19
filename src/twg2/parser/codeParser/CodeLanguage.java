package twg2.parser.codeParser;

import java.util.List;
import java.util.function.Function;

import twg2.parser.baseAst.LanguageAstUtil;
import twg2.parser.documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public interface CodeLanguage {

	public LanguageAstUtil getAstUtil();

	public String displayName();

	public List<String> getFileExtensions();

	public Function<ParseInput, ? extends CodeFileSrc<DocumentFragmentText<CodeFragmentType>, ? extends CodeLanguage>> getParser();

}