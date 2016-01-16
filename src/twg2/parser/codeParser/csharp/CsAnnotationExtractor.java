package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.val;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.condition.AstParser;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.text.stringUtils.StringTrim;
import twg2.treeLike.simpleTree.SimpleTree;

public class CsAnnotationExtractor implements AstParser<List<AnnotationSig>> {

	static enum State {
		INIT,
		COMPLETE,
		FAILED;
	}


	List<AnnotationSig> annotations = new ArrayList<>();
	State state = State.INIT;
	String name = "C# annotation";


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(state != State.FAILED) {
			if(AstFragType.isBlock(tokenNode.getData(), "[") && tokenNode.getChildren() != null && tokenNode.getChildren().size() > 0) {
				val annot = parseAnnotationBlock(tokenNode);
				annotations.add(annot);
				state = State.COMPLETE;
				return true;
			}
		}
		// annotations are optional
		state = State.COMPLETE;
		return false;
	}


	private static AnnotationSig parseAnnotationBlock(SimpleTree<DocumentFragmentText<CodeFragmentType>> node) {
		val children = node.getChildren();
		val size = children.size();
		if(size == 1) {
			val annotTypeData = children.get(0).getData();
			if(annotTypeData.getFragmentType() != CodeFragmentType.IDENTIFIER) { throw new IllegalArgumentException("annotation node expected to contain identifier, found '" + annotTypeData.getText() + "'"); }
			return new AnnotationSig(annotTypeData.getText(), NameUtil.splitFqName(annotTypeData.getText()), new HashMap<>());
		}
		else {
			val annotTypeData = children.get(0).getData();
			if(annotTypeData.getFragmentType() != CodeFragmentType.IDENTIFIER) { throw new IllegalArgumentException("annotation node expected to contain identifier, found '" + annotTypeData.getText() + "'"); }
			val annotParamsBlock = children.get(1).getData();
			if(annotParamsBlock.getFragmentType() != CodeFragmentType.BLOCK) { throw new IllegalArgumentException("annotation node expected to contain identifier, found '" + annotParamsBlock.getText() + "'"); }

			Map<String, String> params = new HashMap<>();
			val paramChilds = children.get(1).getChildren();
			val sizeI = paramChilds.size();
			if(sizeI == 1) {
				val singleParam = paramChilds.get(0).getData();
				if(singleParam.getFragmentType().isCompound()) { throw new IllegalArgumentException("annotation param expected to start with an identifier, string, or other literal value, found '" + singleParam.getText() + "'"); }
				params.put("value", StringTrim.trimQuotes(singleParam.getText()));
			}
			else {
				for(int i = 0; i < sizeI; i+=3) {
					val nameParam = paramChilds.get(i).getData();
					if(nameParam.getFragmentType() != CodeFragmentType.IDENTIFIER) { throw new IllegalArgumentException("annotation param expected to start with identifier, found '" + nameParam.getText() + "'"); }
					if(paramChilds.get(i + 1).getData().getFragmentType() != CodeFragmentType.OPERATOR) { throw new IllegalArgumentException("annotation param expected to be separated by operator, found '" + paramChilds.get(i + 1).getData().getText() + "'"); }
					String valueStr = StringTrim.trimQuotes(paramChilds.get(i + 2).getData().getText());
					int iOff = 3;
					if(i + iOff + 1 < sizeI && paramChilds.get(i + iOff).getData().getFragmentType() == CodeFragmentType.OPERATOR) {
						iOff++;
						valueStr = valueStr + StringTrim.trimQuotes(paramChilds.get(i + iOff).getData().getText());
						iOff++;
					}
					i += (iOff - 3);
					params.put(nameParam.getText().trim(), valueStr);
				}
			}
			return new AnnotationSig(annotTypeData.getText(), NameUtil.splitFqName(annotTypeData.getText()), params);
		}
	}


	@Override
	public List<AnnotationSig> getParserResult() {
		return annotations;
	}


	@Override
	public boolean isComplete() {
		return state == State.COMPLETE;
	}


	@Override
	public boolean isFailed() {
		return state == State.FAILED;
	}


	@Override
	public boolean canRecycle() {
		return true;
	}


	@Override
	public CsAnnotationExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CsAnnotationExtractor copy() {
		val copy = new CsAnnotationExtractor();
		return copy;
	}


	// package-private
	void reset() {
		annotations.clear();
		state = State.INIT;
	}

}
