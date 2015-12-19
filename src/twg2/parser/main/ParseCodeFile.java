package twg2.parser.main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import twg2.parser.baseAst.csharp.CsBlock;
import twg2.parser.codeParser.codeStats.ParseDirectoryCodeFiles;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.output.WriteSettings;
import lombok.val;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 */
public class ParseCodeFile {

	public static void parseAndPrintCSharpFileInfo() throws IOException {
		Path file = Paths.get("./rsc/ITrackSearchService.cs");
		//Path file = Paths.get("./rsc/TrackInfo.cs");
		val files = Arrays.asList(file);
		val parsedFiles = ParseDirectoryCodeFiles.parseFiles(files);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			val parsedFile = parsedFiles.get(i);
			CsMain.printParseFileInfo(files.get(i).toString(), parsedFile, true, true, true, true, true);
		}
	}


	public static void parseAndValidProjectFilesLinks() throws IOException {
		//Path fileOrDir = Paths.get("Entities");
		Path fileOrDir = Paths.get("Services");
		int depth = 1;
		val fileSet = new ProjectClassSet<CsBlock>();
		val files = CsMain.getFilesByExtension(fileOrDir, depth, "cs");
		//val files = Arrays.asList(Paths.get("Request.cs"));
		CsMain.parseFileSet(files, fileSet);

		val writeSettings = new WriteSettings(true, false, false);
		val res = fileSet.getCompilationUnitsStartWith(Arrays.asList(""));
		for(val classSig : res) {
			classSig.toJson(System.out, writeSettings);
		}
		System.out.println();
		System.out.println("files: " + files.size());
	}


	public static void main(String[] args) throws IOException {
		//parseAndPrintCSharpFileInfo();
		parseAndValidProjectFilesLinks();
	}

}
