package twg2.parser.text;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import twg2.collections.dataStructures.PairList;
import twg2.collections.tuple.Tuples;
import twg2.parser.condition.text.CharParser;
import twg2.parser.textFragment.TextConsumer;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textParser.TextParser;

/**
 * @author TeamworkGuy2
 * @since 2015-5-29
 */
public class CharMultiConditionParser {
	private PairList<CharParserFactory, TextConsumer> conditions = new PairList<>();
	private PairList<CharParserFactory, Entry<CharParser, TextConsumer>> curMatches = new PairList<>();


	public CharMultiConditionParser() {
		this.reset();
	}


	@SafeVarargs
	public CharMultiConditionParser(Entry<CharParserFactory, TextConsumer>... conditions) {
		for(Entry<CharParserFactory, TextConsumer> cond : conditions) {
			this.conditions.add(cond);
		}
		this.reset();
	}


	public CharMultiConditionParser(Collection<? extends Entry<CharParserFactory, TextConsumer>> conditions) {
		for(Entry<CharParserFactory, TextConsumer> cond : conditions) {
			this.conditions.add(cond);
		}
		this.reset();
	}


	public boolean acceptNext(char ch, TextParser buf) {
		// add conditions that match
		for(int i = 0, size = conditions.size(); i < size; i++) {
			CharParserFactory cond = conditions.getKey(i);

			// as possible tokens are encountered (based on one char), add them to the list of current matches
			if(cond.isMatch(ch, buf)) {
				if(cond.isCompound() || !curMatches.containsKey(cond)) {
					CharParser parserCond = cond.createParser();
					curMatches.add(cond, Tuples.of(parserCond, conditions.getValue(i)));
				}
			}
		}

		// for each matching parser, check if it accepts the next token, if so, keep it,
		// else remove it from the current set of matching parsers
		// IMPORTANT: we loop backward so that more recently started parser can consume input first (this ensures that things like matching quote or parentheses are matched in order)
		for(int i = curMatches.size() - 1; i > -1; i--) {
			CharParserFactory preCond = curMatches.getKey(i);
			Entry<CharParser, TextConsumer> condEntry = curMatches.getValue(i);
			CharParser cond = condEntry.getKey();

			cond.acceptNext(ch, buf);

			boolean complete = cond.isComplete();
			boolean failed = cond.isFailed();

			if(complete || failed) {
				// call the consumer/listener when the token is done being parsed AND no in-flight conditions are compound
				// (a non-compound conditions that started parsing before this condition may or may not complete successfully)
				// OR there are no other conditions being parsed
				if(complete) {
					if(curMatches.size() == 1 || allCompound(curMatches.keyList(), 0, i)) {
						CharSequence text = cond.getParserDestination();
						TextFragmentRef frag = cond.getCompleteMatchedTextCoords();

						// when a non-compound token finishes parsing, remove all other parsers that started parsing between the start and end point of the completed token
						// TODO note: this does not prevent parsers from starting and completing tokens within the span of a non-compound token,
						// for example ', ' in Map<String, String>
						if(!preCond.isCompound()) {
							int removeCount = curMatches.size() - i - 1;
							int ii = removeCount;
							while(ii > 0) {
								curMatches.removeIndex(i + ii);
								ii--;
							}
						}

						int off = frag.getOffsetStart();
						condEntry.getValue().accept(text, off, frag.getOffsetEnd() - off, frag.getLineStart(), frag.getColumnStart(), frag.getLineEnd(), frag.getColumnEnd()); // +1 because the current character was the last match
					}

					curMatches.removeIndex(i);
					// IMPORTANT: this ensures that a character can only be used to complete 1 token
					break;
				}
				curMatches.removeIndex(i);
			}
		}
		return false;
	}


	private void reset() {
		this.curMatches.clear();
	}


	private static boolean allCompound(List<CharParserFactory> conds, int off, int len) {
		if(len < 1) {
			return true;
		}
		for(int i = off, size = off + len; i < size; i++) {
			if(!conds.get(i).isCompound()) {
				return false;
			}
		}
		return true;
	}

}
