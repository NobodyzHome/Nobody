<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%--
  Created by IntelliJ IDEA.
  User: maziqiang
  Date: 2018/5/18
  Time: 12:29
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<spring:url var="scripts" value="/scripts"/>
<html>
<head>
    <title>hello world</title>
    <script type="text/javascript" src="<spring:eval expression="scripts"/>/jquery/jquery.js"></script>
    <script type="text/javascript">
        $(
            function () {
                $("#submit").click(
                    function () {
                        var param = {
                            id: 59,
                            name: "张三",
                            sex: "男",
                            birthDay: "2018-10-11 20:50:33",
                            interest: ["唱歌", "跳舞", "画画"],
                            firend: "小明"
                        };

                        var json = JSON.stringify(param);

                        $.ajax("/basic/direct", {
                            method: "POST",
                            async: true,
                            contentType: "application/json",
                            data: json,
                            success: function (data) {
                                alert(data);
                            },
                            error: function () {
                                alert("error!");
                            }
                        });

                    }
                );
            }
        )
    </script>
</head>
<body>
welcome
<input type="button" id="submit" value="提交"/>
</body>
</html>
