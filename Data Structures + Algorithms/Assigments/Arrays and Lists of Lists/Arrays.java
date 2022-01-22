package arrays;

/* NOTE: The file Arrays/Utils.java contains some functions that may be useful
 * in testing your answers. */

/** HW #2 */

/** Array utilities.
 *  @author
 */
class Arrays {

    /* C1. */
    /** Returns a new array consisting of the elements of A followed by the
     *  the elements of B. */
    static int[] catenate(int[] A, int[] B) {
        int[] mergedarray = new int[A.length + B.length];
        if(A == null) {
            return B;
        }
        else if (B == null) {
            return A;
        }
        else {
            int n = 0;
            while (n < A.length){
                mergedarray[n] = A[n];
                n+=1;
            }
            int m = 0;
            while (m<B.length){
                mergedarray[n] = B[m];
                m+=1;
                n+=1;
            }
        }
        return mergedarray;
    }

    /* C2. */
    /** Returns the array formed by removing LEN items from A,
     *  beginning with item #START. If the start + len is out of bounds for our array, you
     *  can return null.
     *  Example: if A is [0, 1, 2, 3] and start is 1 and len is 2, the
     *  result should be [0, 3]. */
    static int[] remove(int[] A, int start, int len) {
        int[] removedarray = new int[A.length - len];
        if (start + len >A.length){
            return null;
        } else {
            int j = 0;
            while (j < start) {
                removedarray[j] = A[j];
                j += 1;
            }
            int k = j+len;
            while (k < A.length){
                removedarray[j] = A[k];
                k+=1;
                j+=1;
            }
        }
        return removedarray;
    }

    /* C3. */
    /** Returns the array of arrays formed by breaking up A into
     *  maximal ascending lists, without reordering.
     *  For example, if A is {1, 3, 7, 5, 4, 6, 9, 10}, then
     *  returns the three-element array
     *  {{1, 3, 7}, {5}, {4, 6, 9, 10}}. */
    static int[][] naturalRuns(int[] A) {

        return null;
    }
}
