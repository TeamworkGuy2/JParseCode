package twg2.parser.codeParser.java;

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

public class JavaAnnotationExtractor implements AstParser<List<AnnotationSig>> {

	static enum State {
		INIT,
		FOUND_ANNOTATION_MARK,
		FOUND_NAME,
		COMPLETE,
		FAILED;
	}


	List<AnnotationSig> annotations = new ArrayList<>();
	String foundName;
	State state = State.INIT;
	String name = "Java annotation";


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if((state == State.COMPLETE || state == State.FAILED || state == State.FOUND_NAME) &&
				(AstFragType.isType(tokenNode.getData(), CodeFragmentType.SEPARATOR) && "@".equals(tokenNode.getData().getText()))) {
			if(state == State.FOUND_NAME) {
				val annot = parseAnnotationBlock(foundName, tokenNode);
				annotations.add(annot);
				foundName = null;
			}
			state = State.FOUND_ANNOTATION_MARK;
			return true;
		}
		else if(state == State.FOUND_ANNOTATION_MARK) {
			if(AstFragType.isIdentifier(tokenNode.getData())) {
				foundName = tokenNode.getData().getText();
				state = State.FOUND_NAME;
				return true;
			}
			state = State.FAILED;
		}
		else if(state == State.FOUND_NAME) {
			val annot = parseAnnotationBlock(foundName, tokenNode);
			annotations.add(annot);
			foundName = null;
			state = State.COMPLETE;
			return true;
		}
		// annotations are optional
		state = State.COMPLETE;
		return false;
	}


	private static AnnotationSig parseAnnotationBlock(String annotName, SimpleTree<DocumentFragmentText<CodeFragmentType>> node) {
		val children = node.getChildren();
		val size = children.size();
		if(size == 0 || !AstFragType.isBlock(node.getData(), "(")) {
			return new AnnotationSig(annotName, NameUtil.splitFqName(annotName), new HashMap<>());
		}
		else {
			Map<String, String> params = new HashMap<>();
			if(size == 1) {
				val singleParam = children.get(0).getData();
				if(singleParam.getFragmentType().isCompound()) { throw new IllegalArgumentException("annotation param expected to start with an identifier, string, or other literal value, found '" + singleParam.getText() + "'"); }
				params.put("value", StringTrim.trimQuotes(singleParam.getText()));
			}
			else {
				for(int i = 0; i < size; i+=3) {
					val nameParam = children.get(i).getData();
					if(nameParam.getFragmentType() != CodeFragmentType.IDENTIFIER) { throw new IllegalArgumentException("annotation param expected to start with identifier, found '" + nameParam.getText() + "'"); }
					if(children.get(i + 1).getData().getFragmentType() != CodeFragmentType.OPERATOR) { throw new IllegalArgumentException("annotation param expected to be separated by operator, found '" + children.get(i + 1).getData().getText() + "'"); }
					String valueStr = StringTrim.trimQuotes(children.get(i + 2).getData().getText());
					int iOff = 3;
					if(i + iOff + 1 < size && children.get(i + iOff).getData().getFragmentType() == CodeFragmentType.OPERATOR) {
						iOff++;
						valueStr = valueStr + StringTrim.trimQuotes(children.get(i + iOff).getData().getText());
						iOff++;
					}
					i += (iOff - 3);
					params.put(nameParam.getText().trim(), valueStr);
				}
			}
			return new AnnotationSig(annotName, NameUtil.splitFqName(annotName), params);
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
	public JavaAnnotationExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public JavaAnnotationExtractor copy() {
		val copy = new JavaAnnotationExtractor();
		return copy;
	}


	// package-private
	void reset() {
		annotations.clear();
		state = State.INIT;
	}

}
