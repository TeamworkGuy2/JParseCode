package twg2.parser.codeParser.tools.performance;

import java.io.IOException;
import java.util.Map;

import lombok.Getter;
import lombok.val;
import twg2.dataUtil.dataUtils.EnumError;
import twg2.io.files.FileUtil;
import twg2.io.write.JsonWrite;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringPad;

/**
 * @author TeamworkGuy2
 * @since 2016-09-11
 */
public class TokenizeStepDetails implements JsonWritableSig {


	public static enum ParserAction {
		PARSER_CONDITIONS_ADDED,
		CHAR_CHECKS,
		TEXT_FRAGMENTS_CONSUMED;
	}



	@Getter int conditionsAdded;
	@Getter int charChecks;
	@Getter int textFragsConsumed;


	public void log(ParserAction action, long detail) {
		switch(action) {
		case PARSER_CONDITIONS_ADDED:
			this.conditionsAdded += detail;
			break;
		case CHAR_CHECKS:
			this.charChecks += detail;
			break;
		case TEXT_FRAGMENTS_CONSUMED:
			this.textFragsConsumed += detail;
			break;
		default:
			throw EnumError.unknownValue(action, ParserAction.class);
		}
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		toJson(null, false, dst, st);
	}


	public void toJson(String srcName, boolean includeSurroundingBrackets, Appendable dst, WriteSettings st) throws IOException {
		if(includeSurroundingBrackets) { dst.append("{ "); }
		dst.append(
				(srcName != null ? ("\"file\": \"" + srcName + "\", ") : "") +
				"\"charChecks\": " + this.charChecks + ", " +
				"\"conditionsAdded\": " + this.conditionsAdded + ", " +
				"\"textFragmentsConsumed\": " + this.textFragsConsumed
			);
		if(includeSurroundingBrackets) { dst.append(" }"); }
	}


	@Override
	public String toString() {
		return toString(null, true);
	}


	public String toString(String srcName, boolean includeClassName) {
		return (includeClassName ? "parserSteps: { " : "") +
				(srcName != null ? ("file: " + srcName + ", ") : "") +
				"charChecks: " + this.charChecks + ", " +
				"conditionsAdded: " + this.conditionsAdded + ", " +
				"textFragmentsConsumed: " + this.textFragsConsumed +
			(includeClassName ? " }" : "");
	}


	public static void toJsons(Map<String, TokenizeStepDetails> fileParserDetails, boolean includeSurroundingBrackets, Appendable dst, WriteSettings st) throws IOException {
		if(includeSurroundingBrackets) { dst.append("[\n"); }
		JsonWrite.joinStrConsume(fileParserDetails.entrySet(), ",\n", dst, (entry) -> {
			val stat = entry.getValue();

			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());

			stat.toJson(fileName, includeSurroundingBrackets, dst, st);
		});
		if(includeSurroundingBrackets) { dst.append("\n]"); }
	}


	public static String toStrings(Map<String, TokenizeStepDetails> fileParserDetails) {
		val sb = new StringBuilder();

		for(val entry : fileParserDetails.entrySet()) {
			val stat = entry.getValue();
			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());
			fileName = '"' + fileName.substring(0, Math.min(30, fileName.length())) + '"';
			fileName = StringPad.padRight(fileName, 32, ' ');

			sb.append(stat.toString(fileName, true) + "\n");
		}

		return sb.toString();
	}

}
