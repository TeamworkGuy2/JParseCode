package documentParser.block;

import twg2.collections.primitiveCollections.IntArrayList;
import twg2.collections.primitiveCollections.IntList;
import twg2.text.stringUtils.StringIndex;

/** Static methods to parse documentParser.block of text marked by start and end characters or strings.
 * For example the input string:<br>
 * <pre>function test(t) {
 *   t = Number.parseInt(t, 10);
 *   {
 *     var value = 3 * 5, neg = -1, pos = 1;
 *     t = t < value ? t * neg : value;
 *     t = Math.max(t, pos);
 *   }
 *   return t;
 * }</pre>
 * 
 * Would return 4 documentParser.block representing the strings:<br>
 * <pre>"function test(t) "</pre>
 * <pre>"{
 *   t = Number.parseInt(t, 10);"</pre>
 * <pre>"  {
 *     var value = 3 * 5, neg = -1, pos = 1;
 *     t = t < value ? t * neg : value;
 *     t = Math.max(t, pos);
 *   }"</pre>
 * <pre>"  return t;
 * }"<pre><br>
 * @author TeamworkGuy2
 * @since 2014-12-12
 */
@javax.annotation.Generated("StringTemplate")
public final class ParseBlocks {

	private ParseBlocks() { throw new AssertionError("cannot instantiate static class ParseBlocks"); }


	/** Identify the documentParser.block in the {@code input} string.
	 * @param input the string containing {@code blockStart} and {@code blockEnd}
	 * @param blockStart the string that identifies the start of a block
	 * @param blockEnd the string that identifies the end of a block
	 * @return a {@link TextBlock} with nested documentParser.block describing each of the documentParser.block in {@code input}
	 */
	public static final TextBlock splitIntoBlocks(String input, String blockStart, String blockEnd) {
		IntermediateBlock blocks = new IntermediateBlock();
		splitIntoBlocks(blocks, input, blockStart, blockEnd, new IntArrayList());
		return blocks;
	}


	/** Identify the documentParser.block in the {@code input} string.
	 * @param input the string containing {@code blockStart} and {@code blockEnd}
	 * @param blockStart the character that identifies the start of a block
	 * @param blockEnd the character that identifies the end of a block
	 * @param dst the destination to store pairs of start and end block indices in.
	 * The indices are zero based character indices into the {@code input} string
	 * @return a {@link TextBlock} with nested documentParser.block describing each of the documentParser.block in {@code input}
	 */
	public static final TextBlock splitIntoBlocks(String input, int blockStart, int blockEnd, IntArrayList dst) {
		IntermediateBlock blocks = new IntermediateBlock();
		splitIntoBlocks(blocks, input, blockStart, blockEnd, dst);
		return blocks;
	}


	/** Identify the documentParser.block in the {@code input} string.
	 * @param input the string containing {@code blockStart} and {@code blockEnd}
	 * @param blockStart the string that identifies the start of a block
	 * @param blockEnd the string that identifies the end of a block
	 * @param dst the destination to store pairs of start and end block indices in.
	 * The indices are zero based character indices into the {@code input} string
	 * @return a {@link TextBlock} with nested documentParser.block describing each of the documentParser.block in {@code input}
	 */
	public static final TextBlock splitIntoBlocks(StringBuilder input, String blockStart, String blockEnd,
			IntArrayList dst) {
		IntermediateBlock blocks = new IntermediateBlock();
		splitIntoBlocks(blocks, input, blockStart, blockEnd, dst);
		return blocks;
	}


	/** Identify the documentParser.block in the {@code input} string.
	 * @param input the string containing {@code blockStart} and {@code blockEnd}
	 * @param blockStart the character that identifies the start of a block
	 * @param blockEnd the character that identifies the end of a block
	 * @param dst the destination to store pairs of start and end block indices in.
	 * The indices are zero based character indices into the {@code input} string
	 * @return a {@link TextBlock} with nested documentParser.block describing each of the documentParser.block in {@code input}
	 */
	public static final TextBlock splitIntoBlocks(StringBuilder input, int blockStart, int blockEnd,
			IntArrayList dst) {
		IntermediateBlock blocks = new IntermediateBlock();
		splitIntoBlocks(blocks, input, blockStart, blockEnd, dst);
		return blocks;
	}


