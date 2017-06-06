<%-- 
    Document   : login
    Created on : Jun 1, 2017, 8:38:18 PM
    Author     : Lovro
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Prijava</h1>
        <a href="${pageContext.servletContext.contextPath}/pages/index.jsp">Index</a> <br> <br>
        <!-- <form action="login.jsp" method="POST"> -->
        <form action="j_security_check" method="POST">
            <b>Korisničko ime:</b> <input type="text" name="j_username"> <br/> <br/>
            <b>Lozinka:</b> <input type="password" name="j_password"> <br/> <br/>
            <input type="submit" value="Prijavite se"> 
        </form>
    </body>
</html>
