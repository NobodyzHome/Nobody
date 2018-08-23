<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action="<c:url value="/test/nancy/${action}"/>" method="post"
		enctype="multipart/form-data">
		<input name="file1" type="file" /> <input name="file2" type="file" />
		<input name="file3" type="file" /> <input name="file4" type="file" />
		<input type="submit" value="提交">
	</form>

	<img src="<c:url value="${img1}"/>">
	<img src="<c:url value="${img2}"/>">
	<img src="<c:url value="${img3}"/>">
	<img src="<c:url value="${img4}"/>">
</body>
</html>