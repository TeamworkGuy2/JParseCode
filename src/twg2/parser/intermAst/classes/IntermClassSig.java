package twg2.parser.intermAst.classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.parser.baseAst.AccessModifier;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.csharp.CsDataTypeExtractor;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.intermAst.type.TypeSig;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.JsonWrite;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public interface IntermClassSig extends JsonWritableSig {

	public AccessModifier getAccessModifier();

	public List<String> getFullyQualifyingName();

	public String getDeclarationType();

	public String getSimpleName();


	/** Resolves the {@link IntermClassSig.SimpleImpl#getExtendImplementSimpleNames()} into fully qualifying names and creates a new {@link IntermClassSig} with all other fields the same
	 */
	public static <T_ID, T_SIG extends IntermClassSig.SimpleImpl> IntermClassSig.ResolvedImpl resolveClassSigFrom(T_SIG classSig, IntermClass.SimpleImpl<? extends CompoundBlock> namespaceClass,
			ProjectClassSet.Simple<T_ID, ? extends CompoundBlock> projFiles, CompoundBlock defaultBlockType, Collection<List<String>> missingNamespacesDst) {
		List<List<String>> resolvedCompilationUnitNames = new ArrayList<>();
		List<CompoundBlock> resolvedCompilationUnitBlockTypes = new ArrayList<>();
		val classExtendImplementNames = classSig.getExtendImplementSimpleNames();

		if(classExtendImplementNames != null) {
			for(val simpleName : classExtendImplementNames) {
				val resolvedClass = projFiles.resolveSimpleNameToClass(simpleName, namespaceClass, missingNamespacesDst);
				if(resolvedClass != null) {
					resolvedCompilationUnitBlockTypes.add(resolvedClass.getBlockType());
					resolvedCompilationUnitNames.add(resolvedClass.getSignature().getFullyQualifyingName());
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
				val extendClassSimpleType = CsDataTypeExtractor.extractGenericTypes(name);
				extendClassType = TypeSig.resolveFrom(extendClassSimpleType, namespaceClass, projFiles, missingNamespacesDst);
				extendsClass = true;
			}
			// Get the implements interface names
			if(resolvedCompilationUnitBlockTypes.size() > (extendsClass ? 1 : 0)) {
				implementInterfaceTypes = new ArrayList<>();
				for(int i = extendsClass ? 1 : 0, size = resolvedCompilationUnitBlockTypes.size(); i < size; i++) {
					if(!resolvedCompilationUnitBlockTypes.get(i).isInterface()) {
						throw new IllegalStateException("class cannot extend more than one class (checking extends/implements list: " + classSig.getExtendImplementSimpleNames() + ") for class '" + classSig.getFullyQualifyingName() + "'");
					}
					val name = NameUtil.joinFqName(resolvedCompilationUnitNames.get(i));
					val implementInterfaceSimpleType = CsDataTypeExtractor.extractGenericTypes(name);
					val implementInterfaceType = TypeSig.resolveFrom(implementInterfaceSimpleType, namespaceClass, projFiles, missingNamespacesDst);
					implementInterfaceTypes.add(implementInterfaceType);
				}
			}
		}

		// resolve generic signature
		List<TypeSig.Resolved> resolvedClassParams = Collections.emptyList();
		if(classSig.isGeneric()) {
			resolvedClassParams = new ArrayList<>();
			for(val simpleParam : classSig.getGenericParams()) {
				TypeSig.Resolved resolvedClassParam = TypeSig.resolveFrom(simpleParam, namespaceClass, projFiles, missingNamespacesDst);
				resolvedClassParams.add(resolvedClassParam);
			}
		}

		val classFqName = classSig.getFullyQualifyingName();

		val res = new IntermClassSig.ResolvedImpl(classSig.getAccessModifier(), classFqName, resolvedClassParams, classSig.getDeclarationType(),
				extendClassType, implementInterfaceTypes);
		return res;
	}




	@Immutable
	@AllArgsConstructor
	public static class SimpleImpl implements IntermClassSig {
		private final @Getter AccessModifier accessModifier;
		private final @Getter List<String> fullyQualifyingName;
		private final @Getter List<TypeSig.Simple> genericParams;
		private final @Getter String declarationType;
		private final @Getter List<String> extendImplementSimpleNames;


		@Override
		public String getSimpleName() {
			return fullyQualifyingName.get(fullyQualifyingName.size() - 1);
		}


		public boolean isGeneric() {
			return genericParams.size() > 0;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
			dst.append("\"access\": \"" + accessModifier + "\", ");
			dst.append("\"name\": \"" + (st.fullClassName ? NameUtil.joinFqName(fullyQualifyingName) : fullyQualifyingName.get(fullyQualifyingName.size() - 1)) + "\"");

			if(declarationType != null) {
				dst.append(", ");
				dst.append("\"declarationType\": \"" + declarationType + "\"");
			}

			if(genericParams != null && genericParams.size() > 0) {
				dst.append(", ");
				dst.append("\"genericParameters\": [");
				JsonWrite.joinStrConsumer(genericParams, ", ", dst, (p) -> p.toJson(dst, st));
				dst.append("]");
			}

			if(extendImplementSimpleNames.size() > 0) {
				dst.append(", ");
				dst.append("\"extendImplementClassNames\": [");
				JsonWrite.joinStrConsumer(extendImplementSimpleNames, ", ", dst, (n) -> dst.append('"' + n + '"'));
				dst.append("] ");
			}

			dst.append(" }");
		}


		@Override
		public String toString() {
			return accessModifier.toSrc() + " " + declarationType + " " + NameUtil.joinFqName(fullyQualifyingName);
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-5
	 */
	@Immutable
	@AllArgsConstructor
	public static class ResolvedImpl implements IntermClassSig {
		private final @Getter AccessModifier accessModifier;
		private final @Getter List<String> fullyQualifyingName;
		private final @Getter List<TypeSig.Resolved> genericParams;
		private final @Getter String declarationType;
		private final @Getter TypeSig.Resolved extendClass;
		private final @Getter List<TypeSig.Resolved> implementInterfaces;


		@Override
		public String getSimpleName() {
			return fullyQualifyingName.get(fullyQualifyingName.size() - 1);
		}


		public boolean isGeneric() {
			return genericParams.size() > 0;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
			dst.append("\"access\": \"" + accessModifier + "\", ");
			dst.append("\"name\": \"" + (st.fullClassName ? NameUtil.joinFqName(fullyQualifyingName) : fullyQualifyingName.get(fullyQualifyingName.size() - 1)) + "\", ");
			dst.append("\"declarationType\": \"" + declarationType + "\"");

			if(genericParams != null && genericParams.size() > 0) {
				dst.append(", ");
				dst.append("\"genericParameters\": [");
				JsonWrite.joinStrConsumer(genericParams, ", ", dst, (p) -> p.toJson(dst, st));
				dst.append("]");
			}

			if(extendClass != null) {
				dst.append(", ");
				dst.append("\"extendClassName\": ");
				extendClass.toJson(dst, st);
			}

			if(implementInterfaces.size() > 0) {
				dst.append(", ");
				dst.append("\"implementClassNames\": [");
				JsonWrite.joinStrConsumer(implementInterfaces, ", ", dst, (intfType) -> intfType.toJson(dst, st));
				dst.append("] ");
			}

			dst.append(" }");
		}


		@Override
		public String toString() {
			return accessModifier.toSrc() + " " + declarationType + " " + NameUtil.joinFqName(fullyQualifyingName);
		}

	}

}
