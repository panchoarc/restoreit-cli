package com.panchoarc.restoreit.utils;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InputPrompter {

    @Autowired
    private Terminal terminal;

    public String promptConAutocompletado(String label, List<String> opciones) {
        Completer completer = new StringsCompleter(opciones);
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();

        String entrada;
        do {
            entrada = reader.readLine(label + ": ").trim().toLowerCase();
        } while (!opciones.contains(entrada));
        return entrada;
    }

    public String promptConDefault(LineReader reader, String label, String defaultValue) {
        String entrada = reader.readLine(label + " [" + defaultValue + "]: ");
        return entrada.isBlank() ? defaultValue : entrada;
    }

    public String promptObligatorio(LineReader reader, String label) {
        String valor;
        do {
            valor = reader.readLine(label + ": ");
        } while (valor.isBlank());
        return valor;
    }

    public String promptPassword(LineReader reader, String label) {
        return reader.readLine(label + ": ", '*');
    }
}
