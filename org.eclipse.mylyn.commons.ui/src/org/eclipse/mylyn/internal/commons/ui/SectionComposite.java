/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.commons.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * @author Steffen Pingel
 */
public class SectionComposite extends SharedScrolledComposite {

	FormToolkit toolkit;

	public Composite content;

	public SectionComposite(Composite parent, int style) {
		super(parent, style | SWT.V_SCROLL);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (toolkit != null) {
					toolkit.dispose();
					toolkit = null;
				}
			}
		});
		content = new Composite(this, SWT.NONE);
		content.setLayout(GridLayoutFactory.fillDefaults().create());
		setContent(content);
		content.setBackground(null);
		setExpandVertical(true);
		setExpandHorizontal(true);
	}

	@Override
	public Composite getContent() {
		return content;
	}

	public ExpandableComposite createSection(String title) {
		return createSection(title, SWT.NONE, false);
	}

	public ExpandableComposite createSection(String title, int expansionStyle) {
		return createSection(title, SWT.NONE, false);
	}

	public ExpandableComposite createSection(String title, int expansionStyle, final boolean grabExcessVerticalSpace) {
		final ExpandableComposite section = getToolkit().createExpandableComposite(
				getContent(),
				ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT | ExpandableComposite.COMPACT
						| expansionStyle);
		section.titleBarTextMarginWidth = 0;
		section.setBackground(null);
		section.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if ((Boolean) e.data == true && grabExcessVerticalSpace) {
					GridData g = (GridData) section.getLayoutData();
					g.verticalAlignment = GridData.FILL;
					g.grabExcessVerticalSpace = true;
					section.setLayoutData(g);
				} else {
					GridData g = (GridData) section.getLayoutData();
					g.verticalAlignment = GridData.BEGINNING;
					g.grabExcessVerticalSpace = false;
					section.setLayoutData(g);
				}
				Point newSize = getDisplay().getActiveShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				Point currentSize = getDisplay().getActiveShell().getSize();
				if (newSize.x > currentSize.x || newSize.y > currentSize.y) {
					getDisplay().getActiveShell().setSize(newSize);
				} else {
					layout(true);
					reflow(true);
				}
			}
		});
		section.setText(title);
		if (content.getLayout() instanceof GridLayout) {
			GridDataFactory.fillDefaults()
					.indent(0, 5)
					.grab(true, false)
					.span(((GridLayout) content.getLayout()).numColumns, SWT.DEFAULT)
					.applyTo(section);
		}
		return section;
	}

	public FormToolkit getToolkit() {
		checkWidget();
		if (toolkit == null) {
			toolkit = new FormToolkit(CommonsUiPlugin.getDefault().getFormColors(getDisplay()));
		}
		return toolkit;
	}

}
