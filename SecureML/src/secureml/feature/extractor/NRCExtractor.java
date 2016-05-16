package secureml.feature.extractor;
import secureml.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import secureml.ResLoader;

/**
 * @author SISI
 * @date May 15,2016
 * 
 * developed for
 * University of Washington, Tacoma
 * Secure Maching Learning Team
 * secureml.insttech.washington.edu
 */

public class NRCExtractor {
	
	public HashMap<String, int[]> dictionary = loadDictionary("NRCDic.txt");	
	/*
	public static void main (String[] args) {
			
		String test = "Well, right now I just woke up from a mid-day nap. It's sort of weird, but ever since I moved to Texas, I have had problems concentrating on things. I remember starting my homework in  10th grade as soon as the clock struck 4 and not stopping until it was done. Of course it was easier, but I still did it. But when I moved here, the homework got a little more challenging and there was a lot more busy work, and so I decided not to spend hours doing it, and just getting by. But the thing was that I always paid attention in class and just plain out knew the stuff, and now that I look back, if I had really worked hard and stayed on track the last two years without getting  lazy, I would have been a genius, but hey, that's all good. It's too late to correct the past, but I don't really know how to stay focused n the future. The one thing I know is that when  people say that b/c they live on campus they can't concentrate, it's b. s. For me it would be easier there, but alas, I'm living at home under the watchful eye of my parents and a little nagging sister that just nags and nags and nags. You get my point. Another thing is, is that it's just a hassle to have to go all the way back to  school to just to go to library to study. I need to move out, but I don't know how to tell them. Don't get me wrong, I see where they're coming from and why they don't  want me to move out, but I need to get away and be on my own. They've sheltered me so much and I don't have a worry in the world. The only thing that they ask me to do is keep my room clean and help out with the business once in a while, but I can't even do that. But I need to. But I got enough money from UT to live at a dorm or apartment  next semester and I think Ill take advantage of that. But off that topic now, I went to sixth street last night and had a blast. I haven't been there in so long. Now I know why I love Austin so much. When I lived in VA, I used to go up to DC all the time and had a blast, but here, there are so many students running around at night. I just want to have some fun and I know that I am responsible enough to be able to  have fun, but keep my priorities straight. Living at home, I can't go out at all without them asking where? with who?  why?  when are you coming back?  and all those  questions. I just wish I could be treated like a responsible person for once, but  my sister screwed that up for me. She went crazy the second she moved into college and messed up her whole college career by partying too much. And that's the ultimate reason that they don't want me to go and have fun. But I'm not little anymore,  and they need to let me go and explore the world, but Im Indian; with Indian culture, with Indian values. They go against \"having fun. \"  I mean in the sense of meeting people or going out with people or partying or just plain having fun. My school is difficult already, but somehow I think that having more freedom will put more pressure on me to  do better in school b/c that's what my parents and ultimately I expect of myself. Well it's been fun writing, I don't know if you go anything out of this writing, but it helped me get some of my thoughts into order. So I hope you had fun reading it and good luck TA's.";  
		HashMap<String, int[]> dictionary = loadDictionary("NRCDic.txt");
		nrcOnString(test);
	}
	*/
	/*
	public void nrcOnFile (String csvfile) throws IOException {
		File file = null;
		try {
			file = new File(getClass().getClassLoader().getResource("NRCDic.csv").toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		bw.write("#AUTHID");
		for (int i = 1; i <= 10; i++){
			bw.write(",NRC" + i);
		}		
		bw.write("\n");
		bw.flush();
		
		String csvFile = csvfile;
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = "\",\"";
		try {
			br = new BufferedReader(new FileReader(csvFile));
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] content = line.split(csvSplitBy);
				bw.write(content[0].replace("\"", ""));
				String input = content[1].replace("\"", "");
				ArrayList<Double> result = nrcOnString(input);
				for (int i = 0; i < 10; i++) {
					bw.write("," + result.get(i).toString());
				}
				bw.write("\n");
				bw.flush();
				}
			}catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
						} catch (IOException e) {
						e.printStackTrace();
						}
					}
				}
			bw.close();		
		}
	*/
	/**
	 * Extract NRC features of the input String
	 * @param input - String
	 * @return result - ArrayList<Double>
	 */
	public ArrayList<Double> nrcOnString (String input) {
		ArrayList<Double> result = new ArrayList<Double>();
		int[] results = new int[10];
		String[] words = input.toLowerCase().split("\\s+");
		for (String word : words) {
			if (dictionary.keySet().contains(word)) {
				int[] values = dictionary.get(word);
				for (int i = 0; i < values.length; i++) {
					results[i] += values[i];
				}
			}
		}
		int sentiValue = results[0] + results[1];
		int sumValue = 0;
		for (int i = 2; i < results.length; i++) {
			sumValue += results[i];
		}
		
		if (sentiValue == 0) {
			result.add(0.0);
			result.add(0.0);
		} else {
			result.add(results[0]*1.0/sentiValue);
			result.add(results[1]*1.0/sentiValue);
		}
		if (sumValue == 0) {
			for (int i = 2; i < results.length; i++) {
				result.add(0.0);
			}
		} else {
			for (int i = 2; i < results.length; i++) {
				result.add(results[i]*1.0/sumValue);
			}
		}
		return result;
	}
	
	/**
	 * Load dictionary from the dictionary file
	 * @param file - String, the path of the dictionary file
	 * @return the dictionary -HashMap<String, int[]>
	 */
	public HashMap<String, int[]> loadDictionary (String file) {
		HashMap<String, int[]> dictionary = new HashMap<String, int[]>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(ResLoader.getInstance().loadFile(file)));
			br.readLine();
			String line;
			while((line = br.readLine()) != null) {
//				System.out.println(line);
				String[] content = line.split("\\s+");
				String keyWord = content[0];
//				System.out.print("key " + keyWord);
				int[] values = new int[10];
				for (int i = 0; i < 10; i++) {
					values[i] = Integer.parseInt(content[i + 1]);
//					System.out.print(" " + values[i]);
				}
//				System.out.println();
				dictionary.put(keyWord, values);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dictionary;		
	}	
}
