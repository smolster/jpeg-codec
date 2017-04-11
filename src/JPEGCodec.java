public class JPEGCodec {
	
	private int N; // Macroblock size
	private DCT dct;
	private Quantize quant;
	private Huffman huff;
	
	public JPEGCodec(int blockSize, int quality) {
		this.N = blockSize;
		this.dct = new DCT(N);
		this.quant = new Quantize(N, quality);
		this.huff = new Huffman(N);
	}
	
	public void setBlockSize(int blockSize) {
		this.N = blockSize;
		dct.setBlockSize(N);
		quant.setBlockSize(N);
		huff.setBlockSize(N);
	}
	
	public void setQuality(int quality) {
		quant.setQuality(quality);
	}
	
	public void encodeImage(String filepathIn, String filepathOut, int quality, int blockSize) {
		setBlockSize(blockSize);
		ImageHandler h = new ImageHandler();
		int[][] pixels = h.getPixels(filepathIn);
		if (pixels == null) {
			System.out.println("Could not load pixels from " + filepathIn + ". Terminating.");
			return;
		}
		
		MyImage image = new MyImage(pixels, blockSize);
		
		MyImage imageOut = new MyImage(JPEGCodec.getEmptyPixels(image.trueWidth ,image.trueHeight), blockSize);
		
		for (int x = 0; x < image.widthInBlocks; x++) {
			for (int y = 0; y < image.heightInBlocks; y++) {
				int[][] in = image.getMacroblock(x, y);
				
				int[][] out = decodeBlock(encodeBlock(in));
				
				imageOut.setMacroblock(x, y, out);
			}
		}
		
		h.writeToFile(imageOut.getPixels(), filepathOut);
	}
	
	private int[] encodeBlock(int[][] in) {
		
		int[][] dctBlock = dct.forwardDCT(in);
		
		int[][] quantizedBlock = quant.quantize(dctBlock);
		
		int[] huffmanArray = huff.zigZag(quantizedBlock);
		
		return huffmanArray;
	}
	
	public int[][] decodeBlock(int[] huffmanArrray) {
		int[][] quantizedBlock = huff.unZigZag(huffmanArrray);
		
		int[][] dctBlock = quant.dequantize(quantizedBlock);

		int[][] pixels = dct.reverseDCT(dctBlock);
		
		return pixels;
	}
	
	public static int[][] getEmptyPixels(int w, int h) {
		int[][] pixels = new int[w][h];
	
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				pixels[x][y] = 0;
	
		return pixels;
	}
	
}