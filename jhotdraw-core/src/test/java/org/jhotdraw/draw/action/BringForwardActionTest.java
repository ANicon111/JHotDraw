package org.jhotdraw.draw.action;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.figure.*;

public class BringForwardActionTest {

    @Test
    public void testBringForwardMovesFigureOneStep() {
        Drawing drawing = new QuadTreeDrawing();

        Figure f1 = new RectangleFigure();
        Figure f2 = new RectangleFigure();
        Figure f3 = new RectangleFigure();

        drawing.add(f1);
        drawing.add(f2);
        drawing.add(f3);

        DrawingView view = new DefaultDrawingView();
        view.setDrawing(drawing);

        // Move f2 forward (toward front = higher index)
        BringForwardAction.bringForward(view, Collections.singleton(f2));

        List<Figure> children = drawing.getChildren();

        assertEquals(f1, children.get(0));
        assertEquals(f3, children.get(1));
        assertEquals(f2, children.get(2));
    }

    @Test
    public void testBringForwardAtFrontDoesNothing() {
        Drawing drawing = new QuadTreeDrawing();

        Figure f1 = new RectangleFigure();
        Figure f2 = new RectangleFigure();

        drawing.add(f1);
        drawing.add(f2); // already at front

        DrawingView view = new DefaultDrawingView();
        view.setDrawing(drawing);

        BringForwardAction.bringForward(view, Collections.singleton(f2));

        List<Figure> children = drawing.getChildren();

        // Should remain unchanged
        assertEquals(f1, children.get(0));
        assertEquals(f2, children.get(1));
    }
}
