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

	<form
		action="<spring:url value="${urlPrefix}/multipart/excercise/${action}.htm"/>"
		method="post" enctype="multipart/form-data">
		<input type="file" name="fileA" /> <input type="file" name="fileB" />
		<input type="file" name="fileC" /> <input type="submit" value="提交" />
	</form>

	<img src="<spring:url value="${img1}"/>" />
	<img src="<spring:url value="${img2}"/>" />
	<img src="<spring:url value="${img3}"/>" />
</body>
</html>