package org.taverna.server.localworker.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface RemoteSingleRun extends Remote {
	/**
	 * @return The name of the Baclava file to use for all inputs, or
	 *         <tt>null</tt> if no Baclava file is set.
	 */
	@Nullable
	public String getInputBaclavaFile() throws RemoteException;

	/**
	 * Sets the Baclava file to use for all inputs. This overrides the use of
	 * individual inputs.
	 * 
	 * @param filename
	 *            The filename to use. Must not start with a <tt>/</tt> or
	 *            contain any <tt>..</tt> segments. Will be interpreted relative
	 *            to the run's working directory.
	 */
	public void setInputBaclavaFile(@NonNull String filename)
			throws RemoteException;

	/**
	 * @return The list of input assignments.
	 */
	@NonNull
	public List<RemoteInput> getInputs() throws RemoteException;

	/**
	 * Create an input assignment.
	 * 
	 * @param name
	 *            The name of the port that this will be an input for.
	 * @return The assignment reference.
	 */
	@NonNull
	public RemoteInput makeInput(@NonNull String name) throws RemoteException;

	/**
	 * @return The file (relative to the working directory) to write the outputs
	 *         of the run to as a Baclava document, or <tt>null</tt> if they are
	 *         to be written to non-Baclava files in a directory called
	 *         <tt>out</tt>.
	 */
	@Nullable
	public String getOutputBaclavaFile() throws RemoteException;

	/**
	 * Sets where the output of the run is to be written to. This will cause the
	 * output to be generated as a Baclava document, rather than a collection of
	 * individual non-Baclava files in the subdirectory of the working directory
	 * called <tt>out</tt>.
	 * 
	 * @param filename
	 *            Where to write the Baclava file (or <tt>null</tt> to cause the
	 *            output to be written to individual files); overwrites any
	 *            previous setting of this value.
	 */
	public void setOutputBaclavaFile(@Nullable String filename)
			throws RemoteException;

	/**
	 * @return The current status of the run.
	 */
	@NonNull
	public RemoteStatus getStatus() throws RemoteException;

	/**
	 * Set the status of the run, which should cause it to move into the given
	 * state. This may cause some significant changes.
	 * 
	 * @param s
	 *            The state to try to change to.
	 * @throws IllegalStateTransitionException
	 *             If the requested state change is impossible. (Note that it is
	 *             always legal to set the status to the current status.)
	 * @throws ImplementationException 
	 */
	public void setStatus(@NonNull RemoteStatus s)
			throws IllegalStateTransitionException, RemoteException, ImplementationException;

	/**
	 * @return When this workflow run was found to have finished, or
	 *         <tt>null</tt> if it has never finished (either still running or
	 *         never started).
	 */
	@Nullable
	public Date getFinishTimestamp() throws RemoteException;

	/**
	 * @return When this workflow run was started, or <tt>null</tt> if it has
	 *         never been started.
	 */
	@Nullable
	public Date getStartTimestamp() throws RemoteException;

	/**
	 * @return Handle to the main working directory of the run.
	 */
	@NonNull
	public RemoteDirectory getWorkingDirectory() throws RemoteException;

	/**
	 * @return The list of listener instances attached to the run.
	 */
	@NonNull
	public List<RemoteListener> getListeners() throws RemoteException;

	/**
	 * Add a listener to the run.
	 * 
	 * @param listener
	 *            The listener to add.
	 * @throws ImplementationException 
	 */
	public void addListener(@NonNull RemoteListener listener)
			throws RemoteException, ImplementationException;

	/**
	 * @return The security context structure for this run.
	 * @throws ImplementationException 
	 */
	@NonNull
	public RemoteSecurityContext getSecurityContext() throws RemoteException, ImplementationException;

	/**
	 * Kill off this run, removing all resources which it consumes.
	 * @throws ImplementationException 
	 */
	public void destroy() throws RemoteException, ImplementationException;

	/**
	 * Get the types of listener supported by this run.
	 * 
	 * @return A list of listener type names.
	 */
	@NonNull
	public List<String> getListenerTypes() throws RemoteException;

	/**
	 * Create a listener that can be attached to this run.
	 * 
	 * @param type
	 *            The type name of the listener to create; it must be one of the
	 *            names returned by the {@link #getListenerTypes()} operation.
	 * @param configuration
	 *            The configuration document for this listener. The nature of
	 *            the contents of this are determined by the type.
	 * @return A handle for the listener.
	 */
	@NonNull
	public RemoteListener makeListener(@NonNull String type,
			@NonNull String configuration) throws RemoteException;
}
