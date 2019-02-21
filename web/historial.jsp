<%-- any content can be specified here e.g.: --%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@ page pageEncoding="UTF-8" %>
<%

    String archivo;
    ArrayList<String> historial = (ArrayList<String>) application.getAttribute("historial");
    Iterator iterador;

    iterador = historial.iterator();
    while (iterador.hasNext()) {
        archivo = (String) iterador.next();
        %>
        <option><%=archivo%></option>
        <%
    }
%>


