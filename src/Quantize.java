
public class Quantize {

	private int N;
	private int quality;
	private int[][] quantMatrix;
	
	public Quantize(int blockSize, int quality) {
		this.N = blockSize;
		setQuality(quality);
		
		initQuantMatrix();
	}
	
	/* This scaling function taken from the JPEG 6b libraries. */
	public void setQuality (int quality) {
		if (quality <= 0)
			this.quality = 1;
		if (quality > 100)
			this.quality = 100;
		if (quality < 50)
			this.quality = 5000 / quality;
		else
			this.quality = 200 - quality*2;
	}
	
	private void initQuantMatrix() {
		this.quantMatrix = new int[N][N];
		
		int[][] stQuant = getStandardQuantMatrix();
		
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < N; y++) {
				if (x < 8 && y < 8) {
					quantMatrix[x][y] = stQuant[x][y];
				} else if (x >= 8 && y < 8) {
					quantMatrix[x][y] = stQuant[7][y] + 15*(8-x+1);
				} else if (x < 8 && y >= 8) {
					quantMatrix[x][y] = stQuant[x][7] + 15*(8-y+1);
				} else {
					quantMatrix[x][y] = ((quantMatrix[x-1][y] + quantMatrix[x][y-1])/2) + 15;
				}
				
				// Apply scale factor
				double tempQuant = ((double) (quantMatrix[x][y] * quality + 50))/100.0;
				int tempQuantInt = (int) Math.round(tempQuant);
				
				if (tempQuantInt <= 0)
					tempQuantInt = 0;

				if (tempQuantInt > 32767)
					tempQuantInt = 32767;
				
				quantMatrix[x][y] = tempQuantInt;
			}
		}
	}
	
	public void setBlockSize(int blockSize) {
		this.N = blockSize;
		initQuantMatrix();
	}
	
	/* This method returns a standard 8x8 quantization matrix */
	
	private static int[][] getStandardQuantMatrix() {
		int[][] matrix = new int[8][];
		
		int[] row1 = {16, 11, 10, 16, 24, 40, 51, 61};
		int[] row2 = {12, 12, 14, 19, 26, 58, 60, 55};
		int[] row3 = {14, 13, 16, 24, 40, 57, 69, 56};
		int[] row4 = {14, 17, 22, 29, 51, 87, 80, 62};
		int[] row5 = {18, 22, 37, 56, 68, 109, 103, 77};
		int[] row6 = {24, 35, 55, 64, 81, 104, 113, 92};
		int[] row7 = {49, 64, 78, 87, 103, 121, 120, 101};
		int[] row8 = {72, 92, 95, 98, 112, 100, 103, 99};
		
		matrix[0] = row1;
		matrix[1] = row2;
		matrix[2] = row3;
		matrix[3] = row4;
		matrix[4] = row5;
		matrix[5] = row6;
		matrix[6] = row7;
		matrix[7] = row8;
		
		return matrix;
	}
	
	public int[][] quantize(int[][] in) {
		int[][] out = new int[N][N];		
		
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				out[i][j] = (int) ( Math.round( (in[i][j] / ( (int) (quantMatrix[i][j]))) ));
		
		return out;
	}
	
	public int[][] dequantize(int[][] in) {
		int[][] out = new int[N][N];
		
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				out[i][j] = quantMatrix[i][j] * in[i][j];
		
		return out;
	}
}
