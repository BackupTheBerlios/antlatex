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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;

import de.dokutransdata.glossar.tools.anttasks.*;

/**
 * Complex Task to generate DVI/PDF Files with (PDF)LaTeX.
 * 
 * @author jaloma
 * 
 */
public class LaTeX extends SimpleExternalTask {
	public static final String RCS_ID = "Version @(#) $Revision: 1.6 $";

	static final String VERSION = "0.0.7";

	static final String BUILD = "1";

	/**
	 * BibTeX-Task
	 */
	private BibTeXTask bibtex;

	/**
	 * Multi-BibTeX-Schalter
	 */
	private boolean multibib = false;

	/**
	 * figbib-Schalter
	 */
	private boolean figbib = false;

	/**
	 * gloss-Schalter (Glossar mit Hilfe von BibTeX)
	 */
	private boolean glossbib = false;

	/**
	 * Schalter zum Aufraeumen.
	 */
	private boolean clean;

	/**
	 * Task zum Loeschen von Dateien.
	 */
	private Delete tempFiles = null;

	/**
	 * Task fuer Makeindex
	 */
	private MakeindexTask makeindex;

	/**
	 * Mir bekannte Erweiterungen fuer temporaere Dateien.
	 */
	private final String deletePatterns[] = { "*.aux", "*.log", "*.toc",
			"*.lof", "*.lot", "*.bbl", "*.blg", "*.out", "*.ilg", "*.gil",
			"*.gxs", "*.gxg", "*.glx", "*.glg", "*.gil", "*.gls", "*.glo",
			"*.ind", "*.idx", "*.lor", "*.los", "*.tmp", "*.lg", "*.4tc",
			"*.xal", "*.xgl", "*.4ct", "*.tpt", "*.xref", "*.idv", "WARNING*",
			"*.lol" };

	/**
	 * Task fuer GlossTeX
	 */
	private GlossTeXTask glosstex;

	private GlossTeX jxGlosstex;

	private List files = new ArrayList();

	private List bibtexs = new ArrayList();

	public BibTeXTask createBibtex() {
		BibTeXTask msg = new BibTeXTask();
		bibtexs.add(msg);
		return msg;
	}
	
	private List makeindexs = new ArrayList();
	
	public MakeindexTask createMakeindex() {
		MakeindexTask msg = new MakeindexTask();
		makeindexs.add(msg);
		return msg;
	}
	

	/**
	 * Hauptdokument mit Erweiterung (tex|ltx)
	 */
	private String latexfile;

	/**
	 * Name der Protokolldatei des LaTeX-Laufes
	 */
	private String logfile;

	/**
	 * Hauptdokument <b>ohne</b> Erweiterung
	 */
	private String mainFile;

	/**
	 * Jobname
	 */
	private String jobname;

	/**
	 * Ausgabeverzeichnis
	 */
	private File outputDir;

	/**
	 * Verzeichnis der temporären Dateien
	 */
	private File auxDir;

	/**
	 * PDFLaTeX-Schalter
	 */
	private boolean pdftex;

	private String reRunPattern = "(Rerun (LaTeX|to get cross-references right)|Package glosstex Warning: Term |There were undefined references|Package natbib Warning: Citation\\(s\\) may have changed)";

	private String passThruLaTeXParameters = null;

	/**
	 * Initialisierung (verbose = false, pdftex = false, clean = false)
	 */
	public LaTeX() {
		super();
		latexfile = null;
		jobname = null;
		logfile = null;
		mainFile = null;
		outputDir = null;
		auxDir = null;
		verbose = false;
		pdftex = false;
		clean = false;

		makeindex = null;
		bibtex = null;
		glosstex = null;
		jxGlosstex = null;
		tempFiles = null;

		figbib = false;
		glossbib = false;
		multibib = false;

		bibtexs = new ArrayList();
	}

	private Delete createDefaultDelete() {
		Delete delete1 = null;
		if (verbose) {
			log("Delete is not set: Use default-values!");
		}
		delete1 = (Delete) this.getProject().createTask("delete");
		if (delete1 == null) {
			log("Can not create delete Task...");
			return null;
		}
		FileSet fileset = new FileSet();
		if (auxDir != null) {
			fileset.setDir(auxDir);
		} else if (outputDir != null) {
			fileset.setDir(outputDir);
		} else {
			fileset.setDir(workingDir);
		}
		for (int i = 0; i < deletePatterns.length; i++) {
			fileset.createInclude().setName(deletePatterns[i]);
		}

		delete1.addFileset(fileset);
		delete1.setVerbose(verbose);
		return delete1;
	}

