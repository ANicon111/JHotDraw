package org.jhotdraw.app.action.file;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;

import org.jhotdraw.action.AbstractViewAction;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.jhotdraw.util.ResourceBundleUtil;

public class OpenContainingFolderAction extends AbstractViewAction {

    private static final long serialVersionUID = 1L;

    public static final String ID = "file.openContainingFolder";

    public OpenContainingFolderAction(Application app, View view) {
        super(app, view);

        ResourceBundleUtil labels =
                ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
        labels.configureAction(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        View view = getActiveView();
        if (view == null || view.getURI() == null) {
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            URI uri = view.getURI();
            File file = new File(uri);
            File parentFolder = file.getParentFile();
            Desktop.getDesktop().open(parentFolder);
        } catch (Exception ex) {
            String message = (ex.getMessage() != null) ? ex.getMessage() : ex.getClass().getName();
            JOptionPane.showMessageDialog(null,
                    "Could not open containing folder:\n" + message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}