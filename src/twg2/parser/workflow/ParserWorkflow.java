package twg2.parser.workflow;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import twg2.ast.interm.classes.ClassAst;
import twg2.collections.builder.MapBuilder;
import twg2.collections.dataStructures.PairList;
import twg2.dateTimes.TimeUnitUtil;
import twg2.io.fileLoading.DirectorySearchInfo;
import twg2.io.fileLoading.SourceFiles;
import twg2.io.files.FileFormatException;
import twg2.io.files.FileReadUtil;
import twg2.io.json.stringify.JsonStringify;
import twg2.logging.LogPrefixFormat;
import twg2.logging.LogService;
import twg2.logging.LogServiceImpl;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.analytics.PerformanceTrackers;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.main.ParserMisc;
import twg2.parser.output.WriteSettings;
import twg2.parser.project.ProjectClassSet;
import twg2.text.stringUtils.StringSplit;
import twg2.text.stringUtils.StringTrim;

/**
 * @author TeamworkGuy2
 * @since 2016-1-9
 */
public class ParserWorkflow {
	static String newline = System.lineSeparator();

	List<DirectorySearchInfo> sources;
	List<DestinationInfo> destinations;
	Path logFile;


	public ParserWorkflow(List<DirectorySearchInfo> sources, List<DestinationInfo> destinations, Path log) {
		this.sources = sources;
		this.destinations = destinations;
		this.logFile = log;
	}


	public void run(Level logLevel, ExecutorService executor, FileReadUtil fileReader, PerformanceTrackers perfTracking) throws IOException, FileFormatException {
		// TODO educated guess at average namespace name parts
		NameUtil.estimatedFqPartsCount = 5;

		var missingNamespaces = new HashSet<List<String>>();
		var log = this.logFile != null ? new LogServiceImpl(logLevel, new PrintStream(this.logFile.toFile()), LogPrefixFormat.DATETIME_LEVEL_AND_CLASS) : null;

		var loadRes = SourceFiles.load(this.sources);
		if(log != null) {
			loadRes.log(log, logLevel, true);
		}

		// TODO debugging
		long start = System.nanoTime();

		ParsedResult parseRes = ParsedResult.parse(loadRes.getSources(), executor, fileReader, perfTracking);

		long end = System.nanoTime();

		if(perfTracking != null) {
			var readerStats = fileReader.getStats();
			// print out file reader stats
			System.out.println(readerStats.toString());
			// print out total files stats
			var fileSizes = perfTracking.getParseStats().entrySet().stream().mapToInt((entry) -> entry.getValue().getValue2());
			System.out.println("Loaded " + perfTracking.getParseStats().size() + " files, total " + fileSizes.sum() + " bytes");
		}

		// TODO debugging
		System.out.println("load() time: " + TimeUnitUtil.convert(TimeUnit.NANOSECONDS, (end - start), TimeUnit.MILLISECONDS) + " " + TimeUnitUtil.abbreviation(TimeUnit.MILLISECONDS, true, false));

		if(log != null) {
			parseRes.log(log, logLevel, true);
		}

		var resolvedRes = ResolvedResult.resolve(parseRes.compilationUnits, missingNamespaces);
		if(log != null) {
			resolvedRes.log(log, logLevel, true);
		}

		var filterRes = FilterResult.filter(resolvedRes.compilationUnits, this.destinations);
		if(log != null) {
			filterRes.log(log, logLevel, true);
		}

		WriteResult.write(filterRes.filterSets, missingNamespaces);
	}




	public static class DestinationInfo {
		String path;
		List<String> namespaces;


		@Override
		public String toString() {
			return path + ": " + namespaces.toString();
		}


		public static DestinationInfo parse(String str, String argName) {
			String[] values = StringSplit.split(str, "=", 2);

			if(values[0] == null) {
				throw new IllegalArgumentException("argument '" + argName + "' should contain an argument value");
			}

			var dstInfo = new DestinationInfo();
			dstInfo.namespaces = Collections.emptyList();
			dstInfo.path = values[0];

			if(values[1] != null) {
				if(!values[1].startsWith("[") || !values[1].endsWith("]")) {
					throw new IllegalArgumentException("'" + argName + "' value should be a '[namespace_string,..]'");
				}
				List<String> namespaces = StringSplit.split(values[1].substring(1, values[1].length() - 1), ',');
				dstInfo.namespaces = namespaces;
			}
			return dstInfo;
		}

	}




