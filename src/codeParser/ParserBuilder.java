package codeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.val;
import parser.condition.Precondition;
import parser.textFragment.TextFragmentRef;
import parser.textFragment.TextTransformer;
import streamUtils.EnhancedIterator;
import streamUtils.StringLineSupplier;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParser.TextParserImpl;
import twg2.treeLike.simpleTree.SimpleTree;
import documentParser.DocumentFragmentText;
import documentParser.DocumentParser;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class ParserBuilder {
	private DocumentParser<CodeFragmentType> fileParser;


	{
		this.fileParser = new DocumentParser<>();
	}


	public ParserBuilder addParser(Precondition parser, TextTransformer<CodeFragmentType> transformer) {
		this.fileParser.addFragmentParser(parser, transformer);
		return this;
	}


	public ParserBuilder addConstParser(Precondition parser, CodeFragmentType type) {
		this.fileParser.addFragmentParser(parser, (text, off, len) -> type);
		return this;
	}


	public CodeFile<DocumentFragmentText<CodeFragmentType>> buildAndParse(String src) throws IOException {
		List<String> lines = new ArrayList<>();

		// intercept each line request and add the line to our list of lines
		StringLineSupplier srcLineReader = new StringLineSupplier(src, 0, src.length(), true, true);
		EnhancedIterator<String> lineReader = new EnhancedIterator<>(() -> {
			String str = srcLineReader.get();
			if(str != null) {
				lines.add(str);
			}
			return str;
		});

		TextParser input = new TextParserImpl(lineReader, true);

		val docTextFragment = new TextFragmentRef.ImplMut(0, src.length(), 0, 0, -1, -1);
		val docRoot = new DocumentFragmentText<>(CodeFragmentType.DOCUMENT, docTextFragment, docTextFragment.getText(src).toString());

		SimpleTree<DocumentFragmentText<CodeFragmentType>> docTree = fileParser.parseDocument(input, docRoot,
				(type, frag) -> new DocumentFragmentText<>(type, frag, frag.getText(src).toString()),
				(docFrag) -> docFrag.getFragmentType().isCompound(),
				(parent, child) -> parent != child && parent.getTextFragment().contains(child.getTextFragment()));

		docTextFragment.setLineEnd(input.getLineNumber() - 1);
		docTextFragment.setColumnEnd(input.getColumnNumber() - 1);

		return new CodeFile<>(docTree, src, lines);
	}

}
