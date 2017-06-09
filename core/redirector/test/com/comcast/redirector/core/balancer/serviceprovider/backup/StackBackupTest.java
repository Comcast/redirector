/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.core.balancer.serviceprovider.backup;

import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StackBackupTest {

    @Test
    public void addOneStack() throws Exception {

        StackSnapshot stackSnapshot = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.1", null));
        }});

        StackBackup backup = new StackBackup();
        backup.addSnapshot(stackSnapshot);

        assertEquals(1, backup.getSnapshotList().size());
        assertEquals(1, backup.getAllStacks().size());
        assertEquals(stackSnapshot, backup.getSnapshotList().get(0));
    }

    @Test
    public void addHostInToExistsStack() throws Exception {

        StackSnapshot stackSnapshot = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.1", null));
        }});

        StackBackup backup = new StackBackup();
        backup.addSnapshot(stackSnapshot);

        StackSnapshot theSameSnapshotWithNewHost = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.2", null));
        }});
        backup.addSnapshot(theSameSnapshotWithNewHost);

        assertEquals(1, backup.getSnapshotList().size());
        assertEquals(1, backup.getAllStacks().size());
        assertEquals(2, backup.getSnapshotList().get(0).getHosts().size());
    }

    @Test
    public void addHostInToExistsStackWithPathIsNull() throws Exception {

        StackSnapshot stackSnapshot = new StackSnapshot(null, new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.1", null));
        }});

        StackBackup backup = new StackBackup();
        backup.addSnapshot(stackSnapshot);

        StackSnapshot theSameSnapshotWithNewHost = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.2", null));
        }});
        backup.addSnapshot(theSameSnapshotWithNewHost);

        assertEquals(2, backup.getSnapshotList().size());
        assertEquals(1, backup.getSnapshotList().get(0).getHosts().size());
    }

    @Test
    public void addHostInToExistsStackIsNull() throws Exception {
        StackBackup backup = new StackBackup();
        backup.addSnapshot(null);

        StackSnapshot snapshotWithNewHost = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.2", null));
        }});
        backup.addSnapshot(snapshotWithNewHost);
        assertEquals(2, backup.getSnapshotList().size());
        assertNull( backup.getSnapshotList().get(0));
        assertEquals(1, backup.getSnapshotList().get(1).getHosts().size());
    }

    @Test
    public void addHostWithPathIsNullInToExistsStack() throws Exception {

        StackSnapshot stackSnapshot = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.1", null));
        }});

        StackBackup backup = new StackBackup();
        backup.addSnapshot(stackSnapshot);

        StackSnapshot theSameSnapshotWithNewHost = new StackSnapshot(null, new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.2", null));
        }});
        backup.addSnapshot(theSameSnapshotWithNewHost);

        assertEquals(2, backup.getSnapshotList().size());
        assertEquals(1, backup.getSnapshotList().get(0).getHosts().size());
    }

    @Test
    public void addHostIsNullInToExistsStack() throws Exception {
        StackSnapshot stackSnapshot = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.1", null));
        }});

        StackBackup backup = new StackBackup();
        backup.addSnapshot(stackSnapshot);
        backup.addSnapshot(null);

        assertEquals(2, backup.getSnapshotList().size());
        assertEquals(1, backup.getSnapshotList().get(0).getHosts().size());
        assertNull(backup.getSnapshotList().get(1));
    }

    @Test
    public void addHostsInToExistsStack() throws Exception {

        StackBackup backup = new StackBackup();
        StackSnapshot stackSnapshot = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.1", null));
        }});

        backup.addSnapshot(stackSnapshot);
        backup.addSnapshot(stackSnapshot);
        backup.addSnapshot(stackSnapshot);
        backup.addSnapshot(stackSnapshot);

        assertEquals(1, backup.getSnapshotList().size());
        assertEquals(1, backup.getAllStacks().size());
        assertEquals(1, backup.getSnapshotList().get(0).getHosts().size());
    }

    @Test
    public void removeHostInExistsStack() throws Exception {
        StackSnapshot stackSnapshot = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.1", null));
            add(new StackSnapshot.Host("127.0.0.2", null));
        }});

        StackBackup backup = new StackBackup();
        backup.addSnapshot(stackSnapshot);

        backup.deleteSnapshot(stackSnapshot);
        assertEquals(1, backup.getSnapshotList().size());
        assertEquals(1, backup.getAllStacks().size());
        assertEquals(0, backup.getSnapshotList().get(0).getHosts().size());
    }

    @Test
    public void removeHostInExistsStackWithMoreThanOneHost() throws Exception {
        StackBackup backup = new StackBackup();

        StackSnapshot stackSnapshot = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.1", null));
        }});

        StackSnapshot theSameSnapshotWithNewHost = new StackSnapshot("/PO/POC1/1.90/pandora", new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host("127.0.0.2", null));
        }});

        backup.addSnapshot(stackSnapshot);
        backup.addSnapshot(theSameSnapshotWithNewHost);

        backup.deleteSnapshot(theSameSnapshotWithNewHost);
        assertEquals(1, backup.getSnapshotList().size());
        assertEquals(1, backup.getAllStacks().size());
        assertEquals(1, backup.getSnapshotList().get(0).getHosts().size());
        assertEquals("127.0.0.1", backup.getSnapshotList().get(0).getHosts().get(0).getIpv4());
        assertNull(backup.getSnapshotList().get(0).getHosts().get(0).getIpv6());
    }
}
