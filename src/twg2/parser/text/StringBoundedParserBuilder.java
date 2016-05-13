package twg2.parser.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.val;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.collections.tuple.Tuples;
import twg2.functions.BiPredicates;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParser;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.parser.textParser.TextParser;

/**
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class StringBoundedParserBuilder {
	private List<Entry<BiPredicates.CharObject<TextParser>, CharParser>> filters;
	private boolean compound;
	private String name;


	public StringBoundedParserBuilder(String name) {
		this.filters = new ArrayList<>();
		this.name = name;
	}


	@SuppressWarnings("unchecked")
	public CharParserFactory build() {
		return new CharParserMatchableFactory<CharParser>(name, compound, filters.toArray(new Entry[filters.size()]));
	}


	public StringBoundedParserBuilder addStartEndMarkers(String name, String start, String end, Inclusion includeEnd) {
		val startFilter = new StringConditions.Start(name + "-start", new String[] { start }, Inclusion.INCLUDE);
		val endFilter = new StringConditions.End(name + "-end", new String[] { end }, includeEnd);
		val cond = new CharConditionPipe.AllRequired<>(name, startFilter, endFilter);
		this.filters.add(Tuples.of(cond.getFirstCharMatcher(), cond));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(String name, String start, char end, Inclusion includeEnd) {
		val startFilter = new StringConditions.Start(name + "-start", new String[] { start }, Inclusion.INCLUDE);
		val endFilter = new CharConditions.End(name + "-end", CharArrayList.of(end), includeEnd);
		val cond = new CharConditionPipe.AllRequired<>(name, startFilter, endFilter);
		this.filters.add(Tuples.of(cond.getFirstCharMatcher(), cond));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(String name, char start, String end, Inclusion includeEnd) {
		val startFilter = new CharConditions.Start(name + "-start", CharArrayList.of(start), Inclusion.INCLUDE);
		val endFilter = new StringConditions.End(name + "-end", new String[] { end }, includeEnd);
		val cond = new CharConditionPipe.AllRequired<>(name, startFilter, endFilter);
		this.filters.add(Tuples.of(cond.getFirstCharMatcher(), cond));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(String name, char start, char end, Inclusion includeEnd) {
		val startFilter = new CharConditions.Start(name + "-start", CharArrayList.of(start), Inclusion.INCLUDE);
		val endFilter = new CharConditions.End(name + "-end", CharArrayList.of(end), includeEnd);
		val cond = new CharConditionPipe.AllRequired<>(name, startFilter, endFilter);
		this.filters.add(Tuples.of(cond.getFirstCharMatcher(), cond));
		return this;
	}


	public StringBoundedParserBuilder addStartEndNotPrecededByMarkers(String name, char start, char notPreced, char end, Inclusion includeEnd) {
		return addStartEndNotPrecededByMarkers(name, start, notPreced, end, 0, includeEnd);
	}


	public StringBoundedParserBuilder addStartEndNotPrecededByMarkers(String name, char start, char notPreced, char end, int minPreEndChars, Inclusion includeEnd) {
		val startFilter = new CharConditions.Start(name + "-start", CharArrayList.of(start), Inclusion.INCLUDE);
		val endFilter = new CharConditions.EndNotPrecededBy(name + "-end", CharArrayList.of(end), minPreEndChars, includeEnd, CharArrayList.of(notPreced));
		val cond = new CharConditionPipe.AllRequired<>(name, startFilter, endFilter);
		this.filters.add(Tuples.of(cond.getFirstCharMatcher(), cond));
		return this;
	}


	public StringBoundedParserBuilder addStringLiteralMarker(String name, String str) {
		val cond = new StringConditions.Literal(name, new String[] { str }, Inclusion.INCLUDE);
		this.filters.add(Tuples.of(cond.getFirstCharMatcher(), cond));
		return this;
	}


	public StringBoundedParserBuilder addCharLiteralMarker(String name, char ch) {
		val cond = new CharConditions.Literal(name, CharArrayList.of(ch), Inclusion.INCLUDE);
		this.filters.add(Tuples.of(cond.getFirstCharMatcher(), cond));
		return this;
	}


	public StringBoundedParserBuilder addConditionMarker(CharParser condition, CharParserMatchable conditionFilter) {
		this.filters.add(Tuples.of(conditionFilter.getFirstCharMatcher(), condition));
		return this;
	}


	public StringBoundedParserBuilder isCompound(boolean compound) {
		this.compound = compound;
		return this;
	}

}
