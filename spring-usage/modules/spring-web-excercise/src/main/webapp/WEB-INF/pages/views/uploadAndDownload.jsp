<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<spring:url scope="request" value="${path}" var="imgPath" />
	<spring:url scope="request" value="/dispatcher/dept/uploadAndDownload"
		var="action" />

	<form action="${action}" method="post" enctype="multipart/form-data">
		<input name="file1" type="file" /> <input type="submit" value="提交" />
	</form>

	<img src="${imgPath}" />
</body>
</html>