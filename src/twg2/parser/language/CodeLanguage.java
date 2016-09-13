package twg2.parser.language;

import java.util.List;
import java.util.function.Function;

import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.AstUtil;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.Operator;
import twg2.parser.codeParser.OperatorUtil;
import twg2.parser.workflow.CodeFileSrc;
import twg2.parser.workflow.ParseInput;

/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public interface CodeLanguage {

	public AstUtil<? extends BlockType, ?> getAstUtil();

	public KeywordUtil<? extends AccessModifier> getKeywordUtil();

	public OperatorUtil<? extends Operator> getOperatorUtil();

	public String displayName();

	public List<String> getFileExtensions();

	/**
	 * @return a function which takes parser input parameters and an optional parser performance tracker (can be null, in which case no stats are recorded) and returns a {@link CodeFileSrc}
	 */
	public Function<ParseInput, ? extends CodeFileSrc<? extends CodeLanguage>> getParser();

	public AstExtractor<? extends BlockType> getExtractor();

}
