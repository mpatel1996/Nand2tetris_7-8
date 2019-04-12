
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Parser {
    private static List<String> arithCmds = Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not");
    private String mCurrCommand = null;
    private Scanner mScanner = null;
    private String mArg0 = null;
    private String mArg1 = null;
    private String mArg2 = null;
    private String mCmdType = null;

    // opens input file/stream and gets ready to parse it
    public Parser(File file) {
        try {
            mScanner = new Scanner(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // are there more commands in the input?
    public boolean hasMoreCommands() {
        boolean hasMore = false;
        if (mScanner.hasNextLine()) {
            hasMore = true;
        }
        return hasMore;
    }

    // reads next command from input and makes it current command; should be called only if hasMoreCommands() is true
    // initially there is no current command
    public void advance() {
        String strLine = mScanner.nextLine();
        while (strLine.equals("") || strLine.contains("//")) {
            if (strLine.contains("//")) {
                strLine = removeComments(strLine);
            }
            if (strLine.trim().equals("")) {
                strLine = mScanner.nextLine();
            }
        }

        mCurrCommand = strLine;
        String[] cmds = mCurrCommand.split(" ");
        mArg0 = cmds[0];
        if (cmds.length > 1) {
            mArg1 = cmds[1];
        }
        if (cmds.length > 2) {
            mArg2 = cmds[2];
        }
        if (mArg0.equals("push")) {
            mCmdType = "PUSH";
        } else if (mArg0.equals("pop")) {
            mCmdType = "POP";
        } else if (arithCmds.contains(mArg0)) {
            mCmdType = "ARITHMETIC";
        }
        else if (mArg0.equals("label")) {
            mCmdType = "LABEL";
        }
        else if (mArg0.equals("goto")) {
            mCmdType = "GOTO";
        }
        else if (mArg0.equals("if-goto")) {
            mCmdType = "IF";
        }
        else if (mArg0.equals("function")) {
            mCmdType = "FUNCTION";
        }
        else if (mArg0.equals("return")) {
            mCmdType = "RETURN";
        }
        else if (mArg0.equals("call")) {
            mCmdType = "CALL";
        }
    }
   
    
    // removes comments from a line
    private String removeComments(String strLine) {
        String strNoComments = strLine;
        
        if (strLine.contains("//")) {
            int offSet = strLine.indexOf("//");
            strNoComments = strLine.substring(0, offSet).trim();
        }
        return strNoComments;
    }

    // returns type for current VM command, ARITHMETIC returned for all arithmetic commands
    public String commandType() {
        return mCmdType;
    }

    // returns first argument of current command, in case of ARITHMETIC the command itself
    // (add, sub, etc) is returned. should not be called if current command is RETURN
    public String arg1() {
        String strArg1 = null;
        if (mCmdType.equals("ARITHMETIC")) {
            strArg1 = mArg0;
        } else if (!(mCmdType.equals("RETURN"))) {
            strArg1 = mArg1;
        }
        return strArg1;
    }

    // returns 2nd arg of curr command - should be called only if curr command is PUSH, POP, FUNCTION, or CALL
    public int arg2() {
        int nArg2 = 0;
        if (mCmdType.equals("PUSH") || mCmdType.equals("POP") || mCmdType.equals("FUNCTION") || mCmdType.equals("CALL")) {
            nArg2 = Integer.parseInt(mArg2);
        }
        return nArg2;

    }

}