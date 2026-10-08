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
              System.out.println(arguments);
          } else if (command.equals("type")) {
              System.out.println(type(arguments));
          } else
              System.out.println(input + ": command not found");
      }

      sc.close();
    }

    public static String type(String command) {
        String[] builtIns = {"echo", "exit", "type"};

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
}
