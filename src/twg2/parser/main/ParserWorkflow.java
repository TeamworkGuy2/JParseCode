package twg2.parser.main;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
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

import dataUtils.TimeUnitUtil;
import lombok.AllArgsConstructor;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.arrays.ArrayUtil;
import twg2.collections.tuple.Tuples;
import twg2.io.files.FileReadUtil;
import twg2.io.log.Logging;
import twg2.io.log.LoggingImpl;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileParsed;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.output.JsonWrite;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringSplit;
import twg2.text.stringUtils.StringTrim;

/**
 * @author TeamworkGuy2
 * @since 2016-1-9
 */
public class ParserWorkflow {
	private static String newline = System.lineSeparator();

	List<SourceInfo> sources;
	List<DestinationInfo> destinations;
	Path logFile;


	public ParserWorkflow(List<SourceInfo> sources, List<DestinationInfo> destinations, Path log) {
		this.sources = sources;
		this.destinations = destinations;
		this.logFile = log;
	}


	public void run(Level logLevel, ExecutorService executor, FileReadUtil fileReader) throws IOException {
		HashSet<List<String>> missingNamespaces = new HashSet<>();
		Logging log = this.logFile != null ? new LoggingImpl(logLevel, new PrintStream(this.logFile.toFile()), LoggingImpl.PrefixFormat.DATETIME_LEVEL_AND_CLASS) : null;

		try {
			val loadRes = LoadResult.load(this.sources);
			if(log != null) {
				loadRes.log(log, logLevel, true);
			}

			// TODO debugging
			long start = System.nanoTime();

			val parseRes = ParsedResult.parse(loadRes.sources, executor, fileReader);

			// TODO debugging
			System.out.println("load() time: " + TimeUnitUtil.convert(TimeUnit.NANOSECONDS, (System.nanoTime() - start), TimeUnit.MILLISECONDS) + " " + TimeUnitUtil.abbreviation(TimeUnit.MILLISECONDS, true, false));

			if(log != null) {
				parseRes.log(log, logLevel, true);
			}

			val resolvedRes = ResolvedResult.resolve(parseRes.compilationUnits, missingNamespaces);
			if(log != null) {
				resolvedRes.log(log, logLevel, true);
			}

			val filterRes = FilterResult.filter(resolvedRes.compilationUnits, this.destinations);
			if(log != null) {
				filterRes.log(log, logLevel, true);
			}

			WriteResult.write(filterRes.filterSets, missingNamespaces);

		} catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}




	public static class SourceInfo {
		private static int DEFAULT_MAX_RECURSIVE_DEPTH = 20;
		String path;
		int maxRecursiveDepth;
		String[] validFileExtensions;


		@Override
		public String toString() {
			return path + "=" + maxRecursiveDepth + (validFileExtensions.length > 0 ? ",[" + String.join(",", validFileExtensions) + "]" : "");
		}


		public static SourceInfo parse(String str, String argName) {
			val values = StringSplit.split(str, "=", 2);

			if(values[0] == null) {
				throw new IllegalArgumentException("argument '" + argName + "' should contain an argument value");
			}

			val srcInfo = new SourceInfo();
			srcInfo.validFileExtensions = ArrayUtil.EMPTY_STRING_ARRAY;
			srcInfo.maxRecursiveDepth = DEFAULT_MAX_RECURSIVE_DEPTH;
			srcInfo.path = values[0];

			if(values[1] != null) {
				val depthAndExtensions = StringSplit.split(values[1], ',', new String[2]);
				int idx = 0;
				if(depthAndExtensions[0] != null) {
					val depth = Integer.parseInt(depthAndExtensions[0]);
					srcInfo.maxRecursiveDepth = depth;
					idx++;
				}

				val extensionsAryStr = depthAndExtensions[idx];
				if(extensionsAryStr != null) {
					if(!extensionsAryStr.startsWith("[") || !extensionsAryStr.endsWith("]")) {
						throw new IllegalArgumentException("second portion of '" + argName + "' value should be a '[file_extension_string,..]'");
					}
					val extensions = StringSplit.split(extensionsAryStr.substring(1, extensionsAryStr.length() - 1), ',');
					srcInfo.validFileExtensions = extensions.toArray(new String[extensions.size()]);
				}
			}
			return srcInfo;
		}

	}




