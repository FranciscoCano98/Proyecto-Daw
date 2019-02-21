/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Gerardo
 */
/* Localizacion por defecto
 Location = C:\Usuarios\..usuario..\AppData\Roaming\NetBeans\8.0.2\config\GF_4.1\domain1\generated\jsp\EditorTextoJSP
*/

@MultipartConfig(location = "C:\\Users\\Gerardo\\Documents\\NetBeansProjects\\EditorTextoJSP\\build\\web\\archivos",
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)

public class editortexto extends HttpServlet {
    
    //ruta donde se alojan los archivos del usuario.
    private String ruta;
    //coleccopn tipo diccionario que contiene los nombres y clave de los usuarios.
    //se carga desde el fichero usuarios.ini
    private Properties usuarios; 
    /*Constante que contiene los nombre y claves de usuario*/
    private final String ficheroUsuarios = "usuarios.ini";
    /*coleccion para ir almacenando los nombres de los archivos que vamos 
    abriendo y cerrando*/
    ArrayList<String> historial;

    @Override
    public void init() {
        /*
        Se llama una sola vez, con la primera peticion, ideal para inicializar 
        los recursos que se vayan a utilizar por parte de todos los usuarios a 
        lo largo de toda la vida de la aplicacion
        */
        
        //Creamos la coleccion para el historial
        historial = new ArrayList<>();
        /*Lo guardamos a nivel de aplicacion para que este disponible
        en todas las paginas*/
        this.getServletContext().setAttribute("historial", historial);
        /*Establecemos la ruta a los archivos de la aplicacion*/
        ruta = this.getServletContext().getRealPath("/") + "archivos" + File.separator;
        /*La coleccion con los nombres y claves de usuario que se carga desde
        un fichero*/
        usuarios = new Properties();
        try {
            /*Que se carga desde un fichero*/
            usuarios.load(new FileReader(ruta + ficheroUsuarios));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(editortexto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(editortexto.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws com.itextpdf.text.DocumentException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, DocumentException {

        HttpSession sesion;
        String url = null;
        String texto = null;
        String archivo = null;
        boolean ok;
        
        /*Recojo la sesion para almacenar mensajes en su mayor parte*/
        sesion = request.getSession();
        /*Recogemos la accion*/
        String accion = request.getParameter("accion");
        accion = accion.toLowerCase().replaceAll(" ", "");
        switch (accion) {
            case "abrir":
                /*Recogemos el nombre del archivo*/
                archivo = request.getParameter("archivo");
                try {
                    texto = abrir(archivo);
                    /*Almacenamos el último archivo abierto*/
                    sesion.setAttribute("archivoActual", archivo);
                } catch (FileNotFoundException ex) {
                    sesion = request.getSession();
                    sesion.setAttribute("error", "No se encuentra el archivo");
                }
                url = "index.jsp?texto=" + texto;
                break;
            case "guardar":
                /*Recogemos el nombre del archivo y el texto*/
                archivo = request.getParameter("archivo");
                texto = request.getParameter("texto");
                //y se guarda
                guardar(texto, archivo);
                sesion.setAttribute("archivoActual", archivo);
                url = "index.jsp";
                break;
            case "cargar":
                /*Cargamos un archivo desde la lista de historia
                algo parecido a una lista de recientes*/
                archivo = request.getParameter("historial");
                try {
                    //Y lo abrimos
                    texto = abrir(archivo);
                    url = "index.jsp?texto=" + texto;
                    sesion.setAttribute("archivoActual", archivo);
                } catch (FileNotFoundException ex) {
                    sesion = request.getSession();
                    sesion.setAttribute("error", "No se encuentra el archivo");
                    url = "index.jsp";
                }

                break;
            case "acceder":
                //Identificarse en el sistema
                ok = acceder(request);
                if (ok) {
                    url = "index.jsp";
                    /*Guardamos el nombre de usuario que ha accedido al 
                    sistema*/
                    sesion.setAttribute("usuario", request.getParameter("login"));
                    sesion.setAttribute("archivoActual", "nuevo.txt");
                } else {
                    /*En caso contrario, volvemos a la misma página y 
                    preparamos el mensaje a través de la sesión*/
                    sesion.setAttribute("error", "Usuario incorrecto");
                    url = "acceso.jsp";
                }
                break;
            case "registrar":
                /*Comprobamos si ha tenido éxito o no*/
                ok = registrar(request);
                if (ok) {
                    sesion.setAttribute("error", "El usuario se ha registrado correctamente");
                } else {
                    sesion.setAttribute("error", "Ya existe un usuario con el mimso nombre");
                }
                url = "acceso.jsp";
                break;
            case "buscar":
                /*Abre una nueva página desde donde seleccionar un archivo para 
                abrir una vez que se ha listado el directorio "archivos"*/
                url = "buscar.jsp";
                break;
            case "imprimir":
                /*Genera un pdf que es enviado al cliente*/
                imprimir(request, response);
                break;
            case "subir":
                /*Sube un archivo desde el cliente al directorio "archivos"*/
                ok = upload(request);
                url = "index.jsp";
                if (!ok) {
                    sesion.setAttribute("error", "No ha sido posible subir el archivo");
                }
                break;
            case "guardarcomopdf":
                /*Igual que imprimir pero dejando el pdf en un fichero*/
                url = "index.jsp";
                guardarComoPdf(request, response);
                break;
            case "crearcarpeta":
                /*Crea una carpeta en el directorio "archivos"*/
                crearCarpeta(request, response);
                url = "index.jsp";
                break;
            case "importarcomoxml":
                /*Recibe un xml con usuarios que los agrega al fichero
                usuarios.ini*/
                ok = importarcomoxml(request, response);
                url = "acceso.jsp";
                if (!ok) {
                    sesion.setAttribute("error", "No ha sido posible realizar la importación");
                }
                break;
            case "exportarcomoxml":
                /*Envia al cliente un xml con todos los usuarios*/
                exportarcomoXml(request, response);
                return;

        }
        
        /*Algunas opciones no vuelven a una página, sino que devuelven un 
        fichero: xml, pdf, etc ... Por ello se filtra esa condición*/
        if (url != null) {
            // Preparamos el tipo de respuesta
            // response.setContentType("text/html;charset=UTF-8");
            // Redireccionamos a la url que corresnponda
            //  response.sendRedirect(url);
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/" + url);
            rd.forward(request, response);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (DocumentException ex) {
            Logger.getLogger(editortexto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (DocumentException ex) {
            Logger.getLogger(editortexto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String abrir(String archivo) throws IOException {

        BufferedReader fichero;
        fichero = new BufferedReader(new FileReader(ruta + archivo));
        String texto = fichero.readLine();
        while (fichero.ready()) {
            texto = texto + fichero.readLine();
        }
        fichero.close();
        historial.add(archivo);
        return texto;
    }

    private void guardar(String texto, String archivo)
            throws FileNotFoundException {

        PrintWriter fichero;
        fichero = new PrintWriter(ruta + archivo);
        fichero.write(texto);
        fichero.flush();
        fichero.close();
        historial.add(archivo);
    }

    private boolean acceder(HttpServletRequest request){
        boolean ok = true;
        String login, password, clave;

        login = request.getParameter("login");
        password = request.getParameter("password");
        /*Obtenemos la clave asociada al login*/
        clave = usuarios.getProperty(login);
        /*Si existe el login o las claves no coinciden ==> Error*/
        if (clave == null || !clave.equals(password)) {
            ok = false;
        }
        return ok;
    }

    private boolean registrar(HttpServletRequest request) throws IOException{
        boolean ok = false;
        String login, password, clave;
        
        /*Recogemos los parámetros*/
        login = request.getParameter("login");
        password = request.getParameter("password");
        clave = usuarios.getProperty(login);
        if (clave == null) {
            /*Creamos la entrada*/
            usuarios.setProperty(login, password);
            /*Y almacenamos como fichero de texto*/
            usuarios.store(new FileWriter(ruta + ficheroUsuarios), "");
            //y como fichero xml
            usuarios.storeToXML(new FileOutputStream(ruta + ficheroUsuarios + ".xml"), "");
            ok = true;
        }
        return ok;
    }

    private boolean imprimir(HttpServletRequest request, HttpServletResponse response) throws IOException {

        boolean ok = true;
        String nombreArchivo;

        nombreArchivo = (String) request.getSession().getAttribute("archivoActual") + ".pdf";
        // Establecemos el tipo de respuesta
        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment;filename=" + nombreArchivo);
        /* Creamos un objeto documento con dimensiones A4 sobre 
        el que iremos añadiendo el resto de elementos */
        Document document = new Document(PageSize.A4);
        // Creamos un buffer temporal 
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            /* Creamos un escritor que interactuará con el 
            documento a través del buffer. 
            El buffer es el destino, o sea, que para grabarlo en un fichero
            debería ser un objeto FileOutputStream */
            PdfWriter.getInstance(document, buffer);
            generarPDF(document, request);
            // Enviamos el documento como bytes por la salida de la respuesta
            DataOutput output = new DataOutputStream(response.getOutputStream());
            byte[] bytes = buffer.toByteArray();
            response.setContentLength(bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                output.writeByte(bytes[i]);
            }

        } catch (DocumentException ex) {
            Logger.getLogger(editortexto.class.getName()).log(Level.SEVERE, null, ex);
            ok = false;
        }

        return ok;
    }

    private PdfPCell crearCelda(String texto, BaseColor color) {

        Paragraph parrafo = new Paragraph();
        parrafo.setFont(new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, color));
        parrafo.add(texto);
        parrafo.setAlignment(Paragraph.ALIGN_CENTER);
        parrafo.setSpacingAfter(4);
        // Creamos una celda
        PdfPCell celda = new PdfPCell(parrafo);
        celda.setHorizontalAlignment(PdfPCell.ALIGN_JUSTIFIED);
        celda.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        // Añadimos el párafo a la celda
        celda.addElement(parrafo);
        return celda;
    }

    private boolean upload(HttpServletRequest request) throws ServletException {

        boolean ok = true;

        HttpSession sesion = request.getSession();
        try {
            Part fichero = request.getPart("fichero");
            fichero.write(fichero.getSubmittedFileName());
            sesion.setAttribute("error", "Nombre:" + fichero.getName() + "Fichero:" + fichero.getSubmittedFileName());
        } catch (IOException ex) {
            ok = false;
            sesion.setAttribute("error", ex.getMessage());
        }
        return ok;
    }

    private boolean guardarComoPdf(HttpServletRequest request, HttpServletResponse response) throws DocumentException {

        boolean ok = true;
        String path;

        path = ruta + request.getParameter("archivo") + ".pdf";
        // Establecemos el tipo de respuesta
        response.setContentType("application/pdf");
        /* Creamos un objeto documento con dimensiones A4 sobre 
        el que iremos añadiendo el resto de elementos */
        Document document = new Document(PageSize.A4);

        try {
            /* Creamos un escritor que interactuará con el
            documento a través del buffer.
            El buffer es el destino, o sea, que para grabarlo en un fichero
            debería ser un objeto FileOutputStream */
            PdfWriter.getInstance(document, new FileOutputStream(path));
            generarPDF(document, request);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(editortexto.class.getName()).log(Level.SEVERE, null, ex);
            ok = false;
        }
        return ok;
    }

    private void generarPDF(Document document, HttpServletRequest request) throws DocumentException {

        // Abrimos el documento
        document.open();

        // Creamos una tabla de tres columnas iguales
        PdfPTable tabla = new PdfPTable(3);
        tabla.setTotalWidth(new float[]{90, 90, 90});
        tabla.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);

        // LA CABECERA DE LA TABLA
        Paragraph parrafo = new Paragraph();
        parrafo.setFont(new Font(FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(0, 0, 255)));
        parrafo.setAlignment(Paragraph.ALIGN_CENTER);
    parrafo.add("Ejemplo de generación de un documento PDF");
        parrafo.setSpacingAfter(8);
        
        // Creamos una celda
        PdfPCell celda = new PdfPCell(parrafo);
        celda.setColspan(3);
        celda.setHorizontalAlignment(PdfPCell.ALIGN_JUSTIFIED);
        celda.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        // Añadimos el párafo a la celda
        celda.addElement(parrafo);
        // Añadimos la  celda a la tabla
        tabla.addCell(celda);

        // Añadimos las cabeceras de las columnas 
        celda = crearCelda("Autor", new BaseColor(255, 0, 0));
        tabla.addCell(celda);

        celda = crearCelda("Fecha", new BaseColor(255, 0, 0));
        tabla.addCell(celda);

        celda = crearCelda("Archivo", new BaseColor(255, 0, 0));
        tabla.addCell(celda);

        // Añadimos la información relativa a cada cabecera
        HttpSession sesion = request.getSession();
        String nombreUsuario = (String) sesion.getAttribute("usuario");
        celda = crearCelda(nombreUsuario, new BaseColor(0, 255, 0));
        tabla.addCell(celda);

        Date fecha = new Date();
        celda = crearCelda(fecha.toString(), new BaseColor(0, 255, 0));
        tabla.addCell(celda);

        String nombreArchivo = (String) sesion.getAttribute("archivoActual");
        celda = crearCelda(nombreArchivo, new BaseColor(0, 255, 0));
        tabla.addCell(celda);

        // Finalmente el archivo de texto
        String texto = request.getParameter("texto");
        parrafo = new Paragraph();
        parrafo.setFont(new Font(FontFamily.HELVETICA, 14, Font.NORMAL));
        parrafo.setAlignment(Paragraph.ALIGN_JUSTIFIED);
        parrafo.setSpacingAfter(2);
        parrafo.add(texto);
        // Creamos una celda
        celda = new PdfPCell(parrafo);
        celda.setColspan(3);
        celda.setHorizontalAlignment(PdfPCell.ALIGN_JUSTIFIED);
        celda.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        // Añadimos el párafo a la celda
        celda.addElement(parrafo);
        // Añadimos la  celda a la tabla
        tabla.addCell(celda);

        // Agregamos la tabla al documento
        document.add(tabla);

        // Cerramos el documento
        document.close();

    }

    private boolean crearCarpeta(HttpServletRequest request, HttpServletResponse response) {

        boolean ok;
        String carpeta;

        carpeta = request.getParameter("archivo");
        File directorio = new File(ruta + carpeta);
        ok = directorio.mkdir();
        return ok;
    }

    private boolean importarcomoxml(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        boolean ok = true;

        try {
            PrintWriter out = response.getWriter();
            Part fichero = request.getPart("fichero");
            
            /*Leer un fichero XML*/
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            org.w3c.dom.Document document = documentBuilder.parse(fichero.getInputStream());
            
            /*Corregir errores del documento*/
            document.getDocumentElement().normalize();
            //out.println("Elemento raiz:" + document.getDocumentElement().getNodeName());
            /*Se procesa con métodos idénticos al DOM*/
            NodeList listaUsuarios = document.getElementsByTagName("usuario");
            for (int temp = 0; temp < listaUsuarios.getLength(); temp++) {
                /*Obtener un elemento de una lista, equivalente a get(i)*/
                Node nodo = listaUsuarios.item(temp);
                /*Mostramos la salida*/
                out.println("Elemento:" + nodo.getNodeName());
                /*preguntamos por el tipo nodo*/
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    /*Si es un nodo lo convierto en elemento*/
                    org.w3c.dom.Element element = (org.w3c.dom.Element) nodo;
                    //out.println("id: " + element.getAttribute("id"));
                    //out.println("Nombre: " + element.getElementsByTagName("nombre").item(0).getTextContent());
                    //out.println("username: " + element.getElementsByTagName("username").item(0).getTextContent());
                    //out.println("password: " + element.getElementsByTagName("password").item(0).getTextContent());
                    
                    /*Construimos el nombre de usuario y su contraseña*/
                    String username = element.getElementsByTagName("username").item(0).getTextContent();
                    String password = element.getElementsByTagName("password").item(0).getTextContent();
                    /*Y lo agregamos al fichero de usuarios*/
                    usuarios.setProperty(username, password);
                }
            }
            /*Finalmente grabamos el fichero*/
            usuarios.store(new FileWriter(ruta + ficheroUsuarios), "");
        } catch (SAXException | ParserConfigurationException ex) {
            Logger.getLogger(editortexto.class.getName()).log(Level.SEVERE, null, ex);
            ok = false;
        }

        return ok;
    }

    private void exportarcomoXml(HttpServletRequest request, HttpServletResponse response) {

        try {
            /*Creamos un objeto para construir el documento*/
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //Creamos un nuevo documento
            org.w3c.dom.Document doc = docBuilder.newDocument();
            //Creamos el elemento raíz
            org.w3c.dom.Element rootElement = doc.createElement("usuarios");
            //Se añade el elemento creado al documento
            doc.appendChild(rootElement);
            /*Cogemos las keys del objeto usuario.
            Enumeration es como un iterator, más antiguo*/
            Enumeration listaUsuarios = usuarios.propertyNames();
            int i = 0;
            /*Recorremos el array de claves del objeto usuario, que serian los
            logins de los usuarios*/
            while (listaUsuarios.hasMoreElements()) {
                //Extraemos la propiedad
                String propiedad = (String) listaUsuarios.nextElement();
                //Y consultamos su clave
                String clave = usuarios.getProperty(propiedad);

                //Creamos el elemento xml <usuario>
                org.w3c.dom.Element usuario = doc.createElement("usuario");
                //Lo añadimos a <usuarios>
                rootElement.appendChild(usuario);

                //Creamos un atributo id para el elemento usuario
                Attr attr = doc.createAttribute("id");
                attr.setValue(String.valueOf(i));
                //Se lo añadimos como atributo
                usuario.setAttributeNode(attr);

                //Creamos el elemento login
                org.w3c.dom.Element login = doc.createElement("login");
                //Le añadimos como texto la clave
                login.appendChild(doc.createTextNode(propiedad));
                //Lo añadimos al elemento <usuario>
                usuario.appendChild(login);

                //Creamos el elemento <password>
                org.w3c.dom.Element password = doc.createElement("password");
                //Le añadimos el texto
                password.appendChild(doc.createTextNode(clave));
                //Y se lo añadimos a usuario
                usuario.appendChild(password);
            }
            
            //ESTO ES ASÍ !NO TOCAR¡
            //Grabamos el fichero
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            // StreamResult result = new StreamResult(new File("C:\\archivo.xml"));
            // Si se quiere mostrar por la consola...
            // StreamResult result = new StreamResult(System.out);
            
            //Lo enviamos por la salida 
            response.setContentType("text/xml");
            response.setHeader("Content-disposition","attachment");
            StreamResult result = new StreamResult(response.getOutputStream());

            
            transformer.transform(source, result);

        } catch (ParserConfigurationException | TransformerException | IOException ex) {
            Logger.getLogger(editortexto.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
