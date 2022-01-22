package lists;

import org.junit.Test;
import static org.junit.Assert.*;

/** FIXME
 *
 *  @author Tyler Freund
 */

public class ListsTest {
    /** FIXME
     */
    @Test
    public void testnaturalRuns(){
        IntList a = IntList.list(1, 3, 7, 5, 4, 6, 9, 10, 10, 11);
//        IntList empty = IntList.list();

        int[][] aa = {{1, 3, 7}, {5}, {4, 6, 9, 10}, {10, 11}};
//        int[][] emptyempty = {{}};

        IntListList c  = Lists.naturalRuns(a);
//        IntListList d  = Lists.naturalRuns(empty);

        assertEquals(IntListList.list(aa), c);
//        assertEquals(IntListList.list(empty), d);
    }
    // It might initially seem daunting to try to set up
    // IntListList expected.
    //
    // There is an easy way to get the IntListList that you want in just
    // few lines of code! Make note of the IntListList.list method that
    // takes as input a 2D array.

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ListsTest.class));
    }
}
