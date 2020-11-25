package bkd;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.NoSuchElementException;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class Main {
	public static BidiMap<Integer, String> list = new DualHashBidiMap<>();
	//public static HashMap<String, LinkedList<String>> map = new HashMap<>();
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
	static void cmds(String[] args) {
		
	}
	static int recoverq() throws IOException{
		int start = 1;
		BufferedReader map = new BufferedReader(new FileReader(dir+".map"));
		while(map.readLine() != null) {
			lstart++;
		}
		map.close();
		return start;
	}
	static void load() throws IOException{
		if(!new File(dir+".q").isFile()) {
			System.out.println("Queue location file not found. Reprocessing...");
			lstart = recoverq();
		}
		else {
			BufferedReader q = new BufferedReader(new FileReader(dir+".q"));
			String qst = q.readLine();
			q.close();
			try {
				Integer.parseInt(qst);
			}catch(NumberFormatException ex) {
				System.out.println("Queue location file currupted. Reprocessing...");
				lstart = recoverq();
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
		String st = "";
		for(String s = reader.readLine(); s != null; s = reader.readLine()) st += s;
		reader.close();
		String[] arr = st.split("([\\[\\]])");
		for(int i = 0; i < arr.length-1; i+=2) {
			HashSet<String> llinks = new HashSet<>();
			String[] ar2 = arr[i+1].split("\"");
			String key = arr[i].split("\"")[1];
			list.put(lstart, key);
			lstart++;
			writein.append(key+"\n");
			for(int ii = 1; ii < ar2.length-1; ii+=2) {
				llinks.add(ar2[ii]);
				list.put(lstart, ar2[ii]);
				lstart++;
				writein.append(ar2[ii]+"\n");
			}
			writemap.append(arr[i].split("\"")[1]);
			for(String e:llinks) {
				writemap.append(" "+list.getKey(e));
			}
			writemap.append("\n");
		}
		writemap.close();
		writein.close();
	}
	static void export() throws IOException {
		load();
		new File(dir+".json").createNewFile();
		BufferedReader reader = new BufferedReader(new FileReader(dir + ".map"));
		String st = new String();
		st += "{";
		int count = 1;
		for(String s = reader.readLine(); s != null; s = reader.readLine()) {
			st += "\""+list.get(count)+"\":[";
			count++;
			for(String e2:s.split(" ")) {
				st += "\""+e2+"\",";
			}
			if(st.charAt(st.length()-1)==',')st.substring(0, st.length()-1);
			st += "],\n";
		}
		reader.close();
		if(st.charAt(st.length()-2)==',')st.substring(0, st.length()-2);
		if(st != null)st += "}";
		FileWriter writer = new FileWriter(dir + "map.json");
		writer.write(st);
		writer.close();
		System.out.println("Finished.");
	}
	static void map(int re) throws IOException{
		BufferedWriter writemap = new BufferedWriter(new FileWriter(dir + ".map", true));
		BufferedWriter writein = new BufferedWriter(new FileWriter(dir + ".index", true));
		try {
		for(long i = 0; i < re; i++) {
			String turl = list.get(lstart);
			//System.out.println(turl);
			lstart++;
			if(turl == null) {
				writein.close();
				writemap.close();
				return;
			}
			HashSet<String> tlist = new HashSet<>();
			String html;
			try {
				html = Jsoup.connect(turl).get().html();
			} catch(Exception ex) {
				System.out.println("Unable to reach: " + turl);
				i--;
				continue;
				//return;
			}
			for(Element e:Jsoup.parse(html).select("a[href]")) {
				String nlink = urlmerge(e.attr("href"), turl);
				if(nlink.equals(""))continue;
				tlist.add(nlink);
			}
			System.out.println(tlist.size());
			for(String e:tlist) {
				if(!list.containsValue(e)) {
					list.put(llen, e);
					llen++;
					writein.append(e+"\n");
				}
				writemap.append(list.getKey(e)+" ");
			}
			writemap.append("\n");
			System.out.print((i+1)+"/"+re+" complete\r");
		}
		}catch(NoSuchElementException ex) {
			System.out.println("Reached the end");
			writein.close();
			writemap.close();
			return;
		}
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