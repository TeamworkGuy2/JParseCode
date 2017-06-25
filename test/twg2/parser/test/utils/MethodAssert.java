package twg2.parser.test.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import twg2.ast.interm.method.ParameterSig;
import twg2.parser.codeParser.AccessModifier;

/**
 * @author TeamworkGuy2
 * @since 2017-06-25
 */
public class MethodAssert {

	public static void assertParameter(List<ParameterSig> params, int idx, String name, String type, List<? extends AccessModifier> accessMods, List<String> comments) {
		ParameterSig p = params.get(idx);
		Assert.assertEquals(name, p.getName());
		Assert.assertEquals(type, p.getTypeSimpleName());
		if(accessMods != null) {
			Assert.assertEquals(accessMods, p.getParameterModifiers());
		}
		if(comments != null) {
			Assert.assertEquals(Arrays.asList(" <summary>Add name</summary>\n",
					" <param name=\"name\">the name</param>\n",
					" <returns>the names</returns>\n"), comments);
		}
	}

}
