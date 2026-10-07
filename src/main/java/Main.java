import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
      Scanner sc = new Scanner(System.in);
      String[] builtIns = {"echo", "exit", "type"};
      while (true) {
          System.out.print("$ ");
          String input = sc.nextLine();
          String[] inputParts = input.trim().split(" ");
          String command = inputParts[0];
          String arguments = String.join(" ", Arrays.copyOfRange(inputParts, 1, inputParts.length));
          if (command.equals("exit")) {
              System.exit(0);
          } else if (command.equals("echo")) {
              System.out.println(arguments);
          } else if (command.equals("type")) {
              for (int i = 0; i < builtIns.length; i++) {
                  if (arguments.trim().equals(builtIns[i])) {
                      System.out.println(builtIns[i] + " is a shell builtin");
                      break;
                  }
              }
              System.out.println(arguments.trim() + ": not found");
          } else
              System.out.println(input + ": command not found");
      }
    }
}
