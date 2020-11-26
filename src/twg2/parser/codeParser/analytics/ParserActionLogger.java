package twg2.parser.codeParser.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import twg2.io.files.FileUtil;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.condition.text.CharParser;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;
import twg2.text.stringUtils.StringPad;
import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.CharParserMatchableFactory;
import twg2.text.tokenizer.analytics.TokenizationLogger;

/**
 * @author TeamworkGuy2
 * @since 2016-09-11
 */
public class ParserActionLogger implements TokenizationLogger, JsonWritableSig {
	public int countCompoundCharParserMatch;
	public int countCompoundCharParserAcceptNext;
	public int countCreateParser;
	public int countTextFragmentsConsumed;
	public int totalParserReuseCount;
	public List<CharParserMatchableFactory.Reusable<CharParser>> reusableParserFactories;


	public ParserActionLogger() {
	}


	@Override
	public void logCountCompoundCharParserMatch(int count) {
		countCompoundCharParserMatch += count;
	}


	@Override
	public void logCountCompoundCharParserAcceptNext(int count) {
		countCompoundCharParserAcceptNext += count;
	}


	@Override
	public void logCountCreateParser(int count) {
		countCreateParser += count;
	}


	@Override
	public void logCountTextFragmentsConsumed(int count) {
		countTextFragmentsConsumed += count;
	}


	public void logCharParserFactoryReuse(Collection<? extends CharParserFactory> charParserFactories) {
		this.reusableParserFactories = new ArrayList<>();
		
		for(var parserFactory : charParserFactories) {
			if(parserFactory instanceof CharParserMatchableFactory.Reusable) {
				@SuppressWarnings("unchecked")
				var parserFactoryReusable = (CharParserMatchableFactory.Reusable<CharParser>)parserFactory;
				reusableParserFactories.add(parserFactoryReusable);
				totalParserReuseCount += parserFactoryReusable.getReuseCount();
			}
		}
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		toJson(null, false, dst, st);
	}


	public void toJson(String srcName, boolean includeSurroundingBrackets, Appendable dst, WriteSettings st) throws IOException {
		if(includeSurroundingBrackets) { dst.append("{\n"); }
		if(srcName != null) {
			dst.append("\t\"file\": \"");
			StringEscapeJson.toJsonString(srcName, dst);
			dst.append("\"");
		}

		if(countCompoundCharParserMatch > 0) {
			dst.append("\t\"compoundCharParserMatch\": ").append(Integer.toString(countCompoundCharParserMatch));
		}
		if(countCompoundCharParserAcceptNext > 0) {
			dst.append(",\n");
			dst.append("\t\"compoundCharParserAcceptNext\": ").append(Integer.toString(countCompoundCharParserAcceptNext));
		}
		if(countCreateParser > 0) {
			dst.append(",\n");
			dst.append("\t\"createParser\": ").append(Integer.toString(countCreateParser));
		}
		if(countTextFragmentsConsumed > 0) {
			dst.append(",\n");
			dst.append("\t\"textFragmentsConsumed\": ").append(Integer.toString(countTextFragmentsConsumed));
		}

		if(includeSurroundingBrackets) { dst.append("\n}"); }
	}


	@Override
	public String toString() {
		return toString(null, true);
	}


	public String toString(String srcName, boolean includeClassName) {
		var dst = new StringBuilder();
		if(includeClassName) {
			dst.append("parserSteps: {\n");
		}
		if(srcName != null) {
			dst.append("file: " + srcName);
		}

		if(countCompoundCharParserMatch > 0) {
			dst.append("compoundCharParserMatch: ").append(Integer.toString(countCompoundCharParserMatch));
		}
		if(countCompoundCharParserAcceptNext > 0) {
			dst.append(", ");
			dst.append("compoundCharParserAcceptNext: ").append(Integer.toString(countCompoundCharParserAcceptNext));
		}
		if(countCreateParser > 0) {
			dst.append(", ");
			dst.append("createParser: ").append(Integer.toString(countCreateParser));
		}
		if(countTextFragmentsConsumed > 0) {
			dst.append(", ");
			dst.append("textFragmentsConsumed: ").append(Integer.toString(countTextFragmentsConsumed));
		}

		if(includeClassName) {
			dst.append("\n}");
		}
		return dst.toString();
	}


	public static void toJsons(Map<String, ParserActionLogger> fileParserDetails, boolean includeSurroundingBrackets, Appendable dst, WriteSettings st) throws IOException {
		if(includeSurroundingBrackets) { dst.append("[\n"); }

		JsonStringify.inst.joinConsume(fileParserDetails.entrySet(), ",\n", dst, (entry) -> {
			var stat = entry.getValue();
			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());
			stat.toJson(fileName, includeSurroundingBrackets, dst, st);
		});

		if(includeSurroundingBrackets) { dst.append("\n]"); }
	}


	public static String toStrings(Map<String, ParserActionLogger> fileParserDetails) {
		var sb = new StringBuilder();

		for(var entry : fileParserDetails.entrySet()) {
			var stat = entry.getValue();
			String fileName = FileUtil.getFileNameWithoutExtension(entry.getKey());
			fileName = '"' + fileName.substring(0, Math.min(30, fileName.length())) + '"';
			fileName = StringPad.padRight(fileName, 32, ' ');

			sb.append(stat.toString(fileName, true)).append("\n");
		}

		return sb.toString();
	}

}
