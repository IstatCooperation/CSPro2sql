package cspro2sql.sql;

import cspro2sql.bean.DictionaryInfo;
import cspro2sql.bean.Questionnaire;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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
 * @version 0.9.10
 */
public class DictionaryQuery {

    private static final String DICTIONARY_UPDATE_REVISION = "update CSPRO2SQL_DICTIONARY set REVISION = ? where ID = ?";
    private static final String DICTIONARY_SELECT_INFO_BY_ID = "select ID, NAME, STATUS, REVISION, TOTAL, LOADED, DELETED, ERRORS, LAST_GUID, NEXT_REVISION from CSPRO2SQL_DICTIONARY where ID = ?";
    private static final String DICTIONARY_SELECT_INFO_BY_NAME = "select ID, NAME, STATUS, REVISION, TOTAL, LOADED, DELETED, ERRORS, LAST_GUID, NEXT_REVISION from CSPRO2SQL_DICTIONARY where NAME = ?";
    private static final String DICTIONARY_UPDATE_STATUS_RUN = "update CSPRO2SQL_DICTIONARY set STATUS = 1, TOTAL = 0, LOADED = 0, DELETED = 0, ERRORS = 0 where ID = ?";
    private static final String DICTIONARY_UPDATE_STATUS_RECOVERY = "update CSPRO2SQL_DICTIONARY set STATUS = 1 where ID = ?";
    private static final String DICTIONARY_UPDATE_STATUS_STOP = "update CSPRO2SQL_DICTIONARY set STATUS = 0 where ID = ?";
    private static final String DICTIONARY_UPDATE_NEXT_REVISION = "update CSPRO2SQL_DICTIONARY set NEXT_REVISION = ? where ID = ?";
    private static final String DICTIONARY_UPDATE_LOADED = "update CSPRO2SQL_DICTIONARY set TOTAL = ?, LOADED = ?, DELETED = ?, ERRORS = ?, LAST_GUID = ? where ID = ?";
    private static final String DICTIONARY_INSERT_ERROR = "insert into CSPRO2SQL_ERROR (DICTIONARY, ERROR, DATE, CSPRO_GUID, QUESTIONNAIRE, SQL_SCRIPT) values (?,?,?,?,?,?)";

    private final PreparedStatement selectInfoById;
    private final PreparedStatement selectInfoByName;
    private final PreparedStatement updateRevision;
    private final PreparedStatement updateStatusRun;
    private final PreparedStatement updateStatusRecovery;
    private final PreparedStatement updateStatusStop;
    private final PreparedStatement updateNextRevision;
    private final PreparedStatement updateLoaded;
    private final PreparedStatement insertError;

    public DictionaryQuery(Connection conn) throws SQLException {
        selectInfoById = conn.prepareStatement(DICTIONARY_SELECT_INFO_BY_ID);
        selectInfoByName = conn.prepareStatement(DICTIONARY_SELECT_INFO_BY_NAME);
        updateRevision = conn.prepareStatement(DICTIONARY_UPDATE_REVISION);
        updateStatusRun = conn.prepareStatement(DICTIONARY_UPDATE_STATUS_RUN);
        updateStatusRecovery = conn.prepareStatement(DICTIONARY_UPDATE_STATUS_RECOVERY);
        updateStatusStop = conn.prepareStatement(DICTIONARY_UPDATE_STATUS_STOP);
        updateNextRevision = conn.prepareStatement(DICTIONARY_UPDATE_NEXT_REVISION);
        updateLoaded = conn.prepareStatement(DICTIONARY_UPDATE_LOADED);
        insertError = conn.prepareStatement(DICTIONARY_INSERT_ERROR);
    }

