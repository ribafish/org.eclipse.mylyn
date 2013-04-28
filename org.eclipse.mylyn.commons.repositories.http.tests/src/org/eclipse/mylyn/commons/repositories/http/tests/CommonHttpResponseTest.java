/*******************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.commons.repositories.http.tests;

import static org.junit.Assert.fail;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.commons.core.operations.CancellableOperationMonitorThread;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpResponse;
import org.eclipse.mylyn.commons.sdk.util.TestUrl;
import org.eclipse.mylyn.internal.commons.core.operations.NullOperationMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Steffen Pingel
 */
public class CommonHttpResponseTest {

	private final TestUrl urls = TestUrl.DEFAULT;

	private NullOperationMonitor monitor;

	private CommonHttpResponse response;

	private final CancellableOperationMonitorThread monitorThread = new CancellableOperationMonitorThread();

	@Before
	public void setUp() throws Exception {
		monitor = new NullOperationMonitor();
		RepositoryLocation location = new RepositoryLocation();
		location.setUrl(urls.getHttpOk().toString());

		HttpGet request = new HttpGet(location.getUrl());
		CommonHttpClient client = new CommonHttpClient(location);
		HttpResponse clientResponse = client.execute(request, monitor);
		response = new CommonHttpResponse(request, clientResponse, monitorThread, monitor);
	}

	@After
	public void tearDown() {
		if (response != null) {
			response.release();
		}
	}

	@Test
	public void testCancel() throws Exception {
		monitor.setCanceled(true);
		InputStream in = response.getResponseEntityAsStream();
		monitorThread.processOperations();
		try {
			in.read();
			fail("Expected OperationCancelledException");
		} catch (OperationCanceledException e) {
			// ignore
		}
	}

	@Test
	public void testCancelAfterRead() throws Exception {
		InputStream in = response.getResponseEntityAsStream();
		in.read();
		monitor.setCanceled(true);
		monitorThread.processOperations();
		try {
			in.read();
			fail("Expected OperationCancelledException");
		} catch (OperationCanceledException e) {
			// ignore
		}
	}

}
