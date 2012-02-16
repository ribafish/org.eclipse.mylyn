/*******************************************************************************
 * Copyright (c) 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.gerrit.tests.core;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.gerrit.tests.support.GerritFixture;
import org.eclipse.mylyn.gerrit.tests.support.GerritHarness;
import org.eclipse.mylyn.internal.gerrit.core.GerritConnector;
import org.eclipse.mylyn.internal.gerrit.core.GerritQuery;
import org.eclipse.mylyn.internal.gerrit.core.GerritTaskSchema;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tests.util.InMemoryTaskDataCollector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Steffen Pingel
 */
public class GerritConnectorTest extends TestCase {

	private GerritHarness harness;

	private GerritConnector connector;

	private TaskRepository repository;

	@Override
	@Before
	public void setUp() throws Exception {
		harness = GerritFixture.current().harness();
		connector = new GerritConnector();
		repository = GerritFixture.current().singleRepository();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		harness.dispose();
	}

	@Test
	public void testPerformQueryAnonymous() throws Exception {
		// XXX some test repositories require OpenID auth which is not supported when running tests
		repository.setCredentials(AuthenticationType.REPOSITORY, null, false);

		IRepositoryQuery query = new RepositoryQuery(repository.getConnectorKind(), "query"); //$NON-NLS-1$
		query.setAttribute(GerritQuery.TYPE, GerritQuery.ALL_OPEN_CHANGES);
		InMemoryTaskDataCollector resultCollector = new InMemoryTaskDataCollector();

		IStatus status = connector.performQuery(repository, query, resultCollector, null, new NullProgressMonitor());
		assertEquals(Status.OK_STATUS, status);
		assertTrue(resultCollector.getResults().size() > 0);
		for (TaskData result : resultCollector.getResults()) {
			assertTrue(result.isPartial());
			assertNull(result.getRoot().getAttribute(GerritTaskSchema.getDefault().UPLOADED.getKey()));
		}
	}

}
