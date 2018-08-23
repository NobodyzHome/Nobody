<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<c:url value="/line/saveOrUpdate" var="action" />
	<form:form action="${action}" method="post" modelAttribute="baseLine">
		<table>
			<tr>
				<td>线路编号</td>
				<td><form:input path="lineNo" /></td>
			</tr>
			<tr>
				<td>线路代码</td>
				<td><form:input path="lineCode" /></td>
			</tr>
			<tr>
				<td>线路名称</td>
				<td><form:input path="lineName" /></td>
			</tr>
			<tr>
				<td>是否运营</td>
				<td><form:input path="isRun" /></td>
			</tr>
			<tr>
				<td>修改日期</td>
				<td><form:input path="modifyDate" /></td>
			</tr>
			<tr>
				<td rowspan="2"><input type="submit" value="提交" /></td>
			</tr>
		</table>
	</form:form>
</body>
</html>