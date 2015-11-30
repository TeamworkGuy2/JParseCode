package codeParser.eclipseProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import twg2.io.serialize.base.DataElement;
import twg2.io.serialize.xml.XmlAttributes;
import twg2.io.serialize.xml.XmlInput;
import twg2.io.serialize.xml.XmlOutput;
import twg2.io.serialize.xml.Xmlable;
import twg2.text.stringUtils.StringCompare;

/**
 * @author TeamworkGuy2
 * @since 2015-5-25
 */
public class EclipseClasspathFile implements Xmlable {
	private static String ENTRY_KEY = "classpath";


	/**
	 * @author TeamworkGuy2
	 * @since 2015-5-25
	 */
	// package-private
	static class ClassPathEntry implements Xmlable {
		private static String ENTRY_KEY = "classpathentry";
		// package-private
		String kind;
		String path;
		String sourcePath;


		public boolean contains(String text) {
			return StringCompare.containsIgnoreCase(new String[] { kind, path, sourcePath }, text);
		}


		@Override
		public void readXML(XmlInput in) throws IOException, XMLStreamException {
			in.readStartBlock(ENTRY_KEY);
			XmlAttributes attrs = in.getCurrentElementAttributes();
			List<String> attrNames = attrs.getAttributeNames();

			int kindIdx = attrNames.indexOf("kind");
			this.kind = kindIdx > -1 ? attrs.getAttributeString(kindIdx) : null;

			int pathIdx = attrNames.indexOf("path");
			this.path = pathIdx > -1 ? attrs.getAttributeString(pathIdx) : null;

			int sourcePathIdx = attrNames.indexOf("sourcepath");
			this.sourcePath = sourcePathIdx > -1 ? attrs.getAttributeString(sourcePathIdx) : null;

			in.readEndBlock();
		}


		@Override
		public void writeXML(XmlOutput out) throws IOException, XMLStreamException {
			throw new IllegalStateException("unimplemented");
		}


		@Override
		public String toString() {
			return ENTRY_KEY + " " + kind + ": " + path + (sourcePath != null ? (", " + sourcePath) : "");
		}

	}


	@Override
	public void readXML(XmlInput in) throws IOException, XMLStreamException {
		DataElement elem = in.readStartBlock(ENTRY_KEY);
		elem = in.peekNext();
		while(elem != null && !(elem.isEndBlock() && elem.getName() == ENTRY_KEY)) {
			ClassPathEntry entry = new ClassPathEntry();
			entry.readXML(in);

			this.classPathEntries.add(entry);
			elem = in.peekNext();
		}
		//in.readEndBlock();
	}


	@Override
	public void writeXML(XmlOutput out) throws IOException, XMLStreamException {
		throw new IllegalStateException("unimplemented");
	}


	// package-private
	File file;
	List<ClassPathEntry> classPathEntries = new ArrayList<>();


	public EclipseClasspathFile(File file) {
		this.file = file;
	}


	public List<ClassPathEntry> getLibClassPathEntries() {
		return getClassPathEntries("lib");
	}


	/**
	 * @param type the type of imports to parse (i.e. 'lib', 'con', 'src', 'output')
	 * @return a list of the {@link ClassPathEntry}'s that contain the specified type
	 */
	public List<ClassPathEntry> getClassPathEntries(String type) {
		List<ClassPathEntry> cpe = new ArrayList<>();
		for(ClassPathEntry entry : classPathEntries) {
			if(entry.kind != null && (type == null || entry.kind.contains(type))) {
				cpe.add(entry);
			}
		}
		return cpe;
	}


	@Override
	public String toString() {
		StringBuilder strB = new StringBuilder();
		strB.append(file);
		strB.append(": [\n");
		for(ClassPathEntry entry : classPathEntries) {
			strB.append(entry.toString());
			strB.append('\n');
		}
		strB.append("]\n");
		return strB.toString();
	}

}
