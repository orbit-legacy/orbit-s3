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

package cloud.orbit.actors.extensions.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.orbit.actors.extensions.StorageExtension;
import cloud.orbit.actors.runtime.RemoteReference;
import cloud.orbit.concurrent.Task;
import cloud.orbit.concurrent.TaskContext;
import cloud.orbit.exception.UncheckedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Created by joe@bioware.com on 2016-04-04.
 */
public class S3StorageExtension implements StorageExtension
{
    private String name = "default";
    private String bucketName = "orbit-test-bucket";
    private S3Configuration s3Configuration = new S3Configuration();
    private S3Connection s3Connection;

    public S3StorageExtension()
    {

    }

    public S3StorageExtension(S3Configuration s3Configuration)
    {
        this.s3Configuration = s3Configuration;
    }

    @Override
    public Task<Void> start()
    {
        s3Connection = new S3Connection(s3Configuration);

        // Check the bucket actually exists
        // TODO: Should we create it here if it doesn't?
        if(!s3Connection.getS3Client().doesBucketExist(bucketName))
        {
            throw new UncheckedException("S3 bucket '" + bucketName + "' does not exist.");
        }

        return Task.done();
    }

    @Override
    public Task<Void> stop()
    {
        return Task.done();
    }

    @Override
    public Task<Void> clearState(final RemoteReference<?> reference, final Object state)
    {
        return Task.runAsync(() ->
        {
            final String bucketName = getBucketName();
            final String itemId = generateDocumentId(reference);


            s3Connection.getS3Client().deleteObject(bucketName, itemId);
        });
    }

    @Override
    public Task<Boolean> readState(final RemoteReference<?> reference, final Object state)
    {
        return Task.supplyAsync(() ->
        {
            final ObjectMapper mapper = s3Connection.getMapper();
            final String bucketName = getBucketName();
            final String itemId = generateDocumentId(reference);

            try
            {
                final S3Object s3Object = s3Connection.getS3Client().getObject(bucketName, itemId);
                mapper.readerForUpdating(state).readValue(s3Object.getObjectContent());
                s3Object.close();
                return Task.fromValue(true);
            }
            catch (AmazonServiceException e)
            {
                final String errorCode = e.getErrorCode();
                if (!errorCode.equals("NoSuchKey"))
                {
                    throw e;
                }
                return Task.fromValue(false);

            }
            catch (Exception e)
            {
                throw new UncheckedException(e);
            }
        });
    }

    @Override
    public Task<Void> writeState(final RemoteReference<?> reference, final Object state)
    {
        return Task.runAsync(() ->
        {
            final ObjectMapper mapper = s3Connection.getMapper();
            final String bucketName = getBucketName();
            final String itemId = generateDocumentId(reference);

            final ByteArrayOutputStream fileStream = new ByteArrayOutputStream();
            try
            {

                mapper.writeValue(fileStream, state);
            }
            catch (IOException e)
            {
                throw new UncheckedException(e);
            }

            final InputStream inputStream = new ByteArrayInputStream(fileStream.toByteArray());
            s3Connection.getS3Client().putObject(bucketName, itemId, inputStream, null);
        });
    }

    public String generateDocumentId(final RemoteReference<?> reference)
    {
        final Class<?> referenceClass = RemoteReference.getInterfaceClass(reference);
        final String idDecoration = referenceClass.getSimpleName();

        final String documentId = String.format(
                "%s%s%s",
                idDecoration,
                "/",
                String.valueOf(RemoteReference.getId(reference)));

        return documentId;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(final String bucketName)
    {
        this.bucketName = bucketName;
    }

    public S3Configuration getS3Configuration()
    {
        return s3Configuration;
    }

    public void setS3Configuration(final S3Configuration s3Configuration)
    {
        this.s3Configuration = s3Configuration;
    }
}
