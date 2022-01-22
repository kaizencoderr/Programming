package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Tyler Freund
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
        case "init":
            Commandclass.init();
            break;
        case "add":
            Commandclass.add(args[1]);
            break;
        case "commit":
            if (args[1].length() == 0) {
                System.out.println("Please enter a commit message.");
            }
            Commandclass.commit(args[1]);
            break;
        case "checkout":
            if (args.length == 3) {
                Commandclass.checkout1(args[2]);
                break;
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                }
                Commandclass.checkout2(args[1], args[3]);
                break;
            } else {
                Commandclass.checkout3(args[1]);
                break;
            }
        default:
            main2(args);
        }
    }

    /** An extension of the above main method.
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....  */
    public static void main2(String[] args) {
        switch (args[0]) {
        case "log":
            Commandclass.log();
            break;
        case "global-log":
            Commandclass.globallog();
            break;
        case "rm":
            Commandclass.rm(args[1]);
            break;
        case "find":
            Commandclass.find(args[1]);
            break;
        case "branch":
            Commandclass.branch(args[1]);
            break;
        case "status":
            Commandclass.status();
            break;
        case "rm-branch":
            Commandclass.rmbranch(args[1]);
            break;
        case "reset":
            Commandclass.reset(args[1]);
            break;
        case "merge":
            Commandclass.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }


}
