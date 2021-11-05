package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Tyler Freund
 */
class Permutation {
    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles.split("\\)|\\(|\\ ", cycles.length() * 2);
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char m = _alphabet.toChar(wrap(p));
        char star = _alphabet.toChar(p);
        for (String cycle: _cycles) {
            int cl = cycle.length();
            for (int i = 0; i < cl; i += 1) {
                int z = _alphabet.toInt(m) + 1;
                if (z > _alphabet.size() && cycle.charAt(i) == m) {
                    m = cycle.charAt(0);
                } else if (cycle.charAt(i) == m && cl > 1 && cl - 1 != i) {
                    m = cycle.charAt(i + 1);
                    break;
                } else if (cycle.charAt(i) == m && cl > 1 && i != 0) {
                    m = cycle.charAt(0);
                    break;
                } else if (cycle.charAt(i)  == star && i + 1 > cl - 1) {
                    m = cycle.charAt(0);
                }
            }
        }
        return _alphabet.toInt(m);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char m = _alphabet.toChar(wrap(c));
        char star = _alphabet.toChar(c);
        for (String cycle: _cycles) {
            int cl = cycle.length();
            for (int i = 0; i < cl; i += 1) {
                if (cl == 1) {
                    return _alphabet.toInt(m);
                } else {
                    boolean con1 = _alphabet.toInt(m) + 2 > _alphabet.size();
                    if (con1 && cycle.charAt(i) == m) {
                        if (i > 0) {
                            m = cycle.charAt(i - 1);
                        }
                    } else if (cycle.charAt(i) == m && cl > 1 && i != 0) {
                        if (i > 0) {
                            m = cycle.charAt(i - 1);
                        }
                        break;
                    } else if (cycle.charAt(i) == m && cl > 1 && i == 0) {
                        m = cycle.charAt(cycle.length() - 1);
                        break;
                    } else if (cycle.charAt(i) == star && i + 1 > cl - 1) {
                        if (i > 0) {
                            m = cycle.charAt(i - 1);
                        }
                    }
                }
            }
        }
        return _alphabet.toInt(m);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int mapper = _alphabet.toInt(p);
        int star = this.permute(mapper);
        return _alphabet.toChar(star);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int mapper = _alphabet.toInt(c);
        int star = this.invert(mapper);
        return _alphabet.toChar(star);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < alphabet().size(); i++) {
            if (this.permute(i) == i) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Cycles of this permutation. */
    private String[] _cycles;
}
