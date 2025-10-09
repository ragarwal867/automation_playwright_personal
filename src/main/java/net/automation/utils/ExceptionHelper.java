package net.automation.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHelper {
    public static String getDetailedExceptionInfo(Throwable exception) {
        StringBuilder sb = new StringBuilder();

        sb.append("Exception: ").append(exception.getClass().getName()).append("\n");

        if (exception.getMessage() != null) {
            sb.append("Message: ").append(exception.getMessage()).append("\n");
        } else {
            sb.append("Message: (no message provided)\n");
        }

        sb.append("Stack Trace:\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        sb.append(sw.toString());

        Throwable cause = exception.getCause();
        if (cause != null) {
            sb.append("\nCaused by: ").append(cause.getClass().getName()).append("\n");
            sb.append("Cause Message: ").append(cause.getMessage()).append("\n");
            sb.append("Cause Stack Trace:\n");
            StringWriter causeSw = new StringWriter();
            PrintWriter causePw = new PrintWriter(causeSw);
            cause.printStackTrace(causePw);
            sb.append(causeSw.toString());
        }

        return sb.toString();
    }
}
