/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.context.tasks.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.core.storage.CommonStore;
import org.eclipse.mylyn.commons.core.storage.ICommonStorable;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContext;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryTaskHandleUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.context.AbstractTaskContextStore;

/**
 * @author Steffen Pingel
 */
public class TaskContextStore extends AbstractTaskContextStore {

	private static final String FOLDER_DATA = "data"; //$NON-NLS-1$

	private File directory;

	private CommonStore taskStore;

	public ICommonStorable getStorable(ITask task) {
		return getTaskStore().get(getPath(task));
	}

	private IPath getPath(ITask task) {
		IPath path = new Path(""); //$NON-NLS-1$
		path = path.append(task.getConnectorKind() + "-" + CoreUtil.asFileName(task.getRepositoryUrl())); //$NON-NLS-1$
		path = path.append(FOLDER_DATA);
		path = path.append(CoreUtil.asFileName(task.getTaskId()));
		return path;
	}

	@Override
	public IAdaptable copyContext(ITask sourceTask, ITask targetTask) {
		IInteractionContext result = copyContextInternal(sourceTask, targetTask);
		return asAdaptable(result);
	}

	@Override
	public void clearContext(ITask task) {
		ContextCorePlugin.getContextManager().deleteContext(task.getHandleIdentifier());
	}

