/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.core.client.compat;

import java.util.List;

/**
 * Provides additional fields used by Gerrit 2.2.
 * 
 * @author Steffen Pingel
 */
public class PatchSetPublishDetailX extends com.google.gerrit.common.data.PatchSetPublishDetail {

	protected boolean canSubmit;

	protected List<PermissionLabel> labels;

	public boolean canSubmit() {
		return canSubmit;
	}

	public List<PermissionLabel> getLabels() {
		return labels;
	}

}
