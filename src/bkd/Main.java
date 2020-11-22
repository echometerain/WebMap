package bkd;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.NoSuchElementException;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class Main {
	public static BidiMap<Integer, String> list = new DualHashBidiMap<>();
	//public static HashMap<String, LinkedList<String>> map = new HashMap<>();
	private static int llen = 1;
	private static int lstart = 1;
	private static String sl = "/";
	private static String dir = System.getProperty("user.dir");
	public static void main(String[] args) throws IOException {
		
		if(System.getProperty("os.name").startsWith("Windows")) sl = "\\";
		dir = dir+sl+"Data"+sl;
		if(!new File(dir).isDirectory()) {
			new File(dir).mkdir();
		}
		
		BufferedReader s = new BufferedReader(new InputStreamReader(System.in));
		String st = s.readLine();
		s.close();
		args = st.split(" ");
		
		if(new File(dir+args[0]+".index").isFile() && new File(dir+args[0]+".map").isFile()) {
			load(args[0]);
			System.out.print("Loading...\r");
		}else {
			new File(dir+args[0]+".index").createNewFile();
			new File(dir+args[0]+".map").createNewFile();
		}
		int n = 0;
		try {
		n = Integer.parseInt(args[1])+1;
		}catch(NumberFormatException e) {
			System.out.println("Repetition size must be a number");
		}
		for(int i = 2; i < args.length; i++) {
			list.put(llen,args[i]);
			llen++;
		}
		
		map(n, args[0]);
	}
	static void load(String name) throws IOException{
		if(!new File(dir+name+".index").isFile()) {
			System.out.println("index not found");
			return;
		}
		BufferedReader reader = new BufferedReader(new FileReader(dir+name+".index"));
		System.out.println(dir+name+".index");
		if(!new File(dir+name+".map").isFile()) {
			System.out.println("map not found");
			reader.close();
			return;
		}
		for(String s = reader.readLine(); s != null; s = reader.readLine()) {
			list.put(llen, s);
			llen++;
		}
		llen--;
		lstart = Integer.parseInt(list.get(llen));
		list.remove(llen);
		reader.close();
	}
	static void loadjson(String name) throws IOException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(dir+name+".json"));
		}catch(FileNotFoundException ex) {
			System.out.println("json not found");
			return;
		}
		BufferedWriter writemap = new BufferedWriter(new FileWriter(dir + name + ".map"));
		BufferedWriter writein = new BufferedWriter(new FileWriter(dir + name + ".index"));
		StringBuilder st = new StringBuilder();
		for(String s = reader.readLine(); s == null; s = reader.readLine()) st.append(s);
		reader.close();
		String[] arr = st.toString().split("([\\[\\]])");
		for(int i = 0; i < arr.length-1; i+=2) {
			HashSet<String> llinks = new HashSet<>();
			String[] ar2 = arr[i+1].split("\"");
			for(int ii = 1; ii < ar2.length-1; ii+=2) {
				llinks.add(ar2[ii]);
			}
			writemap.append(arr[i].split("\"")[1]);
			writein.append("404");
			for(String e:llinks) {
				writemap.append(" "+e);
				writein.append("\n"+e);
			}
			writemap.append("\n");
		}
		writemap.close();
		writein.close();
	}
	static void export(String name) throws IOException {
		/*
		String st = new String();
		st += "{";
		for(String e:map.keySet()) {
			st += "\""+e+"\":[";
			for(String e2:map.get(e)) {
				st += "\""+e2+"\",";
			}
			if(st.charAt(st.length()-1)==',')st.substring(0, st.length()-1);
			st += "],\n";
		}
		if(st.charAt(st.length()-2)==',')st.substring(0, st.length()-2);
		if(st != null)st += "}";
		new File(dir + "map.json").createNewFile();
		FileWriter writer = new FileWriter(dir + "map.json");
		writer.write(st);
		writer.close();
		FileWriter w2 = new FileWriter(dir + "index.txt");
		while(!list.isEmpty()) {
			w2.write(list.get(llen)+"\n");
			llen++;
		}
		w2.close();
		System.out.println("Finished.");
		*/
	}
	static void map(int re, String name) throws IOException{
		BufferedWriter writemap = new BufferedWriter(new FileWriter(dir + name + ".map", true));
		BufferedWriter writein = new BufferedWriter(new FileWriter(dir + name + ".index", true));
		try {
		for(long i = 1; i < re; i++) {
			String turl = list.get(lstart);
			//System.out.println(turl);
			lstart++;
			if(turl == null) {
				writein.close();
				writemap.close();
				return;
			}
			HashSet<String> tlist = new HashSet<>();
			Document html;
			try {
				html = Jsoup.connect(turl).get();
				turl = html.location();
			} catch(Exception ex) {
				System.out.println(0);
				System.out.println("Unable to reach: " + turl);
				i--;
				continue;
				//return;
			}
			for(Element e:html.select("a[href]")) {
				String nlink = urlmerge(e.attr("href"), turl);
				if(list.containsValue(nlink)) {
					continue;
				}
				tlist.add(nlink);
			}
			writemap.append(turl);
			for(String e:tlist) {
				writemap.append(" "+e);
				writein.append(e+"\n");
				if(!list.containsValue(e)) {
					list.put(llen, e);
					llen++;
				}
			}
			writemap.append("\n");
			System.out.print(i+"/"+(re-1)+" complete\r");
		}
		}catch(NoSuchElementException ex) {
			System.out.println("Reached the end");
			writein.close();
			writemap.close();
			return;
		}
		writein.append(Integer.toString(lstart));
		writein.close();
		writemap.close();
	}
	static String urlmerge(String url, String lurl) {
		if(url.length() == 0) return lurl;
		String[] parts = lurl.split("/");
		while(true) {
			if(url.charAt(0) == '\r'||url.charAt(0) == '\t'||url.charAt(0) == '\n'||url.charAt(0) == ' ') {
				url = url.substring(1);
			}
			else break;
		}
		try {
			if(url.startsWith("http:")||url.startsWith("https:")) return url;
			if(url.contains(":")) return lurl;
			if(url.startsWith("//")) return parts[0]+url;
		}catch(Exception ignored) {}
		if(url.charAt(0) == '#'||url.charAt(0) == '?') {
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
			url = "/"+ url;
		}
		url = lurl + url;
		//System.out.println(url);
		return url;
	}
}
