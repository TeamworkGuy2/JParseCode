package twg2.parser.tokenizers;

import twg2.parser.codeParser.analytics.TokenizeStepLogger;
import twg2.parser.fragment.CodeToken;
import twg2.parser.workflow.CodeFileSrc;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
@FunctionalInterface
public interface CodeTokenizer {

	/** Parse a source string using the parsers provided by the {@link CodeTokenizer}
	 * @param src the source string
	 * @param srcName (optional) the name of the source, can be null
	 * @param stepsDetails (optional) code parser stat tracker, if null, no stats are tracked
	 * @return a parsed {@link CodeFileSrc} containing {@link CodeToken} nodes represented the tokens parsed from {@code src}
	 */
	public CodeFileSrc tokenizeDocument(char[] src, int srcOff, int srcLen, String srcName, TokenizeStepLogger stepsDetails);

}
