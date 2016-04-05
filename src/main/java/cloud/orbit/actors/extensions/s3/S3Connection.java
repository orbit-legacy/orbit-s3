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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.orbit.actors.extensions.json.ActorReferenceModule;
import cloud.orbit.actors.runtime.DefaultDescriptorFactory;
import cloud.orbit.util.StringUtils;

/**
 * Created by joe@bioware.com on 2016-04-04.
 */
public class S3Connection
{
    private AmazonS3Client s3Client;
    private ObjectMapper mapper;

    public S3Connection(final S3Configuration s3Configuration)
    {
        initializeMapper();
        initializeS3(s3Configuration);
    }

    private void initializeMapper()
    {
        mapper = new ObjectMapper();

        mapper.registerModule(new ActorReferenceModule(DefaultDescriptorFactory.get()));

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void initializeS3(final S3Configuration s3Configuration)
    {
        switch(s3Configuration.getCredentialType())
        {
            case BASIC_CREDENTIALS:
                s3Client = new AmazonS3Client(new BasicAWSCredentials(s3Configuration.getAccessKey(), s3Configuration.getSecretKey()));
                break;

            case BASIC_SESSION_CREDENTIALS:
                s3Client = new AmazonS3Client(new BasicSessionCredentials(s3Configuration.getAccessKey(), s3Configuration.getSecretKey(), s3Configuration.getSessionToken()));
                break;

            case DEFAULT_PROVIDER_CHAIN:
            default:
                s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
                break;
        }

        String awsRegion = StringUtils.defaultIfBlank(s3Configuration.getRegion(), AWSConfigValue.getRegion());
        if (StringUtils.isNotBlank(awsRegion))
        {
            s3Client.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
        }

        if (StringUtils.isNotBlank(s3Configuration.getEndpoint()))
        {
            s3Client.setEndpoint(s3Configuration.getEndpoint());
        }
    }

    public ObjectMapper getMapper()
    {
        return mapper;
    }

    public AmazonS3Client getS3Client()
    {
        return s3Client;
    }

}
