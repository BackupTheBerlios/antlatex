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
	public String getIf( ){
		return If;
	}
	protected String theCommand = null;

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

	protected final int invoke(final String cmd, final List args)
			throws BuildException {
		if (!run) {return -1; }
		if (If != null && If.equals("off")) { return -1; }
		String[] arguments = new String[1 + args.size()];

		arguments[0] = cmd;
		if (antTask == null) {
			antTask = this;
		}

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
			verbose = true;
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
