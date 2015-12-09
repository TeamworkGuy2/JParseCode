package codeParser.codeStats;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import twg2.collections.tuple.Tuples;
import twg2.collections.util.ListUtil;
import twg2.io.files.FileReadUtil;
import twg2.io.files.FileVisitorUtil;
import twg2.io.json.Json;
import twg2.text.stringUtils.StringReplace;
import twg2.text.stringUtils.StringSplit;
import codeParser.CodeFileSrc;
import codeParser.CodeFragmentType;
import codeParser.CodeLanguage;
import codeParser.CodeLanguageOptions;
import codeParser.ParseCommentsAndWhitespace;
import codeParser.ParseInput;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Typing;

import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
@NoArgsConstructor
public class ParseDirectoryCodeFiles {

	@AllArgsConstructor
	@NoArgsConstructor
	private static class UncategorizedFilesStats {
		@Getter ParsedCategoryStats uncategorizedFilesStats;
		@Getter List<String> files;
	}


	@JsonSerialize(using = Json.PathSerializer.class, typing = Typing.STATIC)
	@JsonDeserialize(using = Json.PathDeserializer.class)
	@Getter Path relativePath;
	@Getter List<ParsedFileStats> fileStats;
	@Getter Map<CodeLanguage, ParsedCategoryStats> statsPerCategory;
	@Getter UncategorizedFilesStats uncategorizedFilesStats;



	public ParseDirectoryCodeFiles(Path relativePath, List<ParsedFileStats> fileStats) {
		this.relativePath = relativePath;
		this.fileStats = fileStats;
		val categorizedStats = categorizeFileStats(fileStats);
		this.statsPerCategory = categorizedStats.getKey();
		this.uncategorizedFilesStats = new UncategorizedFilesStats(reduceFileStats("uncategorizedFiles", categorizedStats.getValue()), ListUtil.map(categorizedStats.getValue(), (s) -> s.getSrcId()));
	}


	static Entry<Map<CodeLanguage, ParsedCategoryStats>, List<ParsedFileStats>> categorizeFileStats(List<ParsedFileStats> fileStats) {
		List<ParsedFileStats> uncategorizedFiles = new ArrayList<>();
		Map<CodeLanguage, List<ParsedFileStats>> filesPerCategory = new HashMap<>();

		for(ParsedFileStats fileStat : fileStats) {
			CodeLanguage lang = CodeLanguageOptions.tryFromFileExtension(StringSplit.lastMatch(fileStat.getSrcId(), "."));
			if(lang == null) {
				uncategorizedFiles.add(fileStat);
			}
			else {
				List<ParsedFileStats> files = filesPerCategory.get(lang);
				if(files == null) {
					files = new ArrayList<>();
					filesPerCategory.put(lang, files);
				}
				files.add(fileStat);
			}
		}

		Map<CodeLanguage, ParsedCategoryStats> categoryStats = new HashMap<>();
		for(Entry<CodeLanguage, List<ParsedFileStats>> filesCategory : filesPerCategory.entrySet()) {
			val combinedStats = reduceFileStats(filesCategory.getKey().toString(), filesCategory.getValue());
			categoryStats.put(filesCategory.getKey(), combinedStats);
		}

		return Tuples.of(categoryStats, uncategorizedFiles);
	}


	static ParsedCategoryStats reduceFileStats(String statsName, Collection<ParsedFileStats> fileStats) {
		int totalLines = 0;
		int whitespaceLines = 0;
		int commentLines = 0;
		int totalCharsSize = 0;
		int fileStatsCount = 0;
		for(ParsedFileStats fileStat : fileStats) {
			commentLines += fileStat.getCommentLineCount();
			whitespaceLines += fileStat.getWhitespaceLineCount();
			totalLines += fileStat.getTotalLineCount();
			totalCharsSize += fileStat.getCharCount();
			fileStatsCount++;
		}
		return new ParsedCategoryStats(statsName, totalCharsSize, fileStatsCount, whitespaceLines, commentLines, totalLines);
	}