	public static class DestinationInfo {
		String path;
		List<String> namespaces;


		@Override
		public String toString() {
			return path + ": " + namespaces.toString();
		}


		public static DestinationInfo parse(String str, String argName) {
			val values = StringSplit.split(str, "=", 2);

			if(values[0] == null) {
				throw new IllegalArgumentException("argument '" + argName + "' should contain an argument value");
			}

			val dstInfo = new DestinationInfo();
			dstInfo.namespaces = Collections.emptyList();
			dstInfo.path = values[0];

			if(values[1] != null) {
				if(!values[1].startsWith("[") || !values[1].endsWith("]")) {
					throw new IllegalArgumentException("'" + argName + "' value should be a '[namespace_string,..]'");
				}
				val namespaces = StringSplit.split(values[1].substring(1, values[1].length() - 1), ',');
				dstInfo.namespaces = namespaces;
			}
			return dstInfo;
		}

	}




	@AllArgsConstructor
	@Immutable
	public static class LoadResult {
		final List<Entry<SourceInfo, List<Path>>> sources;


		public void log(Logging log, Level level, boolean includeHeader) {
			if(Logging.wouldLog(log, level)) {
				val sb = new StringBuilder();
				if(includeHeader) {
					sb.append(newline);
					sb.append("files by source:");
					sb.append(newline);
				}
				for(val src : sources) {
					sb.append(newline);
					sb.append(src.getKey());
					sb.append(newline);
					JsonWrite.joinStrConsumer(src.getValue(), newline, sb, (f) -> sb.append(f.toString()));
					sb.append(newline);
				}

				log.log(level, this.getClass(), sb.toString());
			}
		}


		public static LoadResult load(List<SourceInfo> sourceInfos) throws IOException {
			val allFiles = new ArrayList<Entry<SourceInfo, List<Path>>>();

			for(val srcInfo : sourceInfos) {
				val fileSet = ParserMain.getFilesByExtension(Paths.get(srcInfo.path), srcInfo.maxRecursiveDepth, srcInfo.validFileExtensions);
				allFiles.add(Tuples.of(srcInfo, fileSet));
			}

			return new LoadResult(Collections.unmodifiableList(allFiles));
		}

	}




	public static class ParsedResult {
		/** The set of all parsed files */
		ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, CompoundBlock> compilationUnits; 

		public ParsedResult(ProjectClassSet.Simple<? extends CodeFileSrc<? extends CodeLanguage>, ? extends CompoundBlock> compilationUnits) {
			this.compilationUnits = (ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, CompoundBlock>)(ProjectClassSet)compilationUnits;
		}


		public void log(Logging log, Level level, boolean includeHeader) {
			if(Logging.wouldLog(log, level)) {
				val files = compilationUnits.getCompilationUnitsStartWith(Arrays.asList(""));
				val fileSets = new HashMap<CodeFileSrc<CodeLanguage>, List<IntermClass.SimpleImpl<CompoundBlock>>>();
				for(val file : files) {
					List<IntermClass.SimpleImpl<CompoundBlock>> fileSet = fileSets.get(file.getId());
					if(fileSet == null) {
						fileSet = new ArrayList<>();
						fileSets.put(file.getId(), fileSet);
					}
					fileSet.add(file.getParsedClass());
				}

				val sb = new StringBuilder();
				if(includeHeader) {
					sb.append(newline);
					sb.append("Classes/interfaces by file:");
					sb.append(newline);
				}
				for(val fileSet : fileSets.entrySet()) {
					sb.append(fileSet.getKey().getSrcName());
					sb.append(": [");
					JsonWrite.joinStrConsumer(fileSet.getValue(), ", ", sb, (f) -> sb.append(NameUtil.joinFqName(f.getSignature().getFullName())));
					sb.append("]" + newline);
				}

				log.log(level, this.getClass(), sb.toString());
			}
		}


		public static ParsedResult parse(List<Entry<SourceInfo, List<Path>>> files, ExecutorService executor, FileReadUtil fileReader) throws IOException {
			val fileSet = new ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, CompoundBlock>();

			for(val filesWithSrc : files) {
				ParserMain.parseFileSet(filesWithSrc.getValue(), fileSet, executor, fileReader);
			}

			return new ParsedResult(fileSet);
		}

	}




