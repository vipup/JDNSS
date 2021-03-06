
// Generated by delombok at Fri Mar 20 22:19:47 CET 2020
package edu.msudenver.cs.jdnss;

 

enum JDNSSLogLevels {
    OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL;
}

class jdnssArgs {
    private boolean once = false;
    private int threads = 10;
    private boolean version;
    String[] IPaddresses = {"TLS@0.0.0.0@853", "TCP@0.0.0.0@53", "UDP@0.0.0.0@53"}; // "MC@224.0.0.251@5353"
    int backlog = 4;
    JDNSSLogLevels logLevel = JDNSSLogLevels.ERROR;
    private boolean help;
    private String DBClass;
    private String DBURL;
    private String DBUser;
    private String DBPass;
    String serverSecret;
    String keystoreFile;
    String keystorePassword;
    boolean debugSSL;
    String prefsFile;
    private String[] additional;

    @java.lang.SuppressWarnings("all")
    public boolean isOnce() {
        return this.once;
    }

    @java.lang.SuppressWarnings("all")
    public int getThreads() {
        return this.threads;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isVersion() {
        return this.version;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isHelp() {
        return this.help;
    }

    @java.lang.SuppressWarnings("all")
    public String getDBClass() {
        return this.DBClass;
    }

    @java.lang.SuppressWarnings("all")
    public String getDBURL() {
        return this.DBURL;
    }

    @java.lang.SuppressWarnings("all")
    public String getDBUser() {
        return this.DBUser;
    }

    @java.lang.SuppressWarnings("all")
    public String getDBPass() {
        return this.DBPass;
    }

    @java.lang.SuppressWarnings("all")
    public String[] getAdditional() {
        return this.additional;
    }
}