	/** Identify the documentParser.block in the {@code input} string.
	 * @param documentParser.block the destination root {@link IntermediateBlock} to store parsed
	 * {@link TextBlock TextBlocks} in
	 * @param input the string containing {@code blockStart} and {@code blockEnd} to parse
	 * @param startStr the string that identifies the start of a block
	 * @param endStr the string that identifies the end of a block
	 * @param dst the destination to store pairs of start and end block indices in.
	 * The indices are zero based character indices into the {@code input} string.
	 * A {@link TextBlock} with nested documentParser.block describing each of the documentParser.block in {@code input}
	 */
	public static final void splitIntoBlocks(IntermediateBlock blocks, String input, String startStr, String endStr,
			IntList dst) {
		int startStrLen = startStr.length();
		int endStrLen = endStr.length();
		IntArrayList startIndices = new IntArrayList();
		int startIndex = input.indexOf(startStr, 0);
		int prevEndIndex = 0;
		int endIndex = input.indexOf(endStr, 0);
		IntermediateBlock blockDepth = blocks;

		int level = 0;
		int loopI = 0;
		while(endIndex > -1) {
			// find each leaf block
			while(startIndex > -1 && startIndex < endIndex) {
				startIndices.add(startIndex);
				if(startIndices.size() > 1) {
					int tmpStart = startIndices.get(startIndices.size() - 2) + startStrLen;
					int tmpLength = startIndex - tmpStart;
					dst.add(tmpStart);
					dst.add(startIndex);
					int len = tmpLength;
					for(int i = 0, size = startIndices.size(); i < size; i++) {
						//startIndices.set(i, startIndices.get(i) - len);
					}
					String s1 = input.substring(tmpStart, tmpStart + tmpLength);
					blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
				}
				else {
					int tmpStart = loopI > 0 ? prevEndIndex + endStrLen : prevEndIndex;
					int tmpLength = startIndex - tmpStart;
					dst.add(tmpStart);
					dst.add(startIndex);
					for(int i = 0, size = startIndices.size(); i < size; i++) {
						//startIndices.set(i, startIndices.get(i) - tmpLength);
					}
					String s2 = input.substring(tmpStart, tmpStart + tmpLength);
					blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
				}
				startIndex = input.indexOf(startStr, startIndex + startStrLen);

				IntermediateBlock tempBlock = new IntermediateBlock(blockDepth);
				blockDepth.addSubBlock(tempBlock);
				blockDepth = tempBlock;

				level++;
			}

			if(startIndices.size() > 0) {
				int tmpStart = startIndices.get(startIndices.size() - 1) + startStrLen;
				int tmpLength = endIndex - tmpStart;
				dst.add(tmpStart);
				dst.add(endIndex);
				for(int i = 0, size = startIndices.size(); i < size; i++) {
					//startIndices.set(i, startIndices.get(i) - tmpLength);
				}
				String s3 = input.substring(tmpStart, tmpStart + tmpLength);
				blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
			}
			else {
				int tmpStart = prevEndIndex + endStrLen;
				int tmpLength = endIndex - tmpStart;
				dst.add(tmpStart);
				dst.add(endIndex);
				for(int i = 0, size = startIndices.size(); i < size; i++) {
					//startIndices.set(i, startIndices.get(i) - tmpLength);
				}
				String s4 = input.substring(tmpStart, tmpStart + tmpLength);
				blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
			}

			prevEndIndex = endIndex;
			endIndex = input.indexOf(endStr, endIndex + endStrLen);
			startIndices.clear();
			if(blockDepth.hasParentBlock()) {
				blockDepth = blockDepth.getParentBlock();
			}
			else if(endIndex != -1) {
				throw new IllegalStateException("mismatching number of opening/closing documentParser.block");
			}
			level--;
			loopI++;
		}

		int tmpStart = loopI > 0 ? prevEndIndex + startStrLen : prevEndIndex;
		int tmpLength = input.length() - tmpStart;
		for(int i = 0, size = startIndices.size(); i < size; i++) {
			//startIndices.set(i, startIndices.get(i) - tmpLength);
		}
		String s5 = input.substring(tmpStart, tmpLength);
		blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength - tmpStart));
	}


	/** Identify the documentParser.block in the {@code input} string.
	 * @param documentParser.block the destination root {@link IntermediateBlock} to store parsed
	 * {@link TextBlock TextBlocks} in
	 * @param input the string containing {@code blockStart} and {@code blockEnd} to parse
	 * @param startChar the string that identifies the start of a block
	 * @param endChar the string that identifies the end of a block
	 * @param dst the destination to store pairs of start and end block indices in.
	 * The indices are zero based character indices into the {@code input} string.
	 * A {@link TextBlock} with nested documentParser.block describing each of the documentParser.block in {@code input}
	 */
	public static final void splitIntoBlocks(IntermediateBlock blocks, String input, int startChar, int endChar,
			IntList dst) {
		int startStrLen = 1;
		int endStrLen = 1;
		IntArrayList startIndices = new IntArrayList();
		int startIndex = input.indexOf(startChar, 0);
		int prevEndIndex = 0;
		int endIndex = input.indexOf(endChar, 0);
		IntermediateBlock blockDepth = blocks;

		int level = 0;
		int loopI = 0;
		while(endIndex > -1) {
			// find each leaf block
			while(startIndex > -1 && startIndex < endIndex) {
				startIndices.add(startIndex);
				if(startIndices.size() > 1) {
					int tmpStart = startIndices.get(startIndices.size() - 2) + startStrLen;
					int tmpLength = startIndex - tmpStart;
					dst.add(tmpStart);
					dst.add(startIndex);
					int len = startIndex - (startIndices.size() - 2) + startStrLen;
					for(int i = 0, size = startIndices.size(); i < size; i++) {
						//startIndices.set(i, startIndices.get(i) - len);
					}
					String s1 = input.substring(tmpStart, tmpStart + tmpLength);
					blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
				}
				else {
					int tmpStart = loopI > 0 ? prevEndIndex + endStrLen : prevEndIndex;
					int tmpLength = startIndex - tmpStart;
					dst.add(tmpStart);
					dst.add(startIndex);
					for(int i = 0, size = startIndices.size(); i < size; i++) {
						//startIndices.set(i, startIndices.get(i) - tmpLength);
					}
					String s2 = input.substring(tmpStart, tmpStart + tmpLength);
					blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
				}
				startIndex = StringIndex.indexOf(input, startIndex + startStrLen, startChar);

				IntermediateBlock tempBlock = new IntermediateBlock(blockDepth);
				blockDepth.addSubBlock(tempBlock);
				blockDepth = tempBlock;

				level++;
			}

			if(startIndices.size() > 0) {
				int tmpStart = startIndices.get(startIndices.size() - 1) + startStrLen;
				int tmpLength = endIndex - tmpStart;
				dst.add(tmpStart);
				dst.add(endIndex);
				for(int i = 0, size = startIndices.size(); i < size; i++) {
					//startIndices.set(i, startIndices.get(i) - tmpLength);
				}
				String s3 = input.substring(tmpStart, tmpStart + tmpLength);
				blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
			}
			else {
				int tmpStart = prevEndIndex + endStrLen;
				int tmpLength = endIndex - tmpStart;
				dst.add(tmpStart);
				dst.add(endIndex);
				for(int i = 0, size = startIndices.size(); i < size; i++) {
					//startIndices.set(i, startIndices.get(i) - tmpLength);
				}
				String s4 = input.substring(tmpStart, tmpStart + tmpLength);
				blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
			}

			prevEndIndex = endIndex;
			endIndex = StringIndex.indexOf(input, endIndex + endStrLen, endChar);
			startIndices.clear();
			if(blockDepth.hasParentBlock()) {
				blockDepth = blockDepth.getParentBlock();
			}
			else if(endIndex != -1) {
				throw new IllegalStateException("mismatching number of opening/closing documentParser.block");
			}
			level--;
			loopI++;
		}

		int tmpStart = loopI > 0 ? prevEndIndex + startStrLen : prevEndIndex;
		int tmpLength = input.length();
		for(int i = 0, size = startIndices.size(); i < size; i++) {
			//startIndices.set(i, startIndices.get(i) - tmpLength);
		}
		String s5 = input.substring(tmpStart, tmpLength);
		blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength - tmpStart));
	}


	/** Identify the documentParser.block in the {@code input} string.
	 * @param documentParser.block the destination root {@link IntermediateBlock} to store parsed
	 * {@link TextBlock TextBlocks} in
	 * @param input the string containing {@code blockStart} and {@code blockEnd} to parse
	 * @param startStr the string that identifies the start of a block
	 * @param endStr the string that identifies the end of a block
	 * @param dst the destination to store pairs of start and end block indices in.
	 * The indices are zero based character indices into the {@code input} string.
	 * A {@link TextBlock} with nested documentParser.block describing each of the documentParser.block in {@code input}
	 */
	public static final void splitIntoBlocks(IntermediateBlock blocks, StringBuilder input, String startStr, String endStr,
			IntList dst) {
		int startStrLen = startStr.length();
		int endStrLen = endStr.length();
		IntArrayList startIndices = new IntArrayList();
		int startIndex = input.indexOf(startStr);
		int prevEndIndex = 0;
		int endIndex = input.indexOf(endStr);
		IntermediateBlock blockDepth = blocks;

		int level = 0;
		int loopI = 0;
		while(endIndex > -1) {
			// find each leaf block
			while(startIndex > -1 && startIndex < endIndex) {
				startIndices.add(startIndex);
				if(startIndices.size() > 1) {
					int tmpStart = startIndices.get(startIndices.size() - 2) + startStrLen;
					int tmpLength = startIndex - tmpStart;
					dst.add(tmpStart);
					dst.add(startIndex);
					int len = tmpLength;
					for(int i = 0, size = startIndices.size(); i < size; i++) {
						//startIndices.set(i, startIndices.get(i) - len);
					}
					String s1 = input.substring(tmpStart, tmpStart + tmpLength);
					blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
				}
				else {
					int tmpStart = loopI > 0 ? prevEndIndex + endStrLen : prevEndIndex;
					int tmpLength = startIndex - tmpStart;
					dst.add(tmpStart);
					dst.add(startIndex);
					for(int i = 0, size = startIndices.size(); i < size; i++) {
						//startIndices.set(i, startIndices.get(i) - tmpLength);
					}
					String s2 = input.substring(tmpStart, tmpStart + tmpLength);
					blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
				}
				startIndex = input.indexOf(startStr, startIndex + startStrLen);

				IntermediateBlock tempBlock = new IntermediateBlock(blockDepth);
				blockDepth.addSubBlock(tempBlock);
				blockDepth = tempBlock;

				level++;
			}

			if(startIndices.size() > 0) {
				int tmpStart = startIndices.get(startIndices.size() - 1) + startStrLen;
				int tmpLength = endIndex - tmpStart;
				dst.add(tmpStart);
				dst.add(endIndex);
				for(int i = 0, size = startIndices.size(); i < size; i++) {
					//startIndices.set(i, startIndices.get(i) - tmpLength);
				}
				String s3 = input.substring(tmpStart, tmpStart + tmpLength);
				blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
			}
			else {
				int tmpStart = prevEndIndex + endStrLen;
				int tmpLength = endIndex - tmpStart;
				dst.add(tmpStart);
				dst.add(endIndex);
				for(int i = 0, size = startIndices.size(); i < size; i++) {
					//startIndices.set(i, startIndices.get(i) - tmpLength);
				}
				String s4 = input.substring(tmpStart, tmpStart + tmpLength);
				blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
			}

			prevEndIndex = endIndex;
			endIndex = input.indexOf(endStr, endIndex + endStrLen);
			startIndices.clear();
			if(blockDepth.hasParentBlock()) {
				blockDepth = blockDepth.getParentBlock();
			}
			else if(endIndex != -1) {
				throw new IllegalStateException("mismatching number of opening/closing documentParser.block");
			}
			level--;
			loopI++;
		}

		int tmpStart = loopI > 0 ? prevEndIndex + startStrLen : prevEndIndex;
		int tmpLength = input.length() - tmpStart;
		for(int i = 0, size = startIndices.size(); i < size; i++) {
			//startIndices.set(i, startIndices.get(i) - tmpLength);
		}
		String s5 = input.substring(tmpStart, tmpLength);
		blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength - tmpStart));
	}


	/** Identify the documentParser.block in the {@code input} string.
	 * @param documentParser.block the destination root {@link IntermediateBlock} to store parsed
	 * {@link TextBlock TextBlocks} in
	 * @param input the string containing {@code blockStart} and {@code blockEnd} to parse
	 * @param startChar the string that identifies the start of a block
	 * @param endChar the string that identifies the end of a block
	 * @param dst the destination to store pairs of start and end block indices in.
	 * The indices are zero based character indices into the {@code input} string.
	 * A {@link TextBlock} with nested documentParser.block describing each of the documentParser.block in {@code input}
	 */
	public static final void splitIntoBlocks(IntermediateBlock blocks, StringBuilder input, int startChar, int endChar,
			IntList dst) {
		int startStrLen = 1;
		int endStrLen = 1;
		IntArrayList startIndices = new IntArrayList();
		int startIndex = StringIndex.indexOf(input, 0, startChar);
		int prevEndIndex = 0;
		int endIndex = StringIndex.indexOf(input, 0, endChar);
		IntermediateBlock blockDepth = blocks;

		int level = 0;
		int loopI = 0;
		while(endIndex > -1) {
			// find each leaf block
			while(startIndex > -1 && startIndex < endIndex) {
				startIndices.add(startIndex);
				if(startIndices.size() > 1) {
					int tmpStart = startIndices.get(startIndices.size() - 2) + startStrLen;
					int tmpLength = startIndex - tmpStart;
					dst.add(tmpStart);
					dst.add(startIndex);
					int len = startIndex - (startIndices.size() - 2) + startStrLen;
					for(int i = 0, size = startIndices.size(); i < size; i++) {
						//startIndices.set(i, startIndices.get(i) - len);
					}
					String s1 = input.substring(tmpStart, tmpStart + tmpLength);
					blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
				}
				else {
					int tmpStart = loopI > 0 ? prevEndIndex + endStrLen : prevEndIndex;
					int tmpLength = startIndex - tmpStart;
					dst.add(tmpStart);
					dst.add(startIndex);
					for(int i = 0, size = startIndices.size(); i < size; i++) {
						//startIndices.set(i, startIndices.get(i) - tmpLength);
					}
					String s2 = input.substring(tmpStart, tmpStart + tmpLength);
					blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
				}
				startIndex = StringIndex.indexOf(input, startIndex + startStrLen, startChar);

				IntermediateBlock tempBlock = new IntermediateBlock(blockDepth);
				blockDepth.addSubBlock(tempBlock);
				blockDepth = tempBlock;

				level++;
			}

			if(startIndices.size() > 0) {
				int tmpStart = startIndices.get(startIndices.size() - 1) + startStrLen;
				int tmpLength = endIndex - tmpStart;
				dst.add(tmpStart);
				dst.add(endIndex);
				for(int i = 0, size = startIndices.size(); i < size; i++) {
					//startIndices.set(i, startIndices.get(i) - tmpLength);
				}
				String s3 = input.substring(tmpStart, tmpStart + tmpLength);
				blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
			}
			else {
				int tmpStart = prevEndIndex + endStrLen;
				int tmpLength = endIndex - tmpStart;
				dst.add(tmpStart);
				dst.add(endIndex);
				for(int i = 0, size = startIndices.size(); i < size; i++) {
					//startIndices.set(i, startIndices.get(i) - tmpLength);
				}
				String s4 = input.substring(tmpStart, tmpStart + tmpLength);
				blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength));
			}

			prevEndIndex = endIndex;
			endIndex = StringIndex.indexOf(input, endIndex + endStrLen, endChar);
			startIndices.clear();
			if(blockDepth.hasParentBlock()) {
				blockDepth = blockDepth.getParentBlock();
			}
			else if(endIndex != -1) {
				throw new IllegalStateException("mismatching number of opening/closing documentParser.block");
			}
			level--;
			loopI++;
		}

		int tmpStart = loopI > 0 ? prevEndIndex + startStrLen : prevEndIndex;
		int tmpLength = input.length();
		for(int i = 0, size = startIndices.size(); i < size; i++) {
			//startIndices.set(i, startIndices.get(i) - tmpLength);
		}
		String s5 = input.substring(tmpStart, tmpLength);
		blockDepth.addSubBlock(new TextOffsetBlock(blockDepth, tmpStart, tmpLength - tmpStart));
	}

}
