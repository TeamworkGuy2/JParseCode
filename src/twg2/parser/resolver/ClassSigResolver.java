package twg2.parser.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.val;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.BaseDataTypeExtractor;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.project.ProjectClassSet;

/**
 * @author TeamworkGuy2
 * @since 2016-3-19
 */
public class ClassSigResolver {

	/** Resolves the {@link twg2.ast.interm.classes.ClassSig.SimpleImpl#getExtendImplementSimpleNames()} into fully qualifying names and creates a new {@link ClassSig} with all other fields the same
	 */
	public static <T_ID, T_SIG extends ClassSig.SimpleImpl> ClassSig.ResolvedImpl resolveClassSigFrom(KeywordUtil keywordUtil, T_SIG classSig, ClassAst.SimpleImpl<? extends CompoundBlock> namespaceClass,
			ProjectClassSet.Simple<T_ID, ? extends CompoundBlock> projFiles, CompoundBlock defaultBlockType, Collection<List<String>> missingNamespacesDst) {
		List<List<String>> resolvedCompilationUnitNames = new ArrayList<>();
		List<CompoundBlock> resolvedCompilationUnitBlockTypes = new ArrayList<>();
		val classExtendImplementNames = classSig.getExtendImplementSimpleNames();

		if(classExtendImplementNames != null) {
			for(val simpleName : classExtendImplementNames) {
				val resolvedClass = projFiles.resolveSimpleNameToClass(simpleName, namespaceClass, missingNamespacesDst);
				if(resolvedClass != null) {
					resolvedCompilationUnitBlockTypes.add(resolvedClass.getBlockType());
					resolvedCompilationUnitNames.add(resolvedClass.getSignature().getFullName());
				}
				else {
					resolvedCompilationUnitBlockTypes.add(defaultBlockType);
					resolvedCompilationUnitNames.add(new ArrayList<>(Arrays.asList(simpleName)));
				}
			}
		}

		// check the extends/implements name list, ensure that the first
		TypeSig.Resolved extendClassType = null;
		List<TypeSig.Resolved> implementInterfaceTypes = Collections.emptyList();
		if(resolvedCompilationUnitNames.size() > 0) {
			val firstCompilationUnitName = resolvedCompilationUnitNames.get(0);
			val firstCompilationUnitBlockType = resolvedCompilationUnitBlockTypes.get(0);
			boolean extendsClass = false;
			// Get the extends class name
			// TODO maybe should check isClass() rather than !isInterface()
			if(!firstCompilationUnitBlockType.isInterface()) {
				val name = NameUtil.joinFqName(firstCompilationUnitName);
				val extendClassSimpleType = BaseDataTypeExtractor.extractGenericTypes(name, keywordUtil);
				extendClassType = TypeSigResolver.resolveFrom(extendClassSimpleType, namespaceClass, projFiles, missingNamespacesDst);
				extendsClass = true;
			}
			// Get the implements interface names
			if(resolvedCompilationUnitBlockTypes.size() > (extendsClass ? 1 : 0)) {
				implementInterfaceTypes = new ArrayList<>();
				for(int i = extendsClass ? 1 : 0, size = resolvedCompilationUnitBlockTypes.size(); i < size; i++) {
					if(!resolvedCompilationUnitBlockTypes.get(i).isInterface()) {
						throw new IllegalStateException("class cannot extend more than one class (checking extends/implements list: " + classSig.getExtendImplementSimpleNames() + ") for class '" + classSig.getFullName() + "'");
					}
					val name = NameUtil.joinFqName(resolvedCompilationUnitNames.get(i));
					val implementInterfaceSimpleType = BaseDataTypeExtractor.extractGenericTypes(name, keywordUtil);
					val implementInterfaceType = TypeSigResolver.resolveFrom(implementInterfaceSimpleType, namespaceClass, projFiles, missingNamespacesDst);
					implementInterfaceTypes.add(implementInterfaceType);
				}
			}
		}

		// resolve generic signature
		List<TypeSig.Resolved> resolvedClassParams = Collections.emptyList();
		if(classSig.isGeneric()) {
			resolvedClassParams = new ArrayList<>();
			for(val simpleParam : classSig.getParams()) {
				TypeSig.Resolved resolvedClassParam = TypeSigResolver.resolveFrom(simpleParam, namespaceClass, projFiles, missingNamespacesDst);
				resolvedClassParams.add(resolvedClassParam);
			}
		}

		val classFqName = classSig.getFullName();

		val res = new ClassSig.ResolvedImpl(classFqName, resolvedClassParams, classSig.getAccessModifier(), classSig.getDeclarationType(),
				extendClassType, implementInterfaceTypes);
		return res;
	}

}
