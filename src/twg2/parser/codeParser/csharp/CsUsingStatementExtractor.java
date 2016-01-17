package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class CsUsingStatementExtractor implements AstParser<List<List<String>>> {

	static enum State {
		INIT,
		FOUND_USING,
		COMPLETE,
		FAILED;
	}


	private static final CodeLanguageOptions.CSharp lang = CodeLanguageOptions.C_SHARP;

	List<List<String>> usingStatements = new ArrayList<>();
	State state = State.INIT;
	String name = "C# import statement";


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode) {
		if(state != State.FOUND_USING) {
			if(lang.getAstUtil().getChecker().isKeyword(tokenNode.getData(), CsKeyword.USING)) {
				state = State.FOUND_USING;
			}
		}
		else if(state == State.FOUND_USING) {
			val data = tokenNode.getData();
			if(AstFragType.isIdentifier(data)) {
				usingStatements.add(NameUtil.splitFqName(data.getText()));
				state = State.COMPLETE;
				return true;
			}
			else {
				state = State.FAILED;
			}
		}
		return false;
	}


	@Override
	public List<List<String>> getParserResult() {
		return usingStatements;
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
	public CsUsingStatementExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CsUsingStatementExtractor copy() {
		val copy = new CsUsingStatementExtractor();
		return copy;
	}


	// package-private
	void reset() {
		usingStatements.clear();
		state = State.INIT;
	}

}
