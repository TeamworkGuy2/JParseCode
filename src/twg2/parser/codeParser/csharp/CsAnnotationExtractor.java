package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.parser.codeParser.extractors.AnnotationExtractor;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.stateMachine.AstParserReusableBase;
import twg2.treeLike.simpleTree.SimpleTree;

public class CsAnnotationExtractor extends AstParserReusableBase<CsAnnotationExtractor.State, List<AnnotationSig>> {
	public static int acceptNextCalls = 0;

	static enum State {
		INIT,
		COMPLETE,
		FAILED;
	}


	List<AnnotationSig> annotations = new ArrayList<>();


	public CsAnnotationExtractor() {
		super("C# annotation", State.COMPLETE, State.FAILED);
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		var lang = CodeLanguageOptions.C_SHARP;
		acceptNextCalls++;

		if(state != State.FAILED) {
			var childs = tokenNode.getChildren();
			var childCount = childs != null ? childs.size() : 0;
			CodeToken annotTypeFrag = null;
			if(AstFragType.isBlock(tokenNode.getData(), '[')) {
				int i = 0;
				while(i < childCount && AstFragType.isIdentifier(annotTypeFrag = childs.get(i).getData())) {
					var paramsChild = childs.size() > i + 1 ? childs.get(i + 1) : null;

					var annot = AnnotationExtractor.parseAnnotationBlock(lang, annotTypeFrag.getTokenType(), annotTypeFrag.getText(), paramsChild);
					annotations.add(annot);
					state = State.COMPLETE;
					i += (paramsChild != null && paramsChild.size() > 0 ? 2 : 1);
				}
				return i > 0;
			}
		}
		// annotations are optional
		state = State.COMPLETE;
		return false;
	}


	@Override
	public List<AnnotationSig> getParserResult() {
		return annotations;
	}


	@Override
	public CsAnnotationExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CsAnnotationExtractor copy() {
		return new CsAnnotationExtractor();
	}


	// package-private
	void reset() {
		annotations.clear();
		state = State.INIT;
	}

}
