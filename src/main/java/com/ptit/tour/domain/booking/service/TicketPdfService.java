package com.ptit.tour.domain.booking.service;

import com.ptit.tour.domain.booking.dto.TicketDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TicketPdfService {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    public byte[] generate(TicketDto ticket) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            FontBundle fonts = loadFonts(document);
            boolean unicodeReady = fonts.unicodeReady();

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float pageHeight = page.getMediaBox().getHeight();
                float y = pageHeight - 64;

                y = writeLine(content, fonts.bold(), 20, 56, y, "PTOUR E-TICKET", unicodeReady);
                y = writeLine(content, fonts.regular(), 11, 56, y - 6, "Booking code: " + ticket.bookingCode(), unicodeReady);
                y = writeLine(content, fonts.regular(), 11, 56, y, "Issued at: " + DATE_FORMAT.format(ticket.issuedAt()), unicodeReady);
                y -= 16;

                for (String line : List.of(
                    "Tour: " + ticket.tourTitle(),
                    "Destination: " + ticket.destinationName(),
                    "Departure date: " + ticket.departureDate(),
                    "Duration: " + ticket.durationDays() + " days / " + ticket.durationNights() + " nights",
                    "Guests: " + ticket.guestCount(),
                    "Customer: " + ticket.customerName(),
                    "Email: " + ticket.customerEmail(),
                    "Phone: " + safe(ticket.customerPhone()),
                    "Total amount: " + ticket.totalAmount().toPlainString() + " VND",
                    "Status: " + ticket.status(),
                    "Download link: " + ticket.downloadUrl(),
                    "QR payload: " + ticket.qrCodeData()
                )) {
                    y = writeParagraph(content, fonts.regular(), 11, 56, y, line, unicodeReady);
                    y -= 4;
                }
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Khong the tao PDF cho e-ticket", ex);
        }
    }

    private FontBundle loadFonts(PDDocument document) throws IOException {
        Path regularPath = findFont(List.of(
            Path.of("C:/Windows/Fonts/arial.ttf"),
            Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf")
        ));
        Path boldPath = findFont(List.of(
            Path.of("C:/Windows/Fonts/arialbd.ttf"),
            Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf")
        ));

        if (regularPath != null && boldPath != null) {
            return new FontBundle(
                PDType0Font.load(document, Files.newInputStream(regularPath)),
                PDType0Font.load(document, Files.newInputStream(boldPath)),
                true
            );
        }

        return new FontBundle(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, false);
    }

    private Path findFont(List<Path> candidates) {
        return candidates.stream().filter(Files::exists).findFirst().orElse(null);
    }

    private float writeParagraph(
        PDPageContentStream content,
        PDFont font,
        int fontSize,
        float x,
        float y,
        String text,
        boolean unicodeReady
    ) throws IOException {
        String prepared = prepare(text, unicodeReady);
        List<String> lines = wrap(prepared, 92);
        float currentY = y;
        for (String line : lines) {
            currentY = writeLine(content, font, fontSize, x, currentY, line, true);
        }
        return currentY;
    }

    private float writeLine(
        PDPageContentStream content,
        PDFont font,
        int fontSize,
        float x,
        float y,
        String text,
        boolean unicodeReady
    ) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(prepare(text, unicodeReady));
        content.endText();
        return y - (fontSize + 6);
    }

    private List<String> wrap(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return List.of(text);
        }

        StringBuilder current = new StringBuilder();
        List<String> lines = new java.util.ArrayList<>();
        for (String word : text.split(" ")) {
            if (current.length() > 0 && current.length() + word.length() + 1 > maxLength) {
                lines.add(current.toString());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(word);
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String prepare(String text, boolean unicodeReady) {
        String value = safe(text);
        if (unicodeReady) {
            return value;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('đ', 'd')
            .replace('Đ', 'D');
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private record FontBundle(PDFont regular, PDFont bold, boolean unicodeReady) {}
}
