package bkd;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;
public class Main {
	static FileReader files;
	static Queue<String> list = new LinkedList<>();
	static HashMap<String, LinkedList<String>> map = new HashMap<>();
	public static void main(String[] args) throws IOException {
		BufferedReader s = new BufferedReader(new InputStreamReader(System.in));
		args = new String[4];
		args[0] = s.readLine();
		args[1] = s.readLine();
		args[2] = s.readLine();
		s.close();
		switch(args[0]) {
		case "--index":
			index(args);
			break;
		case "--load":
			
		}
	}
	static void index(String[] args) {
		int n = 0;
		try {
			n = Integer.parseInt(args[1]);
		}catch(NumberFormatException e) {
			System.out.println("Repetition size must be a number");
		}
		for(int i = 2; i < args.length; i++) {
			list.add(urlfix(args[i]));
		}
		outer:
		for(long i = 0; i < n; i++) {
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
				} catch(Exception ex) {
					System.out.println("Syntax Error: " + turl);
					continue;
				}
				for(Element e:html.select("a[href]")) {
					String nlink = urlfix(urlmerge(e.attr("href"), turl));
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
			System.out.print(i+"/"+n + " complete\r");
		}
		for(String e:map.keySet()) {
			System.out.println(e);
			for(String e2:map.get(e)) {
				System.out.println(" |- " + e2);
			}
			System.out.println();
		}
	}
	static String urlmerge(String url, String lurl) {
		if(url.length() == 0) return lurl;
		String[] parts = lurl.split("/");
		//System.out.println();
		//System.out.println(lurl+ " "+url);
		if(url.equals("../")) {
			return parts[0] + "//" + parts[2];
		}
		if(url.charAt(0) == '#') {
			if(lurl.charAt(lurl.length()-1) == '/') {
				lurl = lurl.substring(0, lurl.length()-1);
			}
			return lurl+url;
		}
		if(lurl.charAt(lurl.length()-1) != '/') {
			lurl = lurl + "/";
		}
		try {
			if(url.startsWith("https")||url.startsWith("http:")) return url;
			if(url.startsWith("//")) {
				url = Jsoup.connect(url).get().location();
				return url;
			}
		}catch(Exception ignored) {}
		if(url.charAt(0) == '/') {
			lurl = parts[0]+"//"+parts[2];
		}
		else if(url.charAt(0) == '.') {
			if(url.startsWith("./")) {
				//String[] parts = lurl.split("/");
				//lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
				url = url.substring(2, url.length());
			}
			else if(url.startsWith("../")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
				url = url.substring(3, url.length());
			}
			else return lurl;
		}
		url = lurl + url;
		//System.out.println(url);
		return url;
	}
	static String urlfix(String url) {
		//if(url.charAt(0) != '/') {
			//if(url.charAt(0) == 'h') url = url.substring(5);
			//if(url.charAt(0) == ':') url = url.substring(1);
		//}
		String root = url.split("/")[2];
		if(root.split(".").length == 2) {
			url = "//www."+url.substring(2);
		}
		return url;
	}
}
