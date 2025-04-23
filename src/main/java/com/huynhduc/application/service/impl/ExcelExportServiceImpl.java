package com.huynhduc.application.service.impl;
import com.huynhduc.application.entity.Product;
import com.huynhduc.application.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
@Service
public class ExcelExportServiceImpl implements ExcelExportService {
    @Override
    public void exportProductItems(List<Product> products, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách sản phẩm");
        // Tao tieu de cot
        Row header = sheet.createRow(0);
        String[] columns = {"STT", "Tên sản phẩm", "Nhãn hiệu", "Danh mục", "Giá nhập","Giá bán","Ngày tạo","Ngày sửa"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = ((Row) header).createCell(i);
            cell.setCellValue(columns[i]);
        }

        int rowNum = 1;
        int stt = 1;
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

        for (var product : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(product.getName());
            row.createCell(2).setCellValue(product.getBrand().getName());
            row.createCell(3).setCellValue((product.getCategories().isEmpty() || product.getCategories().get(0).getName().isEmpty())
                    ? "Rỗng"
                    : product.getCategories().get(0).getName());
            row.createCell(4).setCellValue(product.getPrice());
            row.createCell(5).setCellValue(product.getSalePrice());

            Cell createdAtCell = row.createCell(6);
            createdAtCell.setCellValue(product.getCreatedAt());
            createdAtCell.setCellStyle(dateCellStyle);

            Cell modifiedAtCell = row.createCell(7);
            modifiedAtCell.setCellValue(product.getModifiedAt());
            modifiedAtCell.setCellStyle(dateCellStyle);
        }
        // Xuất file về client
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=danhsachsanpham.xlsx");

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        workbook.close();
        out.close();
    }
}
