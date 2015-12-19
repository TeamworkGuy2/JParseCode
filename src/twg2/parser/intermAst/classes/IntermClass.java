package twg2.parser.intermAst.classes;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.util.NameUtil;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.JsonWrite;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@Immutable
@AllArgsConstructor
public class IntermClass<T_BLOCK extends CompoundBlock> implements JsonWritableSig {
	private final @Getter IntermClassSig signature;
	private final @Getter List<List<String>> usingStatements;
	private final @Getter List<IntermFieldSig> fields;
	private final @Getter List<IntermMethodSig> methods;
	private final @Getter SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree;
	private final @Getter T_BLOCK blockType;


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append("\"" + NameUtil.joinFqName(signature.getFullyQualifyingName()) + "\": {\n");

		dst.append("\"classSignature\": ");
		signature.toJson(dst, st);
		dst.append(",\n");

		dst.append("\"blockType\": \"" + blockType + "\",\n");

		dst.append("\"using\": [");
		JsonWrite.joinStrConsumer(usingStatements, ", ", dst, (us) -> dst.append('"' + NameUtil.joinFqName(us) + '"'));
		dst.append("],\n");

		dst.append("\"fields\": [");
		JsonWrite.joinStrConsumer(fields, ", ", dst, (f) -> f.toJson(dst, st));
		dst.append("],\n");

		dst.append("\"methods\": [");
		JsonWrite.joinStrConsumer(methods, ", ", dst, (m) -> m.toJson(dst, st));
		dst.append("]\n");

		dst.append("},\n");
	}


	@Override
	public String toString() {
		return blockType + " " + signature.toString() + " { " + StringJoin.Objects.join(fields, "; ") + " " + StringJoin.Objects.join(methods, "; ") + " }";
	}

}
