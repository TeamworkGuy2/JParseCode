package twg2.parser.codeParser.analytics;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.swing.SortOrder;

import lombok.val;
import twg2.collections.builder.ListBuilder;
import twg2.dataUtil.dataUtils.EnumError;
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
		val stat = getOrCreateParseStats(srcName, null);
		stat.getValue0().log(action, timeNanos);
	}


	public void log(ParserAction action, String srcName, long detail) {
		val stat = getOrCreateParseStats(srcName, null);
		stat.getValue1().logCount(action, detail);
	}


	public ParseTimes getOrCreateParseTimes(String srcName) {
		return getOrCreateParseStats(srcName, null).getValue0();
	}


	public TokenizeStepLogger getOrCreateStepDetails(String srcName) {
		return getOrCreateParseStats(srcName, null).getValue1();
	}


	public void setSrcSize(String srcName, int fileSize) {
		val stats = fileParseStats.get(srcName);
		val inst = (stats == null
				? Tuples.of(parseTimesFactory.apply(srcName), stepDetailsFactory.apply(srcName), fileSize)
				: Tuples.of(stats.getValue0(), stats.getValue1(), fileSize));
		fileParseStats.put(srcName, inst);
	}


	public Map<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>> getParseStats() {
		return this.fileParseStats;
	}


	public List<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> getTopParseTimes(SortOrder s, int size) {
		val list = ListBuilder.mutable(
			this.fileParseStats.entrySet().stream()
				.sorted(PerformanceTrackers.createParseTimesSorter(s)).iterator()
		);
		return (size < 0 ? list.subList(list.size() + size, list.size()) : list.subList(0, size));
	}


	public List<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> getTopParseStepDetails(SortOrder s, int size) {
		val list = ListBuilder.mutable(
			this.fileParseStats.entrySet().stream()
				.sorted(PerformanceTrackers.createParseStepDetailsSorter(s)).iterator()
		);
		return (size < 0 ? list.subList(list.size() + size, list.size()) : list.subList(0, size));
	}


	private Tuple3<ParseTimes, TokenizeStepLogger, Integer> getOrCreateParseStats(String srcName, Integer fileSize) {
		val stats = fileParseStats.get(srcName);
		if(stats == null) {
			val inst = Tuples.of(parseTimesFactory.apply(srcName), stepDetailsFactory.apply(srcName), fileSize);
			fileParseStats.put(srcName, inst);
			return inst;
		}
		return stats;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		for(val stat : fileParseStats.entrySet()) {
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
		val sb = new StringBuilder();
		while(parseStatsIter.hasNext()) {
			val stat = parseStatsIter.next();
			val key = stat.getKey();
			val parseTimes = stat.getValue().getValue0();
			val stepDetails = stat.getValue().getValue1();
			val fileName = StringPad.padRight(StringSplit.lastMatch(key, '\\'), 40, ' ');
			sb.append(fileName + " : ");
			sb.append(parseTimes.toString(null, false));
			sb.append(", ");
			sb.append(stepDetails.toString(null, false));
			sb.append('\n');
		}
		return sb.toString();
	}


	private static final Comparator<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> createParseTimesSorter(SortOrder s) {
		switch(s) {
		case ASCENDING:
			return (a, b) -> (int)(a.getValue().getValue0().getTotalNs() - b.getValue().getValue0().getTotalNs());
		case DESCENDING:
			return (a, b) -> (int)(b.getValue().getValue0().getTotalNs() - a.getValue().getValue0().getTotalNs());
		default:
			throw EnumError.unsupportedValue(s, SortOrder.class);
		}
	}


	private static final Comparator<Entry<String, Tuple3<ParseTimes, TokenizeStepLogger, Integer>>> createParseStepDetailsSorter(SortOrder s) {
		switch(s) {
		case ASCENDING:
			return (a, b) -> (int)(a.getValue().getValue1().getLogCount(ParserAction.CHAR_CHECKS) - b.getValue().getValue1().getLogCount(ParserAction.CHAR_CHECKS));
		case DESCENDING:
			return (a, b) -> (int)(b.getValue().getValue1().getLogCount(ParserAction.CHAR_CHECKS) - a.getValue().getValue1().getLogCount(ParserAction.CHAR_CHECKS));
		default:
			throw EnumError.unsupportedValue(s, SortOrder.class);
		}
	}

}
