<%-- 
    Document   : pregledKorisnika
    Created on : Jun 1, 2017, 10:12:28 PM
    Author     : Lovro
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link type="text/css" rel="stylesheet" 
              href="${pageContext.servletContext.contextPath}/css/displaytag.css" />
    </head>
    <body>
        <sql:setDataSource 
            driver="${applicationScope.BP_Konfig.driverDatabase}"
            url="${applicationScope.BP_Konfig.serverDatabase}${applicationScope.BP_Konfig.userDatabase}" 
            user="${applicationScope.BP_Konfig.userUsername}" 
            password="${applicationScope.BP_Konfig.userPassword}" 
            var="vezaBP" />

        <sql:query dataSource="${vezaBP}" var="polaznici" >
            SELECT * FROM korisnik
        </sql:query>

        <display:table name="${polaznici.rows}" pagesize="5">
            <display:column title="ID" property="id" />
            <display:column title="Korisničko ime" property="username" />
            <display:column title="Prezime" property="prezime" />
            <display:column title="email" property="email" />
        </display:table>
    </body>
</html>
