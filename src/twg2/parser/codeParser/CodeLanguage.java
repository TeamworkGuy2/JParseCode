package twg2.parser.codeParser;

import java.util.List;
import java.util.function.Function;

import twg2.parser.baseAst.AstUtil;
import twg2.parser.baseAst.CompoundBlock;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public interface CodeLanguage {

	public AstUtil getAstUtil();

	public KeywordUtil getKeywordUtil();

	public String displayName();

	public List<String> getFileExtensions();

	public Function<ParseInput, ? extends CodeFileSrc<? extends CodeLanguage>> getParser();

	public AstExtractor<? extends CompoundBlock> getExtractor();

}
