package twg2.parser.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.val;
import twg2.collections.builder.ListUtil;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.collections.tuple.Tuples;
import twg2.functions.BiPredicates;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParser;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.parser.textParser.TextParser;

/**
 * @author TeamworkGuy2
 * @since 2015-6-25
 */
public class StringParserBuilder {
	private List<CharParser> stringConds;
	private List<BiPredicates.CharObject<TextParser>> stringStartFilters;
	private String name;


	public StringParserBuilder(String name) {
		this.stringConds = new ArrayList<>();
		this.stringStartFilters = new ArrayList<>();
		this.name = name;
	}


	public CharParserFactory build() {
		@SuppressWarnings("unchecked")
		Entry<BiPredicates.CharObject<TextParser>, CharParser>[] conditions = ListUtil.combineArray(stringStartFilters, stringConds, Tuples::of, new Entry[stringConds.size()]);
		return new CharParserMatchableFactory<>(name, false, conditions);
	}


	public StringParserBuilder addStringLiteralMarker(String name, String str) {
		val cond = new StringConditions.Literal(name, new String[] { str }, Inclusion.INCLUDE);
		this.stringConds.add(cond);
		this.stringStartFilters.add(cond.getFirstCharMatcher());
		return this;
	}


	public StringParserBuilder addCharLiteralMarker(String name, char start) {
		val cond = new CharConditions.Literal(name, CharArrayList.of(start), Inclusion.INCLUDE);
		this.stringConds.add(cond);
		this.stringStartFilters.add(cond.getFirstCharMatcher());
		return this;
	}


	public StringParserBuilder addCharMatcher(String name, char[] chars) {
		val cond = new CharConditions.Contains(name, CharArrayList.of(chars), Inclusion.INCLUDE);
		this.stringConds.add(cond);
		this.stringStartFilters.add(cond.getFirstCharMatcher());
		return this;
	}


	public StringParserBuilder addConditionMatcher(CharParserMatchable conditionFilter) {
		this.stringConds.add(conditionFilter);
		this.stringStartFilters.add(conditionFilter.getFirstCharMatcher());
		return this;
	}


	public StringParserBuilder addConditionMatcher(CharParser condition, CharParserMatchable conditionFilter) {
		this.stringConds.add(condition);
		this.stringStartFilters.add(conditionFilter.getFirstCharMatcher());
		return this;
	}

}