	public static class ResolvedResult {
		/** The set of all resolved files (resolution converts simple type names to fully qualifying names for method parameters, fields, extended/implemented classes, etc.) */
		ProjectClassSet.Resolved<CodeFileSrc<CodeLanguage>, CompoundBlock> compilationUnits;
		HashSet<List<String>> missingNamespaces = new HashSet<>();


		public ResolvedResult(ProjectClassSet.Resolved<? extends CodeFileSrc<? extends CodeLanguage>, ? extends CompoundBlock> compilationUnits,
				HashSet<List<String>> missingNamespaces) {
			this.compilationUnits = (ProjectClassSet.Resolved<CodeFileSrc<CodeLanguage>, CompoundBlock>)(ProjectClassSet)compilationUnits;
		}


		public void log(Logging log, Level level, boolean includeHeader) {
			if(Logging.wouldLog(log, level)) {
				val files = compilationUnits.getCompilationUnitsStartWith(Arrays.asList(""));
				val fileSets = new HashMap<CodeFileSrc<CodeLanguage>, List<IntermClass.ResolvedImpl<CompoundBlock>>>();
				for(val file : files) {
					List<IntermClass.ResolvedImpl<CompoundBlock>> fileSet = fileSets.get(file.getId());
					if(fileSet == null) {
						fileSet = new ArrayList<>();
						fileSets.put(file.getId(), fileSet);
					}
					fileSet.add(file.getParsedClass());
				}

				val sb = new StringBuilder();
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

				for(val fileSet : fileSets.entrySet()) {
					sb.append(fileSet.getKey().getSrcName());
					sb.append(": [");
					JsonWrite.joinStrConsumer(fileSet.getValue(), ", ", sb, (f) -> sb.append(NameUtil.joinFqName(f.getSignature().getFullName())));
					sb.append("]" + newline);
				}

				log.log(level, this.getClass(), sb.toString());
			}
		}


		public static ResolvedResult resolve(ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, CompoundBlock> simpleFileSet, HashSet<List<String>> missingNamespaces) throws IOException {
			val resFileSet = ProjectClassSet.resolveClasses(simpleFileSet, CsBlock.CLASS, missingNamespaces);

			return new ResolvedResult(resFileSet, missingNamespaces);
		}

	}




	public static class FilterResult {
		/** List of names associated with parser results */
		Map<DestinationInfo, List<CodeFileParsed.Resolved<CodeFileSrc<CodeLanguage>, CompoundBlock>>> filterSets;

		public FilterResult(Map<? extends DestinationInfo, ? extends List<? extends CodeFileParsed.Resolved<? extends CodeFileSrc<? extends CodeLanguage>, ? extends CompoundBlock>>> filterSets) {
			this.filterSets = (Map<DestinationInfo, List<CodeFileParsed.Resolved<CodeFileSrc<CodeLanguage>, CompoundBlock>>>)(Map)filterSets;
		}


		public void log(Logging log, Level level, boolean includeHeader) {
			if(Logging.wouldLog(log, level)) {
				val sb = new StringBuilder();
				if(includeHeader) {
					sb.append(newline);
					sb.append("destination sets:");
					sb.append(newline);
				}

				for(val entry : filterSets.entrySet()) {
					sb.append(newline);
					sb.append(entry.getKey());
					sb.append(newline);
					JsonWrite.joinStrConsumer(entry.getValue(), newline, sb, (f) -> sb.append(NameUtil.joinFqName(f.getParsedClass().getSignature().getFullName())));
					sb.append(newline);
				}
				log.log(level, this.getClass(), sb.toString());
			}
		}


		public static FilterResult filter(ProjectClassSet.Resolved<CodeFileSrc<CodeLanguage>, CompoundBlock> resFileSet, List<DestinationInfo> destinations) throws IOException {
			Map<DestinationInfo, List<CodeFileParsed.Resolved<CodeFileSrc<CodeLanguage>, CompoundBlock>>> resSets = new HashMap<>();
			for(val dstInfo : destinations) {
				List<CodeFileParsed.Resolved<CodeFileSrc<CodeLanguage>, CompoundBlock>> matchingNamespaces = new ArrayList<>();
				for(val namespace : dstInfo.namespaces) {
					val fileSet = resFileSet.getCompilationUnitsStartWith(StringSplit.split(namespace, '.'));
					matchingNamespaces.addAll(fileSet);
				}
				resSets.put(dstInfo, matchingNamespaces);
			}

			return new FilterResult(resSets);
		}

	}




