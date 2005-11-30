/**
 * 
 */
package de.dokutransdata.antlatex;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

/**
 * Simple Task for Makeindex.
 * 
 * @author jaloma
 * 
 */
public class MakeindexTask extends SimpleExternalTask {

	private List files = new ArrayList();

	private List idxFiles = new ArrayList();

	private String idxStyle;

	private String makeIndexPath = "makeindex";

	private String outFile;

	private String protocolFile;

	private boolean compressIntermediateBlanks = false;

	public void setCompressIntermediateBlanks(boolean flag) {
		compressIntermediateBlanks = flag;
	}

	private boolean germanWordOrder = false;

	public void setGermanWordOrder(boolean flag) {
		germanWordOrder = flag;
	}

	private boolean letterOrder = false;

	public void setLetterOrder(boolean flag) {
		letterOrder = flag;
	}

	private String startingPageNumber = null;

	/**
	 * @param newValue Kann ein integer, 'any', 'even', 'odd' sein.
	 */
	public void setStartingPageNumber(String newValue) {
		if (newValue.equals("any") || newValue.equals("odd")
				|| newValue.equals("even") || newValue.equals("")) {
			startingPageNumber = newValue;
		} else {
			Integer.parseInt(newValue);
			startingPageNumber = newValue;
		}
	}

	private boolean pageRangeFormation = true;

	public void setPageRangeFormation(boolean flag) {
		pageRangeFormation = flag;
	}

	/**
	 * 
	 */
	public MakeindexTask(List newIdx) {
		idxFiles = newIdx;
		outFile = null;
		protocolFile = null;
	}

	public MakeindexTask() {
		outFile = null;
		protocolFile = null;
	}

	/**
	 * Callback-Methode um eingeschachtelte &lt;fileset&gt;-Elemente einfügen zu
	 * können.
	 * 
	 * @param f
	 *            from build.xml
	 */
	public void add(FileSet f) {
		files.add(f);
	}

	/**
	 * Setzt ein Fileset-Element um nach einer Liste von IdxFiles!
	 */
	private void setup() {
		// get the fileset with full qualified pathname!
		if (files.size() != 0) { // Wenn ein Fileset-Element existiert wird
			// die interne Logik des LaTeX-Aufrufes
			// ueberschrieben!!.
			idxFiles = new ArrayList();
		}
		for (int i = 0; i < files.size(); i++) {
			FileSet fs = (FileSet) files.get(i);
			String[] fnames = fs.toString().split(";");
			File localDir = fs.getDir(fs.getProject());
			for (int k = 0; k < fnames.length; k++) {
				String fname = new String(localDir + File.separator + fnames[k]);

				idxFiles.add(fname);
			}
		}
	}

	public final void execute() throws BuildException {
		run();
	}

	public List getIdxFiles() {
		return idxFiles;
	}

	public String getIdxStyle() {
		return idxStyle;
	}

	public String getMakeIndexPath() {
		return makeIndexPath;
	}

	public String getOutFile() {
		return outFile;
	}

	public String getProtocolFile() {
		return protocolFile;
	}

	// Usage: makeindex [-ilqrcg] [-s sty] [-o ind] [-t log] [-p num] [idx0 idx1
	// ...]
	// -i = Standardeingabe
	/**
	 * Stellt aus den Attributen die Parameter fuer den Aufruf von Makeindex
	 * zusammen und ruft anschliessend das externe Programm auf.
	 * 
	 * @return Rueckgabewert des externen Programms
	 * @throws BuildException
	 */
	public final int run() throws BuildException {
		setup();
		if (idxFiles.size() == 0) {
			throw new BuildException("Makeindex: Keine Dateien angegeben!");
		}
		List args = new ArrayList();
		if (this.compressIntermediateBlanks) {
			args.add("-c");
		}
		if (this.germanWordOrder) {
			args.add("-g");
		}
		if (this.letterOrder) {
			args.add("-l");
		}
		if (!this.verbose) {
			args.add("-q");
		}
		if (!this.pageRangeFormation) {
			args.add("-r");
		}
		if (this.idxStyle != null && !this.idxStyle.equals("")) {
			args.add("-s");
			args.add(idxStyle);
		}
		if (this.outFile != null && !this.outFile.equals("")) {
			args.add("-o");
			args.add(outFile);
		}
		if (this.protocolFile != null && !this.protocolFile.equals("")) {
			args.add("-t");
			args.add(protocolFile);
		}
		if (this.startingPageNumber != null
				&& !this.startingPageNumber.equals("")) {
			args.add("-p");
			args.add(this.startingPageNumber);
		}
		for (int i = 0; i < idxFiles.size(); i++) {
			args.add(idxFiles.get(i));
		}
		if (this.theCommand == null) {
			this.setCommand(makeIndexPath);
		}
		return invoke(this.getCommand(), args);
	}

	/**
	 * @param newValue
	 *            Liste der Dateien zum Scannen.
	 */
	public void setIdxFiles(List newValue) {
		idxFiles = newValue;
	}

	/**
	 * Für den Parameter -s wird der Name der Styledatei übergeben.
	 * 
	 * @param newValue
	 *            Name der Styledatei
	 */
	public void setIdxStyle(String newValue) {
		idxStyle = newValue;
	}

	/**
	 * Für den Parameter -o wird der Name der Ausgabedatei übergeben.
	 * 
	 * @param newValue
	 *            Name der Ausgabedatei
	 */
	public void setOutFile(String newValue) {
		outFile = newValue;
	}

	/**
	 * Für den Parameter -t wird der Name der Protokolldatei übergeben.
	 * 
	 * @param newValue
	 *            Name der Datei
	 */
	public void setProtocolFile(String newValue) {
		protocolFile = newValue;
	}
}
