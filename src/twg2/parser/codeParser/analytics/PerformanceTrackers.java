package twg2.parser.codeParser.analytics;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import twg2.collections.builder.ListBuilder;
import twg2.parser.codeParser.analytics.ParseTimes.TrackerAction;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringPad;
import twg2.text.stringUtils.StringSplit;
import twg2.text.tokenizer.analytics.ParserAction;
import twg2.tuple.Tuple3;
import twg2.tuple.Tuples;

/**
 * @author TeamworkGuy2
 * @since 2016-09-11
 */
public class PerformanceTrackers implements JsonWritableSig {
	private Function<String, ParseTimes> parseTimesFactory = (srcName) -> new ParseTimes();
	private Function<String, TokenizeStepLogger> stepDetailsFactory = (srcName) -> new TokenizeStepLogger();
	private HashMap<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>> fileParseStats = new HashMap<>();


	public void log(TrackerAction action, String srcName, long timeNanos) {
		var stat = getOrCreateParseStats(srcName, null);
		stat.getValue0().log(action, timeNanos);
	}


	public void log(ParserAction action, String srcName, long detail) {
		var stat = getOrCreateParseStats(srcName, null);
		stat.getValue1().logCount(action, detail);
	}


	public ParseTimes getOrCreateParseTimes(String srcName) {
		return getOrCreateParseStats(srcName, null).getValue0();
	}


	public TokenizeStepLogger getOrCreateStepDetails(String srcName) {
		return getOrCreateParseStats(srcName, null).getValue1();
	}


	public void setSrcSize(String srcName, int fileSize) {
		var stats = fileParseStats.get(srcName);
		var inst = (stats == null
				? Tuples.of(parseTimesFactory.apply(srcName), stepDetailsFactory.apply(srcName), fileSize)
				: Tuples.of(stats.getValue0(), stats.getValue1(), fileSize));
		fileParseStats.put(srcName, inst);
	}


	public Map<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>> getParseStats() {
		return this.fileParseStats;
	}


	public List<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> getTopParseTimes(boolean sortAscending, int size) {
		var list = ListBuilder.mutable(
			this.fileParseStats.entrySet().stream()
				.sorted(PerformanceTrackers.createParseTimesSorter(sortAscending)).iterator()
		);
		return (size < 0 ? list.subList(list.size() + size, list.size()) : list.subList(0, size));
	}


	public List<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> getTopParseStepDetails(boolean sortAscending, int size) {
		var list = ListBuilder.mutable(
			this.fileParseStats.entrySet().stream()
				.sorted(PerformanceTrackers.createParseStepDetailsSorter(sortAscending)).iterator()
		);
		return (size < 0 ? list.subList(list.size() + size, list.size()) : list.subList(0, size));
	}


	private Tuple3<ParseTimes, TokenizeStepLogger, Integer> getOrCreateParseStats(String srcName, Integer fileSize) {
		var stats = fileParseStats.get(srcName);
		if(stats == null) {
			var inst = Tuples.of(parseTimesFactory.apply(srcName), stepDetailsFactory.apply(srcName), fileSize);
			fileParseStats.put(srcName, inst);
			return inst;
		}
		return stats;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		for(var stat : fileParseStats.entrySet()) {
			dst.append("{ ");
			dst.append("\"file\": \"");
			dst.append(stat.getKey());
			dst.append("\", ");
			stat.getValue().getValue0().toJson(null, false, dst, st);
			dst.append(", ");
			stat.getValue().getValue1().toJson(null, false, dst, st);
			dst.append(", \"fileSize\" :");
			dst.append(stat.getValue().getValue2().toString());
			dst.append(" },\n");
		}
	}


	@Override
	public String toString() {
		return toString(fileParseStats.entrySet().iterator());
	}


	public static final String toString(Iterator<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> parseStatsIter) {
		var sb = new StringBuilder();
		while(parseStatsIter.hasNext()) {
			var stat = parseStatsIter.next();
			var key = stat.getKey();
			var parseTimes = stat.getValue().getValue0();
			var stepDetails = stat.getValue().getValue1();
			var fileName = StringPad.padRight(StringSplit.lastMatch(key, '\\'), 40, ' ');
			sb.append(fileName).append(" : ").append(parseTimes.toString(null, false));
			sb.append(", ");
			sb.append(stepDetails.toString(null, false));
			sb.append('\n');
		}
		return sb.toString();
	}


	private static final Comparator<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> createParseTimesSorter(boolean sortAscending) {
		if(sortAscending) {
			return (a, b) -> (int)(a.getValue().getValue0().getTotalNs() - b.getValue().getValue0().getTotalNs());
		}
		else {
			return (a, b) -> (int)(b.getValue().getValue0().getTotalNs() - a.getValue().getValue0().getTotalNs());
		}
	}


	private static final Comparator<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> createParseStepDetailsSorter(boolean sortAscending) {
		if(sortAscending) {
			return (a, b) -> (int)(a.getValue().getValue1().getLogCount(ParserAction.CHAR_CHECKS) - b.getValue().getValue1().getLogCount(ParserAction.CHAR_CHECKS));
		}
		else {
			return (a, b) -> (int)(b.getValue().getValue1().getLogCount(ParserAction.CHAR_CHECKS) - a.getValue().getValue1().getLogCount(ParserAction.CHAR_CHECKS));
		}
	}

}
