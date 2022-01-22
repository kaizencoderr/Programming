package enigma;
import org.junit.Test;
import static org.junit.Assert.*;

import static enigma.EnigmaException.*;
/** A series of unit tests for the Alphabet Class methods.
 *  @author Tyler Freund
 */

public class MyTests {
    @Test
    public void testSize() {
        Alphabet t1 = new Alphabet("abcdefghij");
        Alphabet t2 = new Alphabet("#!^<>");
        Alphabet t3 = new Alphabet("");

        assertEquals(10, t1.size());
        assertEquals(5, t2.size());
        assertEquals(0, t3.size());
    }
    @Test
    public void testContains() {
        Alphabet t1 = new Alphabet("abcdefghij");
        Alphabet t2 = new Alphabet("#!^<>");
        Alphabet t3 = new Alphabet("");

        assertEquals(true, t1.contains('d'));
        assertEquals(true, t2.contains('!'));
        assertEquals(false, t3.contains('!'));
        assertEquals(false, t1.contains('y'));
        assertEquals(false, t1.contains('A'));
    }
    @Test
    public void testToChar() {
        Alphabet t1 = new Alphabet("abcdefghij");
        Alphabet t2 = new Alphabet("#!^<>");

        assertEquals('a', t1.toChar(0));
        assertEquals('!', t2.toChar(1));
        assertEquals('j', t1.toChar(9));
        assertEquals('>', t2.toChar(4));
    }
    @Test
    public void testToInt() {
        Alphabet t1 = new Alphabet("abcdefghij");
        Alphabet t2 = new Alphabet("#!^<>");

        assertEquals(1, t1.toInt('b'));
        assertEquals(4, t2.toInt('>'));
        assertEquals(8, t1.toInt('i'));
        assertEquals(1, t2.toInt('!'));
    }
    @Test
    public void testPermute() {
        Alphabet alph = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String cyc = new String("(AELTPHQXRU) (BKNW)"
                + " (CMOY) (DFG) (IV) (JZ) (S)");
        Permutation perm = new Permutation(cyc, alph);

        assertEquals(5, perm.permute(3));
        assertEquals(4, perm.permute(0));
    }
    @Test
    public void testInvert() {
        Alphabet alph = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String cyc = new String("(ABCD) (EFG)");
        Permutation perm = new Permutation(cyc, alph);

        assertEquals(1, perm.invert(2));
        assertEquals(3, perm.invert(0));
    }
    @Test
    public void testPermute4() {
        Alphabet alph = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String cyc = new String("(ABCD) (EFG)");
        Permutation perm = new Permutation(cyc, alph);

        assertEquals(3, perm.permute(2));
        assertEquals(0, perm.permute(3));
    }
    @Test
    public void testInvert4() {
        Alphabet alph = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String cyc = new String("(ABCD) (EFG)");
        Permutation perm = new Permutation(cyc, alph);

        assertEquals(1, perm.invert(2));
        assertEquals(3, perm.invert(0));
    }
    @Test
    public void testPermute1() {
        Alphabet alph = new Alphabet("!@#$%^&*");
        String cyc = new String("($)");
        Permutation perm = new Permutation(cyc, alph);

        assertEquals('!', perm.permute('!'));
        assertEquals('$', perm.permute('$'));
    }
    @Test
    public void testInvert1() {
        Alphabet alph = new Alphabet("!@#$%^&*");
        String cyc = new String("(^%)");
        Permutation perm = new Permutation(cyc, alph);

        assertEquals('%', perm.invert('^'));
        assertEquals('&', perm.invert('&'));
    }
    @Test
    public void testSize4() {
        Alphabet alph = new Alphabet("!@#$%^&*");
        String cyc = new String("(^%)");
        Permutation perm = new Permutation(cyc, alph);
        assertEquals(8, perm.size());

        Alphabet alpha = new Alphabet("");
        String cycl = new String("");
        Permutation permu = new Permutation(cycl, alpha);
        assertEquals(0, permu.size());
    }
    @Test
    public void testDerangement() {
        Alphabet alph = new Alphabet("!@#$%^&*");
        String cyc = new String("(^)");
        Permutation perm = new Permutation(cyc, alph);
        assertFalse(perm.derangement());
    }
    @Test
    public void testDerangementtrue() {
        Alphabet alph = new Alphabet("!@#$%^&*");
        String cyc = new String("(@!) ($#) (^%) (*&)");
        Permutation perm = new Permutation(cyc, alph);
        assertTrue(perm.derangement());
    }
    @Test
    public void testInvertPermute() {
        Permutation p = new Permutation("(BACD)", new Alphabet("ABCD"));
        assertEquals('D', p.invert('B'));
        assertEquals('C', p.permute('A'));
    }
    @Test
    public void testAtNotch() {
        Permutation p = new Permutation("(BACD)", new Alphabet("ABCD"));
        Rotor rr = new MovingRotor("rotorname", p, "A");

        Rotor xx = new MovingRotor("rotorname", p, "A");
        xx.set(2);

        assertTrue(rr.atNotch());
        assertFalse(xx.atNotch());
    }

    @Test
    public void testconvertforward() {
        String cycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        Alphabet alpha = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation p = new Permutation(cycles, alpha);
        Rotor rr = new Rotor("roto", p);

        assertEquals(3, rr.convertForward(6));
        assertEquals(20, rr.convertBackward(0));
    }

}
