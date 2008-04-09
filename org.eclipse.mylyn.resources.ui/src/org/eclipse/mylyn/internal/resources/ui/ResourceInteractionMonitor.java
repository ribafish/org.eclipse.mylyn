/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/*
 * Created on Apr 20, 2005
 */
package org.eclipse.mylyn.internal.resources.ui;

import java.util.Iterator;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.mylyn.resources.ResourcesUiBridgePlugin;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;

/**
 * @author Mik Kersten
 */
public class ResourceInteractionMonitor extends AbstractUserInteractionMonitor {

	@Override
	protected void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection, boolean contributeToContext) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;

//			Object selectedObject = structuredSelection.getFirstElement();
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
				Object selectedObject = iterator.next();
				if (selectedObject instanceof File) {
					File file = (File) selectedObject;
					super.handleElementSelection(part, file, contributeToContext);
				}
			}
		} else if (selection instanceof TextSelection) {
			if (part instanceof EditorPart) {
				try {
					Object object = ((EditorPart) part).getEditorInput().getAdapter(IResource.class);
					if (object instanceof IFile) {
						IFile file = (IFile) object;
						if (file.getFileExtension() != null
								&& !ContextCorePlugin.getDefault().getKnownContentTypes().contains(
										file.getFileExtension())) {
							super.handleElementEdit(part, object, contributeToContext);
						}
					}
				} catch (Throwable t) {
					StatusHandler.log(new Status(IStatus.ERROR, ResourcesUiBridgePlugin.PLUGIN_ID,
							"Failed to resolve resource edit", t));
				}
			}
		}
	}
}
