package bkd;
import java.net.*;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
public class Main {
	static Queue<String> list = new LinkedList<>();
	static HashMap<String, LinkedList<String>> map = new HashMap<>();
	public static void main(String[] args) throws IOException {
		BufferedReader s = new BufferedReader(new InputStreamReader(System.in));
		String url;
		int n = Integer.parseInt(s.readLine());
		url = s.readLine();
		list.add(url);
		outer:
		for(int i = 0; i < n; i++) {
			try {
				String turl = list.poll();
				try {
					new URL(turl);
				} catch(MalformedURLException ex) {
					System.out.println("Syntax Error:" + turl);
					continue;
				}
				LinkedList<String> tlist = new LinkedList<>();
				Document html = Jsoup.connect(turl).get();
				Elements links = html.select("a[href]");
				for(Element e:links) {
					String nlink = urlcheck(e.attr("href"), turl);
					if(map.containsKey(nlink)) {
						continue outer;
					}
					tlist.add(nlink);
				}
				list.addAll(tlist);
				map.put(turl, tlist);
			}catch(NoSuchElementException ex) {
				System.out.println("Reached the end");
				return;
			}
		}
		for(String e:map.keySet()) {
			System.out.println(e);
		}
	}
	static String urlcheck(String url, String lurl) {
		if(!lurl.endsWith("/")) {
			lurl = lurl + "/";
		}
		if(url.length() < 4) {
			if(url.equals("./")) {
				String[] parts = lurl.split("/");
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
				return lurl;
			}
			if(url.equals("../")) {
				String[] parts = lurl.split("/");
				return parts[0] + "//" + parts[2];
			}
		}
		String sub = url.substring(0, 7);
		if(!(sub.equals("https:/")||sub.equals("http://"))) {
			if(url.startsWith("/")) {
				url = url.substring(1, url.length());
			}
			if(url.startsWith(".")) {
				if(url.substring(0, 2).equals("./")) {
					String[] parts = lurl.split("/");
					lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
					url = url.substring(2, url.length());
				}
				if(url.substring(0, 3).equals("../")) {
					String[] parts = lurl.split("/");
					lurl = parts[0] + "//" + parts[2] + '/';
					url = url.substring(3, url.length());
				}
			}
			System.out.println(lurl + " " + url);
			url = lurl + url;
		}
		return url;
	}
	
}
