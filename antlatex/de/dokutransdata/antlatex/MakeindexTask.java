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
	public static final String RCS_ID="Version @(#) $Revision: 1.5 $";

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
	public void setStartingPageNumber(String newValue) throws BuildException {
		if (newValue.startsWith("${")) {
			throw new BuildException("Variable "+newValue+" is not set!");
		}
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
	
	private boolean inLoop = true;

	/**
	 * @return Returns the inLoop.
	 */
	public final boolean isInLoop() {
		return this.inLoop;
	}

	/**
	 * @param inLoop The inLoop to set.
	 */
	public final void setInLoop(boolean inLoop) {
		this.inLoop = inLoop;
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
	 * @return Returns the files.
	 */
	public final List getFiles() {
		return this.files;
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
		if (!this.run) { return;}
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
	public void setIdxStyle(String newValue) throws BuildException {
		if (newValue.startsWith("${")) {
			throw new BuildException("Variable "+newValue+" is not set!");
		}
		idxStyle = newValue;
	}

	/**
	 * Für den Parameter -o wird der Name der Ausgabedatei übergeben.
	 * 
	 * @param newValue
	 *            Name der Ausgabedatei
	 */
	public void setOutFile(String newValue) throws BuildException {
		if (newValue.startsWith("${")) {
			throw new BuildException("Variable "+newValue+" is not set!");
		}
		outFile = newValue;
	}

	/**
	 * Für den Parameter -t wird der Name der Protokolldatei übergeben.
	 * 
	 * @param newValue
	 *            Name der Datei
	 */
	public void setProtocolFile(String newValue) throws BuildException {
		if (newValue.startsWith("${")) {
			throw new BuildException("Variable "+newValue+" is not set!");
		}
		protocolFile = newValue;
	}
	
	public String toString() {
		String txt = "";
		txt += "makeindex";
		txt += " compress blanks: "+compressIntermediateBlanks;
		txt += " starting Page: "+ startingPageNumber;
		txt += " page Range: "+pageRangeFormation;
		txt += " letter Order: "+letterOrder;
		txt += " german: "+germanWordOrder;
		txt += " inLoop: "+inLoop;
		txt += " files: "+files;
		return txt;
	}
}
