<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

<spring:theme code="script.hello" arguments="${prefix}" var="scriptPath" />
<spring:url value="${scriptPath}" var="scriptContextPath" />
<script type="text/javascript" src="${scriptContextPath}"></script>

<spring:theme code="style.contents" arguments="${prefix}"
	var="stylePath" />
<spring:url value="${stylePath}" var="styleContextPath" />
<link rel="stylesheet" type="text/css" href="${styleContextPath}" />
</head>
<body>

	<a
		href="<spring:url value="${prefix}/theme/excercise.htm?theme=summer&view=themeExcercise" />">夏天</a>
	<a
		href="<spring:url value="${prefix}/theme/excercise.htm?theme=winter&view=themeExcercise" />">冬天</a>
	<a
		href="<spring:url value="${prefix}/theme/excercise.htm?view=themeExcercise&locale=zh_CN"/>">中文</a>
	<a
		href="<spring:url value="${prefix}/theme/excercise.htm?view=themeExcercise&locale=en_GB" />">英文</a>
	<br />

	<div>
		<p>
			<spring:theme code="mainPage.contents" />
		</p>
	</div>

	<spring:theme code="image.bottom" arguments="${prefix}" var="imgPath" />
	<spring:url value="${imgPath}" var="imgContextPath" />
	<spring:eval
		expression="T(org.springframework.util.StringUtils).getFilename('${imgPath}')"
		var="imgFileName" />
	<img alt="${imgFileName}" src="${imgContextPath}" />
</body>
</html>