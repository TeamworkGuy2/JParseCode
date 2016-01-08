package twg2.parser.test.csharp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.collections.util.ListUtil;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.main.CsMain;
import checks.CheckCollections;

/**
 * @author TeamworkGuy2
 * @since 2016-1-8
 */
public class CsParserExamples {
	private IntermClass.ResolvedImpl<CsBlock> trackSearchServiceDef;
	private IntermClass.ResolvedImpl<CsBlock> albumInfoDef;
	private IntermClass.ResolvedImpl<CsBlock> trackInfoDef;

	@Parameter
	private ProjectClassSet<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>, IntermClass.SimpleImpl<CsBlock>> projFiles;
	@Parameter
	private List<IntermClass.ResolvedImpl<CsBlock>> resClasses;


	public CsParserExamples() throws IOException {
		Path trackSearchServiceFile = Paths.get("rsc/csharp/ParserExamples/Services/ITrackSearchService.cs");
		Path albumInfoFile = Paths.get("rsc/csharp/ParserExamples/Models/AlbumInfo.cs");
		Path trackInfoFile = Paths.get("rsc/csharp/ParserExamples/Models/TrackInfo.cs");
		projFiles = new ProjectClassSet<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>, IntermClass.SimpleImpl<CsBlock>>();

		HashSet<List<String>> missingNamespaces = new HashSet<>();
		CsMain.parseFileSet(Arrays.asList(trackSearchServiceFile, albumInfoFile, trackInfoFile), projFiles);
		val resFileSet = ProjectClassSet.resolveClasses(projFiles, CsBlock.CLASS, missingNamespaces);

		val res = resFileSet.getCompilationUnitsStartWith(Arrays.asList(""));

		// get a subset of all the parsed files
		resClasses = new ArrayList<>();
		for(val classInfo : res) {
			resClasses.add(classInfo.getValue());
		}
		resClasses.sort((c1, c2) -> NameUtil.joinFqName(c1.getSignature().getFullyQualifyingName()).compareTo(NameUtil.joinFqName(c2.getSignature().getFullyQualifyingName())));
		trackSearchServiceDef = ensureOne(resClasses.stream().filter((t) -> "ITrackSearchService".equals(t.getSignature().getSimpleName())));
		trackInfoDef = ensureOne(resClasses.stream().filter((t) -> "TrackInfo".equals(t.getSignature().getSimpleName())));
		albumInfoDef = ensureOne(resClasses.stream().filter((t) -> "AlbumInfo".equals(t.getSignature().getSimpleName())));
	}


	@Test
	public void checkResolvedNames() {
		val classNames = ListUtil.map(resClasses, (ic) -> NameUtil.joinFqName(ic.getSignature().getFullyQualifyingName()));
		CheckCollections.assertLooseEquals(Arrays.asList("ParserExamples.Services.ITrackSearchService", "ParserExamples.Models.AlbumInfo", "ParserExamples.Models.TrackInfo"), classNames);

		// SearchResult<TrackInfo> Search(TrackSearchCriteria criteria)
		val mthd1Ret = trackSearchServiceDef.getMethods().get(0).getReturnType();
		Assert.assertEquals("ParserExamples.Models.TrackInfo", NameUtil.joinFqName(mthd1Ret.getGenericParams().get(0).getFullyQualifyingName()));

		// SearchResult<IDictionary<AlbumInfo, IList<Track>>> GetAlbumTracks(string albumName)
		val mthd2Ret = trackSearchServiceDef.getMethods().get(1).getReturnType();

		// TODO debugging
		System.out.println("ret: " + mthd2Ret);

		Assert.assertEquals("IDictionary", NameUtil.joinFqName(mthd2Ret.getGenericParams().get(0).getFullyQualifyingName()));
		Assert.assertEquals("ParserExamples.Models.AlbumInfo", NameUtil.joinFqName(mthd2Ret.getGenericParams().get(0).getGenericParams().get(0).getFullyQualifyingName()));
		Assert.assertEquals("IList", NameUtil.joinFqName(mthd2Ret.getGenericParams().get(0).getGenericParams().get(1).getFullyQualifyingName()));
		Assert.assertEquals("ParserExamples.Models.TrackInfo", NameUtil.joinFqName(mthd2Ret.getGenericParams().get(0).getGenericParams().get(1).getGenericParams().get(0).getFullyQualifyingName()));

	}


	private static <R> R ensureOne(Stream<R> stream) {
		Object[] objs = stream.toArray();
		if(objs.length != 1) {
			throw new IllegalArgumentException("Expected stream of 1 object, found " + objs.length + ": " + Arrays.toString(objs));
		}
		@SuppressWarnings("unchecked")
		val res = (R)objs[0];
		return res;
	}

}
