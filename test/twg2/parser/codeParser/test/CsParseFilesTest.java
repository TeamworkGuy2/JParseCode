package twg2.parser.codeParser.test;

import static twg2.parser.test.utils.TypeAssert.assertType;
import static twg2.parser.test.utils.TypeAssert.ls;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.type.TypeSig.TypeSigResolved;
import twg2.io.files.FileFormatException;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.main.ParserMisc;
import twg2.parser.project.ProjectClassSet;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2016-1-8
 */
public class CsParseFilesTest {
	private ClassAst.ResolvedImpl<CsBlock> trackSearchServiceDef;
	private ClassAst.ResolvedImpl<CsBlock> albumInfoDef;
	private ClassAst.ResolvedImpl<CsBlock> trackInfoDef;

	@Parameter
	private ProjectClassSet.Intermediate<CsBlock> projFiles;


	public CsParseFilesTest() throws IOException, FileFormatException {
		Path trackSearchServiceFile = Paths.get("rsc/csharp/ParserExamples/Services/ITrackSearchService.cs");
		Path albumInfoFile = Paths.get("rsc/csharp/ParserExamples/Models/AlbumInfo.cs");
		Path trackInfoFile = Paths.get("rsc/csharp/ParserExamples/Models/TrackInfo.cs");
		projFiles = new ProjectClassSet.Intermediate<CsBlock>();
		// TODO until better solution for managing algorithm parallelism
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		FileReadUtil fileReader = FileReadUtil.threadLocalInst();

		HashSet<List<String>> missingNamespaces = new HashSet<>();
		ParserMisc.parseFileSet(Arrays.asList(trackSearchServiceFile, albumInfoFile, trackInfoFile), projFiles, executor, fileReader, null);
		ProjectClassSet.Resolved<CsBlock> resFileSet = ProjectClassSet.resolveClasses(projFiles, CsBlock.CLASS, missingNamespaces);

		List<CodeFileParsed.Resolved<CsBlock>> res = resFileSet.getCompilationUnitsStartWith(Arrays.asList(""));

		// get a subset of all the parsed files
		for(CodeFileParsed.Resolved<CsBlock> classInfo : res) {
			ClassAst.ResolvedImpl<CsBlock> classParsed = classInfo.parsedClass;
			String simpleName = classParsed.getSignature().getSimpleName();
			if("ITrackSearchService".equals(simpleName)) {
				trackSearchServiceDef = classParsed;
			}
			else if("TrackInfo".equals(simpleName)) {
				trackInfoDef = classParsed;
			}
			else if("AlbumInfo".equals(simpleName)) {
				albumInfoDef = classParsed;
			}
			else {
				throw new IllegalStateException("unknown class '" + NameUtil.joinFqName(classParsed.getSignature().getFullName()) + "'");
			}
		}
	}


	@Test
	public void checkResolvedClasses() {
		// TrackInfo : ISerializable, IComparable<TrackInfo>
		Assert.assertArrayEquals(ls("ParserExamples", "Models", "TrackInfo"), trackInfoDef.getSignature().getFullName().toArray());
		assertType(ls("ISerializable"), trackInfoDef.getSignature().getExtendClass());
		Assert.assertEquals(1, trackInfoDef.getSignature().getImplementInterfaces().size());
		assertType(ls("IComparable", ls("TrackInfo")), trackInfoDef.getSignature().getImplementInterfaces().get(0));
	}


	@Test
	public void checkResolvedMethodNames() {
		// SearchResult<TrackInfo> Search(TrackSearchCriteria criteria)
		TypeSigResolved mthd1Ret = trackSearchServiceDef.getMethods().get(0).returnType;
		Assert.assertEquals("ParserExamples.Models.TrackInfo", NameUtil.joinFqName(mthd1Ret.getParams().get(0).getFullName()));

		// SearchResult<IDictionary<AlbumInfo, IList<Track>>> GetAlbumTracks(string albumName)
		TypeSigResolved mthd2Ret = trackSearchServiceDef.getMethods().get(1).returnType;

		Assert.assertEquals("IDictionary", NameUtil.joinFqName(mthd2Ret.getParams().get(0).getFullName()));
		Assert.assertEquals("ParserExamples.Models.AlbumInfo", NameUtil.joinFqName(mthd2Ret.getParams().get(0).getParams().get(0).getFullName()));
		Assert.assertEquals("IList", NameUtil.joinFqName(mthd2Ret.getParams().get(0).getParams().get(1).getFullName()));
		Assert.assertEquals("ParserExamples.Models.TrackInfo", NameUtil.joinFqName(mthd2Ret.getParams().get(0).getParams().get(1).getParams().get(0).getFullName()));

	}

}
