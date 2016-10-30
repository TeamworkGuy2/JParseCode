package twg2.parser.workflow;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import twg2.parser.codeParser.analytics.ParseTimes;
import twg2.parser.codeParser.analytics.TokenizeStepLogger;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
@Accessors(fluent = true)
@AllArgsConstructor
public class ParseInput {
	@Getter private final char[] src;
	@Getter private final int srcOff;
	@Getter private final int srcLen;
	@Getter private final String fileName; // optional
	@Getter private final Consumer<Exception> errorHandler;
	@Getter private final ParseTimes parseTimes; // optional
	@Getter private final TokenizeStepLogger parserStepsTracker; // optional

	@Override
	public String toString() {
		return "parseInput: { file: " + fileName + " }";
	}

}
