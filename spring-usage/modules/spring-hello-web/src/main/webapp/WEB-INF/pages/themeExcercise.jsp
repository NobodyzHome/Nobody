<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

<spring:theme code="script.prompt" var="scriptPath" />
<spring:url value="${scriptPath}" var="scriptContextPath" />
<script type="text/javascript" src="${scriptContextPath}"></script>

<spring:theme code="style.div.font" var="stylePath" />
<spring:url value="${stylePath}" var="styleContextPath" />
<link rel="stylesheet" type="text/css" href="${styleContextPath}" />
</head>
<body>
	<a href="${requestScope.requestURI}?theme=happy">开心</a>
	<a href="${requestScope.requestURI}?theme=sad">悲伤</a>
	<a href="${requestScope.requestURI}?locale=zh_CN">中文</a>
	<a href="${requestScope.requestURI}?locale=en_GB">英文</a>

	<spring:eval
		expression="new java.lang.Object[]{new java.util.Date(),5300}"
		var="args" />
	<div>
		<spring:theme code="contents.welcome" arguments="${args}" />
	</div>

	<spring:theme code="image.bottom" var="imgPath" />
	<spring:url value="${imgPath}" var="imgContextPath" />
	<img src="${imgContextPath}" />
</body>
</html>