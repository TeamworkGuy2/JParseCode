package twg2.parser.main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import lombok.val;
import twg2.ast.interm.classes.ClassAst;
import twg2.io.fileLoading.SourceFiles;
import twg2.io.files.FileFormatException;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.analytics.PerformanceTrackers;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguage;
import twg2.parser.output.WriteSettings;
import twg2.parser.project.ProjectClassSet;
import twg2.parser.workflow.CodeFileSrc;
import twg2.parser.workflow.ParserWorkflow;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2016-1-4
 */
public class MainParser {

	public static void parseAndValidProjectCsClasses(FileReadUtil fileReader) throws IOException, FileFormatException {
		val fileSet = new ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, CsBlock>();
		val files1 = SourceFiles.getFilesByExtension(Paths.get("server/Services"), 1, "cs");
		val files2 = SourceFiles.getFilesByExtension(Paths.get("server/Entities"), 3, "cs");

		HashSet<List<String>> missingNamespaces = new HashSet<>();
		val files = new ArrayList<Path>();
		files.addAll(files1);
		files.addAll(files2);

		ExecutorService executor = null;
		PerformanceTrackers perfTracking = null;
		ParserMisc.parseFileSet(files, fileSet, executor, fileReader, perfTracking);
		val resFileSet = ProjectClassSet.resolveClasses(fileSet, CsBlock.CLASS, missingNamespaces);

		val res = resFileSet.getCompilationUnitsStartWith(Arrays.asList("Corningstone", "Entities"));

		// get a subset of all the parsed files
		List<String> resFiles = new ArrayList<>();
		List<ClassAst.ResolvedImpl<CsBlock>> resClasses = new ArrayList<>();

		// fill indices with null so we can random access any valid index
		for(val classInfo : res) {
			//String classFqName = NameUtil.joinFqName(classInfo.getValue().getSignature().getFullName());
			resClasses.add(classInfo.getParsedClass());
			resFiles.add(classInfo.getId().getSrcName());
		}
		resClasses.sort((c1, c2) -> NameUtil.joinFqName(c1.getSignature().getFullName()).compareTo(NameUtil.joinFqName(c2.getSignature().getFullName())));

		val writeSettings = new WriteSettings(true, false, false, false);

		for(val classInfo : resClasses) {
			System.out.print("\"" + NameUtil.joinFqName(classInfo.getSignature().getFullName()) + "\": ");
			classInfo.toJson(System.out, writeSettings);
		}

		System.out.println("\n");
		System.out.println("files (" + resFiles.size() + " of " + files.size() + "): " + StringJoin.join(resFiles, "\n"));
		String[] nonSystemMissingNamespaces = missingNamespaces.stream().filter((ns) -> !"System".equals(ns.get(0))).map((ns) -> NameUtil.joinFqName(ns)).toArray((n) -> new String[n]);
		System.out.println("missing non-system namespaces: (" + nonSystemMissingNamespaces.length + "): " + Arrays.toString(nonSystemMissingNamespaces));
	}


	public static void main(String[] args) throws IOException, FileFormatException {
		boolean multithread = false;
		boolean logPerformance = false;
		ExecutorService executor = multithread ? Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) : null;
		PerformanceTrackers perfTracking = logPerformance ? new PerformanceTrackers() : null;
		FileReadUtil fileReader = FileReadUtil.threadLocalInst();

		if(args.length > 0) {
			// TODO for VisualVM pause
			//Scanner in = new Scanner(System.in);
			//System.out.print("press enter to continue: ");
			//in.nextLine();

			val parserWorkflow = ParserWorkflow.parseArgs(args);
			parserWorkflow.run(Level.INFO, executor, fileReader, perfTracking);

			// TODO for VisualVM pause
			//System.out.print("press enter to end: ");
			//in.nextLine();
		}
		else {
			//ParseCodeFile.parseAndPrintCSharpFileInfo();
			//ParseCodeFile.parseAndPrintFileStats();
			parseAndValidProjectCsClasses(fileReader);
		}

		/*
		String perfData = null;
		System.out.println();
		perfData = PerformanceTrackers.toString(perfTracking.getTopParseTimes(SortOrder.ASCENDING, -10).iterator());
		System.out.println(perfData);
		System.out.println("====\n");
		perfData = PerformanceTrackers.toString(perfTracking.getTopParseStepDetails(SortOrder.ASCENDING, -10).iterator());
		System.out.println(perfData);
		*/

		//val writeSettings = new WriteSettings(true, false, false, true);
		//perfTracker.toJson(System.out, writeSettings);

		if(executor != null) {
			executor.shutdown();
		}
	}


	// JS code to get stats from ParserPerformanceTracker JSON output
	/*
	var a = [parserPerformanceTrackerJson...];
	var map = {};
	a.forEach((ary) => {
	  ary.forEach((ent) => {
	    var b = map[ent.file] || { file: ent.file, units: ent.units, setup: 0, load: 0, tokenize: 0, parse: 0, total: 0, setupAry: [], loadAry: [], tokenizeAry: [], parseAry: [] };
	    map[ent.file] = b;
	    b.setup += ent.setup;
	    b.load += ent.load;
	    b.tokenize += ent.tokenize;
	    b.parse += ent.parse;
	    b.setupAry.push(ent.setup);
	    b.loadAry.push(ent.load);
	    b.tokenizeAry.push(ent.tokenize);
	    b.parseAry.push(ent.parse);
	    b.total += ent.setup + ent.load + ent.tokenize + ent.parse;
	  })
	})
	var b = Object.keys(map).map((key) => map[key])
		.sort((a,b) => b.total - a.total)
		.map((ent) => ({ file: ent.file, total: ent.total / ent.setupAry.length + " " + ent.units }))
		.slice(0, 10);
	JSON.stringify(b, null, '  ')
	 */

}
