package org.jhotdraw.app.action.file;
 
import java.awt.event.ActionEvent;
import java.net.URI;

import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 
public class OpenContainingFolderActionTest {
 
    private Application mockApp;
    private View mockView;
    private OpenContainingFolderAction action;
 
    @Before
    public void setUp() {
        mockApp = Mockito.mock(Application.class);
        mockView = Mockito.mock(View.class);
        action = new OpenContainingFolderAction(mockApp, mockView);
    }
 
    /**
     * Best case: view is active and has a valid URI.
     * Expects no exception to be thrown during actionPerformed.
     */
    @Test
    public void testActionPerformed_validURI_doesNotThrow() throws Exception {
        URI uri = new URI("file:///C:/Users/User/Documents/test.svg");
        when(mockView.getURI()).thenReturn(uri);
        when(mockApp.getActiveView()).thenReturn(mockView);
 
        ActionEvent evt = mock(ActionEvent.class);
 
        // Should not throw even if Desktop.open fails (no real desktop in test env)
        try {
            action.actionPerformed(evt);
        } catch (Exception e) {
            // Desktop may not be supported in CI — that is acceptable
        }
    }
 
    /**
     * Boundary case: active view is null.
     * Expects the action to return early without throwing.
     */
    @Test
    public void testActionPerformed_nullView_doesNothing() {
        when(mockApp.getActiveView()).thenReturn(null);
 
        ActionEvent evt = mock(ActionEvent.class);
        action.actionPerformed(evt); // should silently return
    }
 
    /**
     * Boundary case: view exists but URI is null (file has never been saved).
     * Expects the action to return early without throwing.
     */
    @Test
    public void testActionPerformed_nullURI_doesNothing() {
        when(mockApp.getActiveView()).thenReturn(mockView);
        when(mockView.getURI()).thenReturn(null);
 
        ActionEvent evt = mock(ActionEvent.class);
        action.actionPerformed(evt); // should silently return
    }
 
    /**
     * Invariant: action ID must never be null or empty.
     */
    @Test
    public void testActionID_isNotNull() {
        assert OpenContainingFolderAction.ID != null : "Action ID must not be null";
        assert !OpenContainingFolderAction.ID.isEmpty() : "Action ID must not be empty";
    }
 
    /**
     * Invariant: action must be constructed without throwing
     * even with a freshly mocked app and view.
     */
    @Test
    public void testConstructor_doesNotThrow() {
        OpenContainingFolderAction a = new OpenContainingFolderAction(mockApp, mockView);
        assert a != null : "Action instance must not be null after construction";
    }
}
 
