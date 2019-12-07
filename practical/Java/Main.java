/**
 * 
 * Demo basic PRNG file for proof of concept of NIST 800-22 Test Suite 
 * 
 */ 

import java.security.*;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;

public class Main {

	public static void main(String[] args) {
		
		String tmp;
		// Define how many bit-streams and random numbers per bit-stream should be generated. 
		// Streams are separated by a blank line in the output file 
		int streams = 1;
		int iterations = 10000000;
		Random rand = new Random();
		Random fileRND = new Random();
		
			try{
			FileOutputStream out = new FileOutputStream("java_"+fileRND.nextInt(50000000)+".dat");
			PrintStream p = new PrintStream(out);
			
			for(int v=0; v<streams; v++)
			{
				
				for(int i= 0; i<iterations; i++)
				{
					tmp = String.valueOf(rand.nextInt(2));
					p.append(tmp);
//					p.append(String.valueOf(0)); // for troubleshoot the "igmac: UNDERFLOW" output of 800-22 NIST STS 
				}		
			p.append("\n");
	
			}
			p.close();
			}catch(Exception e){
				e.printStackTrace();
			}
	}

}
