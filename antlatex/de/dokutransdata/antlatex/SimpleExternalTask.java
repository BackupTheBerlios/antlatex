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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.LogStreamHandler;

/**
 * Base to execute external tasks.
 * 
 * @author jaloma
 * 
 */
public class SimpleExternalTask extends Task {
	class SilentStreamHandler implements ExecuteStreamHandler {

		SilentStreamHandler() {
		}
		
		public void setProcessErrorStream(InputStream inputstream) {
		}

		public void setProcessInputStream(OutputStream outputstream) {
		}

		public void setProcessOutputStream(InputStream inputstream) {
		}

		public void start() {
		}

		public void stop() {
		}
	}

	public static final String RCS_ID="Version @(#) $Revision: 1.4 $";
	protected File workingDir;

	protected boolean verbose = false;

	protected String If = null;

	protected boolean run = true;

	public void setRun(boolean property) {
		run = property;
	}

	public boolean getRun() {
		return run;
	}

	public void setIf(String property) {
		If = property;
	}

	public String getIf() {
		return If;
	}

	protected String theCommand = null;

	protected String thePath = null;

	protected Task antTask;

	public SimpleExternalTask() {
		workingDir = null;
	}

	public void setVerbose(boolean newValue) {
		verbose = newValue;
	}

	public final void setWorkingDir(File newValue) {
		workingDir = newValue;
	}

	public final void setCommand(String newValue) {
		theCommand = newValue;
	}

	public final String getCommand() {
		return theCommand;
	}

	public final void setPath(String newValue) {
		thePath = newValue;
	}

	public final String getPath() {
		return thePath;
	}

	protected final int invoke(final String cmd, final List args)
			throws BuildException {
		if (!run) {
			return -1;
		}
		if (If != null && If.equals("off")) {
			return -1;
		}
		if (antTask == null) {
			antTask = this;
		}

		String[] arguments = new String[1 + args.size()];
		String command = cmd;
		//antTask.log("Path: "+thePath);
		if (thePath != null && !thePath.equals("")) {
			File f = new File(thePath,cmd);
// chg :jal:20060112 getCanonicalPath versaut unter Linux den Link/Name-Mechanismus bei LaTeX
//			try {
				command = f.getAbsolutePath();
//			} catch (IOException ie) {
//				antTask.log(ie.getLocalizedMessage());
//				command = cmd;
//			}
		}

		arguments[0] = command;
		try {
			StringBuffer sb = new StringBuffer("Exec: " + arguments[0] + " ");
			for (int i = 0; (i < args.size()); i++) {
				String arg = (String) args.get(i);
				// if (arg.indexOf(" ") != -1) { arg = "\"" + arg + "\""; }
				arguments[i + 1] = arg;
				sb.append(arg + " ");
			}

			if (verbose) {
				antTask.log(sb.toString());
			}

			Execute exeCmd = null;
			//verbose = true;
			if (verbose) {
				exeCmd = new Execute(new LogStreamHandler(antTask, 2, 1), null);
			} else {
				exeCmd = new Execute(new SilentStreamHandler());
			}

			if (workingDir != null) {
				exeCmd.setWorkingDirectory(workingDir);
			}
			
			exeCmd.setCommandline(arguments);
			exeCmd.setAntRun(antTask.getProject());

			return exeCmd.execute();
		} catch (IOException ioe) {
			System.out.println(ioe);
			throw new BuildException(cmd + " not found on the PATH.", ioe);
		}
	}
}