	/**
	 * Callback-Methode fuer Ant
	 * 
	 * @return Delete-Task
	 */
	public Object createDeleteTempFiles() {
		if (verbose) {
			log("Delete is created");
		}
		tempFiles = (Delete) this.getProject().createTask("Delete");
		tempFiles.setVerbose(verbose);
		return tempFiles;
	}

	/**
	 * Callback-Methode fuer Ant
	 * 
	 * @return Makeindex-Task
	 */
//	public Object createMakeindex() {
//		makeindex = new MakeindexTask();
//		if (verbose) {
//			log("Index is created " + makeindex);
//		}
//		return makeindex;
//	}

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
	 * Callback-Methode fuer Ant
	 * 
	 * @return BibTeX-Task
	 */
	// public Object createBibtex() {
	// bibtex = new BibTeXTask();
	// if (verbose) {
	// log("BibTex is created " + bibtex);
	// }
	// return bibtex;
	// }
	/**
	 * Callback-Methode fuer Ant
	 * 
	 * @return GlossTeX-Task
	 */
	public Object createGlosstex() {
		glosstex = new GlossTeXTask();
		if (verbose) {
			log("GlossTex is created " + glosstex);
		}
		return glosstex;
	}

	/**
	 * Callback-Methode fuer Ant
	 * 
	 * @return jxGlossTeX-Task
	 */
	public Object createJxGlosstex() {
		jxGlosstex = new GlossTeX();
		if (verbose) {
			log("jxGlossTeX is created " + jxGlosstex);
		}
		return jxGlosstex;
	}

