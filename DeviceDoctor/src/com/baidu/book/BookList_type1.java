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
public class BookList_type1 {
	
	public List<BookMeta> booklist;
	
	public BookList_type1(){
		this.booklist = set_BookList_type1();
	}
	
	public List<BookMeta> set_BookList_type1(){
		
		int id =0;
		booklist = new ArrayList <BookMeta>();
		booklist.add(new BookMeta(1,id++,"平时网络比较稳定，突然某一天开始网速极其慢","可能账号被盗，其他用户跟你共享网络资源或者遭遇了ARP攻击","一般智能路由都有设备管理功能，剔除非预期的设备即可；密码设置复杂一点，防止被盗；通过ping内外网确认是否受到攻击"));
		booklist.add(new BookMeta(1,id++,"网络相对不稳定，一天时间内时好时坏","同时在使用一些p2p软件下载资源，带宽占用较大","关闭相关的软件"));
		booklist.add(new BookMeta(1,id++,"某次聚会活动后发现网络相对不稳定","网络中存在一些僵尸节点	","尽量避免僵尸点存在，后台剔除一些已经不在但是还占用网路资源的节点"));
		booklist.add(new BookMeta(1,id++,"网络突然变慢","附近有电磁干扰，比如开了电磁炉等设备","如果路由器和设备支持的话，切换到5G频道，不支持的话，通过wifi分析仪找一个信道通畅的连接过去。"));
		booklist.add(new BookMeta(1,id++,"部分网址访问慢","本地网络运营商和网址对应的运营商不是同一家","暂无"));
		booklist.add(new BookMeta(1,id++,"部分网址无法访问","路由器设置了防火墙","解除防火墙设置"));
		booklist.add(new BookMeta(1,id++,"使用一段时间后，网速越来越慢","路由器CPU或者内存问题","重启路由"));
		return booklist;
	}
	public List<BookMeta> get_BookList_type1(){
		
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