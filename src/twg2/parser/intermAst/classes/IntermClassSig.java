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
	public static <T_ID, T_CLASS extends IntermClass.SimpleImpl<? extends CompoundBlock>> IntermClassSig.ResolvedImpl resolveClassSigFrom(T_CLASS intermClass,
			ProjectClassSet<T_ID, ? extends T_CLASS> projFiles, CompoundBlock defaultBlockType, Collection<List<String>> missingNamespacesDst) {
		List<List<String>> resolvedCompilationUnitNames = new ArrayList<>();
		List<CompoundBlock> resolvedCompilationUnitBlockTypes = new ArrayList<>();
		val classSig = intermClass.getSignature();
		val classExtendImplementNames = classSig.getExtendImplementSimpleNames();

		if(classExtendImplementNames != null) {
			for(val simpleName : classExtendImplementNames) {
				val resolvedClass = projFiles.resolveSimpleNameToClass(simpleName, intermClass.getUsingStatements(), missingNamespacesDst);
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

		List<String> extendClassFullyQualifyingName = null;
		List<List<String>> implementInterfaceFullyQualifyingNames = new ArrayList<>();
		if(resolvedCompilationUnitNames.size() > 0) {
			val firstCompilationUnitName = resolvedCompilationUnitNames.get(0);
			val firstCompilationUnitBlockType = resolvedCompilationUnitBlockTypes.get(0);
			boolean extendsClass = false;
			// Get the extends class name
			// TODO maybe should check isClass() rather than !isInterface()
			if(!firstCompilationUnitBlockType.isInterface()) {
				extendClassFullyQualifyingName = firstCompilationUnitName;
				extendsClass = true;
			}
			// Get the implements interface names
			if(resolvedCompilationUnitBlockTypes.size() > 1) {
				for(int i = extendsClass ? 1 : 0, size = resolvedCompilationUnitBlockTypes.size(); i < size; i++) {
					if(!resolvedCompilationUnitBlockTypes.get(i).isInterface()) {
						throw new IllegalStateException("class cannot extend more than one class (checking extends/implements list: " + classSig.getExtendImplementSimpleNames() + ") for class '" + classSig.getFullyQualifyingName() + "'");
					}
					implementInterfaceFullyQualifyingNames.add(resolvedCompilationUnitNames.get(i));
				}
			}
		}

		TypeSig.Simple genericClassParams = CsDataTypeExtractor.extractGenericTypes(NameUtil.joinFqName(classSig.getFullyQualifyingName()));
		TypeSig.Resolved resolvedClassParams = TypeSig.resolveFrom(genericClassParams, intermClass.getUsingStatements(), projFiles, missingNamespacesDst);
		val classFqName = resolvedClassParams.getFullyQualifyingName();
		val classParams = resolvedClassParams.isGeneric() ? resolvedClassParams.getGenericParams() : Collections.<TypeSig.Resolved>emptyList();

		val res = new IntermClassSig.ResolvedImpl(classSig.getAccessModifier(), classFqName, classParams, classSig.getDeclarationType(),
				extendClassFullyQualifyingName, implementInterfaceFullyQualifyingNames);
		return res;
	}




	@Immutable
	@AllArgsConstructor
	public static class SimpleImpl implements IntermClassSig {
		private final @Getter AccessModifier accessModifier;
		private final @Getter List<String> fullyQualifyingName;
		private final @Getter String declarationType;
		private final @Getter List<String> extendImplementSimpleNames;


		@Override
		public String getSimpleName() {
			return fullyQualifyingName.get(fullyQualifyingName.size() - 1);
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
		private final @Getter List<String> extendClassFullyQualifyingName;
		private final @Getter List<List<String>> implementInterfaceFullyQualifyingNames;


		@Override
		public String getSimpleName() {
			return fullyQualifyingName.get(fullyQualifyingName.size() - 1);
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

			if(extendClassFullyQualifyingName != null) {
				dst.append(", ");
				dst.append("\"extendClassName\": \"" + NameUtil.joinFqName(extendClassFullyQualifyingName) + "\"");
			}

			if(implementInterfaceFullyQualifyingNames.size() > 0) {
				dst.append(", ");
				dst.append("\"implementClassNames\": [");
				if(implementInterfaceFullyQualifyingNames != null) {
					JsonWrite.joinStrConsumer(implementInterfaceFullyQualifyingNames, ", ", dst, (ns) -> dst.append('"' + NameUtil.joinFqName(ns) + '"'));
				}
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
