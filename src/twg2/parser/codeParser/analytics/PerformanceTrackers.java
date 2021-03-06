package twg2.parser.codeParser.analytics;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import twg2.collections.builder.ListBuilder;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringPad;
import twg2.text.stringUtils.StringSplit;
import twg2.tuple.Tuple3;
import twg2.tuple.Tuples;

/**
 * @author TeamworkGuy2
 * @since 2016-09-11
 */
public class PerformanceTrackers implements JsonWritableSig {
	private final HashMap<String, Tuple3<ParseTimes, ParserActionLogger, Integer>> fileStats;


	public PerformanceTrackers() {
		this.fileStats = new HashMap<>();
	}


	public ParseTimes getOrCreateParseTimes(String srcName) {
		return getOrCreateParseStats(srcName, null).getValue0();
	}


	public ParserActionLogger getOrCreateParseActions(String srcName) {
		return getOrCreateParseStats(srcName, null).getValue1();
	}


	public void setSrcSize(String srcName, int fileSize) {
		synchronized(fileStats) {
			var stats = fileStats.get(srcName);
			var inst = (stats == null
					? Tuples.of(new ParseTimes(), new ParserActionLogger(), fileSize)
					: Tuples.of(stats.getValue0(), stats.getValue1(), fileSize));
			fileStats.put(srcName, inst);
		}
	}


	public Map<String, Tuple3<ParseTimes, ParserActionLogger, Integer>> getParseStats() {
		return this.fileStats;
	}


	public List<Entry<String, Tuple3<ParseTimes, ParserActionLogger, Integer>>> getTopParseTimes(boolean sortAscending, int size) {
		var list = ListBuilder.mutable(
			this.fileStats.entrySet().stream()
				.sorted(PerformanceTrackers.createParseTimesSorter(sortAscending)).iterator()
		);
		return (size < 0 ? list.subList(list.size() + size, list.size()) : list.subList(0, size));
	}


	public List<Entry<String, Tuple3<ParseTimes, ParserActionLogger, Integer>>> getTopParseActions(boolean sortAscending, int size) {
		var list = ListBuilder.mutable(
			this.fileStats.entrySet().stream()
				.sorted(PerformanceTrackers.createParseActionsSorter(sortAscending)).iterator()
		);
		return (size < 0 ? list.subList(list.size() + size, list.size()) : list.subList(0, size));
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		for(var stat : fileStats.entrySet()) {
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
		return toString(fileStats.entrySet());
	}


	private Tuple3<ParseTimes, ParserActionLogger, Integer> getOrCreateParseStats(String srcName, Integer fileSize) {
		synchronized(fileStats) {
			var stats = fileStats.get(srcName);
			if(stats == null) {
				stats = Tuples.of(new ParseTimes(), new ParserActionLogger(), fileSize);
				fileStats.put(srcName, stats);
			}
			return stats;
		}
	}


	public static String toString(Collection<Entry<String, Tuple3<ParseTimes, ParserActionLogger, Integer>>> parseStats) {
		var sb = new StringBuilder();

		for(var stat : parseStats) {
			var key = stat.getKey();
			var parseTimes = stat.getValue().getValue0();
			var parseActions = stat.getValue().getValue1();
			var fileSizeBytes = stat.getValue().getValue2();
			var fileName = StringPad.padRight(StringSplit.lastMatch(key, '\\'), 32, ' ');

			sb.append(fileName).append(" : ");
			sb.append("bytes: ").append(fileSizeBytes).append(", ");
			sb.append(parseTimes.toString(null, false));
			sb.append(", ");
			sb.append(parseActions.toString(null, false));
			sb.append('\n');
		}

		return sb.toString();
	}


	private static Comparator<Entry<String, Tuple3<ParseTimes, ParserActionLogger, Integer>>> createParseTimesSorter(boolean sortAscending) {
		if(sortAscending) {
			return (a, b) -> (int)(a.getValue().getValue0().getTotalNs() - b.getValue().getValue0().getTotalNs());
		}
		else {
			return (a, b) -> (int)(b.getValue().getValue0().getTotalNs() - a.getValue().getValue0().getTotalNs());
		}
	}


	private static Comparator<Entry<String, Tuple3<ParseTimes, ParserActionLogger, Integer>>> createParseActionsSorter(boolean sortAscending) {
		if(sortAscending) {
			return (a, b) -> (int)(a.getValue().getValue1().countCompoundCharParserMatch - b.getValue().getValue1().countCompoundCharParserMatch);
		}
		else {
			return (a, b) -> (int)(b.getValue().getValue1().countCompoundCharParserMatch - a.getValue().getValue1().countCompoundCharParserMatch);
		}
	}

}
