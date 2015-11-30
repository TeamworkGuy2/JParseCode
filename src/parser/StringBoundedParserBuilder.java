package parser;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import parser.condition.CharConditions;
import parser.condition.ConditionPipeFilter;
import parser.condition.ParserCondition;
import parser.condition.Precondition;
import parser.condition.PreconditionImpl;
import parser.condition.StringConditions;
import twg2.collections.primitiveCollections.CharArrayList;

/**
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class StringBoundedParserBuilder {
	private List<ParserCondition> filters;
	private boolean compound;


	public StringBoundedParserBuilder() {
		this.filters = new ArrayList<>();
	}


	public Precondition build() {
		return new PreconditionImpl<ParserCondition>(compound, filters.toArray(new ParserCondition[filters.size()]));
	}


	public StringBoundedParserBuilder addStartEndMarkers(String start, String end, Inclusion includeEnd) {
		val startFilter = StringConditions.startStringFactory().create(start, Inclusion.INCLUDE);
		val endFilter = StringConditions.endStringFactory().create(end, includeEnd);
		this.filters.add(new ConditionPipeFilter.PipeAllRequiredFilter<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(String start, char end, Inclusion includeEnd) {
		val startFilter = StringConditions.startStringFactory().create(start, Inclusion.INCLUDE);
		val endFilter = CharConditions.endCharFactory().create(end, includeEnd);
		this.filters.add(new ConditionPipeFilter.PipeAllRequiredFilter<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(char start, String end, Inclusion includeEnd) {
		val startFilter = CharConditions.startCharFactory().create(start, Inclusion.INCLUDE);
		val endFilter = StringConditions.endStringFactory().create(end, includeEnd);
		this.filters.add(new ConditionPipeFilter.PipeAllRequiredFilter<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(char start, char end, Inclusion includeEnd) {
		val startFilter = CharConditions.startCharFactory().create(start, Inclusion.INCLUDE);
		val endFilter = CharConditions.endCharFactory().create(end, includeEnd);
		this.filters.add(new ConditionPipeFilter.PipeAllRequiredFilter<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndNotPrecededByMarkers(char start, char notPreced, char end, Inclusion includeEnd) {
		val startFilter = CharConditions.startCharFactory().create(start, Inclusion.INCLUDE);
		val endFilter = CharConditions.endCharNotPrecededByFactory().create(CharArrayList.of(notPreced), end, includeEnd);
		this.filters.add(new ConditionPipeFilter.PipeAllRequiredFilter<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStringLiteralMarker(String str) {
		this.filters.add(StringConditions.stringLiteralFactory().create(str));
		return this;
	}


	public StringBoundedParserBuilder addCharLiteralMarker(char ch) {
		this.filters.add(CharConditions.charLiteralFactory().create(ch));
		return this;
	}


	public StringBoundedParserBuilder addConditionMarker(ParserCondition condition) {
		this.filters.add(condition);
		return this;
	}


	public StringBoundedParserBuilder isCompound(boolean compound) {
		this.compound = compound;
		return this;
	}

}
