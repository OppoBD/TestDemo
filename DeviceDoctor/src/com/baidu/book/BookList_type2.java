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
public class BookList_type2 {
	
	public List<BookMeta> booklist;
	
	public BookList_type2(){
		this.booklist = set_BookList_type2();
	}
	
	public List<BookMeta> set_BookList_type2(){
		
		int id =0;
		booklist = new ArrayList <BookMeta>();
		booklist.add(new BookMeta(2,id++,"如何知道我的DNS该设置为多少?","一般需要咨询您的网络供应商，常见的DNS可参考http://www.114dns.com/DNS_List.html",""));
		return booklist;
	}
	public List<BookMeta> get_BookList_type2(){
		
		return this.booklist;
	}
	
	public String get_result(){
		String res ="";
		Iterator<BookMeta> ite = this.booklist.iterator();
		while(ite.hasNext()){
			BookMeta bm = (BookMeta)ite.next();
			res += "<b>问: </b>"+bm.appearance +"<br><b>答: </b>"+bm.reason+"<br><br>"; 
		}
		return res;
	}
   	
}