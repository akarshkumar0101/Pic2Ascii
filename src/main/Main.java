package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import ui.FrameWrapper;

public class Main {
	static int ascii = ' ';

	public static void main(String[] args) {

		Map<Character, byte[][]> fontMap = Main.establishFontMap();

		final BufferedImage inputImg;
		try {
			inputImg = ImageIO.read(new File("profile copy.jpeg"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		final byte[][] inputImgData = Main.extractGrayScaleBytes(inputImg);

		JComponent jcomp = new JComponent() {
			private static final long serialVersionUID = 8390218429482709636L;

			@Override
			public void paintComponent(Graphics g) {
				byte[][] qmap = fontMap.get((char) Main.ascii);
				// System.out.println(Main.ascii);

				qmap = inputImgData;
				double pixWidth = (double) this.getWidth() / qmap.length;
				double pixHeight = (double) this.getHeight() / qmap[0].length;

				for (int x = 0; x < qmap.length; x += 1) {
					for (int y = 0; y < qmap[0].length; y += 1) {
						// System.out.println((int)((char) data[x][y]));
						int pixdata = qmap[x][y] & 0xFF;
						g.setColor(new Color(pixdata, pixdata, pixdata));

						g.fillRect((int) (x * pixWidth), (int) (y * pixHeight), (int) pixWidth + 1,
								(int) pixHeight + 1);
					}
				}
			}
		};
		jcomp.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				Main.ascii++;
				jcomp.repaint();
			}
		});
		FrameWrapper<JComponent> frame = new FrameWrapper<>("temp", 900, 900, true, false);

		frame.setComponent(jcomp);
		frame.setVisible(true);
		jcomp.requestFocus();

		final int asciiImgWidth = 150;
		final int asciiImgHeight = 100;

		double pixWidth = (double) inputImgData.length / asciiImgWidth;
		double pixHeight = (double) inputImgData[0].length / asciiImgHeight;

		for (int y = 0; y < asciiImgHeight; y++) {
			for (int x = 0; x < asciiImgWidth; x++) {

				int minDiff = Integer.MAX_VALUE;
				char minDiffChar = '\0';
				for (char c : fontMap.keySet()) {
					int diff = 0;

					byte[][] fontImg = fontMap.get(c);
					byte[][] imgPix = Main.subImage(inputImgData, (int) (x * pixWidth), (int) (y * pixHeight),
							(int) pixWidth, (int) pixHeight);
					diff = Main.calculateDiff(fontImg, imgPix, fontImg.length, fontImg[0].length);

					if (diff < minDiff) {
						minDiff = diff;
						minDiffChar = c;
					}
				}

				System.out.print(minDiffChar);
			}
			System.out.println();
		}

	}

	public static Map<Character, byte[][]> establishFontMap() {
		final BufferedImage fontImg;

		try {
			fontImg = ImageIO.read(new File("font map2.png"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		final byte[][] fontImgData = Main.extractGrayScaleBytes(fontImg);

		double charFontImgWidth = (double) fontImgData.length / 20;
		double charFontImgHeight = (double) fontImgData[0].length / 6;

		Map<Character, byte[][]> fontMap = new HashMap<>();
		for (int x = 0; x < 20; x++) {
			for (int y = 0; y < 6; y++) {
				int ascii = 20 * (y + 1) + x;
				if (ascii >= 33 && ascii <= 126) {
					Character c = (char) ascii;
					byte[][] charFont = Main.subImage(fontImgData, (int) (x * charFontImgWidth),
							(int) (y * charFontImgHeight), (int) charFontImgWidth, (int) charFontImgHeight);
					fontMap.put(c, charFont);
				}
			}
		}
		byte[][] spaceFont = new byte[(int) charFontImgWidth][(int) charFontImgHeight];
		for (int x = 0; x < (int) charFontImgWidth; x++) {
			for (int y = 0; y < (int) charFontImgHeight; y++) {
				spaceFont[x][y] = 0;
			}
		}

		fontMap.put(' ', spaceFont);

		return fontMap;
	}

	public static byte[][] extractGrayScaleBytes(BufferedImage colorImg) {
		BufferedImage grayImg = Main.toGrayScale(colorImg);

		byte[][] data = new byte[grayImg.getWidth()][grayImg.getHeight()];

		for (int x = 0; x < grayImg.getWidth(); x++) {
			for (int y = 0; y < grayImg.getHeight(); y++) {
				data[x][y] = (byte) grayImg.getRGB(x, y);
			}
		}

		return data;
	}

	public static BufferedImage toGrayScale(BufferedImage colorImg) {
		BufferedImage grayImg = new BufferedImage(colorImg.getWidth(), colorImg.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = grayImg.getGraphics();
		g.drawImage(colorImg, 0, 0, null);
		g.dispose();
		return grayImg;
	}
	public static byte[][] subImage(byte[][] img, int xStart, int yStart, int width, int height) {
		byte[][] sub = new byte[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				sub[x][y] = img[x + xStart][y + yStart];
			}
		}
		return sub;
	}

	public static int calculateDiff(byte[][] img1, byte[][] img2, int xPrecision, int yPrecision) {
		int diff = 0;
		double img1xScale = (double) img1.length / xPrecision;
		double img2xScale = (double) img2.length / xPrecision;

		double img1yScale = (double) img1[0].length / yPrecision;
		double img2yScale = (double) img2[0].length / yPrecision;

		for (int x = 0; x < xPrecision; x++) {
			for (int y = 0; y < yPrecision; y++) {
				int img1Data = img1[(int) (x * img1xScale)][(int) (y * img1yScale)] & 0xFF;
				int img2Data = img2[(int) (x * img2xScale)][(int) (y * img2yScale)] & 0xFF;

				diff += -Math.abs(img1Data - img2Data);
			}
		}

		return diff;
	}

}
