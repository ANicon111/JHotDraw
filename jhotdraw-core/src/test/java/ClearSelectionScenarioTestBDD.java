package org.jhotdraw.draw.bdd;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.jhotdraw.draw.bdd.stages.GivenDrawingCanvasState;
import org.jhotdraw.draw.bdd.stages.WhenSelectionActionExecuted;
import org.jhotdraw.draw.bdd.stages.ThenSelectionVerification;
import org.junit.Test;

/**
 * Automates Behavior-Driven Testing scenarios using the JGiven framework framework.
 */
public class ClearSelectionScenarioTestBDD extends ScenarioTest<
    GivenDrawingCanvasState, 
    WhenSelectionActionExecuted, 
    ThenSelectionVerification> {

    @Test
    public void clearing_active_selections_should_leave_canvas_elements_intact() {
        given().a_drawing_view_with_elements_configured()
               .and().the_elements_are_actively_selected();

        when().the_user_triggers_the_clear_selection_action();

        then().the_view_selection_count_should_be(0)
              .and().the_total_canvas_elements_remain_unchanged(2);
    }

    @Test
    public void clearing_an_already_empty_selection_should_not_alter_system_state() {
        given().a_drawing_view_with_elements_configured()
               .and().no_elements_are_selected();

        when().the_user_triggers_the_clear_selection_action();

        then().the_view_selection_count_should_be(0);
    }
}