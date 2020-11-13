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
		int n = 0;
		try {
			n = Integer.parseInt(s.readLine());
		}catch(NumberFormatException e) {
			System.out.println("Repetition size must be a number");
		}
		String url = s.readLine();
		list.add(url);
		outer:
		for(int i = 0; i < n; i++) {
			try {
				String turl = list.poll();
				if(list.poll() == null && i > 0)return;
				if(map.containsKey(turl)) {
					continue outer;
				}
				LinkedList<String> tlist = new LinkedList<>();
				Document html;
				try {
					html = Jsoup.connect(turl).get();
				} catch(MalformedURLException ex) {
					System.out.println("Syntax Error:" + turl);
					continue;
				}
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
			System.out.print(i + "% \r");
		}
		for(String e:map.keySet()) {
			System.out.println(e);
		}
	}
	static String urlcheck(String url, String lurl) {
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
		if(!lurl.endsWith("/")) {
			lurl = lurl + "/";
		}
		try {
			if(url.substring(0, 7).equals("https:/")||url.substring(0, 7).equals("http://")) return url;
		}catch(StringIndexOutOfBoundsException ignored) {}
		if(url.startsWith("/")) {
			url = url.substring(1, url.length());
		}
		else if(url.startsWith("([A-z])")) {
			String[] parts = lurl.split("/");
			lurl = parts[0]+"//"+parts[1];
		}
		else if(url.startsWith(".")) {
			System.out.print("I got here");
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
		url.replace("\r", "");
		if(!url.endsWith("/")) {
			url = url + "/";
		}
		url = lurl + url;
		System.out.println(lurl+ " "+url);
		return url;
	}
	
}
