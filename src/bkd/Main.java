package bkd; 
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Element;
import org.jsoup.HttpStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class Main {
	private static HashSet<String> sub = new HashSet<>(Arrays.asList(
			new String[]{"include", "exclude", "media", "nolink", "script"}));
	private static HashSet<Character> tasks = new HashSet<>(Arrays.asList(
			new Character[]{'i', 'j', 'e', 'g', 'q'}));
	public static BidiMap<Integer, String> list = new DualHashBidiMap<>();
	public static HashMap<String, Character> modes = new HashMap<>();
	private static HashSet<String> uset = new HashSet<>();
	private static int re = -2;
	private static int qre = -2;
	public static boolean qmode = false;
	public static boolean qonly = false;
	private static int llen = 1;
	public static int lstart = 1;
	private static String sl = "/";
	public static String dir = System.getProperty("user.dir");
	private static indexend sh = new indexend();
	private static String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0";
	private static String lexclude = null;
	private static String linclude = null;
	private static String qexclude = null;
	private static String qinclude = null;
	static {
		modes.put("index", 'i');
		modes.put("json", 'j');
		modes.put("export", 'e');
		modes.put("graph", 'g');
		modes.put("recompute", 'r');
		modes.put("scope", 's');
		modes.put("url", 'u');
		modes.put("query", 'q');
		modes.put("merge", 'm');
	}
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
		dir = dir+args[0];
		cmds(args);
	}
	static void cmds(String[] args) throws IOException{
		HashSet<Character> tasq = new HashSet<>();
		char mode = ' ';
		String smode = null;
		//boolean qre = false;
		//outer:
		for(int i = 1; i < args.length; i++) {
			args[i] = args[i].toLowerCase();
			if(args[i].charAt(0)=='-') {
				if(args[i].charAt(1)=='-') {
					if(modes.containsKey(args[i].substring(2))){
						mode = args[i].charAt(2);
					}
					else {
						System.out.println("Syntax error at: \"" + args[i] + "\"");
						return;
					}
				}else {
					if(modes.containsValue(args[i].charAt(1))) {
						mode = args[i].charAt(1);
					}
					else {
						System.out.println("Syntax error at: \"" + args[i] + "\"");
						return;
					}
				}
				if(mode == 'r') {
					mode = ' ';
					remove();
					continue;
				}
				smode = null;
				if(tasks.contains(mode)) {
					tasq.add(mode);
				}
			}
			else{
				if(sub.contains(args[i])) {
					if(mode == ' ')continue;
					smode = args[i];
					if(mode == 's'){}
					continue;
				}
				switch(mode) {
				case 'i':
					if(smode == null) {
						if(isint(args[i])||args[i].equals("inf")) {
							if(args[i].equals("inf")) {
								re = -2;
							}
							else {
								re = Integer.parseInt(args[i]);
								if(re<0)re=-2;
							}
						}
						else {
							System.out.println("Index iterations must be integer: \"" + args[i] + "\"");
							return;
						}
					}
					else if(smode.equals("include")) linclude = args[i];
					else if(smode.equals("exclude")) lexclude = args[i];
					else{
						System.out.println("Syntax error at: \"" + args[i-1] + "\"");
						return;
					}
					break;
				case 'q':
					qonly = true;
					qmode = true;
					if(smode == null) {
						if(isint(args[i])||args[i].equals("inf")) {
							if(args[i].equals("inf")) {
								qre = -2;
							}
							else {
								qre = Integer.parseInt(args[i]);
								if(qre<0)qre=-2;
							}
						}
						else {
							System.out.println("Index iterations must be integer: \"" + args[i] + "\"");
							return;
						}
					}
					else if(smode.equals("include")) qinclude = args[i];
					else if(smode.equals("exclude")) qexclude = args[i];
					else{
						System.out.println("Syntax error at: \"" + args[i-1] + "\"");
						return;
					}
					break;
				case 'u':
					if(args[i].charAt(args[i].length()-1) == '/')args[i]=args[i].substring(0, args[i].length()-1);
					uset.add(args[i]);
					break;
				default:
					System.out.println("Syntax error at: \"" + args[i] + "\"");
					return;
				}
				if(smode == null) mode = ' ';
			}
		}
		System.gc();
		if(tasq.contains('i')) {
			qonly = false;
			load();
			map();
		}
		else if(tasq.contains('q')) {
			qmode = true;
			qonly = true;
			load();
			map();
		}
		if(tasq.contains('j')) {
			importr();
		}
		if(tasq.contains('e')) {
			export();
		}
		
	}
	static void remove() {
		new File(dir+".index").delete();
		new File(dir+".map").delete();
		new File(dir+".q").delete();
	}
	static void recoverq() throws IOException{
		BufferedReader map = new BufferedReader(new FileReader(dir+".map"));
		while(map.readLine() != null) {
			lstart++;
		}
		map.close();
	}
	static void load() throws IOException{
		if(!new File(dir+".index").isFile()) {
			new File(dir+".index").createNewFile();
			new File(dir+".map").createNewFile();
			return;
		}
		if(!new File(dir+".q").isFile() && lstart == 1) {
			System.out.println("Queue location file not found. Reprocessing...");
			recoverq();
		}
		else if(lstart == 1){
			BufferedReader q = new BufferedReader(new FileReader(dir+".q"));
			String qst = q.readLine();
			q.close();
			if(isint(qst)) {
				lstart = Integer.parseInt(qst);
			}
			else{
				System.out.println("Queue location file currupted. Reprocessing...");
				recoverq();
			}
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
		if(new File(dir+".json").isFile()) {
			reader = new BufferedReader(new FileReader(dir+".json"));
		}
		else{
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
			if(i==0) {
				list.put(llen, arr[i].split("\"")[1]);
				llen++;
				writein.append(arr[i].split("\"")[1]+"\n");
			}
			String[] ar2 = arr[i+1].split("\"");
			HashSet<String> llinks = new HashSet<>();
			for(int ii = 1; ii < ar2.length-1; ii+=2) {
				llinks.add(ar2[ii]);
				if(!list.containsValue(ar2[ii])) {
					list.put(llen, ar2[ii]);
					llen++;
					writein.append(ar2[ii]+"\n");
				}
			}
			for(String e:llinks) {
				writemap.append(list.getKey(e)+" ");
			}
			writemap.append("\n");
		}
		writemap.close();
		writein.close();
		System.out.println("Finished importing");
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
				if(e2.equals("")) continue;
				if(isint(e2)){
					st += "\""+list.get(Integer.parseInt(e2))+"\",";
				}
				else{
					System.out.println("Map file should only contain numbers");
					reader.close();
					return;
				}
			}
			if(!s.equals(""))st = st.substring(0, st.length()-1);
			st += "],\n";
		}
		reader.close();
		if(st.charAt(st.length()-2)==',')st.substring(0, st.length()-2);
		if(st != null)st += "}";
		FileWriter writer = new FileWriter(dir + ".json");
		writer.write(st);
		writer.close();
		System.out.println("Finished exporting");
	}
	static void map() throws IOException{
		Runtime.getRuntime().addShutdownHook(sh);
		BufferedWriter writemap = new BufferedWriter(new FileWriter(dir + ".map", true));
		BufferedWriter writein = new BufferedWriter(new FileWriter(dir + ".index", true));
		if(qonly) writemap = writein = null;
		for(String e:uset) {
			if(!qonly)writein.append(e);
			list.put(llen, e);
			llen++;
		}
		for(long i = 0; i < re || re==-2; i++) {
			if(!list.containsKey(lstart)) {
				System.out.println("Reached the end");
				System.out.println(list.keySet());
				if(!qonly) {
					writein.close();
					writemap.close();
				}
				return;
			}
			String turl = list.get(lstart);
			//System.out.println(turl);
			lstart++;
			if(turl == null) {
				if(!qonly) {
					writein.close();
					writemap.close();
				}
				return;
			}
			HashSet<String> tlist = new HashSet<>();
			String html;
			try {
				html = Jsoup.connect(turl).followRedirects(true)
						.userAgent(agent)
						.ignoreHttpErrors(true)
						.get().html();
			} catch(HttpStatusException ex) {
				System.out.println("Unable to reach: " + turl);
				if(!qonly)writemap.append("\n");
				continue;
				//return;
			} catch(UnsupportedMimeTypeException ex) {
				if(!qonly)writemap.append("\n");
				continue;
			}
			for(Element e:Jsoup.parse(html).select("a[href]")) {
				String nlink = urlmerge(e.attr("href"), turl);
				if(nlink.equals(""))continue;
				if(nlink.charAt(nlink.length()-1) == '/')nlink=nlink.substring(0, nlink.length()-1);
				tlist.add(nlink);
			}
			//System.out.println(tlist.size());
			for(String e:tlist) {
				if(!list.containsValue(e)) {
					if((linclude == null || e.matches(linclude)) || (lexclude == null || !e.matches(lexclude))){
						list.put(llen, e);
						llen++;
						if(!qonly)writein.append(e+"\n");
					}
					if(qmode) {
						if((qinclude == null || e.matches(qinclude)) || (qexclude == null || !e.matches(qexclude))){
							if(qre == 0) {
								if(!qonly) {
									writein.close();
									writemap.close();
								}
								return;
							}
							if(qre != -2) qre--;
							System.out.println(e);
						}
					}
				}
				if(!qonly)writemap.append(list.getKey(e)+" ");
			}
			if(!qonly)writemap.append("\n");
			if(re == -2) {
				System.out.print((i+1)+" complete\r");
			}
			else {
				System.out.print((i+1)+"/"+re+" complete\r");
			}
		}
		if(!qonly) {
			writein.close();
			writemap.close();
		}
		Runtime.getRuntime().removeShutdownHook(sh);
		sh.start();
	}
	static String urlmerge(String url, String lurl) {
		if(url.length() == 0) return lurl;
		if(lurl.charAt(lurl.length()-1) == '/')lurl=lurl.substring(0, lurl.length()-1);
		if(url.charAt(url.length()-1) == '/')url=url.substring(0, url.length()-1);
		String[] parts = lurl.split("/");
		while(true) {
			if(url.length() == 0) return lurl;
			char ch = url.charAt(0);
			if(ch == '\r'||ch == '\t'||ch == '\n'||ch == ' ') {
				url = url.substring(1);
			}
			else break;
		}
		if(url.startsWith("http:")||url.startsWith("https:")) return url;
		if(url.split(":").length == 8)
		if(url.contains(":")) return "";
		if(url.startsWith("//")) return parts[0]+url;
		if(url.charAt(0) == '#'||url.charAt(0) == '?'||url.charAt(0) == '&') {
			return lurl+url;
		}
		if(parts.length<3) return "";
		if(url.charAt(0) == '/') {
			lurl = parts[0]+"//"+parts[2];
		}
		else if(url.charAt(0) == '.') {
			if(parts.length>3 && (parts[parts.length-1].contains(".") || parts[parts.length-1].contains("?") || parts[parts.length-1].contains("#"))) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()));
				parts = lurl.split("/");
			}
			if(url.startsWith("./")) {
				url = url.substring(2);
			}
			else if(url.startsWith("../../")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+parts[parts.length-2].length()+2));
				url = url.substring(6);
			}
			else if(url.startsWith("../")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
				url = url.substring(3);
			}
			else return "";
		}
		else {
			if(parts.length>3 && parts[parts.length-1].contains(".")) {
				lurl = lurl.substring(0, lurl.length()-(parts[parts.length-1].length()+1));
			}
		}
		url = lurl + url;
		return url;
	}
	static boolean isint(String x) {
		try {
			Integer.parseInt(x);
			return true;
		}catch(NumberFormatException e) { return false;}
	}
}
class indexend extends Thread{
	public void run(){
		try {
			new File(Main.dir+".q").createNewFile();
			FileWriter f = new FileWriter(Main.dir+".q");
			f.append(Integer.toString(Main.lstart));
			f.close();
		} catch(IOException ignore) {}
		
	}
}