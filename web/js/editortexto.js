/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$(inicializar);

function inicializar() {
    S("dialogoBusqueda").dialog({
       autoOpen:false,
       modal:true
       
    });
    
    $("#busqueda").click(function() {
        mostrarDirectorioJSON();
        
    });
}

function mostrarDirectorioJSON() {
    
    $.post("editortexto",
    {
        accion:"listar"
    },
    function(carpetas) {
        alert(carpetas);
    }, "json");
    
    
}
