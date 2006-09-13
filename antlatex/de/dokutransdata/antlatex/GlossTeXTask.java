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
package de.dokutransdata.antlatex;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

/**
 * Task for GlossTeX
 * 
 * @author jaloma
 *
 */
public class GlossTeXTask extends SimpleExternalTask {
	public static final String RCS_ID="Version @(#) $Revision: 1.5 $";

	private String auxFile;

	private List files = new ArrayList();
	private List glossarDefinitionFiles;
	private String glossTeXPath = "glosstex";

	private MakeindexTask makeindex;

	private int verboseLevel;

	public GlossTeXTask(MakeindexTask newProc) {
		glossarDefinitionFiles = new ArrayList();
		verboseLevel = -1;
		makeindex = newProc;
	}
	public GlossTeXTask() {
		glossarDefinitionFiles = new ArrayList();
		verboseLevel = -1;
		makeindex = null;
	}

	/** Callback-Methode um eingeschachtelte &lt;fileset&gt;-Elemente einfügen zu können.
	 * @param f from build.xml
	 */
	public void add(FileSet f) {
		files.add(f);
	}
	/**
	 * Callback-Methode fuer Ant
	 * @return Makeindex-Task
	 */
	public Object createMakeindex() {
		makeindex = new MakeindexTask(); //(MakeindexTask) this.getProject().createTask("index");
		log("Index is created "+makeindex);
		makeindex.setVerbose(verbose);
		return makeindex;
	}

	public void addGlossarDefinitionFile(String newValue) throws BuildException {
		if (newValue.startsWith("${")) {
			throw new BuildException("Variable "+newValue+" is not set!");
		}
		glossarDefinitionFiles.add(newValue);
	}

	public final void execute() throws BuildException {
        // get the fileset with full qualified pathname!
		if (!this.run) { return; }
        for (int i=0; i<files.size();i++ ) {
        	FileSet fs = (FileSet)files.get(i);
        	String[] fnames = fs.toString().split(";");
        	File localDir = fs.getDir(fs.getProject());
        	for (int k=0;k<fnames.length;k++) {
        		String fname = new String(localDir+
        				File.separator+
        				fnames[k]);
        		int idx;
        		//System.err.println(fname);
        		String mainFile;
        		if ((idx = fname.lastIndexOf(".aux")) != -1) {
        			mainFile = fname.substring(0, idx);
        		} else {
        			continue;
        		}
                auxFile = mainFile;
                run();
        	}
        }
	}

	public String getAuxFile() {
		return auxFile;
	}

	public List getGlossarDefinitionFiles() {
		return glossarDefinitionFiles;
	}

	public int getVerboseLevel() {
		return verboseLevel;
	}

//	This is GlossTeX, version 0.4
//	GlossTeX comes with ABSOLUTELY NO WARRANTY. (C)opyright 1996, 97 Volkan Yavuz
//	Usage: glosstex aux gdf0 [gdf1 ...] [-v[0-5]]
	public final int run() throws BuildException {
		if (!this.run) { return 0; }
		if (makeindex == null) {
			throw new BuildException("Makeindex is not defined, can not run glosstex");
		}
		List args = new ArrayList();
		args.add(auxFile);
		for (int i = 0; i < glossarDefinitionFiles.size(); i++) {
			args.add(glossarDefinitionFiles.get(i));
		}
		if (verboseLevel > -1) {
			args.add("-v" + verboseLevel);
		}
		if (this.theCommand == null) {
			this.setCommand(glossTeXPath);
		}
		invoke(this.getCommand(), args);
		//-o $(MAIN).glx -t $(MAIN).gil -s $(GLXIST)
		makeindex.antTask = this.antTask;
		args = new ArrayList();
		args.add(auxFile + ".gxs");
		makeindex.setIdxFiles(args);
		makeindex.setOutFile(auxFile+".glx");
		makeindex.setProtocolFile(auxFile+".gil");
		makeindex.setIf(this.getIf());
		makeindex.setVerbose(verbose);
		return makeindex.run();
	}

	public void setAuxFile(String newValue) throws BuildException {
		if (newValue.startsWith("${")) {
			throw new BuildException("Variable "+newValue+" is not set!");
		}
		auxFile = newValue;
	}

	public void setGlossarDefinitionFiles(List newValues) {
		glossarDefinitionFiles = newValues;
	}

	public void setGlossarDefinitionFiles(String newValues) throws BuildException {
		if (newValues.startsWith("${")) {
			throw new BuildException("Variable "+newValues+" is not set!");
		}
		String[] dataFiles = newValues.split(" ");
		for (int i =0; i<dataFiles.length; i++) {
			this.addGlossarDefinitionFile(dataFiles[i]);
		}
	}

	public void setVerboseLevel(int newValue) {
		verboseLevel = newValue;
	}

	public String toString() {
		String txt = "";
		txt += "GlossTeX";
		txt += " auxFile: "+auxFile;
		txt += " verbose Level: "+verboseLevel;
		txt += " files: "+files;
		return txt;
	}
}
