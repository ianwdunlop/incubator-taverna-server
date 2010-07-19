package org.taverna.server.localworker.impl;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.taverna.server.localworker.remote.RemoteStatus.Finished;
import static org.taverna.server.localworker.remote.RemoteStatus.Initialized;
import static org.taverna.server.localworker.remote.RemoteStatus.Operating;
import static org.taverna.server.localworker.remote.RemoteStatus.Stopped;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.taverna.server.localworker.remote.IllegalStateTransitionException;
import org.taverna.server.localworker.remote.RemoteDirectory;
import org.taverna.server.localworker.remote.RemoteInput;
import org.taverna.server.localworker.remote.RemoteListener;
import org.taverna.server.localworker.remote.RemoteSecurityContext;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.localworker.remote.RemoteStatus;

/**
 * This class implements one side of the connection between the Taverna Server
 * master server and this process. It delegates to a {@link Worker} instance the
 * handling of actually running a workflow.
 * 
 * @author Donal Fellows
 * @see DirectoryDelegate
 * @see FileDelegate
 * @see WorkerCore
 */
public class LocalWorker extends UnicastRemoteObject implements RemoteSingleRun {
	private String executeWorkflowCommand;
	private String workflow;
	private File base;
	private DirectoryDelegate baseDir;
	RemoteStatus status;
	String inputBaclava, outputBaclava;
	Map<String, String> inputFiles;
	Map<String, String> inputValues;
	Worker core;
	private Thread shutdownHook;

	/**
	 * @param executeWorkflowCommand
	 * @param workflow
	 * @param workerClass
	 * @throws RemoteException
	 *             If registration of the worker fails.
	 */
	protected LocalWorker(String executeWorkflowCommand, String workflow,
			Class<? extends Worker> workerClass) throws RemoteException {
		super();
		this.workflow = workflow;
		this.executeWorkflowCommand = executeWorkflowCommand;
		base = new File(randomUUID().toString());
		try {
			forceMkdir(base);
		} catch (IOException e) {
			throw new RemoteException("problem creating run working directory",
					e);
		}
		baseDir = new DirectoryDelegate(base, null);
		inputFiles = new HashMap<String, String>();
		inputValues = new HashMap<String, String>();
		try {
			core = workerClass.newInstance();
		} catch (Exception e) {
			out.println("problem when creating core worker implementation");
			e.printStackTrace(out);
			throw new RuntimeException(
					"problem when creating core worker implementation", e);
		}
		Thread t = new Thread(new Runnable() {
			/**
			 * Kill off the worker launched by the core.
			 */
			@Override
			public void run() {
				try {
					destroy();
				} catch (RemoteException e) {
				}
			}
		});
		getRuntime().addShutdownHook(t);
		shutdownHook = t;
		status = Initialized;
	}

	@Override
	public void destroy() throws RemoteException {
		if (status != Finished && status != Initialized)
			try {
				core.killWorker();
			} catch (Exception e) {
				out.println("problem when killing worker");
				e.printStackTrace(out);
			}
		try {
			if (shutdownHook != null)
				getRuntime().removeShutdownHook(shutdownHook);
		} catch (RuntimeException e) {
			throw new RemoteException("problem removing shutdownHook", e);
		} finally {
			shutdownHook = null;
		}
		// Is this it?
		try {
			if (base != null)
				forceDelete(base);
		} catch (IOException e) {
			out.println("problem deleting working directory");
			e.printStackTrace(out);
			throw new RemoteException("problem deleting working directory", e);
		} finally {
			base = null;
		}
	}

	@Override
	public void addListener(RemoteListener listener) throws RemoteException {
		throw new RemoteException("not implemented");
	}

	@Override
	public String getInputBaclavaFile() {
		return inputBaclava;
	}

	@Override
	public List<RemoteInput> getInputs() throws RemoteException {
		ArrayList<RemoteInput> result = new ArrayList<RemoteInput>();
		for (String name : inputFiles.keySet())
			result.add(new InputDelegate(name));
		return result;
	}

	@Override
	public List<String> getListenerTypes() {
		return emptyList();
	}

	@Override
	public List<RemoteListener> getListeners() {
		return singletonList(core.getDefaultListener());
	}

	@Override
	public String getOutputBaclavaFile() {
		return outputBaclava;
	}

