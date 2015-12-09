package test;

import intermAst.project.ProjectClassSet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import lombok.val;
import main.CSharpMain;
import baseAst.csharp.CSharpBlock;
import codeParser.codeStats.ParseDirectoryCodeFiles;

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
			CSharpMain.printParseFileInfo(files.get(i).toString(), parsedFile, true, true, true, true, true);
		}
	}


	public static void parseAndValidProjectFilesLinks() throws IOException {
		//Path fileOrDir = Paths.get("../../test-files-1");
		Path fileOrDir = Paths.get("../../test-files-2");
		int depth = 1;
		val fileSet = new ProjectClassSet<CSharpBlock>();
		val files = CSharpMain.getFilesByExtension(fileOrDir, depth, "cs");
		CSharpMain.parseFileSet(files, fileSet);

		val res = fileSet.getCompilationUnitsStartWith(Arrays.asList(""));
		for(val classSig : res) {
			classSig.toJson(System.out);
		}
		System.out.println();
		System.out.println("files: " + files.size());
	}


	public static void main(String[] args) throws IOException {
		//parseAndPrintCSharpFileInfo();
		parseAndValidProjectFilesLinks();
	}

}
