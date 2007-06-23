/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.trac.tests;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.trac.core.ITracClient;
import org.eclipse.mylyn.internal.trac.core.TracCorePlugin;
import org.eclipse.mylyn.internal.trac.core.TracRepositoryConnector;
import org.eclipse.mylyn.internal.trac.core.TracTask;
import org.eclipse.mylyn.internal.trac.core.ITracClient.Version;
import org.eclipse.mylyn.internal.trac.core.model.TracTicket;
import org.eclipse.mylyn.internal.trac.core.model.TracTicket.Key;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.trac.tests.support.TestFixture;
import org.eclipse.mylyn.trac.tests.support.XmlRpcServer.TestData;

/**
 * @author Steffen Pingel
 */
public class TracTaskDataHandlerTest extends TestCase {

	private TracRepositoryConnector connector;

	private TaskRepository repository;

	private TaskRepositoryManager manager;

	private TestData data;

	public TracTaskDataHandlerTest() {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		data = TestFixture.init010();

		manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());

		connector = (TracRepositoryConnector) manager.getRepositoryConnector(TracCorePlugin.REPOSITORY_KIND);
		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);
	}

	protected void init(String url, Version version) {
		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);

		repository = new TaskRepository(TracCorePlugin.REPOSITORY_KIND, url);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		repository.setTimeZoneId(ITracClient.TIME_ZONE);
		repository.setCharacterEncoding(ITracClient.CHARSET);
		repository.setVersion(version.name());

		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());
	}

	public void testGetChangedSinceLastSyncWeb096() throws Exception {
		init(TracTestConstants.TEST_TRAC_096_URL, Version.TRAC_0_9);
		TracTask task = (TracTask) connector.createTaskFromExistingId(repository, data.offlineHandlerTicketId + "",
				new NullProgressMonitor());

		Set<AbstractTask> tasks = new HashSet<AbstractTask>();
		tasks.add(task);

		assertEquals(null, repository.getSynchronizationTimeStamp());
		boolean changed = connector.markStaleTasks(repository, tasks, new NullProgressMonitor());
		assertEquals(true, changed);
		assertEquals(null, repository.getSynchronizationTimeStamp());
		assertFalse(task.isStale());

		int time = (int) (System.currentTimeMillis() / 1000) + 1;
		repository.setSynchronizationTimeStamp(time + "");
		changed = connector.markStaleTasks(repository, tasks, new NullProgressMonitor());
		assertEquals(true, changed);
		assertFalse(task.isStale());
	}

	public void testGetChangedSinceLastSyncXmlRpc010() throws Exception {
		init(TracTestConstants.TEST_TRAC_010_URL, Version.XML_RPC);
		getChangedSinceLastSync();
	}

	public void testGetChangedSinceLastSyncXmlRpc011() throws Exception {
		init(TracTestConstants.TEST_TRAC_011_URL, Version.XML_RPC);
		getChangedSinceLastSync();
	}

	private void getChangedSinceLastSync() throws Exception {
		TracTask task = (TracTask) connector.createTaskFromExistingId(repository, data.offlineHandlerTicketId + "",
				new NullProgressMonitor());
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		RepositoryTaskData taskData = TasksUiPlugin.getTaskDataManager().getNewTaskData(task.getRepositoryUrl(), task.getTaskId());

		int lastModified = Integer.parseInt(taskData.getLastModified());

		Set<AbstractTask> tasks = new HashSet<AbstractTask>();
		tasks.add(task);

		assertEquals(null, repository.getSynchronizationTimeStamp());
		boolean changed = connector.markStaleTasks(repository, tasks, new NullProgressMonitor());
		assertTrue(changed);
		assertTrue(task.isStale());

		// always returns the ticket because time comparison mode is >=
		task.setStale(false);
		repository.setSynchronizationTimeStamp(lastModified + "");
		changed = connector.markStaleTasks(repository, tasks, new NullProgressMonitor());
		assertTrue(changed);
		assertTrue(task.isStale());

		task.setStale(false);
		repository.setSynchronizationTimeStamp((lastModified + 1) + "");
		changed = connector.markStaleTasks(repository, tasks, new NullProgressMonitor());
		assertTrue(changed);
		assertFalse(task.isStale());

		// change ticket making sure it gets a new change time
		Thread.sleep(1000);
		ITracClient client = connector.getClientManager().getRepository(repository);
		TracTicket ticket = client.getTicket(data.offlineHandlerTicketId);
		if (ticket.getValue(Key.DESCRIPTION).equals(lastModified + "")) {
			ticket.putBuiltinValue(Key.DESCRIPTION, lastModified + "x");
		} else {
			ticket.putBuiltinValue(Key.DESCRIPTION, lastModified + "");
		}
		client.updateTicket(ticket, "comment");

		task.setStale(false);
		repository.setSynchronizationTimeStamp((lastModified + 1) + "");
		changed = connector.markStaleTasks(repository, tasks, new NullProgressMonitor());
		assertTrue(changed);
		assertTrue(task.isStale());
	}

	public void testNonNumericTaskId() {
		try {
			connector.getTaskDataHandler().getTaskData(repository, "abc", new NullProgressMonitor());
			fail("Expected CoreException");
		} catch (CoreException e) {
		}
	}

}
