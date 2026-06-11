package org.jhotdraw.app.action.app;

import static org.mockito.Mockito.*;

import java.util.Collections;

import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.junit.Test;

public class ExitActionTest {

    /**
     * Test helper that allows us to verify whether doExit() was called.
     */
    private static class TestableExitAction extends ExitAction {
        boolean exitCalled = false;

        public TestableExitAction(Application app) {
            super(app);
        }

        @Override
        protected void doExit() {
            exitCalled = true;
        }
    }

    /**
     * Best-case scenario:
     * No unsaved views -> application should exit.
     */
    @Test
    public void testActionPerformed_NoUnsavedViews_ExitsApplication() {
        Application app = mock(Application.class);

        when(app.isEnabled()).thenReturn(true);
        when(app.views()).thenReturn(Collections.emptyList());

        TestableExitAction action = new TestableExitAction(app);

        action.actionPerformed(null);

        verify(app).setEnabled(false);
        assert(action.exitCalled);
    }

    /**
     * Boundary case:
     * Application disabled -> nothing happens.
     */
    @Test
    public void testActionPerformed_ApplicationDisabled_DoesNothing() {
        Application app = mock(Application.class);

        when(app.isEnabled()).thenReturn(false);

        TestableExitAction action = new TestableExitAction(app);

        action.actionPerformed(null);

        verify(app, never()).setEnabled(false);
        assert(!action.exitCalled);
    }

    /**
     * Boundary case:
     * Unsaved view exists but cannot be reviewed because it is disabled.
     * Application should be re-enabled and not exit.
     */
    @Test
    public void testActionPerformed_UnsavedViewDisabled_ReEnablesApplication() {
        Application app = mock(Application.class);
        View view = mock(View.class);

        when(app.isEnabled()).thenReturn(true);
        when(app.views()).thenReturn(Collections.singletonList(view));

        when(view.hasUnsavedChanges()).thenReturn(true);
        when(view.isEnabled()).thenReturn(false);

        TestableExitAction action = new TestableExitAction(app);

        action.actionPerformed(null);

        verify(app).setEnabled(false);
        verify(app).setEnabled(true);

        assert(!action.exitCalled);
    }
}