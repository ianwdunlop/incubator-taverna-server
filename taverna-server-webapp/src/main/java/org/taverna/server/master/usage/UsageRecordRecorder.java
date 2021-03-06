/*
 */
package org.taverna.server.master.usage;
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

import static org.apache.commons.logging.LogFactory.getLog;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.ogf.usage.JobUsageRecord;
import org.springframework.beans.factory.annotation.Required;
import org.taverna.server.master.api.ManagementModel;
import org.taverna.server.master.utils.Contextualizer;
import org.taverna.server.master.utils.JDOSupport;

/**
 * A simple state-aware writer of usage records. It just appends them, one by
 * one, to the file whose name is stored in the state.
 * 
 * @author Donal Fellows
 */
public class UsageRecordRecorder extends JDOSupport<UsageRecord> {
	private Log log = getLog("Taverna.Server.Webapp");
	public UsageRecordRecorder() {
		super(UsageRecord.class);
	}

	private String logFile = null;
	private boolean disableDB = false;
	private ManagementModel state;
	private Contextualizer contextualizer;
	private String logDestination;
	private PrintWriter writer;
	private Object lock = new Object();
	private UsageRecordRecorder self;

	/**
	 * @param state
	 *            the state to set
	 */
	@Required
	public void setState(ManagementModel state) {
		this.state = state;
	}

	@Required
	public void setSelf(UsageRecordRecorder self) {
		this.self = self;
	}

	public void setLogFile(String logFile) {
		this.logFile = (logFile == null || logFile.equals("none")) ? null : logFile;
	}

	public void setDisableDB(String disable) {
		disableDB = "yes".equalsIgnoreCase(disable);
	}

	/**
	 * @param contextualizer
	 *            the system's contextualizer, used to allow making the UR dump
	 *            file be placed relative to the webapp.
	 */
	@Required
	public void setContextualizer(Contextualizer contextualizer) {
		this.contextualizer = contextualizer;
	}

	/**
	 * Accept a usage record for recording.
	 * 
	 * @param usageRecord
	 *            The serialized usage record to record.
	 */
	public void storeUsageRecord(String usageRecord) {
		String logfile = state.getUsageRecordLogFile();
		if (logfile == null)
			logfile = this.logFile;
		if (logfile != null) {
			logfile = contextualizer.contextualize(logfile);
			synchronized (lock) {
				if (!logfile.equals(logDestination)) {
					if (writer != null) {
						writer.close();
						writer = null;
					}
					try {
						writer = new PrintWriter(new FileWriter(logfile));
						logDestination = logfile;
					} catch (IOException e) {
						log.warn("failed to open usage record log file", e);
					}
				}
				if (writer != null) {
					writer.println(usageRecord);
					writer.flush();
				}
			}
		}

		if (!disableDB)
			saveURtoDB(usageRecord);
	}

	/**
	 * How to save a usage record to the database.
	 * 
	 * @param usageRecord
	 *            The serialized usage record to save.
	 */
	protected void saveURtoDB(String usageRecord) {
		UsageRecord ur;
		try {
			ur = new UsageRecord(usageRecord);
		} catch (JAXBException e) {
			log.warn("failed to deserialize usage record", e);
			return;
		}

		try {
			self.saveURtoDB(ur);
		} catch (RuntimeException e) {
			log.warn("failed to save UR to database", e);
		}
	}

	@WithinSingleTransaction
	public void saveURtoDB(UsageRecord ur) {
		persist(ur);
	}

	@WithinSingleTransaction
	public List<JobUsageRecord> getUsageRecords() {
		List<String> urs = allByDate();
		List<JobUsageRecord> result = new ArrayList<>();
		for (String ur : urs)
			try {
				result.add(JobUsageRecord.unmarshal(ur));
			} catch (JAXBException e) {
				log.warn("failed to unmarshal UR", e);
			}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<String> allByDate() {
		return (List<String>) namedQuery("allByDate").execute();
	}

	@PreDestroy
	public void close() {
		if (writer != null)
			writer.close();
	}
}