    public DictionaryInfo getDictionaryInfo(int dictionaryId) {
        try {
            selectInfoById.setInt(1, dictionaryId);
            try (ResultSet result = selectInfoById.executeQuery()) {
                result.next();
                return new DictionaryInfo(
                        result.getInt("ID"),
                        result.getString("NAME"),
                        result.getInt("STATUS"),
                        result.getInt("REVISION"),
                        result.getInt("TOTAL"),
                        result.getInt("LOADED"),
                        result.getInt("DELETED"),
                        result.getInt("ERRORS"),
                        result.getBytes("LAST_GUID"),
                        result.getInt("NEXT_REVISION"));
            }
        } catch (SQLException ex) {
            return null;
        }
    }

    public DictionaryInfo getDictionaryInfo(String dictionaryName) {
        try {
            selectInfoByName.setString(1, dictionaryName);
            try (ResultSet result = selectInfoByName.executeQuery()) {
                result.next();
                return new DictionaryInfo(
                        result.getInt("ID"),
                        result.getString("NAME"),
                        result.getInt("STATUS"),
                        result.getInt("REVISION"),
                        result.getInt("TOTAL"),
                        result.getInt("LOADED"),
                        result.getInt("DELETED"),
                        result.getInt("ERRORS"),
                        result.getBytes("LAST_GUID"),
                        result.getInt("NEXT_REVISION"));
            }
        } catch (SQLException ex) {
            return null;
        }
    }

    public DictionaryInfo run(DictionaryInfo dictionaryInfo, boolean force, boolean recovery) {
        try {
            if (!force && !recovery) {
                DictionaryInfo.Status status = getDictionaryInfo(dictionaryInfo.getId()).getStatus();
                if (status == DictionaryInfo.Status.RUNNING) {
                    return null;
                }
            }
            updateNextRevision.setInt(1, dictionaryInfo.getNextRevision());
            updateNextRevision.setInt(2, dictionaryInfo.getId());
            updateNextRevision.executeUpdate();
            if (recovery && !force) {
                setStatus(dictionaryInfo.getId(), updateStatusRecovery);
            } else {
                setStatus(dictionaryInfo.getId(), updateStatusRun);
            }
            return getDictionaryInfo(dictionaryInfo.getId());
        } catch (SQLException ex) {
            return null;
        }
    }

    public boolean stop(DictionaryInfo dictionaryInfo) {
        try {
            setStatus(dictionaryInfo.getId(), updateStatusStop);
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }

    public boolean updateLoaded(DictionaryInfo info) {
        try {
            updateLoaded.setInt(1, info.getTotal());
            updateLoaded.setInt(2, info.getLoaded());
            updateLoaded.setInt(3, info.getDeleted());
            updateLoaded.setInt(4, info.getErrors());
            updateLoaded.setBytes(5, info.getLastGuid());
            updateLoaded.setInt(6, info.getId());
            updateLoaded.executeUpdate();
            updateLoaded.getConnection().commit();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }

    public boolean updateRevision(DictionaryInfo dictionaryInfo) {
        try {
            updateRevision.setInt(1, dictionaryInfo.getNextRevision());
            updateRevision.setInt(2, dictionaryInfo.getId());
            updateRevision.executeUpdate();
            updateRevision.getConnection().commit();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }

    public void writeError(DictionaryInfo dictionaryInfo, String msg, Questionnaire q, String script) throws SQLException {
        try {
            insertError.setInt(1, dictionaryInfo.getId());
            insertError.setString(2, msg);
            insertError.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            insertError.setBytes(4, q.getGuid());
            insertError.setString(5, q.getPlainText());
            insertError.setString(6, script);
            insertError.executeUpdate();
            insertError.getConnection().commit();
        } catch (Exception ex) {
            System.err.println(q.getPlainText());
            insertError.setInt(1, dictionaryInfo.getId());
            insertError.setString(2, msg);
            insertError.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            insertError.setBytes(4, q.getGuid());
            insertError.setString(5, ex.getMessage());
            insertError.setString(6, "ERROR");
            insertError.executeUpdate();
            insertError.getConnection().commit();
        }
    }

    private void setStatus(int dictionaryId, PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, dictionaryId);
        stmt.executeUpdate();
        stmt.getConnection().commit();
    }

}
