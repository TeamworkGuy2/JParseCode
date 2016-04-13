package twg2.parser.test;

import static twg2.parser.test.ParserTestUtils.parseTestSameParsed;

import java.util.List;

import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.parser.codeParser.csharp.CsClassParser;
import twg2.parser.condition.text.CharParser;

/**
 * @author TeamworkGuy2
 * @since 2016-2-7
 */
public class ParseAnnotationTest {

	@Test
	public void annotationParseTest() {
		String name = "AnnotationParse";
		CharParser cond = CsClassParser.createAnnotationParser().createParser();

		parseTestSameParsed(false, false, name, cond, "[");
		parseTestSameParsed(false, true, name, cond, "]");
		parseTestSameParsed(false, false, name, cond, "[]");
		parseTestSameParsed(true, false, name, cond, "[A]");
	}


	public static void assertAnnotation(List<AnnotationSig> sigs, int idx, String name, String[] argNames, String... argValues) {
		Assert.assertTrue("there are only " + sigs.size() + " '" + name + "' annotations, cannot check index " + idx, idx < sigs.size());
		assertAnnotation(sigs.get(idx), name, argNames, argValues);
	}


	public static void assertAnnotation(AnnotationSig sig, String name, String[] argNames, String... argValues) {
		Assert.assertEquals(name, sig.getName());

		if((argNames == null && argValues == null) || (argNames.length == 0 && argValues.length == 0)) {
			return;
		}

		Assert.assertTrue("annotation '" + name + "' argument names null", argNames != null);
		Assert.assertTrue("annotation '" + name + "' argument values null", argValues != null);

		Assert.assertTrue("annotation '" + name + "' argument names " + argNames.length + " and values " + argValues.length + " lengths must be equal", argNames.length == argValues.length);

		val sigArgs = sig.getArguments();

		for(int i = 0, size = argNames.length; i < size; i++) {
			val argName = argNames[i];
			val argVal = argValues[i];
			Assert.assertTrue("annotation '" + name + "' missing name '" + argName + "' found " + sigArgs.keySet(), sigArgs.containsKey(argName));
			Assert.assertEquals("annotation '" + name + "' mismatching value '" + argVal + "' vs '" + sigArgs.get(argName) + "'", argVal, sigArgs.get(argName));
		}
	}

}
