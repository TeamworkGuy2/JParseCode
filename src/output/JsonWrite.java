package output;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author TeamworkGuy2
 * @since 2015-12-13
 */
public class JsonWrite {

	@FunctionalInterface
	public static interface ConsumerIo<T> {
		public void accept(T t) throws IOException;
	}




	@FunctionalInterface
	public static interface BiConsumerIo<T, U> {
		public void accept(T t, U u) throws IOException;
	}




	// ==== Consumer ====
	public static final <T extends Object> void joinStrConsumer(Iterable<T> objs, String delimiter, StringBuilder dst, ConsumerIo<T> toString) throws IOException {
		try {
			joinStrConsumer(objs, delimiter, (Appendable)dst, toString);
		} catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}


	public static final <T extends Object> void joinStrConsumer(Iterable<T> objs, String delimiter, Appendable dst, ConsumerIo<T> toString) throws IOException {
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


	public static final <T extends Object> void joinStrConsumer(Iterable<T> objs, String delimiter, StringBuilder dst, BiConsumerIo<T, Appendable> toString) throws IOException {
		try {
			joinStrConsumer(objs, delimiter, (Appendable)dst, toString);
		} catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}


	public static final <T extends Object> void joinStrConsumer(Iterable<T> objs, String delimiter, Appendable dst, BiConsumerIo<T, Appendable> toString) throws IOException {
		boolean firstLoop = true;
		for(T obj : objs) {
			if(!firstLoop) {
				dst.append(delimiter);
			}
			if(obj != null) {
				toString.accept(obj, dst);
			}
			else {
				dst.append("null");
			}
			firstLoop = false;
		}
	}

}
