package com.huynhduc.application.service;

import com.huynhduc.application.entity.Product;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface ExcelExportService {
    public void exportProductItems(List<Product> products, HttpServletResponse response) throws IOException;
}
