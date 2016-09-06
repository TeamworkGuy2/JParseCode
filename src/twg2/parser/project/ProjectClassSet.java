package twg2.parser.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.val;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.collections.builder.ListUtil;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.CodeFileParsed;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguage;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.resolver.ClassSigResolver;
import twg2.parser.resolver.FieldSigResolver;
import twg2.parser.resolver.MethodSigResolver;

/** A group of classes/interfaces representing all of the compilation units in a project.
 * Provides {@link #resolveSimpleName(String, List, Collection)} for resolving simple names to fully qualifying names
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class ProjectClassSet<T_ID, T_BLOCK extends BlockType, T_CLASS extends ClassAst<? extends ClassSig, ? extends JsonWritableSig, T_BLOCK>, T_CODE_FILE extends CodeFileParsed<? extends T_ID, ? extends T_CLASS>> {
	Map<String, T_CODE_FILE> compilationUnitsByFullyQualifyingName = new HashMap<>();
	Map<String, List<T_CODE_FILE>> compilationUnitsByNamespaces = new HashMap<>();


	public void addCompilationUnit(List<String> fullyQualifyingName, T_CODE_FILE classUnit) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		compilationUnitsByFullyQualifyingName.put(fullName, classUnit);

		// loop through the fully qualifying name parts and add the new compilation unit to each namespace set
		String partialName = "";
		for(String namePart : fullyQualifyingName) {
			partialName = NameUtil.appendToFqName(partialName, namePart);
			List<T_CODE_FILE> nsCompilationUnits = compilationUnitsByNamespaces.get(partialName);
			if(nsCompilationUnits == null) {
				compilationUnitsByNamespaces.put(partialName, nsCompilationUnits = new ArrayList<>());
			}
			nsCompilationUnits.add(classUnit);
		}
	}


	public T_CLASS getCompilationUnit(List<String> fullyQualifyingName) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		return compilationUnitsByFullyQualifyingName.get(fullName).getParsedClass();
	}


	public List<T_CODE_FILE> getCompilationUnitsStartWith(List<String> startOfFullyQualifyingName) {
		String startName = NameUtil.joinFqName(startOfFullyQualifyingName);
		List<T_CODE_FILE> resBlocks = new ArrayList<>();

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
					val compilationUnitSimpleName = nsCompilationUnit.getParsedClass().getSignature().getSimpleName();
					if(compilationUnitSimpleName.equals(simpleName)) {
						if(matchingCompilationUnit != null) {
							throw new IllegalStateException("found multiple compilation units matching the name '" + simpleName + "' in namespace '" + nsName + "'" +
									", [" + matchingCompilationUnit.getSignature() + ", " + nsCompilationUnit.getParsedClass().getSignature() + "]");
						}
						matchingCompilationUnit = nsCompilationUnit.getParsedClass();
					}
				}
			}
		}

		return matchingCompilationUnit;
	}


	public T_CLASS resolveSimpleNameToClassSingleNamespace(String simpleName, List<String> namespace, Collection<List<String>> missingNamespacesDst) {
		T_CLASS matchingCompilationUnit = null;
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
				val compilationUnitSimpleName = nsCompilationUnit.getParsedClass().getSignature().getSimpleName();
				if(compilationUnitSimpleName.equals(simpleName)) {
					if(matchingCompilationUnit != null) {
						throw new IllegalStateException("found multiple compilation units matching the name '" + simpleName + "' in namespace '" + nsName + "'" +
								", [" + matchingCompilationUnit.getSignature() + ", " + nsCompilationUnit.getParsedClass().getSignature() + "]");
					}
					matchingCompilationUnit = nsCompilationUnit.getParsedClass();
				}
			}
		}
		return matchingCompilationUnit;
	}


	public List<String> resolveSimpleName(String simpleName, List<List<String>> namespaces, Collection<List<String>> missingNamespacesDst) {
		val resolvedClass = resolveSimpleNameToClass(simpleName, namespaces, missingNamespacesDst);
		return resolvedClass != null ? resolvedClass.getSignature().getFullName() : null;
	}


	public T_CLASS resolveSimpleNameToClass(String simpleName, ClassAst.SimpleImpl<? extends BlockType> classScope, Collection<List<String>> missingNamespacesDst) {
		ClassSig.SimpleImpl classSig = classScope.getSignature();

		// try resolve using the class' nested classes
		T_CLASS resolvedClass = resolveSimpleNameToClassSingleNamespace(simpleName, classSig.getFullName(), missingNamespacesDst);

		// try resolve using the class' imports
		if(resolvedClass == null) {
			resolvedClass = resolveSimpleNameToClass(simpleName, classScope.getUsingStatements(), missingNamespacesDst);
		}

		// try resolve using the class' parent package/namespace
		if(resolvedClass == null) {
			resolvedClass = resolveSimpleNameToClassSingleNamespace(simpleName, NameUtil.allExceptLastFqName(classSig.getFullName()), missingNamespacesDst);
		}
		// TODO support resolution of types that are generic class params
		//if(resolvedClass == null && classSig.isGeneric()) {
		//	resolvedClass = resolveSimpleNameToClass(simpleName, Arrays.asList(classSig.getGenericParams().get(0)), missingNamespacesDst);
		//}
		return resolvedClass;
	}


	public List<String> resolveSimpleName(String simpleName, ClassAst.SimpleImpl<? extends BlockType> classScope, Collection<List<String>> missingNamespacesDst) {
		val resolvedClass = resolveSimpleNameToClass(simpleName, classScope, missingNamespacesDst);
		return resolvedClass != null ? resolvedClass.getSignature().getFullName() : null;
	}




	public static class Simple<T_ID, T_BLOCK extends BlockType> extends ProjectClassSet<T_ID, T_BLOCK, ClassAst.SimpleImpl<T_BLOCK>, CodeFileParsed.Simple<T_ID, T_BLOCK>> {

		@Override
		public void addCompilationUnit(List<String> fullyQualifyingName, CodeFileParsed.Simple<T_ID, T_BLOCK> classUnit) {
			super.addCompilationUnit(fullyQualifyingName, classUnit);
		}

	}




	public static class Resolved<T_ID, T_BLOCK extends BlockType> extends ProjectClassSet<T_ID, T_BLOCK, ClassAst.ResolvedImpl<T_BLOCK>, CodeFileParsed.Resolved<T_ID, T_BLOCK>> {

		@Override
		public void addCompilationUnit(List<String> fullyQualifyingName, CodeFileParsed.Resolved<T_ID, T_BLOCK> classUnit) {
			super.addCompilationUnit(fullyQualifyingName, classUnit);
		}

	}




	/** Resolve all the simple names in each of the input project class set's compilation units and return a new project class set containing all the resulting compilation units with fully qualifying names.<br>
	 * Some namespaces may not be found and some simple names may not be resolvable, these issues can be tracked and returned via optional destination parameters.
	 * If these optional parameters are null, errors are thrown instead
	 */
	public static <_T_ID extends CodeFileSrc<? extends CodeLanguage>, _T_BLOCK extends BlockType> ProjectClassSet.Resolved<_T_ID, _T_BLOCK> resolveClasses(ProjectClassSet.Simple<_T_ID, _T_BLOCK> projFiles,
			_T_BLOCK defaultBlockType, Collection<List<String>> missingNamespacesDst) {

		ProjectClassSet.Resolved<_T_ID, _T_BLOCK> resFiles = new ProjectClassSet.Resolved<>();

		// TODO annotations and class names need type signature and generic type parsing

		for(val fileEntry : projFiles.compilationUnitsByFullyQualifyingName.entrySet()) {
			val file = fileEntry.getValue().getParsedClass();
			val namespaces = file.getUsingStatements();
			val lang = fileEntry.getValue().getId().getLanguage();
			val resSig = ClassSigResolver.resolveClassSigFrom(lang.getKeywordUtil(), file.getSignature(), file, projFiles, defaultBlockType, missingNamespacesDst);
			val resMethods = ListUtil.map(file.getMethods(), (mthd) -> MethodSigResolver.resolveFrom(lang.getKeywordUtil(), mthd, file, projFiles, missingNamespacesDst));
			val resFields = ListUtil.map(file.getFields(), (fld) -> FieldSigResolver.resolveFrom(fld, file, projFiles, missingNamespacesDst));
			val resEnumMembers = file.getEnumMembers() != null ? ListUtil.map(file.getEnumMembers(), (fld) -> FieldSigResolver.resolveFrom(fld, file, projFiles, missingNamespacesDst)) : null;

			val resClass = new ClassAst.ResolvedImpl<_T_BLOCK>(resSig, namespaces, resFields, resMethods, resEnumMembers, file.getBlockType());

			resFiles.addCompilationUnit(resSig.getFullName(), new CodeFileParsed.Resolved<_T_ID, _T_BLOCK>(fileEntry.getValue().getId(), resClass, fileEntry.getValue().getAstTree()));
		}
		return resFiles;
	}

}
