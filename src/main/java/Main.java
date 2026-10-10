import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
      Scanner sc = new Scanner(System.in);

      while (true) {
          System.out.print("$ ");

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
                String targetDirectory = arguments;
                File file;
                if (targetDirectory.startsWith("/")) {
                    file = new File(targetDirectory);
                } else if (targetDirectory.equals("~")) {
                    file = new File(System.getenv("HOME"));
                } else {
                    file = new File(System.getProperty("user.dir"), targetDirectory).getCanonicalFile();
                }

                if (file.exists() && file.isDirectory()) {
                    System.setProperty("user.dir", file.getAbsolutePath());
                } else {
                    System.out.println("cd: " + targetDirectory + ": No such file or directory");
                }
          } else {
                execute(command, arguments);
            }
      }

      sc.close();
    }

    private static String[] parseArguments(String arguments) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;

        for (int i = 0; i < arguments.length(); i++) {
            char ch = arguments.charAt(i);

            if (ch == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (Character.isWhitespace(ch) && !inSingleQuote) {
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
        String[] builtIns = {"echo", "exit", "type", "pwd"};

        for (int i = 0; i < builtIns.length; i++) {
            if (command.equals(builtIns[i])) {
                return command + " is a shell builtin";
            }
        }

        String path = System.getenv("PATH");
        String[] pathDirs = path.split(File.pathSeparator);

        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i], command);

            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }

        return command + ": not found";
    }

    private static boolean isExecutable(String command) {
        String path = System.getenv("PATH");
        String[] pathDirs = path.split(File.pathSeparator);

        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i], command);

            if (file.exists() && file.canExecute()) {
                return true;
            }
        }

        return false;
    }
    
    private static void execute(String command, String arguments) throws IOException, InterruptedException {
        if (!isExecutable(command)) {
            System.out.println(command + ": command not found");
            return;
        }

        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(command);
        if (!arguments.isEmpty()) {
            fullCommand.addAll(Arrays.asList(arguments.split("\\s+")));
        }

        ProcessBuilder pb = new ProcessBuilder(fullCommand);
        pb.inheritIO();
        pb.start().waitFor();
    }
}
