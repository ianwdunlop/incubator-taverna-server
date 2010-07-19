package org.taverna.server.master.interfaces;

import java.security.Principal;
import java.util.Collection;

import org.taverna.server.master.exceptions.FilesystemAccessException;

/**
 * Represents a directory that is the working directory of a workflow run, or a
 * sub-directory of it.
 * 
 * @author Donal Fellows
 * @see File
 */
public interface Directory extends DirectoryEntry {
	/**
	 * @return A list of the contents of the directory.
	 * @throws FilesystemAccessException
	 *             If things go wrong.
	 */
	public Collection<DirectoryEntry> getContents()
			throws FilesystemAccessException;

	/**
	 * @return The contents of the directory (and its sub-directories) as a zip.
	 * @throws FilesystemAccessException
	 *             If things go wrong.
	 */
	public byte[] getContentsAsZip() throws FilesystemAccessException;

	/**
	 * Creates a sub-directory of this directory.
	 * 
	 * @param actor
	 *            Who this is being created by.
	 * @param name
	 *            The name of the sub-directory.
	 * @return A handle to the newly-created directory.
	 * @throws FilesystemAccessException
	 *             If the name is the same as some existing entry in the
	 *             directory, or if something else goes wrong during creation.
	 */
	public Directory makeSubdirectory(Principal actor, String name)
			throws FilesystemAccessException;

	/**
	 * Creates an empty file in this directory.
	 * 
	 * @param actor
	 *            Who this is being created by.
	 * @param name
	 *            The name of the file to create.
	 * @return A handle to the newly-created file.
	 * @throws FilesystemAccessException
	 *             If the name is the same as some existing entry in the
	 *             directory, or if something else goes wrong during creation.
	 */
	public File makeEmptyFile(Principal actor, String name)
			throws FilesystemAccessException;
}