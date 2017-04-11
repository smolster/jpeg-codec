
public class MyImage {

	int N;
	int[][] pixels;
	
	int trueWidth; // Pixel width in
	int trueHeight; // Pixel height in
	
	int widthInBlocks;
	int heightInBlocks;
	
	public MyImage(int[][] pixels, int blockSize) {
		this.N = blockSize;
		this.trueWidth = pixels.length;
		this.trueHeight = pixels[0].length;
		this.pixels = pixels;
		
		this.updateBoundaries();
	}
	
	private void updateBoundaries() {
		// If 'pixels' extends at all into the next block, it should be counted.
		this.widthInBlocks = (pixels.length / N) + ((pixels.length % N > 0) ? 1 : 0);
		this.heightInBlocks = (pixels[0].length / N) + ((pixels[0].length % N > 0) ? 1 : 0);
	}
	
	public void setBlockSize(int blockSize) {
		this.N = blockSize;
		this.updateBoundaries();
	}
	
	public int[][] getMacroblock(int x, int y) {
		int[][] block = new int[N][N];
		int startX = x*N; // First row of block
		int startY = y*N; // First column of block
		
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				int pixelsXIndex = startX+i;
				int pixelsYIndex = startY+j;
				
				// If block extends off of image, mirror pixels for encoding.
				if (pixelsXIndex >= this.trueWidth) {
					int xDiff = pixelsXIndex - this.trueWidth;
					pixelsXIndex = this.trueWidth - xDiff - 1; // subtract 1 to account for 0-index
				}
				if (startY + j >= this.trueHeight) {
					int yDiff = pixelsYIndex - this.trueHeight;
					pixelsYIndex = this.trueHeight - yDiff - 1;
				}

				block[i][j] = pixels[pixelsXIndex][pixelsYIndex];
			}
		}
		
		return block;
	}
	
	public void setMacroblock(int x, int y, int[][] block) {
		int startX = x*N;
		int startY = y*N;
		
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				// If pixel to be set extends over true boundaries of image, disregard it.
				if (startX + i >= pixels.length || startY + j >= pixels[0].length)
					continue;
				else
					this.pixels[startX+i][startY+j] = block[i][j];
			}
		}
	}
	
	public int getPixel(int x, int y) {
		return this.pixels[x][y];
	}
	
	public int[][] getPixels() {
		return pixels;
	}
}
