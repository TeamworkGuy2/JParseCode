package twg2.parser.codeParser.tools;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
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


	public static String appendToFqName(String existingName, String nextPart) {
		return (existingName != null && existingName.length() > 0) ? existingName + "." + nextPart : nextPart;
	}


	public static List<String> allExceptLastFqName(List<String> names) {
		val resNames = new ArrayList<String>();
		for(int i = 0, size = names.size() - 1; i < size; i++) {
			resNames.add(names.get(i));
		}
		return resNames;
	}

}
