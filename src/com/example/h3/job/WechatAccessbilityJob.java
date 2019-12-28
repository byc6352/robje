/**
 * 
 */
package com.example.h3.job;

import java.util.Timer;
import java.util.TimerTask;

import com.example.h3.BackgroundMusic;
import com.example.h3.Config;
import com.example.h3.MainActivity;
import com.example.h3.QiangHongBaoService;
import com.example.h3.util.AccessibilityHelper;
import com.example.h3.util.FloatingWindowPic;
import com.byc.robje.R;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
/**
 * @author byc
 *
 */
public class WechatAccessbilityJob extends BaseAccessbilityJob  {
	
	private static WechatAccessbilityJob current;
	//-------------------------------拆包延时---------------------------------------------
	private int mWechatOpenDelayTime=0;//拆包延时
	private SpeechUtil speeker ;
	private FloatingWindowPic fwp;
	private BackgroundMusic mBackgroundMusic;

	public int mLuckyMoneyType=0;//红包类别：已拆过，福利包，有雷包；
	private LuckyMoneyReceiveJob mReceiveJob;
	private LuckyMoneyDetailJob mDetailJob;
	private LuckyMoneyLauncherJob mLauncherJob;
	private LuckyMoney mLuckyMoney;//红包对象；
	private String mCurrentUI="";
	private AccessibilityNodeInfo mRootNode; 
	private boolean bDel=false;//删除广告语
	


