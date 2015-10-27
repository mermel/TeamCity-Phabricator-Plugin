package com.couchmate.teamcity.utils;

import com.couchmate.teamcity.TCPhabException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.couchmate.teamcity.utils.CommonUtils.isNullOrEmpty;

/**
 * Created by mjo20 on 10/10/2015.
 */
public final class CommandBuilder {

    private final Runtime runtime = Runtime.getRuntime();
    private String path = null;
    private String command = null;
    private String action = null;
    private String workingDir = null;
    private List<String> args = new ArrayList<String>();

    public CommandBuilder setWorkingDir(String workingDir){
        if(isNullOrEmpty(workingDir)) throw new IllegalArgumentException("Must provide a valid working directory");
        else this.workingDir = workingDir;
        return this;
    }

    public CommandBuilder setCommand(String cmd){
        if(isNullOrEmpty(cmd)) throw new IllegalArgumentException("Must provide a command");
        else this.command = cmd;
        return this;
    }

    public CommandBuilder setAction(String action){
        if(isNullOrEmpty(action)) throw new IllegalArgumentException("Must provide a valid action");
        else this.action = action;
        return this;
    }

    public CommandBuilder setArg(String arg){
        if(isNullOrEmpty(arg)) throw new IllegalArgumentException("Must provide a valid argument");
        else this.args.add(arg);
        return this;
    }

    public CommandBuilder setArg(int pos, String arg){
        if(isNullOrEmpty(arg)) throw new IllegalArgumentException("Must provide a valid argument");
        else this.args.add(pos, arg);
        return this;
    }

    public CommandBuilder setArgs(String... args){
        for(String arg : args){
            this.args.add(arg);
        }
        return this;
    }

    public CommandBuilder setFlag(String flag){
        if(isNullOrEmpty(flag)) throw new IllegalArgumentException("Must provide a valid flag");
        else this.args.add(formatFlag(flag));
        return this;
    }

    public CommandBuilder setFlag(int pos, String flag){
        if(isNullOrEmpty(flag)) throw new IllegalArgumentException("Must provide a valid flag");
        else this.args.add(pos, formatFlag(flag));
        return this;
    }

    public CommandBuilder setArgWithValue(KeyValue argWithValue){
        this.args.add(String.format("%s %s", argWithValue.getKey(), argWithValue.getValue()));
        return this;
    }

    public CommandBuilder setArgWithValue(int pos, KeyValue argWithValue){
        this.args.add(pos, String.format("%s %s", argWithValue.getKey(), argWithValue.getValue()));
        return this;
    }

    public CommandBuilder setFlagWithValue(KeyValue flagWithValue){
        this.args.add(String.format("%s %s", formatFlag(flagWithValue.getKey()), flagWithValue.getValue()));
        return this;
    }

    public CommandBuilder setFlagWithValue(int pos, KeyValue flagWithValue){
        this.args.add(pos, String.format("%s %s", formatFlag(flagWithValue.getKey()), flagWithValue.getValue()));
        return this;
    }

    public CommandBuilder setArgWithValueEquals(KeyValue argWithValueEquals){
        this.args.add(String.format("%s=%s", argWithValueEquals.getKey(), argWithValueEquals.getValue()));
        return this;
    }

    public CommandBuilder setArgWithValueEquals(int pos, KeyValue argWithValueEquals){
        this.args.add(pos, String.format("%s=%s", argWithValueEquals.getKey(), argWithValueEquals.getValue()));
        return this;
    }

    public CommandBuilder setFlagWithValueEquals(KeyValue flagWithValueEquals){
        this.args.add(String.format("%s=%s", formatFlag(flagWithValueEquals.getKey()), flagWithValueEquals.getValue()));
        return this;
    }

    public CommandBuilder setFlagWithValueEquals(int pos, KeyValue flagWithValueEquals){
        this.args.add(pos, String.format("%s=%s", formatFlag(flagWithValueEquals.getKey()), flagWithValueEquals.getValue()));
        return this;
    }

    public Command build() throws TCPhabException {
        if(isNullOrEmpty(this.command)) throw new TCPhabException("Must provide a valid command");
        else this.args.add(0, command);
        if(!isNullOrEmpty(this.action)) this.args.add(1, action);

        return new Command(
                this.runtime,
                (String[])this.args.toArray(),
                this.workingDir
        );
    }

    private static String formatFlag(String flag){
        Pattern withFlag = Pattern.compile("^\\-\\-\\w+$");
        Pattern singleWord = Pattern.compile("^\\w$");
        Matcher m = withFlag.matcher(flag.trim());
        Matcher m1 = singleWord.matcher(flag.trim());
        if(m.matches()) return flag.trim();
        else if(m1.matches()) return String.format("--%s", flag.trim());
        else throw new IllegalArgumentException(String.format("%s is not a valid flag", flag));
    }

    public class Command {
        private final Runtime runtime;
        private final String[] args;
        private final File workingDir;
        private Process process;

        private Command(){
            this.runtime = null;
            this.args = null;
            this.workingDir = null;
        }

        public Command(
                final Runtime runtime,
                final String[] args,
                final String workingDir
        ){
            this.runtime = runtime;
            this.args = args;
            this.workingDir = isNullOrEmpty(workingDir) ? null : new File(workingDir);
        }

        public int exec(){
            try { this.process = this.runtime.exec(args, null, workingDir); return this.process.waitFor(); }
            catch (IOException | InterruptedException e) { return 666; }
        }

        public BufferedOutputStream getOutputStream(){
            if(!this.process.isAlive()) return null;
            else return new BufferedOutputStream(this.process.getOutputStream());
        }
    }

}