	public static FileVisitorUtil.Cache createFilter(String... allowedFileExtensions) {
		val visitorBldr = new FileVisitorUtil.Builder();
		visitorBldr.getPreVisitDirectoryFilter().addDirectoryNameFilters(false, "/bin", "/appcache", "/i/", "/debug", "/Properties", "/obj", "/tasks",
				"/i18next-1.7.3", "/Excel", "/jspdf", "/pdfjs", "/zip", "/react", "/tsDefinitions", "/dest", "/tests",
				"/node_modules", "/modules/legacy", "/scripts/vendor", "/scripts/handsontable", "/scripts/lib" // specific to the new project
			);
		visitorBldr.getVisitFileFilter().addFileExtensionFilters(true, allowedFileExtensions);
		visitorBldr.getVisitFileFilter().setTrackMatches(true);

		return visitorBldr.build();
	}


	public static List<Path> loadFiles(Path projectDir, String... allowedFileExtensions) throws IOException {
		val visitorCache = createFilter(allowedFileExtensions);
		val visitor = visitorCache.getFileVisitor();

		Files.walkFileTree(projectDir, visitor);

		return visitorCache.getVisitFileFilterCache().getMatches();
	}


	public static List<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>> parseFiles(List<Path> files) throws IOException {
		List<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>> parsedFiles = new ArrayList<>();

		for(Path path : files) {
			File file = path.toFile();
			String srcStr = StringReplace.replace(FileReadUtil.defaultInst.readString(new FileReader(file)), "\r\n", "\n");
			String fileName = file.getName();
			String fileExt = StringSplit.lastMatch(fileName, ".");
			val lang = CodeLanguageOptions.tryFromFileExtension(fileExt);
			if(lang != null) {
				val parseParams = new ParseInput(srcStr, null, fileName);
				
				CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage> parsedFileInfo = null;
				try {
					parsedFileInfo = (CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>)lang.getParser().apply(parseParams);
				} catch(Exception e) {
					throw new RuntimeException(parseParams.toString(), e);
				}

				parsedFiles.add(parsedFileInfo);
			}
			else {
				throw new IllegalArgumentException("unsupported file extension '" + fileExt + "' for parsing '" + fileName + "'");
			}
		}

		return parsedFiles;
	}


	public static ParseDirectoryCodeFiles parseFileStats(Path relativePath, List<Path> files) throws IOException {
		List<ParsedFileStats> filesStats = new ArrayList<>();

		for(Path path : files) {
			File file = path.toFile();
			String srcStr = StringReplace.replace(FileReadUtil.defaultInst.readString(new FileReader(file)), "\r\n", "\n");
			String fileExt = StringSplit.lastMatch(file.getName(), ".");
			if("json".equals(fileExt)) {
				int lineCount = StringSplit.countMatches(srcStr, "\n");
				val parsedStats = new ParsedFileStats(file.toString(), srcStr.length(), 0, 0, lineCount);
				filesStats.add(parsedStats);
			}
			else {
				val parsedFileInfo = ParseCommentsAndWhitespace.buildCommentsAndWhitespaceTreeFromFileExtension(fileExt, srcStr);
				val parsedStats = ParseCommentsAndWhitespace.calcCommentsAndWhitespaceLinesTreeStats(file.toString(), srcStr.length(), parsedFileInfo.getLines(), parsedFileInfo.getDoc());
				filesStats.add(parsedStats);
			}
		}

		ParseDirectoryCodeFiles parsedRes = new ParseDirectoryCodeFiles(relativePath, filesStats);
		return parsedRes;
	}


	public static void main(String[] args) throws IOException {
		Path oldProjDir = Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/psor/ca");
		Path newProjDir = Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/ps/l/ca");

		String[] oldProjFileTypes = { ".cs", ".js", ".json", ".html", ".css" };
		val oldFiles = loadFiles(oldProjDir, oldProjFileTypes);
		val results = parseFileStats(oldProjDir, oldFiles);

		String[] newProjFileTypes = { ".cs", ".ts", ".json", ".html", ".css" };
		//val newFiles = loadFiles(newProjDir, newProjFileTypes);
		//val results = parseFiles(newProjDir, newFiles);

		File dstLog = new File("C:/Users/TeamworkGuy2/Documents/parsed-file-stats.txt");

		Json.getDefaultInst().setPrettyPrint(true);
		Json.stringify(results, dstLog);
	}

}
