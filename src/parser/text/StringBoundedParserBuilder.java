package parser.text;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import parser.Inclusion;
import twg2.collections.primitiveCollections.CharArrayList;

/**
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class StringBoundedParserBuilder {
	private List<CharParserCondition> filters;
	private boolean compound;


	public StringBoundedParserBuilder() {
		this.filters = new ArrayList<>();
	}


	public CharPrecondition build() {
		return new CharPreconditionImpl<CharParserCondition>(compound, filters.toArray(new CharParserCondition[filters.size()]));
	}


	public StringBoundedParserBuilder addStartEndMarkers(String start, String end, Inclusion includeEnd) {
		val startFilter = StringConditions.startStringFactory().create(start, Inclusion.INCLUDE);
		val endFilter = StringConditions.endStringFactory().create(end, includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(String start, char end, Inclusion includeEnd) {
		val startFilter = StringConditions.startStringFactory().create(start, Inclusion.INCLUDE);
		val endFilter = CharConditions.endCharFactory().create(end, includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(char start, String end, Inclusion includeEnd) {
		val startFilter = CharConditions.startCharFactory().create(start, Inclusion.INCLUDE);
		val endFilter = StringConditions.endStringFactory().create(end, includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(char start, char end, Inclusion includeEnd) {
		val startFilter = CharConditions.startCharFactory().create(start, Inclusion.INCLUDE);
		val endFilter = CharConditions.endCharFactory().create(end, includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndNotPrecededByMarkers(char start, char notPreced, char end, Inclusion includeEnd) {
		val startFilter = CharConditions.startCharFactory().create(start, Inclusion.INCLUDE);
		val endFilter = CharConditions.endCharNotPrecededByFactory().create(CharArrayList.of(notPreced), end, includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(startFilter, endFilter));
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


	public StringBoundedParserBuilder addConditionMarker(CharParserCondition condition) {
		this.filters.add(condition);
		return this;
	}


	public StringBoundedParserBuilder isCompound(boolean compound) {
		this.compound = compound;
		return this;
	}

}
