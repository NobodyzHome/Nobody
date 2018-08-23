<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Has Theme</title>
</head>
<body>
	<spring:theme code="mainPage.image.bottom" var="imgPath" />
	<spring:url value="${imgPath}" var="imgContextPath" />
	<img alt="test" src="${imgContextPath}" />
</body>
</html>