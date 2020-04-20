package twg2.parser.main;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import twg2.io.files.FileFormatException;
import twg2.parser.codeParser.analytics.PerformanceTrackers;
import twg2.parser.output.WriteSettings;
import twg2.parser.workflow.ParserWorkflow;

/**
 * @author TeamworkGuy2
 * @since 2016-1-4
 */
// TODO make extractClassFieldsAndMethodSignatures() easier to call
// see test/utils/CodeFileAndAst.java (x1)
// see parser/main/ParserMisc.java (x3)
public class MainParser {

	public static void main(String[] args) throws IOException, FileFormatException {
		var parserWorkflow = ParserWorkflow.parseArgs(args);
		int threads = parserWorkflow.getThreadCount();
		boolean logPerformance = parserWorkflow.isDebug();
		ExecutorService executor = threads > 1 ? Executors.newFixedThreadPool(threads) : null;
		PerformanceTrackers perfTracking = logPerformance ? new PerformanceTrackers() : null;

		// TODO for VisualVM pause
		//java.util.Scanner in = new java.util.Scanner(System.in);
		//System.out.print("press enter to continue: ");
		//in.nextLine();

		parserWorkflow.run(Level.INFO, executor, perfTracking);

		// TODO for VisualVM pause
		//System.out.print("press enter to end: ");
		//in.nextLine();

		//ParseCodeFile.parseAndPrintCSharpFileInfo();
		//ParseCodeFile.parseAndPrintFileStats();

		if(logPerformance) {
			System.out.println("FileTokenizer identifier check cnt=" + twg2.parser.codeParser.csharp.CsFileTokenizer.cnt);
			System.out.println("BlockExtractor accept next token cnt=" + twg2.parser.codeParser.extractors.BlockExtractor.acceptNextCalls);

			System.out.println("\n==== Parse Timings (slowest 10 in millis) ====");
			var perfData = perfTracking.getTopParseTimes(true, -10);
			System.out.println(PerformanceTrackers.toString(perfData.iterator()));

			System.out.println("\n==== Parse Step Details (slowest 10 in millis) ====");
			perfData = perfTracking.getTopParseStepDetails(true, -10);
			System.out.println(PerformanceTrackers.toString(perfData.iterator()));

			//System.out.println("\n==== All Performance Data ====\n");
			//var writeSettings = new WriteSettings(true, false, false, true);
			//perfTracking.toJson(System.out, writeSettings);
		}

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
