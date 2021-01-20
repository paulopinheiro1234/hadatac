package org.hadatac.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Command {

    public static String exec(int mode, boolean verbose, String[] command) {
        String message = "";
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc1 = rt.exec(command);
            InputStream stderr = proc1.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            if (verbose) {
                while ( (line = br.readLine()) != null)
                    message += Feedback.println(mode, line);
            }
            int exitVal = proc1.waitFor();
            message += Feedback.print(mode, "    exit value: [" + exitVal + "]    ");
            //message += println(mode, "   Process: [" + command[0] + "]   exitValue: [" + exitVal + "]");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return message;
    }

}

