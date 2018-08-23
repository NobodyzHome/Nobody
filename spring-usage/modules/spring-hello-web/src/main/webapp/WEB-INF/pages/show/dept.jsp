<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<c:url value="/dept/saveOrUpdate" var="action" />
	<form:form modelAttribute="baseDept" method="post" action="${action}">
		<table>
			<tr>
				<td><spring:message code="deptNo" /></td>
				<td><form:input path="deptNo" /></td>
			</tr>
			<tr>
				<td><spring:message code="deptCode" /></td>
				<td><form:input path="deptCode" /></td>
			</tr>
			<tr>
				<td><spring:message code="deptName" /></td>
				<td><form:input path="deptName" /></td>
			</tr>
			<tr>
				<td><spring:message code="baseDept.modifyDate" /></td>
				<td><form:input path="modifyDate" /></td>
			</tr>
			<tr>
				<td><spring:message code="baseDept.isRun" /></td>
				<td><form:input path="isRun" /></td>
			</tr>
			<tr>
				<td rowspan="2"><input type="submit"
					value="<spring:message code="submit"/>"></td>
			</tr>
		</table>
	</form:form>
</body>
</html>