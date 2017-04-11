import java.lang.Math;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class Test {

	MyImage image;
	JPEGCodec codec;
	ImageHandler h;
	String filenameIn;
	DecimalFormat df; // Used for pretty-printing ratios
	
	public Test(String filename) {
		this.filenameIn = filename;
		this.codec = new JPEGCodec(8, 100);
		this.h = new ImageHandler();
		int[][] tmp = h.getPixels("resources/" + filename);
		if (tmp == null)
			System.out.println("Test pixels not initialized.");
		else image = new MyImage(tmp, 8);
		
		this.df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.CEILING);
	}
	
	//TODO: Add ratios/diffs for each encode/decode pair steps. (e.g. compare dct & dequantized blocks)
	public void testSingleMacroblock(int maxBlockSize, int quality, boolean testDCT, boolean testQuantize, boolean testHuffman, boolean printArrays) {
		System.out.println("Testing full encode/decode for single macroblock with options:");
		System.out.println("Test DCT:\t" + ((testDCT) ? "ON" : "OFF"));
		System.out.println("Test Quantize:\t" + ((testQuantize) ? "ON" : "OFF"));
		System.out.println("Test Huffman:\t" + ((testHuffman) ? "ON" : "OFF"));
		
		DCT dct = new DCT(4);
		Quantize quant = new Quantize(4, quality);
		
		for (int i = 4; i <= maxBlockSize; i+=4) {
			dct.setBlockSize(i);
			quant.setBlockSize(i);
			image.setBlockSize(i);
			
			int[][] in = image.getMacroblock(image.widthInBlocks/2, image.heightInBlocks/2);
			
			int[][] dctBlock = in;
			if (testDCT)
				dctBlock = dct.forwardDCT(in);
			
			int[][] quantizedBlock = dctBlock;
			if (testQuantize)
				quantizedBlock = quant.quantize(dctBlock);
			
			//TODO: Remove this.
			if (printArrays){
				Test.printArray("Quantized", quantizedBlock);
				System.out.println();
			}
			
			int[][] huffmanBlock = quantizedBlock;
			int[][] deHuffmanBlock = huffmanBlock;
			if (testHuffman) {
				//TODO: Huffman encode
				//TODO: Huffman decode
			}
			
			int[][] dequantizedBlock = deHuffmanBlock;
			if (testQuantize)
				dequantizedBlock = quant.dequantize(deHuffmanBlock);
			
			int[][] out= dequantizedBlock;
			if (testDCT)
				out = dct.reverseDCT(dequantizedBlock);
			
			int avgDiff = avgDiffArrays(in, out);
			double avgRatio = avgRatioArrays(in, out);
			
			if (printArrays) {
				Test.printArray("INPUT", in);
				System.out.println();
				Test.printArray("OUTPUT", out);
			}
			
			System.out.print("Blocksize " + i + " -- Average Diff: " + avgDiff + "\tAverage Ratio: " + avgRatio);
			if (avgDiff <= 10)
				System.out.print(" -- ACCEPTED.");
			
			System.out.println();
		}
	}

	//TODO: Add parameter for image
	public void testSingleImage(int maxBlockSize, int quality, boolean testDCT, boolean testQuantize, boolean testHuffman, boolean isRegression, String filepath, MyImage img) {
		if (!isRegression) {
			System.out.println("Testing full encode/decode with options: ");
			System.out.println("Test DCT:\t" + ((testDCT) ? "ON" : "OFF"));
			System.out.println("Test Quantize:\t" + ((testQuantize) ? "ON" : "OFF"));
			System.out.println("Test Huffman:\t" + ((testHuffman) ? "ON" : "OFF"));
		}
			
		MyImage imageToUse = this.image;
		if (img != null)
			imageToUse = img;
		else if (filepath != null)
			imageToUse = new MyImage(h.getPixels(filepath), 4);
		
		if (imageToUse.getPixels() == null)
			return;
		
		DCT dct = new DCT(4);
		Quantize quant = new Quantize(4, quality);
		Huffman huff = new Huffman(4);
		
		for (int i = 4; i <= maxBlockSize; i+=4) {
			dct.setBlockSize(i);
			quant.setBlockSize(i);
			huff.setBlockSize(i);
			imageToUse.setBlockSize(i);
			
			MyImage imageOut = new MyImage(Test.getEmptyPixels(imageToUse.trueWidth, imageToUse.trueHeight), i);
			
			double sumMacroblockAverageRatios = 0.0;
			double sumMacroblockAverageDiffs = 0.0;
			double sumMacroblockAverageNumZeroes = 0.0;
			
			for (int x = 0; x < imageToUse.widthInBlocks; x++) {
				for (int y = 0; y < imageToUse.heightInBlocks; y++) {
					int[][] in = imageToUse.getMacroblock(x, y);
					
					int[][] dctBlock = in;
					if (testDCT)
						dctBlock = dct.forwardDCT(in);
					
					int[][] quantizedBlock = dctBlock;
					if (testQuantize)
						quantizedBlock = quant.quantize(dctBlock);
					
					int[] huffmanArray = huff.zigZag(quantizedBlock);
					
					int[][] deHuffmanBlock = huff.unZigZag(huffmanArray);
					
					int[][] dequantizedBlock = deHuffmanBlock;
					if (testQuantize)
						dequantizedBlock = quant.dequantize(deHuffmanBlock);
					
					int[][] out= dequantizedBlock;
					if (testDCT)
						out = dct.reverseDCT(dequantizedBlock);
					
					imageOut.setMacroblock(x, y, out);
					
					sumMacroblockAverageNumZeroes += Test.percentageZeroes(huffmanArray);
					sumMacroblockAverageRatios += Test.avgRatioArrays(in, out);
					sumMacroblockAverageDiffs += Test.avgDiffArrays(in, out);
					
				}
			}
			
			double averagePercentageZeroes = sumMacroblockAverageNumZeroes / (imageToUse.widthInBlocks*imageToUse.heightInBlocks);
			double averageMacroblockRatio = sumMacroblockAverageRatios / (imageToUse.widthInBlocks*imageToUse.heightInBlocks);
			double averageMacroblockDiff = sumMacroblockAverageDiffs / (imageToUse.widthInBlocks*imageToUse.heightInBlocks);

			System.out.print("Blocksize " + i + "\t-- Avg. Diff: " + df.format(averageMacroblockDiff) +
								"\tAvg. Ratio: " + df.format(averageMacroblockRatio) + "\tAvg. % Zero: " + 
									df.format(averagePercentageZeroes));
			if (averageMacroblockRatio >= .90 && averageMacroblockRatio <= 1.10)
				System.out.print(" -- ACCEPTED.");
			System.out.println();
			String filenameOut;
			if (isRegression)
				filenameOut = "test/fullregressiontest2/" + filepath + "-OUT-" + i + ".jpg";
			else
				filenameOut = "test/singleimagetest/" + filenameIn + "-OUT-" + i + ".jpg";
			h.writeToFile(imageOut.getPixels(), filenameOut);
			
		}
	}
	
	public void testZigZag(int maxBlockSize, boolean print) {
		System.out.println("Testing zig-zag functionality...");
		Huffman huff = new Huffman(4);
		
		for (int i = 4; i <= maxBlockSize; i+=4) {
			System.out.print("Blocksize " + i + " -- ");
			huff.setBlockSize(i);
			image.setBlockSize(i);
			
			int[][] in = image.getMacroblock(image.widthInBlocks/2, image.heightInBlocks/2);
			int[] zz = huff.zigZag(in);
			int[][] out = huff.unZigZag(zz);
			
			boolean foundError = false;
			for (int x = 0; x < i; x++) {
				for (int y = 0; y < i; y++)
					if (in[x][y] != out[x][y]) {
						if (!foundError) {
							System.out.print("Found error.\n");
							foundError = true;
						}
						System.out.println("Error at x = " + x + ", y = " + y);
					}
			}
			
			if (!foundError) {
				System.out.print("ACCEPTED.\n");
			}
			
			if (print) {
				Test.printArray("INPUT", in);
				System.out.println();
				
				Test.printArray("ZigZag", zz);
				System.out.println();
				
				Test.printArray("OUTPUT", out);
			}
		}
	}
	
	public void fullRegressionTest(int maxBlockSize, int quality, boolean print) {
		System.out.println("Running full regression test.");
		
		for (int i = 1; i <= 10; i++) {
			String filename = "img" + i + ".jpg";
			String inputPath = "resources/" + filename;
			if (print)
				System.out.println("Loading image from " + inputPath);
			
			int[][] pixels = h.getPixels(inputPath);
			if (pixels == null) {
				if (print)
					System.out.println("Error loading file. Skipping.");
				continue;
			} else if (print) {
				System.out.println("Successfully loaded pixels. W: " + pixels.length + "\tH: " + pixels[0].length);
			}
			
			MyImage img = new MyImage(pixels, 4);
			this.testSingleImage(maxBlockSize, quality, true, true, true, true, filename, img);
			System.out.println("Done.");
		}
	}
	
	/* Helper Methods */
	
	/* Helper Methods */
	
	private static double avgRatioArrays(int[][] a, int[][] b) {
		if (a.length != b.length || a[0].length != b[0].length) { // Incompatible.
			return Integer.MAX_VALUE;
		}
		double sumOfRatios = 0.0;
		
		for (int x = 0; x < a.length; x++) {
			for (int y = 0; y < a[0].length; y++) {
				double aDouble = (double) a[x][y];
				double bDouble = (double) b[x][y];
				
				// 0 -> 1 to protect against division by 0
				if (bDouble == 0)
					bDouble = 1.0;
				
				sumOfRatios += aDouble/bDouble; 
			}
		}
		
		double avgRatio = sumOfRatios/(a.length*a[0].length);
		return Math.abs(avgRatio);
	}
	
	
	private static int avgDiffArrays(int[][] a, int[][] b) {
		if (a.length != b.length || a[0].length != b[0].length) { // Incompatible.
			return Integer.MAX_VALUE;
		}
		int sumOfDiffs = 0;
		
		for (int x = 0; x < a.length; x++) {
			for (int y = 0; y < a[0].length; y++) {
				sumOfDiffs += Math.abs(a[x][y] - b[x][y]); 
			}
		}
		
		int avgDiff = sumOfDiffs / (a.length * a[0].length);
		
		return avgDiff;
	}
	
	
	public static void printArray(String title, double[][] in) {
		System.out.println(title);
		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < in.length; j++) {
				System.out.print(in[i][j] + "\t");
			}
			System.out.println();
		}
	}
	
	public static void printArray(String title, int[][] in) {

		System.out.println(title);
		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < in.length; j++) {
				System.out.print(in[i][j] + "\t");
			}
			System.out.println();
		}
	}
	
	public static void printArray(String title, int[] in) {
		for (int i = 0; i < in.length; i++) {
			System.out.print(in[i] + "\t");
		}
		System.out.print("\n");
	}
	
	private static int[][] getEmptyPixels(int w, int h) {
		int[][] pixels = new int[w][h];
		
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				pixels[x][y] = 0;
		
		return pixels;
	}
	
	private static double percentageZeroes(int[] in) {
		double numZeroes = 0.0;
		
		for (int i = 0; i < in.length; i++)
			if (in[i] == 0)
				numZeroes++;
		
		return numZeroes/((double) in.length);
	}
	
	public static void main(String[] args) {
		Test t = new Test("img1.jpg");
		t.fullRegressionTest(16, 100, true);;
	}
}
