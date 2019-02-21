<%-- 
    Document   : acceso
    Created on : 14-feb-2019, 10:12:42
    Author     : franc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Editor de texto</title>
        <style type="text/css">
            div {
                margin:10px;
            }
        </style>
    </head>
    <body>
        <p>
        <%
        String mensaje;
        mensaje=(String) session.getAttribute("error");
        if (mensaje != null) {
            out.print(mensaje);
            session.removeAttribute("error");
        }
        %>
        </p>
        <form action="editortexto" method="post" enctype="multipart/form-data">
            <input type="text" name="login" placeholder="login"/>
            <input type="text" name="password" placeholder="password"/>
            <input type="submit" name="accion" value="Acceder"/>
            <input type="submit" name="accion" value="Registrar"/>
            <div>
                <input type="file" name="fichero"/>
                <input type="submit" name="accion" value="Importar como xml"/>
                <input type="submit" name="accion" value="Importar como json"/>
                <input type="submit" name="accion" value="Exportar como xml"/>
                <input type="submit" name="accion" value="Exportar como json"/>
            </div>
        </form>
    </body>
</html>
