import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
public class ImageHandler {

	public ImageHandler() {}
	
	//TODO: Allow getPixels to handle both interleaved and non-interleaved images
	
	public int[][] getPixels(String filename) {
		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("ERROR -- File " + filename + " does not exist. Cannot get pixels.");
			return null;
		}
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			System.out.println("ERROR, couldn't read file.");
			return null;
		}
		
		Raster r = img.getData(); // This is a single tile containing all the pixels
		int W = r.getWidth();
		int H = r.getHeight();
		
		int[][] pixels = new int[W][H];
		
		for (int x = 0; x < W; x++) {
			for (int y = 0; y < H; y++) {
				pixels[x][y] = r.getSample(x, y, 0);
			}
		}
		
		return pixels;
	}
	
	public void writeToFile(int[][] pixels, String filename) {
		File fileOut = new File(filename);
		
		BufferedImage imageOut = new BufferedImage(pixels.length, pixels[0].length, BufferedImage.TYPE_BYTE_GRAY);
		
		WritableRaster rOut = imageOut.getRaster();
	
		for (int x = 0; x < imageOut.getWidth(); x++) {
			for (int y = 0; y < imageOut.getHeight(); y++) {
				rOut.setSample(x, y, 0, pixels[x][y]);
			}
		}
		
		imageOut.setData(rOut);
		
		try {
			ImageIO.write(imageOut, "jpg", fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
