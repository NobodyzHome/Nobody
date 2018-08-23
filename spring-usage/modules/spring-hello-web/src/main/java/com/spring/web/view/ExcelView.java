package com.spring.web.view;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.view.document.AbstractXlsView;

public class ExcelView extends AbstractXlsView {

	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Sheet sheet = workbook.createSheet("测试");
		Row titleRow = sheet.createRow(0);
		Row valueRow = sheet.createRow(1);
		AtomicInteger index = new AtomicInteger(0);

		model.forEach((key, value) -> {
			Cell titleCell = titleRow.createCell(index.get());
			Cell valueCell = valueRow.createCell(index.getAndAdd(1));

			titleCell.setCellValue(key);
			valueCell.setCellValue(ObjectUtils.nullSafeToString(value));
		});

	}

}
