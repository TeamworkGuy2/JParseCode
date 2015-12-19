package twg2.parser.test;

import static twg2.parser.test.ParserTestUtils.parseTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.val;

import org.junit.Test;

import twg2.parser.text.CharConditionPipe;
import twg2.parser.text.CharConditions;
import twg2.parser.text.CharParserCondition;
import twg2.parser.text.StringConditions;

/**
 * @author TeamworkGuy2
 * @since 2015-11-29
 */
public class CharConditionPipeFilterTest {

	@Test
	public void allrequiredTest() throws IOException {
		val name = "AllRequired";
		val condSet0 = new ArrayList<CharParserCondition>(Arrays.asList(
				CharConditions.startCharFactory().create(new char[] { '<' }),
				CharConditions.endCharFactory().create(new char[] { '>' })
		));

		val condSet1 = new ArrayList<CharParserCondition>(Arrays.asList(
				StringConditions.stringLiteralFactory().create("test")
		));

		val condSet2 = new ArrayList<CharParserCondition>(Arrays.asList(
				StringConditions.endStringFactory().create("!")
		));

		val condSets = new ArrayList<List<CharParserCondition>>(Arrays.asList(
				condSet0,
				condSet1,
				condSet2
		));

		CharConditionPipe.BasePipe<CharParserCondition> pipeCond = new CharConditionPipe.AllRequired<CharParserCondition>(condSets);
		// TODO testing CharConditionPipe.setupPipeAllRequiredFilter(pipeCond);

		parseTest(false, false, name, pipeCond, "<abc>");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>test");

		parseTest(false, true, name, pipeCond.copyOrReuse(), "<abc>te;");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>test;");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>test stuff!");
	}


	@Test
	public void optionalSuffixTest() throws IOException {
		val name = "OptionalSuffix";
		val condSet0 = new ArrayList<CharParserCondition>(Arrays.asList(
				CharConditions.startCharFactory().create(new char[] { '<' }),
				CharConditions.endCharFactory().create(new char[] { '>' })
		));

		val condSetOptional = new ArrayList<CharParserCondition>(Arrays.asList(
				StringConditions.stringLiteralFactory().create("test")
		));

		val condSets = new ArrayList<List<CharParserCondition>>(Arrays.asList(
				condSet0,
				condSetOptional
		));

		CharConditionPipe.BasePipe<CharParserCondition> pipeCond = new CharConditionPipe.OptionalSuffix<CharParserCondition>(condSets);
		// TODO testing CharConditionPipe.setupPipeOptionalSuffixFilter(pipeCond);

		parseTest(true, false, name, pipeCond, "<abc>");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>te");

		parseTest(false, true, name, pipeCond.copyOrReuse(), "<abc>te;");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>test");
	}


	@Test
	public void repeatableSeparatedText() {
		val name = "RepeatableSeparator";
		val condSet0 = new ArrayList<CharParserCondition>(Arrays.asList(
				CharConditions.startCharFactory().create(new char[] { '<' }),
				CharConditions.endCharFactory().create(new char[] { '>' })
		));

		val condSetOptional = new ArrayList<CharParserCondition>(Arrays.asList(
				StringConditions.stringLiteralFactory().create(", ")
		));

		val condSets = new ArrayList<List<CharParserCondition>>(Arrays.asList(
				condSet0,
				condSetOptional
		));

		CharConditionPipe.BasePipe<CharParserCondition> pipeCond = new CharConditionPipe.RepeatableSeparator<CharParserCondition>(condSets);
		// TODO testing CharConditionPipe.setupPipeRepeatableSeparatorFilter(pipeCond);

		parseTest(true, false, name, pipeCond, "<abc>");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>,");

		parseTest(false, true, name, pipeCond.copyOrReuse(), "<abc>,;");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>, ");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>, <test>");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>, <test>, <1>");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>, <test>, <1>, <this works!>");
	}

}
