
public class Huffman {
	
	private int N;
	private static final boolean ZZ_FORWARD = true;
	
	public Huffman(int blockSize) {
		this.N = blockSize;		
	}
	
	public void setBlockSize(int blockSize) {
		this.N = blockSize;
	}
	
	public int[] zigZag(int[][] in) {
		int[] out = new int[N*N];
		boolean direction = Huffman.ZZ_FORWARD;
		int x = 0;
		int y = 0;
		int index = 0;
		
		for (int i = 0 ; i < (2*N - 1); i++, direction = !direction) {
			if (direction == ZZ_FORWARD) {
				while (x >= 0 && y != N) {
					if (x == N) {
						x--;
						y++;
					}
					out[index] = in[x][y];
					x--;
					y++;
					index++;
				}
				x++;
			} else {
				while (y >= 0 && x != N) {
					if (y == N) {
						y--;
						x++;
					}
					out[index] = in[x][y];
					y--;
					x++;
					index++;
				}
				y++;
			}
		}
		return out;
	}
	
	public int[][] unZigZag(int[] in) {
		int out[][] = new int[N][N];
		
		for (int x = 0; x < N; x++)
			for (int y = 0; y < N; y++)
				out[x][y] = 11;
		
		boolean direction = Huffman.ZZ_FORWARD;
		int x = 0;
		int y = 0;
		int index = 0;
		
		while (x < N && y < N && index < in.length) {
			out[y][x] = in[index];
			index++;
			
			if (direction == ZZ_FORWARD) {
				if (y == 0 || x == (N-1)) {
					direction = !direction;
					if (x == N-1)
						y++;
					else
						x++;
				} else {
					y--;
					x++;
				}
			} else { // Backwards
				if (x == 0 || y == N-1) {
					direction = !direction;
					if (y == N-1)
						x++;
					else
						y++;
				} else {
					y++;
					x--;
				}
			}
		}
		return out;
	}

	
}
