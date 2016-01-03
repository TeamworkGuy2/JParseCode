package twg2.parser.codeParser.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import lombok.val;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.parser.baseAst.csharp.CsBlock;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.csharp.CsBlockExtractor;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.main.ParseCodeFile;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class ParseCsClassTest {
	@Parameter
	private CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage> file = ParseCodeFile.parseFiles(Arrays.asList(Paths.get("rsc/TrackInfo.cs"))).get(0);


	public ParseCsClassTest() throws IOException {
	}


	@Test
	public void parseBlocksTest() {
		val tree = file.getDoc();
		val blocks = CsBlockExtractor.extractBlockFieldsAndInterfaceMethods(tree);

		Assert.assertEquals(1, blocks.size());

		val trackInfoBlock = blocks.get(0);
		Assert.assertEquals(CsBlock.CLASS, trackInfoBlock.getBlockType());
		Assert.assertEquals("TrackInfo", trackInfoBlock.getSignature().getSimpleName());
	}


}