	class SecurityDelegate extends UnicastRemoteObject implements
			RemoteSecurityContext {
		protected SecurityDelegate() throws RemoteException {
			super();
		}
	}

	@Override
	public RemoteSecurityContext getSecurityContext() throws RemoteException {
		return new SecurityDelegate();
	}

	@Override
	public RemoteStatus getStatus() {
		// only state that can spontaneously change to another
		if (status == Operating)
			status = core.getWorkerStatus();
		return status;
	}

	@Override
	public RemoteDirectory getWorkingDirectory() {
		return baseDir;
	}

	void validateFilename(String filename) throws RemoteException {
		if (filename == null)
			throw new IllegalArgumentException("filename must be non-null");
		if (filename.length() == 0 || filename.startsWith("/")
				|| filename.contains("//"))
			throw new RemoteException("invalid filename");
		if (Arrays.asList(filename.split("/")).contains(".."))
			throw new RemoteException("invalid filename");
	}

	class InputDelegate extends UnicastRemoteObject implements RemoteInput {
		private String name;

		InputDelegate(String name) throws RemoteException {
			super();
			this.name = name;
			if (!inputFiles.containsKey(name)) {
				if (status != RemoteStatus.Initialized)
					throw new RemoteException("not initializing");
				inputFiles.put(name, null);
				inputValues.put(name, null);
			}
		}

		@Override
		public String getFile() {
			return inputFiles.get(name);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return inputValues.get(name);
		}

		@Override
		public void setFile(String file) throws RemoteException {
			if (status != RemoteStatus.Initialized)
				throw new RemoteException("not initializing");
			validateFilename(file);
			inputValues.put(name, null);
			inputFiles.put(name, file);
			inputBaclava = null;
		}

		@Override
		public void setValue(String value) throws RemoteException {
			if (status != RemoteStatus.Initialized)
				throw new RemoteException("not initializing");
			inputValues.put(name, value);
			inputFiles.put(name, null);
			LocalWorker.this.inputBaclava = null;
		}
	}

	@Override
	public RemoteInput makeInput(String name) throws RemoteException {
		return new InputDelegate(name);
	}

	@Override
	public RemoteListener makeListener(String type, String configuration)
			throws RemoteException {
		throw new RemoteException("listener manufacturing unsupported");
	}

	@Override
	public void setInputBaclavaFile(String filename) throws RemoteException {
		if (status != RemoteStatus.Initialized)
			throw new RemoteException("not initializing");
		validateFilename(filename);
		for (String input : inputFiles.keySet()) {
			inputFiles.put(input, null);
			inputValues.put(input, null);
		}
		inputBaclava = filename;
	}

	@Override
	public void setOutputBaclavaFile(String filename) throws RemoteException {
		if (status != RemoteStatus.Initialized)
			throw new RemoteException("not initializing");
		if (filename != null)
			validateFilename(filename);
		outputBaclava = filename;
	}

	@Override
	public void setStatus(RemoteStatus newStatus)
			throws IllegalStateTransitionException, RemoteException {
		if (status == newStatus)
			return;

		switch (newStatus) {
		case Initialized:
			throw new IllegalStateTransitionException(
					"may not move back to start");
		case Operating:
			switch (status) {
			case Initialized:
				try {
					core.initWorker(executeWorkflowCommand, workflow, base,
							inputBaclava, inputFiles, inputValues,
							outputBaclava);
				} catch (Exception e) {
					throw new RemoteException(
							"problem creating executing workflow", e);
				}
				break;
			case Stopped:
				try {
					core.startWorker();
				} catch (Exception e) {
					throw new RemoteException("problem starting workflow run",
							e);
				}
				break;
			case Finished:
				throw new IllegalStateTransitionException("already finished");
			}
			status = Operating;
			break;
		case Stopped:
			switch (status) {
			case Initialized:
				throw new IllegalStateTransitionException(
						"may only stop from Operating");
			case Operating:
				try {
					core.stopWorker();
				} catch (Exception e) {
					throw new RemoteException("problem stopping workflow run",
							e);
				}
				break;
			case Finished:
				throw new IllegalStateTransitionException("already finished");
			}
			status = Stopped;
			break;
		case Finished:
			switch (status) {
			case Operating:
			case Stopped:
				try {
					core.killWorker();
				} catch (Exception e) {
					throw new RemoteException("problem killing workflow run", e);
				}
				break;
			}
			status = Finished;
			break;
		}
	}
}