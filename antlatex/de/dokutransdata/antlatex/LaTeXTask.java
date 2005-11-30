/**
 * 
 */
package de.dokutransdata.antlatex;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

/**
 * Simple Task for LaTeX, generates only the PDF/DVI File, without oberservation of references, index etc.
 * @author jaloma
 * 
 */
public class LaTeXTask extends SimpleExternalTask {

	private String latexfile;

	private boolean pdftex;

	public final void execute() throws BuildException {
		run();
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
