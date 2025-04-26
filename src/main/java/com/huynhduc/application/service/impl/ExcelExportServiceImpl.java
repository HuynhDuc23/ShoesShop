package com.huynhduc.application.service.impl;
import com.huynhduc.application.entity.Category;
import com.huynhduc.application.entity.Product;
import com.huynhduc.application.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
@Service
public class ExcelExportServiceImpl implements ExcelExportService {
    public void exportProductItems(List<Product> products, HttpServletResponse response) throws IOException {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm rỗng");
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream out = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách sản phẩm");

            // Create header row
            createHeaderRow(sheet, workbook);

            // Populate product data
            populateProductData(sheet, products, workbook);

            // Auto-size columns
            autoSizeColumns(sheet);

            // Set response headers and write output
            configureResponse(response);
            workbook.write(out);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất file Excel: " + e.getMessage(), e);
        }
    }

    private void createHeaderRow(Sheet sheet, XSSFWorkbook workbook) {
        String[] columns = {"STT", "Tên sản phẩm", "Danh mục", "Giá nhập", "Giá bán", "Ngày tạo", "Ngày sửa"};
        Row header = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void populateProductData(Sheet sheet, List<Product> products, XSSFWorkbook workbook) {
        int rowNum = 1;
        int stt = 1;

        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            fillProductRow(row, product, stt++, dateCellStyle);
        }
    }

    private void fillProductRow(Row row, Product product, int stt, CellStyle dateCellStyle) {
        row.createCell(0).setCellValue(stt);
        row.createCell(1).setCellValue(getSafeString(product.getName(), "N/A"));
        row.createCell(2).setCellValue(getCategoryName(product));
        row.createCell(3).setCellValue(getSafeLong(product.getPrice(), 0L));
        row.createCell(4).setCellValue(getSafeLong(product.getSalePrice(), 0L));

        Cell createdAtCell = row.createCell(5);
        createdAtCell.setCellValue(product.getCreatedAt());
        createdAtCell.setCellStyle(dateCellStyle);

        Cell modifiedAtCell = row.createCell(6);
        modifiedAtCell.setCellValue(product.getModifiedAt());
        modifiedAtCell.setCellStyle(dateCellStyle);
    }

    private String getCategoryName(Product product) {
        if (product.getCategories() == null || product.getCategories().isEmpty()) {
            return "Rỗng";
        }
        Category firstCategory = product.getCategories().get(0);
        return firstCategory != null && ((Category) firstCategory).getName() != null ? firstCategory.getName() : "Rỗng";
    }

    private String getSafeString(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    private long getSafeLong(Long value, long defaultValue) {
        return value != null ? value : defaultValue;
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 7; i++) { // 7 columns
            sheet.autoSizeColumn(i);
        }
    }

    private void configureResponse(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "DanhSachSanPham_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
    }
}
