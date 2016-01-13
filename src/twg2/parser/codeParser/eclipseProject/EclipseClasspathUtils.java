package twg2.parser.codeParser.eclipseProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.xml.stream.XMLStreamException;

import lombok.val;
import twg2.collections.util.dataStructures.PairList;
import twg2.io.serialize.xml.XmlHandler;
import twg2.parser.codeParser.eclipseProject.EclipseClasspathFile.ClassPathEntry;
import twg2.text.stringUtils.StringCase;
import twg2.text.stringUtils.StringSplit;

/**
 * @author TeamworkGuy2
 * @since 2015-5-31
 */
public class EclipseClasspathUtils {

	public static Map<String, EclipseClasspathFile> loadProjectClasspathFiles(File projectsDir) throws FileNotFoundException, IOException, XMLStreamException {
		File[] dirs = projectsDir.listFiles((file) -> file.isDirectory() && !file.getName().startsWith("."));
		Map<String, EclipseClasspathFile> projectFiles = new HashMap<>();

		for(File proj : dirs) {
			File file = new File(proj, ".classpath");
			EclipseClasspathFile parsedCp = new EclipseClasspathFile(file);
			parsedCp.readXML(XmlHandler.createXMLReader(new FileReader(file), true, true, true));
			projectFiles.put(proj.getName(), parsedCp);
		}

		return projectFiles;
	}


	public static Map<String, EclipseClasspathFile> getProjectsContainingLibs(Map<String, EclipseClasspathFile> projectFiles, String type, List<String> containsList, boolean containsAll) throws FileNotFoundException, IOException, XMLStreamException {
		Map<String, EclipseClasspathFile> res = new HashMap<>();

		for(Entry<String, EclipseClasspathFile> cpFileEntry : projectFiles.entrySet()) {
			EclipseClasspathFile cpFile = cpFileEntry.getValue();
			// the list of project imports
			List<String> importJars = new ArrayList<>(Arrays.asList(cpFile.getClassPathEntries(type).stream().map((cpe) -> {
				String[] pathParts = cpe.path.split("/");
				return pathParts[pathParts.length - 1];
			}).toArray((num) -> new String[num])));

			// the list of does contain imports
			if(containsAll ? importJars.containsAll(containsList) : containsAny(importJars, containsList)) {
				res.put(cpFile.file.getParentFile().getName(), cpFile);
			}
		}
		return res;
	}


	/**
	 * @param type the type of class path entry imports to compare against, null to compare against all
	 * @return a map of project directories to list of imports from the {@code doesContain} list that do not appear in that project's imports
	 */
	public static PairList<String, List<String>> getProjectsContainingLibsMissingLibs(Map<String, EclipseClasspathFile> projectFiles, String type, List<String> ifContainsList, List<String> doesContain) throws FileNotFoundException, IOException, XMLStreamException {
		PairList<String, List<String>> resultNotContain = new PairList<>();

		for(Entry<String, EclipseClasspathFile> cpFileEntry : projectFiles.entrySet()) {
			EclipseClasspathFile cpFile = cpFileEntry.getValue();
			// the list of project imports
			List<String> importJars = new ArrayList<>(Arrays.asList(cpFile.getClassPathEntries(type).stream().map((cpe) -> {
				String[] pathParts = cpe.path.split("/");
				return pathParts[pathParts.length - 1];
			}).toArray((num) -> new String[num])));

			List<String> notContainList = new ArrayList<>();
			// the list of does contain imports
			if(importJars.containsAll(ifContainsList)) {
				for(String importStr : doesContain) {
					if(!importJars.contains(importStr)) {
						notContainList.add(importStr);
					}
				}
				if(notContainList.size() > 0) {
					resultNotContain.add(cpFile.file.getParentFile().getName(), notContainList);
				}
			}
		}
		return resultNotContain;
	}


	public static void printProjectDependencyTree(File projectsDir, String projName) throws FileNotFoundException, IOException, XMLStreamException {
		Map<String, EclipseClasspathFile> projectFiles = loadProjectClasspathFiles(projectsDir);
		StringBuilder tmpSb = new StringBuilder();
		_printProjectDependencyTree(projectFiles, projName, tmpSb);
	}