    @Override
    public void onCreateJob(QiangHongBaoService service) {
        super.onCreateJob(service);
        mReceiveJob=LuckyMoneyReceiveJob.getLuckyMoneyReceiveJob(context);
        mDetailJob=LuckyMoneyDetailJob.getLuckyMoneyDetailJob(context);
        mLauncherJob=LuckyMoneyLauncherJob.getLuckyMoneyLauncherJob(context);
        mLuckyMoney=LuckyMoney.getLuckyMoney(context);//红包对象；
        speeker=SpeechUtil.getSpeechUtil(context);
        mBackgroundMusic=BackgroundMusic.getInstance(context);
        
		fwp=FloatingWindowPic.getFloatingWindowPic(context,R.layout.float_click_delay_show);
		int w=Config.screenWidth-200;
		int h=Config.screenHeight-200;
		fwp.SetFloatViewPara(100, 200,w,h);
		//接收广播消息
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_CLICK_LUCKY_MONEY);
        context.registerReceiver(ClickLuckyMoneyReceiver, filter);
    }
    @Override
    public void onStopJob() {

    }
    public static synchronized WechatAccessbilityJob getJob() {
        if(current == null) {
            current = new WechatAccessbilityJob();
        }
        return current;
    }
    
    //----------------------------------------------------------------------------------------
    @Override
    public void onReceiveJob(AccessibilityEvent event) {
    	
    	final int eventType = event.getEventType();
    	String sClassName=event.getClassName().toString();
		//++++++++++++++++++++++++++++++++++++窗体改变+++++++++++++++++++++++++++++++++++++++++++++++++
    	Log.i(TAG, sClassName);
		//mRootNode=event.getSource();
		//if(mRootNode==null)return;
		//AccessibilityHelper.printParentInfo(mRootNode);
		//mRootNode=AccessibilityHelper.getRootNode(mRootNode);
		//AccessibilityHelper.recycle(mRootNode);
		if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			mCurrentUI=sClassName;
			Log.i(TAG, sClassName);
			mRootNode=event.getSource();
			//弹出菜单窗体：-----------------删除 广告语---------------------------------------------------------
			if(bDel){
			if(mCurrentUI.equals("com.tencent.mm.ui.base.k")){
				AccessibilityNodeInfo adPop=AccessibilityHelper.findNodeInfosByText(mRootNode, "删除",-1);
				if(adPop!=null){
					AccessibilityNodeInfo parent=adPop.getParent();
					if(parent!=null)parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					bDel=false;
				}
			}
			}
			//-------------------------拆红包界面----------------------------------------------------
			if(Config.wv<1020){
					if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_RECEIVEUI)){
						if(UnpackLuckyMoney(mRootNode))
							Config.JobState=Config.STATE_CLICK_LUCKYMONEY;
						else
							Config.JobState=Config.STATE_NONE;
					}
			}else{
					if(mCurrentUI.indexOf(Config.WINDOW_LUCKYMONEY_RECEIVEUI_2)!=-1){
						if(UnpackLuckyMoney(mRootNode))
							Config.JobState=Config.STATE_CLICK_LUCKYMONEY;
						else
							Config.JobState=Config.STATE_NONE;
					}
			}
			//-------------------------显示详细信息界面----------------------------------------------------
			if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_DETAILUI)){
				if(Config.JobState==Config.STATE_NONE)return;
				UnpackLuckyMoneyShow(mRootNode) ;
				Config.JobState=Config.STATE_NONE;
			}
		}//if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) 
		if (eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
			//mRootNode=event.getSource();
		}
		//+++++++++++++++++++++++++++++++++内容改变+++++++++++++++++++++++++++++++++++++++++++++++
		if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

			if(Config.JobState==Config.STATE_CLICK_LUCKYMONEY)return;
			if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
				mRootNode=event.getSource();
				if(mRootNode==null)return;
				//AccessibilityHelper.printParentInfo(mRootNode);
				mRootNode=AccessibilityHelper.getRootNode(mRootNode);
				//AccessibilityHelper.recycle(mRootNode);
				if(mLauncherJob.isMemberChatUi(mRootNode)){
					//实现长按指定 内容：----------------------------弹出删除菜单-------------------------------------
					AccessibilityNodeInfo adNode=AccessibilityHelper.findNodeInfosByText(mRootNode,mLauncherJob.mStrAD ,-1);
					if(adNode!=null){
						if(adNode.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK))bDel=true;
					}
					//----------------------------------------------------------------------------------
					mLauncherJob.mIntAD=mLauncherJob.mIntAD+1;
					if(mLauncherJob.mIntAD>mLauncherJob.MAX_NO_REG_AD)mLauncherJob.SendAD(mRootNode);//试用版发布广告；
					
					if(clickLuckyMoney(mRootNode))Config.JobState=Config.STATE_CLICK_LUCKYMONEY;			
				}
				//if(Config.getConfig(context).bAutoClearThunder)clickLuckyMoney();
			}//if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
		}//if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) 
		//}//if(WECHAT_PACKAGENAME.equals(pkn))
    }
  
    //红包来了，点击：
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean clickLuckyMoney(AccessibilityNodeInfo rootNode){

    	if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
    		
    		mLuckyMoney.LuckyMoneyNode=mLuckyMoney.getLastLuckyMoneyNode(rootNode);
    		if(mLuckyMoney.LuckyMoneyNode!=null)mLauncherJob.mIntAD=0;//群里面有红包则不发布广告；
    		mLuckyMoney.RobbedLuckyMoneyInfoNode=mLuckyMoney.getLastReceivedLuckyMoneyInfoNode(rootNode);
    		if(mLuckyMoney.isNewLuckyMoney(mLuckyMoney.LuckyMoneyNode, mLuckyMoney.RobbedLuckyMoneyInfoNode)){
    			//if(Config.bNoPayFor)//过滤赔付包：
    			//if(!mLuckyMoney.isLuckyMoneyLei(mLuckyMoney.LuckyMoneyNode))return false;
  
    					//进入抢包状态：
    					Config.JobState=Config.STATE_CLICK_LUCKYMONEY;
    					ClickLuckyMoneyDelay();//点包延时显示；
    					return true;
    			//}
    		}
    	}
        return false;
    }

  
    //拆红包
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean UnpackLuckyMoney(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return false;
        speeker.speak("正在为您分析：");
    	UnpackLuckyMoneyDelay();//拆包延时显示；
    	return mReceiveJob.OpenLuckyMoney(rootNode);//拆包
    	
    }
    //点包延时：
    private void ClickLuckyMoneyDelay() {
        //7.播放背景音乐：
        //mBackgroundMusic=BackgroundMusic.getInstance(context);
        //mBackgroundMusic.playBackgroundMusic( "bg_music.mp3", true);
    	speeker.speak("正在进行数据计算：");
    	mBackgroundMusic.playBackgroundMusic( "ml.wav", true);
		fwp.ShowFloatingWindow(); 
    	fwp.c=getConfig().getWechatOpenDelayTime();
    	fwp.mSendMsg=Config.ACTION_CLICK_LUCKY_MONEY;
    	fwp.mShowPicType=FloatingWindowPic.SHOW_PIC_GREEN;
    	fwp.StartSwitchPics();
    	//fwp.StopSwitchPics();
    	//fwp.RemoveFloatingWindowPic();
    	//mBackgroundMusic.stopBackgroundMusic();
    }
    //拆包延时：
    private void UnpackLuckyMoneyDelay() {
    	
    	//float f=0;
    	mWechatOpenDelayTime=getConfig().getWechatOpenDelayTime();
    	//for(int i=0;i<mWechatOpenDelayTime;i++){
    		//handler.postDelayed(run, 10);
    	//}
    	float f=(float) (Math.random()*10000);
    	String s=String.valueOf(f);
    	Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
	    try{
	    	  Thread.sleep(10*mWechatOpenDelayTime);
	    }catch(Exception e){
	    } 
	    if(mWechatOpenDelayTime>0){
	    	f=(float) (Math.random()*10000);
	    	s=String.valueOf(f);
	    	Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
	    }

    }
    //拆包信息展示：
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void UnpackLuckyMoneyShow(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {return;}
        
        mDetailJob.LuckyMoneyDetailShow(rootNode);
        
    }
	private BroadcastReceiver ClickLuckyMoneyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            //Log.d(TAG, "receive-->" + action);
            if(Config.ACTION_CLICK_LUCKY_MONEY.equals(action)) {
            	mBackgroundMusic.stopBackgroundMusic();
            	//点击红包：
            	if(mLuckyMoney.ClickLuckyMoney(mLuckyMoney.LuckyMoneyNode))
            		Config.JobState=Config.STATE_CLICK_LUCKYMONEY;
            	else
            		Config.JobState=Config.TYPE_LUCKYMONEY_NONE;
            }
        }
    };
}
