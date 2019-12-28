package com.example.h3;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityManager;
import android.util.Log;
import android.widget.Toast;
import android.annotation.TargetApi;
import android.os.Build;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.Iterator;

import com.example.h3.job.WechatAccessbilityJob;
/**
 * <p>Created by byc</p>
 * <p/>
 * �������ҷ���
 */
public class QiangHongBaoService extends AccessibilityService {
	//�����ʶ
	private static String TAG = "byc001";

	//�����Ϣ�Ĺؼ���
	//static final String WECHAT_RECEIVE_UI= "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
	//static final String WECHAT_DETAIL_UI= "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
	//private String mCurWindow="";
	//private boolean bReadClick=false;
	//��ȡ�������ID
	 //static String nID = ""; 
	//��ʵ������
	 private static QiangHongBaoService service;
	 //job����
	 private WechatAccessbilityJob job;
	 @Override
	 public void onCreate() {
		 super.onCreate();
	     service = this;
	     TAG=Config.TAG;
	     Log.i(TAG, "qianghongbao service onCreate");
	     //job=WechatAccessbilityJob.getJob();
	     //job.onCreateJob(service);
	}
	 public Config getConfig() {
	        return Config.getConfig(this);
	 }

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		job.onReceiveJob(event);
	}//public
    @Override
    public void onInterrupt() {
    	 Log.d(TAG, "qianghongbao service interrupt");
        Toast.makeText(this, "�ж����������", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        service = this;
        TAG=Config.TAG;
        Log.i(TAG, "qianghongbao service onServiceConnected");
        job=WechatAccessbilityJob.getJob();
        job.onCreateJob(service);
        
        //���͹㲥���Ѿ���������
        Intent intent = new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        sendBroadcast(intent);
    }
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    Log.d(TAG, "qianghongbao service destory");
	    job.onStopJob();

	    service = null;
	    job = null;
        //���͹㲥���Ѿ��Ͽ���������
        Intent intent = new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        sendBroadcast(intent);
	}
    
    /**
     * �жϵ�ǰ�����Ƿ���������
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isRunning() {
        if(service == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = service.getServiceInfo();
        if(info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if(i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if(!isConnect) {
            return false;
        }
        return true;
    }
    /*
    //
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void recycle(AccessibilityNodeInfo info) {
		if (info.getChildCount() == 0) {
			//info.setText("9.9");
			Log.i(TAG, "child widget----------------------------" + info.getClassName());
			//Log.i(TAG, "showDialog:" + info.canOpenPopup());
			Log.i(TAG, "Text��" + info.getText());
			Log.i(TAG, "windowId:" + info.getWindowId());
			Log.i(TAG, "ResouceId:" + info.getViewIdResourceName());
			Log.i(TAG, "isClickable:" + info.isClickable());
			Log.i(TAG, "isEditable:" + info.isEditable());
			if(info.isEditable())info.setText("aabb");
		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if(info.getChild(i)!=null){
					recycle(info.getChild(i));
				}
			}
		}
	}
   
    //����
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void testUI() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindowΪ��");
            
            return;
        }
        recycle(nodeInfo);
    }
    
    //����
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindowΪ��");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("����");//�_ ����
        for (AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

   
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindowΪ��");
            Toast.makeText(this, "rootWindowΪ��", Toast.LENGTH_SHORT).show();
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("��ȡ���");
        if (list.isEmpty()) {
        	Toast.makeText(this, "list.isEmpty", Toast.LENGTH_SHORT).show();
            list = nodeInfo.findAccessibilityNodeInfosByText(ENVELOPE_TEXT_KEY);
            for (AccessibilityNodeInfo n : list) {
                Log.i(TAG, "-->΢�ź��:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //���µĺ������
        	Log.d(TAG, "-->�������:" + list.size());
        	 //���µĺ������
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                
                Log.d(TAG, "-->nID:" + nID+";parent:"+parent.toString().substring(49,57));
                if(nID.equals(parent.toString().substring(49,57)))break;
                
                Log.d(TAG, "-->��ȡ���:" + parent);
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    nID=parent.toString().substring(49,57);
                    Log.d(TAG, "-->nID:" + nID);
                    break;
                }
            }

        }
    }
    */
    
    
    /*
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openEnvelope(AccessibilityEvent event) {
    	
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            //�����˺������һ������ȥ����
        	UnpackRedPackage() ;
            //checkKey1();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            //����������ϸ�ļ�¼����
            //nonething
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            //���������,ȥ���к��
        	
            //checkKey2();
        }
    }
    */
}
