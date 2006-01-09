/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: Grep1.java,v 1.2 2006/01/09 13:43:38 jaloma Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java 
 * language and environment is gratefully acknowledged.
 * 
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */

package de.dokutransdata.antlatex;

import java.util.regex.*;
import java.io.*;

/**
 * A grep-like program. No options, but takes a pattern and an arbitrary list of
 * text files.
 */
public class Grep1 {
	/** The pattern we're looking for */
	protected Pattern pattern;

	/** The matcher for this pattern */
	protected Matcher matcher;

	private boolean verbose = false;

	/**
	 * Main will make a Grep object for the pattern, and run it on all input
	 * files listed in argv.
	 */
	public static int doit(String[] argv, boolean verbose) throws Exception {

		if (argv.length < 1) {
			System.err.println("Usage: Grep1 pattern [filename]");
			System.exit(1);
		}
		int res = 0;
		Grep1 pg = new Grep1(argv[0]);
		pg.setVerbose(verbose);
		if (argv.length == 1) {
			res = pg.process(new BufferedReader(
					new InputStreamReader(System.in)), "(standard input)",
					false);
		} else {
			for (int i = 1; i < argv.length; i++) {
				res = pg.process(new BufferedReader(new FileReader(argv[i])),
						argv[i], pg.getVerbose());
			}
		}
		return res;
	}

	/** Construct a Grep1 program */
	public Grep1(String patt) {
		pattern = Pattern.compile(patt);
		matcher = pattern.matcher("");
	}

	/**
	 * Do the work of scanning one file
	 * 
	 * @param inputFile
	 *            BufferedReader object already open
	 * @param fileName
	 *            String Name of the input file
	 * @param printFileName
	 *            Boolean - true to print filename before lines that match.
	 * @return 1 if pattern was found
	 */
	public int process(BufferedReader inputFile, String fileName,
			boolean printFileName) {
		int res = 0;
		String inputLine;

		try {
			while ((inputLine = inputFile.readLine()) != null) {
				matcher.reset(inputLine);
				// if (matcher.lookingAt()) {
				if (matcher.find()) {
					if (printFileName) {
						System.out.print(fileName + ": ");
					}
					if (verbose) {
						System.out.println(inputLine);
					}
					res = 1;
				}
			}
			inputFile.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return res;
	}

	public boolean getVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 *            The verbose to set.
	 */
	public final void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
