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

	public void addGlossarDefinitionFile(String newValue) {
		glossarDefinitionFiles.add(newValue);
	}

	public final void execute() throws BuildException {
        // get the fileset with full qualified pathname!
        for (int i=0; i<files.size();i++ ) {
        	FileSet fs = (FileSet)files.get(i);
        	String[] fnames = fs.toString().split(";");
        	File localDir = fs.getDir(fs.getProject());
        	for (int k=0;k<fnames.length;k++) {
        		String fname = new String(localDir+
        				File.separator+
        				fnames[k]);
        		int idx;
        		System.err.println(fname);
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

	public void setAuxFile(String newValue) {
		auxFile = newValue;
	}

	public void setGlossarDefinitionFiles(List newValues) {
		glossarDefinitionFiles = newValues;
	}

	public void setGlossarDefinitionFiles(String newValues) {
		String[] dataFiles = newValues.split(" ");
		for (int i =0; i<dataFiles.length; i++) {
			this.addGlossarDefinitionFile(dataFiles[i]);
		}
	}

	public void setVerboseLevel(int newValue) {
		verboseLevel = newValue;
	}

}
