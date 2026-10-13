import java.util.*;
import java.io.*;

public class Main {
    private static final String[] BUILTINS = {"echo", "exit", "type", "pwd", "cd"};

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            if (!sc.hasNextLine()) {
                break;
            }

            String input = sc.nextLine();
            int spaceIndex = input.indexOf(" ");
            String command = spaceIndex == -1 ? input : input.substring(0, spaceIndex);
            String arguments = spaceIndex == -1 ? "" : input.substring(spaceIndex + 1).trim();

            if (command.equals("exit")) {
                break;
            } else if (command.equals("echo")) {
                String[] parts = parseArguments(arguments);
                System.out.println(String.join(" ", parts));
            } else if (command.equals("type")) {
                System.out.println(type(arguments));
            } else if (command.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            } else if (command.equals("cd")) {
                changeDirectory(arguments);
            } else {
                // Parse the whole line once; use the same tokens for lookup and launch.
                String[] inputParts = parseArguments(input);
                if (inputParts.length == 0) {
                    continue;
                }

                if (isExecutable(inputParts[0])) {
                    execute(inputParts);
                } else {
                    System.out.println(inputParts[0] + ": command not found");
                }
            }
        }

        sc.close();
    }

    private static void changeDirectory(String target) throws IOException {
        File file;
        if (target.equals("~")) {
            file = new File(System.getenv("HOME"));
        } else if (target.startsWith("/")) {
            file = new File(target).getCanonicalFile();
        } else {
            file = new File(System.getProperty("user.dir"), target).getCanonicalFile();
        }

        if (file.exists() && file.isDirectory()) {
            System.setProperty("user.dir", file.getAbsolutePath());
        } else {
            System.out.println("cd: " + target + ": No such file or directory");
        }
    }

    private static String[] parseArguments(String arguments) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < arguments.length(); i++) {
            char ch = arguments.charAt(i);

            if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (ch == '\\' && !inSingleQuote && !inDoubleQuote) {
                if (i + 1 < arguments.length()) {
                    current.append(arguments.charAt(i + 1));
                    i++;
                }
            } else if (ch == '\\' && inDoubleQuote) {
                if (i + 1 < arguments.length()) {
                    char nextCh = arguments.charAt(i + 1);

                    if (nextCh == '\\' || nextCh == '"') {
                        current.append(nextCh);
                        i++;
                    } else {
                        current.append(ch);
                    }
                } else {
                    current.append(ch);
                }
            } else if (Character.isWhitespace(ch) && !inSingleQuote && !inDoubleQuote) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(ch);
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens.toArray(new String[0]);
    }

    public static String type(String command) {
        for (String builtin : BUILTINS) {
            if (command.equals(builtin)) {
                return command + " is a shell builtin";
            }
        }

        File found = findInPath(command);
        if (found != null) {
            return command + " is " + found.getAbsolutePath();
        }

        return command + ": not found";
    }

    private static File findInPath(String command) {
        String path = System.getenv("PATH");
        if (path == null) {
            return null;
        }

        for (String dir : path.split(File.pathSeparator)) {
            File file = new File(dir, command);
            if (file.exists() && file.canExecute()) {
                return file;
            }
        }

        return null;
    }

    private static boolean isExecutable(String command) {
        return findInPath(command) != null;
    }

    private static void execute(String[] fullCommand) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(fullCommand);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.inheritIO();
        pb.start().waitFor();
    }
}