package twg2.parser.codeParser.analytics;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import lombok.val;
import twg2.io.files.FileUtil;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;
import twg2.text.stringUtils.StringCase;
import twg2.text.stringUtils.StringPad;
import twg2.text.tokenizer.analytics.ParserAction;
import twg2.text.tokenizer.analytics.TypedLogger;

/**
 * @author TeamworkGuy2
 * @since 2016-09-11
 */
public class TokenizeStepLogger implements TypedLogger<ParserAction, WriteSettings>, JsonWritableSig {

	static class TypedAction {
		String msg;
		long count;
		double durationMilliseconds;


		@Override
		public String toString() {
			return (this.msg != null ? "msg: " + this.msg + ", " : "") +
					(this.count != 0 ? "count: " + this.count + ", " : "") +
					(this.durationMilliseconds != 0 ? "durationMillis: " + this.durationMilliseconds : "");
		}


		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			val json = JsonStringify.inst;

			json.append("{ ", dst);

			if(this.msg != null) {
				json.comma(dst).toProp("message", this.msg, dst);
			}

			if(this.count != 0) {
				json.comma(dst).toProp("count", this.count, dst);
			}

			if(this.durationMilliseconds != 0) {
				json.comma(dst).toProp("durationMillis", this.durationMilliseconds, dst);
			}

			json.append(" }", dst);
		}

	}


	Map<ParserAction, TypedAction> actions;


	public TokenizeStepLogger() {
		this.actions = new EnumMap<>(ParserAction.class);
	}


	public TypedAction getLog(ParserAction action) {
		return this.actions.get(action);
	}


	public String getLogMsg(ParserAction action) {
		val res = this.actions.get(action);
		return res != null ? res.msg : null;
	}


	public long getLogCount(ParserAction action) {
		val res = this.actions.get(action);
		return res != null ? res.count : 0;
	}


	public double getLogDuration(ParserAction action) {
		val res = this.actions.get(action);
		return res != null ? res.durationMilliseconds : 0;
	}


	@Override
	public void logMsg(ParserAction action, String msg) {
		getTypedAction(action).msg = msg;
	}


	@Override
	public void logCount(ParserAction action, long count) {
		getTypedAction(action).count += count;
	}


	@Override
	public void logDuration(ParserAction action, double durationMilliseconds) {
		getTypedAction(action).durationMilliseconds += durationMilliseconds;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		toJson(null, false, dst, st);
	}


	@Override
	public void toJson(String srcName, boolean includeSurroundingBrackets, Appendable dst, WriteSettings st) throws IOException {
		if(includeSurroundingBrackets) { dst.append("{\n"); }
		if(srcName != null) {
			dst.append("\t\"file\": \"");
			StringEscapeJson.toJsonString(srcName, dst);
			dst.append("\"");
		}

		boolean first = true;
		for(ParserAction action : ParserAction.values()) {
			val data = this.actions.get(action);
			if(data != null) {
				if(!first || srcName != null) { dst.append(",\n"); }
				val name = StringCase.toCamelCase(action.name());
				dst.append("\t\"").append(name).append("\": ");
				data.toJson(dst, st);
				first = false;
			}
		}

		if(includeSurroundingBrackets) { dst.append("\n}"); }
	}


	@Override
	public String toString() {
		return toString(null, true);
	}


	@Override
	public String toString(String srcName, boolean includeClassName) {
		val dst = new StringBuilder();
		if(includeClassName) {
			dst.append("parserSteps: {\n");
		}
		if(srcName != null) {
			dst.append("file: " + srcName);
		}

		boolean first = true;
		for(ParserAction action : ParserAction.values()) {
			val data = this.actions.get(action);
			if(data != null) {
				if(!first || srcName != null) { dst.append(",\n"); }
				val name = StringCase.toCamelCase(action.name());
				dst.append("\"").append(name).append("\": ").append(data.toString());
				first = false;
			}
		}
		if(includeClassName) {
			dst.append("\n}");
		}
		return dst.toString();
	}


	public static void toJsons(Map<String, TokenizeStepLogger> fileParserDetails, boolean includeSurroundingBrackets, Appendable dst, WriteSettings st) throws IOException {
		if(includeSurroundingBrackets) { dst.append("[\n"); }
		JsonStringify.inst.joinConsume(fileParserDetails.entrySet(), ",\n", dst, (entry) -> {
			val stat = entry.getValue();
			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());
			stat.toJson(fileName, includeSurroundingBrackets, dst, st);
		});
		if(includeSurroundingBrackets) { dst.append("\n]"); }
	}


	public static String toStrings(Map<String, TokenizeStepLogger> fileParserDetails) {
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


	private TypedAction getTypedAction(ParserAction action) {
		TypedAction data = actions.get(action);
		if(data == null) {
			data = new TypedAction();
			actions.put(action, data);
		}
		return data;
	}

}
