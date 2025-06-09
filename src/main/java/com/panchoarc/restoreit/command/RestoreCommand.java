package com.panchoarc.restoreit.command;


import com.panchoarc.restoreit.utils.InputPrompter;
import org.jline.reader.LineReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
@Command(group = "Restore Commands")
public class RestoreCommand {


    @Autowired
    @Lazy
    private LineReader lineReader;

    @Autowired
    private InputPrompter inputPrompter;


    @Command(command = "restore", alias = {"r"})
    public void restore() {

    }

}