	public static class ParsedResult {
		/** The set of all parsed files */
		ProjectClassSet.Intermediate<BlockType> compilationUnits;


		@SuppressWarnings({ "unchecked" })
		public ParsedResult(ProjectClassSet.Intermediate<? extends BlockType> compilationUnits) {
			this.compilationUnits = (ProjectClassSet.Intermediate<BlockType>)compilationUnits;
		}


		public void log(LogService log, Level level, boolean includeHeader) {
			if(LogService.wouldLog(log, level)) {
				var files = compilationUnits.getCompilationUnitsStartWith(Arrays.asList(""));
				var fileSets = new HashMap<CodeFileSrc, List<ClassAst.SimpleImpl<BlockType>>>();
				for(var file : files) {
					List<ClassAst.SimpleImpl<BlockType>> fileSet = fileSets.get(file.id);
					if(fileSet == null) {
						fileSet = new ArrayList<>();
						fileSets.put(file.id, fileSet);
					}
					fileSet.add(file.parsedClass);
				}

				var sb = new StringBuilder();
				if(includeHeader) {
					sb.append(newline);
					sb.append("Classes/interfaces by file:");
					sb.append(newline);
				}
				for(var fileSet : fileSets.entrySet()) {
					JsonStringify.inst
						.propName(fileSet.getKey().srcName, sb)
						.toArray(fileSet.getValue(), sb, (f) -> NameUtil.joinFqName(f.getSignature().getFullName()))
						.append(newline, sb);
				}

				log.log(level, this.getClass(), sb.toString());
			}
		}


		public static ParsedResult parse(List<Entry<DirectorySearchInfo, List<Path>>> files, ExecutorService executor,
				FileReadUtil fileReader, PerformanceTrackers perfTracking) throws IOException, FileFormatException {
			var fileSet = new ProjectClassSet.Intermediate<BlockType>();

			for(var filesWithSrc : files) {
				ParserMisc.parseFileSet(filesWithSrc.getValue(), fileSet, executor, fileReader, perfTracking);
			}

			return new ParsedResult(fileSet);
		}

	}




	public static class ResolvedResult {
		/** The set of all resolved files (resolution converts simple type names to fully qualifying names for method parameters, fields, extended/implemented classes, etc.) */
		ProjectClassSet.Resolved<BlockType> compilationUnits;
		HashSet<List<String>> missingNamespaces = new HashSet<>();


		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ResolvedResult(ProjectClassSet.Resolved<? extends BlockType> compilationUnits,
				HashSet<List<String>> missingNamespaces) {
			this.compilationUnits = (ProjectClassSet.Resolved<BlockType>)(ProjectClassSet)compilationUnits;
		}


		public void log(LogService log, Level level, boolean includeHeader) {
			if(LogService.wouldLog(log, level)) {
				var files = compilationUnits.getCompilationUnitsStartWith(Arrays.asList(""));
				var fileSets = new HashMap<CodeFileSrc, List<ClassAst.ResolvedImpl<BlockType>>>();
				for(var file : files) {
					List<ClassAst.ResolvedImpl<BlockType>> fileSet = fileSets.get(file.id);
					if(fileSet == null) {
						fileSet = new ArrayList<>();
						fileSets.put(file.id, fileSet);
					}
					fileSet.add(file.parsedClass);
				}

				var sb = new StringBuilder();
				if(includeHeader) {
					sb.append(newline);
					sb.append("Resolved classes/interfaces by file:");
					sb.append(newline);
				}

				if(missingNamespaces.size() > 0) {
					sb.append("missingNamespaces: ");
					sb.append(missingNamespaces);
					sb.append(newline);
				}

				for(var fileSet : fileSets.entrySet()) {
					JsonStringify.inst
						.propNameUnquoted(fileSet.getKey().srcName, sb)
						.toArray(fileSet.getValue(), sb, (f) -> NameUtil.joinFqName(f.getSignature().getFullName()))
						.append(newline, sb);
				}

				log.log(level, this.getClass(), sb.toString());
			}
		}


