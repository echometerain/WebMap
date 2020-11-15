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
		args = new String[3];
		args[0] = s.readLine();
		args[1] = s.readLine();
		args[2] = s.readLine();
		s.close();
		switch(args[0]) {
		case "--index":
			int n = 0;
			try {
				n = Integer.parseInt(args[1]);
			}catch(NumberFormatException e) {
				System.out.println("Repetition size must be a number");
			}
			for(int i = 2; i < args.length; i++) {
				list.add(urlfix(args[i]));
			}
			index(n);
			break;
		case "--load":
			break;
		case "--dir":
			break;
		}
	}
	static void index(int re) {
		outer:
		for(long i = 0; i < re; i++) {
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
					//i--;
					//continue;
					return;
				}
				for(Element e:html.select("a[href]")) {
					String nlink = urlfix(urlmerge(e.attr("href"), turl));
					if(map.containsKey(nlink)) {
						i--;
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
			System.out.print(i+"/"+re + " complete\r");
		}
		/*for(String e:map.keySet()) {
			System.out.println(e);
			for(String e2:map.get(e)) {
				System.out.println(" |- " + e2);
			}
			System.out.println();
		}*/
	}
	static String urlmerge(String url, String lurl) {
		if(url.length() == 0) return lurl;
		String[] parts = lurl.split("/");
		System.out.println();
		System.out.println(lurl+ " "+url);
		if(url.charAt(0) == '#') {
			//if(lurl.charAt(lurl.length()-1) == '/') {
			//	lurl = lurl.substring(0, lurl.length()-1);
			//}
			return lurl+url;
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
			if(parts.length>2 && parts[parts.length-1].contains(".")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
			}
			if(url.startsWith("./")) {
				url = url.substring(2, url.length());
			}
			else if(url.startsWith("../../")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+parts[parts.length-2].length()+2));
				url = url.substring(6, url.length());
			}
			else if(url.startsWith("../")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
				url = url.substring(3, url.length());
			}
			else return lurl;
		}
		if(lurl.charAt(lurl.length()-1) == '/') {
			lurl = lurl.substring(0, lurl.length()-1);
		}
		if(url.length()>0 && url.charAt(0) != '/') {
			url = "/"+url;
		}
		url = lurl + url;
		System.out.println(url);
		return url;
	}
	static String urlfix(String url) {
		String root;
		try {
			root = url.split("/")[2];
		}catch(Exception ex) {return url;}
		if(root.split(".").length == 2) {
			url = "//www."+url.substring(2);
		}
		return url;
	}
}
