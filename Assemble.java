import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Assemble {
    private static Map<String, String> dest = new HashMap<>();
    private static Map<String, String> comp = new HashMap<>();
    private static Map<String, String> jump = new HashMap<>();
    private static Map<String, Integer> symbols = new LinkedHashMap<>();

    static {
        // Initialize dest, comp, and jump tables
        dest.put(null, "000");
        dest.put("M=", "001");
        dest.put("D=", "010");
        dest.put("MD=", "011");
        dest.put("A=", "100");
        dest.put("AM=", "101");
        dest.put("AD=", "110");
        dest.put("AMD=", "111");

        comp.put("0", "0101010");
        comp.put("1", "0111111");
        comp.put("-1", "0111010");
        comp.put("D", "0001100");
        comp.put("A", "0110000");
        comp.put("M", "1110000");
        comp.put("!D", "0001101");
        comp.put("!A", "0110001");
        comp.put("!M", "1110001");
        comp.put("-D", "0001111");
        comp.put("-A", "0110011");
        comp.put("-M", "1110011");
        comp.put("D+1", "0011111");
        comp.put("A+1", "0110111");
        comp.put("M+1", "1110111");
        comp.put("D-1", "0001110");
        comp.put("A-1", "0110010");
        comp.put("M-1", "1110010");
        comp.put("D+A", "0000010");
        comp.put("D+M", "1000010");
        comp.put("D-A", "0010011");
        comp.put("D-M", "1010011");
        comp.put("A-D", "0000111");
        comp.put("M-D", "1000111");
        comp.put("D&A", "0000000");
        comp.put("D&M", "1000000");
        comp.put("D|A", "0010101");
        comp.put("D|M", "1010101");

        jump.put(null, "000");
        jump.put(";JGT", "001");
        jump.put(";JEQ", "010");
        jump.put(";JGE", "011");
        jump.put(";JLT", "100");
        jump.put(";JNE", "101");
        jump.put(";JLE", "110");
        jump.put(";JMP", "111");

        symbols.put("SP", 0);
        symbols.put("LCL", 1);
        symbols.put("ARG", 2);
        symbols.put("THIS", 3);
        symbols.put("THAT", 4);
        symbols.put("SCREEN", 16384);
        symbols.put("KBD", 24576);
        for (int i = 0; i < 16; i++) {
            symbols.put("R" + i, i);
        }
    }

    public static void main(String[] args) {
        List<String> aCommands = new ArrayList<>();
        List<String> asm = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter file name: ");
            String fileName = scanner.nextLine();
            BufferedReader asmFile = new BufferedReader(new FileReader(fileName + ".asm"));

            String line;
            while ((line = asmFile.readLine()) != null) {
                line = line.replaceAll("//.*", "");
                line = line.trim();
                System.out.println("LINE :: " + line);
                if (!line.isEmpty()) {
                    aCommands.add(line);
                }
            }
            asmFile.close();

            int lineNo = 0;
            for (String command : aCommands) {
                Pattern pattern = Pattern.compile("\\(.+\\)");
                Matcher matcher = pattern.matcher(command);
                if (matcher.find()) {
                    String symbol = matcher.group().substring(1, matcher.group().length() - 1);
                    if (!symbols.containsKey(symbol)) {
                        symbols.put(symbol, lineNo);
                        lineNo--;
                    }
                }
                lineNo++;
            }

            for (String aCommand : aCommands) {
                aCommand = aCommand.replaceAll("\\(.+\\)", "");
                if (!aCommand.isEmpty()) {
                    asm.add(aCommand);
                }
            }

            int variableNo = 16;
            for (String command : asm) {
                Pattern symbolPattern = Pattern.compile("@[a-zA-Z]+.*");
                Matcher symbolMatcher = symbolPattern.matcher(command);
                if (symbolMatcher.find()) {
                    String symbol = symbolMatcher.group().substring(1);
                    if (!symbols.containsKey(symbol)) {
                        symbols.put(symbol, variableNo);
                        variableNo++;
                    }
                }
            }

            BufferedWriter hackFile = new BufferedWriter(new FileWriter(fileName + ".hack"));
            for (String command : asm) {
                if (command.charAt(0) == '@') {
                    int address = 0;
                    String symbol = command.substring(1);
                    if (symbols.containsKey(symbol)) {
                        address = symbols.get(symbol) + 32768;
                    } else {
                        address = Integer.parseInt(symbol) + 32768;
                    }
                    hackFile.write("0" + Integer.toBinaryString(address).substring(1) + "\n");
                } else {
                    Pattern destPattern = Pattern.compile(".+=");
                    Pattern jumpPattern = Pattern.compile(";.+");

                    Matcher destMatcher = destPattern.matcher(command);
                    Matcher jumpMatcher = jumpPattern.matcher(command);

                    String destPart = null, jumpPart = null;

                    if (destMatcher.find()) {
                        destPart = destMatcher.group();
                    }

                    if (jumpMatcher.find()) {
                        jumpPart = jumpMatcher.group();
                    }

                    String compPart = command.replaceAll(".+=|;.+", "");

                    String d = dest.get(destPart);
                    String j = jump.get(jumpPart);
                    String c = comp.get(compPart);

                    System.out.println("Command: " + command);
                    System.out.println(destPart + " " + d);
                    System.out.println(jumpPart + " " + j);
                    System.out.println(compPart + " " + c);

                    hackFile.write("111" + c + d + j + "\n");
                }
            }
            hackFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}