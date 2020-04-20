package twg2.parser.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twg2.io.files.FileFormatException;
import twg2.io.files.FileReadUtil;
import twg2.io.json.Json;
import twg2.parser.codeParser.analytics.ParseTimes;
import twg2.parser.codeParser.analytics.PerformanceTrackers;
import twg2.parser.codeParser.analytics.ParserActionLogger;
import twg2.parser.codeParser.analytics.ParseTimes.TrackerAction;
import twg2.parser.codeParser.codeStats.ParseDirectoryCodeFiles;
import twg2.parser.language.CodeLanguage;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.workflow.CodeFileSrc;
import twg2.parser.workflow.ParseInput;
import twg2.text.stringUtils.StringSplit;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 */
public class ParseCodeFile {

	public static List<CodeFileSrc> parseFiles(List<Path> files, FileReadUtil fileReader, PerformanceTrackers perfTracking) throws IOException {
		var parsedFiles = new ArrayList<CodeFileSrc>(files.size());

		for(Path path : files) {
			var file = path.toFile();
			var parsedFile = parseFile(file, fileReader, perfTracking);
			parsedFiles.add(parsedFile);
		}

		return parsedFiles;
	}


	public static CodeFileSrc parseFile(File file, FileReadUtil fileReader, PerformanceTrackers perfTracking) throws IOException {
		String fileStr = file.toString();
		var perfTracker = perfTracking != null ? perfTracking.getOrCreateParseTimes(fileStr) : null;
		var stepsTracker = perfTracking != null ? perfTracking.getOrCreateStepDetails(fileStr) : null;
		long start = 0;
		if(perfTracker != null) { start = System.nanoTime(); }

		char[] src = fileReader.readChars(new FileInputStream(file));

		if(perfTracking != null) { perfTracking.setSrcSize(fileStr, src.length); }

		if(perfTracker != null) {
			perfTracker.setActionTime(TrackerAction.LOAD, System.nanoTime() - start);
		}

		String fileName = file.getName();
		String fileExt = StringSplit.lastMatch(fileName, '.');
		var lang = CodeLanguageOptions.tryFromFileExtension(fileExt);
		if(lang != null) {
			var parsedFileInfo = parseCode(fileStr, lang, src, 0, src.length, perfTracker, stepsTracker);
			return parsedFileInfo;
		}
		else {
			throw new IllegalArgumentException("unsupported file extension '" + fileExt + "' for parsing '" + fileName + "'");
		}
	}


	public static CodeFileSrc parseCode(String fileName, CodeLanguage lang, char[] src, int srcOff, int srcLen, ParseTimes perfTracker, ParserActionLogger stepsTracker) {
		var parseParams = new ParseInput(src, srcOff, srcLen, fileName, null, perfTracker, stepsTracker);
		try {
			CodeFileSrc parsedFileInfo = lang.getParser().apply(parseParams);
			return parsedFileInfo;
		} catch(Exception e) {
			throw new RuntimeException(parseParams.toString(), e);
		}
	}


	public static void parseAndPrintFileStats(Path projDir, String[] fileTypes, Path dstLog, FileReadUtil fileReader) throws IOException {
		var files = ParseDirectoryCodeFiles.loadFiles(projDir, fileTypes);
		var results = ParseDirectoryCodeFiles.parseFileStats(projDir, files, fileReader);

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
		var fileReader = FileReadUtil.threadLocalInst();
		PerformanceTrackers perfTracking = null;
		Path file = Paths.get("./rsc/ITrackSearchService.cs");
		//Path file = Paths.get("./rsc/TrackInfo.cs");
		var files = Arrays.asList(file);
		var parsedFiles = parseFiles(files, fileReader, perfTracking);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			var parsedFile = parsedFiles.get(i);
			ParserMisc.printParseFileInfo(files.get(i).toString(), parsedFile, true, true, true, true, true);
		}
	}

}
