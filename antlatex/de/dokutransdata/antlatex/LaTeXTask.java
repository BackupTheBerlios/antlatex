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
 * Simple Task for LaTeX, generates only the PDF/DVI File, without oberservation
 * of references, index etc.
 * 
 * @author jaloma
 * 
 */
public class LaTeXTask extends SimpleExternalTask {

	private String latexfile;

	private boolean pdftex;

	private List files = new ArrayList();

	public final void execute() throws BuildException {
		if (files.size() > 0) {
			// get the fileset with full qualified pathname!
			for (int i = 0; i < files.size(); i++) {
				FileSet fs = (FileSet) files.get(i);
				String[] fnames = fs.toString().split(";");
				File localDir = fs.getDir(fs.getProject());
				for (int k = 0; k < fnames.length; k++) {
					if (fnames[k] == null) {
						continue;
					}
					String fname = new String(localDir + File.separator
							+ fnames[k]);
					latexfile = fname;
					run();
				}
			}

		} else {
			run();
		}
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

	public String getLatexfile() {
		return latexfile;
	}

	public boolean getPdftex() {
		return pdftex;
	}

	public final int run() throws BuildException {
		List args = new ArrayList();
		if (pdftex) {
			theCommand = "pdflatex";
		} else {
			theCommand = "latex";
		}
		latexfile = latexfile.replace("\\","/");
		args.add("\\nonstopmode\\input{" + latexfile + "}");
		if (!verbose) {
			args.add(">/dev/null 2>/dev/null");
		}
		return invoke(theCommand, args);
	}

	public void setLatexfile(String newValue) {
		latexfile = newValue;
	}

	public void setPdftex(boolean newValue) {
		pdftex = newValue;
	}
}
