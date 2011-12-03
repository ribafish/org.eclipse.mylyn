/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.commons.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.commons.ui.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String WorkbenchUtil_Browser_Initialization_Failed;

	public static String WorkbenchUtil_Invalid_URL_Error;

	public static String WorkbenchUtil_No_URL_Error;

	public static String WorkbenchUtil_Open_Location_Title;

	public static String ColorSelectionWindow_Close;

	public static String SwtUtil_Fading;

	public static String AbstractFilteredTree_Find;

	public static String AbstractNotificationPopup_Close_Notification_Job;

	public static String AbstractNotificationPopup_Notification;

	public static String DatePicker_Choose_Date;

	public static String DatePicker_Clear;

	public static String DateSelectionDialog_Clear;

	public static String DateSelectionDialog_Date_Selection;

	public static String ScreenshotCreationPage_After_capturing;

	public static String ScreenshotCreationPage_CAPTURE_SCRRENSHOT;

	public static String ScreenshotCreationPage_NOTE_THAT_YOU_CONTINUTE;

	public static String AbstractColumnViewerSupport_Restore_defaults;

	public static String DatePickerPanel_Today;

	public static String TextControl_FindToolTip;

	public static String TextControl_AccessibleListenerFindButton;

	public static String CollapseAllAction_Label;

	public static String CollapseAllAction_ToolTip;

	public static String ExpandAllAction_Label;

	public static String ExpandAllAction_ToolTip;

	public static String PropertiesAction_Properties;

	public static String ValidatableWizardDialog_Validate_Button_Label;

}