		public static ResolvedResult resolve(ProjectClassSet.Intermediate<BlockType> simpleFileSet, HashSet<List<String>> missingNamespaces) throws IOException {
			// TODO shouldn't be using CsBlock, should use language block type
			var resFileSet = ProjectClassSet.resolveClasses(simpleFileSet, CsBlock.CLASS, missingNamespaces);

			return new ResolvedResult(resFileSet, missingNamespaces);
		}

	}




	public static class FilterResult {
		/** List of names associated with parser results */
		Map<DestinationInfo, List<CodeFileParsed.Resolved<BlockType>>> filterSets;


		@SuppressWarnings({ "unchecked", "rawtypes" })
		public FilterResult(Map<? extends DestinationInfo, ? extends List<? extends CodeFileParsed.Resolved<? extends BlockType>>> filterSets) {
			this.filterSets = (Map<DestinationInfo, List<CodeFileParsed.Resolved<BlockType>>>)(Map)filterSets;
		}


		public void log(LogService log, Level level, boolean includeHeader) {
			if(LogService.wouldLog(log, level)) {
				var sb = new StringBuilder();
				if(includeHeader) {
					sb.append(newline);
					sb.append("destination sets:");
					sb.append(newline);
				}

				for(var entry : filterSets.entrySet()) {
					sb.append(newline);
					sb.append(entry.getKey());
					sb.append(newline);
					JsonStringify.inst.join(entry.getValue(), newline, false, sb, (f) -> NameUtil.joinFqName(f.parsedClass.getSignature().getFullName()));
					sb.append(newline);
				}
				log.log(level, this.getClass(), sb.toString());
			}
		}


		public static FilterResult filter(ProjectClassSet.Resolved<BlockType> resFileSet, List<DestinationInfo> destinations) throws IOException {
			Map<DestinationInfo, List<CodeFileParsed.Resolved<BlockType>>> resSets = new HashMap<>();
			for(var dstInfo : destinations) {
				List<CodeFileParsed.Resolved<BlockType>> matchingNamespaces = new ArrayList<>();
				for(var namespace : dstInfo.namespaces) {
					var fileSet = resFileSet.getCompilationUnitsStartWith(StringSplit.split(namespace, '.'));
					matchingNamespaces.addAll(fileSet);
				}
				resSets.put(dstInfo, matchingNamespaces);
			}

			return new FilterResult(resSets);
		}

	}




	public static class WriteResult {

		public static void write(Map<DestinationInfo, List<CodeFileParsed.Resolved<BlockType>>> resSets, Collection<List<String>> missingNamespaces) throws IOException {
			var writeSettings = new WriteSettings(true, false, false, true);
			// associates file paths with how many times each has been written to (so we can append on subsequent writes)
			var definitionsByOutputFile = new HashMap<String, PairList<String, char[]>>();

			var tmpSb = new StringBuilder(2048);

			// write class definitions to JSON strings and group by output file
			for(var dstSet : resSets.entrySet()) {
				var dst = dstSet.getKey();
				var classes = dstSet.getValue();
				
				PairList<String, char[]> definitionStrs = definitionsByOutputFile.get(dst.path);
				if(definitionStrs == null) {
					definitionStrs = new PairList<>();
					definitionsByOutputFile.put(dst.path, definitionStrs);
				}

				for(var classInfo : classes) {
					tmpSb.setLength(0);
					String classNameFq = NameUtil.joinFqName(classInfo.parsedClass.getSignature().getFullName());
					tmpSb.append("\"" + classNameFq + "\": ");
					classInfo.parsedClass.toJson(tmpSb, writeSettings);
					char[] dstChars = new char[tmpSb.length()];
					tmpSb.getChars(0, tmpSb.length(), dstChars, 0);
					definitionStrs.add(classNameFq, dstChars);
				}
			}

			for(var dstData : definitionsByOutputFile.entrySet()) {
				List<Entry<String, char[]>> defs = new ArrayList<>(MapBuilder.mutable(dstData.getValue().keyList(), dstData.getValue().valueList(), true).entrySet());
				Collections.sort(defs, (c1, c2) -> c1.getKey().compareTo(c2.getKey()));

				try(var output = Files.newBufferedWriter(Paths.get(dstData.getKey()), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					boolean first = true;
					output.write("{\n\"files\": {");

					for(var def : defs) {
						if(!first) {
							output.append(",\n");
						}
						output.write(def.getValue(), 0, def.getValue().length);
						first = false;
					}

					output.write("}\n}");
					//String[] nonSystemMissingNamespaces = missingNamespaces.stream().filter((ns) -> !"System".equals(ns.get(0))).map((ns) -> NameUtil.joinFqName(ns)).toArray((n) -> new String[n]);
					//System.out.println("missing non-system namespaces: (" + nonSystemMissingNamespaces.length + "): " + Arrays.toString(nonSystemMissingNamespaces));
				} catch(IOException ioe) {
					throw ioe;
				}
			}
		}

	}




