/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.workflow.impl.dao;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.impl.WFImplConstant;
import org.wso2.carbon.identity.workflow.impl.WorkflowImplException;
import org.wso2.carbon.identity.workflow.impl.bean.BPSProfile;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BPSProfileDAO {

    /**
     * Add a new BPS profile
     *
     * @param bpsProfileDTO Details of profile to add
     * @param tenantId      ID of tenant domain
     * @throws WorkflowImplException
     */
    public void addProfile(BPSProfile bpsProfileDTO, int tenantId) throws WorkflowImplException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.ADD_BPS_PROFILE_QUERY;
        String profileName = bpsProfileDTO.getProfileName();
        String encryptedPassword = "";
        String encryptedApiKey = "";

        try {
            if(StringUtils.isNotEmpty(bpsProfileDTO.getApiKey())) {
                encryptedApiKey = encryptData(bpsProfileDTO.getApiKey().toCharArray());
            }
            if(bpsProfileDTO.getPassword() != null && bpsProfileDTO.getPassword().length > 0) {
                encryptedPassword = encryptData(bpsProfileDTO.getPassword());
            }

        } catch (CryptoException e) {
            throw new WorkflowImplException("Error while encrypting the passwords of BPS Profile: " + profileName, e);
        }

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, bpsProfileDTO.getProfileName());
            prepStmt.setString(2, bpsProfileDTO.getManagerHostURL());
            prepStmt.setString(3, bpsProfileDTO.getWorkerHostURL());
            prepStmt.setString(4, bpsProfileDTO.getUsername());
            prepStmt.setString(5, encryptedPassword);
            prepStmt.setInt(6, tenantId);
            prepStmt.setString(7, encryptedApiKey);
            prepStmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new WorkflowImplException("Error when executing the sql query " + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Update existing BPS Profile
     *
     * @param bpsProfile BPS profile object with new details
     * @param tenantId   ID of tenant domain
     * @throws WorkflowImplException
     */
    public void updateProfile(BPSProfile bpsProfile, int tenantId) throws WorkflowImplException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.UPDATE_BPS_PROFILE_QUERY;
        String profileName = bpsProfile.getProfileName();
        String encryptedPassword = "";
        String encryptedApiKey = "";

        try {
            if(StringUtils.isNotEmpty(bpsProfile.getApiKey())) {
                encryptedApiKey = encryptData(bpsProfile.getApiKey().toCharArray());
            }
            if(bpsProfile.getPassword() != null && bpsProfile.getPassword().length > 0) {
                encryptedPassword = encryptData(bpsProfile.getPassword());
            }
        } catch (CryptoException e) {
            throw new WorkflowImplException("Error while encrypting the passwords of BPS Profile: " + profileName, e);
        }

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, bpsProfile.getManagerHostURL());
            prepStmt.setString(2, bpsProfile.getWorkerHostURL());
            prepStmt.setString(3, bpsProfile.getUsername());
            prepStmt.setString(4, encryptedPassword);
            prepStmt.setString(5, encryptedApiKey);
            prepStmt.setInt(6, tenantId);

            prepStmt.setString(7, bpsProfile.getProfileName());
            prepStmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new WorkflowImplException("Error when executing the sql query " + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Retrieve details of a BPS profile
     *
     * @param profileName     Name of profile to retrieve
     * @param tenantId        Id of tenant domain
     * @param isWithPasswords Whether password to be retrieved or not
     * @return
     * @throws WorkflowImplException
     */
    public BPSProfile getBPSProfile(String profileName, int tenantId, boolean isWithPasswords)
            throws WorkflowImplException {

        BPSProfile bpsProfileDTO = null;
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs;
        String query = SQLConstants.GET_BPS_PROFILE_FOR_TENANT_QUERY;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, profileName);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                String managerHostName = rs.getString(SQLConstants.HOST_URL_MANAGER_COLUMN);
                String workerHostName = rs.getString(SQLConstants.HOST_URL_WORKER_COLUMN);
                String user = rs.getString(SQLConstants.USERNAME_COLUMN);
                bpsProfileDTO = new BPSProfile();
                bpsProfileDTO.setProfileName(profileName);
                bpsProfileDTO.setManagerHostURL(managerHostName);
                bpsProfileDTO.setWorkerHostURL(workerHostName);
                bpsProfileDTO.setUsername(user);

                if (isWithPasswords) {
                    String password = rs.getString(SQLConstants.PASSWORD_COLUMN);
                    String apiKey = rs.getString("API_KEY");
                    try {
                        if(StringUtils.isNotEmpty(apiKey)) {
                            String apiKeyString = new String(decryptData(apiKey));
                           bpsProfileDTO.setApiKey(apiKeyString);
                        }
                        if(StringUtils.isNotEmpty(password)){
                            bpsProfileDTO.setPassword(decryptData(password));
                        }

                    } catch (CryptoException | UnsupportedEncodingException e) {
                        throw new WorkflowImplException(
                                "Error while decrypting the password for BPEL Profile " + profileName, e);
                    }
                }
            }
        } catch (SQLException e) {
            throw new WorkflowImplException("Error when executing the sql " + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return bpsProfileDTO;
    }

    /**
     * Retrieve list of existing BPS profiles
     *
     * @param tenantId Id of tenant domain to retrieve BPS profiles
     * @return
     * @throws WorkflowImplException
     */
    public List<BPSProfile> listBPSProfiles(int tenantId) throws WorkflowImplException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs;
        List<BPSProfile> profiles = new ArrayList<>();
        String query = SQLConstants.LIST_BPS_PROFILES_QUERY;
        String decryptPassword = null;
        String decryptedApiKey = null;
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        try {
            Object classCheckResult = null;
            //Checks if IS has BPS features installed with it
            try {
                classCheckResult = Class.forName("org.wso2.carbon.humantask.deployer.HumanTaskDeployer");
            } catch (ClassNotFoundException e) {
                //The purpose of this code segment is to check if BPS features have been installed by trying to load
                // 'HumanTaskDeployer'.If BPS features are not installed, it will throw a ClassNotFoundException. The
                // object retrieved here will be checked for null it will kept null when following this path. So no
                // need to do anything here.
            }
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(SQLConstants.PROFILE_NAME_COLUMN);
                if (classCheckResult == null && name.equals("embeded_bps")) {
                    continue;
                }
                String managerHostName = rs.getString(SQLConstants.HOST_URL_MANAGER_COLUMN);
                String workerHostName = rs.getString(SQLConstants.HOST_URL_WORKER_COLUMN);
                String user = rs.getString(SQLConstants.USERNAME_COLUMN);
                String password = rs.getString(SQLConstants.PASSWORD_COLUMN);
                String apiKey = rs.getString(SQLConstants.APIKEY_COLUMN);
                try {
                    if(password != ""){
                        byte[] decryptedPasswordBytes = cryptoUtil.base64DecodeAndDecrypt(password);
                        decryptPassword = new String(decryptedPasswordBytes, WFImplConstant.DEFAULT_CHARSET);
                    }
                    if(apiKey != ""){
                        byte[] decryptedPasswordBytes = cryptoUtil.base64DecodeAndDecrypt(apiKey);
                        decryptedApiKey = new String(decryptedPasswordBytes, WFImplConstant.DEFAULT_CHARSET);
                    }

                } catch (CryptoException | UnsupportedEncodingException e) {
                    throw new WorkflowImplException("Error while decrypting the password for BPEL Profile " + name, e);
                }
                BPSProfile profileBean = new BPSProfile();
                profileBean.setManagerHostURL(managerHostName);
                profileBean.setWorkerHostURL(workerHostName);
                profileBean.setProfileName(name);
                profileBean.setUsername(user);
                profileBean.setPassword(decryptPassword.toCharArray());
                profileBean.setApiKey(decryptedApiKey);
                profiles.add(profileBean);
            }
        } catch (SQLException e) {
            throw new WorkflowImplException("Error when executing the sql " + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return profiles;
    }

    /**
     * Delete a BPS profile
     *
     * @param profileName Name of the profile to retrieve
     * @throws WorkflowImplException
     */
    public void removeBPSProfile(String profileName) throws WorkflowImplException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_BPS_PROFILES_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, profileName);
            prepStmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new WorkflowImplException("Error when executing the sql " + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Delete BPS profile from a tenant.
     *
     * @param profileName BPS profile name.
     * @param tenantId    Tenant Id.
     * @throws WorkflowImplException If an error occurred while deleting bps profile.
     */
    public void removeBPSProfile(String profileName, int tenantId) throws WorkflowImplException {

        String query = SQLConstants.DELETE_TENANTED_BPS_PROFILES_QUERY;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            try {
                prepStmt.setString(1, profileName);
                prepStmt.setInt(2, tenantId);
                prepStmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new WorkflowImplException("Error when executing the sql " + query, e);
            }
        } catch (SQLException e) {
            throw new WorkflowImplException("Error when executing the sql " + query, e);
        }
    }

    /**
     * Delete BPS profiles of given tenant id.
     *
     * @param tenantId Id of the tenant to remove BPS profiles
     * @throws WorkflowImplException
     */
    public void removeBPSProfiles(int tenantId) throws WorkflowImplException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_BPS_PROFILES_OF_TENANT_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantId);

            prepStmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new WorkflowImplException("Error when executing the sql " + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    private String encryptData(char[] data) throws CryptoException {

        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        return cryptoUtil.encryptAndBase64Encode(toBytes(data));
    }

    private char[] decryptData(String data) throws UnsupportedEncodingException, CryptoException {

        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        byte[] decryptedPasswordBytes = cryptoUtil.base64DecodeAndDecrypt(data);
        return (new String(decryptedPasswordBytes, WFImplConstant.DEFAULT_CHARSET)).toCharArray();

    }

    /**
     * Convert a char array into a byte array
     *
     * @param chars
     * @return
     */
    private byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName(WFImplConstant.DEFAULT_CHARSET).encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000');
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

}
