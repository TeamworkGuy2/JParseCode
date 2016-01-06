package twg2.parser.intermAst.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.val;
import twg2.collections.tuple.Tuples;
import twg2.collections.util.ListUtil;
import twg2.collections.util.dataStructures.PairList;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.output.JsonWritableSig;

/** A group of classes/interfaces representing all of the compilation units in a project.
 * Provides {@link #resolveSimpleName(String, List, Collection)} for resolving simple names to fully qualifying names
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class ProjectClassSet<T_ID, T_CLASS extends IntermClass<? extends IntermClassSig, ? extends JsonWritableSig, ? extends CompoundBlock>> {
	private Map<String, Entry<T_ID, T_CLASS>> compilationUnitsByFullyQualifyingName = new HashMap<>();
	private Map<String, PairList<T_ID, T_CLASS>> compilationUnitsByNamespaces = new HashMap<>();


	public <_T_CLASS extends T_CLASS> void addCompilationUnit(List<String> fullyQualifyingName, T_ID src, _T_CLASS classUnit) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		compilationUnitsByFullyQualifyingName.put(fullName, Tuples.of(src, classUnit));

		// loop through the fully qualifying name parts and add the new compilation unit to each namespace set
		String partialName = "";
		for(String namePart : fullyQualifyingName) {
			partialName = NameUtil.appendToFqName(partialName, namePart);
			PairList<T_ID, T_CLASS> nsCompilationUnits = compilationUnitsByNamespaces.get(partialName);
			if(nsCompilationUnits == null) {
				compilationUnitsByNamespaces.put(partialName, nsCompilationUnits = new PairList<>());
			}
			nsCompilationUnits.add(src, classUnit);
		}
	}


	public T_CLASS getCompilationUnit(List<String> fullyQualifyingName) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		return compilationUnitsByFullyQualifyingName.get(fullName).getValue();
	}


	public List<Entry<T_ID, T_CLASS>> getCompilationUnitsStartWith(List<String> startOfFullyQualifyingName) {
		String startName = NameUtil.joinFqName(startOfFullyQualifyingName);
		List<Entry<T_ID, T_CLASS>> resBlocks = new ArrayList<>();

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
	public T_CLASS resolveSimpleNameToClass(String simpleName, List<List<String>> namespaces, Collection<List<String>> missingNamespacesDst) {
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
					val compilationUnitSimpleName = nsCompilationUnit.getValue().getSignature().getSimpleName();
					if(compilationUnitSimpleName.equals(simpleName)) {
						if(matchingCompilationUnit != null) {
							throw new IllegalStateException("found multiple compilation units matching the name '" + simpleName + "', [" + matchingCompilationUnit + ", " + nsCompilationUnit.getValue() + "]");
						}
						matchingCompilationUnit = nsCompilationUnit.getValue();
					}
				}
			}
		}
		return matchingCompilationUnit;
	}


	public List<String> resolveSimpleName(String simpleName, List<List<String>> namespaces, Collection<List<String>> missingNamespacesDst) {
		val resolvedClass = resolveSimpleNameToClass(simpleName, namespaces, missingNamespacesDst);
		return resolvedClass != null ? resolvedClass.getSignature().getFullyQualifyingName() : null;
	}


	/** Resolve all the simple names in each of the input project class set's compilation units and return a new project class set containing all the resulting compilation units with fully qualifying names.<br>
	 * Some namespaces may not be found and some simple names may not be resolvable, these issues can be tracked and returned via optional destination parameters.
	 * If these optional parameters are null, errors are thrown instead
	 */
	public static <_T_ID, _T_BLOCK extends CompoundBlock> ProjectClassSet<_T_ID, IntermClass.ResolvedImpl<_T_BLOCK>> resolveClasses(ProjectClassSet<_T_ID, ? extends IntermClass.SimpleImpl<_T_BLOCK>> projFiles,
			_T_BLOCK defaultBlockType, Collection<List<String>> missingNamespacesDst) {
		@SuppressWarnings("unchecked")
		val projFilesCast = ((ProjectClassSet<_T_ID, IntermClass.SimpleImpl<_T_BLOCK>>)projFiles);

		ProjectClassSet<_T_ID, IntermClass.ResolvedImpl<_T_BLOCK>> resFiles = new ProjectClassSet<>();

		// TODO annotations and class names need type signature and generic type parsing

		for(val fileEntry : projFilesCast.compilationUnitsByFullyQualifyingName.entrySet()) {
			val file = fileEntry.getValue().getValue();
			val namespaces = file.getUsingStatements();
			val resSig = IntermClassSig.resolveClassSigFrom(file.getSignature(), namespaces, projFilesCast, defaultBlockType, missingNamespacesDst);
			val resMethods = ListUtil.map(file.getMethods(), (mthd) -> IntermMethodSig.resolveFrom(mthd, namespaces, projFilesCast, missingNamespacesDst));
			val resFields = ListUtil.map(file.getFields(), (fld) -> IntermFieldSig.resolveFrom(fld, namespaces, projFilesCast, missingNamespacesDst));
			val resClass = new IntermClass.ResolvedImpl<_T_BLOCK>(resSig, namespaces, resFields, resMethods, file.getBlockTree(), file.getBlockType());

			resFiles.addCompilationUnit(resSig.getFullyQualifyingName(), fileEntry.getValue().getKey(), resClass);
		}
		return resFiles;
	}

}
