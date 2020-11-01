import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class WebLog {
	
	static Scanner in = new Scanner(System.in);
	
	// csv -> list
	static ArrayList<Log_row> csv2list(String fileName){
		
		ArrayList<Log_row> log_list = new ArrayList<Log_row>();	
		
		try { 
			// csv 데이터 파일 
			File csv = new File(fileName); 
			BufferedReader br = new BufferedReader(new FileReader(csv));
	
			// row 읽어서 저장할 string
			String line = "";
			// column인지 체크할 flag와 for문 돌리기 위한 int
			Boolean flag = true; int i; 
			// csv column 저장할 string
			String column;
			
			while ((line = br.readLine()) != null) { 
				// -1 옵션은 마지막 "," 이후 빈 공백도 읽기 위한 옵션 
				String[] token = line.split(",",-1); 
				// 칼럼명 적혀있는 부분은 list에 넣지 않는다.
				if(flag) {
					// 칼럼명 확인 위한 출력문
					for(i=0;i<token.length;i++) { 
						System.out.print(token[i]+" "); 
					}
					System.out.println("순으로 저장된 파일입니다.");
					flag = false;
					continue;
				}
				// 잘못 저장된 row 거르기
				if(token[1].charAt(0)!='[') {
					continue;
				}
				// 저장할 log 객체, 객체 생성 위치 중요.
				Log_row log = new Log_row();
				log.setIp(token[0]);
				log.setTime(splitTime(token[1]));
				log.setURL(splitURL(token[2]));
				log.setStaus(Integer.parseInt(token[3]));
				// log 객체를 arraylist에 추가
				log_list.add(log);
			}
			br.close(); 
			} 
		catch (FileNotFoundException e) { 
			System.out.println("FileNotFoundException");
			e.printStackTrace(); 
		}
		catch (IOException e) { 
			System.out.println("IOException");
			e.printStackTrace(); 
		}
		catch (Exception e) {
			System.out.println("Error");
		}
		return log_list;
	}
	
	// 시간 split 하는 함수
	static String[] splitTime(String time) {
		// time format = [29/Nov/2017:06:58:55
		String [] splitTime = new String[4];
		String[] token = time.split(":",-1); 
		for(int i=0;i<token.length;i++) {
			splitTime[i] = token[i];
		}
		return splitTime;
	}
	
	// URLsplit 하는 함수
	static String[] splitURL(String URL) {
		// URL format = GET /login.php HTTP/1.1
		String [] splitURL = new String[3];
		String[] token = URL.split(" "); 
		for(int i=0;i<token.length;i++) {
			splitURL[i] = token[i];
		}
		return splitURL;
	}
	
	// TPM 구하는 함수
	static HashMap<String, Integer> getTPM(ArrayList<Log_row> list){
		HashMap<String, Integer> tpm = new HashMap<String, Integer>();
		for(Log_row row : list) {
			// [25/Nov/2017 17 58 => 0000-00-00 로 포맷 변경
			String[] time = row.getTime();
			
			// 25/Nov/2017
			time[0] = time[0].substring(1);
			String[] token = time[0].split("/");
			// 월 숫자로 변경
			token[1] = Log_row.convertMonth(token[1]);
			// 0000-00-00 로 날짜 변경
			time[0] = token[2]+"-"+token[1]+"-"+token[0];
			
			// key : date+hour+min
			String key = time[0] + " " + time[1] + ":" + time[2];
			// 이미 있으면 기존 count값++
			if(tpm.containsKey(key)) {
				int value = tpm.get(key)+1;
				tpm.put(key,value);
			}else { // 없으면 새로 +1
				tpm.put(key, 1);
			}
		}
		return tpm;
	}
	
	// hashmap -> csv
	static void hashmap2csv(HashMap<String, Integer> tpm, String fileName) {	
		try {
			BufferedWriter fw = new BufferedWriter(new FileWriter(fileName,true));
			for(String key : tpm.keySet()){
				 String value = Integer.toString(tpm.get(key));
				 // key 0000-00-00 00:00
				 // 0000-00-00, 00:00, count로 변경
				 String[] token = key.split(" ");
				 fw.write(token[0]+","+token[1]+","+value);
				 fw.newLine();
			 }
			fw.flush();
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}		 
	}
	
	public static void main(String[] args) {
		// 읽을 파일 이름 입력 받기
		System.out.print("파일이름을 입력하세요.(예:weblog.csv)(파일은 src폴더와 같은 level에 있어야 합니다.): ");
		String fileName;
		fileName = in.next();
		
		// csv 파일 읽고 arraylist에 저장
		ArrayList<Log_row> list = csv2list(fileName);
		//list로 TPM 구하기
		HashMap<String, Integer> tpm = getTPM(list);
		
		// 만들 파일 이름 입력 받기
		System.out.print("TPM을 저장할 파일 이름을 입력하세요.(예:tpm.csv): ");
		String tpmName;
		tpmName = in.next();
		
		// TPM -> csv 파일에 저장
		hashmap2csv(tpm, tpmName);
		System.out.print("저장되었습니다.");
	}
	
}

class Log_row{
	// ip addresss
	String ip;
	// 날짜, 시, 분
	String[] time = new String[4];
	// 메소드 path 버전?
	String[] URL = new String[3];
	// staus
	int staus;
	
	Log_row(){
		ip = null;
		time = null;
		URL = null;
		staus = 0;
	}
	Log_row(String ip, String[] time, String[] URL, int staus){
		this.ip = ip;
		this.time = time;
		this.URL = URL;
		this.staus = staus;
	}
	
	void setIp(String ip) {
		this.ip = ip;
	}
	void setTime(String[] time){
		this.time = time;
	}
	void setTime(String date, String hour, String min, String sec){
		time[0] = date;
		time[1] = hour;
		time[2] = min;
		time[3] = sec;
	}
	void setURL(String[] URL) {
		this.URL = URL;
	}
	void setURL(String method, String path, String version) {
		URL[0] = method;
		URL[1] = path;
		URL[2] = version;
	}
	void setStaus(int staus) {
		this.staus = staus;
	}
	
	public String getIp() {return ip;}
	public String[] getTime() {return time;}
	public String[] getURL() {return URL;}
	public int getStaus() {return staus;}
	
	static public String convertMonth(String str) {
		String month="";
		switch(str) {
		case "Jan" :
			month = "01";
			break;
		case "Feb" :
			month = "02";
			break;
		case "Mar" :
			month = "03";
			break;
		case "Apr" :
			month = "04";
			break;
		case "May" :
			month = "05";
			break;
		case "Jun" :
			month = "06";
			break;
		case "Jul" :
			month = "07";
			break;
		case "Aug" :
			month = "08";
			break;
		case "Sep" :
			month = "09";
			break;
		case "Oct" :
			month = "10";
			break;
		case "Nov" :
			month = "11";
			break;
		case "Dec" :
			month = "12";
			break;
		}
		return month;
	}
	
	public String toString() {
		String print = ip + ", ";
		for(String s : time) {
			print = print + s + ", ";
		}
		for(String s : URL) {
			print = print + s + ", ";
		}
		print = print + Integer.toString(staus);
		return print;
	}
}
