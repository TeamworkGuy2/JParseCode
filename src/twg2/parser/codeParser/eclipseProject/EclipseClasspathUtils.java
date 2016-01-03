package twg2.parser.codeParser.eclipseProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import twg2.collections.util.dataStructures.PairList;
import twg2.io.serialize.xml.XmlHandler;

/**
 * @author TeamworkGuy2
 * @since 2015-5-31
 */
public class EclipseClasspathUtils {


	/**
	 * @param type the type of class path entry imports to compare against, null to compare against all
	 * @return a map of project directories to list of imports from the {@code doesContain} list that do not appear in that project's imports
	 */
	public static PairList<String, List<String>> getProjectsContainingLibsMissingLibs(File projectsDir, String type, List<String> ifContainsList, List<String> doesContain) throws FileNotFoundException, IOException, XMLStreamException {
		File[] dirs = projectsDir.listFiles((file) -> file.isDirectory() && !file.getName().startsWith("."));
		List<EclipseClasspathFile> projectFiles = new ArrayList<>();
		PairList<String, List<String>> resultNotContain = new PairList<>();

		for(File proj : dirs) {
			File file = new File(proj, ".classpath");
			EclipseClasspathFile parsedCp = new EclipseClasspathFile(file);
			parsedCp.readXML(XmlHandler.createXMLReader(new FileReader(file), true, true, true));
			projectFiles.add(parsedCp);

			// the list of project imports
			List<String> importJars = new ArrayList<>(Arrays.asList(parsedCp.getClassPathEntries(type).stream().map((cpe) -> {
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
					resultNotContain.add(parsedCp.file.getParentFile().getName(), notContainList);
				}
			}
		}
		return resultNotContain;
	}


	public static void main(String[] args) throws FileNotFoundException, IOException, XMLStreamException {
		File projects = new File("C:/Users/TeamworkGuy2/Documents/Java/Projects");
		//List<String> expectImports = Arrays.asList("jdata_util.jar", "jtext_util.jar", "type_util.jar", "jstream_util.jar", "ranges_util.jar", "jcollection_util.jar", "jfunction_util.jar", "parser_string.jar");
		//List<String> doesContain = Arrays.asList("io_util");
		List<String> expectImports = Arrays.asList("jrange.jar");
		List<String> doesContain = Arrays.asList("");

		PairList<String, List<String>> res = getProjectsContainingLibsMissingLibs(projects, null, expectImports, doesContain);

		for(int i = 0, size = res.size(); i < size; i++) {
			System.out.println("project: " + res.getKey(i));
			for(String missingImport : res.getValue(i)) {
				System.out.println("\tmissing: " + missingImport);
			}
		}
		System.out.println("\ncount: " + res.size());
	}

}
