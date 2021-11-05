package enigma;

import java.util.ArrayList;
import java.util.Collection;


import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Tyler Freund
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        if (numRotors <= pawls || numRotors <= 1 || pawls < 0) {
            throw new EnigmaException("wrong rotor or pawl num");
        }
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = new ArrayList<Rotor>(allRotors);
        _rotors = new Rotor[numRotors];
        _plugboard = new Permutation("", alpha);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (int x = 0; x < rotors.length; x++) {
                if (rotors[x] == rotors[i] && i != x) {
                    throw new EnigmaException("no duplicates");
                }
            }
        }
        int i = 0;
        for (String name: rotors) {
            for (Rotor r : _allRotors) {
                String rotorname = r.name();
                String upper = r.name().toUpperCase();
                if (rotorname.equals(name) || upper.equals(name)) {
                    _rotors[i] = r;
                    i++;
                }
            }
        }
        if (!_rotors[0].reflecting()) {
            throw new EnigmaException("first rotor must reflect");
        }
        for (int y = 0; y < _rotors.length; y++) {
            if (_rotors[y].rotates()) {
                for (int z = y; z < _rotors.length; z++) {
                    if (!_rotors[z].rotates()) {
                        throw new EnigmaException("no fixed after moving");
                    }
                }

            }
        }
    }


    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != 4) {
            throw new EnigmaException("setting must be 4 len");
        }
        if (numRotors() - 1 != setting.length()) {
            throw new EnigmaException("Setting must be len numRotors-1");
        }
        for (int j = 0; j < setting.length(); j++) {
            if (!_alphabet.contains(setting.charAt(j))) {
                throw new EnigmaException("Setting must be part of alphabet");
            }
            if (!_rotors[j].reflecting()) {
                _rotors[j].set(setting.charAt(j - 1));
            }
        }
        char x = setting.charAt(setting.length() - 1);
        _rotors[_rotors.length - 1].set(x);
    }


    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        int tracker = c;
        boolean[] advancers = new boolean[_rotors.length];
        for (int i = _numRotors - numPawls(); i < _rotors.length - 1; i++) {
            if (_rotors[i + 1].atNotch()) {
                advancers[i] = true;
            } else if (_rotors[i - 1].rotates() && _rotors[i].atNotch()) {
                advancers[i] = true;
            }
        }
        advancers[advancers.length - 1] = true;
        for (int g = 0; g < advancers.length; g++) {
            if (advancers[g]) {
                _rotors[g].advance();
            }
        }
        if (_plugboard != null) {
            tracker = _plugboard.permute(tracker);
        }
        for (int j = _rotors.length - 1; j > 0; j--) {
            tracker = _rotors[j].convertForward(tracker);
        }
        for (int z = 0; z < _rotors.length; z++) {
            tracker = _rotors[z].convertBackward(tracker);
        }
        if (_plugboard != null) {
            tracker = _plugboard.invert(tracker);
        }
        return tracker;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        char[] result = new char[msg.length()];
        String res = "";
        for (int i = 0; i < msg.length(); i++) {
            int x = this.convert(_alphabet.toInt(msg.charAt(i)));
            result[i] = _alphabet.toChar(x);
        }
        for (char a : result) {
            res += a;
        }
        return res;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of Rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _pawls;

    /** Collection of ALL Rotors. */
    private ArrayList<Rotor> _allRotors;

    /** Rotor Array of my rotors. */
    private Rotor[] _rotors;

    /** Plugboard of my machine. */
    private Permutation _plugboard;
}
