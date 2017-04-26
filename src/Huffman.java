
public class Huffman {
	
	/*
	 * NOTE:	This is an incomplete Huffman implementation. As of 4-25-2017, this class only contains methods for
	 * 			transforming a macroblock in and out of 1-D format.
	 */
	
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

	public int[] compress(int[][] in) {
		int[] zz = zigZag(in);
		
		int[] out = new int[2*N*N]; // Worst possible case--i.e. there are no repeated consecutive elements
		// lets say evens (0, 2, etc.) are numbers, and odds are multipliers/counts.
		
		for (int inI = 0, outI = 0; outI < 2*N*N && inI < N*N ; outI+=2) {
			int count = 1;
			int value = zz[inI];
			while (true) {
				if (++inI < N*N && zz[inI] == value) {
					count++;
				} else break;
			}
			out[outI] = value;
			out[outI+1] = count;
		}
		
		return out;
	}
	
	public int[][] expand(int[] in) {
		int[] out = new int[N*N];
		
		for (int inI = 0, outI = 0; outI < N*N && inI < 2*N*N; inI+=2) {
			int value = in[inI];
			int count = in[inI+1];
			
			if (count == 0)
				break;
			
			int endI = count+outI;
			for (; outI < endI; outI++) {
				out[outI] = value;
			}
		}
		
		return unZigZag(out);
	}
}
