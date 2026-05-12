/*
 * @(#)ExitAction.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.app.action.app;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import org.jhotdraw.action.AbstractApplicationAction;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.jhotdraw.api.gui.URIChooser;
import org.jhotdraw.gui.JSheet;
import org.jhotdraw.gui.event.SheetEvent;
import org.jhotdraw.gui.event.SheetListener;
import org.jhotdraw.net.URIUtil;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Exits the application after letting the user review all unsaved views.
 * <p>
 * This action is called when the user selects the Exit item in the Application
 * menu, or when the application receives a Quit event from Mac OS X Finder.
 * The menu item is automatically created by the application.
 * <p>
 * This action is automatically created by the application and put into
 * the {@code ApplicationModel} before {@link org.jhotdraw.app.ApplicationModel#initApplication}
 * is called.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class ExitAction extends AbstractApplicationAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "application.exit";
    private static final String LABELS_BUNDLE = "org.jhotdraw.app.Labels";
    private Component oldFocusOwner;
    private View unsavedView;

    /**
     * Creates a new instance.
     */
    public ExitAction(Application app) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle(LABELS_BUNDLE);
        labels.configureAction(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        final Application app = getApplication();
        if (!app.isEnabled()) {
            return;
        }

        app.setEnabled(false);

        UnsavedState state = findUnsavedState(app);

        if (state.count > 0 && state.reviewableView == null) {
            app.setEnabled(true);
            return;
        }

        switch (state.count) {
            case 0:
                handleNoUnsaved();
                break;
            case 1:
                handleSingleUnsaved(app, state.reviewableView, state.lastUri);
                break;
            default:
                handleMultipleUnsaved(app, state.count, state.reviewableView);
                break;
        }
    }

    /* === Extracted helpers === */

    private static class UnsavedState {
        final int count;
        final View reviewableView;
        final URI lastUri;

        UnsavedState(int count, View reviewableView, URI lastUri) {
            this.count = count;
            this.reviewableView = reviewableView;
            this.lastUri = lastUri;
        }
    }

    private UnsavedState findUnsavedState(Application app) {
        int count = 0;
        View reviewable = null;
        URI lastUri = null;

        for (View v : app.views()) {
            if (v.hasUnsavedChanges()) {
                if (v.isEnabled()) {
                    reviewable = v;
                }
                lastUri = v.getURI();
                count++;
            }
        }

        return new UnsavedState(count, reviewable, lastUri);
    }

    private void handleNoUnsaved() {
        doExit();
    }

    private void handleSingleUnsaved(Application app, View view, URI uri) {
        unsavedView = view;

        oldFocusOwner = SwingUtilities
                .getWindowAncestor(view.getComponent())
                .getFocusOwner();

        view.setEnabled(false);

        showSaveDialog(view, uri, (choice) -> {
            if (choice == UserChoice.CANCEL) {
                view.setEnabled(true);
                app.setEnabled(true);
            } else if (choice == UserChoice.DONT_SAVE) {
                doExit();
                view.setEnabled(true);
            } else if (choice == UserChoice.SAVE) {
                saveChanges();
            }
        });
    }

    private void handleMultipleUnsaved(Application app, int count, View reviewable) {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle(LABELS_BUNDLE);

        JOptionPane pane = new JOptionPane(
                "<html>" + UIManager.get("OptionPane.css")
                        + "<b>" + labels.getFormatted("application.exit.doYouWantToReview.message", count) + "</b><p>"
                        + labels.getString("application.exit.doYouWantToReview.details"),
                JOptionPane.QUESTION_MESSAGE);

        Object[] options = {
                labels.getString("application.exit.reviewChangesOption"),
                labels.getString("application.exit.cancelOption"),
                labels.getString("application.exit.discardChangesOption")
        };

        pane.setOptions(options);
        pane.setInitialValue(options[0]);

        JDialog dialog = pane.createDialog(app.getComponent(), null);
        dialog.setVisible(true);

        Object value = pane.getValue();

        if (value == null || value.equals(labels.getString("application.exit.cancelOption"))) {
            app.setEnabled(true);

        } else if (value.equals(labels.getString("application.exit.discardChangesOption"))) {
            doExit();
            app.setEnabled(true);

        } else if (value.equals(labels.getString("application.exit.reviewChangesOption"))) {
            unsavedView = reviewable;
            reviewChanges();
        }
    }

    /* === Small helper abstractions === */

    private enum UserChoice { SAVE, DONT_SAVE, CANCEL }

    private void showSaveDialog(View view, URI uri, java.util.function.Consumer<UserChoice> handler) {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle(LABELS_BUNDLE);

        Object[] options = {
                labels.getString("application.exit.saveOption"),
                labels.getString("application.exit.cancelOption"),
                labels.getString("application.exit.dontSaveOption")
        };

        JOptionPane pane = new JOptionPane(
                "<html>" + UIManager.getString("OptionPane.css")
                        + "<b>" + labels.getFormatted(
                        "application.exit.doYouWantToSave.message",
                        (uri == null) ? labels.getString("unnamedFile") : URIUtil.getName(uri))
                        + "</b><p>"
                        + labels.getString("application.exit.doYouWantToSave.details"),
                JOptionPane.WARNING_MESSAGE);

        pane.setOptions(options);

        JSheet.showSheet(pane, view.getComponent(), evt -> {
            Object value = evt.getValue();

            if (value == null || value.equals(options[1])) {
                handler.accept(UserChoice.CANCEL);
            } else if (value.equals(options[2])) {
                handler.accept(UserChoice.DONT_SAVE);
            } else {
                handler.accept(UserChoice.SAVE);
            }
        });
    }

    protected URIChooser getChooser(View view) {
        URIChooser chsr = (URIChooser) (view.getComponent()).getClientProperty("saveChooser");
        if (chsr == null) {
            chsr = getApplication().getModel().createSaveChooser(getApplication(), view);
            view.getComponent().putClientProperty("saveChooser", chsr);
        }
        return chsr;
    }

    protected void saveChanges() {
        View v = unsavedView;
        if (v.getURI() == null) {
            URIChooser chooser = getChooser(v);
            //int option = fileChooser.showSaveDialog(this);
            JSheet.showSaveSheet(chooser, v.getComponent(), new SheetListener() {
                             @Override
                             public void optionSelected(final SheetEvent evt) {
                                 if (evt.getOption() == JFileChooser.APPROVE_OPTION) {
                                     final URI uri = evt.getChooser().getSelectedURI();
                                     saveToFile(uri, evt.getChooser());
                                 } else {
                                     unsavedView.setEnabled(true);
                                     if (oldFocusOwner != null) {
                                         oldFocusOwner.requestFocus();
                                     }
                                     getApplication().setEnabled(true);
                                 }
                             }
                         });
        } else {
            saveToFile(v.getURI(), null);
        }
    }

    protected void reviewChanges() {
        if (unsavedView.isEnabled()) {
            final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(LABELS_BUNDLE);
            oldFocusOwner = SwingUtilities.getWindowAncestor(unsavedView.getComponent()).getFocusOwner();
            unsavedView.setEnabled(false);
            URI unsavedURI = unsavedView.getURI();
            JOptionPane pane = new JOptionPane(
                    "<html>" + UIManager.getString("OptionPane.css")
                    + labels.getFormatted("application.exit.doYouWantToSave.message",
                                          (unsavedURI == null) ? unsavedView.getTitle() : URIUtil.getName(unsavedURI)),
                    JOptionPane.WARNING_MESSAGE);
            Object[] options = {labels.getString("application.exit.saveOption"), labels.getString(
                                "application.exit.cancelOption"), labels.getString("application.exit.dontSaveOption")};
            pane.setOptions(options);
            pane.setInitialValue(options[0]);
            pane.putClientProperty("Quaqua.OptionPane.destructiveOption", 2);
            JSheet.showSheet(pane, unsavedView.getComponent(), new SheetListener() {
                         @Override
                         public void optionSelected(SheetEvent evt) {
                             Object value = evt.getValue();
                             if (value == null || value.equals(labels.getString("application.exit.cancelOption"))) {
                                 unsavedView.setEnabled(true);
                                 getApplication().setEnabled(true);
                             } else if (value.equals(labels.getString("application.exit.dontSaveOption"))) {
                                 getApplication().dispose(unsavedView);
                                 reviewNext();
                             } else if (value.equals(labels.getString("application.exit.saveOption"))) {
                                 saveChangesAndReviewNext();
                             }
                         }
                     });
        } else {
            getApplication().setEnabled(true);
        }
    }

    protected void saveChangesAndReviewNext() {
        final View v = unsavedView;
        if (v.getURI() == null) {
            URIChooser chooser = getChooser(v);
            JSheet.showSaveSheet(chooser, unsavedView.getComponent(), new SheetListener() {
                             @Override
                             public void optionSelected(final SheetEvent evt) {
                                 if (evt.getOption() == URIChooser.APPROVE_OPTION) {
                                     final URI uri = evt.getChooser().getSelectedURI();
                                     saveToFileAndReviewNext(uri, evt.getChooser());
                                 } else {
                                     v.setEnabled(true);
                                     if (oldFocusOwner != null) {
                                         oldFocusOwner.requestFocus();
                                     }
                                     getApplication().setEnabled(true);
                                 }
                             }
                         });
        } else {
            saveToFileAndReviewNext(v.getURI(), null);
        }
    }

    protected void reviewNext() {
        int unsavedViewsCount = 0;
        View documentToBeReviewed = null;
        for (View p : getApplication().views()) {
            if (p.hasUnsavedChanges()) {
                if (p.isEnabled()) {
                    documentToBeReviewed = p;
                }
                unsavedViewsCount++;
            }
        }
        if (unsavedViewsCount == 0) {
            doExit();
        } else if (documentToBeReviewed != null) {
            unsavedView = documentToBeReviewed;
            reviewChanges();
        } else {
            getApplication().setEnabled(true);
            //System.out.println("exit silently aborted");
        }
    }

    protected void saveToFile(final URI uri, final URIChooser chooser) {
        final View v = unsavedView;
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                v.write(uri, chooser);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    v.setURI(uri);
                    doExit();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(ExitAction.class.getName()).log(Level.SEVERE, null, ex);
                    ResourceBundleUtil labels = ResourceBundleUtil.getBundle(LABELS_BUNDLE);
                    JSheet.showMessageSheet(v.getComponent(),
                                            "<html>" + UIManager.getString("OptionPane.css")
                                            + "<b>" + labels.format("file.save.couldntSave.message", URIUtil.
                                                                    getName(uri)) + "</b><p>"
                                            + ex,
                                            JOptionPane.ERROR_MESSAGE);
                }
                v.setEnabled(true);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
                getApplication().setEnabled(true);
            }
        }.execute();
    }

    protected void saveToFileAndReviewNext(final URI uri, final URIChooser chooser) {
        final View v = unsavedView;
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                v.write(uri, chooser);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    v.setURI(uri);
                    getApplication().dispose(unsavedView);
                    reviewNext();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(ExitAction.class.getName()).log(Level.SEVERE, null, ex);
                    ResourceBundleUtil labels = ResourceBundleUtil.getBundle(LABELS_BUNDLE);
                    JSheet.showMessageSheet(v.getComponent(),
                                            "<html>" + UIManager.getString("OptionPane.css")
                                            + "<b>" + labels.format("file.save.couldntSave.message", uri) + "</b><p>"
                                            + ex,
                                            JOptionPane.ERROR_MESSAGE);
                    v.setEnabled(true);
                    if (oldFocusOwner != null) {
                        oldFocusOwner.requestFocus();
                    }
                    getApplication().setEnabled(true);
                }
            }
        }.execute();
    }

    protected void doExit() {
        getApplication().destroy();
    }
}
