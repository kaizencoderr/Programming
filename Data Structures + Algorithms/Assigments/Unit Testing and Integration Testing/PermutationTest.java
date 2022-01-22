package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/**
 * The suite of all JUnit tests for the Permutation class. For the purposes of
 * this lab (in order to test) this is an abstract class, but in proj1, it will
 * be a concrete class. If you want to copy your tests for proj1, you can make
 * this class concrete by removing the 4 abstract keywords and implementing the
 * 3 abstract methods.
 *
 *  @author Tyler Freund
 */
public abstract class PermutationTest {

    /**
     * For this lab, you must use this to get a new Permutation,
     * the equivalent to:
     * new Permutation(cycles, alphabet)
     * @return a Permutation with cycles as its cycles and alphabet as
     * its alphabet
     * @see Permutation for description of the Permutation conctructor
     */
    abstract Permutation getNewPermutation(String cycles, Alphabet alphabet);

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet(chars)
     * @return an Alphabet with chars as its characters
     * @see Alphabet for description of the Alphabet constructor
     */
    abstract Alphabet getNewAlphabet(String chars);

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet()
     * @return a default Alphabet with characters ABCD...Z
     * @see Alphabet for description of the Alphabet constructor
     */
    abstract Alphabet getNewAlphabet();

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /** Check that PERM has an ALPHABET whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha,
                           Permutation perm, Alphabet alpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.toInt(c), ei = alpha.toInt(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        Alphabet alpha = getNewAlphabet();
        Permutation perm = getNewPermutation("", alpha);
        checkPerm("identity", UPPER_STRING, UPPER_STRING, perm, alpha);
    }
    @Test
    public void testPermute() {
        Alphabet alph = getNewAlphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String cyc = new String("(ABCD) (EFG)");
        Permutation perm = getNewPermutation(cyc, alph);

        assertEquals(3, perm.permute(2));
        assertEquals(0, perm.permute(3));
    }
    @Test
    public void testInvert() {
        Alphabet alph = getNewAlphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String cyc = new String("(ABCD) (EFG)");
        Permutation perm = getNewPermutation(cyc, alph);

        assertEquals(1, perm.invert(2));
        assertEquals(3, perm.invert(0));
    }
    @Test
    public void testPermute1() {
        Alphabet alph = getNewAlphabet("!@#$%^&*");
        String cyc = new String("($)");
        Permutation perm = getNewPermutation(cyc, alph);

        assertEquals('!', perm.permute('!'));
        assertEquals('$', perm.permute('$'));
    }
    @Test
    public void testInvert1() {
        Alphabet alph = getNewAlphabet("!@#$%^&*");
        String cyc = new String("(^%)");
        Permutation perm = getNewPermutation(cyc, alph);

        assertEquals('%', perm.invert('^'));
        assertEquals('&', perm.invert('&'));
    }
    @Test
    public void testSize() {
        Alphabet alph = getNewAlphabet("!@#$%^&*");
        String cyc = new String("(^%)");
        Permutation perm = getNewPermutation(cyc, alph);
        assertEquals(8, perm.size());

        Alphabet alpha = getNewAlphabet("");
        String cycl = new String("");
        Permutation permu = getNewPermutation(cycl, alpha);
        assertEquals(0, permu.size());
    }
    @Test
    public void testDerangement() {
        Alphabet alph = getNewAlphabet("!@#$%^&*");
        String cyc = new String("(^)");
        Permutation perm = getNewPermutation(cyc, alph);
        assertFalse(perm.derangement());
    }
    @Test
    public void testDerangementtrue() {
        Alphabet alph = getNewAlphabet("!@#$%^&*");
        String cyc = new String("(@!) ($#) (^%) (*&)");
        Permutation perm = getNewPermutation(cyc, alph);
        assertTrue(perm.derangement());
    }
    @Test
    public void testInvertPermute() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals('D',p.invert('B'));
        assertEquals('C',p.permute('A'));
    }


    // FIXME: Add tests here that pass on a correct Permutation and fail on buggy Permutations.
}
