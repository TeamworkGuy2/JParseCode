package test;

import static test.ParserTestUtils.parseTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.val;

import org.junit.Test;

import parser.condition.CharConditions;
import parser.condition.ConditionPipeFilter;
import parser.condition.ParserCondition;
import parser.condition.StringConditions;

/**
 * @author TeamworkGuy2
 * @since 2015-11-29
 */
public class ConditionPipeFilterTest {

	@Test
	public void allrequiredTest() throws IOException {
		val name = "AllRequired";
		val condSet0 = new ArrayList<ParserCondition>(Arrays.asList(
				CharConditions.startCharFactory().create(new char[] { '<' }),
				CharConditions.endCharFactory().create(new char[] { '>' })
		));

		val condSet1 = new ArrayList<ParserCondition>(Arrays.asList(
				StringConditions.stringLiteralFactory().create("test")
		));

		val condSet2 = new ArrayList<ParserCondition>(Arrays.asList(
				StringConditions.endStringFactory().create("!")
		));

		val condSets = new ArrayList<List<ParserCondition>>(Arrays.asList(
				condSet0,
				condSet1,
				condSet2
		));

		ConditionPipeFilter.BasePipeFilter<ParserCondition> pipeCond = new ConditionPipeFilter.PipeAllRequiredFilter<ParserCondition>(condSets);
		// TODO testing ConditionPipeFilter.setupPipeAllRequiredFilter(pipeCond);

		parseTest(false, false, name, pipeCond, "<abc>");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>test");

		parseTest(false, true, name, pipeCond.copyOrReuse(), "<abc>te;");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>test;");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>test stuff!");
	}


	@Test
	public void optionalSuffixTest() throws IOException {
		val name = "OptionalSuffix";
		val condSet0 = new ArrayList<ParserCondition>(Arrays.asList(
				CharConditions.startCharFactory().create(new char[] { '<' }),
				CharConditions.endCharFactory().create(new char[] { '>' })
		));

		val condSetOptional = new ArrayList<ParserCondition>(Arrays.asList(
				StringConditions.stringLiteralFactory().create("test")
		));

		val condSets = new ArrayList<List<ParserCondition>>(Arrays.asList(
				condSet0,
				condSetOptional
		));

		ConditionPipeFilter.BasePipeFilter<ParserCondition> pipeCond = new ConditionPipeFilter.PipeOptionalSuffixFilter<ParserCondition>(condSets);
		// TODO testing ConditionPipeFilter.setupPipeOptionalSuffixFilter(pipeCond);

		parseTest(true, false, name, pipeCond, "<abc>");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>te");

		parseTest(false, true, name, pipeCond.copyOrReuse(), "<abc>te;");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>test");
	}


	@Test
	public void repeatableSeparatedText() {
		val name = "RepeatableSeparator";
		val condSet0 = new ArrayList<ParserCondition>(Arrays.asList(
				CharConditions.startCharFactory().create(new char[] { '<' }),
				CharConditions.endCharFactory().create(new char[] { '>' })
		));

		val condSetOptional = new ArrayList<ParserCondition>(Arrays.asList(
				StringConditions.stringLiteralFactory().create(", ")
		));

		val condSets = new ArrayList<List<ParserCondition>>(Arrays.asList(
				condSet0,
				condSetOptional
		));

		ConditionPipeFilter.BasePipeFilter<ParserCondition> pipeCond = new ConditionPipeFilter.PipeRepeatableSeparatorFilter<ParserCondition>(condSets);
		// TODO testing ConditionPipeFilter.setupPipeRepeatableSeparatorFilter(pipeCond);

		parseTest(true, false, name, pipeCond, "<abc>");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>,");

		parseTest(false, true, name, pipeCond.copyOrReuse(), "<abc>,;");

		parseTest(false, false, name, pipeCond.copyOrReuse(), "<abc>, ");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>, <test>");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>, <test>, <1>");

		parseTest(true, false, name, pipeCond.copyOrReuse(), "<abc>, <test>, <1>, <this works!>");
	}

}
