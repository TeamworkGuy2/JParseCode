package twg2.parser.test.utils;

import java.util.List;

import org.junit.Assert;

import twg2.ast.interm.field.FieldSig;
import twg2.parser.codeParser.tools.NameUtil;

/**
 * @author TeamworkGuy2
 * @since 2017-06-25
 */
public class FieldAssert {

	/** Compare a manually defined type signature (via nested arrays and strings) with an actual type signature
	 * @param fields the list of fields to assert against
	 * @param idx the index of the {@link FieldSig} in the 'fields' list to assert
	 * @param fqName the expected fully qualifying name of the field
	 * @param type either the expected name of a non-generic type or an array containing a generic type name and an array of it's type parameters
	 * each of which can be a name or nested array containing a nested generic type definition
	 */
	public static void assertField(List<FieldSig> fields, int idx, String fqName, Object type) {
		FieldSig f = fields.get(idx);
		Assert.assertEquals(fqName, NameUtil.joinFqName(f.getFullName()));
		TypeAssert.assertType(type, f.getFieldType());
	}

}
