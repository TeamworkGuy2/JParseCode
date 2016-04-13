package twg2.parser.codeParser.java;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.documentParser.CodeFragment;
import twg2.parser.language.CodeLanguageOptions;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public class JavaImportStatementExtractor implements AstParser<List<List<String>>> {

	static enum State {
		INIT,
		FOUND_USING,
		COMPLETE,
		FAILED;
	}


	private static final CodeLanguageOptions.Java lang = CodeLanguageOptions.JAVA;

	List<List<String>> usingStatements = new ArrayList<>();
	State state = State.INIT;
	String name = "Java import statement";


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeFragment> tokenNode) {
		if(state != State.FOUND_USING) {
			if(lang.getAstUtil().getChecker().isKeyword(tokenNode.getData(), JavaKeyword.IMPORT)) {
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
	public JavaImportStatementExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public JavaImportStatementExtractor copy() {
		val copy = new JavaImportStatementExtractor();
		return copy;
	}


	// package-private
	void reset() {
		usingStatements.clear();
		state = State.INIT;
	}

}