	/**
	 * Ausgabe der initialisierten Attribute
	 */
	private void dump() {
		log("ant_latex " + VERSION + " build " + BUILD);
		log("latexfile    = " + latexfile);
		log("logfile      = " + logfile);
		log("jobname      = " + jobname);
		log("mainFile     = " + mainFile);
		log("workingdir   = " + workingDir);
		log("outputdir    = " + outputDir);
		log("auxdir       = " + auxDir);
		log("verbose      = " + verbose);
		log("multibib     = " + multibib);
		log("figbib       = " + figbib);
		log("glossbib     = " + glossbib);
		log("pdftex       = " + pdftex);
		log("clean        = " + clean);
		log("tempFiles    = " + tempFiles);
		log("bibtex       = " + bibtex);
		log("bibtexs      = " + bibtexs);
		log("makeindex    = " + makeindex);
		log("makeindexs   = " + makeindexs);
		log("glosstex     = " + glosstex);
		log("jxglosstex   = " + jxGlosstex);
		log("others       = " + passThruLaTeXParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public final void execute() throws BuildException {
		if (files.size() == 0 && (latexfile == null || latexfile.equals(""))) {
			throw new BuildException("No files are given!");
		}
		if (tempFiles == null) {
			tempFiles = createDefaultDelete();
		}
		if (files.size() > 0) {
			// get the fileset with full qualified pathname!
			for (int i = 0; i < files.size(); i++) {
				FileSet fs = (FileSet) files.get(i);
				String[] fnames = fs.toString().split(";");
				boolean done = false;
				// File localDir = fs.getDir(fs.getProject());
				for (int k = 0; k < fnames.length; k++) {
					if (fnames[k] == null || fnames[k].equals("")) {
						continue;
					}
					// String fname = new String(localDir + File.separator
					// + fnames[k]);
					setLatexfile(fnames[k]);
					try {
						run();
						done = true;
					} catch (IOException ie) {
					}
				}
				if (!done && verbose) {
					log("No files found!");
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
	 * Eigentliche Verarbeitung der TeX/LaTeX-Datei. Aufruf von LaTeX, BibTeX,
	 * MakeIndex und GlossTeX (je nach Bedarf).
	 * 
	 * @throws BuildException
	 */
	public void run() throws BuildException, IOException {
		if (verbose) {
			log("Running LaTeX now!");
			dump();
		}
		runLaTeX();
		/* Sammeln bzw. ausfuehren von BibTeX-Tasks */ 
		List inLoopBibTeX = new ArrayList();
		for (Iterator it = bibtexs.iterator(); it.hasNext();) { // 4
			BibTeXTask bibTask = (BibTeXTask) it.next();
			/* Der BibTeX-Aufruf muss in der Schleife aufgerufen werden, da evtl. Seitenreferenzen benutzt werden. */
			if (bibTask.isInLoop()) {
				inLoopBibTeX.add(bibTask);
				continue;
			}
			/* Ist kein Fileset-Element vorhanden wird der normale BibTeX-Aufruf benutzt. */
			if (bibTask.getFiles().isEmpty()) {
				bibtex = bibTask;
				runBibTeX();
				bibtex = null;
			} else {
				bibTask.execute();
			}
		}
		
		/* Sammeln bzw. ausfuehren von Makeindex-Tasks */ 
		List inLoopMakeindex = new ArrayList();
		for (Iterator it = makeindexs.iterator(); it.hasNext();) { // 4
			MakeindexTask makeindexTask = (MakeindexTask) it.next();
			/* Der Makeindex-Aufruf muss in der Schleife aufgerufen werden, da evtl. Seitenreferenzen benutzt werden. */
			if (makeindexTask.isInLoop()) {
				inLoopMakeindex.add(makeindexTask);
				continue;
			}
			/* Ist kein Fileset-Element vorhanden wird der normale Makindex-Aufruf benutzt. */
			if (makeindexTask.getFiles().isEmpty()) {
				makeindex = makeindexTask;
				runMakeIndex();
				makeindex = null;
			} else {
				makeindexTask.execute();
			}
		}

//		if (bibtex != null) {
//			runBibTeX();
//		}
		if (figbib) {
			runFigBib();
		}
		if (glossbib) {
			runGlossBib();
		}
		if (jxGlosstex != null) {
			runJxGlossTeX();
		}
		int rc = 0;
		int res = 1;
		while (rc < 5 && res == 1) {
			if (glosstex != null) {
				runGlossTeX();
			}
//			if (makeindex != null) {
//				runMakeIndex();
//			}
			for (Iterator it = inLoopBibTeX.iterator(); it.hasNext();) {
				BibTeXTask bibTask = (BibTeXTask) it.next();
				if (bibTask.getFiles().isEmpty()) {
					bibtex = bibTask;
					runBibTeX();
				} else {
					bibTask.execute();
				}
			}
			for (Iterator it = inLoopMakeindex.iterator(); it.hasNext();) { // 4
				MakeindexTask makeindexTask = (MakeindexTask) it.next();
				/* Ist kein Fileset-Element vorhanden wird der normale Makindex-Aufruf benutzt. */
				if (makeindexTask.getFiles().isEmpty()) {
					makeindex = makeindexTask;
					runMakeIndex();
					makeindex = null;
				} else {
					makeindexTask.execute();
				}
			}
			
			runLaTeX();
			if (verbose) {
				log("Check references!");
			}
			res = runGrep();
			rc++;
		}
		if (rc >= 5 && res == 1) {
			log("Cannot resolve references!");
		}
		if (clean) {
			if (verbose) {
				log("run Delete");
			}
			tempFiles.execute();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#init()
	 */
	public void init() {
		workingDir = this.getProject().getBaseDir();
		// tempFiles = createDefaultDelete();
	}

	/**
	 * Aufruf von BibTeX mit den 'BibTeX'-figbib-Dateien.
	 * @deprecated
	 * @return Erfolgsmeldung von BibTeX
	 * @throws BuildException
	 */
	private final int runFigBib() throws BuildException {
		BibTeXTask figBibtex = new BibTeXTask();
		figBibtex.setVerbose(verbose);
		figBibtex.antTask = this;
		String mFile = mainFile + ".figbib";
		String figBibFile = mFile;
		if (auxDir != null || outputDir != null) {
			File f;
			if (auxDir != null) {
				f = new File(auxDir, figBibFile + ".aux");
			} else {
				f = new File(outputDir, figBibFile + ".aux");
			}
			try {
				mFile = f.getCanonicalPath();
				int idx = mFile.lastIndexOf(".aux");
				mFile = mFile.substring(0, idx);
			} catch (IOException ie) {
				log(mFile + ie.getLocalizedMessage());
				return 0;
			}
		}
		figBibtex.setAuxFile(mFile);
		return figBibtex.run();
	}

	/**
	 * Aufruf von BibTeX mit den 'BibTeX'-gloss-Dateien.
	 * @deprecated
	 * @return Erfolgsmeldung von BibTeX
	 * @throws BuildException
	 */
	private final int runGlossBib() throws BuildException {
		BibTeXTask glossBibtex = new BibTeXTask();
		glossBibtex.setVerbose(verbose);
		glossBibtex.antTask = this;
		String mFile = mainFile + ".gls";
		String figBibFile = mFile;
		if (auxDir != null || outputDir != null) {
			File f;
			if (auxDir != null) {
				f = new File(auxDir, figBibFile + ".aux");
			} else {
				f = new File(outputDir, figBibFile + ".aux");
			}
			try {
				mFile = f.getCanonicalPath();
				int idx = mFile.lastIndexOf(".aux");
				mFile = mFile.substring(0, idx);
			} catch (IOException ie) {
				log(mFile + ie.getLocalizedMessage());
				return 0;
			}
		}
		glossBibtex.setAuxFile(mFile);
		return glossBibtex.run();
	}

	/**
	 * Aufruf des BibTeX-Tasks, verbose-Option des Haupttasks wird uebernommen.
	 * 
	 * @return Wert <> 0 fuer Misserfolg
	 * 
	 * @throws BuildException
	 */
	private final int runBibTeX() throws BuildException {
		bibtex.setVerbose(verbose);
		bibtex.antTask = this;

		if (multibib) {
			FileSet fileset = new FileSet();
			if (auxDir != null) {
				fileset.setDir(auxDir);
			} else if (outputDir != null) {
				fileset.setDir(outputDir);
			} else {
				fileset.setDir(workingDir);
			}
			fileset.createInclude().setName("*.aux");

			bibtex.add(fileset);
			bibtex.execute();
			return 0;
		} else {
			String mFile = mainFile;
			if (auxDir != null || outputDir != null) {
				File f;
				if (auxDir != null) {
					f = new File(auxDir, mainFile + ".aux");
				} else {
					f = new File(outputDir, mainFile + ".aux");
				}
				try {
					mFile = f.getCanonicalPath();
					int idx = mFile.lastIndexOf(".aux");
					mFile = mFile.substring(0, idx);
				} catch (IOException ie) {
					log(mFile + ie.getLocalizedMessage());
					return 0;
				}
			}
			bibtex.setAuxFile(mFile);
			return bibtex.run();
		}
	}

	/**
	 * Aufruf des GlossTeX-Task, verbose-Option des Haupttasks wird uebernommen.
	 * 
	 * @return Wert <> 0 fuer Misserfolg
	 * 
	 * @throws BuildException
	 */
	private final int runGlossTeX() throws BuildException {
		// glosstex $(MAIN) $(GLOSSSOURCE) ;
		// $(MAKEINDEX) $(MAIN).gxs -o
		// $(MAIN).glx -t $(MAIN).gil -s $(GLXIST); fi

		if (auxDir != null) {
			glosstex.setWorkingDir(auxDir);
		} else if (outputDir != null) {
			glosstex.setWorkingDir(outputDir);
		} else {
			glosstex.setWorkingDir(workingDir);
		}
		glosstex.setAuxFile(mainFile);
		glosstex.setVerbose(verbose);
		glosstex.antTask = this;

		return glosstex.run();
	}

	/**
	 * Aufruf des jxGlossTeX-Task, verbose-Option des Haupttasks wird
	 * uebernommen.
	 * 
	 * @return 0 fuer Erfolg
	 * @throws BuildException
	 */
	private final int runJxGlossTeX() throws BuildException {
		File mFile = new File(mainFile + ".aux");
		if (auxDir != null) {
			mFile = new File(auxDir, mainFile + ".aux");
		} else if (outputDir != null) {
			mFile = new File(outputDir, mainFile + ".aux");
		} else if (workingDir != null) {
			mFile = new File(workingDir, mainFile + ".aux");
			jxGlosstex.setWorkingDir(workingDir);
		}
		jxGlosstex.setAuxFile(mFile);
		jxGlosstex.setVerbose(verbose);
		// jxGlosstex.setQuiet(!verbose);
		jxGlosstex.antTask = this;

		jxGlosstex.execute();
		return 0;
	}

	/**
	 * Sucht das Pattern in der Protokolldatei
	 * 
	 * @return 0 fuer gefunden, <>0 nicht gefunden.
	 * 
	 */
	private final int runGrep() {
		if (verbose) {
			log("Check " + reRunPattern);
		}
		String lfile = logfile;
		if (auxDir != null || outputDir != null) {
			File f;
			if (auxDir != null) {
				f = new File(auxDir, logfile);
			} else {
				f = new File(outputDir, logfile);
			}
			try {
				lfile = f.getCanonicalPath();
			} catch (IOException ie) {
				log(ie.getMessage());
				return 0;
			}
		}
		String args[] = { reRunPattern, lfile };
		int res = 0;
		try {
			res = Grep1.doit(args, verbose);
		} catch (Exception e) {

		}
		return res;
	}

	/**
	 * Aufruf des LaTeX-Task.
	 * 
	 * @return Wert <> 0 fuer Misserfolg
	 * @throws BuildException
	 */
	private final int runLaTeX() throws BuildException, IOException {
		LaTeXTask lp = new LaTeXTask();
		lp.antTask = this;
		lp.setLatexfile(latexfile);
		if (jobname != null && !jobname.equals("")) {
			lp.setJobname(jobname);
		}
		if (auxDir != null && !auxDir.equals("")) {
			lp.setAuxDir(auxDir);
		}
		if (outputDir != null && !outputDir.equals("")) {
			lp.setOutputdir(outputDir);
		}
		if (passThruLaTeXParameters != null
				&& !passThruLaTeXParameters.equals("")) {
			lp.setPassThruLaTeXParameters(passThruLaTeXParameters);
		}
		if (thePath != null && !thePath.equals("")) {
			lp.setPath(thePath);
		}
		lp.setWorkingDir(workingDir);
		lp.setPdftex(pdftex);
		lp.setVerbose(verbose);

		return lp.run();
	}

	/**
	 * Aufruf des MakeIndex-Task, verbose-Option wird vom Haupttask uebernommen.
	 * 
	 * @return Wert <> 0 fuer Misserfolg
	 * @throws BuildException
	 */
	private final int runMakeIndex() throws BuildException, IOException {
		List args = new ArrayList();
		String mFile = mainFile;
		if (auxDir != null || outputDir != null) {
			File f;
			if (auxDir != null) {
				f = new File(auxDir, mainFile + ".aux");
			} else {
				f = new File(outputDir, mainFile + ".aux");
			}
			mFile = f.getCanonicalPath();
			int idx = mFile.lastIndexOf(".aux");
			mFile = mFile.substring(0, idx);
		}
		args.add(mFile);
		makeindex.setIdxFiles(args);
		makeindex.antTask = this;
		makeindex.setVerbose(verbose);

		return makeindex.run();
	}

	/**
	 * @return Returns the reRunPattern.
	 */
	public final String getReRunPattern() {
		return reRunPattern;
	}

	/**
	 * @param reRunPattern
	 *            The reRunPattern to set.
	 */
	public final void setReRunPattern(String reRunPattern) {
		this.reRunPattern = reRunPattern;
	}

	/**
	 * Schaltet die Ausfuehrung von MultiBibTeX-Verarbeitung ein oder aus. Hier
	 * wird mit einem Filterset die *.aux verarbeitet.
	 * 
	 * @param flag
	 */
	public void setMultibib(boolean flag) {
		multibib = flag;
	}

	/**
	 * Schaltet das Loeschen der Temporaeren Dateien ein oder aus.
	 * 
	 * @param flag
	 */
	public void setClean(boolean flag) {
		clean = flag;
	}

	/**
	 * Hauptdokument zur Verarbeitung. Es koennen *.tex oder *.ltx Dateien
	 * verarbeitet werden.
	 * 
	 * @param s
	 *            Dateiname
	 * @throws BuildException
	 *             Wenn die Endung nicht *.ltx oder *.tex ist.
	 */
	public void setLatexfile(String s) throws BuildException {
		latexfile = s;
		if (jobname == null || jobname.equals("")) {
			int idx;
			File f = new File(latexfile);
			mainFile = f.getName();
			if ((idx = mainFile.lastIndexOf(".tex")) != -1) {
				mainFile = mainFile.substring(0, idx);
			} else if ((idx = mainFile.lastIndexOf(".ltx")) != -1) {
				mainFile = mainFile.substring(0, idx);
			} else {
				throw new BuildException("Unknown LaTeX-Sourcefile: "
						+ latexfile);
			}
		} else {
			mainFile = jobname;
		}
		logfile = mainFile + ".log";
	}

	public void setJobname(String s) {
		jobname = s;
		mainFile = jobname;
		logfile = mainFile + ".log";
	}

	public void setOutputDir(File s) {
		outputDir = s;
	}

	public void setAuxDir(File s) {
		auxDir = s;
	}

	/**
	 * Schaltet die Verarbeitung durch PDFLaTeX ein.
	 * 
	 * @param flag
	 */
	public void setPdftex(boolean flag) {
		pdftex = flag;
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

	/**
	 * @return Returns the figbib.
	 */
	public final boolean isFigbib() {
		return figbib;
	}

	/**
	 * @param figbib
	 *            The figbib to set.
	 */
	public final void setFigbib(boolean figbib) {
		this.figbib = figbib;
	}

	/**
	 * @return Returns the glossbib.
	 */
	public final boolean isGlossbib() {
		return glossbib;
	}

	/**
	 * @param glossbib
	 *            The glossbib to set.
	 */
	public final void setGlossbib(boolean glossbib) {
		this.glossbib = glossbib;
	}

}
