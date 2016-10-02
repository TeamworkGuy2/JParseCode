package twg2.ast.interm.type;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2015-12-12
 */
public interface TypeSig {


	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-4
	 */
	public static interface TypeSigSimple extends JsonWritableSig {

		public String getTypeName();

		public boolean isNullable();

		public boolean isPrimitive();

		public boolean isArray();

		public boolean isGeneric();

		public int getArrayDimensions();

		public List<TypeSigSimple> getParams();

		@Override
		public String toString();

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 */
	@Immutable
	public static class TypeSigSimpleBase implements TypeSigSimple {
		private final @Getter String typeName;
		private final @Getter boolean nullable;
		private final @Getter int arrayDimensions;
		private final @Getter boolean primitive;


		public TypeSigSimpleBase(String typeName, int arrayDimensions, boolean nullable, boolean primitive) {
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
		public List<TypeSigSimple> getParams() {
			return Collections.emptyList();
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
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

			dst.append(" }");
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
	public static class TypeSigSimpleGeneric implements TypeSigSimple {
		private final @Getter String typeName;
		private final @Getter List<TypeSigSimple> params;
		private final @Getter boolean nullable;
		private final @Getter int arrayDimensions;
		private final @Getter boolean primitive;


		public TypeSigSimpleGeneric(String typeName, List<? extends TypeSigSimple> genericParams, int arrayDimensions, boolean nullable, boolean primitive) {
			@SuppressWarnings("unchecked")
			val genericParamsCast = (List<TypeSigSimple>)genericParams;

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
			dst.append("{ ");
			dst.append("\"typeName\": \"" + typeName + "\", ");

			dst.append("\"genericParameters\": [");
			JsonStringify.joinConsume(params, ", ", dst, (param) -> param.toJson(dst, st));
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

			dst.append(" }");
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
	public static interface TypeSigResolved extends JsonWritableSig {

		public String getSimpleName();

		public List<String> getFullName();

		public boolean isNullable();

		public boolean isPrimitive();

		public boolean isArray();

		public boolean isGeneric();

		public int getArrayDimensions();

		public List<TypeSigResolved> getParams();

		@Override
		public String toString();

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 */
	@Immutable
	public static class TypeSigResolvedBase implements TypeSigResolved {
		private final @Getter String simpleName;
		private final @Getter List<String> fullName;
		private final @Getter boolean nullable;
		private final @Getter int arrayDimensions;
		private final @Getter boolean primitive;


		public TypeSigResolvedBase(List<String> fullyQualifyingName, int arrayDimensions, boolean nullable, boolean primitive) {
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
		public List<TypeSigResolved> getParams() {
			return Collections.emptyList();
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
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

			dst.append(" }");
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
	public static class TypeSigResolvedGeneric implements TypeSigResolved {
		private final @Getter String simpleName;
		private final @Getter List<String> fullName;
		private final @Getter List<TypeSigResolved> params;
		private final @Getter boolean nullable;
		private final @Getter int arrayDimensions;
		private final @Getter boolean primitive;


		public TypeSigResolvedGeneric(List<String> fullyQualifyingName, List<? extends TypeSigResolved> genericParams, int arrayDimensions, boolean nullable, boolean primitive) {
			@SuppressWarnings("unchecked")
			val genericParamsCast = (List<TypeSigResolved>)genericParams;

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
			dst.append("{ ");
			dst.append("\"typeName\": \"" + NameUtil.joinFqName(fullName) + "\", ");

			dst.append("\"genericParameters\": [");
			JsonStringify.joinConsume(params, ", ", dst, (param) -> param.toJson(dst, st));
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

			dst.append(" }");
		}


		@Override
		public String toString() {
			return simpleName + params + (nullable ? "?" : "");
		}

	}

}
