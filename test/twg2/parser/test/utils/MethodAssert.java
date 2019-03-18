package twg2.parser.test.utils;

import java.util.List;

import org.junit.Assert;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.method.ParameterSig;
import twg2.parser.codeParser.Keyword;

/**
 * @author TeamworkGuy2
 * @since 2017-06-25
 */
public class MethodAssert {

	public static void assertParameter(List<ParameterSig> params, int idx, String name, String type, String defaultValue, List<? extends Keyword> accessMods, List<? extends AnnotationSig> annotations) {
		ParameterSig p = params.get(idx);
		Assert.assertEquals(name, p.getName());
		Assert.assertEquals(type, p.getTypeSimpleName());
		Assert.assertEquals(defaultValue, p.getDefaultValue());
		if(accessMods != null) {
			Assert.assertEquals(accessMods, p.getParameterModifiers());
		}
		if(annotations != null) {
			Assert.assertEquals(annotations, p.getAnnotations());
		}
	}

}
