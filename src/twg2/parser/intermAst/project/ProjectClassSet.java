package twg2.parser.intermAst.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.val;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;

/** A group of classes/interfaces representing all of the compilation units in a project.
 * Provides {@link #resolveSimpleName(String, List, List)} for resolving simple names to fully qualifying names
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class ProjectClassSet<T_CLASS extends IntermClass<? extends IntermClassSig, ? extends CompoundBlock>> {
	private Map<String, T_CLASS> compilationUnitsByFullyQualifyingName = new HashMap<>();
	private Map<String, List<T_CLASS>> compilationUnitsByNamespaces = new HashMap<>();


	public <_T_CLASS extends T_CLASS> void addCompilationUnit(List<String> fullyQualifyingName, _T_CLASS classUnit) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		compilationUnitsByFullyQualifyingName.put(fullName, classUnit);

		// loop through the fully qualifying name parts and add the new compilation unit to each namespace set
		String partialName = "";
		for(String namePart : fullyQualifyingName) {
			partialName = NameUtil.appendToFqName(partialName, namePart);
			List<T_CLASS> nsCompilationUnits = compilationUnitsByNamespaces.get(partialName);
			if(nsCompilationUnits == null) {
				compilationUnitsByNamespaces.put(partialName, nsCompilationUnits = new ArrayList<>());
			}
			nsCompilationUnits.add(classUnit);
		}
	}


	public T_CLASS getCompilationUnit(List<String> fullyQualifyingName) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		return compilationUnitsByFullyQualifyingName.get(fullName);
	}


	public List<T_CLASS> getCompilationUnitsStartWith(List<String> startOfFullyQualifyingName) {
		String startName = NameUtil.joinFqName(startOfFullyQualifyingName);
		List<T_CLASS> resBlocks = new ArrayList<>();

		for(val entry : compilationUnitsByFullyQualifyingName.entrySet()) {
			if(entry.getKey().startsWith(startName)) {
				resBlocks.add(entry.getValue());
			}
		}
		return resBlocks;
	}


	/** Given a simple name and a list of namespaces, lookup the compilation units in each namespace and look for one with a matching simple name.<br>
	 * i.e. (assume the compilation unit "myApp.dataSources.BinaryText" exists) given the simple name {@code BinaryText} and the namespaces {@code ["myApp.utils", "myApp.dataSources", "myApp.gui"]},
	 * the returned result would be the BinaryText compilation unit from the "myApp.dataSources" namespace.
	 *
	 * @param simpleName the simple name to lookup
	 * @param namespaces the list of namespaces to search (this might be the list of imports from the top of the compilation unit's source code file)
	 * @param missingNamespacesDst option (can be null), if provided, when this project class set contains noe entries for one of the {@code namespaces},
	 * the namespace is added this {@code missingNamespacesDst} parameter, else thrown an {@link IllegalStateException}
	 */
	public T_CLASS resolveSimpleName(String simpleName, List<List<String>> namespaces, List<List<String>> missingNamespacesDst) {
		T_CLASS matchingCompilationUnit = null;
		for(val namespace : namespaces) {
			val nsName = NameUtil.joinFqName(namespace);
			val nsCompilationUnits = compilationUnitsByNamespaces.get(nsName);

			if(nsCompilationUnits == null) {
				if(missingNamespacesDst != null) {
					missingNamespacesDst.add(namespace);
				}
				else {
					throw new IllegalStateException("could not find namespace '" + nsName + "'");
				}
			}

			if(nsCompilationUnits != null) {
				for(val nsCompilationUnit : nsCompilationUnits) {
					if(nsCompilationUnit.getSignature().getSimpleName().equals(simpleName)) {
						if(matchingCompilationUnit != null) {
							throw new IllegalStateException("found multiple compilation units matching the name '" + simpleName + "', [" + matchingCompilationUnit + ", " + nsCompilationUnit + "]");
						}
						matchingCompilationUnit = nsCompilationUnit;
					}
				}
			}
		}
		return matchingCompilationUnit;
	}


	/** Resolve all the simple names in each of the input project class set's compilation units and return a new project class set containing all the resulting compilation units with fully qualifying names.<br>
	 * Some namespaces may not be found and some simple names may not be resolvable, these issues can be tracked and returned via optional destination parameters.
	 * If these optional parameters are null, errors are thrown instead
	 */
	public static <_T_BLOCK extends CompoundBlock> ProjectClassSet<IntermClass.ResolvedImpl<_T_BLOCK>> resolveClasses(ProjectClassSet<? extends IntermClass<? extends IntermClassSig.SimpleNameImpl, _T_BLOCK>> projFiles,
			_T_BLOCK defaultBlockType, List<List<String>> missingNamespacesDst) {
		ProjectClassSet<IntermClass.ResolvedImpl<_T_BLOCK>> resFiles = new ProjectClassSet<>();
		for(val fileEntry : projFiles.compilationUnitsByFullyQualifyingName.entrySet()) {
			val file = fileEntry.getValue();
			val resSig = IntermClassSig.resolveFrom(file, projFiles, defaultBlockType, missingNamespacesDst);
			val resClass = new IntermClass.ResolvedImpl<>(resSig, file.getUsingStatements(), file.getFields(), file.getMethods(), file.getBlockTree(), file.getBlockType());
			resFiles.addCompilationUnit(resSig.getFullyQualifyingName(), resClass);
		}
		return resFiles;
	}

}
