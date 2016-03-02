package twg2.parser.intermAst.type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.collections.builder.ListBuilder;
import twg2.io.write.JsonWrite;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2015-12-12
 */
public enum TypeSig {
	;


	/** Resolves simple name fields from {@link TypeSig.Simple} into fully qualifying names and creates a new {@link IntermClassSig} with all other fields the same
	 */
	public static TypeSig.Resolved resolveFrom(TypeSig.Simple intermSig, IntermClass.SimpleImpl<? extends CompoundBlock> namespaceClass,
			ProjectClassSet.Simple<?, ? extends CompoundBlock> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		List<TypeSig.Resolved> childSigs = Collections.emptyList();
		if(intermSig.isGeneric()) {
			childSigs = new ArrayList<>();
			for(val childSig : intermSig.getParams()) {
				TypeSig.Resolved resolvedChildSig = resolveFrom(childSig, namespaceClass, projFiles, missingNamespacesDst);
				childSigs.add(resolvedChildSig);
			}
		}

		List<String> resolvedType = projFiles.resolveSimpleName(intermSig.getTypeName(), namespaceClass, missingNamespacesDst);

		if(resolvedType == null) {
			resolvedType = ListBuilder.mutable(intermSig.getTypeName());
		}

		if(childSigs.size() > 0) {
			return new TypeSig.ResolvedGenericImpl(resolvedType, childSigs, intermSig.getArrayDimensions(), intermSig.isNullable(), intermSig.isPrimitive());
		}
		else {
			return new TypeSig.ResolvedBaseImpl(resolvedType, intermSig.getArrayDimensions(), intermSig.isNullable(), intermSig.isPrimitive());
		}
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-4
	 */
	public static interface Simple extends JsonWritableSig {

		public String getTypeName();

		public boolean isNullable();

		public boolean isPrimitive();

		public boolean isArray();

		public boolean isGeneric();

		public int getArrayDimensions();

		public List<TypeSig.Simple> getParams();

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
		private final @Getter int arrayDimensions;
		private final @Getter boolean primitive;


		public SimpleBaseImpl(String typeName, int arrayDimensions, boolean nullable, boolean primitive) {
			this.typeName = typeName;
			this.nullable = nullable;
			this.arrayDimensions = arrayDimensions;
			this.primitive = primitive;
		}


		@Override
		public boolean isGeneric() {
			return false;
		}


		@Override
		public boolean isArray() {
			return arrayDimensions > 0;
		}


		@Override
		public List<TypeSig.Simple> getParams() {
			return Collections.emptyList();
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append(" {");
			dst.append("\"typeName\": \"" + typeName + "\"");

			if(arrayDimensions > 0) {
				dst.append(", ");
				dst.append("\"arrayDimensions\": " + arrayDimensions);
			}

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
			return typeName + (arrayDimensions > 0 ? StringJoin.repeat("[]", arrayDimensions) : "") + (nullable ? "?" : "");
		}

	}



	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 */
	@Immutable
	public static class SimpleGenericImpl implements TypeSig.Simple {
		private final @Getter String typeName;
		private final @Getter List<TypeSig.Simple> params;
		private final @Getter boolean nullable;
		private final @Getter int arrayDimensions;
		private final @Getter boolean primitive;


		public SimpleGenericImpl(String typeName, List<? extends TypeSig.Simple> genericParams, int arrayDimensions, boolean nullable, boolean primitive) {
			@SuppressWarnings("unchecked")
			val genericParamsCast = (List<TypeSig.Simple>)genericParams;

			this.typeName = typeName;
			this.params = genericParamsCast;
			this.nullable = nullable;
			this.arrayDimensions = arrayDimensions;
			this.primitive = primitive;
		}


		@Override
		public boolean isGeneric() {
			return true;
		}


		@Override
		public boolean isArray() {
			return arrayDimensions > 0;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append(" {");
			dst.append("\"typeName\": \"" + typeName + "\", ");

			dst.append("\"genericParameters\": [");
			JsonWrite.joinStrConsume(params, ", ", dst, (param) -> param.toJson(dst, st));
			dst.append("]");

			if(arrayDimensions > 0) {
				dst.append(", ");
				dst.append("\"arrayDimensions\": " + arrayDimensions);
			}

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
			return typeName + params + (nullable ? "?" : "");
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-4
	 */
	public static interface Resolved extends JsonWritableSig {

		public String getSimpleName();

		public List<String> getFullName();

		public boolean isNullable();

		public boolean isPrimitive();

		public boolean isArray();

		public boolean isGeneric();

		public int getArrayDimensions();

		public List<TypeSig.Resolved> getParams();

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
		private final @Getter List<String> fullName;
		private final @Getter boolean nullable;
		private final @Getter int arrayDimensions;
		private final @Getter boolean primitive;


		public ResolvedBaseImpl(List<String> fullyQualifyingName, int arrayDimensions, boolean nullable, boolean primitive) {
			this.simpleName = fullyQualifyingName.get(fullyQualifyingName.size() - 1);
			this.fullName = fullyQualifyingName;
			this.nullable = nullable;
			this.arrayDimensions = arrayDimensions;
			this.primitive = primitive;
		}


		@Override
		public boolean isGeneric() {
			return false;
		}


		@Override
		public boolean isArray() {
			return arrayDimensions > 0;
		}


		@Override
		public List<Resolved> getParams() {
			return Collections.emptyList();
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append(" {");
			dst.append("\"typeName\": \"" + NameUtil.joinFqName(fullName) + "\"");

			if(arrayDimensions > 0) {
				dst.append(", ");
				dst.append("\"arrayDimensions\": " + arrayDimensions);
			}

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
		private final @Getter List<String> fullName;
		private final @Getter List<TypeSig.Resolved> params;
		private final @Getter boolean nullable;
		private final @Getter int arrayDimensions;
		private final @Getter boolean primitive;


		public ResolvedGenericImpl(List<String> fullyQualifyingName, List<? extends TypeSig.Resolved> genericParams, int arrayDimensions, boolean nullable, boolean primitive) {
			@SuppressWarnings("unchecked")
			val genericParamsCast = (List<TypeSig.Resolved>)genericParams;

			this.simpleName = fullyQualifyingName.get(fullyQualifyingName.size() - 1);
			this.fullName = fullyQualifyingName;
			this.params = genericParamsCast;
			this.nullable = nullable;
			this.arrayDimensions = arrayDimensions;
			this.primitive = primitive;
		}


		@Override
		public boolean isGeneric() {
			return true;
		}


		@Override
		public boolean isArray() {
			return arrayDimensions > 0;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append(" {");
			dst.append("\"typeName\": \"" + NameUtil.joinFqName(fullName) + "\", ");

			dst.append("\"genericParameters\": [");
			JsonWrite.joinStrConsume(params, ", ", dst, (param) -> param.toJson(dst, st));
			dst.append("]");

			if(arrayDimensions > 0) {
				dst.append(", ");
				dst.append("\"arrayDimensions\": " + arrayDimensions);
			}

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
			return simpleName + params + (nullable ? "?" : "");
		}

	}

}
