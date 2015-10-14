package analysis;

import java.awt.Point;

import static util.ArrayOps.*;
import static util.ImageOps.*;

public class TranslationDetection {

	public static Point calculateTranslation(double[] img1, double[] img2, int size){
		correlate(img1, img2, img1, size);
		displayLogNorm(img1, size, "correlation");
		return findMax2D(img1, size);
	}
	
}
