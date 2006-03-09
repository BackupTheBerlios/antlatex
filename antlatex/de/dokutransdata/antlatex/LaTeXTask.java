/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
/**
 * 
 */
package de.dokutransdata.antlatex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

	public static final String RCS_ID="Version @(#) $Revision: 1.6 $";

	private String latexfile;

	private String jobname = null;

	private File outputDir = null;

	private File auxDir = null;

	private boolean pdftex;

	private List files = new ArrayList();

	private String passThruLaTeXParameters = null;

	public final void execute() throws BuildException {
		if (files.size() == 0 && (latexfile == null || latexfile.equals(""))) {
			throw new BuildException("No files are given!");
		}
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
					setLatexfile(fname);
					try {
						run();
					} catch (IOException ie) {

					}
				}
			}

		} else {
			try {
				run();
			} catch (IOException ie) {

			}
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

	public final int run() throws BuildException, IOException {
		List args = new ArrayList();
		if (theCommand == null || theCommand.equals("")) {
			if (pdftex) {
				theCommand = "pdflatex";
			} else {
				theCommand = "latex";
			}
			Properties sysprops = System.getProperties();
			String osname = sysprops.getProperty("os.name");
			if (osname.indexOf("Windows") != -1) {
				theCommand += ".exe";
			}
		}
		String command = theCommand;
		latexfile = latexfile.replace('\\', '/');
		if (jobname != null && !jobname.equals("")) {
			args.add("-job-name=" + jobname);
		}
		if (outputDir != null && !outputDir.equals("")) {
			String oDir = outputDir.getCanonicalPath();
			oDir = oDir.replace('\\', '/');
			args.add("-output-directory=" + oDir);
		}

		if (auxDir != null && !auxDir.equals("")) {
			String oDir = auxDir.getCanonicalPath();
			oDir = oDir.replace('\\', '/');
			args.add("-aux-directory=" + oDir);
		}
		args.add("-interaction=nonstopmode");

		if (passThruLaTeXParameters != null && !passThruLaTeXParameters.equals("")) {
			String[] passThru = passThruLaTeXParameters.split(";");
			for (int i = 0; i < passThru.length; i++) {
				String newArg = passThru[i];
				if (newArg.indexOf("output-directory") != -1
						|| newArg.indexOf("aux-directory") != -1
						|| newArg.indexOf("interaction") != -1
						|| newArg.indexOf("job-name") != -1) {
					continue;
				}
				args.add(newArg);
			}
		}

		if (!verbose) {
			args.add("-quiet");
		}

		args.add(latexfile);
		return invoke(command, args);
	}

	public void setLatexfile(String newValue) {
		latexfile = newValue;
	}

	public void setJobname(String newValue) {
		jobname = newValue;
	}

	public void setOutputdir(String newValue) {
		outputDir = new File(newValue);
	}

	public void setOutputdir(File newValue) {
		outputDir = newValue;
	}

	public void setPdftex(boolean newValue) {
		pdftex = newValue;
	}

	/**
	 * @return Returns the auxDir.
	 */
	public final File getAuxDir() {
		return auxDir;
	}

	/**
	 * @param auxDir
	 *            The auxDir to set.
	 */
	public final void setAuxDir(File auxDir) {
		this.auxDir = auxDir;
	}

	/**
	 * @return Returns the passThruLaTeXParameters.
	 */
	public final String getPassThruLaTeXParameters() {
		return passThruLaTeXParameters;
	}

	/**
	 * @param passThruLaTeXParameters
	 *            The passThruLaTeXParameters to set.
	 */
	public final void setPassThruLaTeXParameters(String passThruLaTeXParameters) {
		this.passThruLaTeXParameters = passThruLaTeXParameters;
	}
}
