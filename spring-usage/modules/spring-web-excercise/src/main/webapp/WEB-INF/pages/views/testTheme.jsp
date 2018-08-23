<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<spring:theme code="script.hello" arguments="/dispatcher" var="jsPath" />
<spring:url value="${jsPath}" var="jsContextPath" />
<script type="text/javascript" src="${jsContextPath}"></script>

<style type="text/css"></style>

<spring:theme code="style.main" arguments="/dispatcher" var="stylePath" />
<spring:url value="${stylePath}" var="styleContextPath"></spring:url>

<link rel="stylesheet" type="text/css" href="${styleContextPath}" />

</head>
<body>

	<a
		href="<spring:url value="/dispatcher/views/testTheme.htm?theme=happy"/>">开心</a>
	<a
		href="<spring:url value="/dispatcher/views/testTheme.htm?theme=sad"/>">悲伤</a>

	<a
		href="<spring:url value="/dispatcher/views/testTheme.htm?locale=zh_CN" />">中文</a>
	<a
		href="<spring:url value="/dispatcher/views/testTheme.htm?locale=en_GB"/>">英文</a>

	<div>
		<p>
			<spring:message code="Length.lineName" arguments="线路名称,20,5" />
		</p>
	</div>

	<spring:theme code="image.title" arguments="/dispatcher" var="imgPath" />
	<spring:url value="${imgPath}" var="imgContextPath" />
	<br />
	<img src="${imgContextPath}" />
</body>
</html>