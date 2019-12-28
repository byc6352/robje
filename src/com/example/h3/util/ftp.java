package com.example.h3.util;

import java.io.File;
import java.io.InputStream;
import java.net.Socket;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.*;

import com.baidu.android.common.logging.Log;
import com.example.h3.Config;
import com.example.h3.MainActivity;
import com.example.h3.Sock;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import it.sauronsoftware.ftp4j.FTPClient;

public class ftp {
	//�����ʶ
	private String TAG = "byc001";
	private static ftp current;
	private Context context;
	private String host = "119.23.68.205";
	private int port = 21;
	private String userName;
	private String password; 
    private FTPClient ftpClient; 
    //ftp ��Ϣ����:
    private static final int FTP_MSG_BASE=0;//��½ʧ�ܣ�
    private static final int FTP_LOGIN_FAIL=FTP_MSG_BASE+1;//��½ʧ�ܣ�
    private static final int FTP_LOGIN_SUC=FTP_MSG_BASE+2;//��½�ɹ���
    private static final int FTP_DOWNLOAD_FAIL=FTP_MSG_BASE+3;//����ʧ�ܣ�
    private static final int FTP_DOWNLOAD_SUC=FTP_MSG_BASE+4;//���سɹ���
    private static final int MSG_WHAT=0x12;//��Ϣ��
    private static final String MSG_KEY="msg";//��Ϣ��
    private String mRecvData="";//��Ϣֵ
    private String mFtpDirPath;//�����ļ�·����
    public String mLocalFileName;//�����ļ�����
    private String mRemoteFileName;//Զ���ļ�����
    private static final String FTP_DIR_NAME = "byc";//�����ļ�������
    private  String FTP_FILE_NAME = "robje01.xml";//�����ļ�����
    
    public ftp() {  
    	TAG=Config.TAG;
        this.host =Config.host; 
        this.port = 21;  
        this.userName = Config.ftpUserName;
        this.password = Config.ftpPwd; 
        this.FTP_FILE_NAME=Config.FTP_FILE_NAME;
        this.ftpClient = new FTPClient(); 
        if (mFtpDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mFtpDirPath = sdcardPath + "/" + FTP_DIR_NAME;
        }
        makeDir(mFtpDirPath);
        mLocalFileName=mFtpDirPath+ "/" + FTP_FILE_NAME;
        mRemoteFileName=FTP_FILE_NAME;
    }  
    public static synchronized ftp getFtp() {
        if(current == null) {
            current = new ftp();
        }
        return current;
    }
    /*��ʼ����*/
    public void DownloadStart(){
    	deletefile(mLocalFileName);
    	new ftpThread().start();
    	return ;
    }
//------------------------------------------��Ϣ����-----------------------------------------------
    public Handler handlerFtp = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
        	switch (msg.what) {
			case FTP_LOGIN_FAIL:
				Log.i(TAG, "FTP_LOGIN_FAIL");
				break;
			case FTP_DOWNLOAD_SUC:
				//����XML�ļ�
				try{
					parser(mLocalFileName);
					Log.i(TAG, "FTP_DOWNLOAD_SUC");
					deletefile(mLocalFileName);//ʹ���������ɾ����
				} catch (Exception e) {  
	    	 		e.printStackTrace();
	    	 	}  
				break;
			case FTP_DOWNLOAD_FAIL:
				Log.i(TAG, "FTP_DOWNLOAD_FAIL");
				break;
        	}
        }  
  
    };  
//__________________________________�����ļ�_______________________________________________________
    class ftpThread extends Thread {
     	 @Override  
       public void run() { 
     		try {
     			String[] welcome=ftpClient.connect(host, port);
     			if (welcome != null) {
  				for (String value : welcome) {
  					Log.i(TAG, "connect " + value);
  				}//for (String value : welcome) {
  			}//if (welcome != null) {
     		ftpClient.login(userName, password);
     		} catch (Exception ex) {
     			ex.printStackTrace();
     			Log.i(TAG, "FTP_LOGIN_FAIL");
     			handlerFtp.sendEmptyMessage(FTP_LOGIN_FAIL);
     			return;
     		}//try
     		try {
     			ftpClient.download(mRemoteFileName, new File(mLocalFileName));
     			ftpClient.disconnect(true);
     			handlerFtp.sendEmptyMessage(FTP_DOWNLOAD_SUC);
     			return;
     		} catch (Exception ex) {
				ex.printStackTrace();
     			Log.i(TAG, "FTP_DOWNLOAD_FAIL");
     			handlerFtp.sendEmptyMessage(FTP_DOWNLOAD_FAIL);
				return ;
			}
     	 }//public void run() { 
      }// class ftpThread extends Thread {
  //_______________________________________����XML�ļ�_____________________________________________________
    public boolean decriptfile(String filename){
    	return FileCipherUtil.decrypt(filename, null);
    }
  //_______________________________________����XML�ļ�_____________________________________________________
    public void parser(String xmlfilename) throws Exception{
    	try {  
    			File f=new File(xmlfilename);
    			NodeList items=null;
    			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
    			DocumentBuilder builder=factory.newDocumentBuilder();
    			Document doc = builder.parse(f);
    			Element rootElement = doc.getDocumentElement(); 
    			
    			items = rootElement.getElementsByTagName("version");
    			if(items.getLength()>0)Config.new_version= items.item(0).getFirstChild().getNodeValue();
    			Config.getConfig(null).setNewVersion(Config.new_version);
    			
    			items = rootElement.getElementsByTagName("contact");
    			if(items.getLength()>0)Config.contact= items.item(0).getFirstChild().getNodeValue();
    			Config.getConfig(null).setContactWay(Config.contact);
    			
    			items = rootElement.getElementsByTagName("ad");
    			if(items.getLength()>0)Config.ad= items.item(0).getFirstChild().getNodeValue();
    			Config.getConfig(null).setAd(Config.ad);
    			
    			items = rootElement.getElementsByTagName("download");
    			if(items.getLength()>0)Config.download= items.item(0).getFirstChild().getNodeValue();
    			Config.getConfig(null).setDownloadAddr(Config.download);
    			
    			items = rootElement.getElementsByTagName("homepage");
    			if(items.getLength()>0)Config.homepage= items.item(0).getFirstChild().getNodeValue();
    			Config.getConfig(null).setHomepage(Config.homepage);
    			
    			items = rootElement.getElementsByTagName("warning");
    			if(items.getLength()>0)Config.warning= items.item(0).getFirstChild().getNodeValue();
    			Config.getConfig(null).setWarning(Config.warning);
    			
    	 	} catch (Exception e) {  
    	 		Log.e(TAG, e.getMessage()); 
    	 		e.printStackTrace();
    	 	}  
    }
  //_______________________________________����_____________________________________________________
    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    private void deletefile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }
    //---------------------------------------------------------------------------------------------
   }
//
