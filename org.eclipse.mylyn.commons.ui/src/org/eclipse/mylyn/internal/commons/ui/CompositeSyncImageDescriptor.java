/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.commons.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * @author Mik Kersten
 */
public class CompositeSyncImageDescriptor extends CompositeImageDescriptor {

	private final ImageData base;

	private final ImageData background;

	private final boolean fillBackground;

	protected Point size;

	static int WIDTH;

	public CompositeSyncImageDescriptor(ImageDescriptor icon, boolean fillBackground) {
		this.base = getImageData(icon);
		this.background = getImageData(CommonImages.OVERLAY_WHITE);
		this.size = new Point(background.width, background.height);
		this.fillBackground = fillBackground;
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		if (fillBackground) {
			drawImage(background, 0, 0);
		}
		drawImage(base, 3, 2);
	}

	private ImageData getImageData(ImageDescriptor descriptor) {
		ImageData data = descriptor.getImageData();
		// see bug 51965: getImageData can return null
		if (data == null) {
			data = DEFAULT_IMAGE_DATA;
		}
		return data;
	}

	@Override
	protected Point getSize() {
		return new Point(size.x, size.y);
	}
}