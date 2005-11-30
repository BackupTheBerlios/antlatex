/**
 * 
 */
package de.dokutransdata.antlatex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

/**
 * Task for BibTeX
 * @author jaloma
 */
public class BibTeXTask extends SimpleExternalTask {

	private String auxFile;

	private String bibtexPath = "bibtex";

	private List files = new ArrayList();

	private int minCrossrefs = -1;

	private boolean terse=false;

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

	public final void execute() throws BuildException {
		// get the fileset with full qualified pathname!
		for (int i = 0; i < files.size(); i++) {
			FileSet fs = (FileSet) files.get(i);
			String[] fnames = fs.toString().split(";");
			File localDir = fs.getDir(fs.getProject());
			for (int k = 0; k < fnames.length; k++) {
				if (fnames[k] == null) {
					continue;
				}
				String fname = new String(localDir + File.separator + fnames[k]);
				auxFile = fname;
				run();
			}
		}
	}

	public String getAuxFile() {
		return auxFile;
	}

	public int getMinCrossrefs() {
		return minCrossrefs;
	}

	public boolean getTerse() {
		return terse;
	}

	// Usage: bibtex [OPTION]... AUXFILE[.aux]
	// Write bibliography for entries in AUXFILE to AUXFILE.bbl.
	//
	// -min-crossrefs=NUMBER include item after NUMBER cross-refs; default 2
	// -terse do not print progress reports
	// -help display this help and exit
	// -version output version information and exit
	//
	// Email bug reports to tex-k@mail.tug.org.
	public final int run() throws BuildException {
		List args = new ArrayList();

		if (this.minCrossrefs > -1) {
			args.add("-min-crossrefs=" + this.minCrossrefs);
		}
		if (this.terse || !this.verbose) {
			args.add("-terse");
		}
		args.add(auxFile);

		if (this.theCommand == null) {
			this.setCommand(this.bibtexPath);
		}
		return invoke(theCommand, args);
	}

	public void setAuxFile(String newValue) {
		auxFile = newValue;
	}

	public void setMinCrossrefs(int newValue) {
		minCrossrefs = newValue;
	}

	public void setTerse(boolean newValue) {
		terse = newValue;
	}
}
