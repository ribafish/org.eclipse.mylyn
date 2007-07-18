/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.internal.tasks.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.internal.tasks.ui.views.ITaskListPresentation;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * @author Rob Elves
 */
public class PresentationDropDownSelectionAction extends Action implements IMenuCreator {

	private static final String LABEL_NAME = "Task Presentation";

	public static final String ID = "org.eclipse.mylyn.tasklist.actions.presentationselection";

	private TaskListView view;

	protected Menu dropDownMenu = null;

	public PresentationDropDownSelectionAction(TaskListView view) {
		super();
		this.view = view;
		setMenuCreator(this);
		setText(LABEL_NAME);
		setToolTipText(LABEL_NAME);
		setId(ID);
		setEnabled(true);
		setImageDescriptor(TasksUiImages.TASKLIST_MODE);
	}

	protected void addActionsToMenu() {
		for (ITaskListPresentation presentation : TaskListView.getPresentations()) {
			PresentationSelectionAction action = new PresentationSelectionAction(presentation);
			ActionContributionItem item = new ActionContributionItem(action);
			action.setText(presentation.getPresentationName());
			action.setImageDescriptor(presentation.getImageDescriptor());
			action.setChecked(view.getCurrentPresentation().getPresentationName().equals(
					presentation.getPresentationName()));
			item.fill(dropDownMenu, -1);
		}
	}

	@Override
	public void run() {
		// ignore
	}

	public void dispose() {
		if (dropDownMenu != null) {
			dropDownMenu.dispose();
			dropDownMenu = null;
		}
	}

	public Menu getMenu(Control parent) {
		if (dropDownMenu != null) {
			dropDownMenu.dispose();
		}
		dropDownMenu = new Menu(parent);
		addActionsToMenu();
		return dropDownMenu;
	}

	public Menu getMenu(Menu parent) {
		if (dropDownMenu != null) {
			dropDownMenu.dispose();
		}
		dropDownMenu = new Menu(parent);
		addActionsToMenu();
		return dropDownMenu;
	}

	private class PresentationSelectionAction extends Action {

		private ITaskListPresentation presentation;

		public PresentationSelectionAction(ITaskListPresentation presentation) {
			this.presentation = presentation;
			setText(presentation.getPresentationName());
		}

		@Override
		public void run() {
			view.applyPresentation(presentation);
		}
	}

}
