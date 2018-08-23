<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<spring:eval
		expression="requestContext.getContextUrl('/test/nancy/validation/modelAttribute')"
		var="formAction" />
	<form:form modelAttribute="baseDept" action="${formAction}"
		method="post">
		<table>
			<tr>
				<td><spring:message code="deptNo" /></td>
				<td><form:input path="deptNo" /></td>
				<td><form:errors path="deptNo" cssStyle="color:red" /></td>
			</tr>
			<tr>
				<td><spring:message code="deptCode" /></td>
				<td><form:input path="deptCode" /></td>
				<td><form:errors path="deptCode" cssStyle="color:red" /></td>
			</tr>
			<tr>
				<td><spring:message code="deptName" /></td>
				<td><form:input path="deptName" /></td>
				<td><form:errors path="deptName" cssStyle="color:red" /></td>
			</tr>
			<tr>
				<td><spring:message code="baseDept.modifyDate" /></td>
				<td><form:input path="modifyDate" /></td>
				<td><form:errors path="modifyDate" cssStyle="color:red" /></td>
			</tr>
			<tr>
				<td><spring:message code="baseDept.isRun" /></td>
				<td><form:input path="isRun" /></td>
				<td><form:errors path="isRun" cssStyle="color:red" /></td>
			</tr>
			<tr>
				<td colspan="3"><input type="submit"
					value="<spring:message code="submit" />" /></td>
			</tr>
		</table>
	</form:form>

	<spring:hasBindErrors name="baseDept">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message message="${error}" /> 拒绝的值：${error.rejectedValue}
			<br />
		</c:forEach>
	</spring:hasBindErrors>
</body>
</html>