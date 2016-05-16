package secureml.feature.extractor;
import secureml.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

<<<<<<< HEAD
import secureml.ResLoader;


=======
/**
 * @author SISI
 * @date May 15,2016
 * 
 * developed for
 * University of Washington, Tacoma
 * Secure Maching Learning Team
 * secureml.insttech.washington.edu
 */
>>>>>>> SISI

public class MRCextractor {
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	/*
	public void main (String[] args) throws IOException, QueryException {

		String test = "Well, here we go with the stream of consciousness essay. I used to do things like this in high school sometimes. They were pretty interesting, but I often find myself with a lack of things to say. I normally consider myself someone who gets straight to the point. I wonder if I should hit enter any time to send this back to the front. Maybe I'll fix it later. My friend is playing guitar in my room now. Sort of playing anyway. More like messing with it. He's still learning. There's a drawing on the wall next to me. Comic book characters I think, but I'm not sure who they are. It's been a while since I've kept up with comic's. I just heard a sound from ICQ. That's a chat program on the internet. I don't know too much about it so I can't really explain too well. Anyway, I hope I'm done with this by the time another friend comes over. It will be nice to talk to her again. She went home this weekend for Labor Day. So did my brother. I didn't go. I'm not sure why. No reason to go, I guess. Hmm. when did I start this. Wow, that was a long line. I guess I won't change it later. Okay, I'm running out of things to talk about. I've found that happens to me a lot in conversation. Not a very interesting person, I guess. Well, I don't know. It's something I'm working on. I'm in a class now that might help. The phone just rang. Should I get it?  The guy playing the guitar answered it for me. It's for my roommate. My suitemate just came in and started reading this. I'm uncomfortable with that. He's in the bathroom now. You know, this is a really boring piece of literature. I never realized how dull most everyday thoughts are. Then again, when you keep your mind constantly moving like this, there isn't really time to stop and think deeply about things. I wonder how long this is going to be. I think it's been about ten minutes now. Only my second line. How sad. Well, not really considering how long these lines are. Anyway, I wonder what I'm going to do the rest of the night. I guess there's always homework to do. I guess we'll see. This seat is uncomfortable. My back sort of hurts. I think I'm going to have arthritis when I get older. I always thought that I wouldn't like to grow old. Not too old, I suppose. I've always been a very active person. I have a fear of growing old, I think. I guess it'll go away as I age gradually. I don't know how well I'd deal with paralysis from an accident though. As long as I have God and my friends around, I'll be okay though. I'm pretty thirsty right now. There isn't much to drink around my room. Ultimate Frisbee, I haven't played that all summer. Fun game, but tiring. I'm out of shape. I'd like to get in better shape, but I hate running. It's too dull for me. Hmmm. it's almost over now. Just a few more minutes. Let's see if I make it to the next line. Short reachable goals!  Whatever. Anyway, what else do I have to do tonight. I guess I could read some. My shirt smells like dinner. It's pretty disgusting. I need to wake up for a 9:30 am class tomorrow. I remember when that wasn't early at all. Well, I made it to the next line. I'm so proud of myself. That's sarcasm, by the way. I wonder if I was suppose to right this thing as a narrative. Oh well too late now. Time for me to head out. Until next time, good bye and good luck. I don't know.";
		List<Double> result = mrcOnString(test);
		for (Double d : result) {
			System.out.println(d);
		}
	}
	*/
	
	/**
	 * Extract MRC features of the input String
	 * @param input - String
	 * @return result - ArrayList<Double>
	 * @throws IOException
	 * @throws QueryException
	 */
	public ArrayList<Double> mrcOnString (String input) throws IOException, QueryException{
		MRCDatabase mrcDb = null;
<<<<<<< HEAD
=======
		//load MRC database
>>>>>>> SISI
		mrcDb = new MRCDatabase(ResLoader.getInstance().loadFile("mrc2.dct"));
		ArrayList<Double> result  = new ArrayList<Double>();
		String[] words = input.toUpperCase().replaceAll("[^\\w]", " ").split("\\s+");
		int[] mrcFeatures = new int[14];
		int value = 0;
		int vid = 0;
		int wordCount = words.length;
		
		for (String w: words){
			for (Field f : mrcDb.getFields()){
				//System.out.println("---------" + w);
				try{
					value = mrcDb.getValue(w, f);
				}catch(Exception e){
					value = 0;
				}
				//System.out.println(value);
				mrcFeatures[vid] += value;
				vid++;
			}
			vid = 0;
		}
		for(int i = 0; i < mrcFeatures.length; i++){
			result.add(mrcFeatures[i]*1.0/wordCount);
		}
		return result;
	}
}


