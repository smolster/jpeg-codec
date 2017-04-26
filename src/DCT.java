
public class DCT {

	private int N; // Block size
	private double[][] c;
	
	public DCT(int blockSize) {
		this.N = blockSize;
		initCoeffMatrix();
	}
	
	public void setBlockSize(int blockSize) {
		this.N = blockSize;
		initCoeffMatrix();
	}
	
	private void initCoeffMatrix() {
		this.c = new double[N][N];
		
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < N; y++) {
				if (x == 0 || y == 0)
					c[x][y] = Math.sqrt(2)/N;
				else
					c[x][y] = 2.0/N;
			}
		}
		c[0][0] = 1.0/N;

	}

	public int[][] forwardDCT(int [][] in) {
		// Pixels received are range 0 to 255. Let's shift those down to -128 to 127.
		int[][] shiftedIn = new int[N][N];
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				shiftedIn[i][j] = in[i][j] - 128;
		
		int[][] out = new int[N][N];
		
		for (int u = 0; u < N; u++) {
			for (int v = 0; v < N; v++) {
				double NDouble = (double) N;
				double uDouble = (double) u;
				double vDouble = (double) v;
				double sum = 0.0;
				
				for (int x = 0; x < N; x++) {
					for (int y = 0; y < N; y++) {
						double xDouble = (double) x;
						double yDouble = (double) y;
						double shiftedInDouble = (double) shiftedIn[x][y];
						
						sum += shiftedInDouble * Math.cos((2.0*xDouble + 1.0) * uDouble * (Math.PI/(2.0*NDouble)))
											* Math.cos((2.0*yDouble + 1.0) * vDouble * (Math.PI/(2.0*NDouble)));
					}
				}
				sum *= c[u][v];
				out[u][v] = (int) Math.round(sum);
			}
		}
		return out;
	}
	
	public int[][] reverseDCT(int[][] in) {
		int[][] out = new int[N][N];
		
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < N; y++) {
				double NDouble = (double) N;
				double xDouble = (double) x;
				double yDouble = (double) y;
				double sum = 0.0;
				
				for (int u = 0; u < N; u++) {
					for (int v = 0; v < N; v++) {
						double cosXU = Math.cos((2*xDouble + 1.0) * u * (Math.PI/(2.0*NDouble)));
						double cosYV = Math.cos((2*yDouble + 1.0) * v * (Math.PI/(2.0*NDouble)));
						
						double toAdd = c[u][v] * in[u][v] * cosXU * cosYV;
						sum += toAdd;
					}
				}
				
				out[x][y] = (int) Math.round(sum);
			}
		}
		
		// Unshift 'out'
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < N; y++) {
				out[x][y] += 128;
				if (out[x][y] > 255)
					out[x][y] = 255;
				else if (out[x][y] < 0)
					out[x][y] = 0;
			}
		}
		
		return out;
	}
}
