package com.baidu.book;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

/**
 * 解决方案相关信息
 * 
 * @author li_jing02
 * 
 */
public class BookList_type0 {
	
	public List<BookMeta> booklist;
	
	public  BookList_type0(){
		this.booklist = set_BookList_type0();
		Log.d("booklist size 111:", booklist.size()+"");
	}
	
	public List<BookMeta> set_BookList_type0(){
		
		int id =0;
		
		List<BookMeta> booklist = new ArrayList <BookMeta>();
		booklist.add(new BookMeta(0,id++,"一般路由器上有对应的wan口连接的灯，该灯不亮","wan口没连好","Wan口插好即可"));
		booklist.add(new BookMeta(0,id++,"路由器灯正常，但是上不了网","dns设置问题或者DHCP问题","修改dns值或者DHCP关闭改用静态ip"));
		booklist.add(new BookMeta(0,id++,"近距离能连上，隔墙或者远距离后发现能搜到对应的wifi信号，但是还是上不了网，提示\"目标网络无法接入，请重试\"","可能你的路由器功率较大，穿透距离强，但是wifi通信具有对称性，你的客户端（手机或者pad、电脑）功率不够无法支持远距离或者穿墙功能","加一个路由中继或者调整路由器的安装位置使其再各种使用场合离客户端都比较近"));
		booklist.add(new BookMeta(0,id++,"手机连不上网，电脑能连上","路由器信道可能选择了不支持的信道范围，如果你的手机是日产、美产的范围，只支持1-11的信道，对于12-13信道不支持。","登录路由器后台，人工设置信道为1-11之内（建议1、6、11）"));
		booklist.add(new BookMeta(0,id++,"手机或者pad升级后，无法上网","驱动不支持wmm（音视频优先）功能","登录路由器后台，关闭该选项"));
		booklist.add(new BookMeta(0,id++,"wifi没问题，手机能连上外网，某智能硬件连不上（如i耳目）","不支持路由选择的wifi标准，如i耳之前期不支持802.11x	","修改路由器网络规范，不要选择混合模式和不支持的规范"));
		
		Log.d("booklist size", booklist.size()+"");
		return booklist;
	}
	
	public List<BookMeta> get_BookList_type0(){
		Log.d("booklist size 222:", booklist.size()+"");
		return this.booklist;
	}
	
	public String get_result(){
		String res ="";
		Iterator<BookMeta> ite = this.booklist.iterator();
		while(ite.hasNext()){
			BookMeta bm = (BookMeta)ite.next();
			res += "<b>现象: </b>"+bm.appearance +"<br><b>原因: </b>"+bm.reason+"<br><b> 解决方案: </b>"+bm.solution+"<br><br>"; 
		}
		return res;
	}
   	
}