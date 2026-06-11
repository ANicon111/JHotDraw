package org.jhotdraw.draw.action;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.figure.*;

public class SendBackwardActionTest {

    @Test
    public void testSendBackwardMovesFigureOneStep() {
        Drawing drawing = new QuadTreeDrawing();

        Figure f1 = new RectangleFigure();
        Figure f2 = new RectangleFigure();
        Figure f3 = new RectangleFigure();

        drawing.add(f1);
        drawing.add(f2);
        drawing.add(f3);

        DrawingView view = new DefaultDrawingView();
        view.setDrawing(drawing);

        // Move f3 backward (toward back = lower index)
        SendBackwardAction.sendBackward(view, Collections.singleton(f3));

        List<Figure> children = drawing.getChildren();

        assertEquals(f1, children.get(0));
        assertEquals(f3, children.get(1));
        assertEquals(f2, children.get(2));
    }
}
