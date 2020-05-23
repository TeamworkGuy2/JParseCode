package twg2.parser.test;

import static twg2.text.tokenizer.test.ParserTestUtils.parseTest;
import org.junit.Test;

import twg2.parser.codeParser.csharp.CsFileTokenizer;
import twg2.parser.condition.text.CharParser;

/**
 * @author TeamworkGuy2
 * @since 2016-2-7
 */
public class ParseAnnotationTest {

	@Test
	public void annotationParseTest() {
		String name = "AnnotationParse";
		CharParser cond = CsFileTokenizer.createAnnotationTokenizer().createParser();

		parseTest(false, false, name, cond, "[");
		parseTest(false, true, name, cond, "]");
		parseTest(false, false, name, cond, "[]");
		parseTest(true, false, name, cond, "[A]");
	}

}