	public static class WriteResult {

		public static void write(Map<DestinationInfo, List<CodeFileParsed.Resolved<CodeFileSrc<CodeLanguage>, CompoundBlock>>> resSets, Collection<List<String>> missingNamespaces) throws IOException {
			// get a subset of all the parsed files
			val writeSettings = new WriteSettings(true, false, false);

			// fill indices with null so we can random access any valid index
			for(val dstSet : resSets.entrySet()) {
				val dst = dstSet.getKey();
				val classes = dstSet.getValue();
				val resClasses = new ArrayList<>(classes);
				
				resClasses.sort((c1, c2) -> NameUtil.joinFqName(c1.getParsedClass().getSignature().getFullName()).compareTo(NameUtil.joinFqName(c2.getParsedClass().getSignature().getFullName())));

				try(val output = Files.newBufferedWriter(Paths.get(dst.path), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

					boolean first = true;
					output.write("{\n\"files\": {");
					for(val classInfo : resClasses) {
						if(!first) {
							output.append(",\n");
						}
						output.append("\"" + NameUtil.joinFqName(classInfo.getParsedClass().getSignature().getFullName()) + "\": ");
						classInfo.getParsedClass().toJson(output, writeSettings);
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
		if(Arrays.asList("-help", "--help", "-h").contains(args[0])) {
			System.out.println("An in-progress suite of parsing tools for C#, Java, and TypeScript source code.\n" +
				"Used to create basic ASTs containing class signatures, fields, and methods. (source: https://github.com/TeamworkGuy2/JParserTools)\n" +
				"example command:\n" +
				"-sources 'C:/Users/TeamworkGuy2/Documents/Projects/app/server/Services=1,[cs];" +
					"C:/Users/TeamworkGuy2/Documents/Projects/app/server/Entities=3,[cs]'" +
				" -destinations 'C:/Users/TeamworkGuy2/Downloads/Java_Programs/rsc/Services.json=[Corningstone.Services];" +
					"C:/Users/TeamworkGuy2/Downloads/Java_Programs/rsc/Models.json=[Corningstone.Entities]'" +
				" -log 'C:/Users/TeamworkGuy2/Downloads/Java_Programs/rsc/parser.log'");
		}

		Map<String, String> argNames = new HashMap<>();
		argNames.put("sources", "sources - a semicolon separated list of strings in the format 'path_string[=depth_int[,[file_extension_string], ..], ..]'.  Example: '/project/myApp/Models=3,[java,json]'");
		argNames.put("destinations", "destinations - a semicolon separated list of strings in the format 'path_string[=[namespace_string], ..]'.  Example: '/project/tmp_files/models.json=[MyApp.Models]'");
		argNames.put("log", "log - a log file path in the format 'path_string'.  Example: '/project/tmp_files/parser-log.log'");

		List<SourceInfo> srcs = new ArrayList<>();
		List<DestinationInfo> dsts = new ArrayList<>();
		Path log = null;

		// TODO debugging
		System.out.println("args:");
		for(int i = 0, size = args.length; i < size; i++) {
			System.out.println(args[i]);
		}
		System.out.println();

		for(int i = 0, size = args.length; i < size; i += 2) {
			val name = StringTrim.trimLeading(args[i], '-');
			val desc = argNames.get(name);
			if(desc != null) {
				if(i + 1 >= args.length) {
					throw new IllegalArgumentException("'" + name + "' is a valid argument name, should contain an argument");
				}

				val values = StringSplit.split(args[i + 1], ';');

				if("sources".equals(name)) {
					for(val valueStr : values) {
						val value = SourceInfo.parse(valueStr, "sources");
						srcs.add(value);
					}
				}

				if("destinations".equals(name)) {
					for(val valueStr : values) {
						val value = DestinationInfo.parse(valueStr, "destination");
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
