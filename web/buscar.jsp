<%-- 
    Document   : buscar
    Created on : 14-feb-2019, 18:23:22
    Author     : Gerardo
--%>

<%@page import="java.io.File"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <ul>
        <%
            String ruta=application.getRealPath("/")+"archivos"+File.separator;
            File [] archivos;
            File carpeta=new File(ruta);
            archivos=carpeta.listFiles();
            if (archivos != null) {
            for (int i=0; i<archivos.length; i++) {
                File fichero=archivos[i];
                
                %>
                <%=fichero.getAbsoluteFile()%>
                <br/>
                <%=fichero.getAbsolutePath()%>
                <br/>
                <%=fichero.getCanonicalFile()%>
                <br/>
                <%=fichero.getCanonicalPath()%>
                <br/>
                <li>
                    <a href="editortexto?accion=abrir&archivo=<%=fichero.getName()%>">
                        <%=fichero.getName()%>
                    </a>
                </li>
                <%
            }
            }
        %>
        <%=ruta%>
        </ul>
    </body>
</html>
