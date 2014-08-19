package com.baidu.book;

import java.io.Serializable;
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
public class BookList_Construct implements Serializable {
	
	public List<BookMeta> booklist;
	
	public BookList_Construct(){
		this.booklist = new ArrayList <BookMeta>();
	}
	public BookList_Construct(List<BookMeta> booklist){
		this.booklist = booklist;
		
	}
	
	public void add_BookList(BookMeta bookmeta){
		
		booklist.add(bookmeta);
	}
	public List<BookMeta> get_BookList(){
		
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