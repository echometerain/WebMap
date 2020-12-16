package bkd;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.HttpStatusException;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class Main {
	private static HashSet<Character> modes = new HashSet<>(Arrays.asList(
			new Character[]{'i', 'j', 'e', 'g', 'r', 'q', 's', 'u'}));
	private static HashSet<String> lmodes = new HashSet<>(Arrays.asList(
			new String[]{"index", "json", "export", "graph", "recompute", "query", "scope", "url"}));
	private static HashSet<String> sub = new HashSet<>(Arrays.asList(
			new String[]{"include", "exclude", "media", "nolink", "script"}));
	private static HashSet<Character> tasks = new HashSet<>(Arrays.asList(
			new Character[]{'i', 'j', 'e', 'g'}));// d for dir
	public static BidiMap<Integer, String> list = new DualHashBidiMap<>();
	private static int re = 0;
	private static int llen = 1;
	public static int lstart = 1;
	private static String sl = "/";
	public static String dir = System.getProperty("user.dir");
	private static indexend sh = new indexend();
	private static HashSet<String> lexclude = new HashSet<>();
	private static HashSet<String> linclude = new HashSet<>();
	//private static HashSet<String> qexclude = new HashSet<>();
	//private static HashSet<String> qinclude = new HashSet<>();
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
					if(lmodes.contains(args[i].substring(2))){
						mode = args[i].charAt(2);
					}
					else {
						System.out.println("Syntax error at: \"" + args[i] + "\"");
						return;
					}
				}else {
					if(modes.contains(args[i].charAt(1))) {
						mode = args[i].charAt(1);
					}
					else {
						System.out.println("Syntax error at: \"" + args[i] + "\"");
						return;
					}
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
					else if(smode.equals("include")) linclude.add(args[i]);
					else if(smode.equals("exclude")) lexclude.add(args[i]);
					else{
						System.out.println("Syntax error at: \"" + args[i] + "\"");
						return;
					}
					break;
				case 'q':
					break;
				case 'u':
					list.put(llen, args[i]);
					llen++;
					break;
				default:
					System.out.println("Syntax error at: \"" + args[i] + "\"");
					return;
				}
			}
		}
		System.gc();
		for(char e:tasq) {
			switch(e) {
			case 'i':
				load();
				Runtime.getRuntime().addShutdownHook(sh);
				map();
				Runtime.getRuntime().removeShutdownHook(sh);
				break;
			case 'j':
				importr();
				break;
			case 'e':
				export();
				break;
			case 'g':
				break;
			case 'r':
				break;
			}
			
		}
	}
	static boolean legal(String url) {
		return false;
	}
	static boolean query(String url) {
		return false;
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
		if(!new File(dir+".index").isFile()) {
			new File(dir+".index").createNewFile();
			new File(dir+".map").createNewFile();
			return;
		}
		if(!new File(dir+".q").isFile() && lstart == 1) {
			System.out.println("Queue location file not found. Reprocessing...");
			lstart = recoverq();
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
				lstart = recoverq();
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
				list.put(lstart, arr[i].split("\"")[1]);
				lstart++;
				writein.append(arr[i].split("\"")[1]+"\n");
			}
			String[] ar2 = arr[i+1].split("\"");
			HashSet<String> llinks = new HashSet<>();
			for(int ii = 1; ii < ar2.length-1; ii+=2) {
				llinks.add(ar2[ii]);
				if(!list.containsValue(ar2[ii])) {
					list.put(lstart, ar2[ii]);
					lstart++;
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
			if(st.charAt(st.length()-1)==',')st.substring(0, st.length()-1);
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
		BufferedWriter writemap = new BufferedWriter(new FileWriter(dir + ".map", true));
		BufferedWriter writein = new BufferedWriter(new FileWriter(dir + ".index", true));
		for(long i = 0; i < re && re!=-2; i++) {
			if(!list.containsKey(lstart)) {
				System.out.println("Reached the end");
				writein.close();
				writemap.close();
				return;
			}
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
			} catch(HttpStatusException ex) {
				System.out.println("Unable to reach: " + turl);
				i--;
				continue;
				//return;
			}
			for(Element e:Jsoup.parse(html).select("a[href]")) {
				String nlink = urlmerge(e.attr("href"), turl);
				if(nlink.equals(""))continue;
				String lsub = nlink.substring(0, nlink.length()-1);
				if(list.containsValue(lsub)) nlink = lsub;
				tlist.add(nlink);
			}
			//System.out.println(tlist.size());
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
		writein.close();
		writemap.close();
		sh.start();
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
		} catch(IOException ex) {}
		
	}
}