package twg2.ast.interm.classes;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.field.FieldDefResolved;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.field.FieldSigResolved;
import twg2.ast.interm.method.MethodSig;
import twg2.ast.interm.method.ParameterSig;
import twg2.ast.interm.method.ParameterSigResolved;
import twg2.io.write.JsonWrite;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public interface ClassAst<T_SIG extends ClassSig, T_METHOD extends JsonWritableSig, T_BLOCK extends BlockType> extends JsonWritableSig {

	public T_SIG getSignature();

	public List<List<String>> getUsingStatements();

	public List<T_METHOD> getMethods();

	public T_BLOCK getBlockType();




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-4
	 */
	@Immutable
	public static class Impl<T_SIG extends ClassSig, T_ENUM extends JsonWritableSig, T_FIELD extends JsonWritableSig, T_METHOD extends JsonWritableSig, T_PARAM extends JsonWritableSig, T_BLOCK extends BlockType>
			implements ClassAst<T_SIG, T_METHOD, T_BLOCK> {
		private final @Getter T_SIG signature;
		private final @Getter List<List<String>> usingStatements;
		private final @Getter List<T_ENUM> enumMembers;
		private final @Getter List<T_FIELD> fields;
		private final @Getter List<T_METHOD> methods;
		private final @Getter T_BLOCK blockType;


		public Impl(T_SIG signature, List<List<String>> usingStatements, List<? extends T_FIELD> fields, List<? extends T_METHOD> methods, List<? extends T_ENUM> enums, T_BLOCK blockType) {
			@SuppressWarnings("unchecked")
			val fieldsCast = (List<T_FIELD>)fields;
			@SuppressWarnings("unchecked")
			val methodsCast = (List<T_METHOD>)methods;
			@SuppressWarnings("unchecked")
			val enumsCast = (List<T_ENUM>)enums;

			if(enumsCast != null && enumsCast.size() > 0) {
				System.out.println();
			}

			this.signature = signature;
			this.usingStatements = usingStatements;
			this.enumMembers = enumsCast;
			this.fields = fieldsCast;
			this.methods = methodsCast;
			this.blockType = blockType;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{\n");

			dst.append("\t\"classSignature\": ");
			signature.toJson(dst, st);
			dst.append(",\n");

			dst.append("\t\"blockType\": \"" + blockType + "\",\n");

			dst.append("\t\"using\": [");
			JsonWrite.joinStr(usingStatements, ", ", dst, (us) -> '"' + NameUtil.joinFqName(us) + '"');
			dst.append("],\n");

			if(enumMembers != null && blockType.isEnum()) {
				dst.append("\t\"enumMembers\": [");
				if(enumMembers.size() > 0) {
					dst.append("\n\t\t");
					JsonWrite.joinStrConsume(enumMembers, ",\n\t\t", dst, (f) -> f.toJson(dst, st));
					dst.append("\n\t");
				}
				dst.append("],\n");
			}

			dst.append("\t\"fields\": [");
			if(fields.size() > 0) {
				dst.append("\n\t\t");
				JsonWrite.joinStrConsume(fields, ",\n\t\t", dst, (f) -> f.toJson(dst, st));
				dst.append("\n\t");
			}
			dst.append("],\n");

			dst.append("\t\"methods\": [");
			if(methods.size() > 0) {
				dst.append("\n\t\t");
				JsonWrite.joinStrConsume(methods, ",\n\t\t", dst, (m) -> m.toJson(dst, st));
				dst.append("\n\t");
			}
			dst.append("]\n");

			dst.append("}");
		}


		@Override
		public String toString() {
			return blockType + " " + signature.toString() + " { " + StringJoin.Objects.join(fields, "; ") + " " + StringJoin.Objects.join(methods, "; ") + " }";
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-2
	 */
	@Immutable
	public static class SimpleImpl<T_BLOCK extends BlockType> extends Impl<ClassSig.SimpleImpl, FieldDef, FieldSig, MethodSig.SimpleImpl, ParameterSig, T_BLOCK> {

		public SimpleImpl(ClassSig.SimpleImpl signature, List<List<String>> usingStatements, List<? extends FieldSig> fields,
				List<? extends MethodSig.SimpleImpl> methods, List<? extends FieldDef> enums, T_BLOCK blockType) {
			super(signature, usingStatements, fields, methods, enums, blockType);
		}
		
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-4
	 */
	@Immutable
	public static class ResolvedImpl<T_BLOCK extends BlockType> extends Impl<ClassSig.ResolvedImpl, FieldDefResolved, FieldSigResolved, MethodSig.ResolvedImpl, ParameterSigResolved, T_BLOCK> {

		public ResolvedImpl(ClassSig.ResolvedImpl signature, List<List<String>> usingStatements, List<? extends FieldSigResolved> fields,
				List<? extends MethodSig.ResolvedImpl> methods, List<? extends FieldDefResolved> enums, T_BLOCK blockType) {
			super(signature, usingStatements, fields, methods, enums, blockType);
		}
		
	}

}
