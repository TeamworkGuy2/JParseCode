package twg2.parser.codeParser.test;

import static twg2.parser.test.utils.FieldAssert.assertField;
import static twg2.parser.test.utils.MethodAssert.assertParameter;
import static twg2.parser.test.utils.TypeAssert.ls;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaBlockParser;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.test.utils.TypeAssert;
import twg2.parser.workflow.CodeFileSrc;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-15
 */
public class JavaClassTrackInfoTest {
	@Parameter
	private CodeFileSrc file;

	private List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<JavaBlock>>> blocks;


	public JavaClassTrackInfoTest() throws IOException {
		file = ParseCodeFile.parseFiles(ls(Paths.get("rsc/java/ParserExamples/Models/TrackInfo.java")), FileReadUtil.threadLocalInst(), null).get(0);
		blocks = new JavaBlockParser().extractClassFieldsAndMethodSignatures(file.astTree);

		Assert.assertEquals(2, blocks.size());
	}


	@Test
	public void parseTrackInfoBlockTest() {
		// public class TrackInfo {...
		ClassAst.SimpleImpl<JavaBlock> trackInfo = blocks.get(0).getValue();
		String fullClassName = NameUtil.joinFqName(trackInfo.getSignature().getFullName());
		Assert.assertEquals(JavaBlock.CLASS, trackInfo.getBlockType());
		Assert.assertEquals("ParserExamples.Models.TrackInfo", fullClassName);

		Assert.assertEquals(ls("BaseClass", "Serializable", "Comparable<TrackInfo>"), trackInfo.getSignature().getExtendImplementSimpleNames());

		var fields = trackInfo.getFields();
		assertField(fields, 0, fullClassName + ".Name", "String");
		assertField(fields, 1, fullClassName + ".artist", "String");
		assertField(fields, 2, fullClassName + ".durationMillis", "int");
		assertField(fields, 3, fullClassName + ".contentId", "long");

		// int compareTo(TrackInfo o)
		MethodSigSimple m = trackInfo.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".compareTo", NameUtil.joinFqName(m.fullName));
		assertParameter(m.paramSigs, 0, "o", "TrackInfo", false, null, null, null);

		// <TType extends Number> TType refresh(TType tt)
		m = trackInfo.getMethods().get(1);
		Assert.assertEquals(fullClassName + ".refresh", NameUtil.joinFqName(m.fullName));
		TypeAssert.assertType("TType extends Number", m.typeParameters.get(0));
		assertParameter(m.paramSigs, 0, "tt", "TType", false, null, null, null);
	}


	@Test
	public void parseArtistMetaBlockTest() {
		// class ArtistMeta {...
		ClassAst.SimpleImpl<JavaBlock> artistMeta = blocks.get(1).getValue();
		String fullClassName = NameUtil.joinFqName(artistMeta.getSignature().getFullName());
		Assert.assertEquals(JavaBlock.CLASS, artistMeta.getBlockType());
		Assert.assertEquals("ParserExamples.Models.TrackInfo.ArtistMeta", fullClassName);

		Assert.assertEquals(ls(), artistMeta.getSignature().getExtendImplementSimpleNames());
		Assert.assertEquals(AccessModifierEnum.NAMESPACE_LOCAL, artistMeta.getSignature().getAccessModifier());
	}

}
