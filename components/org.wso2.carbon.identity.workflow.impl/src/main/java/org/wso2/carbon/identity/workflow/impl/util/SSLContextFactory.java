/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.impl.util;

import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.workflow.impl.WorkflowImplException;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.utils.CarbonUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class SSLContextFactory {

    private static String keyStoreType = CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStore.Type");
    /**
     * Default truststore type of the client
     */
    private static String trustStoreType = CarbonUtils.getServerConfiguration().getFirstProperty("Security.TrustStore.Type");
    /**
     * Default keymanager type of the client
     */
    private static String keyManagerType = IdentityUtil.getProperty("Security.KeyManagerType");
    /**
     * Default trustmanager type of the client
     */
    private static String trustManagerType = IdentityUtil.getProperty("Security.TrustManagerType");


    private static KeyStore keyStore;
    private static KeyStore trustStore;
    private static char[] keyStorePassword;
    private static SSLContext sslContext;

    private static final String KEYSTORE_PATH = ServerConfiguration.getInstance()
            .getFirstProperty("Security.KeyStore.Location");
    private static final String TRUSTSTORE_PATH = ServerConfiguration.getInstance()
            .getFirstProperty("Security.TrustStore.Location");
    /**
     * Default ssl protocol for client
     */
    private static final String protocol = "TLSv1.2";

    public static SSLContext getSslContext() throws WorkflowImplException {
        if (sslContext == null) {
            loadKeyStore(KEYSTORE_PATH, ServerConfiguration.getInstance()
                    .getFirstProperty("Security.KeyStore.Password"));
            //Call to load the truststore.
            loadTrustStore(TRUSTSTORE_PATH, ServerConfiguration.getInstance()
                    .getFirstProperty("Security.TrustStore.Password"));
            //Create the SSL context with the loaded truststore/keystore.
            initMutualSSLConnection();
        }
        return sslContext;
    }

    /**
     * Loads the keystore.
     *
     * @param keyStorePath - the path of the keystore
     * @param ksPassword   - the keystore password
     */
    public static void loadKeyStore(String keyStorePath, String ksPassword) throws WorkflowImplException {
        InputStream fis = null;
        try {
            keyStorePassword = ksPassword.toCharArray();
            keyStore = KeyStore.getInstance(keyStoreType);
            fis = new FileInputStream(keyStorePath);
            keyStore.load(fis, keyStorePassword);
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new WorkflowImplException("Error while loading keystore for mutual ssl authentication.", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new WorkflowImplException("Error while loading keystore for mutual ssl authentication.", e);
                }
            }
        }
    }


    /**
     * Loads the trustore
     *
     * @param trustStorePath - the trustore path in the filesystem.
     * @param tsPassword     - the truststore password
     */
    public static void loadTrustStore(String trustStorePath, String tsPassword) throws WorkflowImplException {

        InputStream fis = null;
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            fis = new FileInputStream(trustStorePath);
            trustStore.load(fis, tsPassword.toCharArray());
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new WorkflowImplException("Error while loading trust-store for mutual ssl authentication.", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new WorkflowImplException("Error while loading trust-store for mutual ssl authentication.",
                            e);
                }
            }
        }
    }


    /**
     * Initializes the SSL Context
     */
    public static void initMutualSSLConnection() throws WorkflowImplException {

        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(keyManagerType);
            keyManagerFactory.init(keyStore, keyStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerType);
            trustManagerFactory.init(trustStore);

            // Create and initialize SSLContext for HTTPS communication
            sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
            throw new WorkflowImplException("Error while initializing mutual ssl authentication.", e);
        }
    }
}
