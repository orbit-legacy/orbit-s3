/*
 Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2.  Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
     its contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cloud.orbit.actors.extensions.s3.test;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import cloud.orbit.actors.Actor;
import cloud.orbit.actors.Stage;
import cloud.orbit.actors.extensions.s3.AmazonCredentialType;
import cloud.orbit.actors.extensions.s3.S3Configuration;
import cloud.orbit.actors.extensions.s3.S3StorageExtension;
import cloud.orbit.util.StringUtils;


/**
 * Created by joe@bioware.com on 2016-04-04.
 */
public class S3Test
{
    private S3Configuration s3Configuration;
    private S3StorageExtension s3StorageExtension;
    private Stage stage;

    private static final String TEST_STRING = "OrbitTestString1928374";
    private static final String TEST_STRING2 = "NewTestString4856737";
    private static final String ACTOR_ID = "TestActor3435";

    private void restartStage()
    {
        if(stage != null)
        {
            stage.stop().join();
            stage = null;
        }

        stage = new Stage.Builder().clusterName("s3-test").extensions(s3StorageExtension).build();
        stage.start().join();
    }

    @Test

    public void testS3()
    {

        Assume.assumeTrue(!StringUtils.equals(System.getenv("TRAVIS"), "true")
                || StringUtils.equals(System.getenv("ORBIT_TEST_S3_ENABLED"), "true"));


        s3Configuration = new S3Configuration.Builder()
                .withCredentialType(AmazonCredentialType.DEFAULT_PROVIDER_CHAIN)
                .build();

        s3StorageExtension = new S3StorageExtension(s3Configuration);

        String bucketName = System.getenv("ORBIT_TEST_S3_BUCKET");
        if(bucketName == null)
        {
            bucketName = "orbit-test-bucket";
        }

        s3StorageExtension.setBucketName(bucketName);

        restartStage();



        Actor.getReference(TestActor.class, ACTOR_ID).writeRecord(TEST_STRING).join();

        String recordValue = Actor.getReference(TestActor.class, ACTOR_ID).getRecord().join();
        Assert.assertEquals(recordValue, TEST_STRING);

        restartStage();

        recordValue = Actor.getReference(TestActor.class, ACTOR_ID).getRecord().join();
        Assert.assertEquals(recordValue, TEST_STRING);

        Actor.getReference(TestActor.class, ACTOR_ID).writeRecord(TEST_STRING2).join();

        recordValue = Actor.getReference(TestActor.class, ACTOR_ID).getRecord().join();
        Assert.assertEquals(recordValue, TEST_STRING2);

        restartStage();

        recordValue = Actor.getReference(TestActor.class, ACTOR_ID).getRecord().join();
        Assert.assertEquals(recordValue, TEST_STRING2);

        Actor.getReference(TestActor.class, ACTOR_ID).clearRecord().join();

        recordValue = Actor.getReference(TestActor.class, ACTOR_ID).getRecord().join();
        Assert.assertNull(recordValue);

        restartStage();

        recordValue = Actor.getReference(TestActor.class, ACTOR_ID).getRecord().join();
        Assert.assertNull(recordValue);
    }

}
