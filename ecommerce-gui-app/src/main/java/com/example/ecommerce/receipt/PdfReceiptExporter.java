package com.example.ecommerce.receipt;

import com.example.ecommerce.checkout.CheckoutReceipt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Automatically writes a branded, paginated PDF into the configured receipt directory. */
public final class PdfReceiptExporter implements ReceiptExporter {

    private static final float MARGIN = 44;
    private static final float FONT_SIZE = 11;
    private static final float LINE_HEIGHT = 17;
    private static final int LINES_PER_PAGE = 39;

    private final Path directory;

    public PdfReceiptExporter(Path directory) {
        this.directory = Objects.requireNonNull(directory).toAbsolutePath().normalize();
    }

    @Override
    public Path export(CheckoutReceipt receipt) throws IOException {
        Objects.requireNonNull(receipt);
        Files.createDirectories(directory);
        Path output = directory.resolve("GlobalMart-" + receipt.receiptNumber() + ".pdf");

        byte[] data;
        try (PDDocument document = new PDDocument();
             InputStream fontData = PDDocument.class.getResourceAsStream(
                     "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf")) {
            if (fontData == null) {
                throw new IOException("The bundled receipt font is unavailable.");
            }
            PDFont font = PDType0Font.load(document, fontData);
            List<String> lines = wrapLines(receipt.toDisplayText(), font,
                    PDRectangle.A4.getWidth() - 2 * MARGIN);

            for (int start = 0; start < lines.size(); start += LINES_PER_PAGE) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    drawBrand(content, font, page.getMediaBox().getHeight() - MARGIN);
                    content.setNonStrokingColor(0.09f, 0.15f, 0.21f);
                    content.beginText();
                    content.setFont(font, FONT_SIZE);
                    content.setLeading(LINE_HEIGHT);
                    content.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN - 57);
                    int end = Math.min(start + LINES_PER_PAGE, lines.size());
                    for (String line : lines.subList(start, end)) {
                        content.showText(line);
                        content.newLine();
                    }
                    content.endText();
                }
            }

            document.getDocumentInformation().setTitle("GlobalMart Receipt " + receipt.receiptNumber());
            document.getDocumentInformation().setAuthor("GlobalMart");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            document.save(bytes);
            data = bytes.toByteArray();
        }

        // CREATE_NEW prevents a later checkout from overwriting an existing receipt.
        boolean created = false;
        try (OutputStream file = Files.newOutputStream(output,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            created = true;
            file.write(data);
        } catch (IOException exception) {
            // A failed new-file write may leave an incomplete file; keep the download folder clean.
            if (created) {
                try {
                    Files.deleteIfExists(output);
                } catch (IOException cleanupFailure) {
                    exception.addSuppressed(cleanupFailure);
                }
            }
            throw exception;
        }
        return output;
    }

    private List<String> wrapLines(String text, PDFont font, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String original : text.split("\\R", -1)) {
            String remaining = original;
            while (!remaining.isEmpty() && font.getStringWidth(remaining) / 1000 * FONT_SIZE > width) {
                int end = remaining.length();
                while (end > 1 && font.getStringWidth(remaining.substring(0, end)) / 1000 * FONT_SIZE > width) {
                    end--;
                }
                int space = remaining.lastIndexOf(' ', end);
                if (space > 0) {
                    end = space;
                }
                lines.add(remaining.substring(0, end));
                remaining = remaining.substring(end).stripLeading();
            }
            lines.add(remaining);
        }
        return lines;
    }

    private void drawBrand(PDPageContentStream content, PDFont font, float top) throws IOException {
        content.setStrokingColor(0.13f, 0.39f, 0.65f);
        content.setLineWidth(1.4f);
        drawEllipse(content, MARGIN + 14, top - 14, 13, 13);
        drawEllipse(content, MARGIN + 14, top - 14, 5, 13);
        content.moveTo(MARGIN + 1, top - 14);
        content.lineTo(MARGIN + 27, top - 14);
        content.stroke();

        content.setNonStrokingColor(0.09f, 0.20f, 0.30f);
        content.beginText();
        content.setFont(font, 24);
        content.newLineAtOffset(MARGIN + 38, top - 22);
        content.showText("GlobalMart");
        content.endText();
    }

    private void drawEllipse(PDPageContentStream content, float x, float y, float rx, float ry)
            throws IOException {
        float k = 0.552285f;
        content.moveTo(x + rx, y);
        content.curveTo(x + rx, y + k * ry, x + k * rx, y + ry, x, y + ry);
        content.curveTo(x - k * rx, y + ry, x - rx, y + k * ry, x - rx, y);
        content.curveTo(x - rx, y - k * ry, x - k * rx, y - ry, x, y - ry);
        content.curveTo(x + k * rx, y - ry, x + rx, y - k * ry, x + rx, y);
        content.closePath();
        content.stroke();
    }
}
