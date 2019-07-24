package cspro2sql.bean;

import java.util.Properties;

/**
 * Copyright 2017 ISTAT
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 *
 * @author Guido Drovandi <drovandi @ istat.it>
 * @author Mauro Bruno <mbruno @ istat.it>
 * @version 0.9.18.2
 */
public class ConnectionParams {

    public static final String MYSQL_JDBC = "jdbc:mysql://";
    private static final String MYSQL_PARAMS = "?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private String uri;
    private String username;
    private String password;

    public static ConnectionParams getSourceParams(Properties prop) {
        ConnectionParams sourceParams = new ConnectionParams();
        
        sourceParams.setUri(MYSQL_JDBC + prop.getProperty("db.source.server").trim() + ":" + prop.getProperty("db.source.port").trim() 
                + "/" + prop.getProperty("db.source.schema").trim() + MYSQL_PARAMS);
        sourceParams.setUsername(prop.getProperty("db.source.username").trim());
        sourceParams.setPassword(prop.getProperty("db.source.password").trim());
        return sourceParams;
    }

    public static ConnectionParams getDestParams(Properties prop) {
        ConnectionParams sourceParams = new ConnectionParams();

        if ("sqlserver".equals(prop.getProperty("db.dest.type"))) {
            sourceParams.setUri(MYSQL_JDBC + prop.getProperty("db.dest.server").trim() + ":" + prop.getProperty("db.dest.port").trim() 
                    + ";databasename=" + prop.getProperty("db.dest.schema").trim());
        } else {
            sourceParams.setUri(MYSQL_JDBC + prop.getProperty("db.dest.server").trim() + ":" + prop.getProperty("db.dest.port").trim() 
                    + "/" + prop.getProperty("db.dest.schema").trim() + MYSQL_PARAMS);
        }
        sourceParams.setUsername(prop.getProperty("db.dest.username").trim());
        sourceParams.setPassword(prop.getProperty("db.dest.password").trim());
        return sourceParams;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
