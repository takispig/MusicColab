package com.example.musiccolab;



import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class ActionLog {


    public static void initLogger(Logger logr){
        LogManager.getLogManager().reset();
        logr.setLevel(Level.ALL);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.INFO);
        logr.addHandler(ch);

        try {
            FileHandler fh = new FileHandler("Log.txt");
            fh.setLevel(Level.FINE);
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord logRecord) {
                    String ret = "";
                    if (logRecord.getLevel().intValue() >= Level.WARNING.intValue()){
                        ret += "ATTENTION! ";
                    }
                    ret += logRecord.getLevel()+": ";
                    SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
                    Date d = new Date(logRecord.getMillis());
                    ret += df.format(d) +": ";
                    ret += this.formatMessage(logRecord);
                    ret += "\n";
                    return ret;
                }
            });
            logr.addHandler(fh);
        } catch (IOException e) {
            logr.log(Level.SEVERE, "File logger not working.", e);
        }




    }



}
