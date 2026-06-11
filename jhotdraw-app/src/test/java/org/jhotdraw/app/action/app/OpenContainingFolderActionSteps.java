package org.jhotdraw.app.action.file;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.mockito.Mockito;

import java.awt.event.ActionEvent;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OpenContainingFolderActionSteps extends Stage<OpenContainingFolderActionSteps> {
    public OpenContainingFolderActionSteps() {
    }
    @ProvidedScenarioState
    private Application mockApp;

    @ProvidedScenarioState
    private View mockView;

    @ProvidedScenarioState
    private OpenContainingFolderAction action;

    @ProvidedScenarioState
    private boolean exceptionThrown;

    public OpenContainingFolderActionSteps a_user_has_a_file_open_in_JHotDraw() throws Exception {
        mockApp = Mockito.mock(Application.class);
        mockView = Mockito.mock(View.class);
        URI uri = new URI("file:///C:/Users/User/Documents/test.svg");
        
        Mockito.when(mockView.getURI()).thenReturn(uri);
        Mockito.when(mockApp.getActiveView()).thenReturn(mockView);
        
        action = new OpenContainingFolderAction(mockApp, mockView);
        return self();
    }

    public OpenContainingFolderActionSteps a_user_has_no_file_open() {
        mockApp = Mockito.mock(Application.class);
        mockView = Mockito.mock(View.class);
        
        Mockito.when(mockView.getURI()).thenReturn(null);
        Mockito.when(mockApp.getActiveView()).thenReturn(mockView);
        
        action = new OpenContainingFolderAction(mockApp, mockView);
        return self();
    }

    public OpenContainingFolderActionSteps the_user_triggers_open_containing_folder() {
        ActionEvent evt = mock(ActionEvent.class);
        exceptionThrown = false;
        try {
            action.actionPerformed(evt);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        return self();
    }

    public OpenContainingFolderActionSteps the_action_completes_without_error() {
        assertThat(exceptionThrown).isFalse();
        return self();
    }

    public OpenContainingFolderActionSteps the_action_ID_is_correctly_defined() {
        assertThat(OpenContainingFolderAction.ID)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("file.openContainingFolder");
        return self();
    }
}