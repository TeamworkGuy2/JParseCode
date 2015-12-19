package twg2.parser.codeParser.typescript;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import streamUtils.StreamUtil;
import twg2.io.files.FileUtility;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2015-5-11
 */
public class ImportsTest {


	public static void main(String[] args) {
		Path projectPath = Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/ps/l/ca/app/scripts/");
		Path projectModulesPath = Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/ps/l/ca/node_modules/");
		File folder = Paths.get("C:/Users/TeamworkGuy2/Documents/Visual Studio 2015/Projects/ps/l/ca/app/scripts/modules/psServices").toFile();
		//projectPath.res

		FileUtility.forEachFileByFolderRecursively(folder, (f) -> f.getName().toLowerCase().endsWith(".ts"), Integer.MAX_VALUE, (parent, file) -> {
			try {
				Path parentPath = parent.toPath();
				List<String> lines = StreamUtil.toList(Files.lines(file.toPath()), new ArrayList<>());
				List<String> imports = Imports.extractImports(lines);
				List<Path> relativeImports = new ArrayList<>();
				List<String> relativeImportStrs = new ArrayList<>();

				for(String importStr : imports) {
					Path relativePath = null;
					if(importStr.startsWith(".")) {
						relativePath = projectPath.relativize(parentPath.resolve(importStr)).toAbsolutePath();
					}
					else {
						relativePath = projectModulesPath.resolve(importStr).toAbsolutePath();
					}
					relativeImports.add(relativePath);
					relativeImportStrs.add(relativePath.toFile().getCanonicalPath());
				}

				System.out.println(imports.size() + " imports for: " + file);
				System.out.println(StringJoin.join(relativeImportStrs, "\n"));
				System.out.println();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
