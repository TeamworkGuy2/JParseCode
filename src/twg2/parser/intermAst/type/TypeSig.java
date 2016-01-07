package twg2.parser.intermAst.type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.collections.util.ListBuilder;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.JsonWrite;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-12-12
 */
public enum TypeSig {
	;


	/** Resolves simple name fields from {@link TypeSig.Simple} into fully qualifying names and creates a new {@link IntermClassSig} with all other fields the same
	 */
	public static TypeSig.Resolved resolveFrom(TypeSig.Simple intermSig, IntermClass.SimpleImpl<? extends CompoundBlock> namespaceClass,
			ProjectClassSet<?, ?> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		List<TypeSig.Resolved> childSigs = Collections.emptyList();
		if(intermSig.isGeneric()) {
			childSigs = new ArrayList<>();
			for(val childSig : intermSig.getGenericParams()) {
				TypeSig.Resolved resolvedChildSig = resolveFrom(childSig, namespaceClass, projFiles, missingNamespacesDst);
				childSigs.add(resolvedChildSig);
			}
		}

		List<String> resolvedType = projFiles.resolveSimpleName(intermSig.getTypeName(), namespaceClass, missingNamespacesDst);

		if(resolvedType == null) {
			resolvedType = ListBuilder.newMutable(intermSig.getTypeName());
		}

		if(childSigs.size() > 0) {
			return new TypeSig.ResolvedGenericImpl(resolvedType, childSigs, intermSig.isNullable());
		}
		else {
			return new TypeSig.ResolvedBaseImpl(resolvedType, intermSig.isNullable());
		}
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-4
	 */
	public static interface Simple extends JsonWritableSig {

		public String getTypeName();

		public boolean isNullable();

		public boolean isGeneric();

		public List<TypeSig.Simple> getGenericParams();

		@Override
		public String toString();

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 */
	@Immutable
	public static class SimpleBaseImpl implements TypeSig.Simple {
		private final @Getter String typeName;
		private final @Getter boolean nullable;
		private final @Getter boolean primitive;


		public SimpleBaseImpl(String typeName, boolean nullable) {
			this.typeName = typeName;
			this.nullable = nullable;
			// TODO shouldn't rely on CsKeyword, a TypeSig should be language agnostic
			this.primitive = CsKeyword.isPrimitive(typeName);
		}


		@Override
		public boolean isGeneric() {
			return false;
		}


		@Override
		public List<TypeSig.Simple> getGenericParams() {
			return Collections.emptyList();
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append(" {");
			dst.append("\"typeName\": \"" + typeName + "\"");

			if(nullable) {
				dst.append(", ");
				dst.append("\"nullable\": " + nullable);
			}

			if(primitive) {
				dst.append(", ");
				dst.append("\"primitive\": " + primitive);
			}

			dst.append("}");
		}


		@Override
		public String toString() {
			return typeName + (nullable ? "?" : "");
		}

	}



	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 */
	@Immutable
	public static class SimpleGenericImpl implements TypeSig.Simple {
		private final @Getter String typeName;
		private final @Getter List<TypeSig.Simple> genericParams;
		private final @Getter boolean nullable;
		private final @Getter boolean primitive;


		public SimpleGenericImpl(String typeName, List<? extends TypeSig.Simple> genericParams, boolean nullable) {
			@SuppressWarnings("unchecked")
			val genericParamsCast = (List<TypeSig.Simple>)genericParams;

			this.typeName = typeName;
			this.genericParams = genericParamsCast;
			this.nullable = nullable;
			// TODO shouldn't rely on CsKeyword, a TypeSig should be language agnostic
			this.primitive = CsKeyword.isPrimitive(typeName);
		}


		@Override
		public boolean isGeneric() {
			return true;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append(" {");
			dst.append("\"typeName\": \"" + typeName + "\", ");

			dst.append("\"genericParameters\": [");
			JsonWrite.joinStrConsumer(genericParams, ", ", dst, (param) -> param.toJson(dst, st));
			dst.append("]");

			if(nullable) {
				dst.append(", ");
				dst.append("\"nullable\": " + nullable);
			}

			if(primitive) {
				dst.append(", ");
				dst.append("\"primitive\": " + primitive);
			}

			dst.append("}");
		}


		@Override
		public String toString() {
			return typeName + genericParams + (nullable ? "?" : "");
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-4
	 */
	public static interface Resolved extends JsonWritableSig {

		public String getSimpleName();

		public List<String> getFullyQualifyingName();

		public boolean isNullable();

		public boolean isGeneric();

		public List<TypeSig.Resolved> getGenericParams();

		@Override
		public String toString();

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 */
	@Immutable
	public static class ResolvedBaseImpl implements TypeSig.Resolved {
		private final @Getter String simpleName;
		private final @Getter List<String> fullyQualifyingName;
		private final @Getter boolean nullable;
		private final @Getter boolean primitive;


		public ResolvedBaseImpl(List<String> fullyQualifyingName, boolean nullable) {
			this.simpleName = fullyQualifyingName.get(fullyQualifyingName.size() - 1);
			this.fullyQualifyingName = fullyQualifyingName;
			this.nullable = nullable;
			// TODO shouldn't rely on CsKeyword, a TypeSig should be language agnostic
			this.primitive = CsKeyword.isPrimitive(simpleName);
		}


		@Override
		public boolean isGeneric() {
			return false;
		}

		@Override
		public List<Resolved> getGenericParams() {
			return Collections.emptyList();
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append(" {");
			dst.append("\"typeName\": \"" + NameUtil.joinFqName(fullyQualifyingName) + "\"");

			if(nullable) {
				dst.append(", ");
				dst.append("\"nullable\": " + nullable);
			}

			if(primitive) {
				dst.append(", ");
				dst.append("\"primitive\": " + primitive);
			}

			dst.append("}");
		}


		@Override
		public String toString() {
			return simpleName + (nullable ? "?" : "");
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 */
	@Immutable
	public static class ResolvedGenericImpl implements TypeSig.Resolved {
		private final @Getter String simpleName;
		private final @Getter List<String> fullyQualifyingName;
		private final @Getter List<TypeSig.Resolved> genericParams;
		private final @Getter boolean nullable;
		private final @Getter boolean primitive;


		public ResolvedGenericImpl(List<String> fullyQualifyingName, List<? extends TypeSig.Resolved> genericParams, boolean nullable) {
			@SuppressWarnings("unchecked")
			val genericParamsCast = (List<TypeSig.Resolved>)genericParams;

			this.simpleName = fullyQualifyingName.get(fullyQualifyingName.size() - 1);
			this.fullyQualifyingName = fullyQualifyingName;
			this.genericParams = genericParamsCast;
			this.nullable = nullable;
			// TODO shouldn't rely on CsKeyword, a TypeSig should be language agnostic
			this.primitive = CsKeyword.isPrimitive(simpleName);
		}


		@Override
		public boolean isGeneric() {
			return true;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append(" {");
			dst.append("\"typeName\": \"" + NameUtil.joinFqName(fullyQualifyingName) + "\", ");

			dst.append("\"genericParameters\": [");
			JsonWrite.joinStrConsumer(genericParams, ", ", dst, (param) -> param.toJson(dst, st));
			dst.append("]");

			if(nullable) {
				dst.append(", ");
				dst.append("\"nullable\": " + nullable);
			}

			if(primitive) {
				dst.append(", ");
				dst.append("\"primitive\": " + primitive);
			}

			dst.append("}");
		}


		@Override
		public String toString() {
			return simpleName + genericParams + (nullable ? "?" : "");
		}

	}

}
