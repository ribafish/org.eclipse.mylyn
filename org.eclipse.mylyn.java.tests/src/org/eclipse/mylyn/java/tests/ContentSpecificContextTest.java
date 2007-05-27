/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.java.tests;

import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.tests.AbstractContextTest;
import org.eclipse.mylar.internal.context.core.InteractionContext;
import org.eclipse.mylar.internal.context.core.ScalingFactors;
import org.eclipse.mylar.internal.java.JavaStructureBridge;
import org.eclipse.mylar.monitor.core.InteractionEvent;

/**
 * @author Mik Kersten
 */
public class ContentSpecificContextTest extends AbstractContextTest {

	public void testEventProcessing() {
		InteractionContext context = new InteractionContext("global-id", new ScalingFactors());
		context.setContentLimitedTo(JavaStructureBridge.CONTENT_TYPE);
		ContextCorePlugin.getContextManager().addGlobalContext(context);
		
		ContextCorePlugin.getContextManager().processInteractionEvent(new InteractionEvent(InteractionEvent.Kind.PROPAGATION, "foo-kind", "h0", MOCK_ORIGIN));
		assertEquals(0, context.getAllElements().size());
		ContextCorePlugin.getContextManager().processInteractionEvent(mockSelection("h1"), false, false);
		assertEquals(1, context.getAllElements().size());
		ContextCorePlugin.getContextManager().removeGlobalContext(context);
	}
	
}
