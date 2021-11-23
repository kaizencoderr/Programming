package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;
import java.util.Collection;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }
        _config = getInput(args[0]);
        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }
        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine m = readConfig();
        String forward = _input.nextLine();
        while (_input.hasNext()) {
            String setting = forward;
            setUp(m, setting);
            forward = (_input.nextLine()).toUpperCase();
            while (!(forward.contains("*"))) {
                String result = m.convert(forward.replaceAll(" ", ""));
                if (forward.isEmpty()) {
                    _output.println();
                } else {
                    printMessageLine(result);
                }
                if (!_input.hasNext()) {
                    forward = "*";
                } else {
                    forward = (_input.nextLine()).toUpperCase();
                }
            }

        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            Alphabet alpha = new Alphabet(_config.next());
            _alphabet = alpha;
            if (_alphabet.toString().contains("*")
                    || _alphabet.toString().contains(")")
                    || _alphabet.toString().contains("(")) {
                throw new EnigmaException("incorrect config format");
            }
            int numrotor = _config.nextInt();
            int numpawl = _config.nextInt();
            Collection<Rotor> allrotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                allrotors.add(readRotor());
            }
            Machine mach = new Machine(alpha, numrotor, numpawl, allrotors);
            return mach;
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String type = _config.next();
            String cycles = "";
            String notches = "";
            Rotor x;
            while (_config.hasNext("\\s*[(].+[)]\\s*")) {
                cycles += " " + _config.next();
            }
            Permutation p = new Permutation(cycles, _alphabet);
            if (type.startsWith("M")) {
                notches += type.substring(1);
                x = new MovingRotor(name, p, notches);
                return x;
            } else if (type.startsWith("N")) {
                x = new FixedRotor(name, p);
                return x;
            } else if (type.startsWith("R")) {
                return new Reflector(name, p);
            } else {
                throw new EnigmaException("Invalid rotor type");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] names = new String[M.numRotors()];
        String plug = "";
        Scanner ss = new Scanner(settings);
        if (ss.hasNext("[*]")) {
            ss.next();
            for (int z = 0; z < M.numRotors(); z++) {
                names[z] = ss.next();
            }
            M.insertRotors(names);
            if (ss.hasNext("\\w{" + (M.numRotors() - 1) + "}")) {
                M.setRotors(ss.next());
            }
            while (ss.hasNext("[(]\\w+[)]")) {
                plug += ss.next() + " ";
            }
            if (plug.length() > 0) {
                M.setPlugboard(new Permutation(plug, _alphabet));
            }
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            int lim = msg.length() - i;
            if (lim <= 5) {
                _output.println(msg.substring(i, i + lim));
            } else {
                _output.print(msg.substring(i, i + 5) + " ");
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
