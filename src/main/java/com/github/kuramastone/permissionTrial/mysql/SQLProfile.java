package com.github.kuramastone.permissionTrial.mysql;

public class SQLProfile {

    private String url;
    private String databaseName;
    private String user;
    private String password;
    private boolean useH2;

    public SQLProfile(String url, String databaseName, String user, String password, boolean useH2) {
        this.url = url;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.useH2 = useH2;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean shouldUseH2() {
        return useH2;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