	public static ParserWorkflow parseArgs(String[] args) {
		if(args.length == 0 || Arrays.asList("-help", "--help", "-h").contains(args[0])) {
			System.out.println("An in-progress suite of parsing tools for C#, Java, and TypeScript source code.\n" +
				"Used to create basic ASTs containing class signatures, fields, and methods. (source: https://github.com/TeamworkGuy2/JParserTools)\n" +
				"example command:\n" +
				"-sources 'C:/Users/TeamworkGuy2/Documents/Projects/app/server/Services=1,[cs];" +
					"C:/Users/TeamworkGuy2/Documents/Projects/app/server/Entities=3,[cs]'" +
				" -destinations 'C:/Users/TeamworkGuy2/Downloads/Java_Programs/rsc/Services.json=[App.Services];" +
					"C:/Users/TeamworkGuy2/Downloads/Java_Programs/rsc/Models.json=[App.Entities]'" +
				" -log 'C:/Users/TeamworkGuy2/Downloads/Java_Programs/rsc/parser.log'");
		}

		Map<String, String> argNames = new HashMap<>();
		argNames.put("sources", "sources - a semicolon separated list of strings in the format 'path=depth,[fileExt,fileExt,...];path=depth,[fileExt,fileExt,...];...'.  Example: '/project/myApp/Models=3,[java,json]'");
		argNames.put("destinations", "destinations - a semicolon separated list of strings in the format 'path=[namespace,namespace,...], ...'.  Example: '/project/tmp_files/models.json=[MyApp.Models]'");
		argNames.put("log", "log - a log file path in the format 'path'.  Example: '/project/tmp_files/parser-log.log'");

		List<DirectorySearchInfo> srcs = new ArrayList<>();
		List<DestinationInfo> dsts = new ArrayList<>();
		Path log = null;

		// TODO debugging
		System.out.println("args:");
		for(int i = 0, size = args.length; i < size; i++) {
			System.out.println(args[i]);
		}
		System.out.println();

		for(int i = 0, size = args.length; i < size; i += 2) {
			String name = StringTrim.trimLeading(args[i], '-');
			String desc = argNames.get(name);
			if(desc != null) {
				if(i + 1 >= args.length) {
					throw new IllegalArgumentException("'" + name + "' is a valid argument name, should contain an argument");
				}

				List<String> values = StringSplit.split(args[i + 1], ';');

				if("sources".equals(name)) {
					for(var valueStr : values) {
						var value = DirectorySearchInfo.parseFromArgs(valueStr, "sources");
						srcs.add(value);
					}
				}

				if("destinations".equals(name)) {
					for(var valueStr : values) {
						var value = DestinationInfo.parse(valueStr, "destination");
						dsts.add(value);
					}
				}

				if("log".equals(name)) {
					log = Paths.get(values.get(0));
				}
			}
		}

		return new ParserWorkflow(srcs, dsts, log);
	}

}
