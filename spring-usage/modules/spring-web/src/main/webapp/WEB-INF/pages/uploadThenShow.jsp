<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>文件上传并显示</title>
</head>
<body>
	<spring:url value="/test/uploadThenShow" var="action" />
	<form action="${action}" enctype="multipart/form-data" method="post">
		<input type="file" name="file1" /> <input type="file" name="file2" />
		<input type="submit" value="提交" />
	</form>

	<img alt="test1" src="${img1}" />
	<img alt="test2" src="${img2}" />
</body>
</html>