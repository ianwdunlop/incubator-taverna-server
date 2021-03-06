/*
 */
package org.taverna.server.master.identity;
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

import org.taverna.server.master.interfaces.LocalIdentityMapper;
import org.taverna.server.master.utils.UsernamePrincipal;

/**
 * A trivial principal to user mapper that always uses the same ID.
 * @author Donal Fellows
 */
public class ConstantIDMapper implements LocalIdentityMapper {
	private String id;

	/**
	 * Sets what local user ID all users should be mapped to.
	 * 
	 * @param id
	 *            The local user ID.
	 */
	public void setConstantId(String id) {
		this.id = id;
	}

	@Override
	public String getUsernameForPrincipal(UsernamePrincipal user) {
		return id;
	}
}
