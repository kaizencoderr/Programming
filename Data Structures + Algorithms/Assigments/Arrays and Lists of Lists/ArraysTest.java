package arrays;

import org.junit.Test;
import static org.junit.Assert.*;

/** FIXME
 *  @author FIXME
 */

public class ArraysTest {
    /** FIXME
     */
    @Test
    public void testCatenate() {
        int[] x = {1,2,3};
        int[] y = {4,5,6};
        int[] z = {1,2,3,4,5,6};
        assertArrayEquals(z, Arrays.catenate(x,y));

        int[] list1 = {8,7,6,5};
        int[] list2 = {4,3,2,1};
        int[] list1and2 = {8,7,6,5,4,3,2,1};
        assertArrayEquals(list1and2, Arrays.catenate(list1, list2));

        int[] empty1 = {};
        int[] empty2 = {};
        int[] empty3 = {};
        assertArrayEquals(empty3, Arrays.catenate(empty1, empty2));
    }

    @Test
    public void testRemove(){
        int[] y = {0,1,2,3};
        int[] z = {0,3};
        assertArrayEquals(z, Arrays.remove(y,1,2));

        int[] a = {0,10,20,-30,40,50,60,70};
        int[] b = {0,10,50,60,70};
        assertArrayEquals(b, Arrays.remove(a,2,3));
    }

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ArraysTest.class));
    }
}
