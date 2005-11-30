// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LaTeX.java

package de.dokutransdata.antlatex;

import java.util.ArrayList;
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
	/**
	 * BibTeX-Task
	 */
	private BibTeXTask bibtex;

	/**
	 * Multi-BibTeX-Schalter
	 */
	private boolean multibib;

	/**
	 * Schalter zum Aufraeumen.
	 */
	private boolean clean;

	/**
	 * Task zum Loeschen von Dateien.
	 */
	private Delete tempFiles;

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
	 * PDFLaTeX-Schalter
	 */
	private boolean pdftex;

	private String reRunPattern = "(Rerun (LaTeX|to get cross-references right)|Package glosstex Warning: Term )";
	
	/**
	 * Initialisierung (verbose = false, pdftex = false, clean = false)
	 */
	public LaTeX() {
		super();
		latexfile = null;
		logfile = null;
		mainFile = null;
		verbose = false;
		pdftex = false;
		clean = false;

		makeindex = null;
		bibtex = null;
		glosstex = null;
		jxGlosstex = null;
		tempFiles = null;

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
		fileset.setDir(workingDir);
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
		return tempFiles;
	}

	/**
	 * Callback-Methode fuer Ant
	 * 
	 * @return Makeindex-Task
	 */
	public Object createMakeindex() {
		makeindex = new MakeindexTask();
		if (verbose) {
			log("Index is created " + makeindex);
		}
		return makeindex;
	}

	/**
	 * Callback-Methode fuer Ant
	 * 
	 * @return BibTeX-Task
	 */
	public Object createBibtex() {
		bibtex = new BibTeXTask();
		if (verbose) {
			log("BibTex is created " + bibtex);
		}
		return bibtex;
	}

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

	public Object createJxGlosstex() {
		jxGlosstex = new GlossTeX();
		if (verbose) {
			log("jxGlossTeX is created " + jxGlosstex);
		}
		return jxGlosstex;
	}

	private void dump() {
		log("latexfile    = " + latexfile);
		log("mainFile     = " + mainFile);
		log("workingdir   = " + workingDir);
		log("verbose      = " + verbose);
		log("multibib     = " + multibib);
		log("pdftex       = " + pdftex);
		log("clean        = " + clean);
		log("tempFiles    = " + tempFiles);
		log("bibtex       = " + bibtex);
		log("makeindex    = " + makeindex);
		log("glosstex     = " + glosstex);
		log("jxglosstex   = " + jxGlosstex);
	}

	public final void execute() throws BuildException {
		if (verbose) {
			log("Running LaTeX now!");
			dump();
		}
		runLaTeX();
		if (bibtex != null) {
			runBibTeX();
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
			if (makeindex != null) {
				runMakeIndex();
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

	public void init() {
		workingDir = this.getProject().getBaseDir();
		tempFiles = createDefaultDelete();
	}

	private final int runBibTeX() throws BuildException {
		bibtex.setVerbose(verbose);
		bibtex.antTask = this;

		if (multibib) {
			FileSet fileset = new FileSet();
			fileset.setDir(workingDir);
			fileset.createInclude().setName("*.aux");

			bibtex.add(fileset);
			bibtex.execute();
			return 0;
		} else {
			bibtex.setAuxFile(mainFile);
			return bibtex.run();
		}
	}

	private final int runGlossTeX() throws BuildException {
		// glosstex $(MAIN) $(GLOSSSOURCE) ;
		// $(MAKEINDEX) $(MAIN).gxs -o
		// $(MAIN).glx -t $(MAIN).gil -s $(GLXIST); fi

		glosstex.setWorkingDir(workingDir);
		glosstex.setAuxFile(mainFile);
		glosstex.setVerbose(verbose);
		glosstex.antTask = this;

		return glosstex.run();
	}

	private final int runJxGlossTeX() throws BuildException {
		jxGlosstex.setAuxFile(new java.io.File(mainFile + ".aux"));
		jxGlosstex.setVerbose(verbose);
		jxGlosstex.antTask = this;
		
		jxGlosstex.execute();
		return 0;
	}

	private int runGrep() {
		// String myPattern = "(Rerun to get cross-references right)";
		// String myPattern = "(Rerun (LaTeX|to get cross-references right)|Package glosstex Warning: Term )";
		log("Check "+reRunPattern);
		String args[] = { reRunPattern, logfile };
		int res = 0;
		try {
			res = Grep1.doit(args);
		} catch (Exception e) {

		}
		return res;
	}

	private final int runLaTeX() throws BuildException {
		LaTeXTask lp = new LaTeXTask();
		lp.antTask = this;
		lp.setLatexfile(latexfile);
		lp.setWorkingDir(workingDir);
		lp.setPdftex(pdftex);
		lp.setVerbose(verbose);

		return lp.run();
	}

	private final int runMakeIndex() throws BuildException {
		List args = new ArrayList();
		args.add(mainFile);
		makeindex.setIdxFiles(args);
		makeindex.antTask = this;
//		makeindex.setVerbose(verbose);

		return makeindex.run();
	}

	/**
	 * @return Returns the reRunPattern.
	 */
	public final String getReRunPattern() {
		return reRunPattern;
	}

	/**
	 * @param reRunPattern The reRunPattern to set.
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
		int idx;
		if ((idx = latexfile.lastIndexOf(".tex")) != -1) {
			mainFile = latexfile.substring(0, idx);
		} else if ((idx = latexfile.lastIndexOf(".ltx")) != -1) {
			mainFile = latexfile.substring(0, idx);
		} else {
			throw new BuildException("Unknown LaTeX-Sourcefile");
		}
		logfile = mainFile + ".log";
	}

	/**
	 * Schaltet die Verarbeitung durch PDFLaTeX ein.
	 * 
	 * @param flag
	 */
	public void setPdftex(boolean flag) {
		pdftex = flag;
	}

}
