package src.billiardsmanagement.controller.orders;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.collections.ObservableList;
import src.billiardsmanagement.model.Bill;
import src.billiardsmanagement.model.BillItem;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;

public class PrintBillController {
    private static Font billTimeFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);

    public static void printBill(ObservableList<BillItem> billItems, Bill bill) throws DocumentException, FileNotFoundException {
        // Create a new document with custom page size (width: 70mm)
        String billName = bill.getCustomerName() + "_" + bill.getCustomerPhone() + "_" + System.currentTimeMillis();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("bills/" + billName + ".pdf"));
        float widthInPoints = 70 * 2.83465f;  // Convert 70mm to points
        document.setPageSize(new com.itextpdf.text.RectangleReadOnly(widthInPoints, document.getPageSize().getHeight()));
        document.open();

        Paragraph billTitle = new Paragraph("Bida Bill");
        billTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(billTitle);

        Chunk billTimesChunk = new Chunk("Date : " + LocalDate.now());
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

        Chunk totalPaymentValueChunk = new Chunk("Total Payment: " + bill.getTotalCost());
        totalPaymentValueChunk.setFont(customerDetailFont);
        // Set line height to 20% of font size, and align text fully in width
        totalPaymentValueChunk.setLineHeight(1.2f);
        Paragraph totalPaymentPara = new Paragraph(totalPaymentValueChunk);
        totalPaymentPara.setFont(customerDetailFont);
        totalPaymentPara.setAlignment(Element.ALIGN_LEFT);
        document.add(totalPaymentPara);

        // Create table for bill details
        PdfPTable table = new PdfPTable(4);

        // Set the total width of the table to 65mm (≈ 184.15 points)
        table.setTotalWidth(184.15f);
        table.setLockedWidth(true); // Ensures the table doesn't stretch
        table.setSpacingBefore(7.0f);
        table.setSpacingAfter(4.0f);

        // Set proportional column widths (e.g., all columns equal width)
        float[] columnWidths = {1.5f, 0.5f, 1.0f, 1.0f}; // Adjust proportions as needed
        table.setWidths(columnWidths);

        // Adding headers to the table
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);   // Bold, size 12
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);          // Regular, size 10

        String[] headers = {"Item Name", "QT", "Unit Price", "Total Price"};
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
            cell.addElement(new Phrase(String.valueOf(billItem.getQuantity()), cellFont));
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(3.0f);
            cell.setPaddingBottom(3.0f);
            cell.addElement(new Phrase(String.valueOf(billItem.getUnitPrice()), cellFont));
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(3.0f);
            cell.setPaddingBottom(3.0f);
            cell.addElement(new Phrase(String.valueOf(billItem.getTotalPrice()), cellFont));
            table.addCell(cell);
        }

        // Add the table to the document
        document.add(table);
        // Close the document
        document.close();
        }
    }