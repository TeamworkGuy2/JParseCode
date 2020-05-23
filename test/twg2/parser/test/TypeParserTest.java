package twg2.parser.test;

import static twg2.text.tokenizer.test.ParserTestUtils.parseTest;

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
	public void compoundIdentifierParse() {
		String name = "IdentifierWithGenericTypeParse";
		CharParser cond = GenericTypeTokenizer.createGenericTypeTokenizer(3, IdentifierTokenizer::createCompoundIdentifierTokenizer);

		parseTest(false, true, name, cond, "1t<abc>");
		parseTest(false, false, name, cond, "thing<", "thing");

		parseTest(true, false, name, cond, "thing<,>", "thing");
		parseTest(true, false, name, cond, "thing<abc, >", "thing");
		parseTest(true, false, name, cond, "thing<abc>");
		parseTest(true, false, name, cond, "t_a2c<abc1>");
		parseTest(true, false, name, cond, "thing<abc, _def>");
		parseTest(true, false, name, cond, "thing<abc, _def<sub>>");
		parseTest(true, false, name, cond, "thing<abc, _def<sub, wub, tub>>");
		parseTest(true, false, name, cond, "Modified<A, B>");
		parseTest(true, false, name, cond, "IList<int?>");
		parseTest(true, false, name, cond, "Result<IDictionary<AaInfo, IList<BbInfo>>>");
	}


	@Test
	public void identifierParse() {
		String name = "IdentifierWithArrayDimensions";
		CharParser cond = GenericTypeTokenizer.createGenericTypeTokenizer(3, IdentifierTokenizer::createIdentifierTokenizer);

		parseTest(false, false, name, cond, "thing<abc>[");

		parseTest(true, false, name, cond, "thing<abc>[a", "thing<abc>");
		parseTest(true, false, name, cond, "thing<abc>[]");
		parseTest(true, false, name, cond, "thing<abc>[][]");
		parseTest(true, false, name, cond, "thing<abc[]>");
		parseTest(true, false, name, cond, "thing<abc[]>[][]");
		parseTest(true, false, name, cond, "thing[]");
	}

}
