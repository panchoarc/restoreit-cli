package com.panchoarc.restoreit.completers;

import com.panchoarc.restoreit.enums.DatabaseType;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseTypeProvider implements Completer {

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> candidates) {

        DatabaseType.allNames().stream().map(Candidate::new).forEach(candidates::add);

    }
}
