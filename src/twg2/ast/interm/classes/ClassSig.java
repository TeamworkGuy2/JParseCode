package twg2.ast.interm.classes;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.ast.interm.type.TypeSig;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.tools.NameUtil;
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
		private final @Getter List<TypeSig.TypeSigSimple> params;
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
			val json = JsonStringify.inst;

			dst.append("{ ");

			json.toProp("access", accessModifier.toSrc(), dst);
			json.comma(dst).toProp("name", (st.fullClassName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst);

			if(declarationType != null) {
				json.comma(dst).toProp("declarationType", declarationType, dst);
			}

			if(params != null && params.size() > 0) {
				json.comma(dst).propName("genericParameters", dst)
					.toArrayConsume(params, dst, (p) -> p.toJson(dst, st));
			}

			if(extendImplementSimpleNames.size() > 0) {
				json.comma(dst).propName("extendImplementClassNames", dst)
					.toStringArray(extendImplementSimpleNames, dst);
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
		private final @Getter List<TypeSig.TypeSigResolved> params;
		private final @Getter AccessModifier accessModifier;
		/** The block's type (i.e. 'interface', 'class', 'enum', etc.) */
		private final @Getter String declarationType;
		private final @Getter TypeSig.TypeSigResolved extendClass;
		private final @Getter List<TypeSig.TypeSigResolved> implementInterfaces;


		@Override
		public String getSimpleName() {
			return fullName.get(fullName.size() - 1);
		}


		public boolean isGeneric() {
			return params.size() > 0;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			val json = JsonStringify.inst;

			dst.append("{ ");
			json.toProp("access", accessModifier.toSrc(), dst).comma(dst)
				.toProp("name", (st.fullClassName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst).comma(dst)
				.toProp("declarationType", declarationType, dst);

			if(params != null && params.size() > 0) {
				json.comma(dst).append("\"genericParameters\": ", dst);
				json.toArrayConsume(params, dst, (p) -> p.toJson(dst, st));
			}

			if(extendClass != null) {
				json.comma(dst).append("\"extendClassName\": ", dst);
				extendClass.toJson(dst, st);
			}

			if(implementInterfaces.size() > 0) {
				json.comma(dst).append("\"implementClassNames\": ", dst);
				json.toArrayConsume(implementInterfaces, dst, (intfType) -> intfType.toJson(dst, st));
			}

			dst.append(" }");
		}


		@Override
		public String toString() {
			return accessModifier.toSrc() + " " + declarationType + " " + NameUtil.joinFqName(fullName);
		}

	}

}
