package twg2.parser.main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.val;
import twg2.io.files.FileReadUtil;
import twg2.io.json.Json;
import twg2.parser.baseAst.csharp.CsBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.ParseInput;
import twg2.parser.codeParser.codeStats.ParseDirectoryCodeFiles;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringJoin;
import twg2.text.stringUtils.StringReplace;
import twg2.text.stringUtils.StringSplit;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 */
public class ParseCodeFile {

	public static List<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>> parseFiles(List<Path> files) throws IOException {
		List<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>> parsedFiles = new ArrayList<>();

		for(Path path : files) {
			File file = path.toFile();
			String srcStr = StringReplace.replace(FileReadUtil.defaultInst.readString(new FileReader(file)), "\r\n", "\n");
			String fileName = file.getName();
			String fileExt = StringSplit.lastMatch(fileName, ".");
			val lang = CodeLanguageOptions.tryFromFileExtension(fileExt);
			if(lang != null) {
				val parsedFileInfo = parseCode(fileName, lang, srcStr);
				parsedFiles.add(parsedFileInfo);
			}
			else {
				throw new IllegalArgumentException("unsupported file extension '" + fileExt + "' for parsing '" + fileName + "'");
			}
		}

		return parsedFiles;
	}


	public static CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage> parseCode(String fileName, CodeLanguage lang, String srcStr) {
		val parseParams = new ParseInput(srcStr, null, fileName);
		try {
			@SuppressWarnings("unchecked")
			CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage> parsedFileInfo = (CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>)lang.getParser().apply(parseParams);
			return parsedFileInfo;
		} catch(Exception e) {
			throw new RuntimeException(parseParams.toString(), e);
		}
	}


	public static void parseAndPrintFileStats() throws IOException {
		Path oldProjDir = Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/psor/ca");
		Path newProjDir = Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/ps/l/ca");

		String[] oldProjFileTypes = { ".cs", ".js", ".json", ".html", ".css" };
		val oldFiles = ParseDirectoryCodeFiles.loadFiles(oldProjDir, oldProjFileTypes);
		val results = ParseDirectoryCodeFiles.parseFileStats(oldProjDir, oldFiles);

		String[] newProjFileTypes = { ".cs", ".ts", ".json", ".html", ".css" };
		//val newFiles = loadFiles(newProjDir, newProjFileTypes);
		//val results = parseFiles(newProjDir, newFiles);

		File dstLog = new File("C:/Users/TeamworkGuy2/Documents/parsed-file-stats.txt");

		Json.getDefaultInst().setPrettyPrint(true);
		Json.stringify(results, dstLog);
	}


	public static void parseAndPrintCSharpFileInfo() throws IOException {
		Path file = Paths.get("./rsc/ITrackSearchService.cs");
		//Path file = Paths.get("./rsc/TrackInfo.cs");
		val files = Arrays.asList(file);
		val parsedFiles = parseFiles(files);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			val parsedFile = parsedFiles.get(i);
			CsMain.printParseFileInfo(files.get(i).toString(), parsedFile, true, true, true, true, true);
		}
	}


	public static void parseAndValidProjectCsClasses() throws IOException {
		Path fileOrDir = Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/powerscope/loki/CorningstoneApp/server/Entities"); //("/server/Entities");
		//Path fileOrDir = Paths.get("/server/Services");
		int depth = 3;
		val fileSet = new ProjectClassSet<IntermClass.SimpleImpl<CsBlock>>();
		val files = CsMain.getFilesByExtension(fileOrDir, depth, "cs");
		//val files = Arrays.asList(Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/powerscope/loki/CorningstoneApp/server/Entities/Searching/CustomerUserSearchCriteria.cs")); //("/server/Entities/Messaging/SubmitBidRequest.cs"));
		List<List<String>> missingNamespaces = new ArrayList<>();
		CsMain.parseFileSet(files, fileSet);
		val resFileSet = ProjectClassSet.resolveClasses(fileSet, CsBlock.CLASS, missingNamespaces);

		val writeSettings = new WriteSettings(true, false, false);
		val res = resFileSet.getCompilationUnitsStartWith(Arrays.asList(""));
		for(val classSig : res) {
			classSig.toJson(System.out, writeSettings);
		}
		System.out.println("\n");
		System.out.println("files (" + files.size() + "): " + StringJoin.Objects.join(files, "\n"));
		String[] nonSystemMissingNamespaces = missingNamespaces.stream().filter((ns) -> !"System".equals(ns.get(0))).map((ns) -> NameUtil.joinFqName(ns)).toArray((n) -> new String[n]);
		System.out.println("missing non-system namespaces: (" + nonSystemMissingNamespaces.length + "): " + Arrays.toString(nonSystemMissingNamespaces));
	}


	public static void main(String[] args) throws IOException {
		//parseAndPrintCSharpFileInfo();
		//parseAndPrintFileStats();
		parseAndValidProjectCsClasses();
	}

}
