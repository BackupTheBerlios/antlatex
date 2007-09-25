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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

/**
 * Task for BibTeX
 * 
 * @author jaloma
 */
public class BibTeXTask extends SimpleExternalTask {
	
	public static final String RCS_ID = "Version @(#) $Revision: 1.6 $";

	private String auxFile;
	private AuxFiles auxFiles;
	public void addAuxFiles (AuxFiles fSet) {
		auxFiles = fSet;
		//System.out.println(fSet); 
	}
	public AuxFiles getAuxFiles() {
		return auxFiles;
	}
	private String bibtexPath = "bibtex";

	private ArrayList files = new ArrayList();

	private int minCrossrefs = -1;

	private boolean terse = false;

	private boolean inLoop = false;

//	BibTeXTask() {
//		super();
//	}
	public void init() {
		super.init();
		auxFiles = new AuxFiles();
		auxFiles.setDir(this.workingDir);
	}
	
	public String toString() {
		String txt = "";
		txt += "BibTeX";
		txt += " auxFile: " + auxFile;
		txt += " minCrossrefs: " + minCrossrefs;
		txt += " terse: " + terse;
		txt += " inLoop: " + inLoop;
		txt += " files: " + files;
		txt += " auxFiles: " + auxFiles;
		return txt;
	}

	/**
	 * @return Returns the inLoop.
	 */
	public final boolean isInLoop() {
		return this.inLoop;
	}

	/**
	 * @param inLoop
	 *            The inLoop to set.
	 */
	public final void setInLoop(boolean inLoop) {
		this.inLoop = inLoop;
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

	public void add(AuxFiles f) {
		auxFiles = f;
	}
	public List getFiles() {
		return files;
	}

	public final void execute() throws BuildException {
		// get the fileset with full qualified pathname!
		if (!this.run) {
			return;
		}
		if (files.isEmpty()) {
			files.add(auxFiles);
		}
		for (int i = 0; i < files.size(); i++) {
			FileSet fs = (FileSet) files.get(i);
			String[] fnames = fs.toString().split(";");
			File localDir = fs.getDir(fs.getProject());
			for (int k = 0; k < fnames.length; k++) {
				if (fnames[k] == null) {
					continue;
				}
				String fname = new String(localDir + File.separator + fnames[k]);
				File f = new File(fname);
				String mFile = fname;
				try {
					mFile = f.getCanonicalPath();
				} catch (IOException e) {
					continue;
				}
				int idx;
				if ((idx = fname.lastIndexOf(".aux")) != -1) {
					mFile = fname.substring(0, idx);
				} else {
					continue;
				}
				auxFile = mFile;
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

	/**
	 * Usage: bibtex [OPTION]... AUXFILE[.aux] Write bibliography for entries in
	 * AUXFILE to AUXFILE.bbl.
	 * 
	 * -min-crossrefs=NUMBER include item after NUMBER cross-refs; default 2
	 * -terse do not print progress reports
	 * -help display this help and exit
	 * -version output version information and exit
	 * 
	 * Email bug reports to tex-k@mail.tug.org.
	 * 
	 * @return Rückgabewert des Aufrufs von BibTeX
	 * @throws BuildException
	 */
	public final int run() throws BuildException {
		if (!this.run) {
			return 0;
		}
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

	public void setAuxFile(String newValue) throws BuildException {
		if (newValue.startsWith("${")) {
			throw new BuildException("Variable " + newValue + " is not set!");
		}
		auxFile = newValue;
	}

	public void setMinCrossrefs(int newValue) {
		minCrossrefs = newValue;
	}

	public void setTerse(boolean newValue) {
		terse = newValue;
	}
}
