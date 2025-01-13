package com.scsb.pm.service.helper;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class SftpService {

    private final String host = "172.20.10.4";
    private final int port = 22;
    private final String username = "jasminechang";
    private final String password = "vc278694";
    private String remoteDirectory = "/Users/jasminechang/Documents/uploads"; // 暫存本地


    JSch jsch = new JSch();
    /** 上傳檔案 */
    public String uploadFile(InputStream fileStream, String fileName) throws Exception {
        // 設置SFTP連線
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        try {
            channelSftp.cd(remoteDirectory);
            channelSftp.put(fileStream, fileName); // 上傳
        } catch (Exception e) {
            throw new RuntimeException("Error during SFTP upload file", e);
        } finally {
            channelSftp.disconnect();
            session.disconnect();
        }
        return "File uploaded successfully to " + remoteDirectory + "/" + fileName;
    }

    /** 下載檔案 （檔案傳到遠端再測試）
    public byte[] downloadFile(String remoteFilePath) throws Exception {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            System.out.println("Connecting to SFTP server: " + host + ":" + port);

            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            System.out.println("Connected to SFTP server.");


            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            System.out.println("SFTP channel connected.");

            System.out.println("Downloading file: " + remoteFilePath);
            InputStream inputStream = channelSftp.get(remoteFilePath); // 下載
            byte[] fileData = inputStream.readAllBytes();
            return fileData;

        } catch (Exception e){
            System.err.println("Error during SFTP file download: " + remoteFilePath);
            throw new RuntimeException("Error during SFTP download file", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    } */



    /** 刪除檔案 */
    public void deleteFile(String remotePath) throws Exception {
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        try {
            channelSftp.rm(remotePath); // Remove
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from SFTP: ", e);
        } finally {
            channelSftp.disconnect();
            session.disconnect();
        }
    }
}