	@Override
	public void deleteContext(ITask task) {
		ICommonStorable storable = getStorable(task);
		try {
			storable.deleteAll();
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.WARNING, TasksUiPlugin.ID_PLUGIN,
					"Unexpected error while deleting context state", e)); //$NON-NLS-1$
		} finally {
			storable.release();
		}

		ContextCorePlugin.getContextManager().deleteContext(task.getHandleIdentifier());
	}

	@Override
	public File getFileForContext(ITask task) {
		return ContextCorePlugin.getContextStore().getFileForContext(task.getHandleIdentifier());
	}

	@Override
	public boolean hasContext(ITask task) {
		return ContextCore.getContextStore().hasContext(task.getHandleIdentifier());
	}

	@Override
	public void mergeContext(ITask sourceTask, ITask targetTask) {
		ContextCorePlugin.getContextStore().merge(sourceTask.getHandleIdentifier(), targetTask.getHandleIdentifier());

		// FIXME migrate local state
		taskStore.copy(getPath(sourceTask), getPath(targetTask), false);
	}

	@Override
	public IAdaptable moveContext(ITask sourceTask, ITask targetTask) {
		final IInteractionContext result = copyContextInternal(sourceTask, targetTask);

		// move task activity
		ChangeActivityHandleOperation operation = new ChangeActivityHandleOperation(sourceTask.getHandleIdentifier(),
				targetTask.getHandleIdentifier());
		try {
			operation.run(new NullProgressMonitor());
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.WARNING, TasksUiPlugin.ID_PLUGIN,
					"Failed to migrate activity to new task", e.getCause())); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// ignore
		}

		return asAdaptable(result);
	}

	@Override
	public void refactorRepositoryUrl(TaskRepository repository, String oldRepositoryUrl, String newRepositoryUrl) {
		refactorMetaContextHandles(oldRepositoryUrl, newRepositoryUrl);
		refactorContextFileNames(oldRepositoryUrl, newRepositoryUrl);
		if (repository != null) {
			refactorTasksStoreLocation(repository, oldRepositoryUrl, newRepositoryUrl);
		}
	}

	private void refactorTasksStoreLocation(TaskRepository repository, String oldRepositoryUrl, String newRepositoryUrl) {
		IPath oldPath = new Path(repository.getConnectorKind() + "-" + CoreUtil.asFileName(oldRepositoryUrl)).append(FOLDER_DATA); //$NON-NLS-1$
		IPath newPath = new Path(repository.getConnectorKind() + "-" + CoreUtil.asFileName(newRepositoryUrl)).append(FOLDER_DATA); //$NON-NLS-1$

		File oldFile = new File(directory, oldPath.toOSString());
		if (oldFile.exists()) {
			File newFile = new File(directory, newPath.toOSString());
			newFile.getParentFile().mkdirs();
			oldFile.renameTo(newFile);
		}
	}

	@Override
	public void saveActiveContext() {
		// FIXME save local state
		ContextCorePlugin.getContextStore().saveActiveContext();
	}

	private IAdaptable asAdaptable(final IInteractionContext result) {
		return new IAdaptable() {
			public Object getAdapter(Class adapter) {
				if (adapter == IInteractionContext.class) {
					return result;
				}
				return null;
			}
		};
	}

	private IInteractionContext copyContextInternal(ITask sourceTask, ITask targetTask) {
		ContextCorePlugin.getContextStore().saveActiveContext();
		final IInteractionContext result = ContextCore.getContextStore().cloneContext(sourceTask.getHandleIdentifier(),
				targetTask.getHandleIdentifier());

		// FIXME migrate local state
		taskStore.copy(getPath(sourceTask), getPath(targetTask), true);

		return result;
	}

	@SuppressWarnings("restriction")
	private void refactorContextFileNames(String oldUrl, String newUrl) {
		File dataDir = new File(TasksUiPlugin.getDefault().getDataDirectory(), ITasksCoreConstants.CONTEXTS_DIRECTORY);
		if (dataDir.exists() && dataDir.isDirectory()) {
			File[] files = dataDir.listFiles();
			if (files != null) {
				for (File file : dataDir.listFiles()) {
					int dotIndex = file.getName().lastIndexOf(".xml"); //$NON-NLS-1$
					if (dotIndex != -1) {
						String storedHandle;
						try {
							storedHandle = URLDecoder.decode(file.getName().substring(0, dotIndex),
									InteractionContextManager.CONTEXT_FILENAME_ENCODING);
							int delimIndex = storedHandle.lastIndexOf(RepositoryTaskHandleUtil.HANDLE_DELIM);
							if (delimIndex != -1) {
								String storedUrl = storedHandle.substring(0, delimIndex);
								if (oldUrl.equals(storedUrl)) {
									String id = RepositoryTaskHandleUtil.getTaskId(storedHandle);
									String newHandle = RepositoryTaskHandleUtil.getHandle(newUrl, id);
									File newFile = ContextCorePlugin.getContextStore().getFileForContext(newHandle);
									file.renameTo(newFile);
								}
							}
						} catch (Exception e) {
							StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN,
									"Could not move context file: " + file.getName(), e)); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("restriction")
	private void refactorMetaContextHandles(String oldRepositoryUrl, String newRepositoryUrl) {
		InteractionContext metaContext = ContextCorePlugin.getContextManager().getActivityMetaContext();
		ContextCorePlugin.getContextManager().resetActivityMetaContext();
		InteractionContext newMetaContext = ContextCorePlugin.getContextManager().getActivityMetaContext();
		for (InteractionEvent event : metaContext.getInteractionHistory()) {
			if (event.getStructureHandle() != null) {
				String storedUrl = RepositoryTaskHandleUtil.getRepositoryUrl(event.getStructureHandle());
				if (storedUrl != null) {
					if (oldRepositoryUrl.equals(storedUrl)) {
						String taskId = RepositoryTaskHandleUtil.getTaskId(event.getStructureHandle());
						if (taskId != null) {
							String newHandle = RepositoryTaskHandleUtil.getHandle(newRepositoryUrl, taskId);
							event = new InteractionEvent(event.getKind(), event.getStructureKind(), newHandle,
									event.getOriginId(), event.getNavigation(), event.getDelta(),
									event.getInterestContribution(), event.getDate(), event.getEndDate());
						}
					}
				}
			}
			newMetaContext.parseEvent(event);
		}
	}

	@Override
	public synchronized void setDirectory(File directory) {
		this.directory = directory;
		if (taskStore != null) {
			taskStore.setLocation(directory);
		}

		File contextDirectory = new File(directory.getParent(), ITasksCoreConstants.CONTEXTS_DIRECTORY);
		if (!contextDirectory.exists()) {
			contextDirectory.mkdirs();
		}
		ContextCorePlugin.getContextStore().setContextDirectory(contextDirectory);
	}

	private synchronized CommonStore getTaskStore() {
		if (taskStore == null) {
			taskStore = new CommonStore(directory);
		}
		return taskStore;
	}

}
