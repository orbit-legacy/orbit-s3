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

/**
 * Created by joe@bioware.com on 2016-04-04.
 */
public class S3Configuration
{
    public static class Builder
    {
        private S3Configuration s3Configuration;

        public Builder()
        {
            s3Configuration = new S3Configuration();
        }

        public Builder withCredentialType(final AmazonCredentialType credentialType)
        {
            s3Configuration.setCredentialType(credentialType);
            return this;
        }

        public Builder withAccessKey(final String accessKey)
        {
            s3Configuration.setAccessKey(accessKey);
            return this;
        }

        public Builder withSecretKey(final String secretKey)
        {
            s3Configuration.setSecretKey(secretKey);
            return this;
        }

        public Builder withSessionToken(final String sessionToken)
        {
            s3Configuration.setSessionToken(sessionToken);
            return this;
        }

        public Builder withRegion(final String region)
        {
            s3Configuration.setRegion(region);
            return this;
        }

        public Builder withEndpoint(final String endpoint)
        {
            s3Configuration.setEndpoint(endpoint);
            return this;
        }

        public S3Configuration build()
        {
            return s3Configuration;
        }
    }

    private AmazonCredentialType credentialType;
    private String accessKey;
    private String secretKey;
    private String sessionToken;
    private String region;
    private String endpoint;

    public AmazonCredentialType getCredentialType()
    {
        return credentialType;
    }

    public void setCredentialType(final AmazonCredentialType credentialType)
    {
        this.credentialType = credentialType;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public void setAccessKey(final String accessKey)
    {
        this.accessKey = accessKey;
    }

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey(final String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public void setSessionToken(final String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(final String region)
    {
        this.region = region;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(final String endpoint)
    {
        this.endpoint = endpoint;
    }
}
