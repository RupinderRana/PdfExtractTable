package scoreme.pdf.utility;

import lombok.experimental.UtilityClass;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

@UtilityClass
public class PdfGeometryUtil {

    public float getPageWidth(PDPage page) {
        PDRectangle mediaBox = page.getMediaBox();
        return mediaBox.getWidth();
    }

    public float getPageHeight(PDPage page) {
        PDRectangle mediaBox = page.getMediaBox();
        return mediaBox.getHeight();
    }

    public boolean isWithinBoundingBox(float x, float y, float minX, float minY, float maxX, float maxY) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public float[] findVerticalAlignments(float[] xPositions, float tolerance) {
        // This is a simplified implementation. You would need more sophisticated logic
        // for real-world PDFs
        return xPositions;
    }
}