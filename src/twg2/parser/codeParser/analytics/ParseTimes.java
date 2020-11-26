package twg2.parser.codeParser.analytics;

import java.io.IOException;
import java.util.Map;

import twg2.io.files.FileUtil;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringPad;

/**
 * @author TeamworkGuy2
 * @since 2016-09-07
 */
public class ParseTimes implements JsonWritableSig {
	long setupNs;
	long readNs;
	long tokenizeNs;
	long extractAstNs;


	public long getSetupNs() {
		return setupNs;
	}


	public long getReadNs() {
		return readNs;
	}


	public long getTokenizeNs() {
		return tokenizeNs;
	}


	public long getExtractAstNs() {
		return extractAstNs;
	}


	public long getTotalNs() {
		return setupNs + readNs + tokenizeNs + extractAstNs;
	}


	public void setTimeSetup(long setupNanos) {
		this.setupNs = setupNanos;
	}


	public void setTimeRead(long readNanos) {
		this.readNs = readNanos;
	}


	public void setTimeTokenize(long tokenizeNanos) {
		this.tokenizeNs = tokenizeNanos;
	}


	public void setTimeExtractAst(long extractAstNanos) {
		this.extractAstNs = extractAstNanos;
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
				"\"read\": " + roundNsToMs(this.readNs) + ", " +
				"\"tokenize\": " + roundNsToMs(this.tokenizeNs) + ", " +
				"\"extractAst\": " + roundNsToMs(this.extractAstNs) + ", " +
				"\"units\": \"milliseconds\""
			);
		if(includeSurroundingBrackets) { dst.append(" }"); }
	}


	@Override
	public String toString() {
		return toString(null, true);
	}


	public String toString(String srcName, boolean includeClassName) {
		return (includeClassName ? "parseTimes: { " : "") +
				(srcName != null ? ("file: " + srcName + ", ") : "") +
				"setup: " + roundNsToMs(this.setupNs) + ", " +
				"read: " + roundNsToMs(this.readNs) + ", " +
				"tokenize: " + roundNsToMs(this.tokenizeNs) + ", " +
				"extractAst: " + roundNsToMs(this.extractAstNs) + ", " +
				"total: " + roundNsToMs(this.getTotalNs()) +
			(includeClassName ? " }" : "");
	}


	public static void toJsons(Map<String, ParseTimes> fileParseTimes, boolean includeSurroundingBrackets, Appendable dst, WriteSettings st) throws IOException {
		if(includeSurroundingBrackets) { dst.append("[\n"); }
		JsonStringify.inst.joinConsume(fileParseTimes.entrySet(), ",\n", dst, (entry) -> {
			var stat = entry.getValue();
			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());
			stat.toJson(fileName, includeSurroundingBrackets, dst, st);
		});
		if(includeSurroundingBrackets) { dst.append("\n]"); }
	}


	public static String toStrings(Map<String, ParseTimes> fileParseTimes) {
		long total = 0;
		var sb = new StringBuilder();

		for(var entry : fileParseTimes.entrySet()) {
			var stat = entry.getValue();
			total += stat.getTotalNs();

			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());
			fileName = '"' + fileName.substring(0, Math.min(30, fileName.length())) + '"';
			fileName = StringPad.padRight(fileName, 32, ' ');

			sb.append(stat.toString(fileName, true) + "\n");
		}

		sb.append("total: " + roundNsToMs(total) + " ms");

		return sb.toString();
	}


	public static String roundNsToMs(long nanos) {
		return String.format("%.2f", nanos / 1000000D);
	}


	public static String roundNsToMs(long nanos, int decimalPlaces) {
		return String.format("%." + decimalPlaces + "f", nanos / 1000000D);
	}

}
