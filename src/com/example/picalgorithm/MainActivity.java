package com.example.picalgorithm;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	String colour;

	private final int REQUIRED_SIZE = 140;
	private static final float STATUS_PRAG = (float) 0.20;

	private static int picture_height = 0;
	private static int picture_width = 0;

	private static final int ColorNo1 = 0; // za toleranciju!
	private static final int ColorNo2 = 0;

	private static double mainCounter = 0;
	private static double colorMatchCounter = 0;

	private static String mostComonNo1 = null;
	private static String mostComonNo2 = null;

	private static final int SELECT_PHOTO = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button btn_odaberi = (Button) findViewById(R.id.button_odaberi);
		btn_odaberi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, SELECT_PHOTO);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		colorMatchCounter = 0;
		mainCounter = 0;

		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = imageReturnedIntent.getData();
				//odavde treba uzeti Bitmapu koju kamera pošalje!
				//modificirati samo treba decdeUri() da smanjuje dobivenu bitmapu -> resizePicture() metoda!
				Bitmap bitmap_image = null;
				try {
					bitmap_image = decodeUri(selectedImage);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				picture_height = bitmap_image.getHeight();
				picture_width = bitmap_image.getWidth();
				String visina = Integer.toString(picture_height);
				String sirina = Integer.toString(picture_width);
				Log.w("PICTURE", visina);
				Log.w("PICTURE", sirina);

				for (int i = 0; i < picture_width; i++) {

					for (int j = 0; j < picture_height; j++) {

						int rgb = bitmap_image.getPixel(i, j);
						int[] rgbArr = getRGBArr(rgb);

						if (isSimilarToColors(rgbArr)) {
							colorMatchCounter++;
						}
						mainCounter++;
					}
				}

				// rezultat statusa
				double rezultat = (colorMatchCounter / mainCounter);

				String counterMain = Double.toString(mainCounter);
				String str_colorMatch = Double.toString(colorMatchCounter);
				Log.w("ALGORITAMMMM", "broj: " + counterMain);
				Log.w("ALGORITAMMMM", "broj pogodaka: " + str_colorMatch);

				BitmapDrawable ob = new BitmapDrawable(bitmap_image);
				ImageView iw = (ImageView) findViewById(R.id.imageView1);
				iw.setBackgroundDrawable(ob);

				TextView tv_sirina_slike = (TextView) findViewById(R.id.tv_sirina_slike);
				tv_sirina_slike.setText(sirina);
				TextView tv_visina_slike = (TextView) findViewById(R.id.tv_visina_slike);
				tv_visina_slike.setText(visina);
				TextView tv_broj_piksela = (TextView) findViewById(R.id.tv_broj_piksela);
				tv_broj_piksela.setText(counterMain);
				TextView tv_broj_piksela_pogodak = (TextView) findViewById(R.id.tv_broj_piksela_pogodak);
				tv_broj_piksela_pogodak.setText(str_colorMatch);
				TextView tv_status = (TextView) findViewById(R.id.tv_status);
				//
				if (rezultat >= STATUS_PRAG) {
					tv_status.setText("FIRE AWAY!");
				} else {
					tv_status.setText("Not good enough!");
				}

			}
		}
	}

	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o);

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o2);

	}

	public static int[] getRGBArr(int pixel) {

		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		return new int[] { red, green, blue };

	}

	// http://www.rapidtables.com/web/color/RGB_Color.htm
	public static boolean isSimilarToColors(int[] rgbArr) {
		int[] colorNumber1 = { 255, 10, 10 };
		// int[] colorNumber1 = { 10, 10, 255 };
		int[] zelena = { 0, 220, 0 };
		int tolerance = 150;

		int rrDif = rgbArr[0] - colorNumber1[0];
		int rgDif = rgbArr[1] - colorNumber1[1];
		int rbDif = rgbArr[2] - colorNumber1[2];

		if ((rrDif >= -tolerance)
				&& (rgDif >= -tolerance && rgDif <= tolerance)
				&& (rbDif >= -tolerance && rbDif <= tolerance)) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * @SuppressWarnings("unchecked") public static void
	 * getMostCommonColour(HashMap<Integer, Integer> map) {
	 * 
	 * List list = new LinkedList(map.entrySet()); Collections.sort(list, new
	 * Comparator() { public int compare(Object o1, Object o2) {
	 * 
	 * return ((Comparable) ((Map.Entry) (o1)).getValue())
	 * .compareTo(((Map.Entry) (o2)).getValue());
	 * 
	 * }
	 * 
	 * });
	 * 
	 * Map.Entry me = (Map.Entry) list.get(list.size() - 1); int[] rgb =
	 * getRGBArr((Integer) me.getKey()); mostComonNo1 = Integer.toString(rgb[0])
	 * + " " + Integer.toString(rgb[1]) + " " + Integer.toString(rgb[2]);
	 * 
	 * Map.Entry me2 = (Map.Entry) list.get(list.size() - 2); int[] rgb2 =
	 * getRGBArr((Integer) me2.getKey()); mostComonNo2 =
	 * Integer.toString(rgb2[0]) + " " + Integer.toString(rgb2[1]) + " " +
	 * Integer.toString(rgb2[2]);
	 * 
	 * }
	 */

	/*
	 * public static boolean isGray(int[] rgbArr) {
	 * 
	 * int rgDiff = rgbArr[0] - rgbArr[1]; int rbDiff = rgbArr[0] - rgbArr[2];
	 * 
	 * int tolerance = 10;
	 * 
	 * if (rgDiff > tolerance || rgDiff < -tolerance) if (rbDiff > tolerance ||
	 * rbDiff < -tolerance) {
	 * 
	 * return false;
	 * 
	 * }
	 * 
	 * return true; }
	 */

	/*
	 * Klikom do boje! ImageView iw = (ImageView)findViewById(R.id.imageView1);
	 * final Bitmap bitmap = ((BitmapDrawable)iw.getDrawable()).getBitmap();
	 * iw.setOnTouchListener(new OnTouchListener(){
	 * 
	 * @Override public boolean onTouch(View v, MotionEvent event){ int x =
	 * (int)event.getX(); int y = (int)event.getY(); int pixel =
	 * bitmap.getPixel(x,y);
	 * 
	 * //then do what you want with the pixel data, e.g int redValue =
	 * Color.red(pixel); int blueValue = Color.blue(pixel); int greenValue =
	 * Color.green(pixel); String red = Integer.toString(redValue);
	 * Log.w("ALGORITAM red", red); if (redValue > 200){ Log.w("ALGORITAM 200",
	 * "EVENT!"); } return false; } }); int dominanta =
	 * getDominantColor2(bitmap); String dom = Integer.toString(dominanta);
	 * Log.w("ALGORITAM dominantna", dom);
	 */

	/*
	 * za getDominantColor metodu! final Bitmap bitmap =
	 * BitmapFactory.decodeResource(getApplicationContext().getResources(),
	 * R.drawable.ic_launcher); int dominantColor = getDominantColor(bitmap);
	 * String domin = Integer.toString(dominantColor);
	 * Log.w("ALGORITAM dominantna: ", domin);
	 */

	/*
	 * public int getDominantColor(Bitmap bitmap) { if (bitmap == null) throw
	 * new NullPointerException();
	 * 
	 * int width = bitmap.getWidth(); int height = bitmap.getHeight(); int size
	 * = width * height; int pixels[] = new int[size];
	 * 
	 * Bitmap bitmap2 = bitmap.copy(Bitmap.Config.ARGB_8888, false);
	 * 
	 * bitmap2.getPixels(pixels, 0, width, 0, 0, width, height);
	 * 
	 * HashMap<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
	 * 
	 * int color = 0; Integer count = 0; for (int i = 0; i < pixels.length; i++)
	 * { color = pixels[i]; count = colorMap.get(color); if (count == null)
	 * count = 0; colorMap.put(color, ++count); }
	 * 
	 * int dominantColor = 0; int max = 0; for (Map.Entry<Integer, Integer>
	 * entry : colorMap.entrySet()) { if (entry.getValue() > max) { max =
	 * entry.getValue(); dominantColor = entry.getKey(); } } return
	 * dominantColor; }
	 */

}
