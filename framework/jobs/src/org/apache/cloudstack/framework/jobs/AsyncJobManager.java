// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.framework.jobs;

import java.util.List;

import org.apache.cloudstack.api.command.user.job.QueryAsyncJobResultCmd;
import org.apache.cloudstack.framework.jobs.impl.AsyncJobVO;

import com.cloud.utils.Predicate;
import com.cloud.utils.component.Manager;

public interface AsyncJobManager extends Manager {
    
	AsyncJobVO getAsyncJob(long jobId);
	
	List<? extends AsyncJob> findInstancePendingAsyncJobs(String instanceType, Long accountId);
	
	long submitAsyncJob(AsyncJob job);
	long submitAsyncJob(AsyncJob job, boolean scheduleJobExecutionInContext);
	long submitAsyncJob(AsyncJob job, String syncObjType, long syncObjId);

//	AsyncJobResult queryAsyncJobResult(long jobId);

    void completeAsyncJob(long jobId, int jobStatus, int resultCode, Object resultObject);
    void updateAsyncJobStatus(long jobId, int processStatus, Object resultObject);
    void updateAsyncJobAttachment(long jobId, String instanceType, Long instanceId);
    void logJobJournal(long jobId, AsyncJob.JournalType journalType, String
    	journalText, String journalObjJson);
    
	/**
	 * A running thread inside management server can have a 1:1 linked pseudo job.
	 * This is to help make some legacy code work without too dramatic changes.
	 * 
	 * All pseudo jobs should be expunged upon management start event
	 *
	 * @return pseudo job for the thread
	 */
	AsyncJob getPseudoJob();

    /**
     * Used by upper level job to wait for completion of a down-level job (usually VmWork jobs)
     * in synchronized way. Caller needs to use waitAndCheck() to check the completion status
     * of the down-level job
     * 
     * Due to the amount of legacy code that relies on synchronized-call semantics, this form of joinJob
     * is used mostly
     * 
     * 
     * @param jobId upper job that is going to wait the completion of a down-level job
     * @param joinJobId down-level job
	 */
	void joinJob(long jobId, long joinJobId);
	
    /**
     * Used by upper level job to wait for completion of a down-level job (usually VmWork jobs)
     * in asynchronized way, it will cause upper job to cease current execution, upper job will be
     * rescheduled to execute periodically or on wakeup events detected from message bus
     * 
     * @param jobId upper job that is going to wait the completion of a down-level job
     * @param joinJobId down-level job
     * @Param wakeupHandler	wakeup handler
     * @Param wakeupDispatcher wakeup dispatcher
     * @param wakeupTopcisOnMessageBus
     * @param wakeupIntervalInMilliSeconds
     * @param timeoutInMilliSeconds
     */
    void joinJob(long jobId, long joinJobId, String wakeupHandler, String wakupDispatcher,
    		String[] wakeupTopcisOnMessageBus, long wakeupIntervalInMilliSeconds, long timeoutInMilliSeconds);
    
    /**
     * Dis-join two related jobs
     * 
     * @param jobId
     * @param joinedJobId
     */
    void disjoinJob(long jobId, long joinedJobId);
    
    /**
     * Used by down-level job to notify its completion to upper level jobs
     * 
     * @param joinJobId down-level job for upper level job to join with
     * @param joinStatus AsyncJobConstants status code to indicate success or failure of the
     * 					down-level job
     * @param joinResult object-stream serialized result object
     * 					this is primarily used by down-level job to pass error exception objects
     * 					for legacy code to work. To help pass exception object easier, we use
     * 					object-stream based serialization instead of GSON
     */
    void completeJoin(long joinJobId, int joinStatus, String joinResult);
   
    void releaseSyncSource();
    void syncAsyncJobExecution(AsyncJob job, String syncObjType, long syncObjId, long queueSizeLimit);
    
    /**
     * This method will be deprecated after all code has been migrated to fully-asynchronized mode
     * that uses async-feature of joinJob/disjoinJob
     * 
     * @param wakupTopicsOnMessageBus topic on message bus to wakeup the wait
     * @param checkIntervalInMilliSeconds time to break out wait for checking predicate condition
     * @param timeoutInMiliseconds time out to break out the whole wait process
     * @param predicate
     * @return true, predicate condition is satisfied
     * 			false, wait is timed out
     */
    @Deprecated
    boolean waitAndCheck(String[] wakupTopicsOnMessageBus, long checkIntervalInMilliSeconds,
    	long timeoutInMiliseconds, Predicate predicate);
    
    /**
     * Queries for the status or final result of an async job.
     * @param cmd the command that specifies the job id
     * @return an async-call result object
     */
    AsyncJob queryAsyncJobResult(QueryAsyncJobResultCmd cmd);
}
