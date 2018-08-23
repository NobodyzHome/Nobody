<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form:form modelAttribute="baseDept">
		<table>
			<tr>
				<td><form:input path="deptNo" /></td>
				<td><form:errors path="deptNo" /></td>
			</tr>
			<tr>
				<td><form:input path="deptName" /></td>
				<td><form:errors path="deptName" /></td>
			</tr>
			<tr>
				<td><form:input path="modifyDate" /></td>
				<td><form:errors path="modifyDate" /></td>
			</tr>
		</table>
	</form:form>
	<spring:eval expression="baseDept" />
</body>
</html>