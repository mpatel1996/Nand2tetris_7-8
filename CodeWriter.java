
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class CodeWriter {
    private final FileWriter fw;
    private int jumpCounter = 0;
    //private static final String fileName = "";
    private static int nLabelNum = 0;

    // opens output file/stream and gets ready to write into it
    public CodeWriter(File file) throws IOException {
            fw = new FileWriter(file);
    }

    // informs code writer that translation of new VM file is started
    public void setFileName(File file) {

    }

    // writes the assembly code that is the translation of the given arithmetic command
    public void writeArithmetic(String strCommand) throws IOException {
        switch (strCommand) {
            case "add":
                fw.write(getArithFormat1().concat("M=M+D\n"));
                break;
            case "sub":
                fw.write(getArithFormat1().concat("M=M-D\n"));
                break;
            case "neg":
                fw.write("D=0\n@SP\nA=M-1\nM=D-M\n");
                break;
            case "eq":
                fw.write(getArithFormat1().concat(getArithFormat2("JNE")));
                jumpCounter++;
                break;
            case "gt":
                fw.write(getArithFormat1().concat(getArithFormat2("JLE")));
                jumpCounter++;
                break;
            case "lt":
                fw.write(getArithFormat1().concat(getArithFormat2("JGE")));
                jumpCounter++;
                break;
            case "and":
                fw.write(getArithFormat1().concat("M=M&D\n"));
                break;
            case "or":
                fw.write(getArithFormat1().concat("M=M|D\n"));
                break;
            case "not":
                fw.write("@SP\nA=M-1\nM=!M\n");
                break;
            default:
                break;
        }
    }

    // writes the assembly code that is the translation of the given command, where command is either PUSH or POP, given segment and index as well
    public void writePushPop(String strCommand, String strSegment, int nIndex) {
        String strAcode = null;
        if (strCommand.equals("PUSH")) {
            
            if (strSegment.equals("static")) {
                strAcode = getPushFormat2(String.valueOf(16 + nIndex));
            } else if (strSegment.equals("this")) {
                strAcode = getPushFormat1("THIS", nIndex);
            } else if (strSegment.equals("local")) {
                strAcode = getPushFormat1("LCL", nIndex);
            } else if (strSegment.equals("argument")) {
                strAcode = getPushFormat1("ARG", nIndex);
            } else if (strSegment.equals("that")) {
                strAcode = getPushFormat1("THAT", nIndex);
            } else if (strSegment.equals("constant")) {
                strAcode = "@" + nIndex + "\nD=A\n@SP\n=M\nM=D\n@SP\nM=M+1\n";
            } else if (strSegment.equals("pointer") && nIndex == 0) {
                strAcode = getPushFormat2("THIS");
            } else if (strSegment.equals("pointer") && nIndex == 1) {
                strAcode = getPushFormat2("THAT");
            } else if (strSegment.equals("temp")) {
                strAcode = getPushFormat1("R5", nIndex + 5);
            }

        } else if (strCommand.equals("POP")) {
            if (strSegment.equals("static")) {
                strAcode = getPopFormat2(String.valueOf(16 + nIndex));
            } else if (strSegment.equals("this")) {
                strAcode = getPopFormat1("THIS", nIndex);
            } else if (strSegment.equals("local")) {
                strAcode = getPopFormat1("LCL", nIndex);
            } else if (strSegment.equals("argument")) {
                strAcode = getPopFormat1("ARG", nIndex);
            } else if (strSegment.equals("that")) {
                strAcode = getPopFormat1("THAT", nIndex);
            } else if (strSegment.equals("constant")) {
                strAcode = new StringBuilder().append("@").append(nIndex).append("\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n").toString();

            } else if (strSegment.equals("pointer") && nIndex == 0) {
                strAcode = getPopFormat2("THIS");
            } else if (strSegment.equals("pointer") && nIndex == 1) {
                strAcode = getPopFormat2("THAT");
            } else if (strSegment.equals("temp")) {
                strAcode = getPopFormat1("R5", nIndex + 5);

            }
        }

        try {
            fw.write(strAcode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // get format for arithmetic command - applies to all except for neg and not
    public String getArithFormat1() {
        return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n";
    }

    // get 2nd part of format for arithmetic commands - only for et, gt, and lt
    public String getArithFormat2(String strJump) {
        return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n" +
                "D=M-D\n" +
                "@FALSE" + jumpCounter + "\n" +
                "D;" + strJump + "\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=-1\n" +
                "@CONTINUE" + jumpCounter + "\n" +
                "0;JMP\n" +
                "(FALSE" + jumpCounter + ")\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=0\n" +
                "(CONTINUE" + jumpCounter + ")\n";

    }
    
    // get format for pushing onto stack given the segment and index - for this, local, argument, that, and temp
    public String getPushFormat1(String strSegment, int nIndex) {        
        return "@"+strSegment+ "\nD=M\n@" + nIndex + "\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
    }

    // get format for pushing onto stack given the segment - for static & pointer
    public String getPushFormat2(String strSegment) {
        return "@"+strSegment+ "\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
    }

    // get format for popping off of stack given the segment and index - for this, local, argument, that, and temp
    public String getPopFormat1(String strSegment, int nIndex) {
        return "@" + strSegment + "\nD=M\n@" + nIndex + "\nD=D+A\n@R13\nM=D\n" +
                "@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n";
    }

    // get format for popping off of stack given the segment - for static & pointer
    public String getPopFormat2(String strSegment) {
        return "@" + strSegment + "\nD=A\n@R13\nM=D\n@SP\nAM=M+1\nD=M\n" +
                "@R13\nA=M\nM=D\n";
    }

    // closes output file
    public void close() throws IOException {
       fw.close();
    }
    
    // ************************
    // added for proj 8 
    // ************************
    
    // writes assembly code that effects VM initialization - bootstrap code at beginning of output file
    public void writeInit() {
        try {
            fw.write("@256\nD=A\n@SP\nM=D\n");
            writeCall("Sys.init", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // writes assembly code that is translation of label command
    // arbitrary string with any sequence of letters, digits, underscore, dot, and colon not beginning with digit
    public void writeLabel(String strLabel) {
        try {
            fw.write(new StringBuilder().append("(").append(strLabel).append(")\n").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // writes assembly code that is translation of goto command
    public void writeGoto(String strLabel) {
        try {
            fw.write(new StringBuilder().append("@").append(strLabel).append("\n0;JMP\n").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // writes assembly code that is translation of if-goto command
    public void writeIf(String strLabel) {
        try {
            fw.write(new StringBuilder().append(getArithFormat1()).append("@").append(strLabel).append("\nD;JNE\n").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // writes assembly code that is translation of call command
    public void writeCall(String strFunctionName, int nNumArgs) {
        String strLabel = "RETURN_LABEL" + nLabelNum;
        nLabelNum++;
        try {
            String pFormLCL = getPushFormat2("LCL");
            String pFormARG = getPushFormat2("ARG");
            String pFormTHIS = getPushFormat2("THIS");
            String pFormTHAT = getPushFormat2("THAT");
            
            fw.write("@" + strLabel +"\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n" + pFormLCL + 
                    pFormARG + pFormTHIS+ pFormTHAT + "@SP\nD=M\n@5\nD=D-A\n" + 
                    "@" + nNumArgs + "\nD=D-A\n@ARG\nM=D\n@SP\nD=M\n@LCL\nM=D\n" +
                    "@" + strFunctionName + "\n0;JMP\n(" + strLabel + ")\n");
            
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // writes assembly code that is translation of return command
    public void writeReturn() {
        try {
            String pFormARG = getPushFormat1("ARG",0);
            
            fw.write("@LCL\nD=M\n@FRAME\nM=D\n@5\nA=D-A\nD=M\n@RET\nM=D\n" +
                    pFormARG + "@ARG\nD=M\n@SP\nM=D+1\n@FRAME\nD=M-1\nAM=D\n" +
                    "D=M\n@THAT\nM=D\n@FRAME\nD=M-1\nAM=D\nD=M\n@THIS\n"
                            + "M=D\n@FRAME\nD=M-1\nAM=D\nD=M\n@ARG\nM=D\n"
                            + "@FRAME\nD=M-1\nAM=D\nD=M\n@LCL\nM=D\n@RET\n"
                            + "A=M\n0;JMP\n");
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // writes assembly code that is translation of given function command
    public void writeFunction(String strFunctionName, int nNumLocals) {
        try {
            fw.write(new StringBuilder().append("(").append(strFunctionName).append(")\n").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int nC = 0; nC < nNumLocals; nC++) {
            writePushPop("PUSH", "constant", 0);
        }
    }

}