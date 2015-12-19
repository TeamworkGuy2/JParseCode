package twg2.parser.codeParser.typescript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import twg2.text.stringUtils.StringSplit;
import twg2.text.stringUtils.StringTrim;
/**
 * @author TeamworkGuy2
 * @since 2015-5-11
 */
public class Imports {


	public static List<String> extractImports(Collection<String> lines) {
		List<String> imports = new ArrayList<>();
		for(String line : lines) {
			String lineTrim = line.trim();
			if(lineTrim.startsWith("import ")) {
				// given a string in the format "import ... = require("import_path");", import_path is extracted
				String importStr = StringTrim.trimQuotes(StringSplit.findNthMatch(StringSplit.findNthMatch(lineTrim, "require(", 1, 2), ");", 0, 2));
				imports.add(importStr);
			}
			else if(lineTrim.startsWith("class") || lineTrim.startsWith("module") || lineTrim.startsWith("export")) {
				break;
			}
		}
		return imports;
	}

}
