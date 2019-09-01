package cspro2sql.bean;

import cspro2sql.utils.Utility;
import java.io.PrintStream;
import java.util.Arrays;

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
 * @version 0.9
 */
public class DictionaryInfo {

    public static enum Status {
        STOP(0),
        RUNNING(1);
        private final int status;

        private Status(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    };

    private final int id;
    private final String name;
    private final Status status;
    private final int revision;

    private int total;
    private int loaded;
    private int deleted;
    private int errors;
    private byte[] lastGuid;
    private int nextRevision;

    public DictionaryInfo(){
        this.id = 0;
        this.name = "";
        this.status = Status.STOP;
        this.revision = 0;
        this.total = 0;
        this.loaded = 0;
        this.deleted = 0;
        this.errors = 0;
        this.lastGuid = null;
        this.nextRevision = 0;
    }
    
    public DictionaryInfo(int id, String name, int status, int revision, int total, int loaded, int deleted, int errors, byte[] lastGuid, int nextRevision) {
        this.id = id;
        this.name = name;
        this.status = (status == 1) ? Status.RUNNING : Status.STOP;
        this.revision = revision;
        this.total = total;
        this.loaded = loaded;
        this.deleted = deleted;
        this.errors = errors;
        this.lastGuid = (lastGuid == null ? null : Arrays.copyOf(lastGuid, lastGuid.length));
        this.nextRevision = nextRevision;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public int getRevision() {
        return revision;
    }

    public int getTotal() {
        return total;
    }

    public int getLoaded() {
        return loaded;
    }

    public int getDeleted() {
        return deleted;
    }

    public int getErrors() {
        return errors;
    }

    public byte[] getLastGuid() {
        return lastGuid == null ? null : Arrays.copyOf(lastGuid, lastGuid.length);
    }

    public void setLastGuid(byte[] lastGuid) {
        this.lastGuid = lastGuid == null ? null : Arrays.copyOf(lastGuid, lastGuid.length);
    }

    public int getNextRevision() {
        return nextRevision;
    }

    public void setNextRevision(int nextRevision) {
        this.nextRevision = nextRevision;
    }

    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    public void incTotal() {
        this.total++;
    }
    
    public void incTotal(int total) {
        this.total += total;
    }

    public void incErrors() {
        this.errors++;
    }

    public void incLoaded(int loaded) {
        this.loaded += loaded;
    }

    public void incDeleted(int deleted) {
        this.deleted += deleted;
    }

    public void print(PrintStream out) {
        out.println("Name: " + name);
        out.println("Revision: " + revision);
        out.println("Loader status: " + (status.name()));
        if (status == Status.RUNNING) {
            out.println("Loading to revision: " + nextRevision);
            out.println("Loaded: " + loaded + " quests");
            out.println("Deleted: " + deleted + " quests");
            if (errors > 0) {
                out.println("Detected errors in " + errors + " quests");
            } else {
                out.println("No errors detected");
            }
            out.println("Parsed: " + total + " CSPro quests");
        }
    }

    public void printShort(PrintStream out, Long millis) {
        out.println("Loaded: " + loaded + " quests");
        out.println("Deleted: " + deleted + " quests");
        if (errors > 0) {
            out.println("Detected errors in " + errors + " quests");
        } else {
            out.println("No errors detected");
        }
        out.println("Parsed: " + total + " CSPro quests");
        out.println("Time: " + Utility.convertMillis(millis));
    }

}