	public static void _printProjectDependencyTree(Map<String, EclipseClasspathFile> classPathFiles, String projName, StringBuilder indent) throws FileNotFoundException, IOException, XMLStreamException {
		boolean found = false;
		for(Entry<String, EclipseClasspathFile> cpFileEntry : classPathFiles.entrySet()) {
			EclipseClasspathFile cpFile = cpFileEntry.getValue();

			if(cpFileEntry.getKey().equals(projName)) {
				found = true;
				for(ClassPathEntry cpEntry : cpFile.classPathEntries) {
					if(ClassPathEntry.isLib(cpEntry)) {
						String fileName = StringSplit.firstMatch(StringSplit.lastMatch(cpEntry.path, '/'), '.');
						String possibleProjName = StringCase.toTitleCase(fileName);
						if(possibleProjName.startsWith("J") && !possibleProjName.startsWith("Json") && !possibleProjName.startsWith("Jackson")) {
							possibleProjName = "" + Character.toUpperCase(possibleProjName.charAt(0)) + Character.toUpperCase(possibleProjName.charAt(1)) + possibleProjName.substring(2);
						}

						System.out.println(indent.toString() + possibleProjName);

						indent.append("\t");
						_printProjectDependencyTree(classPathFiles, possibleProjName, indent);
						indent.setLength(indent.length() - 1);
					}
				}
			}
		}

		if(!found) {
			System.out.println("!could not find: '" + projName + "'");
		}
	}


	private static <T> boolean containsAny(Collection<T> src, Collection<T> containsAny) {
		for(T elem : containsAny) {
			if(src.contains(elem)) {
				return true;
			}
		}
		return false;
	}


	private static <T, R> List<R> printSorted(Collection<T> coll, Function<T, R> func, Comparator<R> compare) {
		List<R> res = new ArrayList<>();
		for(val entry : coll) {
			res.add(func.apply(entry));
		}

		res.sort(compare);

		for(R r : res) {
			System.out.println(r);
		}

		return res;
	}


	public static void printProjectsContainingLibs(File projects) throws FileNotFoundException, IOException, XMLStreamException {
		List<String> expectImports = Arrays.asList("data_transfer.jar");
		boolean containsAll = true;

		Map<String, EclipseClasspathFile> projectFiles = loadProjectClasspathFiles(projects);
		Map<String, EclipseClasspathFile> projFiles = getProjectsContainingLibs(projectFiles, null, expectImports, containsAll);
		val res = printSorted(projFiles.entrySet(), (p) -> p.getKey(), (a, b) -> a.compareTo(b));

		System.out.println("\ncount: " + res.size() + " (total: " + projectFiles.size() + ")");
	}


	public static void printProjectsContainingLibsMissingLibs(File projects) throws FileNotFoundException, IOException, XMLStreamException {
		List<String> expectImports = Arrays.asList("jrange.jar");
		List<String> doesContain = Arrays.asList("");
		//List<String> expectImports = Arrays.asList("jdata_util.jar", "jtext_util.jar", "type_util.jar", "jstream_util.jar", "ranges_util.jar", "jcollection_util.jar", "jfunction_util.jar", "parser_string.jar");
		//List<String> doesContain = Arrays.asList("io_util");

		Map<String, EclipseClasspathFile> projectFiles = loadProjectClasspathFiles(projects);
		PairList<String, List<String>> res = getProjectsContainingLibsMissingLibs(projectFiles, null, expectImports, doesContain);

		for(int i = 0, size = res.size(); i < size; i++) {
			System.out.println("project: " + res.getKey(i));
			for(String missingImport : res.getValue(i)) {
				System.out.println("\tmissing: " + missingImport);
			}
		}
		System.out.println("\ncount: " + res.size());
	}


	public static void main(String[] args) throws FileNotFoundException, IOException, XMLStreamException {
		File projects = new File("C:/Users/TeamworkGuy2/Documents/Java/Projects");

		//printProjectDependencyTree(projects, "ParserTools");
		//printProjectsContainingLibsMissingLibs(projects);
		printProjectsContainingLibs(projects);
	}

}
