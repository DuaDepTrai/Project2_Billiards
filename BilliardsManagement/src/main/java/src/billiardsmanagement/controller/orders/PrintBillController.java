package src.billiardsmanagement.controller.orders;

import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.pdfbox.Loader;
import src.billiardsmanagement.model.Bill;
import src.billiardsmanagement.model.BillItem;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

import java.nio.file.Files;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

public class PrintBillController {
    private static Font billTimeFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
    private static String currentBillName = "";

    public static String handleCustomerName(String customerName) {
        String[] cuts = customerName.split(" ");
        return cuts[cuts.length - 1];
    }

    public static void showPdfBillToScreen() {
        AnchorPane ap = new AnchorPane();
        ap.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        VBox box = new VBox();

        // Use ScrollPane if the bill's length exceeds the scene
        ScrollPane scrollPane = new ScrollPane(box);
        scrollPane.setFitToWidth(true);

        try {
            if (!currentBillName.isEmpty()) {
                PDDocument document = Loader.loadPDF(new File(currentBillName));
                PDFRenderer pdfRenderer = new PDFRenderer(document);

                for (int page = 0; page < document.getNumberOfPages(); page++) {
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

                    // Trim unnecessary parts of the image (adjust the coordinates as needed)
                    int trimX = 0; // Adjust as needed
                    int trimY = 0; // Adjust as needed
                    int trimWidth = bim.getWidth() - trimX; // New width
                    int trimHeight = bim.getHeight() - trimY; // New height

                    // Crop the image
                    BufferedImage croppedBim = bim.getSubimage(trimX, trimY, trimWidth, trimHeight);

                    // Save the trimmed image as a PNG file
                    File outputFile = new File(currentBillName + "-" + (page + 1) + ".png");
                    ImageIO.write(croppedBim, "PNG", outputFile);

                    // Load the image file into a JavaFX Image
                    Image image = new Image(outputFile.toURI().toString());
                    ImageView imageView = new ImageView(image);

                    // Set ImageView properties to fit the scene
                    imageView.setFitWidth(300); // Set desired width
                    imageView.setPreserveRatio(true); // Preserve aspect ratio
                    imageView.setSmooth(true); // Enable smoothing

                    // Add the ImageView to the VBox
                    box.getChildren().add(imageView);
                }
                document.close();
            }


            Stage stage = new Stage();

            // Thêm nút Cancel
            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(event -> {
                stage.close(); // Đóng cửa sổ khi nhấn nút Cancel
            });

            ap.getChildren().add(box);
            ap.getChildren().add(cancelButton);

            stage.setTitle("PDF Bill Invoice");
            stage.setScene(new Scene(scrollPane, 300, 500));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cutPdfBill() throws IOException {
        PdfReader reader = new PdfReader(currentBillName);
        PdfWriter writer = new PdfWriter(new FileOutputStream(currentBillName.split("\\.")[0] + "_new.pdf"));
        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        PdfPage page = pdfDoc.getPage(1); // Get the first page

        List<Float> yCoords = new ArrayList<>();

        // Sử dụng LocationTextExtractionStrategy để lấy tọa độ văn bản
        IEventListener listener = new LocationTextExtractionStrategy() {
            @Override
            public void eventOccurred(com.itextpdf.kernel.pdf.canvas.parser.data.IEventData data, com.itextpdf.kernel.pdf.canvas.parser.EventType type) {
                if (type == com.itextpdf.kernel.pdf.canvas.parser.EventType.RENDER_TEXT) {
                    TextRenderInfo textRenderInfo = (TextRenderInfo) data;
                    Rectangle rect = textRenderInfo.getBaseline().getBoundingRectangle();
                    yCoords.add(rect.getY());
                }
            }
        };

        PdfCanvasProcessor processor = new PdfCanvasProcessor(listener);
        processor.processPageContent(page);

        // Tính toán minY và maxY từ yCoords
        float minY = yCoords.stream().min(Float::compare).orElse(0f);
        float maxY = yCoords.stream().max(Float::compare).orElse(page.getPageSize().getHeight());

        // Padding
        float padding = 30;
        minY = Math.max(minY - padding, 0); // top-padding of the bill
        maxY = Math.min(maxY + padding, page.getPageSize().getHeight()); // bottom-padding of the bill

        // Vertical Crop Box / Media Box
        page.setMediaBox(new Rectangle(page.getMediaBox().getX(), minY, page.getMediaBox().getWidth(), maxY - minY));
        page.setCropBox(new Rectangle(page.getCropBox().getX(), minY, page.getCropBox().getWidth(), maxY - minY));

        // close all process so that Files can overwrite
        pdfDoc.close();
        reader.close();
        writer.close();

        // overwrite the old file with the new Cropped file
        Files.deleteIfExists(Paths.get(currentBillName)); // Delete old file safely
        Files.move(Paths.get(currentBillName.split("\\.")[0] + "_new.pdf"), Paths.get(currentBillName), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void printBill(ObservableList<BillItem> billItems, Bill bill) throws DocumentException, FileNotFoundException {
        // Create a new document with custom page size (width: 70mm)
        Document document = new Document();

        StringBuilder sb = new StringBuilder();
        
        // Create bills directory if it doesn't exist
        // This code will create a new directory in your disk with the path specified.
        // For example, D:\BilliardsManagement\src\main\bills
        File billsDir = new File("\\BilliardsManagement\\src\\main\\bills");
        if (!billsDir.exists()) {
            billsDir.mkdirs();
        }

        sb.append(billsDir.getAbsolutePath());
        sb.append(File.separator);
        sb.append(handleCustomerName(bill.getCustomerName()));
        sb.append("_");
        sb.append(bill.getCustomerPhone());
        sb.append("_");
        String formattedDateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH'h'mm").format(LocalDateTime.now());
        sb.append(formattedDateTime);
        sb.append("_");
        sb.append(String.valueOf(System.currentTimeMillis()).substring(8));
        sb.append(".pdf");

        // then, insert the prefix location
        String billName = sb.toString();
        System.out.println("Bill Name = " + billName);

        // assign billName to currentBillName (static) for using in showPdfBillToScreen
// set current bill name so that we can use it later
        setCurrentBillName(billName);

        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(billName));
        float widthInPoints = 70 * 2.83465f;  // Convert 70mm to points
        document.setPageSize(new com.itextpdf.text.RectangleReadOnly(widthInPoints, document.getPageSize().getHeight()));
        document.open();

        Paragraph billTitle = new Paragraph("Bida Bill");
        billTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(billTitle);

        Chunk billTimesChunk = new Chunk("Date : " + formattedDateTime);
        billTimesChunk.setFont(billTimeFont);
        Paragraph billTimesPara = new Paragraph(billTimesChunk);
        billTimesPara.setAlignment(Element.ALIGN_CENTER);
        document.add(billTimesPara);

        Font customerDetailFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);

        Chunk customerNameValueChunk = new Chunk("Customer Name: " + bill.getCustomerName() + "\n");
        customerNameValueChunk.setFont(customerDetailFont);
        // Set line height to 20% of font size, and align text fully in width
        customerNameValueChunk.setLineHeight(1.2f);
        Paragraph customerNamePara = new Paragraph(customerNameValueChunk);
        customerNamePara.setAlignment(Element.ALIGN_LEFT);
        document.add(customerNamePara);

        Chunk customerPhoneValueChunk = new Chunk("Phone: " + bill.getCustomerPhone() + "\n");
        customerPhoneValueChunk.setFont(customerDetailFont);
        // Set line height to 20% of font size, and align text fully in width
        customerPhoneValueChunk.setLineHeight(1.2f);
        Paragraph customerPhonePara = new Paragraph(customerPhoneValueChunk);
        customerPhonePara.setAlignment(Element.ALIGN_LEFT);
        document.add(customerPhonePara);

        Chunk totalPaymentValueChunk = new Chunk("Total Payment: " + Math.round(bill.getTotalCost()));
        totalPaymentValueChunk.setFont(customerDetailFont);
        // Set line height to 20% of font size, and align text fully in width
        totalPaymentValueChunk.setLineHeight(1.2f);
        Paragraph totalPaymentPara = new Paragraph(totalPaymentValueChunk);
        totalPaymentPara.setFont(customerDetailFont);
        totalPaymentPara.setAlignment(Element.ALIGN_LEFT);
        document.add(totalPaymentPara);

        // Create table for bill details
        PdfPTable table = new PdfPTable(5);

        // Set the total width of the table to 65mm (≈ 184.15 points)
        table.setTotalWidth(184.15f);
        table.setLockedWidth(true); // Ensures the table doesn't stretch
        table.setSpacingBefore(7.0f);
        table.setSpacingAfter(4.0f);

        // Set proportional column widths (e.g., all columns equal width)
        float[] columnWidths = {1.3f, 0.6f, 0.8f, 1.1f, 1.1f}; // Adjust proportions as needed
        table.setWidths(columnWidths);

        // Adding headers to the table
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);   // Bold, size 12
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);          // Regular, size 10

        String[] headers = {"Item", "QT", "Unit", "Price", "Total"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);   // Center alignment for headers
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(3.0f);
            cell.setPaddingBottom(3.0f);
            table.addCell(cell);
        }

