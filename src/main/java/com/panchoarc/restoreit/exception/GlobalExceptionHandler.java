package com.panchoarc.restoreit.exception;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.springframework.shell.command.annotation.ExceptionResolver;
import org.springframework.shell.command.annotation.ExitCode;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class GlobalExceptionHandler {

    @ExceptionResolver({UserInterruptException.class})
    @ExitCode(code = 1)
    public void userInterruptHandler(UserInterruptException e, Terminal terminal) {
        PrintWriter writer = terminal.writer();
        writer.println("❌ Operación cancelada por el usuario (Ctrl+C).");
        writer.flush();
    }

    @ExceptionResolver({EndOfFileException.class})
    @ExitCode(code = 1)
    public void eofHandler(EndOfFileException e, Terminal terminal) {
        PrintWriter writer = terminal.writer();
        writer.println("❌ Entrada final detectada (Ctrl+D).");
        writer.flush();
    }

    @ExceptionResolver({Exception.class})
    @ExitCode(code = 2)
    public void genericExceptionHandler(Exception e, Terminal terminal) {
        PrintWriter writer = terminal.writer();
        writer.println("❌ Error inesperado: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        writer.flush();
    }
}
