import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VirtualMachine {

    private static File fileName;
    private static File fileOut;
    
    public static void main(String[] args) throws IOException {
        fileName = new File(args[0]);

        ArrayList<File> files = new ArrayList<>();
        if (args.length != 1) {
            throw new IllegalArgumentException("Inaccurate usage.");
        } else if (fileName.isFile() && !(args[0].endsWith(".vm"))) {
            throw new IllegalArgumentException("Not the correct file type.");
        } else {
            if (fileName.isFile() && args[0].endsWith(".vm")) {
                files.add(fileName);
                String firstPart = args[0].substring(0, args[0].length() - 3);
                fileOut = new File(firstPart + ".asm");
            } else // fileName is a directory - access all files in the directory
            {
                files = getVMFiles(fileName);
                fileOut = new File(fileName + ".asm");
            }
        }        

        CodeWriter codeWriter = new CodeWriter(fileOut);
        
        // comment out line below for the tests that do not require bootstrapping code
        codeWriter.writeInit();
        
        files.forEach((file) -> {
            codeWriter.setFileName(file);
            // construct parser to parse VM input files - use a separate parser for handling each input file
            Parser parser = new Parser(file);
            // march through VM commands in input file, generate assembly code
            while (parser.hasMoreCommands())  {
                parser.advance();
                if (parser.commandType().equals("ARITHMETIC")) {
                    try{
                        codeWriter.writeArithmetic(parser.arg1());
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                } else if (parser.commandType().equals("PUSH") || parser.commandType().equals("POP")) {
                    codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                } else if (parser.commandType().equals("LABEL")) {
                    codeWriter.writeLabel(parser.arg1());
                } else if (parser.commandType().equals("GOTO")) {
                    codeWriter.writeGoto(parser.arg1());
                } else if (parser.commandType().equals("IF")) {
                    codeWriter.writeIf(parser.arg1());
                } else if (parser.commandType().equals("FUNCTION")) {
                    codeWriter.writeFunction(parser.arg1(), parser.arg2());
                } else if (parser.commandType().equals("RETURN")) {
                    codeWriter.writeReturn();
                } else if (parser.commandType().equals("CALL")) {
                    codeWriter.writeCall(parser.arg1(), parser.arg2());
                }
            }

        });

        codeWriter.close();

    }

    // gather all files in the directory argument into an arraylist
    public static ArrayList<File> getVMFiles(File directory) {
        File[] files = directory.listFiles();
        ArrayList<File> fResults = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".vm")) {
                    fResults.add(file);
                }
            }
        }        
        return fResults;

    }
}