package twg2.parser.test;

import static twg2.parser.test.ParserTestUtils.parseTestSameParsed;

import org.junit.Test;

import twg2.parser.condition.text.CharParser;
import twg2.parser.tokenizers.GenericTypeTokenizer;
import twg2.parser.tokenizers.IdentifierTokenizer;

/**
 * @author TeamworkGuy2
 * @since 2016-2-6
 */
public class TypeParserTest {

	@Test
	public void identifierWithGenericTypeParse() {
		String name = "IdentifierWithGenericTypeParse";
		CharParser cond = GenericTypeTokenizer.createGenericTypeTokenizer(3, IdentifierTokenizer::createCompoundIdentifierTokenizer);

		parseTestSameParsed(false, false, name, cond, "thing<");
		parseTestSameParsed(false, true, name, cond, "thing<,>");
		parseTestSameParsed(false, true, name, cond, "thing<abc, >");
		parseTestSameParsed(true, false, name, cond, "thing<abc>");
		parseTestSameParsed(false, true, name, cond, "1t<abc>");
		parseTestSameParsed(true, false, name, cond, "t1_a2c<abc1>");
		parseTestSameParsed(true, false, name, cond, "thing<abc, _def>");
		parseTestSameParsed(true, false, name, cond, "thing<abc, _def<sub>>");
		parseTestSameParsed(true, false, name, cond, "thing<abc, _def<sub, wub, tub>>");
		parseTestSameParsed(true, false, name, cond, "Modified<A, B>");
		parseTestSameParsed(true, false, name, cond, "Result<IDictionary<AaInfo, IList<BbInfo>>>");
	}


	@Test
	public void identifierWithArrayDimensions() {
		String name = "IdentifierWithArrayDimensions";
		CharParser cond = GenericTypeTokenizer.createGenericTypeTokenizer(3, IdentifierTokenizer::createCompoundIdentifierTokenizer);

		parseTestSameParsed(false, false, name, cond, "thing<abc>[");
		parseTestSameParsed(false, true, name, cond, "thing<abc>[a");
		parseTestSameParsed(false, true, name, cond, "thing<abc>[a");
		parseTestSameParsed(true, false, name, cond, "thing<abc>[]");
		parseTestSameParsed(true, false, name, cond, "thing<abc>[][]");
		parseTestSameParsed(true, false, name, cond, "thing<abc[]>");
		parseTestSameParsed(true, false, name, cond, "thing<abc[]>[][]");
		parseTestSameParsed(true, false, name, cond, "thing[]");
	}

}
