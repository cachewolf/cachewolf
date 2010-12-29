import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;


public class ImageParser {

	static Hashtable<String, int[][]> validChars = new Hashtable<String, int[][]>();
	
	static {
		validChars.put(".", new int[][] {
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 0, 0 },
				{0, 1, 0 },
				{0, 1, 0 }
		});
		validChars.put("/", new int[][] {
				{0, 0, 0, 0, 1},
				{0, 0, 0, 1, 0},
				{0, 0, 0, 1, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 1, 0, 0, 0},
				{0, 1, 0, 0, 0},
				{1, 0, 0, 0, 0}
		});
		validChars.put("1", new int[][] {
				{0, 0, 1, 0, 0},
				{1, 1, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{0, 0, 1, 0, 0},
				{1, 1, 1, 1, 1}
		});
		validChars.put("2", new int[][] {
				{0, 1, 1, 1, 0},
				{1, 0, 0, 0, 1},
				{0, 0, 0, 0, 1},
				{0, 0, 0, 1, 0},
				{0, 0, 1, 0, 0},
				{0, 1, 0, 0, 0},
				{1, 0, 0, 0, 0},
				{1, 1, 1, 1, 1}
		});
		validChars.put("3", new int[][] {
				{0, 1, 1, 1, 0},
				{1, 0, 0, 0, 1},
				{0, 0, 0, 0, 1},
				{0, 0, 1, 1, 0},
				{0, 0, 0, 0, 1},
				{0, 0, 0, 0, 1},
				{1, 0, 0, 0, 1},
				{0, 1, 1, 1, 0}
		});
		validChars.put("4", new int[][] {
				{0, 0, 0, 0, 1, 0},
				{0, 0, 0, 1, 1, 0},
				{0, 0, 1, 0, 1, 0},
				{0, 1, 0, 0, 1, 0},
				{1, 0, 0, 0, 1, 0},
				{1, 1, 1, 1, 1, 1},
				{0, 0, 0, 0, 1, 0},
				{0, 0, 0, 0, 1, 0}
		});
		validChars.put("5", new int[][] {
				{1, 1, 1, 1, 1},
				{1, 0, 0, 0, 0},
				{1, 0, 0, 0, 0},
				{1, 1, 1, 1, 0},
				{0, 0, 0, 0, 1},
				{0, 0, 0, 0, 1},
				{1, 0, 0, 0, 1},
				{0, 1, 1, 1, 0}
		});
	}
	
	private static void parseImage(File datei) {
		if (datei.exists()) {
			try {
				BufferedImage bild = ImageIO.read(datei);

				String chars = testValidChars(bild);
				System.out.println(chars);
				
				String size= testSize(bild);
				System.out.println(size);

//				String image = printImage(bild);
//				System.out.println(image);
//				
//				String alpha = printAlpha(bild);
//				System.out.println(alpha);
			} catch (IOException e) {
				System.out.println("passt net");
				e.printStackTrace();
			}
		} else {
			System.out.println("File not found: "+datei.getAbsolutePath());
		}
	}

	private static String testSize(BufferedImage bild) {
		// -7005927
		int rgb = bild.getRGB(5, 23);
		if (rgb == -7005927) return "nano";
		rgb = bild.getRGB(10, 23);
		if (rgb == -7005927) return "small";
		rgb = bild.getRGB(17, 23);
		if (rgb == -7005927) return "regular";
		rgb = bild.getRGB(26, 23);
		if (rgb == -7005927) return "large";
		rgb = bild.getRGB(40, 23);
		if (rgb == -6735302) return "not_choosen";
		rgb = bild.getRGB(41, 24);
		if (rgb == -7005927) return "other";
		return null;
	}

	private static String testValidChars(BufferedImage bild) {
		StringBuffer sb = new StringBuffer();
		for (int startX = 0; startX < bild.getWidth(); startX++) {
			for (String validChar : validChars.keySet()) {
				if (testValidChar(bild, startX, 4, validChars.get(validChar))) {
	//				System.out.println(validChar);
	//				System.out.println("at "+startX+"/"+startY);
					sb.append(validChar);
				} else {
	//				System.out.println("keine "+validChar+" bei "+startX+"/"+startY);
				}
			}
		}
		return sb.toString();
	}

	private static boolean testValidChar(BufferedImage bild, int startX, int startY, int[][] validChar) {
		for (int y = 0; y < validChar.length; y++) {
			if (bild.getHeight() > startY+y) {
				for (int x = 0; x < validChar[0].length; x++) {
					if (bild.getWidth() > startX+x) {
						int[] alpha = bild.getAlphaRaster().getPixel(startX+x, startY+y, new int[1]);
						if ((alpha[0] == 0 && validChar[y][x] == 0) ||
								(alpha[0] > 0 && validChar[y][x] > 0)) {
							// matches
						} else {
							return false;
						} 
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private static String printImage(BufferedImage bild) {
		StringBuffer sb = new StringBuffer();
		for (int y = 0; y < bild.getHeight(); y++) {
			for (int x = 0; x < bild.getWidth(); x++) {
				int rgb = bild.getRGB(x, y);
				int[] alpha = bild.getAlphaRaster().getPixel(x, y, new int[1]);
//				Color farbe = new Color(rgb);
//				sb.append(pad(alpha[0]) + "|" + pad(farbe.getRed())+" "+pad(farbe.getGreen())+" "+pad(farbe.getBlue()));
				sb.append(pad(alpha[0]) + "|" + pad(rgb));
				if (x == bild.getWidth()-1) {
					sb.append("\n");
				} else {
					sb.append(", ");
				}
			}
		}
		return sb.toString();
	}

	private static String printAlpha(BufferedImage bild) {
		StringBuffer sb = new StringBuffer();
		for (int y = 0; y < bild.getHeight(); y++) {
			for (int x = 0; x < bild.getWidth(); x++) {
				int[] alpha = bild.getAlphaRaster().getPixel(x, y, new int[1]);
				sb.append(alpha[0] > 0 ? 1 : 0);
				if (x == bild.getWidth()-1) {
					sb.append("\n");
				} else {
					sb.append(", ");
				}
			}
		}
		return sb.toString();
	}

	private static String pad(int number) {
		Integer integer = new Integer(number);
		String string = integer.toString();
		int length = string.length();
		for (int i = 0; i < 8 - length; i++) {
			string = " "+string;
		}
		return string;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File datei ;
		datei = new File("micro.png");
		parseImage(datei);
		datei = new File("small.png");
		parseImage(datei);
		datei = new File("regular.png");
		parseImage(datei);
		datei = new File("large.png");
		parseImage(datei);
		datei = new File("not_choosen.png");
		parseImage(datei);
		datei = new File("other.png");
		parseImage(datei);
		datei = new File("2_1.png");
		parseImage(datei);
		datei = new File("2.5_1.5.png");
		parseImage(datei);
		datei = new File("3_1.5.png");
		parseImage(datei);
		datei = new File("4.5_2.5.png");
		parseImage(datei);
	}

}
