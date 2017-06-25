package twg2.parser.test.utils;

import java.util.List;
import java.util.Map;

import org.junit.Assert;

import twg2.ast.interm.annotation.AnnotationSig;

/**
 * @author TeamworkGuy2
 * @since 2016-09-05
 */
public class AnnotationAssert {

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

		Map<String, String> sigArgs = sig.getArguments();

		for(int i = 0, size = argNames.length; i < size; i++) {
			String argName = argNames[i];
			String argVal = argValues[i];
			Assert.assertTrue("annotation '" + name + "' missing name '" + argName + "' found " + sigArgs.keySet(), sigArgs.containsKey(argName));
			Assert.assertEquals("annotation '" + name + "' mismatching value '" + argVal + "' vs '" + sigArgs.get(argName) + "'", argVal, sigArgs.get(argName));
		}
	}

}
