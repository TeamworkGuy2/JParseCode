package intermAst.classes;

import intermAst.field.IntermFieldSig;
import intermAst.method.IntermMethodSig;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.CompoundBlock;
import baseAst.util.NameUtil;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@Immutable
@AllArgsConstructor
public class IntermClassWithFieldsMethods<T_BLOCK extends CompoundBlock> {
	private final @Getter IntermClassSig signature;
	private final @Getter List<List<String>> usingStatements;
	private final @Getter List<IntermFieldSig> fields;
	private final @Getter List<IntermMethodSig> methods;
	private final @Getter SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree;
	private final @Getter T_BLOCK blockType;


	public void toJson(Appendable dst) throws IOException {
		dst.append("\"" + NameUtil.joinFqName(signature.getFullyQualifyingName()) + "\": {\n");

		dst.append("\"classSignature\": ");
		signature.toJson(dst);
		dst.append(",\n");

		dst.append("\"blockType\": \"" + blockType + "\",\n");

		dst.append("\"using\": [");
		boolean notFirst = false;
		for(val usingStatement : usingStatements) {
			dst.append((notFirst ? ", " : "") + '"' + NameUtil.joinFqName(usingStatement) + '"');
			notFirst = true;
		}
		dst.append("],\n");

		dst.append("\"fields\": [");
		notFirst = false;
		for(val field : fields) {
			dst.append((notFirst ? ", " : ""));
			field.toJson(dst);
			notFirst = true;
		}
		dst.append("],\n");

		dst.append("\"methods\": [");
		notFirst = false;
		for(val method : methods) {
			dst.append((notFirst ? ", " : ""));
			method.toJson(dst);
			notFirst = true;
		}
		dst.append("]\n");

		dst.append("},\n");
	}


	@Override
	public String toString() {
		return blockType + " " + signature.toString() + " { " + StringJoin.Objects.join(fields, "; ") + " " + StringJoin.Objects.join(methods, "; ") + " }";
	}

}
