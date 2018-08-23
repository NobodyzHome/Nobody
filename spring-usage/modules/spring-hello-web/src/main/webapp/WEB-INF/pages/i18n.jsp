<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>${requestContext.locale}</title>
</head>
<body>
	${jay}
	<br />${selina}
	<br />${hello}
	<br /> ${dept}
	<br />
	<spring:eval expression="dept.deptNo" />
	<br />
	<spring:eval expression="dept.modifyDate" />
	<br />【requestContext.requestUri】 is: ${requestContext.requestUri}
	<br />【requestContext.queryString】 is: ${requestContext.queryString}
	<br />
	【requestContext.getContextUrl('/hello/{lala}/{user}/web',{'user':'zhangsan','lala':'happy'})】
	is:
	<spring:eval
		expression="requestContext.getContextUrl('/hello/{lala}/{user}/web',{'user':'zhangsa','lala':'happy'})" />
</body>
</html>