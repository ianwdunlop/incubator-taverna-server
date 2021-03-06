/*
 */
package org.taverna.server.master.factories;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.taverna.server.master.common.Workflow;
import org.taverna.server.master.exceptions.NoCreateException;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.utils.UsernamePrincipal;

/**
 * How to construct a Taverna Server Workflow Run.
 * 
 * @author Donal Fellows
 */
public interface RunFactory {
	/**
	 * Make a Taverna Server workflow run that is bound to a particular user
	 * (the "creator") and able to run a particular workflow.
	 * 
	 * @param creator
	 *            The user creating the workflow instance.
	 * @param workflow
	 *            The workflow to instantiate
	 * @return An object representing the run.
	 * @throws NoCreateException
	 *             On failure.
	 */
	TavernaRun create(UsernamePrincipal creator, Workflow workflow)
			throws NoCreateException;

	/**
	 * Check whether the factory is permitting runs to actually start operating.
	 * 
	 * @return Whether a run should start.
	 */
	boolean isAllowingRunsToStart();
}
