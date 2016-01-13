package twg2.parser.main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import lombok.val;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2016-1-4
 */
public class MainParser {


	public static void parseAndValidProjectCsClasses() throws IOException {
		val fileSet = new ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, CsBlock>();
		val files1 = ParserMain.getFilesByExtension(Paths.get("/server/Services"), 1, "cs");
		val files2 = ParserMain.getFilesByExtension(Paths.get("/server/Entities"), 3, "cs");

		HashSet<List<String>> missingNamespaces = new HashSet<>();
		val files = new ArrayList<Path>();
		files.addAll(files1);
		files.addAll(files2);
		ParserMain.parseFileSet(files, fileSet);
		val resFileSet = ProjectClassSet.resolveClasses(fileSet, CsBlock.CLASS, missingNamespaces);

		val res = resFileSet.getCompilationUnitsStartWith(Arrays.asList("Corningstone", "Entities"));

		// get a subset of all the parsed files
		List<String> resFiles = new ArrayList<>();
		List<IntermClass.ResolvedImpl<CsBlock>> resClasses = new ArrayList<>();

		// fill indices with null so we can random access any valid index
		for(val classInfo : res) {
			//String classFqName = NameUtil.joinFqName(classInfo.getValue().getSignature().getFullyQualifyingName());
			resClasses.add(classInfo.getParsedClass());
			resFiles.add(classInfo.getId().getSrcName());
		}
		resClasses.sort((c1, c2) -> NameUtil.joinFqName(c1.getSignature().getFullyQualifyingName()).compareTo(NameUtil.joinFqName(c2.getSignature().getFullyQualifyingName())));

		val writeSettings = new WriteSettings(true, false, false);

		for(val classInfo : resClasses) {
			System.out.print("\"" + NameUtil.joinFqName(classInfo.getSignature().getFullyQualifyingName()) + "\": ");
			classInfo.toJson(System.out, writeSettings);
		}

		System.out.println("\n");
		System.out.println("files (" + resFiles.size() + " of " + files.size() + "): " + StringJoin.Objects.join(resFiles, "\n"));
		String[] nonSystemMissingNamespaces = missingNamespaces.stream().filter((ns) -> !"System".equals(ns.get(0))).map((ns) -> NameUtil.joinFqName(ns)).toArray((n) -> new String[n]);
		System.out.println("missing non-system namespaces: (" + nonSystemMissingNamespaces.length + "): " + Arrays.toString(nonSystemMissingNamespaces));
	}


	public static void main(String[] args) throws IOException {
		if(args.length > 0) {
			val parserWorkflow = ParserWorkflow.parseArgs(args);
			parserWorkflow.run(Level.INFO);
		}
		else {
			//parseAndPrintCSharpFileInfo();
			//parseAndPrintFileStats();
			parseAndValidProjectCsClasses();
		}
	}

}
