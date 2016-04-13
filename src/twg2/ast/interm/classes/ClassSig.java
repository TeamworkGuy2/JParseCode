package twg2.ast.interm.classes;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.type.TypeSig;
import twg2.io.write.JsonWrite;
import twg2.parser.baseAst.AccessModifier;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public interface ClassSig extends JsonWritableSig {

	public AccessModifier getAccessModifier();

	public List<String> getFullName();

	public String getDeclarationType();

	public String getSimpleName();




	@Immutable
	@AllArgsConstructor
	public static class SimpleImpl implements ClassSig {
		private final @Getter List<String> fullName;
		/** This type's generic type parameters, if any */
		private final @Getter List<TypeSig.Simple> params;
		/** The block's type (i.e. 'interface', 'class', 'enum', etc.) */
		private final @Getter AccessModifier accessModifier;
		private final @Getter String declarationType;
		private final @Getter List<String> extendImplementSimpleNames;


		@Override
		public String getSimpleName() {
			return fullName.get(fullName.size() - 1);
		}


		public boolean isGeneric() {
			return params.size() > 0;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
			dst.append("\"access\": \"" + accessModifier + "\", ");
			dst.append("\"name\": \"" + (st.fullClassName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)) + "\"");

			if(declarationType != null) {
				dst.append(", ");
				dst.append("\"declarationType\": \"" + declarationType + "\"");
			}

			if(params != null && params.size() > 0) {
				dst.append(", ");
				dst.append("\"genericParameters\": [");
				JsonWrite.joinStrConsume(params, ", ", dst, (p) -> p.toJson(dst, st));
				dst.append("]");
			}

			if(extendImplementSimpleNames.size() > 0) {
				dst.append(", ");
				dst.append("\"extendImplementClassNames\": [");
				JsonWrite.joinStr(extendImplementSimpleNames, ", ", dst, (n) -> '"' + n + '"');
				dst.append("] ");
			}

			dst.append(" }");
		}


		@Override
		public String toString() {
			return accessModifier.toSrc() + " " + declarationType + " " + NameUtil.joinFqName(fullName);
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-5
	 */
	@Immutable
	@AllArgsConstructor
	public static class ResolvedImpl implements ClassSig {
		private final @Getter List<String> fullName;
		/** This type's generic type parameters, if any */
		private final @Getter List<TypeSig.Resolved> params;
		private final @Getter AccessModifier accessModifier;
		/** The block's type (i.e. 'interface', 'class', 'enum', etc.) */
		private final @Getter String declarationType;
		private final @Getter TypeSig.Resolved extendClass;
		private final @Getter List<TypeSig.Resolved> implementInterfaces;


		@Override
		public String getSimpleName() {
			return fullName.get(fullName.size() - 1);
		}


		public boolean isGeneric() {
			return params.size() > 0;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
			dst.append("\"access\": \"" + accessModifier + "\", ");
			dst.append("\"name\": \"" + (st.fullClassName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)) + "\", ");
			dst.append("\"declarationType\": \"" + declarationType + "\"");

			if(params != null && params.size() > 0) {
				dst.append(", ");
				dst.append("\"genericParameters\": [");
				JsonWrite.joinStrConsume(params, ", ", dst, (p) -> p.toJson(dst, st));
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
				JsonWrite.joinStrConsume(implementInterfaces, ", ", dst, (intfType) -> intfType.toJson(dst, st));
				dst.append("] ");
			}

			dst.append(" }");
		}


		@Override
		public String toString() {
			return accessModifier.toSrc() + " " + declarationType + " " + NameUtil.joinFqName(fullName);
		}

	}

}