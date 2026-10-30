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

            String[] allTokens = parseArguments(input);
            if (allTokens.length == 0) {
                continue;
            }

            String outputFile = null;
            String errorFile = null;
            boolean appendMode = false;
            String[] tokens = allTokens;

            int redirectIndex = -1;
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].equals(">") || tokens[i].equals("1>")) {
                    redirectIndex = i;
                    if (redirectIndex + 1 < tokens.length) {
                        outputFile = tokens[i + 1];
                    }
                }
                if (tokens[i].equals("2>")) {
                    redirectIndex = i;
                    if (redirectIndex + 1 < tokens.length) {
                        errorFile = tokens[i + 1];
                    }
                }
                if (tokens[i].equals("1>>") || tokens[i].equals(">>")) {
                    redirectIndex = i;
                    if (redirectIndex + 1 < tokens.length) {
                        outputFile = tokens[i + 1];
                    }
                    appendMode = true;
                }
                if (tokens[i].equals("2>>")) {
                    redirectIndex = i;
                    if (redirectIndex + 1 < tokens.length) {
                        errorFile = tokens[i + 1];
                    }
                    appendMode = true;
                }
            }
            if (redirectIndex != -1) {
                tokens = Arrays.copyOfRange(allTokens, 0, redirectIndex);
            }

            String cmd = tokens[0];
            String[] cmdArgs = Arrays.copyOfRange(tokens, 1, tokens.length);
            
            PrintStream out = System.out;
            PrintStream err = System.err;
            if (outputFile != null) {
                out = new PrintStream(new FileOutputStream(outputFile, appendMode));
            }
            if (errorFile != null) {
                err = new PrintStream(new FileOutputStream(errorFile, appendMode));
            }

            if (cmd.equals("exit")) {
                break;
            } else if (cmd.equals("echo")) {
                out.println(String.join(" ", cmdArgs));
            } else if (cmd.equals("type")) {
                out.println(type(cmdArgs.length > 0 ? cmdArgs[0] : ""));
            } else if (cmd.equals("pwd")) {
                out.println(System.getProperty("user.dir"));
            } else if (cmd.equals("cd")) {
                changeDirectory(cmdArgs.length > 0 ? cmdArgs[0] : "", err);
            } else {
                if (isExecutable(cmd)) {
                    execute(tokens, outputFile, errorFile, appendMode);
                } else {
                    err.println(cmd + ": command not found");
                }
            }

            if (outputFile != null) {
                out.close();
            }
            if (errorFile != null) {
                err.close();
            }
        }

        sc.close();
    }

    private static void changeDirectory(String target, PrintStream err) throws IOException {
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
            err.println("cd: " + target + ": No such file or directory");
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

    private static void execute(String[] fullCommand, String outputFile, String errorFile, boolean appendMode) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(fullCommand);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);

        if (outputFile != null) {
            pb.redirectOutput(appendMode 
                ? ProcessBuilder.Redirect.appendTo(new File(outputFile))
                : ProcessBuilder.Redirect.to(new File(outputFile)));
        } else {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        if (errorFile != null) {
            pb.redirectError(appendMode 
                ? ProcessBuilder.Redirect.appendTo(new File(errorFile))
                : ProcessBuilder.Redirect.to(new File(errorFile)));
        } else {
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        }

        pb.start().waitFor();
    }
}
