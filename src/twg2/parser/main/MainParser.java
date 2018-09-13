package twg2.parser.main;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import lombok.val;
import twg2.io.files.FileFormatException;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.analytics.PerformanceTrackers;
import twg2.parser.workflow.ParserWorkflow;

/**
 * @author TeamworkGuy2
 * @since 2016-1-4
 */
public class MainParser {

	public static void main(String[] args) throws IOException, FileFormatException {
		boolean multithread = false;
		boolean logPerformance = false;
		ExecutorService executor = multithread ? Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) : null;
		PerformanceTrackers perfTracking = logPerformance ? new PerformanceTrackers() : null;
		FileReadUtil fileReader = FileReadUtil.threadLocalInst();

		// TODO for VisualVM pause
		//Scanner in = new Scanner(System.in);
		//System.out.print("press enter to continue: ");
		//in.nextLine();

		val parserWorkflow = ParserWorkflow.parseArgs(args);
		parserWorkflow.run(Level.INFO, executor, fileReader, perfTracking);

		// TODO for VisualVM pause
		//System.out.print("press enter to end: ");
		//in.nextLine();

		//ParseCodeFile.parseAndPrintCSharpFileInfo();
		//ParseCodeFile.parseAndPrintFileStats();

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
