package bkd;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;
public class Main {
	public static Queue<String> list = new LinkedList<>();
	public static HashMap<String, LinkedList<String>> map = new HashMap<>();
	static String sl = "/";
	static String dir = System.getProperty("user.dir");
	public static void main(String[] args) throws IOException {
		
		BufferedReader s = new BufferedReader(new InputStreamReader(System.in));
		args = new String[3];
		args[0] = s.readLine();
		args[1] = s.readLine();
		args[2] = s.readLine();
		s.close();
		
		if(System.getProperty("os.name").startsWith("Windows")) sl = "\\";
		dir = sl+"Data"+sl;
		if(!new File(dir).isDirectory()) {
			new File(dir).mkdir();
		}
		if(new File(dir+args[0]).isDirectory()) {
			dir = dir+args[0];
			load();
			System.out.print("Loading...\r");
		}else {
			dir = dir+args[0];
			new File(dir).mkdir();
		}
		int n = 0;
		try {
		n = Integer.parseInt(args[1])+1;
		}catch(NumberFormatException e) {
			System.out.println("Repetition size must be a number");
		}
		for(int i = 2; i < args.length; i++) {
			list.add(args[i]);
			}
		index(n);
		save();
	}
	static void load() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(dir+"index.txt"));
		for(String s = reader.readLine(); s != null; s = reader.readLine()) {
			String[] tlinks = s.split(" ");
			LinkedList<String> links = new LinkedList<>(Arrays.asList(tlinks));
			links.removeFirst();
			map.put(tlinks[0], links);
		}
		reader.close();
		reader = new BufferedReader(new FileReader(dir+"queue.txt"));
		LinkedList<String> links = new LinkedList<>(Arrays.asList(reader.readLine().split(" ")));
		list.addAll(links);
		reader.close();
	}
	static void save() throws IOException {
		System.out.print("Saving...\r");
		new File(dir + "index.txt").createNewFile();
		FileWriter writer = new FileWriter(dir + "index.txt");
		for(String e:map.keySet()) {
			writer.write(e+" ");
			for(String e2:map.get(e)) {
				writer.write(e2+" ");
			}
			writer.write("\n");
		}
		writer.close();
		FileWriter w2 = new FileWriter(dir + "queue.txt");
		while(!list.isEmpty()) {
			w2.write(list.poll()+" ");
		}
		w2.close();
		System.out.print("Complete. \r");
	}
	static void index(int re) {
		outer:
		for(long i = 0; i < re; i++) {
			try {
				String turl = list.poll();
				if(list.poll() == null && i > 0)return;
				if(map.containsKey(turl)) {
					i--;
					continue outer;
				}
				LinkedList<String> tlist = new LinkedList<>();
				Document html;
				try {
					html = Jsoup.connect(turl).get();
					turl = html.location();
				} catch(Exception ex) {
					System.out.println("Unable to reach: " + turl);
					i--;
					continue;
					//return;
				}
				for(Element e:html.select("a[href]")) {
					String nlink = urlmerge(e.attr("href"), turl);
					if(map.containsKey(nlink)) {
						continue;
					}
					tlist.add(nlink);
				}
				list.addAll(tlist);
				map.put(turl, tlist);
			}catch(NoSuchElementException ex) {
				System.out.println("Reached the end");
				return;
			}
			System.out.print(i+"/"+(re-1) + " complete\r");
		}
	}
	static String urlmerge(String url, String lurl) {
		if(url.length() == 0) return lurl;
		String[] parts = lurl.split("/");
		//System.out.println();
		//System.out.println(lurl+ " "+url);
		try {
			if(url.startsWith("http:")||url.startsWith("https:")) return url;
			if(url.contains(":")) return lurl;
			if(url.startsWith("//")) return parts[0]+url;
		}catch(Exception ignored) {}
		if(url.charAt(0) == '#') {
			return lurl+url;
		}
		try {
		if(url.charAt(0) == '/') {
			lurl = parts[0]+"//"+parts[2];
		}
		else if(url.charAt(0) == '.') {
			if(parts.length>3 && parts[parts.length-1].contains(".")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()));
				parts = lurl.split("/");
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
		else {
			if(parts.length>3 && parts[parts.length-1].contains(".")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
			}
		}
		}catch(StringIndexOutOfBoundsException ex) {}
		if(lurl.charAt(lurl.length()-1) == '/') {
			lurl = lurl.substring(0, lurl.length()-1);
		}
		if(url.length()>0 && url.charAt(0) != '/') {
			url = "/"+url;
		}
		url = lurl + url;
		//System.out.println(url);
		return url;
	}
}
