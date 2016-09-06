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
import twg2.io.files.FileFormatException;
import twg2.io.files.FileReadUtil;
import twg2.io.json.Json;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.ParseInput;
import twg2.parser.codeParser.codeStats.ParseDirectoryCodeFiles;
import twg2.parser.language.CodeLanguage;
import twg2.parser.language.CodeLanguageOptions;
import twg2.text.stringUtils.StringReplace;
import twg2.text.stringUtils.StringSplit;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 */
public class ParseCodeFile {

	public static List<CodeFileSrc<CodeLanguage>> parseFiles(List<Path> files, FileReadUtil fileReader) throws IOException {
		List<CodeFileSrc<CodeLanguage>> parsedFiles = new ArrayList<>();

		for(Path path : files) {
			val parsedFile = parseFile(path.toFile(), fileReader);
			parsedFiles.add(parsedFile);
		}

		return parsedFiles;
	}


	public static CodeFileSrc<CodeLanguage> parseFile(File file, FileReadUtil fileReader) throws IOException {
		String srcStr = StringReplace.replace(fileReader.readString(new FileReader(file)), "\r\n", "\n");

		String fileName = file.getName();
		String fileExt = StringSplit.lastMatch(fileName, ".");
		val lang = CodeLanguageOptions.tryFromFileExtension(fileExt);
		if(lang != null) {
			val parsedFileInfo = parseCode(file.toString(), lang, srcStr);
			return parsedFileInfo;
		}
		else {
			throw new IllegalArgumentException("unsupported file extension '" + fileExt + "' for parsing '" + fileName + "'");
		}
	}


	public static CodeFileSrc<CodeLanguage> parseCode(String fileName, CodeLanguage lang, String srcStr) {
		val parseParams = new ParseInput(srcStr, null, fileName);
		try {
			@SuppressWarnings("unchecked")
			CodeFileSrc<CodeLanguage> parsedFileInfo = (CodeFileSrc<CodeLanguage>)lang.getParser().apply(parseParams);
			return parsedFileInfo;
		} catch(Exception e) {
			throw new RuntimeException(parseParams.toString(), e);
		}
	}


	public static void parseAndPrintFileStats(Path projDir, String[] fileTypes, Path dstLog, FileReadUtil fileReader) throws IOException {
		val files = ParseDirectoryCodeFiles.loadFiles(projDir, fileTypes);
		val results = ParseDirectoryCodeFiles.parseFileStats(projDir, files, fileReader);

		Json.getDefaultInst().setPrettyPrint(true);
		Json.stringify(results, dstLog.toFile());
	}


	public static void parseAndPrintOldAndNewFileStats() throws IOException {
		FileReadUtil fileReader = FileReadUtil.threadLocalInst();

		parseAndPrintFileStats(Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/psor/ca"),
				new String[] { ".cs", ".js", ".json", ".html", ".css" },
				Paths.get("C:/Users/TeamworkGuy2/Documents/parsed-file-stats-old.txt"), fileReader);

		parseAndPrintFileStats(Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/ps/l/ca"),
				new String[] { ".cs", ".ts", ".json", ".html", ".css" },
				Paths.get("C:/Users/TeamworkGuy2/Documents/parsed-file-stats-new.txt"), fileReader);
	}


	public static void parseAndPrintCSharpFileInfo() throws IOException, FileFormatException {
		val fileReader = FileReadUtil.threadLocalInst();
		Path file = Paths.get("./rsc/ITrackSearchService.cs");
		//Path file = Paths.get("./rsc/TrackInfo.cs");
		val files = Arrays.asList(file);
		val parsedFiles = parseFiles(files, fileReader);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			val parsedFile = parsedFiles.get(i);
			ParserMisc.printParseFileInfo(files.get(i).toString(), parsedFile, true, true, true, true, true);
		}
	}

}
