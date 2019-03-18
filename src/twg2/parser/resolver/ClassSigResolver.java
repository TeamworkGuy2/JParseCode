package twg2.parser.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.val;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.classes.ClassSigResolved;
import twg2.ast.interm.classes.ClassSigSimple;
import twg2.ast.interm.type.TypeSig;
import twg2.collections.builder.ListBuilder;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.extractors.DataTypeExtractor;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.project.ProjectClassSet;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2016-3-19
 */
public class ClassSigResolver {

	/** Resolves the {@link twg2.ast.interm.classes.ClassSigSimple#getExtendImplementSimpleNames()} into fully qualifying names and creates a new {@link ClassSig} with all other fields the same
	 */
	public static <T_ID, T_SIG extends ClassSigSimple, T_BLOCK extends BlockType> ClassSigResolved resolveClassSigFrom(
			KeywordUtil<? extends Keyword> keywordUtil,
			T_SIG classSig,
			ClassAst.SimpleImpl<? extends BlockType> namespaceClass,
			ProjectClassSet<ClassAst.SimpleImpl<T_BLOCK>, CodeFileParsed.Intermediate<T_BLOCK>> projFiles,
			BlockType defaultBlockType,
			Collection<List<String>> missingNamespacesDst
	) {
		val classExtendImplementNames = classSig.getExtendImplementSimpleNames();
		int nameCnt = classExtendImplementNames.size();
		List<List<String>> resolvedParentNames = new ArrayList<>(nameCnt);
		List<BlockType> resolvedParentBlockTypess = new ArrayList<>(nameCnt);

		if(classExtendImplementNames != null) {
			for(val simpleName : classExtendImplementNames) {
				val resolvedClass = projFiles.resolveSimpleNameToClass(simpleName, namespaceClass, missingNamespacesDst);
				if(resolvedClass != null) {
					resolvedParentBlockTypess.add(resolvedClass.getBlockType());
					resolvedParentNames.add(resolvedClass.getSignature().getFullName());
				}
				else {
					resolvedParentBlockTypess.add(defaultBlockType);
					resolvedParentNames.add(ListBuilder.mutable(simpleName));
				}
			}
		}

		// check the extends/implements name list, ensure that the first
		TypeSig.TypeSigResolved extendClassType = null;
		List<TypeSig.TypeSigResolved> implementInterfaceTypes = Collections.emptyList();
		if(resolvedParentNames.size() > 0) {
			val firstCompilationUnitName = resolvedParentNames.get(0);
			val firstCompilationUnitBlockType = resolvedParentBlockTypess.get(0);
			boolean extendsClass = false;
			// Get the extends class name
			// TODO maybe should check isClass() rather than !isInterface()
			if(!firstCompilationUnitBlockType.isInterface()) {
				val name = NameUtil.joinFqName(firstCompilationUnitName);
				val extendClassSimpleType = DataTypeExtractor.extractGenericTypes(name, keywordUtil);
				extendClassType = TypeSigResolver.resolveFrom(extendClassSimpleType, namespaceClass, projFiles, missingNamespacesDst);
				extendsClass = true;
			}
			// Get the implements interface names
			if(resolvedParentBlockTypess.size() > (extendsClass ? 1 : 0)) {
				implementInterfaceTypes = new ArrayList<>(resolvedParentBlockTypess.size() - (extendsClass ? 1 : 0));
				for(int i = extendsClass ? 1 : 0, size = resolvedParentBlockTypess.size(); i < size; i++) {
					// if the extend/implement type is not a recognized interface and the name is resolved (resolved names are longer than 1 part, since all classes should come from a namespace)
					// assume that unresolved names could be interfaces and don't count them against the 1 extend class limit
					if(!resolvedParentBlockTypess.get(i).isInterface() && resolvedParentNames.get(i).size() > 1) {
						throw new IllegalStateException("class cannot extend more than one class (checking extends/implements list: " + classSig.getExtendImplementSimpleNames() + ") for class '" + classSig.getFullName() + "'");
					}
					val name = NameUtil.joinFqName(resolvedParentNames.get(i));
					val implementInterfaceSimpleType = DataTypeExtractor.extractGenericTypes(name, keywordUtil);
					val implementInterfaceType = TypeSigResolver.resolveFrom(implementInterfaceSimpleType, namespaceClass, projFiles, missingNamespacesDst);
					implementInterfaceTypes.add(implementInterfaceType);
				}
			}
		}

		// resolve generic signature
		List<TypeSig.TypeSigResolved> resolvedClassParams = Collections.emptyList();
		if(classSig.isGeneric()) {
			resolvedClassParams = new ArrayList<>();
			for(val simpleParam : classSig.getParams()) {
				val resolvedClassParam = TypeSigResolver.resolveFrom(simpleParam, namespaceClass, projFiles, missingNamespacesDst);
				resolvedClassParams.add(resolvedClassParam);
			}
		}

		val classFqName = classSig.getFullName();

		val res = new ClassSigResolved(classFqName, resolvedClassParams, classSig.getAccessModifier(), classSig.getAnnotations(),
				classSig.getDeclarationType(), extendClassType, implementInterfaceTypes);
		return res;
	}

}
