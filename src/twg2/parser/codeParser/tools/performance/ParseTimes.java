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
 * @since 2016-09-07
 */
public class ParseTimes implements JsonWritableSig {


	public static enum TrackerAction {
		SETUP,
		LOAD,
		TOKENIZE,
		PARSE;
	}



	@Getter long setupNs;
	@Getter long loadNs;
	@Getter long tokenizeNs;
	@Getter long parseNs;


	public long getTotalNs() {
		return setupNs + loadNs + tokenizeNs + parseNs;
	}


	public void log(TrackerAction action, long timeNanos) {
		switch(action) {
		case SETUP:
			this.setupNs = timeNanos;
			break;
		case LOAD:
			this.loadNs = timeNanos;
			break;
		case TOKENIZE:
			this.tokenizeNs = timeNanos;
			break;
		case PARSE:
			this.parseNs = timeNanos;
			break;
		default:
			throw EnumError.unknownValue(action, TrackerAction.class);
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
				"\"setup\": " + roundNsToMs(this.setupNs) + ", " +
				"\"load\": " + roundNsToMs(this.loadNs) + ", " +
				"\"tokenize\": " + roundNsToMs(this.tokenizeNs) + ", " +
				"\"parse\": " + roundNsToMs(this.parseNs) + ", " +
				"\"units\": \"milliseconds\""
			);
		if(includeSurroundingBrackets) { dst.append(" }"); }
	}


	@Override
	public String toString() {
		return toString(null, true);
	}


	public String toString(String srcName, boolean includeClassName) {
		return (includeClassName ? "parserPerformance: { " : "") +
				(srcName != null ? ("file: " + srcName + ", ") : "") +
				"setup: " + roundNsToMs(this.setupNs) + ", " +
				"load: " + roundNsToMs(this.loadNs) + ", " +
				"tokenize: " + roundNsToMs(this.tokenizeNs) + ", " +
				"parse: " + roundNsToMs(this.parseNs) + ", " +
				"total: " + roundNsToMs(this.getTotalNs()) +
			(includeClassName ? " }" : "");
	}


	public static final void toJsons(Map<String, ParseTimes> fileParseTimes, boolean includeSurroundingBrackets, Appendable dst, WriteSettings st) throws IOException {
		if(includeSurroundingBrackets) { dst.append("[\n"); }
		JsonWrite.joinStrConsume(fileParseTimes.entrySet(), ",\n", dst, (entry) -> {
			val stat = entry.getValue();

			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());

			stat.toJson(fileName, includeSurroundingBrackets, dst, st);
		});
		if(includeSurroundingBrackets) { dst.append("\n]"); }
	}


	public static final String toStrings(Map<String, ParseTimes> fileParseTimes) {
		long total = 0;
		val sb = new StringBuilder();

		for(val entry : fileParseTimes.entrySet()) {
			val stat = entry.getValue();
			long subtotal = stat.setupNs + stat.loadNs + stat.tokenizeNs + stat.parseNs;
			total += subtotal;

			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());
			fileName = '"' + fileName.substring(0, Math.min(30, fileName.length())) + '"';
			fileName = StringPad.padRight(fileName, 32, ' ');

			sb.append(stat.toString(fileName, true) + "\n");
		}

		sb.append("total: " + roundNsToMs(total) + " ms");

		return sb.toString();
	}


	public static String roundNsToMs(long ns) {
		return String.format("%.2f", ns/1000000D);
	}

}
