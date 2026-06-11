package org.jhotdraw.draw;

import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;

/**
 * Unit tests verifying the selection-management functionality of DefaultDrawingView
 * following refactoring transformations.
 */
public class DefaultDrawingViewTest {

    private DefaultDrawingView drawingView;
    private Drawing drawing;
    private Figure figure1;
    private Figure figure2;

    @Before
    public void setUp() {
        // Initialize the domain objects
        drawingView = new DefaultDrawingView();
        drawing = new QuadTreeDrawing(); // standard concrete concrete implementation of Drawing
        drawingView.setDrawing(drawing);

        // Instantiate mock or simple shapes to add onto the canvas
        figure1 = new RectangleFigure();
        figure2 = new RectangleFigure();

        drawing.add(figure1);
        drawing.add(figure2);
    }

    /**
     * Test Case 1: Best Case Scenario
     * Verifies that when figures are selected, calling clearSelectedFigures()
     * successfully updates the selection context to empty.
     */
    @Test
    public void testClearSelectedFiguresRemovesAllSelections() {
        // Step 1: Add figures to the active visual selection context
        drawingView.addToSelection(figure1);
        drawingView.addToSelection(figure2);

        // Pre-condition check: Ensure the view tracks exactly 2 selections
        assertEquals("Pre-condition failed: Figures were not successfully selected.", 
                     2, drawingView.getSelectionCount());

        // Step 2: Invoke our refactored domain method
        drawingView.clearSelectedFigures();

        // Post-condition check: Invariant verification that selection count resets cleanly to 0
        assertEquals("Post-condition failed: Selection count was not cleared.", 
                     0, drawingView.getSelectionCount());
        assertTrue("Post-condition failed: Selection set is not empty.", 
                   drawingView.getSelectedFigures().isEmpty());
    }

    /**
     * Test Case 2: Boundary Case Scenario
     * Verifies system behavior remains stable and preserves invariants when 
     * clearSelectedFigures() is called on an already empty canvas selection state.
     */
    @Test
    public void testClearSelectedFiguresOnEmptySelection() {
        // Pre-condition: Selection count is already naturally 0
        assertEquals("Pre-condition failed: Selection context should start empty.", 
                     0, drawingView.getSelectionCount());

        // Step 1: Invoke our refactored domain method
        drawingView.clearSelectedFigures();

        // Invariant enforcement: System shouldn't throw errors or modify figure presence
        assertEquals("Boundary failure: Clearing an empty selection altered selection count incorrectly.", 
                     0, drawingView.getSelectionCount());
        assertEquals("Boundary failure: Figures were accidentally removed from the core drawing model.", 
                     2, drawing.getFigures().size());
    }
}
