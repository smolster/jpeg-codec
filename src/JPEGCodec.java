public class JPEGCodec {
	
	private int N; // Macroblock size
	private int quality;
	private DCT dct;
	private Quantize quant;
	private Huffman huff;
	
	public JPEGCodec(int blockSize, int quality) {
		this.N = blockSize;
		this.dct = new DCT(N);
		this.quant = new Quantize(N, quality);
		this.huff = new Huffman(N);
		
		if (quality > 100) quality = 100;
		if (quality < 1) quality = 1;
		this.quality = quality;
	}
	
	public void setBlockSize(int blockSize) {
		this.N = blockSize;
		dct.setBlockSize(N);
		quant.setBlockSize(N);
		huff.setBlockSize(N);
	}
	
	public void setQuality(int quality) {
		if (quality > 100) quality = 100;
		if (quality < 1) quality = 1;
		this.quality = quality;
		quant.setQuality(quality);
	}
	
	public int getBlockSize() {
		return this.N;
	}
	
	public int getQuality() {
		return this.quality;
	}
	
	public MyImage encodeImage(MyImage imgIn, int blockSize, int quality) {
		this.setQuality(quality);
		this.setBlockSize(blockSize);
		
		return encodeImage(imgIn);
	}
	
	public MyImage encodeImage(MyImage imgIn) {
		MyImage imgOut = new MyImage(JPEGCodec.getEmptyPixels(imgIn.trueWidth, imgIn.trueHeight), this.N);
		
		for (int x = 0; x < imgIn.widthInBlocks; x++) {
			for (int y = 0; y < imgIn.heightInBlocks; y++) {
				int[][] in = imgIn.getMacroblock(x, y);
				
				int[][] out = decodeBlock(encodeBlock(in));
				
				imgOut.setMacroblock(x, y, out);
			}
		}
		return imgOut;
	}
	
	private int[] encodeBlock(int[][] in) {
		
		int[][] dctBlock = dct.forwardDCT(in);
		
		int[][] quantizedBlock = quant.quantize(dctBlock);
		
		int[] huffmanArray = huff.zigZag(quantizedBlock);
		
		//TODO: Huffman encode.
		
		return huffmanArray;
	}
	
	public int[][] decodeBlock(int[] huffmanArray) {
		//TODO: Huffman decode.
		
		int[][] quantizedBlock = huff.unZigZag(huffmanArray);
		
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