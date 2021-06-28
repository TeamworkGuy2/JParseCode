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
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsBlockParser;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.test.utils.TypeAssert;
import twg2.parser.workflow.CodeFileSrc;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class CsClassTrackInfoTest {
	@Parameter
	private CodeFileSrc file;

	private List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<CsBlock>>> blocks;


	public CsClassTrackInfoTest() throws IOException {
		file = ParseCodeFile.parseFiles(ls(Paths.get("rsc/csharp/ParserExamples/Models/TrackInfo.cs")), FileReadUtil.threadLocalInst(), null).get(0);
		blocks = new CsBlockParser().extractClassFieldsAndMethodSignatures(file.astTree);

		Assert.assertEquals(2, blocks.size());
	}


	@Test
	public void parseTrackInfoBlockTest() {
		// public class TrackInfo {...
		ClassAst.SimpleImpl<CsBlock> trackInfo = blocks.get(0).getValue();
		String fullClassName = NameUtil.joinFqName(trackInfo.getSignature().getFullName());
		Assert.assertEquals(CsBlock.CLASS, trackInfo.getBlockType());
		Assert.assertEquals("ParserExamples.Models.TrackInfo", fullClassName);

		Assert.assertEquals(ls("BaseClass", "ISerializable", "IComparable<TrackInfo>"), trackInfo.getSignature().getExtendImplementSimpleNames());

		var fields = trackInfo.getFields();
		assertField(fields, 0, fullClassName + ".Name", "string");
		assertField(fields, 1, fullClassName + ".artist", "string");
		assertField(fields, 2, fullClassName + ".durationMillis", "int");
		assertField(fields, 3, fullClassName + ".contentId", "long");

		// int CompareTo(Implementer other)
		MethodSigSimple m = trackInfo.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".CompareTo", NameUtil.joinFqName(m.fullName));
		assertParameter(m.paramSigs, 0, "other", "Implementer", false, null, null, null);

		// void GetObjectData(SerializationInfo info, StreamingContext context)
		m = trackInfo.getMethods().get(1);
		Assert.assertEquals(fullClassName + ".GetObjectData", NameUtil.joinFqName(m.fullName));
		assertParameter(m.paramSigs, 0, "info", "SerializationInfo", false, null, null, null);
		assertParameter(m.paramSigs, 1, "context", "StreamingContext", false, null, null, null);

		// TType Refresh<TType>(TType tt) where TType : IConvertible
		m = trackInfo.getMethods().get(2);
		Assert.assertEquals(fullClassName + ".Refresh", NameUtil.joinFqName(m.fullName));
		TypeAssert.assertType("TType", m.typeParameters.get(0)); // TODO type bounds for C# type parameters are not parsed/stored
		assertParameter(m.paramSigs, 0, "tt", "TType", false, null, null, null);
	}


	@Test
	public void parseArtistMetaBlockTest() {
		// public class ArtistMeta {...
		ClassAst.SimpleImpl<CsBlock> artistMeta = blocks.get(1).getValue();
		String fullClassName = NameUtil.joinFqName(artistMeta.getSignature().getFullName());
		Assert.assertEquals(CsBlock.CLASS, artistMeta.getBlockType());
		Assert.assertEquals("ParserExamples.Models.TrackInfo.ArtistMeta", fullClassName);

		Assert.assertEquals(ls(), artistMeta.getSignature().getExtendImplementSimpleNames());
		Assert.assertEquals(AccessModifierEnum.PRIVATE, artistMeta.getSignature().getAccessModifier());
	}

}
