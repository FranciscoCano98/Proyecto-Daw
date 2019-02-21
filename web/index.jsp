<%-- 
    Document   : index
    Created on : 06-feb-2019, 21:24:27
    Author     : franc
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String texto = request.getParameter("texto");
    if (texto == null) {
        texto = "";
    }

    String mensaje = (String) session.getAttribute("error");
    if (mensaje == null) {
        mensaje = "";
    } else {
        session.removeAttribute("error");
    }
    
    String usuario=(String) session.getAttribute("usuario");


%>
<!DOCTYPE html>

<% if (usuario == null ) { 
    %>
    <%--Si no hay usuario identificado redireccionamos a la página de acceso--%>
    <jsp:forward page="acceso.jsp"></jsp:forward>
    <%
}

%>
 
    
    

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Editor de Texto</title>
        <script src="js/jquery331.js" type="text/javascript"></script>
        <script src="js/jqueryui112/jquery-ui.js" type="text/javascript"></script>
        <script src="js/editortexto.js" type="text/javascript"></script>
    </head>
    <body>
        <%=mensaje%>
        <form action="editortexto" method="post" enctype="multipart/form-data">
            <input type="submit" value="Buscar" name="accion"/>
            <input type="button" value="Busqueda" id="busqueda"/>
            <input type="submit" value="Imprimir" name="accion"/>            
            <input type="file" name="fichero"/>
            <input type="submit" value="Subir" name="accion"/>            
            <div>
                <textarea rows="10" cols="80" name="texto"><%=texto%></textarea>
            </div>
            <input type="text" name="archivo"/>
            <input type="submit" value="Abrir" name="accion"/>
            <input type="submit" value="Guardar" name="accion"/>            
            <input type="submit" value="Guardar como pdf" name="accion"/>            
            <input type="submit" value="Crear carpeta" name="accion"/>            
            <select name="historial">     
                <%-- Se incluye una página jsp precompilada --%>
                <jsp:include page="historial.jsp"></jsp:include>
                <%-- Se incluye el código --%>
                <%@ include file="historial.jspf" %>
            </select>
            <input type="submit" value="Cargar" name="accion"/>            
        </form>
        <div id="dialogoBusqueda">
            <ul>
            </ul>
        </div>
    </body>
</html>
