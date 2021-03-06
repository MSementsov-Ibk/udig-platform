/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package org.locationtech.udig.issues.internal.view;

import org.locationtech.udig.issues.IIssue;
import org.locationtech.udig.issues.IssueConstants;
import org.locationtech.udig.issues.internal.Messages;
import org.locationtech.udig.project.ui.internal.ProjectUIPlugin;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * Manages fixing issues.
 * 
 * @author jones
 * @since 1.0.0
 */
public class IssueHandler {

    private IIssue issue;
    private IViewPart view;
    private IEditorPart editor;
    private boolean hasRestoredView=false;
    private boolean hasRestoredEditor=false;
    private boolean hasRestoredPerspective=false;

    public IssueHandler( IIssue issue ) {
        this.issue = issue;
    }

    public static IssueHandler createHandler( IIssue issue ) {
        return new IssueHandler(issue);
    }

    public void restorePerspective() {
    	hasRestoredPerspective=true;
        String targetP = issue.getPerspectiveID();
        if (targetP == null)
            return;
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        String currentP = activeWorkbenchWindow.getActivePage().getPerspective().getId();
        if (targetP.equals(currentP))
            return;
        try {
            IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
            for( IWorkbenchPage page : pages ) {
                currentP = activeWorkbenchWindow.getActivePage().getPerspective().getId();
                if (targetP.equals(currentP)) {
                    activeWorkbenchWindow.setActivePage(page);
                    return;
                }
            }
            IPerspectiveDescriptor p = PlatformUI.getWorkbench().getPerspectiveRegistry()
                    .findPerspectiveWithId(targetP); 
            activeWorkbenchWindow.getActivePage().setPerspective(p);
            activeWorkbenchWindow.getActivePage().showView(IssueConstants.VIEW_ID);

        } catch (WorkbenchException e) {
            ProjectUIPlugin.log(
                    Messages.IssueHandler_error_perspective + issue.getProblemObject(), 
                    e);
        }

    }

    public void restoreViewPart() {
    	hasRestoredView=true;
        if (issue.getViewPartId() == null)
            return;
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            
            view = page.showView(issue.getViewPartId());
            XMLMemento memento=XMLMemento.createWriteRoot("root"); //$NON-NLS-1$
            issue.getViewMemento(memento);
            view.init(view.getViewSite(), memento);
        } catch (PartInitException e) {
            ProjectUIPlugin.log(
                    Messages.IssueHandler_error_view + issue.getProblemObject(), e); 
        }
    }


    
    public void restoreEditor() {
    	hasRestoredEditor=true;
        if (issue.getEditorInput() == null || issue.getEditorID() == null)
            return;

        try {
            editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(issue.getEditorInput(), issue.getEditorID());
        } catch (PartInitException e) {
            ProjectUIPlugin.log(
                    Messages.IssueHandler_error_editor + issue.getProblemObject(), e); 
        }

    }

    public void fixIssue() {
    	if( !hasRestoredPerspective )
    		restorePerspective();
    	if( !hasRestoredView )
    		restoreViewPart();
    	if (!hasRestoredEditor)
    		restoreEditor();
        issue.fixIssue(view, editor);
    }

}
