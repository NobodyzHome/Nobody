package com.spring.web.view;

import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

public class ExcelView extends AbstractXlsView {

	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Sheet sheet = workbook.createSheet("test");
		Row columnNameRow = sheet.createRow(0);
		Row columnValueRow = sheet.createRow(1);
		int index = 0;

		for (Map.Entry<String, Object> entry : model.entrySet()) {
			columnNameRow.createCell(index).setCellValue(entry.getKey());
			columnValueRow.createCell(index++)
					.setCellValue(Objects.isNull(entry.getValue()) ? "无" : entry.getValue().toString());
		}
	}

}
