package twg2.parser.baseAst.util;

import java.util.ArrayList;
import java.util.List;

import twg2.text.stringUtils.StringJoin;
import twg2.text.stringUtils.StringSplit;

/** Static methods for dealing with 'fully-qualifying' namespace, package, module, class, method, and field names
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public class NameUtil {

	public static List<String> newFqName(List<String> names, String appendName) {
		List<String> list = new ArrayList<>(names);
		list.add(appendName);
		return list;
	}


	public static List<String> splitFqName(String name) {
		List<String> names = StringSplit.split(name, '.');
		return names;
	}


	public static String joinFqName(List<String> names) {
		String name = StringJoin.join(names, ".");
		return name;
	}

}
