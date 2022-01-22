package enigma;

/**
 * Represents a permutation of a range of integers starting at 0 corresponding
 * to the characters of an alphabet. For lab6, this is made an abstract
 * class so we don't give you the solutions. In proj1, this is a concrete
 * class you will need to implement.
 * @author Michelle Hwang
 */
class Permutation {
    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        // Fix me
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
        char map = _alphabet.toChar(p % size());
        char newmap = _alphabet.toChar(p % size());
        String[] cyclearr = _cycles.split("\\)", 1000);
        for (String a : cyclearr) {
            for (int j = 1; j < a.length(); j++) {
                if (map == a.charAt(j) && j + 1 < a.length()) {
                    newmap = a.charAt(j + 1);
                } else if (map == a.charAt(j) && j + 1 == a.length()) {
                    newmap = a.charAt(1);
                }
            }
        }
        return _alphabet.toInt(newmap);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char map = _alphabet.toChar(c % size());
        char newmap = _alphabet.toChar(c % size());
        String[] cyclearr = _cycles.split("\\)", 1000);
        for (String a : cyclearr) {
            for (int j = 1; j < a.length(); j++) {
                if (map == a.charAt(j) && j > 1) {
                    newmap = a.charAt(j - 1);
                } else if (map == a.charAt(j) && j == 1) {
                    newmap = a.charAt(a.length() - 1);
                }
            }
        }
        return _alphabet.toInt(newmap); // FIXME
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int map = _alphabet.toInt(p);
        int newmap = this.permute(map);
        return _alphabet.toChar(newmap);  // FIXME
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int map = _alphabet.toInt(c);
        int newmap = this.invert(map);
        return _alphabet.toChar(newmap);  // FIXME
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
        return true;  // FIXME
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    private String _cycles;

    // FIXME: ADDITIONAL FIELDS HERE, AS NEEDED
}
