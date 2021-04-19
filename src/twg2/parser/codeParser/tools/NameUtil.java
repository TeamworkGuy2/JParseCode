package twg2.parser.codeParser.tools;

import java.util.ArrayList;
import java.util.List;

import twg2.text.stringUtils.StringJoin;
import twg2.text.stringUtils.StringSplit;

/** Static methods for dealing with 'fully-qualifying' namespace, package, module, class, method, and field names
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public class NameUtil {
	/** size hint passed to {@link ArrayList} constructor since default of 10 doesn't well fit the the number of name segments in most fully qualifying names */
	public static int estimatedFqPartsCount = 5;


	public static List<String> newFqName(List<String> names, String appendName) {
		List<String> list = new ArrayList<>(names);
		list.add(appendName);
		return list;
	}


	public static List<String> splitFqName(String name) {
		List<String> dst = new ArrayList<>(estimatedFqPartsCount);
		StringSplit.split(name, '.', 0, dst);
		return dst;
	}


	public static String joinFqName(List<String> names) {
		String name = StringJoin.join(names, ".");
		return name;
	}


	public static String appendToFqName(String existingName, String nextPart) {
		return (existingName != null && existingName.length() > 0) ? existingName + "." + nextPart : nextPart;
	}


	public static List<String> allExceptLastFqName(List<String> names) {
		int size = names.size() - 1;
		var resNames = new ArrayList<String>(size);
		for(int i = 0; i < size; i++) {
			resNames.add(names.get(i));
		}
		return resNames;
	}


	public static String joinFqNameExceptLast(List<String> names) {
		int size = names.size() - 1;
		var res = new StringBuilder();
		for(int i = 0; i < size; i++) {
			if(i > 0) { res.append('.'); }
			res.append(names.get(i));
		}
		return res.toString();
	}

}
