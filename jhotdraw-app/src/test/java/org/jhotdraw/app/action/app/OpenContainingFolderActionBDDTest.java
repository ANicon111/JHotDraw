package org.jhotdraw.app.action.file;

import com.tngtech.jgiven.junit.SimpleScenarioTest;
import org.junit.Test;

public class OpenContainingFolderActionBDDTest 
        extends SimpleScenarioTest<OpenContainingFolderActionSteps> {

    /**
     * BDD Scenario 1 - Best case:
     * Given a user has a file open in JHotDraw
     * When the user triggers Open Containing Folder
     * Then the action completes without error
     */
    @Test
    public void open_containing_folder_with_valid_file() throws Exception {
        given().a_user_has_a_file_open_in_JHotDraw();
        when().the_user_triggers_open_containing_folder();
        then().the_action_completes_without_error();
    }

    /**
     * BDD Scenario 2 - Boundary case:
     * Given a user has no file open
     * When the user triggers Open Containing Folder
     * Then the action completes without error (graceful no-op)
     */
    @Test
    public void open_containing_folder_with_no_file_open() {
        given().a_user_has_no_file_open();
        when().the_user_triggers_open_containing_folder();
        then().the_action_completes_without_error();
    }

    /**
     * BDD Scenario 3 - Invariant:
     * Given the action class is loaded
     * When we inspect the action ID
     * Then it must match the expected identifier
     */
    @Test
    public void action_has_correct_identifier() throws Exception {
        given().a_user_has_a_file_open_in_JHotDraw();
        when().the_user_triggers_open_containing_folder();
        then().the_action_ID_is_correctly_defined();
    }
}