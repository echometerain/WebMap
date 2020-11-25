package bkd;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.HashSet;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class Main {
	public static BidiMap<Integer, String> list = new DualHashBidiMap<>();
	private static int llen = 1;
	public static int lstart = 1;
	private static String sl = "/";
	public static String dir = System.getProperty("user.dir");
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
		
		dir+=args[0];
		if(new File(dir+".index").isFile()) {
			load();
			System.out.print("Loading...\r");
		}else {
			new File(dir+".index").createNewFile();
			new File(dir+".map").createNewFile();
		}
		Runtime.getRuntime().addShutdownHook(new shutdown());
		int n = 0;
		try {
		n = Integer.parseInt(args[1]);
		}catch(NumberFormatException e) {
			
			System.out.println("Repetition size must be a number");
		}
		for(int i = 2; i < args.length; i++) {
			list.put(llen,args[i]);
			llen++;
		}
		
		map(n);
	}
	static void recoverq() throws IOException{
		BufferedReader map = new BufferedReader(new FileReader(dir+".map"));
		while(map.readLine() != null) {
			lstart++;
		}
		map.close();
	}
	static void load() throws IOException{
		if(!new File(dir+".q").isFile()) {
			System.out.println("Queue location file not found. Reprocessing...");
			recoverq();
		}
		else {
			BufferedReader q = new BufferedReader(new FileReader(dir+".q"));
			String qst = q.readLine();
			q.close();
			try {
				Integer.parseInt(qst);
			}catch(NumberFormatException ex) {
				System.out.println("Queue location file currupted. Reprocessing...");
				recoverq();
			}
			lstart = Integer.parseInt(qst);
		}
		if(!new File(dir+".index").isFile()) {
			System.out.println("Index not found.");
			return;
		}
		if(!new File(dir+".map").isFile()) {
			System.out.println("Map not found. Recompute?");
			return;
		}
		BufferedReader reader = new BufferedReader(new FileReader(dir+".index"));
		for(String s = reader.readLine(); s != null; s = reader.readLine()) {
			list.put(llen, s);
			llen++;
		}
		reader.close();
	}
	static void importr() throws IOException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(dir+".json"));
		}catch(FileNotFoundException ex) {
			System.out.println("json not found");
			return;
		}
		BufferedWriter writemap = new BufferedWriter(new FileWriter(dir + ".map"));
		BufferedWriter writein = new BufferedWriter(new FileWriter(dir + ".index"));
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
	static void export() throws IOException {
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
	static void map(int re) throws IOException{
		BufferedWriter writemap = new BufferedWriter(new FileWriter(dir + ".map", true));
		BufferedWriter writein = new BufferedWriter(new FileWriter(dir + ".index", true));
		String html;
		int retries = 0;
		String turl = "";
		for(long i = 0; i < re; i++) {
			if(list.containsKey(lstart)) {
				turl = list.get(lstart);
				//System.out.println(turl);
				lstart++;
			}
			else {
				System.out.println("Reached the end");
				writein.close();
				writemap.close();
				return;
			}
			HashSet<String> tlist = new HashSet<>();
			try {
				html = Jsoup.connect(turl).get().html();
			} catch(Exception ex) {
				System.out.println("Unable to reach: " + turl);
				i--;
				continue;
				//return;
			}
			for(Element e:Jsoup.parse(html).select("a[herf]")) {
				String nlink = urlfix(urlmerge(e.attr("href"), turl));
				if(nlink.length() == 0)continue;
				tlist.add(nlink);
			}
			if(retries > 2) {
				retries = 0;
			}
			else if(tlist.size() == 0) {
				lstart--;
				i--;
				retries++;
				continue;
			}
			//System.out.println(turl);
			if(tlist.size() == 0) {
				lstart--;
				i--;
				continue;
			}
			for(String e:tlist) {
				if(!list.containsValue(e)) {
					list.put(llen, urlfix(e));
					llen++;
					writein.append(e+"\n");
				}
				writemap.append(list.getKey(e)+" ");
			}
			writemap.append("\n");
			System.out.print((i+1)+"/"+re+" complete\r");
		}
		writein.close();
		writemap.close();
	}
	static String urlmerge(String url, String lurl) {
		if(url.length() == 0) return "";
		String[] parts = lurl.split("/");
		try {
			if(url.startsWith("http:")||url.startsWith("https:")) return url;
			if(url.contains(":")) return "";
			if(url.startsWith("//")) return parts[0]+url;
		}catch(Exception ignored) {}
		if(url.charAt(0) == '#'||url.charAt(0) == '?'||url.charAt(0) == '&') {
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
			else return "";
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
	static String urlfix(String url) {
		if(url.length()==0)return "";
		char sta;
		char end;
		while(true) {
			sta = url.charAt(0);
			end = url.charAt(url.length()-1);
			if(sta == '\r'||sta == '\t'||sta == '\n'||sta == ' ') {
				url = url.substring(1);
			}
			else if(end == '\r'||end == '\t'||end == '\n'||end == ' ') {
				url = url.substring(0, url.length()-1);
			}
			else break;
		}
		return url;
	}
}
class shutdown extends Thread{
	public void run(){
		try {
			new File(Main.dir+".q").createNewFile();
			FileWriter f = new FileWriter(Main.dir+".q");
			f.append(Integer.toString(Main.lstart));
			f.close();
		} catch(IOException ex) {}
		
	}
}