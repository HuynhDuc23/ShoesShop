package com.huynhduc.application.service.impl;
import com.huynhduc.application.entity.Category;
import com.huynhduc.application.entity.Product;
import com.huynhduc.application.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import java.time.ZoneId;
@Service
public class ExcelExportServiceImpl implements ExcelExportService {
    public void exportProductItems(List<Product> products, HttpServletResponse response) throws IOException {
        // Gỡ lỗi: Kiểm tra danh sách sản phẩm
        System.out.println("Bắt đầu xuất Excel, số sản phẩm: " + (products != null ? products.size() : "null"));

        // Kiểm tra phản hồi
        if (response.isCommitted()) {
            System.out.println("Phản hồi đã được commit");
            throw new IllegalStateException("Response already committed.");
        }

        // Cấu hình phản hồi
        configureResponse(response);
        System.out.println("Cấu hình phản hồi thành công");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); // Giữ 100 hàng trong bộ nhớ
             ServletOutputStream out = response.getOutputStream()) {
            System.out.println("Tạo workbook thành công");

            SXSSFSheet sheet = workbook.createSheet("Danh sách sản phẩm");
            System.out.println("Tạo sheet thành công");

            // Tạo hàng tiêu đề
            createHeaderRow(sheet, workbook);
            System.out.println("Tạo hàng tiêu đề thành công");

            // Điền dữ liệu sản phẩm
            populateProductData(sheet, products, workbook);
            System.out.println("Điền dữ liệu sản phẩm thành công");

            // Tự động điều chỉnh kích thước cột
            autoSizeColumns(sheet);
            System.out.println("Tự động điều chỉnh cột thành công");

            System.out.println("Bắt đầu ghi workbook vào output stream");
            workbook.write(out);
            System.out.println("Ghi workbook thành công");
            out.flush();
            System.out.println("Flush output stream thành công");
        } catch (Exception e) {
            System.err.println("Lỗi khi xuất Excel: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Lỗi khi xuất file Excel: " + e.getMessage(), e);
        }
    }

    private void createHeaderRow(SXSSFSheet sheet, SXSSFWorkbook workbook) {
        // Kiểm tra null
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet không được null");
        }
        if (workbook == null) {
            throw new IllegalArgumentException("Workbook không được null");
        }

        String[] columns = {"STT", "Tên sản phẩm", "Danh mục", "Giá nhập", "Giá bán", "Ngày tạo", "Ngày sửa"};
        Row header = sheet.createRow(0);
        System.out.println("Tạo hàng tiêu đề tại row 0");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        System.out.println("Tạo cell style cho tiêu đề");

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            System.out.println("Tạo ô tiêu đề: " + columns[i] + " tại cột " + i);
        }
    }

    private void populateProductData(SXSSFSheet sheet, List<Product> products, SXSSFWorkbook workbook) {
        if (products == null || products.isEmpty()) {
            System.out.println("Danh sách sản phẩm rỗng, tạo tệp Excel với chỉ tiêu đề");
            return; // Tạo tệp với chỉ tiêu đề
        }

        int rowNum = 1;
        int stt = 1;

        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

        for (Product product : products) {
            System.out.println("Xử lý sản phẩm: " + (product != null ? product.getName() : "null"));
            try {
                Row row = sheet.createRow(rowNum++);
                fillProductRow(row, product, stt++, dateCellStyle);
            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý sản phẩm tại STT " + stt + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void fillProductRow(Row row, Product product, int stt, CellStyle dateCellStyle) {
        // Kiểm tra sản phẩm null
        if (product == null) {
            System.out.println("Sản phẩm null tại STT: " + stt);
            row.createCell(0).setCellValue(stt);
            for (int i = 1; i < 7; i++) {
                row.createCell(i).setCellValue("N/A");
            }
            return;
        }

        row.createCell(0).setCellValue(stt);
        row.createCell(1).setCellValue(product.getName() != null ? product.getName() : "N/A");
        row.createCell(2).setCellValue(getCategoryName(product));
        row.createCell(3).setCellValue(product.getPrice());
        row.createCell(4).setCellValue(product.getSalePrice());

        // Xử lý ngày tạo (Timestamp)
        Cell createdAtCell = row.createCell(5);
        if (product.getCreatedAt() != null) {
            Timestamp createdAt = product.getCreatedAt();
            createdAtCell.setCellValue(new Date(createdAt.getTime()));
            createdAtCell.setCellStyle(dateCellStyle);
        } else {
            createdAtCell.setCellValue("N/A");
        }

        // Xử lý ngày sửa (Timestamp)
        Cell modifiedAtCell = row.createCell(6);
        if (product.getModifiedAt() != null) {
            Timestamp modifiedAt = product.getModifiedAt();
            modifiedAtCell.setCellValue(new Date(modifiedAt.getTime()));
            modifiedAtCell.setCellStyle(dateCellStyle);
        } else {
            modifiedAtCell.setCellValue("N/A");
        }
    }

    private String getCategoryName(Product product) {
        if (product == null || product.getCategories() == null || product.getCategories().isEmpty()) {
            return "Rỗng";
        }
        Category firstCategory = product.getCategories().get(0);
        return firstCategory != null && firstCategory.getName() != null ? firstCategory.getName() : "Rỗng";
    }

    private void autoSizeColumns(SXSSFSheet sheet) {
        int numberOfColumns = 7;
        for (int i = 0; i < numberOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void configureResponse(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "DanhSachSanPham_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }
}
