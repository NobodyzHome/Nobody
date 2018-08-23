<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>文件上传</title>
</head>
<body>
	<spring:url value="/test/upload" var="action" />

	<form action="${action}" enctype="multipart/form-data" method="post">
		<input type="file" name="file1" /> <input type="file" name="file2" />
		<input type="text" name="format" value="xml" /> <input type="submit"
			value="提交" />
	</form>
</body>
</html>