        for (BillItem billItem : billItems) {
            // Create a new cell and set the properties you want
            PdfPCell cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(3.0f);
            cell.setPaddingBottom(3.0f);

            // Add billItem details to the cell
            cell.addElement(new Phrase(billItem.getItemName(), cellFont));
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(3.0f);
            cell.setPaddingBottom(3.0f);

            double quantity = billItem.getQuantity();
            String roundedQuantity;
            // SimpleStringProperty return the whole string "StringProperty [value: Booking]" what the fuck
            if(billItem.getItemType().contains("Booking")){
                roundedQuantity = String.format("%.1f", Math.ceil(quantity * 10) / 10.0) + "h"; // Rounds to 1 decimal place
            }
            else {
                roundedQuantity = String.valueOf((int)Math.round(quantity)); // Removes decimals for OrderItem
            }
            cell.addElement(new Phrase(roundedQuantity, cellFont));
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(3.0f);
            cell.setPaddingBottom(3.0f);
            cell.addElement(new Phrase(String.valueOf(billItem.getUnit()), cellFont));
            table.addCell(cell);
            System.out.println("Get Unit = "+billItem.getUnit());

            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(3.0f);
            cell.setPaddingBottom(3.0f);
            cell.addElement(new Phrase(String.valueOf(Math.round(billItem.getUnitPrice())), cellFont));
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(3.0f);
            cell.setPaddingBottom(3.0f);
            cell.addElement(new Phrase(String.valueOf(Math.round(billItem.getTotalPrice())), cellFont));
            table.addCell(cell);
        }

        // Add the table to the document
        document.add(table);
        // Close the document
        document.close();
    }

    public static void setCurrentBillName(String currentBillName) {
        PrintBillController.currentBillName = currentBillName;
    }
}