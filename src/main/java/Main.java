import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
      Scanner sc = new Scanner(System.in);

      while (true) {
          System.out.print("$ ");
          String input = sc.nextLine();
          
          String[] inputParts = input.trim().split(" ");
          String command = inputParts[0];
          String arguments = String.join(" ", Arrays.copyOfRange(inputParts, 1, inputParts.length));

          if (command.equals("exit")) {
              break;
          } else if (command.equals("echo")) {
              System.out.println(arguments);
          } else if (command.equals("type")) {
              System.out.println(type(arguments.trim()));
          } else
              System.out.println(input + ": command not found");
      }

      sc.close();
    }
    public static String type(String command) {
        String builtIns = {"echo", "exit", "type"};

        for (int i = 0; i < builtIns.length; i++) {
            if (command.equals(builtIns[i])) {
                return command + " is a shell builtin";
            }
        }

        String path = System.getenv("PATH");
        String[] pathDirs = path.split(File.Separator);

        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i], command);

            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }

        return command + ": not found";
    }
}
