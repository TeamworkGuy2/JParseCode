package twg2.parser.output;

import java.io.IOException;
import java.io.UncheckedIOException;

import twg2.functions.IoFunc;

/**
 * @author TeamworkGuy2
 * @since 2015-12-13
 */
public class JsonWrite {


	// ==== Consumer ====
	public static final <T extends Object> void joinStr(Iterable<T> objs, String delimiter, StringBuilder dst, IoFunc.FunctionIo<T, String> toString) {
		try {
			joinStr(objs, delimiter, (Appendable)dst, toString);
		} catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}


	public static final <T extends Object> void joinStr(Iterable<T> objs, String delimiter, Appendable dst, IoFunc.FunctionIo<T, String> toString) throws IOException {
		boolean firstLoop = true;
		for(T obj : objs) {
			if(!firstLoop) {
				dst.append(delimiter);
			}
			if(obj != null) {
				dst.append(toString.apply(obj));
			}
			else {
				dst.append("null");
			}
			firstLoop = false;
		}
	}


	public static final <T extends Object> void joinStrConsume(Iterable<T> objs, String delimiter, StringBuilder dst, IoFunc.ConsumerIo<T> toString) {
		try {
			joinStrConsume(objs, delimiter, (Appendable)dst, toString);
		} catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}


	public static final <T extends Object> void joinStrConsume(Iterable<T> objs, String delimiter, Appendable dst, IoFunc.ConsumerIo<T> toString) throws IOException {
		boolean firstLoop = true;
		for(T obj : objs) {
			if(!firstLoop) {
				dst.append(delimiter);
			}
			if(obj != null) {
				toString.accept(obj);
			}
			else {
				dst.append("null");
			}
			firstLoop = false;
		}
	}

}
